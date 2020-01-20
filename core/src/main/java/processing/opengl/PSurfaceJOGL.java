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

import org.jetbrains.annotations.NotNull;

import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import processing.core.PGraphics;
import processing.core.PSurface;

public class PSurfaceJOGL implements PSurface {

    public PJOGL pgl;
    protected GLOffscreenAutoDrawable drawable;

    public PSurfaceJOGL(PGraphics graphics) {
        this.pgl = (PJOGL) ((PGraphicsOpenGL) graphics).pgl;
    }

    public void init() {
        GLProfile profile = GLProfile.getDefault();
        GLCapabilities caps = new GLCapabilities(profile);
        caps.setAlphaBits(PGL.REQUESTED_ALPHA_BITS);
        caps.setDepthBits(PGL.REQUESTED_DEPTH_BITS);
        caps.setStencilBits(PGL.REQUESTED_STENCIL_BITS);
        caps.setSampleBuffers(true);
        caps.setNumSamples(PGL.smoothToSamples(0));
        caps.setBackgroundOpaque(true);
        caps.setOnscreen(true);
        pgl.setCaps(caps);

        drawable = GLDrawableFactory.getFactory(profile).createOffscreenAutoDrawable(
                null,
                caps,
                null,
                1,
                1
        );
        drawable.setContext(drawable.createContext(null), true);
        drawable.invoke(true, glAutoDrawable -> {
            pgl.getGL(glAutoDrawable);
            pgl.init(glAutoDrawable);
            return true;
        });
    }

    @Override
    public void render(@NotNull Function0<Unit> function) {
        drawable.invoke(true, glAutoDrawable -> {
            function.invoke();
            return true;
        });
    }

    public float getPixelScale() {
        return 1;
    }

}
