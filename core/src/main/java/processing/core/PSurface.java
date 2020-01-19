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

package processing.core;

import org.jetbrains.annotations.NotNull;

import kotlin.Unit;
import kotlin.jvm.functions.Function0;

public interface PSurface {

  // renderer that doesn't draw to the screen
  public void initOffscreen(PApplet sketch);

  // considering removal in favor of separate Component classes for appropriate renderers
  // (i.e. for Java2D or a generic Image surface, but not PDF, debatable for GL or FX)
  //public Component initComponent(PApplet sketch);

  //public Frame initFrame(PApplet sketch, Color backgroundColor,
//  public void initFrame(PApplet sketch, int backgroundColor,
//                        int deviceIndex, boolean fullScreen, boolean spanDisplays);
  public void initFrame(PApplet sketch);

  /**
   * Get the native window object associated with this drawing surface.
   * For Java2D, this will be an AWT Frame object. For OpenGL, the window.
   * The data returned here is subject to the whims of the renderer,
   * and using this method means you're willing to deal with underlying
   * implementation changes and that you won't throw a fit like a toddler
   * if your code breaks sometime in the future.
   */
  public Object getNative();

  //

  // Just call these on an AWT Frame object stored in PApplet.
  // Silly, but prevents a lot of rewrite and extra methods for little benefit.
  // However, maybe prevents us from having to document the 'frame' variable?

  /** Show or hide the window. */
  public void setVisible(boolean visible);

  void render(@NotNull Function0<Unit> function);
}
