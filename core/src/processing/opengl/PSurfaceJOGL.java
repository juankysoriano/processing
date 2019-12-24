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
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import com.jogamp.nativewindow.NativeSurface;
import com.jogamp.nativewindow.util.Dimension;
import com.jogamp.nativewindow.util.PixelFormat;
import com.jogamp.nativewindow.util.PixelRectangle;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLException;
import com.jogamp.opengl.GLProfile;
import com.jogamp.nativewindow.MutableGraphicsConfiguration;
import com.jogamp.newt.Display;
import com.jogamp.newt.Display.PointerIcon;
import com.jogamp.newt.awt.NewtCanvasAWT;
import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.opengl.util.FPSAnimator;


import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.core.PImage;
import processing.core.PSurface;


public class PSurfaceJOGL implements PSurface {
  /** Selected GL profile */
  public static GLProfile profile;

  public PJOGL pgl;

  protected GLWindow window;
  protected FPSAnimator animator;

  private Thread drawExceptionHandler;

  protected PApplet sketch;
  protected PGraphics graphics;

  protected int sketchWidth0;
  protected int sketchHeight0;
  protected int sketchWidth;
  protected int sketchHeight;

  protected Display display;

  protected NewtCanvasAWT canvas;

  protected int windowScaleFactor;

  protected float[] currentPixelScale = {0, 0};

  protected boolean external = false;

  public PSurfaceJOGL(PGraphics graphics) {
    this.graphics = graphics;
    this.pgl = (PJOGL) ((PGraphicsOpenGL)graphics).pgl;
  }

  public Object getNative() {
    return window;
  }

  public void initGL() {
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
      } else throw new RuntimeException(PGL.UNSUPPORTED_GLPROF_ERROR);
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

  public void setSize(int wide, int high) {
    if (pgl.presentMode()) return;

    // When the surface is set to resizable via surface.setResizable(true),
    // a crash may occur if the user sets the window to size zero.
    // https://github.com/processing/processing/issues/5052
    if (high <= 0) {
      high = 1;
    }
    if (wide <= 0) {
      wide = 1;
    }

    boolean changed = sketchWidth != wide || sketchHeight != high;

    sketchWidth = wide;
    sketchHeight = high;

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
      animator.setFPS((int)fps);
      pgl.setFps(fps);
      animator.start();
    }
  }


  public void requestFocus() {
    display.getEDTUtil().invoke(false, new Runnable() {
      @Override
      public void run() {
        window.requestFocus();
      }
    });
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

  private void setCursor(PImage image, int hotspotX, int hotspotY) {
    Display disp = window.getScreen().getDisplay();
    BufferedImage bimg = (BufferedImage)image.getNative();
    DataBufferInt dbuf = (DataBufferInt)bimg.getData().getDataBuffer();
    int[] ipix = dbuf.getData();
    ByteBuffer pixels = ByteBuffer.allocate(ipix.length * 4);
    pixels.asIntBuffer().put(ipix);
    PixelFormat format = PixelFormat.ARGB8888;
    final Dimension size = new Dimension(bimg.getWidth(), bimg.getHeight());
    PixelRectangle pixelrect = new PixelRectangle.GenericPixelRect(format, size, 0, false, pixels);
    final PointerIcon pi = disp.createPointerIcon(pixelrect, hotspotX, hotspotY);
    display.getEDTUtil().invoke(false, new Runnable() {
      @Override
      public void run() {
        window.setPointerVisible(true);
        window.setPointerIcon(pi);
      }
    });
  }

}
