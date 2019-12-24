/* -*- mode: java; c-basic-offset: 2; indent-tabs-mode: nil -*- */

/*
  Part of the Processing project - http://processing.org

  Copyright (c) 2014-15 The Processing Foundation

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

package processing.awt;

import java.awt.Canvas;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.awt.image.*;

import processing.core.PGraphics;
import processing.core.PSurfaceNone;


public class PSurfaceAWT extends PSurfaceNone {
  GraphicsDevice displayDevice;

  // used for canvas to determine whether resizable or not
//  boolean resizable;  // default is false

  // Internally, we know it's always a JFrame (not just a Frame)
//  JFrame frame;
  // Trying Frame again with a11 to see if this avoids some Swing nastiness.
  // In the past, AWT Frames caused some problems on Windows and Linux,
  // but those may not be a problem for our reworked PSurfaceAWT class.
  Frame frame;

  // Note that x and y may not be zero, depending on the display configuration
  Rectangle screenRect;

  // Used for resizing, at least on Windows insets size changes when
  // frame.setResizable() is called, and in resize listener we need
  // to know what size the window was before.
  Insets currentInsets = new Insets(0, 0, 0, 0);

  // 3.0a5 didn't use strategy, and active was shut off during init() w/ retina
//  boolean useStrategy = true;

  Canvas canvas;
//  Component canvas;

//  PGraphics graphics;  // moved to PSurfaceNone

  int sketchWidth;
  int sketchHeight;

  int windowScaleFactor;


  public PSurfaceAWT(PGraphics graphics) {
    //this.graphics = graphics;
    super(graphics);

    /*
    if (checkRetina()) {
//      System.out.println("retina in use");

      // The active-mode rendering seems to be 2x slower, so disable it
      // with retina. On a non-retina machine, however, useActive seems
      // the only (or best) way to handle the rendering.
//      useActive = false;
//      canvas = new JPanel(true) {
//        @Override
//        public void paint(Graphics screen) {
////          if (!sketch.insideDraw) {
//          screen.drawImage(PSurfaceAWT.this.graphics.image, 0, 0, sketchWidth, sketchHeight, null);
////          }
//        }
//      };
      // Under 1.8 and the current 3.0a6 threading regime, active mode w/o
      // strategy is far faster, but perhaps only because it's blitting with
      // flicker--pushing pixels out before the screen has finished rendering.
//      useStrategy = false;
    }
    */
    canvas = new SmoothCanvas();
//    if (useStrategy) {
    //canvas.setIgnoreRepaint(true);
//    }

    // Pass tab key to the sketch, rather than moving between components
    canvas.setFocusTraversalKeysEnabled(false);

  }


//  /**
//   * Handle grabbing the focus on startup. Other renderers can override this
//   * if handling needs to be different. For the AWT, the request is invoked
//   * later on the EDT. Other implementations may not require that, so the
//   * invokeLater() happens in here rather than requiring the caller to wrap it.
//   */
//  @Override
//  void requestFocus() {
////    System.out.println("requesFocus() outer " + EventQueue.isDispatchThread());
//    // for 2.0a6, moving this request to the EDT
//    EventQueue.invokeLater(new Runnable() {
//      public void run() {
//        // Call the request focus event once the image is sure to be on
//        // screen and the component is valid. The OpenGL renderer will
//        // request focus for its canvas inside beginDraw().
//        // http://java.sun.com/j2se/1.4.2/docs/api/java/awt/doc-files/FocusSpec.html
//        // Disabling for 0185, because it causes an assertion failure on OS X
//        // http://code.google.com/p/processing/issues/detail?id=258
//        //        requestFocus();
//
//        // Changing to this version for 0187
//        // http://code.google.com/p/processing/issues/detail?id=279
//        //requestFocusInWindow();
//
//        // For 3.0, just call this directly on the Canvas object
//        if (canvas != null) {
//          //System.out.println("requesting focus " + EventQueue.isDispatchThread());
//          //System.out.println("requesting focus " + frame.isVisible());
//          //canvas.requestFocusInWindow();
//          canvas.requestFocus();
//        }
//      }
//    });
//  }


  // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .


  public class SmoothCanvas extends Canvas {
    private Dimension oldSize = new Dimension(0, 0);
    private Dimension newSize = new Dimension(0, 0);


    // Turns out getParent() returns a JPanel on a JFrame. Yech.
    public Frame getFrame() {
      return frame;
    }


    @Override
    public Dimension getPreferredSize() {
      return new Dimension(sketchWidth, sketchHeight);
    }


    @Override
    public Dimension getMinimumSize() {
      return getPreferredSize();
    }


    @Override
    public Dimension getMaximumSize() {
      //return resizable ? super.getMaximumSize() : getPreferredSize();
      return frame.isResizable() ? super.getMaximumSize() : getPreferredSize();
    }


    @Override
    public void validate() {
      super.validate();
      newSize.width = getWidth();
      newSize.height = getHeight();
//      if (oldSize.equals(newSize)) {
////        System.out.println("validate() return " + oldSize);
//        return;
//      } else {
      if (!oldSize.equals(newSize)) {
//        System.out.println("validate() render old=" + oldSize + " -> new=" + newSize);
        oldSize = newSize;
//        try {
        render();
//        } catch (IllegalStateException ise) {
//          System.out.println(ise.getMessage());
//        }
      }
    }


    @Override
    public void update(Graphics g) {
//      System.out.println("updating");
      paint(g);
    }


    @Override
    public void paint(Graphics screen) {
//      System.out.println("painting");
//      if (useStrategy) {
      render();
      /*
      if (graphics != null) {
        System.out.println("drawing to screen " + canvas);
        screen.drawImage(graphics.image, 0, 0, sketchWidth, sketchHeight, null);
      }
      */

//      } else {
////        new Exception("painting").printStackTrace(System.out);
////        if (graphics.image != null) { // && !sketch.insideDraw) {
//        if (onscreen != null) {
////          synchronized (graphics.image) {
//          // Needs the width/height to be set so that retina images are properly scaled down
////          screen.drawImage(graphics.image, 0, 0, sketchWidth, sketchHeight, null);
//          synchronized (offscreenLock) {
//            screen.drawImage(onscreen, 0, 0, sketchWidth, sketchHeight, null);
//          }
//        }
//      }
    }
  }

    /*
    @Override
    public void addNotify() {
//      System.out.println("adding notify");
      super.addNotify();
      // prior to Java 7 on OS X, this no longer works [121222]
//    createBufferStrategy(2);
    }
    */


  synchronized protected void render() {
    if (canvas.isDisplayable() &&
        graphics.image != null) {
      if (canvas.getBufferStrategy() == null) {
        canvas.createBufferStrategy(2);
      }
      BufferStrategy strategy = canvas.getBufferStrategy();
      if (strategy != null) {
        // Render single frame
//        try {
        do {
          // The following loop ensures that the contents of the drawing buffer
          // are consistent in case the underlying surface was recreated
          do {
            Graphics2D draw = (Graphics2D) strategy.getDrawGraphics();
            // draw to width/height, since this may be a 2x image
            draw.drawImage(graphics.image, 0, 0, sketchWidth, sketchHeight, null);
            draw.dispose();
          } while (strategy.contentsRestored());

          // Display the buffer
          strategy.show();

          // Repeat the rendering if the drawing buffer was lost
        } while (strategy.contentsLost());
      }
    }
  }


  /*
  protected void blit() {
    // Other folks that call render() (i.e. paint()) are already on the EDT.
    // We need to be using the EDT since we're messing with the Canvas
    // object and BufferStrategy and friends.
    //EventQueue.invokeLater(new Runnable() {
    //public void run() {
    //((SmoothCanvas) canvas).render();
    //}
    //});

    if (useStrategy) {
      // Not necessary to be on the EDT to update BufferStrategy
      //((SmoothCanvas) canvas).render();
      render();
    } else {
      if (graphics.image != null) {
        BufferedImage graphicsImage = (BufferedImage) graphics.image;
        if (offscreen == null ||
          offscreen.getWidth() != graphicsImage.getWidth() ||
          offscreen.getHeight() != graphicsImage.getHeight()) {
          System.out.println("creating new image");
          offscreen = (BufferedImage)
            canvas.createImage(graphicsImage.getWidth(),
                               graphicsImage.getHeight());
//          off = offscreen.getGraphics();
        }
//        synchronized (offscreen) {
        Graphics2D off = (Graphics2D) offscreen.getGraphics();
//        off.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1));
        off.drawImage(graphicsImage, 0, 0, null);
//        }
        off.dispose();
        synchronized (offscreenLock) {
          BufferedImage temp = onscreen;
          onscreen = offscreen;
          offscreen = temp;
        }
        canvas.repaint();
      }
    }
  }
  */


  /*
  public Frame initOffscreen() {
    Frame dummy = new Frame();
    dummy.pack();  // get legit AWT graphics
    // but don't show it
    return dummy;
  }
  */

  /*
  @Override
  public Component initComponent(PApplet sketch) {
    this.sketch = sketch;

    // needed for getPreferredSize() et al
    sketchWidth = sketch.sketchWidth();
    sketchHeight = sketch.sketchHeight();

    return canvas;
  }
  */

  @Override
  public Object getNative() {
    return canvas;
  }


//  public Toolkit getToolkit() {
//    return canvas.getToolkit();
//  }


  /*
  @Override
  public void placeWindow(int[] location) {
    setFrameSize(); //sketchWidth, sketchHeight);

    if (location != null) {
      // a specific location was received from the Runner
      // (applet has been run more than once, user placed window)
      frame.setLocation(location[0], location[1]);

    } else {  // just center on screen
      // Can't use frame.setLocationRelativeTo(null) because it sends the
      // frame to the main display, which undermines the --display setting.
      frame.setLocation(screenRect.x + (screenRect.width - sketchWidth) / 2,
                        screenRect.y + (screenRect.height - sketchHeight) / 2);
    }
    Point frameLoc = frame.getLocation();
    if (frameLoc.y < 0) {
      // Windows actually allows you to place frames where they can't be
      // closed. Awesome. http://dev.processing.org/bugs/show_bug.cgi?id=1508
      frame.setLocation(frameLoc.x, 30);
    }

//    if (backgroundColor != null) {
//      ((JFrame) frame).getContentPane().setBackground(backgroundColor);
//    }

    setCanvasSize(); //sketchWidth, sketchHeight);

    frame.addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent e) {
        System.exit(0);
      }
    });

    // handle frame resizing events
    setupFrameResizeListener();

    // all set for rockin
    if (sketch.getGraphics().displayable()) {
      frame.setVisible(true);
    }
  }
  */


  private void setCanvasSize() {
//    System.out.format("setting canvas size %d %d%n", sketchWidth, sketchHeight);
//    new Exception().printStackTrace(System.out);
    int contentW = Math.max(sketchWidth, MIN_WINDOW_WIDTH);
    int contentH = Math.max(sketchHeight, MIN_WINDOW_HEIGHT);

    canvas.setBounds((contentW - sketchWidth)/2,
                     (contentH - sketchHeight)/2,
                     sketchWidth, sketchHeight);
  }


  /** Resize frame for these sketch (canvas) dimensions. */
  private Dimension setFrameSize() {  //int sketchWidth, int sketchHeight) {
    // https://github.com/processing/processing/pull/3162
    frame.addNotify();  // using instead of show() to add the peer [fry]

//    System.out.format("setting frame size %d %d %n", sketchWidth, sketchHeight);
//    new Exception().printStackTrace(System.out);
    currentInsets = frame.getInsets();
    int windowW = Math.max(sketchWidth, MIN_WINDOW_WIDTH) +
      currentInsets.left + currentInsets.right;
    int windowH = Math.max(sketchHeight, MIN_WINDOW_HEIGHT) +
      currentInsets.top + currentInsets.bottom;
    frame.setSize(windowW, windowH);
    return new Dimension(windowW, windowH);
  }


  private void setFrameCentered() {
    // Can't use frame.setLocationRelativeTo(null) because it sends the
    // frame to the main display, which undermines the --display setting.
    frame.setLocation(screenRect.x + (screenRect.width - sketchWidth) / 2,
                      screenRect.y + (screenRect.height - sketchHeight) / 2);
  }


  /** Hide the menu bar, make the Frame undecorated, set it to screenRect. */
  private void setFullFrame() {
    // Tried to use this to fix the 'present' mode issue.
    // Did not help, and the screenRect setup seems to work fine.
    //frame.setExtendedState(Frame.MAXIMIZED_BOTH);

    // https://github.com/processing/processing/pull/3162
    //frame.dispose();  // release native resources, allows setUndecorated()
    frame.removeNotify();
    frame.setUndecorated(true);
    frame.addNotify();

    // this may be the bounds of all screens
    frame.setBounds(screenRect);
    // will be set visible in placeWindow() [3.0a10]
    //frame.setVisible(true);  // re-add native resources
  }

  // needs to resize the frame, which will resize the canvas, and so on...
  @Override
  public void setSize(int wide, int high) {
    // When the surface is set to resizable via surface.setResizable(true),
    // a crash may occur if the user sets the window to size zero.
    // https://github.com/processing/processing/issues/5052
    if (high <= 0) {
      high = 1;
    }
    if (wide <= 0) {
      wide = 1;
    }

//    if (PApplet.DEBUG) {
//      //System.out.format("frame visible %b, setSize(%d, %d) %n", frame.isVisible(), wide, high);
//      new Exception(String.format("setSize(%d, %d)", wide, high)).printStackTrace(System.out);
//    }

    //if (wide == sketchWidth && high == sketchHeight) {  // doesn't work on launch
    if (wide == sketchWidth && high == sketchHeight &&
        (frame == null || currentInsets.equals(frame.getInsets()))) {
//      if (PApplet.DEBUG) {
//        new Exception("w/h unchanged " + wide + " " + high).printStackTrace(System.out);
//      }
      return;  // unchanged, don't rebuild everything
    }

    sketchWidth = wide * windowScaleFactor;
    sketchHeight = high * windowScaleFactor;

//    canvas.setSize(wide, high);
//    frame.setSize(wide, high);
    if (frame != null) {  // skip if just a canvas
      setFrameSize(); //wide, high);
    }
    setCanvasSize();
//    if (frame != null) {
//      frame.setLocationRelativeTo(null);
//    }

    //initImage(graphics, wide, high);

    //throw new RuntimeException("implement me, see readme.md");
//    sketch.width = wide;
//    sketch.height = high;

    // set PGraphics variables for width/height/pixelWidth/pixelHeight
    graphics.setSize(wide, high);
//    System.out.println("out of setSize()");
  }


  //public void initImage(PGraphics gr, int wide, int high) {
  /*
  @Override
  public void initImage(PGraphics graphics) {
    GraphicsConfiguration gc = canvas.getGraphicsConfiguration();
    // If not realized (off-screen, i.e the Color Selector Tool), gc will be null.
    if (gc == null) {
      System.err.println("GraphicsConfiguration null in initImage()");
      GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
      gc = ge.getDefaultScreenDevice().getDefaultConfiguration();
    }

    // Formerly this was broken into separate versions based on offscreen or
    // not, but we may as well create a compatible image; it won't hurt, right?
    int wide = graphics.width * graphics.pixelFactor;
    int high = graphics.height * graphics.pixelFactor;
    graphics.image = gc.createCompatibleImage(wide, high);
  }
  */


//  @Override
//  public Component getComponent() {
//    return canvas;
//  }


//  @Override
//  public void setSmooth(int level) {
//  }


  /*
  private boolean checkRetina() {
    if (PApplet.platform == PConstants.MACOSX) {
      // This should probably be reset each time there's a display change.
      // A 5-minute search didn't turn up any such event in the Java 7 API.
      // Also, should we use the Toolkit associated with the editor window?
      final String javaVendor = System.getProperty("java.vendor");
      if (javaVendor.contains("Oracle")) {
        GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice device = env.getDefaultScreenDevice();

        try {
          Field field = device.getClass().getDeclaredField("scale");
          if (field != null) {
            field.setAccessible(true);
            Object scale = field.get(device);

            if (scale instanceof Integer && ((Integer)scale).intValue() == 2) {
              return true;
            }
          }
        } catch (Exception ignore) { }
      }
    }
    return false;
  }
  */


  /** Get the bounds rectangle for all displays. */
  static Rectangle getDisplaySpan() {
    Rectangle bounds = new Rectangle();
    GraphicsEnvironment environment =
      GraphicsEnvironment.getLocalGraphicsEnvironment();
    for (GraphicsDevice device : environment.getScreenDevices()) {
      for (GraphicsConfiguration config : device.getConfigurations()) {
        Rectangle2D.union(bounds, config.getBounds(), bounds);
      }
    }
    return bounds;
  }


  /*
  private void checkDisplaySize() {
    if (canvas.getGraphicsConfiguration() != null) {
      GraphicsDevice displayDevice = getGraphicsConfiguration().getDevice();

      if (displayDevice != null) {
        Rectangle screenRect =
          displayDevice.getDefaultConfiguration().getBounds();

        displayWidth = screenRect.width;
        displayHeight = screenRect.height;
      }
    }
  }
  */

//  /**
//   * (No longer in use) Use reflection to call
//   * <code>com.apple.eawt.FullScreenUtilities.setWindowCanFullScreen(window, true);</code>
//   */
//  static void macosxFullScreenEnable(Window window) {
//    try {
//      Class<?> util = Class.forName("com.apple.eawt.FullScreenUtilities");
//      Class params[] = new Class[] { Window.class, Boolean.TYPE };
//      Method method = util.getMethod("setWindowCanFullScreen", params);
//      method.invoke(util, window, true);
//
//    } catch (ClassNotFoundException cnfe) {
//      // ignored
//    } catch (Exception e) {
//      e.printStackTrace();
//    }
//  }
//
//
//  /**
//   * (No longer in use) Use reflection to call
//   * <code>com.apple.eawt.Application.getApplication().requestToggleFullScreen(window);</code>
//   */
//  static void macosxFullScreenToggle(Window window) {
//    try {
//      Class<?> appClass = Class.forName("com.apple.eawt.Application");
//
//      Method getAppMethod = appClass.getMethod("getApplication");
//      Object app = getAppMethod.invoke(null, new Object[0]);
//
//      Method requestMethod =
//        appClass.getMethod("requestToggleFullScreen", Window.class);
//      requestMethod.invoke(app, window);
//
//    } catch (ClassNotFoundException cnfe) {
//      // ignored
//    } catch (Exception e) {
//      e.printStackTrace();
//    }
//  }


  //////////////////////////////////////////////////////////////


  /*
  // disabling for now; requires Java 1.7 and "precise" semantics are odd...
  // returns 0.1 for tick-by-tick scrolling on OS X, but it's not a matter of
  // calling ceil() on the value: 1.5 goes to 1, but 2.3 goes to 2.
  // "precise" is a whole different animal, so add later API to shore that up.
  static protected Method preciseWheelMethod;
  static {
    try {
      preciseWheelMethod = MouseWheelEvent.class.getMethod("getPreciseWheelRotation", new Class[] { });
    } catch (Exception e) {
      // ignored, the method will just be set to null
    }
  }
  */


  /*
  public void addListeners(Component comp) {
    comp.addMouseListener(this);
    comp.addMouseWheelListener(this);
    comp.addMouseMotionListener(this);
    comp.addKeyListener(this);
    comp.addFocusListener(this);
  }


  public void removeListeners(Component comp) {
    comp.removeMouseListener(this);
    comp.removeMouseWheelListener(this);
    comp.removeMouseMotionListener(this);
    comp.removeKeyListener(this);
    comp.removeFocusListener(this);
  }
  */


//  /**
//   * Call to remove, then add, listeners to a component.
//   * Avoids issues with double-adding.
//   */
//  public void updateListeners(Component comp) {
//    removeListeners(comp);
//    addListeners(comp);
//  }



  // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .

  @Override
  public Thread createThread() {
    return new AnimationThread() {
      @Override
      public void callDraw() {
        render();
      }
    };
  }


  void debug(String format, Object ... args) {
    System.out.format(format + "%n", args);
  }
}
