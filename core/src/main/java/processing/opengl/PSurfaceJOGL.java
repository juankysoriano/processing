/* -*- mode: java; c-basic-offset: 2; indent-tabs-mode: nil -*- */

/*
  Part of the Processing project - http://processing.org

  Copyright (c) 2012-15 The Processing Foundation
  Copyright (c) 2004-12 Ben Fry and Casey Reas
  Copyright (c) 2001-04 Massachusetts Institute of Technology

  This library is free software; you can redistribute it and/or
  modify it under the terms of the GNU Lesser General Public
  License as published by the Free Software Foundation, version 2.1.

  This library is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General
  Public License along with this library; if not, write to the
  Free Software Foundation, Inc., 59 Temple Place, Suite 330,
  Boston, MA  02111-1307  USA
*/

package processing.opengl;

import java.awt.Component;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import javax.swing.ImageIcon;

import com.jogamp.nativewindow.NativeSurface;
import com.jogamp.nativewindow.ScalableSurface;
import com.jogamp.nativewindow.util.Dimension;
import com.jogamp.nativewindow.util.PixelFormat;
import com.jogamp.nativewindow.util.PixelRectangle;
import com.jogamp.opengl.GLAnimatorControl;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLException;
import com.jogamp.opengl.GLProfile;
import com.jogamp.nativewindow.MutableGraphicsConfiguration;
import com.jogamp.nativewindow.WindowClosingProtocol;
import com.jogamp.newt.Display;
import com.jogamp.newt.Display.PointerIcon;
import com.jogamp.newt.NewtFactory;
import com.jogamp.newt.Screen;
import com.jogamp.newt.awt.NewtCanvasAWT;
import com.jogamp.newt.event.InputEvent;
import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.opengl.util.FPSAnimator;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.core.PImage;
import processing.core.PSurface;
import processing.event.KeyEvent;
import processing.event.MouseEvent;

public class PSurfaceJOGL implements PSurface {
    /**
     * Selected GL profile
     */
    public static GLProfile profile;

    public PJOGL pgl;

    protected GLWindow window;
    protected FPSAnimator animator;
    protected Rectangle screenRect;

    private Thread drawExceptionHandler;

    protected PApplet sketch;
    protected PGraphics graphics;

    protected int sketchWidth0;
    protected int sketchHeight0;
    protected int sketchWidth;
    protected int sketchHeight;

    protected Display display;
    protected Screen screen;
    protected Rectangle displayRect;
    protected Throwable drawException;
    private final Object drawExceptionMutex = new Object();

    protected NewtCanvasAWT canvas;

    protected int windowScaleFactor;

    protected float[] currentPixelScale = {0, 0};

    protected boolean external = false;

    public PSurfaceJOGL(PGraphics graphics) {
        this.graphics = graphics;
        this.pgl = (PJOGL) ((PGraphicsOpenGL) graphics).pgl;
    }

    public void initOffscreen(PApplet sketch) {
        this.sketch = sketch;

        sketchWidth = sketch.sketchWidth();
        sketchHeight = sketch.sketchHeight();

        if (window != null) {
            canvas = new NewtCanvasAWT(window);
            canvas.setBounds(0, 0, window.getWidth(), window.getHeight());
            canvas.setFocusable(true);
        }
    }

    public void initFrame(PApplet sketch) {
        this.sketch = sketch;
        initGL();
        initWindow();
        initListeners();
    }

    public Object getNative() {
        return window;
    }

    protected void initDisplay() {

    }

    protected void initGL() {
//  System.out.println("*******************************");
        if (profile == null) {
            if (PJOGL.profile == 1) {
                try {
                    profile = GLProfile.getGL2ES1();
                } catch (GLException ex) {
                    profile = GLProfile.getMaxFixedFunc(true);
                }
            } else if (PJOGL.profile == 2) {
                try {
                    profile = GLProfile.getGL2ES2();

                    // workaround for https://jogamp.org/bugzilla/show_bug.cgi?id=1347
                    if (!profile.isHardwareRasterizer()) {
                        GLProfile hardware = GLProfile.getMaxProgrammable(true);
                        if (hardware.isGL2ES2()) {
                            profile = hardware;
                        }
                    }

                } catch (GLException ex) {
                    profile = GLProfile.getMaxProgrammable(true);
                }
            } else if (PJOGL.profile == 3) {
                try {
                    profile = GLProfile.getGL2GL3();
                } catch (GLException ex) {
                    profile = GLProfile.getMaxProgrammable(true);
                }
                if (!profile.isGL3()) {
                    PGraphics.showWarning("Requested profile GL3 but is not available, got: " + profile);
                }
            } else if (PJOGL.profile == 4) {
                try {
                    profile = GLProfile.getGL4ES3();
                } catch (GLException ex) {
                    profile = GLProfile.getMaxProgrammable(true);
                }
                if (!profile.isGL4()) {
                    PGraphics.showWarning("Requested profile GL4 but is not available, got: " + profile);
                }
            } else {
                throw new RuntimeException(PGL.UNSUPPORTED_GLPROF_ERROR);
            }
        }

        // Setting up the desired capabilities;
        GLCapabilities caps = new GLCapabilities(profile);
        caps.setAlphaBits(PGL.REQUESTED_ALPHA_BITS);
        caps.setDepthBits(PGL.REQUESTED_DEPTH_BITS);
        caps.setStencilBits(PGL.REQUESTED_STENCIL_BITS);

//  caps.setPBuffer(false);
//  caps.setFBO(false);

//    pgl.reqNumSamples = PGL.smoothToSamples(graphics.smooth);
        caps.setSampleBuffers(true);
        caps.setNumSamples(PGL.smoothToSamples(graphics.smooth));
        caps.setBackgroundOpaque(true);
        caps.setOnscreen(true);
        pgl.setCaps(caps);
    }

    protected void initWindow() {
        window = GLWindow.create(pgl.getCaps());
        window.setSize(1,1);
    }

    protected void initListeners() {
        DrawListener drawlistener = new DrawListener();
        window.addGLEventListener(drawlistener);
    }

    protected void initAnimator() {
        if (PApplet.platform == PConstants.WINDOWS) {
            // Force Windows to keep timer resolution high by
            // sleeping for time which is not a multiple of 10 ms.
            // See section "Clocks and Timers on Windows":
            //   https://blogs.oracle.com/dholmes/entry/inside_the_hotspot_vm_clocks
            Thread highResTimerThread = new Thread(() -> {
                try {
                    Thread.sleep(Long.MAX_VALUE);
                } catch (InterruptedException ignore) {
                }
            }, "HighResTimerThread");
            highResTimerThread.setDaemon(true);
            highResTimerThread.start();
        }

        animator = new FPSAnimator(window, 60);
        drawException = null;
        animator.setUncaughtExceptionHandler(new GLAnimatorControl.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(final GLAnimatorControl animator,
                                          final GLAutoDrawable drawable,
                                          final Throwable cause) {
                synchronized (drawExceptionMutex) {
                    drawException = cause;
                    drawExceptionMutex.notify();
                }
            }
        });

        drawExceptionHandler = new Thread(new Runnable() {
            public void run() {
                synchronized (drawExceptionMutex) {
                    try {
                        while (drawException == null) {
                            drawExceptionMutex.wait();
                        }
                        // System.err.println("Caught exception: " + drawException.getMessage());
                        if (drawException != null) {
                            Throwable cause = drawException.getCause();
                            if (cause instanceof ThreadDeath) {
                                // System.out.println("caught ThreadDeath");
                                // throw (ThreadDeath)cause;
                            } else if (cause instanceof RuntimeException) {
                                throw (RuntimeException) cause;
                            } else if (cause instanceof UnsatisfiedLinkError) {
                                throw new UnsatisfiedLinkError(cause.getMessage());
                            } else if (cause == null) {
                                throw new RuntimeException(drawException.getMessage());
                            } else {
                                throw new RuntimeException(cause);
                            }
                        }
                    } catch (InterruptedException e) {
                        return;
                    }
                }
            }
        });
        drawExceptionHandler.start();
    }

    @Override
    public void setTitle(final String title) {
    }

    @Override
    public void setVisible(final boolean visible) {
        window.setVisible(visible);
    }

    @Override
    public void setResizable(final boolean resizable) { }

    public void setIcon(PImage icon) {
        PGraphics.showWarning("Window icons for OpenGL sketches can only be set in settings()\n" +
                                      "using PJOGL.setIcon(filename).");
    }

    @Override
    public void setAlwaysOnTop(final boolean always) {
    }

    @Override
    public void placeWindow(int[] location, int[] editorLocation) {
    }

    public void startThread() {
        if (animator != null) {
            animator.start();
        }
    }

    public void pauseThread() {
        if (animator != null) {
            animator.pause();
        }
    }

    public void resumeThread() {
        if (animator != null) {
            animator.resume();
        }
    }

    public boolean stopThread() {
        if (drawExceptionHandler != null) {
            drawExceptionHandler.interrupt();
            drawExceptionHandler = null;
        }
        if (animator != null) {
            return animator.stop();
        } else {
            return false;
        }
    }

    public boolean isStopped() {
        if (animator != null) {
            return !animator.isAnimating();
        } else {
            return true;
        }
    }

    public void setLocation(final int x, final int y) {
        window.setTopLevelPosition(x, y);
    }

    public void setSize(int wide, int high) {
        if (pgl.presentMode()) {
            return;
        }

        // When the surface is set to resizable via surface.setResizable(true),
        // a crash may occur if the user sets the window to size zero.
        // https://github.com/processing/processing/issues/5052
        if (high <= 0) {
            high = 1;
        }
        if (wide <= 0) {
            wide = 1;
        }

        boolean changed = sketch.width != wide || sketch.height != high;

        sketchWidth = wide;
        sketchHeight = high;

        sketch.setSize(wide, high);
        graphics.setSize(wide, high);

        if (changed) {
            window.setSize(wide * windowScaleFactor, high * windowScaleFactor);
        }
    }

    public float getPixelScale() {
        if (graphics.pixelDensity == 1) {
            return 1;
        }

        if (PApplet.platform == PConstants.MACOSX) {
            return getCurrentPixelScale();
        }

        return 2;
    }

    private float getCurrentPixelScale() {
        // Even if the graphics are retina, the user might have moved the window
        // into a non-retina monitor, so we need to check
        window.getCurrentSurfaceScale(currentPixelScale);
        return currentPixelScale[0];
    }

    public Component getComponent() {
        return canvas;
    }

    public void setSmooth(int level) {
        pgl.reqNumSamples = level;
        GLCapabilities caps = new GLCapabilities(profile);
        caps.setAlphaBits(PGL.REQUESTED_ALPHA_BITS);
        caps.setDepthBits(PGL.REQUESTED_DEPTH_BITS);
        caps.setStencilBits(PGL.REQUESTED_STENCIL_BITS);
        caps.setSampleBuffers(true);
        caps.setNumSamples(pgl.reqNumSamples);
        caps.setBackgroundOpaque(true);
        caps.setOnscreen(true);
        NativeSurface target = window.getNativeSurface();
        MutableGraphicsConfiguration config = (MutableGraphicsConfiguration) target.getGraphicsConfiguration();
        config.setChosenCapabilities(caps);
    }

    public void setFrameRate(float fps) {
        if (fps < 1) {
            PGraphics.showWarning(
                    "The OpenGL renderer cannot have a frame rate lower than 1.\n" +
                            "Your sketch will run at 1 frame per second.");
            fps = 1;
        } else if (fps > 1000) {
            PGraphics.showWarning(
                    "The OpenGL renderer cannot have a frame rate higher than 1000.\n" +
                            "Your sketch will run at 1000 frames per second.");
            fps = 1000;
        }
        if (animator != null) {
            animator.stop();
            animator.setFPS((int) fps);
            pgl.setFps(fps);
            animator.start();
        }
    }

    public void requestFocus() {
        window.requestFocus();
    }

    class DrawListener implements GLEventListener {
        public void display(GLAutoDrawable drawable) {
        }

        public void dispose(GLAutoDrawable drawable) {
//      sketch.dispose();
        }


        public void init(GLAutoDrawable drawable) {
            pgl.getGL(drawable);
            pgl.init(drawable);
            sketch.handleDraw();
        }

        public void reshape(GLAutoDrawable drawable, int x, int y, int w, int h) {
            pgl.resetFBOLayer();
            pgl.getGL(drawable);
            float scale = PApplet.platform == PConstants.MACOSX ?
                    getCurrentPixelScale() : getPixelScale();
            setSize((int) (w / scale), (int) (h / scale));
        }
    }

    protected class NEWTWindowListener implements com.jogamp.newt.event.WindowListener {
        public NEWTWindowListener() {
            super();
        }

        @Override
        public void windowGainedFocus(com.jogamp.newt.event.WindowEvent arg0) {
            sketch.focused = true;
            sketch.focusGained();
        }

        @Override
        public void windowLostFocus(com.jogamp.newt.event.WindowEvent arg0) {
            sketch.focused = false;
            sketch.focusLost();
        }

        @Override
        public void windowDestroyNotify(com.jogamp.newt.event.WindowEvent arg0) {
            sketch.exit();
        }

        @Override
        public void windowDestroyed(com.jogamp.newt.event.WindowEvent arg0) {
            sketch.exit();
        }

        @Override
        public void windowMoved(com.jogamp.newt.event.WindowEvent arg0) {
            if (external) {
                sketch.frameMoved(window.getX(), window.getY());
            }
        }

        @Override
        public void windowRepaint(com.jogamp.newt.event.WindowUpdateEvent arg0) {
        }

        @Override
        public void windowResized(com.jogamp.newt.event.WindowEvent arg0) {
        }
    }

    // NEWT mouse listener
    protected class NEWTMouseListener extends com.jogamp.newt.event.MouseAdapter {
        public NEWTMouseListener() {
            super();
        }

        @Override
        public void mousePressed(com.jogamp.newt.event.MouseEvent e) {
            nativeMouseEvent(e, MouseEvent.PRESS);
        }

        @Override
        public void mouseReleased(com.jogamp.newt.event.MouseEvent e) {
            nativeMouseEvent(e, MouseEvent.RELEASE);
        }

        @Override
        public void mouseClicked(com.jogamp.newt.event.MouseEvent e) {
            nativeMouseEvent(e, MouseEvent.CLICK);
        }

        @Override
        public void mouseDragged(com.jogamp.newt.event.MouseEvent e) {
            nativeMouseEvent(e, MouseEvent.DRAG);
        }

        @Override
        public void mouseMoved(com.jogamp.newt.event.MouseEvent e) {
            nativeMouseEvent(e, MouseEvent.MOVE);
        }

        @Override
        public void mouseWheelMoved(com.jogamp.newt.event.MouseEvent e) {
            nativeMouseEvent(e, MouseEvent.WHEEL);
        }

        @Override
        public void mouseEntered(com.jogamp.newt.event.MouseEvent e) {
//      System.out.println("enter");
            nativeMouseEvent(e, MouseEvent.ENTER);
        }

        @Override
        public void mouseExited(com.jogamp.newt.event.MouseEvent e) {
//      System.out.println("exit");
            nativeMouseEvent(e, MouseEvent.EXIT);
        }
    }

    // NEWT key listener
    protected class NEWTKeyListener extends com.jogamp.newt.event.KeyAdapter {
        public NEWTKeyListener() {
            super();
        }

        @Override
        public void keyPressed(com.jogamp.newt.event.KeyEvent e) {
            nativeKeyEvent(e, KeyEvent.PRESS);
        }

        @Override
        public void keyReleased(com.jogamp.newt.event.KeyEvent e) {
            nativeKeyEvent(e, KeyEvent.RELEASE);
        }

        public void keyTyped(com.jogamp.newt.event.KeyEvent e) {
            nativeKeyEvent(e, KeyEvent.TYPE);
        }
    }

    protected void nativeMouseEvent(com.jogamp.newt.event.MouseEvent nativeEvent,
                                    int peAction) {
        int modifiers = nativeEvent.getModifiers();
        int peModifiers = modifiers &
                (InputEvent.SHIFT_MASK |
                        InputEvent.CTRL_MASK |
                        InputEvent.META_MASK |
                        InputEvent.ALT_MASK);

        int peButton = 0;
        switch (nativeEvent.getButton()) {
            case com.jogamp.newt.event.MouseEvent.BUTTON1:
                peButton = PConstants.LEFT;
                break;
            case com.jogamp.newt.event.MouseEvent.BUTTON2:
                peButton = PConstants.CENTER;
                break;
            case com.jogamp.newt.event.MouseEvent.BUTTON3:
                peButton = PConstants.RIGHT;
                break;
        }

        int peCount = 0;
        if (peAction == MouseEvent.WHEEL) {
            // Invert wheel rotation count so it matches JAVA2D's
            // https://github.com/processing/processing/issues/3840
            peCount = -(nativeEvent.isShiftDown() ? (int) nativeEvent.getRotation()[0] :
                    (int) nativeEvent.getRotation()[1]);
        } else {
            peCount = nativeEvent.getClickCount();
        }

        int scale;
        if (PApplet.platform == PConstants.MACOSX) {
            scale = (int) getCurrentPixelScale();
        } else {
            scale = (int) getPixelScale();
        }
        int sx = nativeEvent.getX() / scale;
        int sy = nativeEvent.getY() / scale;
        int mx = sx;
        int my = sy;

        if (pgl.presentMode()) {
            mx -= (int) pgl.presentX;
            my -= (int) pgl.presentY;
            if (peAction == KeyEvent.RELEASE &&
                    pgl.insideStopButton(sx, sy - screenRect.height / windowScaleFactor)) {
                sketch.exit();
            }
            if (mx < 0 || sketchWidth < mx || my < 0 || sketchHeight < my) {
                return;
            }
        }

        MouseEvent me = new MouseEvent(nativeEvent, nativeEvent.getWhen(),
                                       peAction, peModifiers,
                                       mx, my,
                                       peButton,
                                       peCount
        );

        sketch.postEvent(me);
    }

    protected void nativeKeyEvent(com.jogamp.newt.event.KeyEvent nativeEvent,
                                  int peAction) {
        int peModifiers = nativeEvent.getModifiers() &
                (InputEvent.SHIFT_MASK |
                        InputEvent.CTRL_MASK |
                        InputEvent.META_MASK |
                        InputEvent.ALT_MASK);

        short code = nativeEvent.getKeyCode();
        char keyChar;
        int keyCode;
        if (isPCodedKey(code)) {
            keyCode = mapToPConst(code);
            keyChar = PConstants.CODED;
        } else if (isHackyKey(code)) {
            // we can return only one char for ENTER, let it be \n everywhere
            keyCode = code == com.jogamp.newt.event.KeyEvent.VK_ENTER ?
                    PConstants.ENTER : code;
            keyChar = hackToChar(code, nativeEvent.getKeyChar());
        } else {
            keyCode = code;
            keyChar = nativeEvent.getKeyChar();
        }

        // From http://jogamp.org/deployment/v2.1.0/javadoc/jogl/javadoc/com/jogamp/newt/event/KeyEvent.html
        // public final short getKeySymbol()
        // Returns the virtual key symbol reflecting the current keyboard layout.
        // public final short getKeyCode()
        // Returns the virtual key code using a fixed mapping to the US keyboard layout.
        // In contrast to key symbol, key code uses a fixed US keyboard layout and therefore is keyboard layout independent.
        // E.g. virtual key code VK_Y denotes the same physical key regardless whether keyboard layout QWERTY or QWERTZ is active. The key symbol of the former is VK_Y, where the latter produces VK_Y.
        KeyEvent ke = new KeyEvent(nativeEvent, nativeEvent.getWhen(),
                                   peAction, peModifiers,
                                   keyChar,
                                   keyCode,
                                   nativeEvent.isAutoRepeat()
        );

        sketch.postEvent(ke);

        if (!isPCodedKey(code) && !isHackyKey(code)) {
            if (peAction == KeyEvent.PRESS) {
                // Create key typed event
                // TODO: combine dead keys with the following key
                KeyEvent tke = new KeyEvent(nativeEvent, nativeEvent.getWhen(),
                                            KeyEvent.TYPE, peModifiers,
                                            keyChar,
                                            0,
                                            nativeEvent.isAutoRepeat()
                );

                sketch.postEvent(tke);
            }
        }
    }

    private static boolean isPCodedKey(short code) {
        return code == com.jogamp.newt.event.KeyEvent.VK_UP ||
                code == com.jogamp.newt.event.KeyEvent.VK_DOWN ||
                code == com.jogamp.newt.event.KeyEvent.VK_LEFT ||
                code == com.jogamp.newt.event.KeyEvent.VK_RIGHT ||
                code == com.jogamp.newt.event.KeyEvent.VK_ALT ||
                code == com.jogamp.newt.event.KeyEvent.VK_CONTROL ||
                code == com.jogamp.newt.event.KeyEvent.VK_SHIFT ||
                code == com.jogamp.newt.event.KeyEvent.VK_WINDOWS;
    }

    // Why do we need this mapping?
    // Relevant discussion and links here:
    // http://forum.jogamp.org/Newt-wrong-keycode-for-key-td4033690.html#a4033697
    // (I don't think this is a complete solution).
    private static int mapToPConst(short code) {
        switch (code) {
            case com.jogamp.newt.event.KeyEvent.VK_UP:
                return PConstants.UP;
            case com.jogamp.newt.event.KeyEvent.VK_DOWN:
                return PConstants.DOWN;
            case com.jogamp.newt.event.KeyEvent.VK_LEFT:
                return PConstants.LEFT;
            case com.jogamp.newt.event.KeyEvent.VK_RIGHT:
                return PConstants.RIGHT;
            case com.jogamp.newt.event.KeyEvent.VK_ALT:
                return PConstants.ALT;
            case com.jogamp.newt.event.KeyEvent.VK_CONTROL:
                return PConstants.CONTROL;
            case com.jogamp.newt.event.KeyEvent.VK_SHIFT:
                return PConstants.SHIFT;
            case com.jogamp.newt.event.KeyEvent.VK_WINDOWS:
                return java.awt.event.KeyEvent.VK_META;
            default:
                return code;
        }
    }

    private static boolean isHackyKey(short code) {
        switch (code) {
            case com.jogamp.newt.event.KeyEvent.VK_BACK_SPACE:
            case com.jogamp.newt.event.KeyEvent.VK_TAB:
            case com.jogamp.newt.event.KeyEvent.VK_ENTER:
            case com.jogamp.newt.event.KeyEvent.VK_ESCAPE:
            case com.jogamp.newt.event.KeyEvent.VK_DELETE:
                return true;
        }
        return false;
    }

    private static char hackToChar(short code, char def) {
        switch (code) {
            case com.jogamp.newt.event.KeyEvent.VK_BACK_SPACE:
                return PConstants.BACKSPACE;
            case com.jogamp.newt.event.KeyEvent.VK_TAB:
                return PConstants.TAB;
            case com.jogamp.newt.event.KeyEvent.VK_ENTER:
                return PConstants.ENTER;
            case com.jogamp.newt.event.KeyEvent.VK_ESCAPE:
                return PConstants.ESC;
            case com.jogamp.newt.event.KeyEvent.VK_DELETE:
                return PConstants.DELETE;
        }
        return def;
    }

    // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .

    class CursorInfo {
        PImage image;
        int x, y;

        CursorInfo(PImage image, int x, int y) {
            this.image = image;
            this.x = x;
            this.y = y;
        }

        void set() {
            setCursor(image, x, y);
        }
    }

    static Map<Integer, CursorInfo> cursors = new HashMap<>();
    static Map<Integer, String> cursorNames = new HashMap<>();

    static {
        cursorNames.put(PConstants.ARROW, "arrow");
        cursorNames.put(PConstants.CROSS, "cross");
        cursorNames.put(PConstants.WAIT, "wait");
        cursorNames.put(PConstants.MOVE, "move");
        cursorNames.put(PConstants.HAND, "hand");
        cursorNames.put(PConstants.TEXT, "text");
    }

    public void setCursor(int kind) {
        if (!cursorNames.containsKey(kind)) {
            PGraphics.showWarning("Unknown cursor type: " + kind);
            return;
        }
        CursorInfo cursor = cursors.get(kind);
        if (cursor == null) {
            String name = cursorNames.get(kind);
            if (name != null) {
                ImageIcon icon =
                        new ImageIcon(getClass().getResource("cursors/" + name + ".png"));
                PImage img = new PImage(icon.getImage());
                // Most cursors just use the center as the hotspot...
                int x = img.width / 2;
                int y = img.height / 2;
                // ...others are more specific
                if (kind == PConstants.ARROW) {
                    x = 10;
                    y = 7;
                } else if (kind == PConstants.HAND) {
                    x = 12;
                    y = 8;
                } else if (kind == PConstants.TEXT) {
                    x = 16;
                    y = 22;
                }
                cursor = new CursorInfo(img, x, y);
                cursors.put(kind, cursor);
            }
        }
        if (cursor != null) {
            cursor.set();
        } else {
            PGraphics.showWarning("Cannot load cursor type: " + kind);
        }
    }

    public void setCursor(PImage image, int hotspotX, int hotspotY) {
        Display disp = window.getScreen().getDisplay();
        BufferedImage bimg = (BufferedImage) image.getNative();
        DataBufferInt dbuf = (DataBufferInt) bimg.getData().getDataBuffer();
        int[] ipix = dbuf.getData();
        ByteBuffer pixels = ByteBuffer.allocate(ipix.length * 4);
        pixels.asIntBuffer().put(ipix);
        PixelFormat format = PixelFormat.ARGB8888;
        final Dimension size = new Dimension(bimg.getWidth(), bimg.getHeight());
        PixelRectangle pixelrect = new PixelRectangle.GenericPixelRect(format, size, 0, false, pixels);
        final PointerIcon pi = disp.createPointerIcon(pixelrect, hotspotX, hotspotY);
        window.setPointerVisible(true);
        window.setPointerIcon(pi);
    }

    public void showCursor() {
        window.setPointerVisible(true);

    }

    public void hideCursor() {
        window.setPointerVisible(false);
    }
}
