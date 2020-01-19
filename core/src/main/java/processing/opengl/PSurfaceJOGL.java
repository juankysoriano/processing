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

import com.jogamp.opengl.*;
import com.jogamp.newt.opengl.GLWindow;

import org.jetbrains.annotations.NotNull;

import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.core.PSurface;

public class PSurfaceJOGL implements PSurface {
    /**
     * Selected GL profile
     */
    public static GLProfile profile;
    public PJOGL pgl;
    protected GLWindow window;
    protected PApplet sketch;
    protected PGraphics graphics;
    protected int sketchWidth;
    protected int sketchHeight;

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

    protected void initGL() {
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

        GLCapabilities caps = new GLCapabilities(profile);
        caps.setAlphaBits(PGL.REQUESTED_ALPHA_BITS);
        caps.setDepthBits(PGL.REQUESTED_DEPTH_BITS);
        caps.setStencilBits(PGL.REQUESTED_STENCIL_BITS);
        caps.setSampleBuffers(true);
        caps.setNumSamples(PGL.smoothToSamples(graphics.smooth));
        caps.setBackgroundOpaque(true);
        caps.setOnscreen(true);
        pgl.setCaps(caps);
    }

    protected void initWindow() {
        window = GLWindow.create(pgl.getCaps());
        window.setVisible(true);
    }

    protected void initListeners() {
        DrawListener drawlistener = new DrawListener();
        window.addGLEventListener(drawlistener);
    }

    @Override
    public void setVisible(final boolean visible) {
        window.setVisible(visible);
    }

    @Override
    public void render(@NotNull Function0<Unit> function) {
        window.invoke(true, glAutoDrawable -> {
            function.invoke();
            return true;
        });
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

    class DrawListener implements GLEventListener {
        public void display(GLAutoDrawable drawable) {
        }

        public void dispose(GLAutoDrawable drawable) {
        }


        public void init(GLAutoDrawable drawable) {
            pgl.getGL(drawable);
            pgl.init(drawable);
        }

        public void reshape(GLAutoDrawable drawable, int x, int y, int w, int h) {
        }
    }
}
