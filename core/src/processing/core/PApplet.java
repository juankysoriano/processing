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

package processing.core;

// dummy object for backwards compatibility, plus the select methods

// before calling settings() to get displayWidth/Height
// handleSettings() and displayDensity()
// used to present the fullScreen() warning about Spaces on OS X

// inside runSketch() to warn users about headless

// used by loadImage()
// allows us to remove our own MediaTracker code

// used by selectInput(), selectOutput(), selectFolder()
import javax.imageio.ImageIO;

// set the look and feel, if specified

// used by link()

// used by desktopFile() method

// loadXML() error handling

import java.awt.image.BufferedImage;
import java.io.*;
import java.lang.reflect.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.text.*;
import java.util.*;
import java.util.regex.*;
import java.util.zip.*;


/**
 * Base class for all sketches that use processing.core.
 * <p/>
 * The <A HREF="https://github.com/processing/processing/wiki/Window-Size-and-Full-Screen">
 * Window Size and Full Screen</A> page on the Wiki has useful information
 * about sizing, multiple displays, full screen, etc.
 * <p/>
 * Processing uses active mode rendering. All animation tasks happen on the
 * "Processing Animation Thread". The setup() and draw() methods are handled
 * by that thread, and events (like mouse movement and key presses, which are
 * fired by the event dispatch thread or EDT) are queued to be safely handled
 * at the end of draw().
 * <p/>
 * Starting with 3.0a6, blit operations are on the EDT, so as not to cause
 * GUI problems with Swing and AWT. In the case of the default renderer, the
 * sketch renders to an offscreen image, then the EDT is asked to bring that
 * image to the screen.
 * <p/>
 * For code that needs to run on the EDT, use EventQueue.invokeLater(). When
 * doing so, be careful to synchronize between that code and the Processing
 * animation thread. That is, you can't call Processing methods from the EDT
 * or at any random time from another thread. Use of a callback function or
 * the registerXxx() methods in PApplet can help ensure that your code doesn't
 * do something naughty.
 * <p/>
 * As of Processing 3.0, we have removed Applet as the base class for PApplet.
 * This means that we can remove lots of legacy code, however one downside is
 * that it's no longer possible (without extra code) to embed a PApplet into
 * another Java application.
 * <p/>
 * As of Processing 3.0, we have discontinued support for versions of Java
 * prior to 1.8. We don't have enough people to support it, and for a
 * project of our (tiny) size, we should be focusing on the future, rather
 * than working around legacy Java code.
 */
public class PApplet implements PConstants {
  /** Full name of the Java version (i.e. 1.5.0_11). */
  static public final String javaVersionName =
    System.getProperty("java.version");

  static public final int javaPlatform;
  static {
    String version = javaVersionName;
    if (javaVersionName.startsWith("1.")) {
      version = version.substring(2);
      javaPlatform = parseInt(version.substring(0, version.indexOf('.')));
    } else {
      // Remove -xxx and .yyy from java.version (@see JEP-223)
      javaPlatform = parseInt(version.replaceAll("-.*","").replaceAll("\\..*",""));
    }
  }

  /**
   * Do not use; javaPlatform or javaVersionName are better options.
   * For instance, javaPlatform is useful when you need a number for
   * comparison, i.e. "if (javaPlatform >= 9)".
   */
  @Deprecated
  public static final float javaVersion = 1 + javaPlatform / 10f;

  /**
   * Current platform in use, one of the
   * PConstants WINDOWS, MACOSX, MACOS9, LINUX or OTHER.
   */
  static public int platform;

  static {
    String osname = System.getProperty("os.name");

    if (osname.indexOf("Mac") != -1) {
      platform = MACOSX;

    } else if (osname.indexOf("Windows") != -1) {
      platform = WINDOWS;

    } else if (osname.equals("Linux")) {  // true for the ibm vm
      platform = LINUX;

    } else {
      platform = OTHER;
    }
  }

  //  public String sketchPath;

  static final boolean DEBUG = false;
//  static final boolean DEBUG = true;

  //  /**
//   * Exception thrown when size() is called the first time.
//   * <p>
//   * This is used internally so that setup() is forced to run twice
//   * when the renderer is changed. This is the only way for us to handle
//   * invoking the new renderer while also in the midst of rendering.
//   */
//  static public class RendererChangeException extends RuntimeException { }

  /**
   * true if no size() command has been executed. This is used to wait until
   * a size has been set before placing in the window and showing it.
   */
//  public boolean defaultSize;

//  /** Storage for the current renderer size to avoid re-allocation. */
//  Dimension currentSize = new Dimension();

  //  /**
//   * Confirms if a Processing program is running inside a web browser. This
//   * variable is "true" if the program is online and "false" if not.
//   */
//  @Deprecated
//  public boolean online = false;
//  // This is deprecated because it's poorly named (and even more poorly
//  // understood). Further, we'll probably be removing applets soon, in which
//  // case this won't work at all. If you want this feature, you can check
//  // whether getAppletContext() returns null.

  // public, but undocumented.. removing for 3.0a5
//  /**
//   * true if the animation thread is paused.
//   */
//  public volatile boolean paused;

  // messages to send if attached as an external vm

  //  static public final String ARGS_SPAN_DISPLAYS = "--span";

  static final String ERROR_MIN_MAX =
    "Cannot use min() or max() on an empty array.";


  // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .

  //  public Frame getFrame() {
//    return frame;
//  }
//
//
//  public void setFrame(Frame frame) {
//    this.frame = frame;
//  }


  // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .


//  /**
//   * Applet initialization. This can do GUI work because the components have
//   * not been 'realized' yet: things aren't visible, displayed, etc.
//   */
//  public void init() {
////    println("init() called " + Integer.toHexString(hashCode()));
//    // using a local version here since the class variable is deprecated
////    Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
////    screenWidth = screen.width;
////    screenHeight = screen.height;
//
//    defaultSize = true;
//    finished = false; // just for clarity
//
//    // this will be cleared by draw() if it is not overridden
//    looping = true;
//    redraw = true;  // draw this guy at least once
//    firstMouse = true;
//
//    // calculated dynamically on first call
////    // Removed in 2.1.2, brought back for 2.1.3. Usually sketchPath is set
////    // inside runSketch(), but if this sketch takes care of calls to init()
////    // when PApplet.main() is not used (i.e. it's in a Java application).
////    // THe path needs to be set here so that loadXxxx() functions work.
////    if (sketchPath == null) {
////      sketchPath = calcSketchPath();
////    }
//
//    // set during Surface.initFrame()
////    // Figure out the available display width and height.
////    // No major problem if this fails, we have to try again anyway in
////    // handleDraw() on the first (== 0) frame.
////    checkDisplaySize();
//
////    // Set the default size, until the user specifies otherwise
////    int w = sketchWidth();
////    int h = sketchHeight();
////    defaultSize = (w == DEFAULT_WIDTH) && (h == DEFAULT_HEIGHT);
////
////    g = makeGraphics(w, h, sketchRenderer(), null, true);
////    // Fire component resize event
////    setSize(w, h);
////    setPreferredSize(new Dimension(w, h));
////
////    width = g.width;
////    height = g.height;
//
//    surface.startThread();
//  }


  // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .
//  public boolean sketchSpanDisplays() {
//    //return false;
//    return spanDisplays;
//  }

  //  /**
//   * Called by the browser or applet viewer to inform this applet
//   * that it is being reclaimed and that it should destroy
//   * any resources that it has allocated.
//   * <p/>
//   * destroy() supposedly gets called as the applet viewer
//   * is shutting down the applet. stop() is called
//   * first, and then destroy() to really get rid of things.
//   * no guarantees on when they're run (on browser quit, or
//   * when moving between pages), though.
//   */
//  @Override
//  public void destroy() {
//    this.dispose();
//  }

  //////////////////////////////////////////////////////////////

  //////////////////////////////////////////////////////////////


  /*
  protected void resizeRenderer(int newWidth, int newHeight) {
    debug("resizeRenderer request for " + newWidth + " " + newHeight);
    if (width != newWidth || height != newHeight) {
      debug("  former size was " + width + " " + height);
      g.setSize(newWidth, newHeight);
      width = newWidth;
      height = newHeight;
    }
  }
  */

  public static PGraphics createGraphics(int w, int h) {
    return createGraphics(w, h, JAVA2D);
  }


  /**
   * ( begin auto-generated from createGraphics.xml )
   *
   * Creates and returns a new <b>PGraphics</b> object of the types P2D or
   * P3D. Use this class if you need to draw into an off-screen graphics
   * buffer. The PDF renderer requires the filename parameter. The DXF
   * renderer should not be used with <b>createGraphics()</b>, it's only
   * built for use with <b>beginRaw()</b> and <b>endRaw()</b>.<br />
   * <br />
   * It's important to call any drawing functions between <b>beginDraw()</b>
   * and <b>endDraw()</b> statements. This is also true for any functions
   * that affect drawing, such as <b>smooth()</b> or <b>colorMode()</b>.<br/>
   * <br/> the main drawing surface which is completely opaque, surfaces
   * created with <b>createGraphics()</b> can have transparency. This makes
   * it possible to draw into a graphics and maintain the alpha channel. By
   * using <b>save()</b> to write a PNG or TGA file, the transparency of the
   * graphics object will be honored. Note that transparency levels are
   * binary: pixels are either complete opaque or transparent. For the time
   * being, this means that text characters will be opaque blocks. This will
   * be fixed in a future release (<a
   * href="http://code.google.com/p/processing/issues/detail?id=80">Issue 80</a>).
   *
   * ( end auto-generated )
   * <h3>Advanced</h3>
   * Create an offscreen PGraphics object for drawing. This can be used
   * for bitmap or vector images drawing or rendering.
   * <UL>
   * <LI>Do not use "new PGraphicsXxxx()", use this method. This method
   * ensures that internal variables are set up properly that tie the
   * new graphics context back to its parent PApplet.
   * <LI>The basic way to create bitmap images is to use the <A
   * HREF="http://processing.org/reference/saveFrame_.html">saveFrame()</A>
   * function.
   * <LI>If you want to create a really large scene and write that,
   * first make sure that you've allocated a lot of memory in the Preferences.
   * <LI>If you want to create images that are larger than the screen,
   * you should create your own PGraphics object, draw to that, and use
   * <A HREF="http://processing.org/reference/save_.html">save()</A>.
   * <PRE>
   *
   * PGraphics big;
   *
   * void setup() {
   *   big = createGraphics(3000, 3000);
   *
   *   big.beginDraw();
   *   big.background(128);
   *   big.line(20, 1800, 1800, 900);
   *   // etc..
   *   big.endDraw();
   *
   *   // make sure the file is written to the sketch folder
   *   big.save("big.tif");
   * }
   *
   * </PRE>
   * <LI>It's important to always wrap drawing to createGraphics() with
   * beginDraw() and endDraw() (beginFrame() and endFrame() prior to
   * revision 0115). The reason is that the renderer needs to know when
   * drawing has stopped, so that it can update itself internally.
   * This also handles calling the defaults() method, for people familiar
   * with that.
   * <LI>With Processing 0115 and later, it's possible to write images in
   * formats other than the default .tga and .tiff. The exact formats and
   * background information can be found in the developer's reference for
   * <A HREF="http://dev.processing.org/reference/core/javadoc/processing/core/PImage.html#save(java.lang.String)">PImage.save()</A>.
   * </UL>
   *
   * @webref rendering
   * @param w width in pixels
   * @param h height in pixels
   * @param renderer Either P2D, P3D, or PDF
   * @see PGraphics#PGraphics
   *
   */
  public static PGraphics createGraphics(int w, int h, String renderer) {
    return createGraphics(w, h, renderer, null);
  }


  /**
   * Create an offscreen graphics surface for drawing, in this case
   * for a renderer that writes to a file (such as PDF or DXF).
   * @param path the name of the file (can be an absolute or relative path)
   */
  public static PGraphics createGraphics(int w, int h,
                                  String renderer, String path) {
    return makeGraphics(w, h, renderer, path, false);
    /*
    if (path != null) {
      path = savePath(path);
    }
    PGraphics pg = makeGraphics(w, h, renderer, path, false);
    //pg.parent = this;  // why wasn't setParent() used before 3.0a6?
    //pg.setParent(this);  // make save() work
    // Nevermind, parent is set in makeGraphics()
    return pg;
    */
  }


//  public PGraphics makePrimaryGraphics(int wide, int high) {
//    return makeGraphics(wide, high, sketchRenderer(), null, true);
//  }


  /**
   * Version of createGraphics() used internally.
   * @param path A path (or null if none), can be absolute or relative ({@link PApplet#savePath} will be called)
   */
  protected static PGraphics makeGraphics(int w, int h,
                                   String renderer, String path,
                                   boolean primary) {
    try {
      Class<?> rendererClass =
        Thread.currentThread().getContextClassLoader().loadClass(renderer);

      Constructor<?> constructor = rendererClass.getConstructor(new Class[] { });
      PGraphics pg = (PGraphics) constructor.newInstance();

      pg.setPrimary(primary);
      if (path != null) {
        pg.setPath(savePath(path));
      }
//      pg.setQuality(sketchQuality());
//      if (!primary) {
//        surface.initImage(pg, w, h);
//      }
      pg.setSize(w, h);

      // everything worked, return it
      return pg;

    } catch (InvocationTargetException ite) {
      String msg = ite.getTargetException().getMessage();
      if ((msg != null) &&
          (msg.indexOf("no jogl in java.library.path") != -1)) {
        // Is this true anymore, since the JARs contain the native libs?
        throw new RuntimeException("The jogl library folder needs to be " +
          "specified with -Djava.library.path=/path/to/jogl");

      } else {
        Throwable target = ite.getTargetException();
        /*
        // removing for 3.2, we'll see
        if (platform == MACOSX) {
          target.printStackTrace(System.out);  // OS X bug (still true?)
        }
        */
        throw new RuntimeException(target.getMessage());
      }

    } catch (ClassNotFoundException cnfe) {
//      if (cnfe.getMessage().indexOf("processing.opengl.PGraphicsOpenGL") != -1) {
//        throw new RuntimeException(openglError +
//                                   " (The library .jar file is missing.)");
//      } else {
      throw new RuntimeException("The " + renderer +
                                         " renderer is not in the class path.");

    } catch (Exception e) {
      if ((e instanceof IllegalArgumentException) ||
          (e instanceof NoSuchMethodException) ||
          (e instanceof IllegalAccessException)) {
        if (e.getMessage().contains("cannot be <= 0")) {
          // IllegalArgumentException will be thrown if w/h is <= 0
          // http://code.google.com/p/processing/issues/detail?id=983
          throw new RuntimeException(e);

        } else {
          String msg = renderer + " needs to be updated " +
            "for the current release of Processing.";
          throw new RuntimeException(msg);
        }
      } else {
        /*
        if (platform == MACOSX) {
          e.printStackTrace(System.out);  // OS X bug (still true?)
        }
        */
        throw new RuntimeException(e.getMessage());
      }
    }
  }

  /**
   * ( begin auto-generated from createImage.xml )
   *
   * Creates a new PImage (the datatype for storing images). This provides a
   * fresh buffer of pixels to play with. Set the size of the buffer with the
   * <b>width</b> and <b>height</b> parameters. The <b>format</b> parameter
   * defines how the pixels are stored. See the PImage reference for more information.
   * <br/> <br/>
   * Be sure to include all three parameters, specifying only the width and
   * height (but no format) will produce a strange error.
   * <br/> <br/>
   * Advanced users please note that createImage() should be used instead of
   * the syntax <tt>new PImage()</tt>.
   *
   * ( end auto-generated )
   * <h3>Advanced</h3>
   * Preferred method of creating new PImage objects, ensures that a
   * reference to the parent PApplet is included, which makes save() work
   * without needing an absolute path.
   *
   * @webref image
   * @param w width in pixels
   * @param h height in pixels
   * @param format Either RGB, ARGB, ALPHA (grayscale alpha channel)
   * @see PImage
   * @see PGraphics
   */
  public static PImage createImage(int w, int h, int format) {
    return new PImage(w, h, format);
  }


  //////////////////////////////////////////////////////////////

  //  /** Not official API, not guaranteed to work in the future. */
//  public boolean canDraw() {
//    return g != null && (looping || redraw);
//  }


  //////////////////////////////////////////////////////////////

//////////////////////////////////////////////////////////////

  // i am focused man, and i'm not afraid of death.
  // and i'm going all out. i circle the vultures in a van
  // and i run the block.


  /*
  static private final String shellQuoted(String arg) {
    if (arg.indexOf(' ') != -1) {
      // check to see if already quoted
      if ((arg.charAt(0) != '\"' || arg.charAt(arg.length()-1) != '\"') &&
          (arg.charAt(0) != '\'' || arg.charAt(arg.length()-1) != '\'')) {

        // see which quotes we can use
        if (arg.indexOf('\"') == -1) {
          // if no double quotes, try those first
          return "\"" + arg + "\"";

        } else if (arg.indexOf('\'') == -1) {
          // if no single quotes, let's use those
          return "'" + arg + "'";
        }
      }
    }
    return arg;
  }
  */

  //////////////////////////////////////////////////////////////

  // SCREEN GRABASS

  //////////////////////////////////////////////////////////////

  // CURSOR

  //

  //////////////////////////////////////////////////////////////

/**
   * ( begin auto-generated from print.xml )
   *
   * Writes to the console area of the Processing environment. This is often
   * helpful for looking at the data a program is producing. The companion
   * function <b>println()</b> works like <b>print()</b>, but creates a new
   * line of text for each call to the function. Individual elements can be
   * separated with quotes ("") and joined with the addition operator (+).<br />
   * <br />
   * Beginning with release 0125, to print the contents of an array, use
   * println(). There's no sensible way to do a <b>print()</b> of an array,
   * because there are too many possibilities for how to separate the data
   * (spaces, commas, etc). If you want to print an array as a single line,
   * use <b>join()</b>. With <b>join()</b>, you can choose any delimiter you
   * like and <b>print()</b> the result.<br />
   * <br />
   * Using <b>print()</b> on an object will output <b>null</b>, a memory
   * location that may look like "@10be08," or the result of the
   * <b>toString()</b> method from the object that's being printed. Advanced
   * users who want more useful output when calling <b>print()</b> on their
   * own classes can add a <b>toString()</b> method to the class that returns
   * a String.
   *
   * ( end auto-generated )
 * @webref output:text_area
 * @usage IDE
 * @param what data to print to console
 * @see PApplet#println()
 * @see PApplet#printArray(Object)
 * @see PApplet#join(String[], char)
 */
  static public void print(byte what) {
    System.out.print(what);
    System.out.flush();
  }

  static public void print(boolean what) {
    System.out.print(what);
    System.out.flush();
  }

  static public void print(char what) {
    System.out.print(what);
    System.out.flush();
  }

  static public void print(int what) {
    System.out.print(what);
    System.out.flush();
  }

  static public void print(long what) {
    System.out.print(what);
    System.out.flush();
  }

  static public void print(float what) {
    System.out.print(what);
    System.out.flush();
  }

  static public void print(double what) {
    System.out.print(what);
    System.out.flush();
  }

  static public void print(String what) {
    System.out.print(what);
    System.out.flush();
  }

  /**
   * @param variables list of data, separated by commas
   */
  static public void print(Object... variables) {
    StringBuilder sb = new StringBuilder();
    for (Object o : variables) {
      if (sb.length() != 0) {
        sb.append(" ");
      }
      if (o == null) {
        sb.append("null");
      } else {
        sb.append(o.toString());
      }
    }
    System.out.print(sb.toString());
  }


  /*
  static public void print(Object what) {
    if (what == null) {
      // special case since this does fuggly things on > 1.1
      System.out.print("null");
    } else {
      System.out.println(what.toString());
    }
  }
  */


  /**
   * ( begin auto-generated from println.xml )
   *
   * Writes to the text area of the Processing environment's console. This is
   * often helpful for looking at the data a program is producing. Each call
   * to this function creates a new line of output. Individual elements can
   * be separated with quotes ("") and joined with the string concatenation
   * operator (+). See <b>print()</b> for more about what to expect in the output.
   * <br/><br/> <b>println()</b> on an array (by itself) will write the
   * contents of the array to the console. This is often helpful for looking
   * at the data a program is producing. A new line is put between each
   * element of the array. This function can only print one dimensional
   * arrays. For arrays with higher dimensions, the result will be closer to
   * that of <b>print()</b>.
   *
   * ( end auto-generated )
 * @webref output:text_area
 * @usage IDE
 * @see PApplet#print(byte)
 * @see PApplet#printArray(Object)
 */
  static public void println() {
    System.out.println();
  }


/**
 * @param what data to print to console
 */
  static public void println(byte what) {
    System.out.println(what);
    System.out.flush();
  }

  static public void println(boolean what) {
    System.out.println(what);
    System.out.flush();
  }

  static public void println(char what) {
    System.out.println(what);
    System.out.flush();
  }

  static public void println(int what) {
    System.out.println(what);
    System.out.flush();
  }

  static public void println(long what) {
    System.out.println(what);
    System.out.flush();
  }

  static public void println(float what) {
    System.out.println(what);
    System.out.flush();
  }

  static public void println(double what) {
    System.out.println(what);
    System.out.flush();
  }

  static public void println(String what) {
    System.out.println(what);
    System.out.flush();
  }

  /**
   * @param variables list of data, separated by commas
   */
  static public void println(Object... variables) {
//    System.out.println("got " + variables.length + " variables");
    print(variables);
    println();
  }


  /*
  // Breaking this out since the compiler doesn't know the difference between
  // Object... and just Object (with an array passed in). This should take care
  // of the confusion for at least the most common case (a String array).
  // On second thought, we're going the printArray() route, since the other
  // object types are also used frequently.
  static public void println(String[] array) {
    for (int i = 0; i < array.length; i++) {
      System.out.println("[" + i + "] \"" + array[i] + "\"");
    }
    System.out.flush();
  }
  */


  /**
   * For arrays, use printArray() instead. This function causes a warning
   * because the new print(Object...) and println(Object...) functions can't
   * be reliably bound by the compiler.
   */
  static public void println(Object what) {
    if (what == null) {
      System.out.println("null");
    } else if (what.getClass().isArray()) {
      printArray(what);
    } else {
      System.out.println(what.toString());
      System.out.flush();
    }
  }

  /**
   * ( begin auto-generated from printArray.xml )
   *
   * To come...
   *
   * ( end auto-generated )
 * @webref output:text_area
 * @param what one-dimensional array
 * @usage IDE
 * @see PApplet#print(byte)
 * @see PApplet#println()
 */
  static public void printArray(Object what) {
    if (what == null) {
      // special case since this does fuggly things on > 1.1
      System.out.println("null");

    } else {
      String name = what.getClass().getName();
      if (name.charAt(0) == '[') {
        switch (name.charAt(1)) {
        case '[':
          // don't even mess with multi-dimensional arrays (case '[')
          // or anything else that's not int, float, boolean, char
          System.out.println(what);
          break;

        case 'L':
          // print a 1D array of objects as individual elements
          Object poo[] = (Object[]) what;
          for (int i = 0; i < poo.length; i++) {
            if (poo[i] instanceof String) {
              System.out.println("[" + i + "] \"" + poo[i] + "\"");
            } else {
              System.out.println("[" + i + "] " + poo[i]);
            }
          }
          break;

        case 'Z':  // boolean
          boolean zz[] = (boolean[]) what;
          for (int i = 0; i < zz.length; i++) {
            System.out.println("[" + i + "] " + zz[i]);
          }
          break;

        case 'B':  // byte
          byte bb[] = (byte[]) what;
          for (int i = 0; i < bb.length; i++) {
            System.out.println("[" + i + "] " + bb[i]);
          }
          break;

        case 'C':  // char
          char cc[] = (char[]) what;
          for (int i = 0; i < cc.length; i++) {
            System.out.println("[" + i + "] '" + cc[i] + "'");
          }
          break;

        case 'I':  // int
          int ii[] = (int[]) what;
          for (int i = 0; i < ii.length; i++) {
            System.out.println("[" + i + "] " + ii[i]);
          }
          break;

        case 'J':  // int
          long jj[] = (long[]) what;
          for (int i = 0; i < jj.length; i++) {
            System.out.println("[" + i + "] " + jj[i]);
          }
          break;

        case 'F':  // float
          float ff[] = (float[]) what;
          for (int i = 0; i < ff.length; i++) {
            System.out.println("[" + i + "] " + ff[i]);
          }
          break;

        case 'D':  // double
          double dd[] = (double[]) what;
          for (int i = 0; i < dd.length; i++) {
            System.out.println("[" + i + "] " + dd[i]);
          }
          break;

        default:
          System.out.println(what);
        }
      } else {  // not an array
        System.out.println(what);
      }
    }
    System.out.flush();
  }


  static public void debug(String msg) {
    if (DEBUG) println(msg);
  }
  //

  /*
  // not very useful, because it only works for public (and protected?)
  // fields of a class, not local variables to methods
  public void printvar(String name) {
    try {
      Field field = getClass().getDeclaredField(name);
      println(name + " = " + field.get(this));
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  */


  //////////////////////////////////////////////////////////////

  // MATH

  // lots of convenience methods for math with floats.
  // doubles are overkill for processing applets, and casting
  // things all the time is annoying, thus the functions below.

/**
   * ( begin auto-generated from abs.xml )
   *
   * Calculates the absolute value (magnitude) of a number. The absolute
   * value of a number is always positive.
   *
   * ( end auto-generated )
   * @webref math:calculation
   * @param n number to compute
   */
  static public final float abs(float n) {
    return (n < 0) ? -n : n;
  }

  static public final int abs(int n) {
    return (n < 0) ? -n : n;
  }

/**
   * ( begin auto-generated from sq.xml )
   *
   * Squares a number (multiplies a number by itself). The result is always a
   * positive number, as multiplying two negative numbers always yields a
   * positive result. For example, -1 * -1 = 1.
   *
   * ( end auto-generated )
   * @webref math:calculation
   * @param n number to square
   * @see PApplet#sqrt(float)
   */
  static public final float sq(float n) {
    return n*n;
  }

/**
   * ( begin auto-generated from sqrt.xml )
   *
   * Calculates the square root of a number. The square root of a number is
   * always positive, even though there may be a valid negative root. The
   * square root <b>s</b> of number <b>a</b> is such that <b>s*s = a</b>. It
   * is the opposite of squaring.
   *
   * ( end auto-generated )
   * @webref math:calculation
   * @param n non-negative number
   * @see PApplet#pow(float, float)
   * @see PApplet#sq(float)
   */
  static public final float sqrt(float n) {
    return (float)Math.sqrt(n);
  }

/**
   * ( begin auto-generated from log.xml )
   *
   * Calculates the natural logarithm (the base-<i>e</i> logarithm) of a
   * number. This function expects the values greater than 0.0.
   *
   * ( end auto-generated )
   * @webref math:calculation
   * @param n number greater than 0.0
   */
  static public final float log(float n) {
    return (float)Math.log(n);
  }

/**
   * ( begin auto-generated from exp.xml )
   *
   * Returns Euler's number <i>e</i> (2.71828...) raised to the power of the
   * <b>value</b> parameter.
   *
   * ( end auto-generated )
   * @webref math:calculation
   * @param n exponent to raise
   */
  static public final float exp(float n) {
    return (float)Math.exp(n);
  }

/**
   * ( begin auto-generated from pow.xml )
   *
   * Facilitates exponential expressions. The <b>pow()</b> function is an
   * efficient way of multiplying numbers by themselves (or their reciprocal)
   * in large quantities. For example, <b>pow(3, 5)</b> is equivalent to the
   * expression 3*3*3*3*3 and <b>pow(3, -5)</b> is equivalent to 1 / 3*3*3*3*3.
   *
   * ( end auto-generated )
   * @webref math:calculation
   * @param n base of the exponential expression
   * @param e power by which to raise the base
   * @see PApplet#sqrt(float)
   */
  static public final float pow(float n, float e) {
    return (float)Math.pow(n, e);
  }

/**
   * ( begin auto-generated from max.xml )
   *
   * Determines the largest value in a sequence of numbers.
   *
   * ( end auto-generated )
   * @webref math:calculation
   * @param a first number to compare
   * @param b second number to compare
   * @see PApplet#min(float, float, float)
   */
  static public final int max(int a, int b) {
    return (a > b) ? a : b;
  }

  static public final float max(float a, float b) {
    return (a > b) ? a : b;
  }

  /*
  static public final double max(double a, double b) {
    return (a > b) ? a : b;
  }
  */

/**
 * @param c third number to compare
 */
  static public final int max(int a, int b, int c) {
    return (a > b) ? ((a > c) ? a : c) : ((b > c) ? b : c);
  }


  static public final float max(float a, float b, float c) {
    return (a > b) ? ((a > c) ? a : c) : ((b > c) ? b : c);
  }


  /**
   * @param list array of numbers to compare
   */
  static public final int max(int[] list) {
    if (list.length == 0) {
      throw new ArrayIndexOutOfBoundsException(ERROR_MIN_MAX);
    }
    int max = list[0];
    for (int i = 1; i < list.length; i++) {
      if (list[i] > max) max = list[i];
    }
    return max;
  }

  static public final float max(float[] list) {
    if (list.length == 0) {
      throw new ArrayIndexOutOfBoundsException(ERROR_MIN_MAX);
    }
    float max = list[0];
    for (int i = 1; i < list.length; i++) {
      if (list[i] > max) max = list[i];
    }
    return max;
  }


//  /**
//   * Find the maximum value in an array.
//   * Throws an ArrayIndexOutOfBoundsException if the array is length 0.
//   * @param list the source array
//   * @return The maximum value
//   */
  /*
  static public final double max(double[] list) {
    if (list.length == 0) {
      throw new ArrayIndexOutOfBoundsException(ERROR_MIN_MAX);
    }
    double max = list[0];
    for (int i = 1; i < list.length; i++) {
      if (list[i] > max) max = list[i];
    }
    return max;
  }
  */


  static public final int min(int a, int b) {
    return (a < b) ? a : b;
  }

  static public final float min(float a, float b) {
    return (a < b) ? a : b;
  }

  /*
  static public final double min(double a, double b) {
    return (a < b) ? a : b;
  }
  */


  static public final int min(int a, int b, int c) {
    return (a < b) ? ((a < c) ? a : c) : ((b < c) ? b : c);
  }

/**
   * ( begin auto-generated from min.xml )
   *
   * Determines the smallest value in a sequence of numbers.
   *
   * ( end auto-generated )
   * @webref math:calculation
   * @param a first number
   * @param b second number
   * @param c third number
   * @see PApplet#max(float, float, float)
   */
  static public final float min(float a, float b, float c) {
    return (a < b) ? ((a < c) ? a : c) : ((b < c) ? b : c);
  }

  /*
  static public final double min(double a, double b, double c) {
    return (a < b) ? ((a < c) ? a : c) : ((b < c) ? b : c);
  }
  */


  /**
   * @param list array of numbers to compare
   */
  static public final int min(int[] list) {
    if (list.length == 0) {
      throw new ArrayIndexOutOfBoundsException(ERROR_MIN_MAX);
    }
    int min = list[0];
    for (int i = 1; i < list.length; i++) {
      if (list[i] < min) min = list[i];
    }
    return min;
  }

  static public final float min(float[] list) {
    if (list.length == 0) {
      throw new ArrayIndexOutOfBoundsException(ERROR_MIN_MAX);
    }
    float min = list[0];
    for (int i = 1; i < list.length; i++) {
      if (list[i] < min) min = list[i];
    }
    return min;
  }


  /*
   * Find the minimum value in an array.
   * Throws an ArrayIndexOutOfBoundsException if the array is length 0.
   * @param list the source array
   * @return The minimum value
   */
  /*
  static public final double min(double[] list) {
    if (list.length == 0) {
      throw new ArrayIndexOutOfBoundsException(ERROR_MIN_MAX);
    }
    double min = list[0];
    for (int i = 1; i < list.length; i++) {
      if (list[i] < min) min = list[i];
    }
    return min;
  }
  */


  static public final int constrain(int amt, int low, int high) {
    return (amt < low) ? low : ((amt > high) ? high : amt);
  }

/**
   * ( begin auto-generated from constrain.xml )
   *
   * Constrains a value to not exceed a maximum and minimum value.
   *
   * ( end auto-generated )
   * @webref math:calculation
   * @param amt the value to constrain
   * @param low minimum limit
   * @param high maximum limit
   * @see PApplet#max(float, float, float)
   * @see PApplet#min(float, float, float)
   */

  static public final float constrain(float amt, float low, float high) {
    return (amt < low) ? low : ((amt > high) ? high : amt);
  }

/**
   * ( begin auto-generated from sin.xml )
   *
   * Calculates the sine of an angle. This function expects the values of the
   * <b>angle</b> parameter to be provided in radians (values from 0 to
   * 6.28). Values are returned in the range -1 to 1.
   *
   * ( end auto-generated )
   * @webref math:trigonometry
   * @param angle an angle in radians
   * @see PApplet#cos(float)
   * @see PApplet#tan(float)
   * @see PApplet#radians(float)
   */
  static public final float sin(float angle) {
    return (float)Math.sin(angle);
  }

/**
   * ( begin auto-generated from cos.xml )
   *
   * Calculates the cosine of an angle. This function expects the values of
   * the <b>angle</b> parameter to be provided in radians (values from 0 to
   * PI*2). Values are returned in the range -1 to 1.
   *
   * ( end auto-generated )
   * @webref math:trigonometry
   * @param angle an angle in radians
   * @see PApplet#sin(float)
   * @see PApplet#tan(float)
   * @see PApplet#radians(float)
   */
  static public final float cos(float angle) {
    return (float)Math.cos(angle);
  }

/**
   * ( begin auto-generated from tan.xml )
   *
   * Calculates the ratio of the sine and cosine of an angle. This function
   * expects the values of the <b>angle</b> parameter to be provided in
   * radians (values from 0 to PI*2). Values are returned in the range
   * <b>infinity</b> to <b>-infinity</b>.
   *
   * ( end auto-generated )
   * @webref math:trigonometry
   * @param angle an angle in radians
   * @see PApplet#cos(float)
   * @see PApplet#sin(float)
   * @see PApplet#radians(float)
   */
  static public final float tan(float angle) {
    return (float)Math.tan(angle);
  }

/**
   * ( begin auto-generated from asin.xml )
   *
   * The inverse of <b>sin()</b>, returns the arc sine of a value. This
   * function expects the values in the range of -1 to 1 and values are
   * returned in the range <b>-PI/2</b> to <b>PI/2</b>.
   *
   * ( end auto-generated )
   * @webref math:trigonometry
   * @param value the value whose arc sine is to be returned
   * @see PApplet#sin(float)
   * @see PApplet#acos(float)
   * @see PApplet#atan(float)
   */
  static public final float asin(float value) {
    return (float)Math.asin(value);
  }

/**
   * ( begin auto-generated from acos.xml )
   *
   * The inverse of <b>cos()</b>, returns the arc cosine of a value. This
   * function expects the values in the range of -1 to 1 and values are
   * returned in the range <b>0</b> to <b>PI (3.1415927)</b>.
   *
   * ( end auto-generated )
   * @webref math:trigonometry
   * @param value the value whose arc cosine is to be returned
   * @see PApplet#cos(float)
   * @see PApplet#asin(float)
   * @see PApplet#atan(float)
   */
  static public final float acos(float value) {
    return (float)Math.acos(value);
  }

/**
   * ( begin auto-generated from atan.xml )
   *
   * The inverse of <b>tan()</b>, returns the arc tangent of a value. This
   * function expects the values in the range of -Infinity to Infinity
   * (exclusive) and values are returned in the range <b>-PI/2</b> to <b>PI/2 </b>.
   *
   * ( end auto-generated )
   * @webref math:trigonometry
   * @param value -Infinity to Infinity (exclusive)
   * @see PApplet#tan(float)
   * @see PApplet#asin(float)
   * @see PApplet#acos(float)
   */
  static public final float atan(float value) {
    return (float)Math.atan(value);
  }

/**
   * ( begin auto-generated from atan2.xml )
   *
   * Calculates the angle (in radians) from a specified point to the
   * coordinate origin as measured from the positive x-axis. Values are
   * returned as a <b>float</b> in the range from <b>PI</b> to <b>-PI</b>.
   * The <b>atan2()</b> function is most often used for orienting geometry to
   * the position of the cursor.  Note: The y-coordinate of the point is the
   * first parameter and the x-coordinate is the second due the the structure
   * of calculating the tangent.
   *
   * ( end auto-generated )
   * @webref math:trigonometry
   * @param y y-coordinate of the point
   * @param x x-coordinate of the point
   * @see PApplet#tan(float)
   */
  static public final float atan2(float y, float x) {
    return (float)Math.atan2(y, x);
  }

/**
   * ( begin auto-generated from degrees.xml )
   *
   * Converts a radian measurement to its corresponding value in degrees.
   * Radians and degrees are two ways of measuring the same thing. There are
   * 360 degrees in a circle and 2*PI radians in a circle. For example,
   * 90&deg; = PI/2 = 1.5707964. All trigonometric functions in Processing
   * require their parameters to be specified in radians.
   *
   * ( end auto-generated )
   * @webref math:trigonometry
   * @param radians radian value to convert to degrees
   * @see PApplet#radians(float)
   */
  static public final float degrees(float radians) {
    return radians * RAD_TO_DEG;
  }

/**
   * ( begin auto-generated from radians.xml )
   *
   * Converts a degree measurement to its corresponding value in radians.
   * Radians and degrees are two ways of measuring the same thing. There are
   * 360 degrees in a circle and 2*PI radians in a circle. For example,
   * 90&deg; = PI/2 = 1.5707964. All trigonometric functions in Processing
   * require their parameters to be specified in radians.
   *
   * ( end auto-generated )
   * @webref math:trigonometry
   * @param degrees degree value to convert to radians
   * @see PApplet#degrees(float)
   */
  static public final float radians(float degrees) {
    return degrees * DEG_TO_RAD;
  }

/**
   * ( begin auto-generated from ceil.xml )
   *
   * Calculates the closest int value that is greater than or equal to the
   * value of the parameter. For example, <b>ceil(9.03)</b> returns the value 10.
   *
   * ( end auto-generated )
   * @webref math:calculation
   * @param n number to round up
   * @see PApplet#floor(float)
   * @see PApplet#round(float)
   */
  static public final int ceil(float n) {
    return (int) Math.ceil(n);
  }

/**
   * ( begin auto-generated from floor.xml )
   *
   * Calculates the closest int value that is less than or equal to the value
   * of the parameter.
   *
   * ( end auto-generated )
   * @webref math:calculation
   * @param n number to round down
   * @see PApplet#ceil(float)
   * @see PApplet#round(float)
   */
  static public final int floor(float n) {
    return (int) Math.floor(n);
  }

/**
   * ( begin auto-generated from round.xml )
   *
   * Calculates the integer closest to the <b>value</b> parameter. For
   * example, <b>round(9.2)</b> returns the value 9.
   *
   * ( end auto-generated )
   * @webref math:calculation
   * @param n number to round
   * @see PApplet#floor(float)
   * @see PApplet#ceil(float)
   */
  static public final int round(float n) {
    return Math.round(n);
  }


  static public final float mag(float a, float b) {
    return (float)Math.sqrt(a*a + b*b);
  }

/**
   * ( begin auto-generated from mag.xml )
   *
   * Calculates the magnitude (or length) of a vector. A vector is a
   * direction in space commonly used in computer graphics and linear
   * algebra. Because it has no "start" position, the magnitude of a vector
   * can be thought of as the distance from coordinate (0,0) to its (x,y)
   * value. Therefore, mag() is a shortcut for writing "dist(0, 0, x, y)".
   *
   * ( end auto-generated )
   * @webref math:calculation
   * @param a first value
   * @param b second value
   * @param c third value
   * @see PApplet#dist(float, float, float, float)
   */
  static public final float mag(float a, float b, float c) {
    return (float)Math.sqrt(a*a + b*b + c*c);
  }


  static public final float dist(float x1, float y1, float x2, float y2) {
    return sqrt(sq(x2-x1) + sq(y2-y1));
  }

/**
   * ( begin auto-generated from dist.xml )
   *
   * Calculates the distance between two points.
   *
   * ( end auto-generated )
   * @webref math:calculation
   * @param x1 x-coordinate of the first point
   * @param y1 y-coordinate of the first point
   * @param z1 z-coordinate of the first point
   * @param x2 x-coordinate of the second point
   * @param y2 y-coordinate of the second point
   * @param z2 z-coordinate of the second point
   */
  static public final float dist(float x1, float y1, float z1,
                                 float x2, float y2, float z2) {
    return sqrt(sq(x2-x1) + sq(y2-y1) + sq(z2-z1));
  }

/**
   * ( begin auto-generated from lerp.xml )
   *
   * Calculates a number between two numbers at a specific increment. The
   * <b>amt</b> parameter is the amount to interpolate between the two values
   * where 0.0 equal to the first point, 0.1 is very near the first point,
   * 0.5 is half-way in between, etc. The lerp function is convenient for
   * creating motion along a straight path and for drawing dotted lines.
   *
   * ( end auto-generated )
   * @webref math:calculation
   * @param start first value
   * @param stop second value
   * @param amt float between 0.0 and 1.0
   * @see PGraphics#curvePoint(float, float, float, float, float)
   * @see PGraphics#bezierPoint(float, float, float, float, float)
   * @see PVector#lerp(PVector, float)
   * @see PGraphics#lerpColor(int, int, float)
   */
  static public final float lerp(float start, float stop, float amt) {
    return start + (stop-start) * amt;
  }

  /**
   * ( begin auto-generated from norm.xml )
   *
   * Normalizes a number from another range into a value between 0 and 1.
   * <br/> <br/>
   * Identical to map(value, low, high, 0, 1);
   * <br/> <br/>
   * Numbers outside the range are not clamped to 0 and 1, because
   * out-of-range values are often intentional and useful.
   *
   * ( end auto-generated )
   * @webref math:calculation
   * @param value the incoming value to be converted
   * @param start lower bound of the value's current range
   * @param stop upper bound of the value's current range
   * @see PApplet#map(float, float, float, float, float)
   * @see PApplet#lerp(float, float, float)
   */
  static public final float norm(float value, float start, float stop) {
    return (value - start) / (stop - start);
  }

  /**
   * ( begin auto-generated from map.xml )
   *
   * Re-maps a number from one range to another. In the example above,
   * the number '25' is converted from a value in the range 0..100 into
   * a value that ranges from the left edge (0) to the right edge (width)
   * of the screen.
   * <br/> <br/>
   * Numbers outside the range are not clamped to 0 and 1, because
   * out-of-range values are often intentional and useful.
   *
   * ( end auto-generated )
   * @webref math:calculation
   * @param value the incoming value to be converted
   * @param start1 lower bound of the value's current range
   * @param stop1 upper bound of the value's current range
   * @param start2 lower bound of the value's target range
   * @param stop2 upper bound of the value's target range
   * @see PApplet#norm(float, float, float)
   * @see PApplet#lerp(float, float, float)
   */
  static public final float map(float value,
                                float start1, float stop1,
                                float start2, float stop2) {
    float outgoing =
      start2 + (stop2 - start2) * ((value - start1) / (stop1 - start1));
    String badness = null;
    if (outgoing != outgoing) {
      badness = "NaN (not a number)";

    } else if (outgoing == Float.NEGATIVE_INFINITY ||
               outgoing == Float.POSITIVE_INFINITY) {
      badness = "infinity";
    }
    if (badness != null) {
      final String msg =
        String.format("map(%s, %s, %s, %s, %s) called, which returns %s",
                      nf(value), nf(start1), nf(stop1),
                      nf(start2), nf(stop2), badness);
      PGraphics.showWarning(msg);
    }
    return outgoing;
  }


  /*
  static public final double map(double value,
                                 double istart, double istop,
                                 double ostart, double ostop) {
    return ostart + (ostop - ostart) * ((value - istart) / (istop - istart));
  }
  */



  //////////////////////////////////////////////////////////////

  // RANDOM NUMBERS


  Random internalRandom;

  /**
   *
   */
  public static final float random(float high) {
    // avoid an infinite loop when 0 or NaN are passed in
    if (high == 0 || high != high) {
      return 0;
    }

    Random internalRandom = new Random();

    // for some reason (rounding error?) Math.random() * 3
    // can sometimes return '3' (once in ~30 million tries)
    // so a check was added to avoid the inclusion of 'howbig'
    float value = 0;
    do {
      value = internalRandom.nextFloat() * high;
    } while (value == high);
    return value;
  }

  /**
   * ( begin auto-generated from randomGaussian.xml )
   *
   * Returns a float from a random series of numbers having a mean of 0
   * and standard deviation of 1. Each time the <b>randomGaussian()</b>
   * function is called, it returns a number fitting a Gaussian, or
   * normal, distribution. There is theoretically no minimum or maximum
   * value that <b>randomGaussian()</b> might return. Rather, there is
   * just a very low probability that values far from the mean will be
   * returned; and a higher probability that numbers near the mean will
   * be returned.
   *
   * ( end auto-generated )
   * @webref math:random
   * @see PApplet#random(float,float)
   */
  public static final float randomGaussian() {
    return (float) new Random().nextGaussian();
  }


  /**
   * ( begin auto-generated from random.xml )
   *
   * Generates random numbers. Each time the <b>random()</b> function is
   * called, it returns an unexpected value within the specified range. If
   * one parameter is passed to the function it will return a <b>float</b>
   * between zero and the value of the <b>high</b> parameter. The function
   * call <b>random(5)</b> returns values between 0 and 5 (starting at zero,
   * up to but not including 5). If two parameters are passed, it will return
   * a <b>float</b> with a value between the the parameters. The function
   * call <b>random(-5, 10.2)</b> returns values starting at -5 up to (but
   * not including) 10.2. To convert a floating-point random number to an
   * integer, use the <b>int()</b> function.
   *
   * ( end auto-generated )
   * @webref math:random
   * @param low lower limit
   * @param high upper limit
   */
  public static final float random(float low, float high) {
    if (low >= high) return low;
    float diff = high - low;
    float value = 0;
    // because of rounding error, can't just add low, otherwise it may hit high
    // https://github.com/processing/processing/issues/4551
    do {
      value = random(diff) + low;
    } while (value == high);
    return value;
  }
  //////////////////////////////////////////////////////////////


  // PERLIN NOISE

  // [toxi 040903]
  // octaves and amplitude amount per octave are now user controlled
  // via the noiseDetail() function.

  // [toxi 030902]
  // cleaned up code and now using bagel's cosine table to speed up

  // [toxi 030901]
  // implementation by the german demo group farbrausch
  // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .

  //  /**
//   * @param extension the type of image to load, for example "png", "gif", "jpg"
//   */
//  public PImage loadImage(String filename, String extension) {
//    return loadImage(filename, extension, null);
//  }

//  /**
//   * @nowebref
//   */
//  public PImage loadImage(String filename, Object params) {
//    return loadImage(filename, null, params);
//  }

  /**
   * Use Java 1.4 ImageIO methods to load an image.
   */
  protected static PImage loadImageIO(String filename) {
    InputStream stream = createInput(filename);
    if (stream == null) {
      System.err.println("The image " + filename + " could not be found.");
      return null;
    }

    try {
      BufferedImage bi = ImageIO.read(stream);
      PImage outgoing = new PImage(bi.getWidth(), bi.getHeight());

      bi.getRGB(0, 0, outgoing.width, outgoing.height,
                outgoing.pixels, 0, outgoing.width);

      // check the alpha for this image
      // was gonna call getType() on the image to see if RGB or ARGB,
      // but it's not actually useful, since gif images will come through
      // as TYPE_BYTE_INDEXED, which means it'll still have to check for
      // the transparency. also, would have to iterate through all the other
      // types and guess whether alpha was in there, so.. just gonna stick
      // with the old method.
      outgoing.checkAlpha();

      stream.close();
      // return the image
      return outgoing;

    } catch (Exception e) {
      System.err.println(e.getCause());
      return null;
    }
  }

  //////////////////////////////////////////////////////////////

  // DATA I/O


//  /**
//   * @webref input:files
//   * @brief Creates a new XML object
//   * @param name the name to be given to the root element of the new XML object
//   * @return an XML object, or null
//   * @see XML
//   * @see PApplet#loadXML(String)
//   * @see PApplet#parseXML(String)
//   * @see PApplet#saveXML(XML, String)
//   */
//  public XML createXML(String name) {
//    try {
//      return new XML(name);
//    } catch (Exception e) {
//      e.printStackTrace();
//      return null;
//    }
//  }

  // version that uses 'options' though there are currently no supported options

//  /**
//   * @webref input:files
//   * @see Table
//   * @see PApplet#loadTable(String)
//   * @see PApplet#saveTable(Table, String)
//   */
//  public Table createTable() {
//    return new Table();
//  }

  //////////////////////////////////////////////////////////////

  // FONT I/O

  //////////////////////////////////////////////////////////////

  // FILE/FOLDER SELECTION


  /*
  private Frame selectFrame;

  private Frame selectFrame() {
    if (frame != null) {
      selectFrame = frame;

    } else if (selectFrame == null) {
      Component comp = getParent();
      while (comp != null) {
        if (comp instanceof Frame) {
          selectFrame = (Frame) comp;
          break;
        }
        comp = comp.getParent();
      }
      // Who you callin' a hack?
      if (selectFrame == null) {
        selectFrame = new Frame();
      }
    }
    return selectFrame;
  }
  */

  //////////////////////////////////////////////////////////////

  // LISTING DIRECTORIES

  //////////////////////////////////////////////////////////////

  // EXTENSIONS

  //////////////////////////////////////////////////////////////

  // READERS AND WRITERS


  /**
   * ( begin auto-generated from createReader.xml )
   *
   * Creates a <b>BufferedReader</b> object that can be used to read files
   * line-by-line as individual <b>String</b> objects. This is the complement
   * to the <b>createWriter()</b> function.
   * <br/> <br/>
   * Starting with Processing release 0134, all files loaded and saved by the
   * Processing API use UTF-8 encoding. In previous releases, the default
   * encoding for your platform was used, which causes problems when files
   * are moved to other platforms.
   *
   * ( end auto-generated )
   * @webref input:files
   * @param filename name of the file to be opened
   * @see BufferedReader
   * @see PrintWriter
   */
  public static BufferedReader createReader(String filename) {
    InputStream is = createInput(filename);
    if (is == null) {
      System.err.println("The file \"" + filename + "\" " +
                       "is missing or inaccessible, make sure " +
                       "the URL is valid or that the file has been " +
                       "added to your sketch and is readable.");
      return null;
    }
    return createReader(is);
  }

  /**
   * @nowebref
   * I want to read lines from a stream. If I have to type the
   * following lines any more I'm gonna send Sun my medical bills.
   */
  static public BufferedReader createReader(InputStream input) {
    InputStreamReader isr =
      new InputStreamReader(input, StandardCharsets.UTF_8);

    BufferedReader reader = new BufferedReader(isr);
    // consume the Unicode BOM (byte order marker) if present
    try {
      reader.mark(1);
      int c = reader.read();
      // if not the BOM, back up to the beginning again
      if (c != '\uFEFF') {
        reader.reset();
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    return reader;
  }

  /**
   * @nowebref
   * I want to print lines to a file. Why am I always explaining myself?
   * It's the JavaSoft API engineers who need to explain themselves.
   */
  static public PrintWriter createWriter(OutputStream output) {
    BufferedOutputStream bos = new BufferedOutputStream(output, 8192);
    OutputStreamWriter osw =
      new OutputStreamWriter(bos, StandardCharsets.UTF_8);
    return new PrintWriter(osw);
  }



  //////////////////////////////////////////////////////////////

  // FILE INPUT


  /**
   * ( begin auto-generated from createInput.xml )
   *
   * This is a function for advanced programmers to open a Java InputStream.
   * It's useful if you want to use the facilities provided by PApplet to
   * easily open files from the data folder or from a URL, but want an
   * InputStream object so that you can use other parts of Java to take more
   * control of how the stream is read.<br />
   * <br />
   * The filename passed in can be:<br />
   * - A URL, for instance <b>openStream("http://processing.org/")</b><br />
   * - A file in the sketch's <b>data</b> folder<br />
   * - The full path to a file to be opened locally (when running as an
   * application)<br />
   * <br />
   * If the requested item doesn't exist, null is returned. If not online,
   * this will also check to see if the user is asking for a file whose name
   * isn't properly capitalized. If capitalization is different, an error
   * will be printed to the console. This helps prevent issues that appear
   * when a sketch is exported to the web, where case sensitivity matters, as
   * opposed to running from inside the Processing Development Environment on
   * Windows or Mac OS, where case sensitivity is preserved but ignored.<br />
   * <br />
   * If the file ends with <b>.gz</b>, the stream will automatically be gzip
   * decompressed. If you don't want the automatic decompression, use the
   * related function <b>createInputRaw()</b>.
   * <br />
   * In earlier releases, this function was called <b>openStream()</b>.<br />
   * <br />
   *
   * ( end auto-generated )
   *
   * <h3>Advanced</h3>
   * Simplified method to open a Java InputStream.
   * <p>
   * This method is useful if you want to use the facilities provided
   * by PApplet to easily open things from the data folder or from a URL,
   * but want an InputStream object so that you can use other Java
   * methods to take more control of how the stream is read.
   * <p>
   * If the requested item doesn't exist, null is returned.
   * (Prior to 0096, die() would be called, killing the applet)
   * <p>
   * For 0096+, the "data" folder is exported intact with subfolders,
   * and openStream() properly handles subdirectories from the data folder
   * <p>
   * If not online, this will also check to see if the user is asking
   * for a file whose name isn't properly capitalized. This helps prevent
   * issues when a sketch is exported to the web, where case sensitivity
   * matters, as opposed to Windows and the Mac OS default where
   * case sensitivity is preserved but ignored.
   * <p>
   * It is strongly recommended that libraries use this method to open
   * data files, so that the loading sequence is handled in the same way
   * as functions like loadBytes(), loadImage(), etc.
   * <p>
   * The filename passed in can be:
   * <UL>
   * <LI>A URL, for instance openStream("http://processing.org/");
   * <LI>A file in the sketch's data folder
   * <LI>Another file to be opened locally (when running as an application)
   * </UL>
   *
   * @webref input:files
   * @param filename the name of the file to use as input
   * @see PApplet#createOutput(String)
   *
   */
  public static InputStream createInput(String filename) {
    InputStream input = createInputRaw(filename);
    if (input != null) {
      // if it's gzip-encoded, automatically decode
      final String lower = filename.toLowerCase();
      if (lower.endsWith(".gz") || lower.endsWith(".svgz")) {
        try {
          // buffered has to go *around* the GZ, otherwise 25x slower
          return new BufferedInputStream(new GZIPInputStream(input));

        } catch (IOException e) {
          System.err.println(e.getMessage());
        }
      } else {
        return new BufferedInputStream(input);
      }
    }
    return null;
  }


  /**
   * Call openStream() without automatic gzip decompression.
   */
  public static InputStream createInputRaw(String filename) {
    if (filename == null) return null;

    if (filename.length() == 0) {
      // an error will be called by the parent function
      //System.err.println("The filename passed to openStream() was empty.");
      return null;
    }

    // First check whether this looks like a URL
    if (filename.contains(":")) {  // at least smells like URL
      try {
        URL url = new URL(filename);
        URLConnection conn = url.openConnection();

        if (conn instanceof HttpURLConnection) {
          HttpURLConnection httpConn = (HttpURLConnection) conn;
          // Will not handle a protocol change (see below)
          httpConn.setInstanceFollowRedirects(true);
          int response = httpConn.getResponseCode();
          // Default won't follow HTTP -> HTTPS redirects for security reasons
          // http://stackoverflow.com/a/1884427
          if (response >= 300 && response < 400) {
            String newLocation = httpConn.getHeaderField("Location");
            return createInputRaw(newLocation);
          }
          return conn.getInputStream();
        } else if (conn instanceof JarURLConnection) {
          return url.openStream();
        }
      } catch (MalformedURLException mfue) {
        // not a url, that's fine

      } catch (FileNotFoundException fnfe) {
        // Added in 0119 b/c Java 1.5 throws FNFE when URL not available.
        // http://dev.processing.org/bugs/show_bug.cgi?id=403

      } catch (IOException e) {
        // changed for 0117, shouldn't be throwing exception
        System.err.println("Error downloading from URL " + filename);
        return null;
        //throw new RuntimeException("Error downloading from URL " + filename);
      }
    }

    InputStream stream = null;

    // Moved this earlier than the getResourceAsStream() checks, because
    // calling getResourceAsStream() on a directory lists its contents.
    // http://dev.processing.org/bugs/show_bug.cgi?id=716
    try {
      // First see if it's in a data folder. This may fail by throwing
      // a SecurityException. If so, this whole block will be skipped.
      File file = new File(dataPath(filename));
      if (!file.exists()) {
        // next see if it's just in the sketch folder
        file = sketchFile(filename);
      }

      if (file.isDirectory()) {
        return null;
      }
      if (file.exists()) {
        try {
          // handle case sensitivity check
          String filePath = file.getCanonicalPath();
          String filenameActual = new File(filePath).getName();
          // make sure there isn't a subfolder prepended to the name
          String filenameShort = new File(filename).getName();
          // if the actual filename is the same, but capitalized
          // differently, warn the user.
          //if (filenameActual.equalsIgnoreCase(filenameShort) &&
          //!filenameActual.equals(filenameShort)) {
          if (!filenameActual.equals(filenameShort)) {
            throw new RuntimeException("This file is named " +
                                       filenameActual + " not " +
                                       filename + ". Rename the file " +
                                       "or change your code.");
          }
        } catch (IOException e) { }
      }

      // if this file is ok, may as well just load it
      stream = new FileInputStream(file);
      if (stream != null) return stream;

      // have to break these out because a general Exception might
      // catch the RuntimeException being thrown above
    } catch (IOException ioe) {
    } catch (SecurityException se) { }

    // Using getClassLoader() prevents java from converting dots
    // to slashes or requiring a slash at the beginning.
    // (a slash as a prefix means that it'll load from the root of
    // the jar, rather than trying to dig into the package location)
    ClassLoader cl = PApplet.class.getClassLoader();

    // by default, data files are exported to the root path of the jar.
    // (not the data folder) so check there first.
    stream = cl.getResourceAsStream("data/" + filename);
    if (stream != null) {
      String cn = stream.getClass().getName();
      // this is an irritation of sun's java plug-in, which will return
      // a non-null stream for an object that doesn't exist. like all good
      // things, this is probably introduced in java 1.5. awesome!
      // http://dev.processing.org/bugs/show_bug.cgi?id=359
      if (!cn.equals("sun.plugin.cache.EmptyInputStream")) {
        return stream;
      }
    }

    // When used with an online script, also need to check without the
    // data folder, in case it's not in a subfolder called 'data'.
    // http://dev.processing.org/bugs/show_bug.cgi?id=389
    stream = cl.getResourceAsStream(filename);
    if (stream != null) {
      String cn = stream.getClass().getName();
      if (!cn.equals("sun.plugin.cache.EmptyInputStream")) {
        return stream;
      }
    }

    try {
      // attempt to load from a local file, used when running as
      // an application, or as a signed applet
      try {  // first try to catch any security exceptions
        try {
          stream = new FileInputStream(dataPath(filename));
          if (stream != null) return stream;
        } catch (IOException e2) { }

        try {
          stream = new FileInputStream(sketchPath(filename));
          if (stream != null) return stream;
        } catch (Exception e) { }  // ignored

        try {
          stream = new FileInputStream(filename);
          if (stream != null) return stream;
        } catch (IOException e1) { }

      } catch (SecurityException se) { }  // online, whups

    } catch (Exception e) {
      System.err.println(e.getCause());
    }

    return null;
  }


  /**
   * @nowebref
   */
  static public InputStream createInput(File file) {
    if (file == null) {
      throw new IllegalArgumentException("File passed to createInput() was null");
    }
    if (!file.exists()) {
      System.err.println(file + " does not exist, createInput() will return null");
      return null;
    }
    try {
      InputStream input = new FileInputStream(file);
      final String lower = file.getName().toLowerCase();
      if (lower.endsWith(".gz") || lower.endsWith(".svgz")) {
        return new BufferedInputStream(new GZIPInputStream(input));
      }
      return new BufferedInputStream(input);

    } catch (IOException e) {
      System.err.println("Could not createInput() for " + file);
      e.printStackTrace();
      return null;
    }
  }


  /**
   * ( begin auto-generated from loadBytes.xml )
   *
   * Reads the contents of a file or url and places it in a byte array. If a
   * file is specified, it must be located in the sketch's "data"
   * directory/folder.<br />
   * <br />
   * The filename parameter can also be a URL to a file found online. For
   * security reasons, a Processing sketch found online can only download
   * files from the same server from which it came. Getting around this
   * restriction requires a <a
   * href="http://wiki.processing.org/w/Sign_an_Applet">signed applet</a>.
   *
   * ( end auto-generated )
   * @webref input:files
   * @param filename name of a file in the data folder or a URL.
   * @see PApplet#loadStrings(String)
   * @see PApplet#saveStrings(String, String[])
   * @see PApplet#saveBytes(String, byte[])
   *
   */
  public byte[] loadBytes(String filename) {
    String lower = filename.toLowerCase();
    // If it's not a .gz file, then we might be able to uncompress it into
    // a fixed-size buffer, which should help speed because we won't have to
    // reallocate and resize the target array each time it gets full.
    if (!lower.endsWith(".gz")) {
      // If this looks like a URL, try to load it that way. Use the fact that
      // URL connections may have a content length header to size the array.
      if (filename.contains(":")) {  // at least smells like URL
        InputStream input = null;
        try {
          URL url = new URL(filename);
          URLConnection conn = url.openConnection();
          int length = -1;

          if (conn instanceof HttpURLConnection) {
            HttpURLConnection httpConn = (HttpURLConnection) conn;
            // Will not handle a protocol change (see below)
            httpConn.setInstanceFollowRedirects(true);
            int response = httpConn.getResponseCode();
            // Default won't follow HTTP -> HTTPS redirects for security reasons
            // http://stackoverflow.com/a/1884427
            if (response >= 300 && response < 400) {
              String newLocation = httpConn.getHeaderField("Location");
              return loadBytes(newLocation);
            }
            length = conn.getContentLength();
            input = conn.getInputStream();
          } else if (conn instanceof JarURLConnection) {
            length = conn.getContentLength();
            input = url.openStream();
          }

          if (input != null) {
            byte[] buffer = null;
            if (length != -1) {
              buffer = new byte[length];
              int count;
              int offset = 0;
              while ((count = input.read(buffer, offset, length - offset)) > 0) {
                offset += count;
              }
            } else {
              buffer = loadBytes(input);
            }
            input.close();
            return buffer;
          }
        } catch (MalformedURLException mfue) {
          // not a url, that's fine

        } catch (FileNotFoundException fnfe) {
          // Java 1.5+ throws FNFE when URL not available
          // http://dev.processing.org/bugs/show_bug.cgi?id=403

        } catch (IOException e) {
          System.err.println(e.getCause());
          return null;

        } finally {
          if (input != null) {
            try {
              input.close();
            } catch (IOException e) {
              // just deal
            }
          }
        }
      }
    }

    InputStream is = createInput(filename);
    if (is != null) {
      byte[] outgoing = loadBytes(is);
      try {
        is.close();
      } catch (IOException e) {
        System.err.println(e.getCause());  // shouldn't happen
      }
      return outgoing;
    }

    System.err.println("The file \"" + filename + "\" " +
                       "is missing or inaccessible, make sure " +
                       "the URL is valid or that the file has been " +
                       "added to your sketch and is readable.");
    return null;
  }


  /**
   * @nowebref
   */
  static public byte[] loadBytes(InputStream input) {
    try {
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      byte[] buffer = new byte[4096];

      int bytesRead = input.read(buffer);
      while (bytesRead != -1) {
        out.write(buffer, 0, bytesRead);
        bytesRead = input.read(buffer);
      }
      out.flush();
      return out.toByteArray();

    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }


  /**
   * @nowebref
   */
  static public byte[] loadBytes(File file) {
    if (!file.exists()) {
      System.err.println(file + " does not exist, loadBytes() will return null");
      return null;
    }

    try {
      InputStream input;
      int length;

      if (file.getName().toLowerCase().endsWith(".gz")) {
        RandomAccessFile raf = new RandomAccessFile(file, "r");
        raf.seek(raf.length() - 4);
        int b4 = raf.read();
        int b3 = raf.read();
        int b2 = raf.read();
        int b1 = raf.read();
        length = (b1 << 24) | (b2 << 16) + (b3 << 8) + b4;
        raf.close();

        // buffered has to go *around* the GZ, otherwise 25x slower
        input = new BufferedInputStream(new GZIPInputStream(new FileInputStream(file)));

      } else {
        long len = file.length();
        // http://stackoverflow.com/a/3039805
        int maxArraySize = Integer.MAX_VALUE - 5;
        if (len > maxArraySize) {
          System.err.println("Cannot use loadBytes() on a file larger than " + maxArraySize);
          return null;
        }
        length = (int) len;
        input = new BufferedInputStream(new FileInputStream(file));
      }
      byte[] buffer = new byte[length];
      int count;
      int offset = 0;
      // count will come back 0 when complete (or -1 if somehow going long?)
      while ((count = input.read(buffer, offset, length - offset)) > 0) {
        offset += count;
      }
      input.close();
      return buffer;

    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
  }


  /**
   * @nowebref
   */
  static public String[] loadStrings(File file) {
    if (!file.exists()) {
      System.err.println(file + " does not exist, loadStrings() will return null");
      return null;
    }

    InputStream is = createInput(file);
    if (is != null) {
      String[] outgoing = loadStrings(is);
      try {
        is.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
      return outgoing;
    }
    return null;
  }


  /**
   * ( begin auto-generated from loadStrings.xml )
   *
   * Reads the contents of a file or url and creates a String array of its
   * individual lines. If a file is specified, it must be located in the
   * sketch's "data" directory/folder.<br />
   * <br />
   * The filename parameter can also be a URL to a file found online. For
   * security reasons, a Processing sketch found online can only download
   * files from the same server from which it came. Getting around this
   * restriction requires a <a
   * href="http://wiki.processing.org/w/Sign_an_Applet">signed applet</a>.
   * <br />
   * If the file is not available or an error occurs, <b>null</b> will be
   * returned and an error message will be printed to the console. The error
   * message does not halt the program, however the null value may cause a
   * NullPointerException if your code does not check whether the value
   * returned is null.
   * <br/> <br/>
   * Starting with Processing release 0134, all files loaded and saved by the
   * Processing API use UTF-8 encoding. In previous releases, the default
   * encoding for your platform was used, which causes problems when files
   * are moved to other platforms.
   *
   * ( end auto-generated )
   *
   * <h3>Advanced</h3>
   * Load data from a file and shove it into a String array.
   * <p>
   * Exceptions are handled internally, when an error, occurs, an
   * exception is printed to the console and 'null' is returned,
   * but the program continues running. This is a tradeoff between
   * 1) showing the user that there was a problem but 2) not requiring
   * that all i/o code is contained in try/catch blocks, for the sake
   * of new users (or people who are just trying to get things done
   * in a "scripting" fashion. If you want to handle exceptions,
   * use Java methods for I/O.
   *
   * @webref input:files
   * @param filename name of the file or url to load
   * @see PApplet#loadBytes(String)
   * @see PApplet#saveStrings(String, String[])
   * @see PApplet#saveBytes(String, byte[])
   */
  public static String[] loadStrings(String filename) {
    InputStream is = createInput(filename);
    if (is != null) {
      String[] strArr = loadStrings(is);
      try {
        is.close();
      } catch (IOException e) {
        System.err.println(e.getCause());
      }
      return strArr;
    }

    System.err.println("The file \"" + filename + "\" " +
                       "is missing or inaccessible, make sure " +
                       "the URL is valid or that the file has been " +
                       "added to your sketch and is readable.");
    return null;
  }

  /**
   * @nowebref
   */
  static public String[] loadStrings(InputStream input) {
    try {
      BufferedReader reader =
        new BufferedReader(new InputStreamReader(input, "UTF-8"));
      return loadStrings(reader);
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }


  static public String[] loadStrings(BufferedReader reader) {
    try {
      String lines[] = new String[100];
      int lineCount = 0;
      String line = null;
      while ((line = reader.readLine()) != null) {
        if (lineCount == lines.length) {
          String temp[] = new String[lineCount << 1];
          System.arraycopy(lines, 0, temp, 0, lineCount);
          lines = temp;
        }
        lines[lineCount++] = line;
      }
      reader.close();

      if (lineCount == lines.length) {
        return lines;
      }

      // resize array to appropriate amount for these lines
      String output[] = new String[lineCount];
      System.arraycopy(lines, 0, output, 0, lineCount);
      return output;

    } catch (IOException e) {
      e.printStackTrace();
      //throw new RuntimeException("Error inside loadStrings()");
    }
    return null;
  }



  //////////////////////////////////////////////////////////////

  // FILE OUTPUT


  /**
   * ( begin auto-generated from createOutput.xml )
   *
   * Similar to <b>createInput()</b>, this creates a Java <b>OutputStream</b>
   * for a given filename or path. The file will be created in the sketch
   * folder, or in the same folder as an exported application.
   * <br /><br />
   * If the path does not exist, intermediate folders will be created. If an
   * exception occurs, it will be printed to the console, and <b>null</b>
   * will be returned.
   * <br /><br />
   * This function is a convenience over the Java approach that requires you
   * to 1) create a FileOutputStream object, 2) determine the exact file
   * location, and 3) handle exceptions. Exceptions are handled internally by
   * the function, which is more appropriate for "sketch" projects.
   * <br /><br />
   * If the output filename ends with <b>.gz</b>, the output will be
   * automatically GZIP compressed as it is written.
   *
   * ( end auto-generated )
   * @webref output:files
   * @param filename name of the file to open
   * @see PApplet#createInput(String)
   */
  public OutputStream createOutput(String filename) {
    return createOutput(saveFile(filename));
  }

  /**
   * @nowebref
   */
  static public OutputStream createOutput(File file) {
    try {
      createPath(file);  // make sure the path exists
      OutputStream output = new FileOutputStream(file);
      if (file.getName().toLowerCase().endsWith(".gz")) {
        return new BufferedOutputStream(new GZIPOutputStream(output));
      }
      return new BufferedOutputStream(output);

    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }


  /**
   * ( begin auto-generated from saveStream.xml )
   *
   * Save the contents of a stream to a file in the sketch folder. This is
   * basically <b>saveBytes(blah, loadBytes())</b>, but done more efficiently
   * (and with less confusing syntax).<br />
   * <br />
   * When using the <b>targetFile</b> parameter, it writes to a <b>File</b>
   * object for greater control over the file location. (Note that unlike
   * some other functions, this will not automatically compress or uncompress
   * gzip files.)
   *
   * ( end auto-generated )
   *
   * @webref output:files
   * @param target name of the file to write to
   * @param source location to read from (a filename, path, or URL)
   * @see PApplet#createOutput(String)
   */
  public boolean saveStream(String target, String source) {
    return saveStream(saveFile(target), source);
  }

  /**
   * Identical to the other saveStream(), but writes to a File
   * object, for greater control over the file location.
   * <p/>
   * Note that unlike other api methods, this will not automatically
   * compress or uncompress gzip files.
   */
  public boolean saveStream(File target, String source) {
    return saveStream(target, createInputRaw(source));
  }

  /**
   * @nowebref
   */
  public boolean saveStream(String target, InputStream source) {
    return saveStream(saveFile(target), source);
  }

  /**
   * @nowebref
   */
  static public boolean saveStream(File target, InputStream source) {
    File tempFile = null;
    try {
      // make sure that this path actually exists before writing
      createPath(target);
      tempFile = createTempFile(target);
      FileOutputStream targetStream = new FileOutputStream(tempFile);

      saveStream(targetStream, source);
      targetStream.close();
      targetStream = null;

      if (target.exists()) {
        if (!target.delete()) {
          System.err.println("Could not replace " +
                             target.getAbsolutePath() + ".");
        }
      }
      if (!tempFile.renameTo(target)) {
        System.err.println("Could not rename temporary file " +
                           tempFile.getAbsolutePath());
        return false;
      }
      return true;

    } catch (IOException e) {
      if (tempFile != null) {
        tempFile.delete();
      }
      e.printStackTrace();
      return false;
    }
  }

  /**
   * @nowebref
   */
  static public void saveStream(OutputStream target,
                                InputStream source) throws IOException {
    BufferedInputStream bis = new BufferedInputStream(source, 16384);
    BufferedOutputStream bos = new BufferedOutputStream(target);

    byte[] buffer = new byte[8192];
    int bytesRead;
    while ((bytesRead = bis.read(buffer)) != -1) {
      bos.write(buffer, 0, bytesRead);
    }

    bos.flush();
  }


  /**
   * ( begin auto-generated from saveBytes.xml )
   *
   * Opposite of <b>loadBytes()</b>, will write an entire array of bytes to a
   * file. The data is saved in binary format. This file is saved to the
   * sketch's folder, which is opened by selecting "Show sketch folder" from
   * the "Sketch" menu.<br />
   * <br />
   * It is not possible to use saveXxxxx() functions inside a web browser
   * unless the sketch is <a
   * href="http://wiki.processing.org/w/Sign_an_Applet">signed applet</A>. To
   * save a file back to a server, see the <a
   * href="http://wiki.processing.org/w/Saving_files_to_a_web-server">save to
   * web</A> code snippet on the Processing Wiki.
   *
   * ( end auto-generated )
   *
   * @webref output:files
   * @param filename name of the file to write to
   * @param data array of bytes to be written
   * @see PApplet#loadStrings(String)
   * @see PApplet#loadBytes(String)
   * @see PApplet#saveStrings(String, String[])
   */
  public void saveBytes(String filename, byte[] data) {
    saveBytes(saveFile(filename), data);
  }


  /**
   * Creates a temporary file based on the name/extension of another file
   * and in the same parent directory. Ensures that the same extension is used
   * (i.e. so that .gz files are gzip compressed on output) and that it's done
   * from the same directory so that renaming the file later won't cross file
   * system boundaries.
   */
  static private File createTempFile(File file) throws IOException {
    File parentDir = file.getParentFile();
    if (!parentDir.exists()) {
      parentDir.mkdirs();
    }
    String name = file.getName();
    String prefix;
    String suffix = null;
    int dot = name.lastIndexOf('.');
    if (dot == -1) {
      prefix = name;
    } else {
      // preserve the extension so that .gz works properly
      prefix = name.substring(0, dot);
      suffix = name.substring(dot);
    }
    // Prefix must be three characters
    if (prefix.length() < 3) {
      prefix += "processing";
    }
    return File.createTempFile(prefix, suffix, parentDir);
  }


  /**
   * @nowebref
   * Saves bytes to a specific File location specified by the user.
   */
  static public void saveBytes(File file, byte[] data) {
    File tempFile = null;
    try {
      tempFile = createTempFile(file);

      OutputStream output = createOutput(tempFile);
      saveBytes(output, data);
      output.close();
      output = null;

      if (file.exists()) {
        if (!file.delete()) {
          System.err.println("Could not replace " + file.getAbsolutePath());
        }
      }

      if (!tempFile.renameTo(file)) {
        System.err.println("Could not rename temporary file " +
                           tempFile.getAbsolutePath());
      }

    } catch (IOException e) {
      System.err.println("error saving bytes to " + file);
      if (tempFile != null) {
        tempFile.delete();
      }
      e.printStackTrace();
    }
  }


  /**
   * @nowebref
   * Spews a buffer of bytes to an OutputStream.
   */
  static public void saveBytes(OutputStream output, byte[] data) {
    try {
      output.write(data);
      output.flush();

    } catch (IOException e) {
      e.printStackTrace();
    }
  }


  //

  /**
   * ( begin auto-generated from saveStrings.xml )
   *
   * Writes an array of strings to a file, one line per string. This file is
   * saved to the sketch's folder, which is opened by selecting "Show sketch
   * folder" from the "Sketch" menu.<br />
   * <br />
   * It is not possible to use saveXxxxx() functions inside a web browser
   * unless the sketch is <a
   * href="http://wiki.processing.org/w/Sign_an_Applet">signed applet</A>. To
   * save a file back to a server, see the <a
   * href="http://wiki.processing.org/w/Saving_files_to_a_web-server">save to
   * web</A> code snippet on the Processing Wiki.<br/>
   * <br/ >
   * Starting with Processing 1.0, all files loaded and saved by the
   * Processing API use UTF-8 encoding. In previous releases, the default
   * encoding for your platform was used, which causes problems when files
   * are moved to other platforms.
   *
   * ( end auto-generated )
   * @webref output:files
   * @param filename filename for output
   * @param data string array to be written
   * @see PApplet#loadStrings(String)
   * @see PApplet#loadBytes(String)
   * @see PApplet#saveBytes(String, byte[])
   */
  public void saveStrings(String filename, String data[]) {
    saveStrings(saveFile(filename), data);
  }


  /**
   * @nowebref
   */
  static public void saveStrings(File file, String data[]) {
    saveStrings(createOutput(file), data);
  }


  /**
   * @nowebref
   */
  static public void saveStrings(OutputStream output, String[] data) {
    PrintWriter writer = createWriter(output);
    for (int i = 0; i < data.length; i++) {
      writer.println(data[i]);
    }
    writer.flush();
    writer.close();
  }


  //////////////////////////////////////////////////////////////


  static protected String calcSketchPath() {
    // try to get the user folder. if running under java web start,
    // this may cause a security exception if the code is not signed.
    // http://processing.org/discourse/yabb_beta/YaBB.cgi?board=Integrate;action=display;num=1159386274
    String folder = null;
    try {
      folder = System.getProperty("user.dir");

      URL jarURL =
          PApplet.class.getProtectionDomain().getCodeSource().getLocation();
      // Decode URL
      String jarPath = jarURL.toURI().getSchemeSpecificPart();

      // Workaround for bug in Java for OS X from Oracle (7u51)
      // https://github.com/processing/processing/issues/2181
      if (platform == MACOSX) {
        if (jarPath.contains("Contents/Java/")) {
          String appPath = jarPath.substring(0, jarPath.indexOf(".app") + 4);
          File containingFolder = new File(appPath).getParentFile();
          folder = containingFolder.getAbsolutePath();
        }
      } else {
        // Working directory may not be set properly, try some options
        // https://github.com/processing/processing/issues/2195
        if (jarPath.contains("/lib/")) {
          // Windows or Linux, back up a directory to get the executable
          folder = new File(jarPath, "../..").getCanonicalPath();
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return folder;
  }


  public static String sketchPath() {
    return calcSketchPath();
  }


  /**
   * Prepend the sketch folder path to the filename (or path) that is
   * passed in. External libraries should use this function to save to
   * the sketch folder.
   * <p/>
   * Note that when running as an applet inside a web browser,
   * the sketchPath will be set to null, because security restrictions
   * prevent applets from accessing that information.
   * <p/>
   * This will also cause an error if the sketch is not inited properly,
   * meaning that init() was never called on the PApplet when hosted
   * my some other main() or by other code. For proper use of init(),
   * see the examples in the main description text for PApplet.
   */
  public static String sketchPath(String where) {
    if (sketchPath() == null) {
      return where;
    }
    // isAbsolute() could throw an access exception, but so will writing
    // to the local disk using the sketch path, so this is safe here.
    // for 0120, added a try/catch anyways.
    try {
      if (new File(where).isAbsolute()) return where;
    } catch (Exception e) { }

    return sketchPath() + File.separator + where;
  }


  public static File sketchFile(String where) {
    return new File(sketchPath(where));
  }


  /**
   * Returns a path inside the applet folder to save to. Like sketchPath(),
   * but creates any in-between folders so that things save properly.
   * <p/>
   * All saveXxxx() functions use the path to the sketch folder, rather than
   * its data folder. Once exported, the data folder will be found inside the
   * jar file of the exported application or applet. In this case, it's not
   * possible to save data into the jar file, because it will often be running
   * from a server, or marked in-use if running from a local file system.
   * With this in mind, saving to the data path doesn't make sense anyway.
   * If you know you're running locally, and want to save to the data folder,
   * use <TT>saveXxxx("data/blah.dat")</TT>.
   */
  public static String savePath(String where) {
    if (where == null) return null;
    String filename = sketchPath(where);
    createPath(filename);
    return filename;
  }


  /**
   * Identical to savePath(), but returns a File object.
   */
  public File saveFile(String where) {
    return new File(savePath(where));
  }

  /**
   * <b>This function almost certainly does not do the thing you want it to.</b>
   * The data path is handled differently on each platform, and should not be
   * considered a location to write files. It should also not be assumed that
   * this location can be read from or listed. This function is used internally
   * as a possible location for reading files. It's still "public" as a
   * holdover from earlier code.
   * <p>
   * Libraries should use createInput() to get an InputStream or createOutput()
   * to get an OutputStream. sketchPath() can be used to get a location
   * relative to the sketch. Again, <b>do not</b> use this to get relative
   * locations of files. You'll be disappointed when your app runs on different
   * platforms.
   */
  public static String dataPath(String where) {
    return dataFile(where).getAbsolutePath();
  }


  /**
   * Return a full path to an item in the data folder as a File object.
   * See the dataPath() method for more information.
   */
  public static File dataFile(String where) {
    // isAbsolute() could throw an access exception, but so will writing
    // to the local disk using the sketch path, so this is safe here.
    File why = new File(where);
    if (why.isAbsolute()) return why;

    URL jarURL = PApplet.class.getProtectionDomain().getCodeSource().getLocation();
    // Decode URL
    String jarPath;
    try {
      jarPath = jarURL.toURI().getPath();
    } catch (URISyntaxException e) {
      e.printStackTrace();
      return null;
    }
    if (jarPath.contains("Contents/Java/")) {
      File containingFolder = new File(jarPath).getParentFile();
      File dataFolder = new File(containingFolder, "data");
      return new File(dataFolder, where);
    }
    // Windows, Linux, or when not using a Mac OS X .app file
    File workingDirItem =
      new File(System.getProperty("user.dir") + File.separator + "data" + File.separator + where);
//    if (workingDirItem.exists()) {
    return workingDirItem;
//    }
//    // In some cases, the current working directory won't be set properly.
  }


  /**
   * On Windows and Linux, this is simply the data folder. On Mac OS X, this is
   * the path to the data folder buried inside Contents/Java
   */
//  public File inputFile(String where) {
//  }


//  public String inputPath(String where) {
//  }


  /**
   * Takes a path and creates any in-between folders if they don't
   * already exist. Useful when trying to save to a subfolder that
   * may not actually exist.
   */
  static public void createPath(String path) {
    createPath(new File(path));
  }


  static public void createPath(File file) {
    try {
      String parent = file.getParent();
      if (parent != null) {
        File unit = new File(parent);
        if (!unit.exists()) unit.mkdirs();
      }
    } catch (SecurityException se) {
      System.err.println("You don't have permissions to create " +
                         file.getAbsolutePath());
    }
  }


  static public String getExtension(String filename) {
    String extension;

    String lower = filename.toLowerCase();
    int dot = filename.lastIndexOf('.');
    if (dot == -1) {
      return "";  // no extension found
    }
    extension = lower.substring(dot + 1);

    // check for, and strip any parameters on the url, i.e.
    // filename.jpg?blah=blah&something=that
    int question = extension.indexOf('?');
    if (question != -1) {
      extension = extension.substring(0, question);
    }

    return extension;
  }


  //////////////////////////////////////////////////////////////

  // URL ENCODING


  static public String urlEncode(String str) {
    try {
      return URLEncoder.encode(str, "UTF-8");
    } catch (UnsupportedEncodingException e) {  // oh c'mon
      return null;
    }
  }


  // DO NOT use for file paths, URLDecoder can't handle RFC2396
  // "The recommended way to manage the encoding and decoding of
  // URLs is to use URI, and to convert between these two classes
  // using toURI() and URI.toURL()."
  // https://docs.oracle.com/javase/8/docs/api/java/net/URL.html
  static public String urlDecode(String str) {
    try {
      return URLDecoder.decode(str, "UTF-8");
    } catch (UnsupportedEncodingException e) {  // safe per the JDK source
      return null;
    }
  }



  //////////////////////////////////////////////////////////////

  // SORT


  /**
   * ( begin auto-generated from sort.xml )
   *
   * Sorts an array of numbers from smallest to largest and puts an array of
   * words in alphabetical order. The original array is not modified, a
   * re-ordered array is returned. The <b>count</b> parameter states the
   * number of elements to sort. For example if there are 12 elements in an
   * array and if count is the value 5, only the first five elements on the
   * array will be sorted. <!--As of release 0126, the alphabetical ordering
   * is case insensitive.-->
   *
   * ( end auto-generated )
   * @webref data:array_functions
   * @param list array to sort
   * @see PApplet#reverse(boolean[])
   */
  static public byte[] sort(byte list[]) {
    return sort(list, list.length);
  }

  /**
        * @param count number of elements to sort, starting from 0
   */
  static public byte[] sort(byte[] list, int count) {
    byte[] outgoing = new byte[list.length];
    System.arraycopy(list, 0, outgoing, 0, list.length);
    Arrays.sort(outgoing, 0, count);
    return outgoing;
  }

  static public char[] sort(char list[]) {
    return sort(list, list.length);
  }

  static public char[] sort(char[] list, int count) {
    char[] outgoing = new char[list.length];
    System.arraycopy(list, 0, outgoing, 0, list.length);
    Arrays.sort(outgoing, 0, count);
    return outgoing;
  }

  static public int[] sort(int list[]) {
    return sort(list, list.length);
  }

  static public int[] sort(int[] list, int count) {
    int[] outgoing = new int[list.length];
    System.arraycopy(list, 0, outgoing, 0, list.length);
    Arrays.sort(outgoing, 0, count);
    return outgoing;
  }

  static public float[] sort(float list[]) {
    return sort(list, list.length);
  }

  static public float[] sort(float[] list, int count) {
    float[] outgoing = new float[list.length];
    System.arraycopy(list, 0, outgoing, 0, list.length);
    Arrays.sort(outgoing, 0, count);
    return outgoing;
  }

  static public String[] sort(String list[]) {
    return sort(list, list.length);
  }

  static public String[] sort(String[] list, int count) {
    String[] outgoing = new String[list.length];
    System.arraycopy(list, 0, outgoing, 0, list.length);
    Arrays.sort(outgoing, 0, count);
    return outgoing;
  }



  //////////////////////////////////////////////////////////////

  // ARRAY UTILITIES


  /**
   * ( begin auto-generated from arrayCopy.xml )
   *
   * Copies an array (or part of an array) to another array. The <b>src</b>
   * array is copied to the <b>dst</b> array, beginning at the position
   * specified by <b>srcPos</b> and into the position specified by
   * <b>dstPos</b>. The number of elements to copy is determined by
   * <b>length</b>. The simplified version with two arguments copies an
   * entire array to another of the same size. It is equivalent to
   * "arrayCopy(src, 0, dst, 0, src.length)". This function is far more
   * efficient for copying array data than iterating through a <b>for</b> and
   * copying each element.
   *
   * ( end auto-generated )
   * @webref data:array_functions
   * @param src the source array
   * @param srcPosition starting position in the source array
   * @param dst the destination array of the same data type as the source array
   * @param dstPosition starting position in the destination array
   * @param length number of array elements to be copied
   * @see PApplet#concat(boolean[], boolean[])
   */
  static public void arrayCopy(Object src, int srcPosition,
                               Object dst, int dstPosition,
                               int length) {
    System.arraycopy(src, srcPosition, dst, dstPosition, length);
  }

  /**
   * Convenience method for arraycopy().
   * Identical to <CODE>arraycopy(src, 0, dst, 0, length);</CODE>
   */
  static public void arrayCopy(Object src, Object dst, int length) {
    System.arraycopy(src, 0, dst, 0, length);
  }

  /**
   * Shortcut to copy the entire contents of
   * the source into the destination array.
   * Identical to <CODE>arraycopy(src, 0, dst, 0, src.length);</CODE>
   */
  static public void arrayCopy(Object src, Object dst) {
    System.arraycopy(src, 0, dst, 0, Array.getLength(src));
  }

  /**
   * Use arrayCopy() instead.
   */
  @Deprecated
  static public void arraycopy(Object src, int srcPosition,
                               Object dst, int dstPosition,
                               int length) {
    System.arraycopy(src, srcPosition, dst, dstPosition, length);
  }

  /**
   * Use arrayCopy() instead.
   */
  @Deprecated
  static public void arraycopy(Object src, Object dst, int length) {
    System.arraycopy(src, 0, dst, 0, length);
  }

  /**
   * Use arrayCopy() instead.
   */
  @Deprecated
  static public void arraycopy(Object src, Object dst) {
    System.arraycopy(src, 0, dst, 0, Array.getLength(src));
  }


  /**
   * ( begin auto-generated from expand.xml )
   *
   * Increases the size of an array. By default, this function doubles the
   * size of the array, but the optional <b>newSize</b> parameter provides
   * precise control over the increase in size.
   * <br/> <br/>
   * When using an array of objects, the data returned from the function must
   * be cast to the object array's data type. For example: <em>SomeClass[]
   * items = (SomeClass[]) expand(originalArray)</em>.
   *
   * ( end auto-generated )
   *
   * @webref data:array_functions
   * @param list the array to expand
   * @see PApplet#shorten(boolean[])
   */
  static public boolean[] expand(boolean list[]) {
    return expand(list, list.length > 0 ? list.length << 1 : 1);
  }

  /**
   * @param newSize new size for the array
   */
  static public boolean[] expand(boolean list[], int newSize) {
    boolean temp[] = new boolean[newSize];
    System.arraycopy(list, 0, temp, 0, Math.min(newSize, list.length));
    return temp;
  }

  static public byte[] expand(byte list[]) {
    return expand(list, list.length > 0 ? list.length << 1 : 1);
  }

  static public byte[] expand(byte list[], int newSize) {
    byte temp[] = new byte[newSize];
    System.arraycopy(list, 0, temp, 0, Math.min(newSize, list.length));
    return temp;
  }

  static public char[] expand(char list[]) {
    return expand(list, list.length > 0 ? list.length << 1 : 1);
  }

  static public char[] expand(char list[], int newSize) {
    char temp[] = new char[newSize];
    System.arraycopy(list, 0, temp, 0, Math.min(newSize, list.length));
    return temp;
  }

  static public int[] expand(int list[]) {
    return expand(list, list.length > 0 ? list.length << 1 : 1);
  }

  static public int[] expand(int list[], int newSize) {
    int temp[] = new int[newSize];
    System.arraycopy(list, 0, temp, 0, Math.min(newSize, list.length));
    return temp;
  }

  static public long[] expand(long list[]) {
    return expand(list, list.length > 0 ? list.length << 1 : 1);
  }

  static public long[] expand(long list[], int newSize) {
    long temp[] = new long[newSize];
    System.arraycopy(list, 0, temp, 0, Math.min(newSize, list.length));
    return temp;
  }

  static public float[] expand(float list[]) {
    return expand(list, list.length > 0 ? list.length << 1 : 1);
  }

  static public float[] expand(float list[], int newSize) {
    float temp[] = new float[newSize];
    System.arraycopy(list, 0, temp, 0, Math.min(newSize, list.length));
    return temp;
  }

  static public double[] expand(double list[]) {
    return expand(list, list.length > 0 ? list.length << 1 : 1);
  }

  static public double[] expand(double list[], int newSize) {
    double temp[] = new double[newSize];
    System.arraycopy(list, 0, temp, 0, Math.min(newSize, list.length));
    return temp;
  }

  static public String[] expand(String list[]) {
    return expand(list, list.length > 0 ? list.length << 1 : 1);
  }

  static public String[] expand(String list[], int newSize) {
    String temp[] = new String[newSize];
    // in case the new size is smaller than list.length
    System.arraycopy(list, 0, temp, 0, Math.min(newSize, list.length));
    return temp;
  }

 /**
  * @nowebref
  */
  static public Object expand(Object array) {
    int len = Array.getLength(array);
    return expand(array, len > 0 ? len << 1 : 1);
  }

  static public Object expand(Object list, int newSize) {
    Class<?> type = list.getClass().getComponentType();
    Object temp = Array.newInstance(type, newSize);
    System.arraycopy(list, 0, temp, 0,
                     Math.min(Array.getLength(list), newSize));
    return temp;
  }

  // contract() has been removed in revision 0124, use subset() instead.
  // (expand() is also functionally equivalent)

  /**
   * ( begin auto-generated from append.xml )
   *
   * Expands an array by one element and adds data to the new position. The
   * datatype of the <b>element</b> parameter must be the same as the
   * datatype of the array.
   * <br/> <br/>
   * When using an array of objects, the data returned from the function must
   * be cast to the object array's data type. For example: <em>SomeClass[]
   * items = (SomeClass[]) append(originalArray, element)</em>.
   *
   * ( end auto-generated )
   *
   * @webref data:array_functions
   * @param array array to append
   * @param value new data for the array
   * @see PApplet#shorten(boolean[])
   * @see PApplet#expand(boolean[])
   */
  static public byte[] append(byte array[], byte value) {
    array = expand(array, array.length + 1);
    array[array.length-1] = value;
    return array;
  }

  static public char[] append(char array[], char value) {
    array = expand(array, array.length + 1);
    array[array.length-1] = value;
    return array;
  }

  static public int[] append(int array[], int value) {
    array = expand(array, array.length + 1);
    array[array.length-1] = value;
    return array;
  }

  static public float[] append(float array[], float value) {
    array = expand(array, array.length + 1);
    array[array.length-1] = value;
    return array;
  }

  static public String[] append(String array[], String value) {
    array = expand(array, array.length + 1);
    array[array.length-1] = value;
    return array;
  }

  static public Object append(Object array, Object value) {
    int length = Array.getLength(array);
    array = expand(array, length + 1);
    Array.set(array, length, value);
    return array;
  }


 /**
   * ( begin auto-generated from shorten.xml )
   *
   * Decreases an array by one element and returns the shortened array.
   * <br/> <br/>
   * When using an array of objects, the data returned from the function must
   * be cast to the object array's data type. For example: <em>SomeClass[]
   * items = (SomeClass[]) shorten(originalArray)</em>.
   *
   * ( end auto-generated )
   *
   * @webref data:array_functions
   * @param list array to shorten
   * @see PApplet#append(byte[], byte)
   * @see PApplet#expand(boolean[])
   */
  static public boolean[] shorten(boolean list[]) {
    return subset(list, 0, list.length-1);
  }

  static public byte[] shorten(byte list[]) {
    return subset(list, 0, list.length-1);
  }

  static public char[] shorten(char list[]) {
    return subset(list, 0, list.length-1);
  }

  static public int[] shorten(int list[]) {
    return subset(list, 0, list.length-1);
  }

  static public float[] shorten(float list[]) {
    return subset(list, 0, list.length-1);
  }

  static public String[] shorten(String list[]) {
    return subset(list, 0, list.length-1);
  }

  static public Object shorten(Object list) {
    int length = Array.getLength(list);
    return subset(list, 0, length - 1);
  }


  /**
   * ( begin auto-generated from splice.xml )
   *
   * Inserts a value or array of values into an existing array. The first two
   * parameters must be of the same datatype. The <b>array</b> parameter
   * defines the array which will be modified and the second parameter
   * defines the data which will be inserted.
   * <br/> <br/>
   * When using an array of objects, the data returned from the function must
   * be cast to the object array's data type. For example: <em>SomeClass[]
   * items = (SomeClass[]) splice(array1, array2, index)</em>.
   *
   * ( end auto-generated )
   * @webref data:array_functions
   * @param list array to splice into
   * @param value value to be spliced in
   * @param index position in the array from which to insert data
   * @see PApplet#concat(boolean[], boolean[])
   * @see PApplet#subset(boolean[], int, int)
   */
  static final public boolean[] splice(boolean list[],
                                       boolean value, int index) {
    boolean outgoing[] = new boolean[list.length + 1];
    System.arraycopy(list, 0, outgoing, 0, index);
    outgoing[index] = value;
    System.arraycopy(list, index, outgoing, index + 1,
                     list.length - index);
    return outgoing;
  }

  static final public boolean[] splice(boolean list[],
                                       boolean value[], int index) {
    boolean outgoing[] = new boolean[list.length + value.length];
    System.arraycopy(list, 0, outgoing, 0, index);
    System.arraycopy(value, 0, outgoing, index, value.length);
    System.arraycopy(list, index, outgoing, index + value.length,
                     list.length - index);
    return outgoing;
  }

  static final public byte[] splice(byte list[],
                                    byte value, int index) {
    byte outgoing[] = new byte[list.length + 1];
    System.arraycopy(list, 0, outgoing, 0, index);
    outgoing[index] = value;
    System.arraycopy(list, index, outgoing, index + 1,
                     list.length - index);
    return outgoing;
  }

  static final public byte[] splice(byte list[],
                                    byte value[], int index) {
    byte outgoing[] = new byte[list.length + value.length];
    System.arraycopy(list, 0, outgoing, 0, index);
    System.arraycopy(value, 0, outgoing, index, value.length);
    System.arraycopy(list, index, outgoing, index + value.length,
                     list.length - index);
    return outgoing;
  }


  static final public char[] splice(char list[],
                                    char value, int index) {
    char outgoing[] = new char[list.length + 1];
    System.arraycopy(list, 0, outgoing, 0, index);
    outgoing[index] = value;
    System.arraycopy(list, index, outgoing, index + 1,
                     list.length - index);
    return outgoing;
  }

  static final public char[] splice(char list[],
                                    char value[], int index) {
    char outgoing[] = new char[list.length + value.length];
    System.arraycopy(list, 0, outgoing, 0, index);
    System.arraycopy(value, 0, outgoing, index, value.length);
    System.arraycopy(list, index, outgoing, index + value.length,
                     list.length - index);
    return outgoing;
  }

  static final public int[] splice(int list[],
                                   int value, int index) {
    int outgoing[] = new int[list.length + 1];
    System.arraycopy(list, 0, outgoing, 0, index);
    outgoing[index] = value;
    System.arraycopy(list, index, outgoing, index + 1,
                     list.length - index);
    return outgoing;
  }

  static final public int[] splice(int list[],
                                   int value[], int index) {
    int outgoing[] = new int[list.length + value.length];
    System.arraycopy(list, 0, outgoing, 0, index);
    System.arraycopy(value, 0, outgoing, index, value.length);
    System.arraycopy(list, index, outgoing, index + value.length,
                     list.length - index);
    return outgoing;
  }

  static final public float[] splice(float list[],
                                     float value, int index) {
    float outgoing[] = new float[list.length + 1];
    System.arraycopy(list, 0, outgoing, 0, index);
    outgoing[index] = value;
    System.arraycopy(list, index, outgoing, index + 1,
                     list.length - index);
    return outgoing;
  }

  static final public float[] splice(float list[],
                                     float value[], int index) {
    float outgoing[] = new float[list.length + value.length];
    System.arraycopy(list, 0, outgoing, 0, index);
    System.arraycopy(value, 0, outgoing, index, value.length);
    System.arraycopy(list, index, outgoing, index + value.length,
                     list.length - index);
    return outgoing;
  }

  static final public String[] splice(String list[],
                                      String value, int index) {
    String outgoing[] = new String[list.length + 1];
    System.arraycopy(list, 0, outgoing, 0, index);
    outgoing[index] = value;
    System.arraycopy(list, index, outgoing, index + 1,
                     list.length - index);
    return outgoing;
  }

  static final public String[] splice(String list[],
                                      String value[], int index) {
    String outgoing[] = new String[list.length + value.length];
    System.arraycopy(list, 0, outgoing, 0, index);
    System.arraycopy(value, 0, outgoing, index, value.length);
    System.arraycopy(list, index, outgoing, index + value.length,
                     list.length - index);
    return outgoing;
  }

  static final public Object splice(Object list, Object value, int index) {
    Class<?> type = list.getClass().getComponentType();
    Object outgoing = null;
    int length = Array.getLength(list);

    // check whether item being spliced in is an array
    if (value.getClass().getName().charAt(0) == '[') {
      int vlength = Array.getLength(value);
      outgoing = Array.newInstance(type, length + vlength);
      System.arraycopy(list, 0, outgoing, 0, index);
      System.arraycopy(value, 0, outgoing, index, vlength);
      System.arraycopy(list, index, outgoing, index + vlength, length - index);

    } else {
      outgoing = Array.newInstance(type, length + 1);
      System.arraycopy(list, 0, outgoing, 0, index);
      Array.set(outgoing, index, value);
      System.arraycopy(list, index, outgoing, index + 1, length - index);
    }
    return outgoing;
  }


  static public boolean[] subset(boolean[] list, int start) {
    return subset(list, start, list.length - start);
  }


 /**
   * ( begin auto-generated from subset.xml )
   *
   * Extracts an array of elements from an existing array. The <b>array</b>
   * parameter defines the array from which the elements will be copied and
   * the <b>offset</b> and <b>length</b> parameters determine which elements
   * to extract. If no <b>length</b> is given, elements will be extracted
   * from the <b>offset</b> to the end of the array. When specifying the
   * <b>offset</b> remember the first array element is 0. This function does
   * not change the source array.
   * <br/> <br/>
   * When using an array of objects, the data returned from the function must
   * be cast to the object array's data type. For example: <em>SomeClass[]
   * items = (SomeClass[]) subset(originalArray, 0, 4)</em>.
   *
   * ( end auto-generated )
  * @webref data:array_functions
  * @param list array to extract from
  * @param start position to begin
  * @param count number of values to extract
  * @see PApplet#splice(boolean[], boolean, int)
  */
  static public boolean[] subset(boolean[] list, int start, int count) {
    boolean[] output = new boolean[count];
    System.arraycopy(list, start, output, 0, count);
    return output;
  }


  static public byte[] subset(byte[] list, int start) {
    return subset(list, start, list.length - start);
  }


  static public byte[] subset(byte[] list, int start, int count) {
    byte[] output = new byte[count];
    System.arraycopy(list, start, output, 0, count);
    return output;
  }


  static public char[] subset(char[] list, int start) {
    return subset(list, start, list.length - start);
  }


  static public char[] subset(char[] list, int start, int count) {
    char[] output = new char[count];
    System.arraycopy(list, start, output, 0, count);
    return output;
  }


  static public int[] subset(int[] list, int start) {
    return subset(list, start, list.length - start);
  }


  static public int[] subset(int[] list, int start, int count) {
    int[] output = new int[count];
    System.arraycopy(list, start, output, 0, count);
    return output;
  }


  static public long[] subset(long[] list, int start) {
    return subset(list, start, list.length - start);
  }


  static public long[] subset(long[] list, int start, int count) {
    long[] output = new long[count];
    System.arraycopy(list, start, output, 0, count);
    return output;
  }


  static public float[] subset(float[] list, int start) {
    return subset(list, start, list.length - start);
  }


  static public float[] subset(float[] list, int start, int count) {
    float[] output = new float[count];
    System.arraycopy(list, start, output, 0, count);
    return output;
  }


  static public double[] subset(double[] list, int start) {
    return subset(list, start, list.length - start);
  }


  static public double[] subset(double[] list, int start, int count) {
    double[] output = new double[count];
    System.arraycopy(list, start, output, 0, count);
    return output;
  }


  static public String[] subset(String[] list, int start) {
    return subset(list, start, list.length - start);
  }


  static public String[] subset(String[] list, int start, int count) {
    String[] output = new String[count];
    System.arraycopy(list, start, output, 0, count);
    return output;
  }


  static public Object subset(Object list, int start) {
    int length = Array.getLength(list);
    return subset(list, start, length - start);
  }


  static public Object subset(Object list, int start, int count) {
    Class<?> type = list.getClass().getComponentType();
    Object outgoing = Array.newInstance(type, count);
    System.arraycopy(list, start, outgoing, 0, count);
    return outgoing;
  }


 /**
   * ( begin auto-generated from concat.xml )
   *
   * Concatenates two arrays. For example, concatenating the array { 1, 2, 3
   * } and the array { 4, 5, 6 } yields { 1, 2, 3, 4, 5, 6 }. Both parameters
   * must be arrays of the same datatype.
   * <br/> <br/>
   * When using an array of objects, the data returned from the function must
   * be cast to the object array's data type. For example: <em>SomeClass[]
   * items = (SomeClass[]) concat(array1, array2)</em>.
   *
   * ( end auto-generated )
  * @webref data:array_functions
  * @param a first array to concatenate
  * @param b second array to concatenate
  * @see PApplet#splice(boolean[], boolean, int)
  * @see PApplet#arrayCopy(Object, int, Object, int, int)
  */
  static public boolean[] concat(boolean a[], boolean b[]) {
    boolean c[] = new boolean[a.length + b.length];
    System.arraycopy(a, 0, c, 0, a.length);
    System.arraycopy(b, 0, c, a.length, b.length);
    return c;
  }

  static public byte[] concat(byte a[], byte b[]) {
    byte c[] = new byte[a.length + b.length];
    System.arraycopy(a, 0, c, 0, a.length);
    System.arraycopy(b, 0, c, a.length, b.length);
    return c;
  }

  static public char[] concat(char a[], char b[]) {
    char c[] = new char[a.length + b.length];
    System.arraycopy(a, 0, c, 0, a.length);
    System.arraycopy(b, 0, c, a.length, b.length);
    return c;
  }

  static public int[] concat(int a[], int b[]) {
    int c[] = new int[a.length + b.length];
    System.arraycopy(a, 0, c, 0, a.length);
    System.arraycopy(b, 0, c, a.length, b.length);
    return c;
  }

  static public float[] concat(float a[], float b[]) {
    float c[] = new float[a.length + b.length];
    System.arraycopy(a, 0, c, 0, a.length);
    System.arraycopy(b, 0, c, a.length, b.length);
    return c;
  }

  static public String[] concat(String a[], String b[]) {
    String c[] = new String[a.length + b.length];
    System.arraycopy(a, 0, c, 0, a.length);
    System.arraycopy(b, 0, c, a.length, b.length);
    return c;
  }

  static public Object concat(Object a, Object b) {
    Class<?> type = a.getClass().getComponentType();
    int alength = Array.getLength(a);
    int blength = Array.getLength(b);
    Object outgoing = Array.newInstance(type, alength + blength);
    System.arraycopy(a, 0, outgoing, 0, alength);
    System.arraycopy(b, 0, outgoing, alength, blength);
    return outgoing;
  }

  //


 /**
   * ( begin auto-generated from reverse.xml )
   *
   * Reverses the order of an array.
   *
   * ( end auto-generated )
  * @webref data:array_functions
  * @param list booleans[], bytes[], chars[], ints[], floats[], or Strings[]
  * @see PApplet#sort(String[], int)
  */
  static public boolean[] reverse(boolean list[]) {
    boolean outgoing[] = new boolean[list.length];
    int length1 = list.length - 1;
    for (int i = 0; i < list.length; i++) {
      outgoing[i] = list[length1 - i];
    }
    return outgoing;
  }

  static public byte[] reverse(byte list[]) {
    byte outgoing[] = new byte[list.length];
    int length1 = list.length - 1;
    for (int i = 0; i < list.length; i++) {
      outgoing[i] = list[length1 - i];
    }
    return outgoing;
  }

  static public char[] reverse(char list[]) {
    char outgoing[] = new char[list.length];
    int length1 = list.length - 1;
    for (int i = 0; i < list.length; i++) {
      outgoing[i] = list[length1 - i];
    }
    return outgoing;
  }

  static public int[] reverse(int list[]) {
    int outgoing[] = new int[list.length];
    int length1 = list.length - 1;
    for (int i = 0; i < list.length; i++) {
      outgoing[i] = list[length1 - i];
    }
    return outgoing;
  }

  static public float[] reverse(float list[]) {
    float outgoing[] = new float[list.length];
    int length1 = list.length - 1;
    for (int i = 0; i < list.length; i++) {
      outgoing[i] = list[length1 - i];
    }
    return outgoing;
  }

  static public String[] reverse(String list[]) {
    String outgoing[] = new String[list.length];
    int length1 = list.length - 1;
    for (int i = 0; i < list.length; i++) {
      outgoing[i] = list[length1 - i];
    }
    return outgoing;
  }

  static public Object reverse(Object list) {
    Class<?> type = list.getClass().getComponentType();
    int length = Array.getLength(list);
    Object outgoing = Array.newInstance(type, length);
    for (int i = 0; i < length; i++) {
      Array.set(outgoing, i, Array.get(list, (length - 1) - i));
    }
    return outgoing;
  }



  //////////////////////////////////////////////////////////////

  // STRINGS


  /**
   * ( begin auto-generated from trim.xml )
   *
   * Removes whitespace characters from the beginning and end of a String. In
   * addition to standard whitespace characters such as space, carriage
   * return, and tab, this function also removes the Unicode "nbsp" character.
   *
   * ( end auto-generated )
   * @webref data:string_functions
   * @param str any string
   * @see PApplet#split(String, String)
   * @see PApplet#join(String[], char)
   */
  static public String trim(String str) {
    if (str == null) {
      return null;
    }
    return str.replace('\u00A0', ' ').trim();
  }


 /**
  * @param array a String array
  */
  static public String[] trim(String[] array) {
    if (array == null) {
      return null;
    }
    String[] outgoing = new String[array.length];
    for (int i = 0; i < array.length; i++) {
      if (array[i] != null) {
        outgoing[i] = trim(array[i]);
      }
    }
    return outgoing;
  }


  /**
   * ( begin auto-generated from join.xml )
   *
   * Combines an array of Strings into one String, each separated by the
   * character(s) used for the <b>separator</b> parameter. To join arrays of
   * ints or floats, it's necessary to first convert them to strings using
   * <b>nf()</b> or <b>nfs()</b>.
   *
   * ( end auto-generated )
   * @webref data:string_functions
   * @param list array of Strings
   * @param separator char or String to be placed between each item
   * @see PApplet#split(String, String)
   * @see PApplet#trim(String)
   * @see PApplet#nf(float, int, int)
   * @see PApplet#nfs(float, int, int)
   */
  static public String join(String[] list, char separator) {
    return join(list, String.valueOf(separator));
  }


  static public String join(String[] list, String separator) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < list.length; i++) {
      if (i != 0) sb.append(separator);
      sb.append(list[i]);
    }
    return sb.toString();
  }


  static public String[] splitTokens(String value) {
    return splitTokens(value, WHITESPACE);
  }


  /**
   * ( begin auto-generated from splitTokens.xml )
   *
   * The splitTokens() function splits a String at one or many character
   * "tokens." The <b>tokens</b> parameter specifies the character or
   * characters to be used as a boundary.
   * <br/> <br/>
   * If no <b>tokens</b> character is specified, any whitespace character is
   * used to split. Whitespace characters include tab (\\t), line feed (\\n),
   * carriage return (\\r), form feed (\\f), and space. To convert a String
   * to an array of integers or floats, use the datatype conversion functions
   * <b>int()</b> and <b>float()</b> to convert the array of Strings.
   *
   * ( end auto-generated )
   * @webref data:string_functions
   * @param value the String to be split
   * @param delim list of individual characters that will be used as separators
   * @see PApplet#split(String, String)
   * @see PApplet#join(String[], String)
   * @see PApplet#trim(String)
   */
  static public String[] splitTokens(String value, String delim) {
    StringTokenizer toker = new StringTokenizer(value, delim);
    String pieces[] = new String[toker.countTokens()];

    int index = 0;
    while (toker.hasMoreTokens()) {
      pieces[index++] = toker.nextToken();
    }
    return pieces;
  }

  static public String[] split(String value, String delim) {
    List<String> items = new ArrayList<>();
    int index;
    int offset = 0;
    while ((index = value.indexOf(delim, offset)) != -1) {
      items.add(value.substring(offset, index));
      offset = index + delim.length();
    }
    items.add(value.substring(offset));
    String[] outgoing = new String[items.size()];
    items.toArray(outgoing);
    return outgoing;
  }


  static protected LinkedHashMap<String, Pattern> matchPatterns;

  static Pattern matchPattern(String regexp) {
    Pattern p = null;
    if (matchPatterns == null) {
      matchPatterns = new LinkedHashMap<String, Pattern>(16, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, Pattern> eldest) {
          // Limit the number of match patterns at 10 most recently used
          return size() == 10;
        }
      };
    } else {
      p = matchPatterns.get(regexp);
    }
    if (p == null) {
      p = Pattern.compile(regexp, Pattern.MULTILINE | Pattern.DOTALL);
      matchPatterns.put(regexp, p);
    }
    return p;
  }


  /**
   * ( begin auto-generated from match.xml )
   *
   * The match() function is used to apply a regular expression to a piece of
   * text, and return matching groups (elements found inside parentheses) as
   * a String array. No match will return null. If no groups are specified in
   * the regexp, but the sequence matches, an array of length one (with the
   * matched text as the first element of the array) will be returned.<br />
   * <br />
   * To use the function, first check to see if the result is null. If the
   * result is null, then the sequence did not match. If the sequence did
   * match, an array is returned.
   * If there are groups (specified by sets of parentheses) in the regexp,
   * then the contents of each will be returned in the array.
   * Element [0] of a regexp match returns the entire matching string, and
   * the match groups start at element [1] (the first group is [1], the
   * second [2], and so on).<br />
   * <br />
   * The syntax can be found in the reference for Java's <a
   * href="http://download.oracle.com/javase/6/docs/api/">Pattern</a> class.
   * For regular expression syntax, read the <a
   * href="http://download.oracle.com/javase/tutorial/essential/regex/">Java
   * Tutorial</a> on the topic.
   *
   * ( end auto-generated )
   * @webref data:string_functions
   * @param str the String to be searched
   * @param regexp the regexp to be used for matching
   * @see PApplet#matchAll(String, String)
   * @see PApplet#split(String, String)
   * @see PApplet#splitTokens(String, String)
   * @see PApplet#join(String[], String)
   * @see PApplet#trim(String)
   */
  static public String[] match(String str, String regexp) {
    Pattern p = matchPattern(regexp);
    Matcher m = p.matcher(str);
    if (m.find()) {
      int count = m.groupCount() + 1;
      String[] groups = new String[count];
      for (int i = 0; i < count; i++) {
        groups[i] = m.group(i);
      }
      return groups;
    }
    return null;
  }


  /**
   * ( begin auto-generated from matchAll.xml )
   *
   * This function is used to apply a regular expression to a piece of text,
   * and return a list of matching groups (elements found inside parentheses)
   * as a two-dimensional String array. No matches will return null. If no
   * groups are specified in the regexp, but the sequence matches, a two
   * dimensional array is still returned, but the second dimension is only of
   * length one.<br />
   * <br />
   * To use the function, first check to see if the result is null. If the
   * result is null, then the sequence did not match at all. If the sequence
   * did match, a 2D array is returned. If there are groups (specified by
   * sets of parentheses) in the regexp, then the contents of each will be
   * returned in the array.
   * Assuming, a loop with counter variable i, element [i][0] of a regexp
   * match returns the entire matching string, and the match groups start at
   * element [i][1] (the first group is [i][1], the second [i][2], and so
   * on).<br />
   * <br />
   * The syntax can be found in the reference for Java's <a
   * href="http://download.oracle.com/javase/6/docs/api/">Pattern</a> class.
   * For regular expression syntax, read the <a
   * href="http://download.oracle.com/javase/tutorial/essential/regex/">Java
   * Tutorial</a> on the topic.
   *
   * ( end auto-generated )
   * @webref data:string_functions
   * @param str the String to be searched
   * @param regexp the regexp to be used for matching
   * @see PApplet#match(String, String)
   * @see PApplet#split(String, String)
   * @see PApplet#splitTokens(String, String)
   * @see PApplet#join(String[], String)
   * @see PApplet#trim(String)
   */
  static public String[][] matchAll(String str, String regexp) {
    Pattern p = matchPattern(regexp);
    Matcher m = p.matcher(str);
    List<String[]> results = new ArrayList<>();
    int count = m.groupCount() + 1;
    while (m.find()) {
      String[] groups = new String[count];
      for (int i = 0; i < count; i++) {
        groups[i] = m.group(i);
      }
      results.add(groups);
    }
    if (results.isEmpty()) {
      return null;
    }
    String[][] matches = new String[results.size()][count];
    for (int i = 0; i < matches.length; i++) {
      matches[i] = results.get(i);
    }
    return matches;
  }



  //////////////////////////////////////////////////////////////

  // CASTING FUNCTIONS, INSERTED BY PREPROC


  /**
   * Convert a char to a boolean. 'T', 't', and '1' will become the
   * boolean value true, while 'F', 'f', or '0' will become false.
   */
  /*
  static final public boolean parseBoolean(char what) {
    return ((what == 't') || (what == 'T') || (what == '1'));
  }
  */

  /**
   * <p>Convert an integer to a boolean. Because of how Java handles upgrading
   * numbers, this will also cover byte and char (as they will upgrade to
   * an int without any sort of explicit cast).</p>
   * <p>The preprocessor will convert boolean(what) to parseBoolean(what).</p>
   * @return false if 0, true if any other number
   */
  static final public boolean parseBoolean(int what) {
    return (what != 0);
  }

  /*
  // removed because this makes no useful sense
  static final public boolean parseBoolean(float what) {
    return (what != 0);
  }
  */

  /**
   * Convert the string "true" or "false" to a boolean.
   * @return true if 'what' is "true" or "TRUE", false otherwise
   */
  static final public boolean parseBoolean(String what) {
    return Boolean.parseBoolean(what);
  }

  // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .

  /*
  // removed, no need to introduce strange syntax from other languages
  static final public boolean[] parseBoolean(char what[]) {
    boolean outgoing[] = new boolean[what.length];
    for (int i = 0; i < what.length; i++) {
      outgoing[i] =
        ((what[i] == 't') || (what[i] == 'T') || (what[i] == '1'));
    }
    return outgoing;
  }
  */

  /**
   * Convert a byte array to a boolean array. Each element will be
   * evaluated identical to the integer case, where a byte equal
   * to zero will return false, and any other value will return true.
   * @return array of boolean elements
   */
  /*
  static final public boolean[] parseBoolean(byte what[]) {
    boolean outgoing[] = new boolean[what.length];
    for (int i = 0; i < what.length; i++) {
      outgoing[i] = (what[i] != 0);
    }
    return outgoing;
  }
  */

  /**
   * Convert an int array to a boolean array. An int equal
   * to zero will return false, and any other value will return true.
   * @return array of boolean elements
   */
  static final public boolean[] parseBoolean(int what[]) {
    boolean outgoing[] = new boolean[what.length];
    for (int i = 0; i < what.length; i++) {
      outgoing[i] = (what[i] != 0);
    }
    return outgoing;
  }

  /*
  // removed, not necessary... if necessary, convert to int array first
  static final public boolean[] parseBoolean(float what[]) {
    boolean outgoing[] = new boolean[what.length];
    for (int i = 0; i < what.length; i++) {
      outgoing[i] = (what[i] != 0);
    }
    return outgoing;
  }
  */

  static final public boolean[] parseBoolean(String what[]) {
    boolean outgoing[] = new boolean[what.length];
    for (int i = 0; i < what.length; i++) {
      outgoing[i] = Boolean.parseBoolean(what[i]);
    }
    return outgoing;
  }

  // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .

  static final public byte parseByte(boolean what) {
    return what ? (byte)1 : 0;
  }

  static final public byte parseByte(char what) {
    return (byte) what;
  }

  static final public byte parseByte(int what) {
    return (byte) what;
  }

  static final public byte parseByte(float what) {
    return (byte) what;
  }

  /*
  // nixed, no precedent
  static final public byte[] parseByte(String what) {  // note: array[]
    return what.getBytes();
  }
  */

  // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .

  static final public byte[] parseByte(boolean what[]) {
    byte outgoing[] = new byte[what.length];
    for (int i = 0; i < what.length; i++) {
      outgoing[i] = what[i] ? (byte)1 : 0;
    }
    return outgoing;
  }

  static final public byte[] parseByte(char what[]) {
    byte outgoing[] = new byte[what.length];
    for (int i = 0; i < what.length; i++) {
      outgoing[i] = (byte) what[i];
    }
    return outgoing;
  }

  static final public byte[] parseByte(int what[]) {
    byte outgoing[] = new byte[what.length];
    for (int i = 0; i < what.length; i++) {
      outgoing[i] = (byte) what[i];
    }
    return outgoing;
  }

  static final public byte[] parseByte(float what[]) {
    byte outgoing[] = new byte[what.length];
    for (int i = 0; i < what.length; i++) {
      outgoing[i] = (byte) what[i];
    }
    return outgoing;
  }

  /*
  static final public byte[][] parseByte(String what[]) {  // note: array[][]
    byte outgoing[][] = new byte[what.length][];
    for (int i = 0; i < what.length; i++) {
      outgoing[i] = what[i].getBytes();
    }
    return outgoing;
  }
  */

  // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .

  /*
  static final public char parseChar(boolean what) {  // 0/1 or T/F ?
    return what ? 't' : 'f';
  }
  */

  static final public char parseChar(byte what) {
    return (char) (what & 0xff);
  }

  static final public char parseChar(int what) {
    return (char) what;
  }

  /*
  static final public char parseChar(float what) {  // nonsensical
    return (char) what;
  }

  static final public char[] parseChar(String what) {  // note: array[]
    return what.toCharArray();
  }
  */

  // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .

  /*
  static final public char[] parseChar(boolean what[]) {  // 0/1 or T/F ?
    char outgoing[] = new char[what.length];
    for (int i = 0; i < what.length; i++) {
      outgoing[i] = what[i] ? 't' : 'f';
    }
    return outgoing;
  }
  */

  static final public char[] parseChar(byte what[]) {
    char outgoing[] = new char[what.length];
    for (int i = 0; i < what.length; i++) {
      outgoing[i] = (char) (what[i] & 0xff);
    }
    return outgoing;
  }

  static final public char[] parseChar(int what[]) {
    char outgoing[] = new char[what.length];
    for (int i = 0; i < what.length; i++) {
      outgoing[i] = (char) what[i];
    }
    return outgoing;
  }

  /*
  static final public char[] parseChar(float what[]) {  // nonsensical
    char outgoing[] = new char[what.length];
    for (int i = 0; i < what.length; i++) {
      outgoing[i] = (char) what[i];
    }
    return outgoing;
  }

  static final public char[][] parseChar(String what[]) {  // note: array[][]
    char outgoing[][] = new char[what.length][];
    for (int i = 0; i < what.length; i++) {
      outgoing[i] = what[i].toCharArray();
    }
    return outgoing;
  }
  */

  // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .

  static final public int parseInt(boolean what) {
    return what ? 1 : 0;
  }

  /**
   * Note that parseInt() will un-sign a signed byte value.
   */
  static final public int parseInt(byte what) {
    return what & 0xff;
  }

  /**
   * Note that parseInt('5') is unlike String in the sense that it
   * won't return 5, but the ascii value. This is because ((int) someChar)
   * returns the ascii value, and parseInt() is just longhand for the cast.
   */
  static final public int parseInt(char what) {
    return what;
  }

  /**
   * Same as floor(), or an (int) cast.
   */
  static final public int parseInt(float what) {
    return (int) what;
  }

  /**
   * Parse a String into an int value. Returns 0 if the value is bad.
   */
  static final public int parseInt(String what) {
    return parseInt(what, 0);
  }

  /**
   * Parse a String to an int, and provide an alternate value that
   * should be used when the number is invalid.
   */
  static final public int parseInt(String what, int otherwise) {
    try {
      int offset = what.indexOf('.');
      if (offset == -1) {
        return Integer.parseInt(what);
      } else {
        return Integer.parseInt(what.substring(0, offset));
      }
    } catch (NumberFormatException e) { }
    return otherwise;
  }

  // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .

  static final public int[] parseInt(boolean what[]) {
    int list[] = new int[what.length];
    for (int i = 0; i < what.length; i++) {
      list[i] = what[i] ? 1 : 0;
    }
    return list;
  }

  static final public int[] parseInt(byte what[]) {  // note this unsigns
    int list[] = new int[what.length];
    for (int i = 0; i < what.length; i++) {
      list[i] = (what[i] & 0xff);
    }
    return list;
  }

  static final public int[] parseInt(char what[]) {
    int list[] = new int[what.length];
    for (int i = 0; i < what.length; i++) {
      list[i] = what[i];
    }
    return list;
  }

  static public int[] parseInt(float what[]) {
    int inties[] = new int[what.length];
    for (int i = 0; i < what.length; i++) {
      inties[i] = (int)what[i];
    }
    return inties;
  }

  /**
   * Make an array of int elements from an array of String objects.
   * If the String can't be parsed as a number, its entry in the
   * array will be set to the value of the "missing" parameter.
   *
   * String s[] = { "1", "300", "apple", "44" };
   * int numbers[] = parseInt(s, 9999);
   *
   * numbers will contain { 1, 300, 9999, 44 }
   */
  static public int[] parseInt(String what[], int missing) {
    int output[] = new int[what.length];
    for (int i = 0; i < what.length; i++) {
      try {
        output[i] = Integer.parseInt(what[i]);
      } catch (NumberFormatException e) {
        output[i] = missing;
      }
    }
    return output;
  }

  // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .

  /*
  static final public float parseFloat(boolean what) {
    return what ? 1 : 0;
  }
  */

  /**
   * Convert an int to a float value. Also handles bytes because of
   * Java's rules for upgrading values.
   */
  static final public float parseFloat(int what) {  // also handles byte
    return what;
  }

  static final public float parseFloat(String what) {
    return parseFloat(what, Float.NaN);
  }

  static final public float parseFloat(String what, float otherwise) {
    try {
      return Float.parseFloat(what);
    } catch (NumberFormatException e) { }

    return otherwise;
  }

  // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .

  /*
  static final public float[] parseFloat(boolean what[]) {
    float floaties[] = new float[what.length];
    for (int i = 0; i < what.length; i++) {
      floaties[i] = what[i] ? 1 : 0;
    }
    return floaties;
  }

  static final public float[] parseFloat(char what[]) {
    float floaties[] = new float[what.length];
    for (int i = 0; i < what.length; i++) {
      floaties[i] = (char) what[i];
    }
    return floaties;
  }
  */

  static final public float[] parseFloat(byte what[]) {
    float floaties[] = new float[what.length];
    for (int i = 0; i < what.length; i++) {
      floaties[i] = what[i];
    }
    return floaties;
  }

  static final public float[] parseFloat(int what[]) {
    float floaties[] = new float[what.length];
    for (int i = 0; i < what.length; i++) {
      floaties[i] = what[i];
    }
    return floaties;
  }

  static final public float[] parseFloat(String what[]) {
    return parseFloat(what, Float.NaN);
  }

  static final public float[] parseFloat(String what[], float missing) {
    float output[] = new float[what.length];
    for (int i = 0; i < what.length; i++) {
      try {
        output[i] = Float.parseFloat(what[i]);
      } catch (NumberFormatException e) {
        output[i] = missing;
      }
    }
    return output;
  }

  // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .

  static final public String str(boolean x) {
    return String.valueOf(x);
  }

  static final public String str(byte x) {
    return String.valueOf(x);
  }

  static final public String str(char x) {
    return String.valueOf(x);
  }

  static final public String str(int x) {
    return String.valueOf(x);
  }

  static final public String str(float x) {
    return String.valueOf(x);
  }

  // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .

  static final public String[] str(boolean x[]) {
    String s[] = new String[x.length];
    for (int i = 0; i < x.length; i++) s[i] = String.valueOf(x[i]);
    return s;
  }

  static final public String[] str(byte x[]) {
    String s[] = new String[x.length];
    for (int i = 0; i < x.length; i++) s[i] = String.valueOf(x[i]);
    return s;
  }

  static final public String[] str(char x[]) {
    String s[] = new String[x.length];
    for (int i = 0; i < x.length; i++) s[i] = String.valueOf(x[i]);
    return s;
  }

  static final public String[] str(int x[]) {
    String s[] = new String[x.length];
    for (int i = 0; i < x.length; i++) s[i] = String.valueOf(x[i]);
    return s;
  }

  static final public String[] str(float x[]) {
    String s[] = new String[x.length];
    for (int i = 0; i < x.length; i++) s[i] = String.valueOf(x[i]);
    return s;
  }


  //////////////////////////////////////////////////////////////

  // INT NUMBER FORMATTING

  static public String nf(float num) {
    int inum = (int) num;
    if (num == inum) {
      return str(inum);
    }
    return str(num);
  }

  static public String[] nf(float[] nums) {
    String[] outgoing = new String[nums.length];
    for (int i = 0; i < nums.length; i++) {
      outgoing[i] = nf(nums[i]);
    }
    return outgoing;
  }

  /**
   * Integer number formatter.
   */

  static private NumberFormat int_nf;
  static private int int_nf_digits;
  static private boolean int_nf_commas;

  /**
   * ( begin auto-generated from nf.xml )
   *
   * Utility function for formatting numbers into strings. There are two
   * versions, one for formatting floats and one for formatting ints. The
   * values for the <b>digits</b>, <b>left</b>, and <b>right</b> parameters
   * should always be positive integers.<br /><br />As shown in the above
   * example, <b>nf()</b> is used to add zeros to the left and/or right of a
   * number. This is typically for aligning a list of numbers. To
   * <em>remove</em> digits from a floating-point number, use the
   * <b>int()</b>, <b>ceil()</b>, <b>floor()</b>, or <b>round()</b>
   * functions.
   *
   * ( end auto-generated )
   * @webref data:string_functions
   * @param nums the numbers to format
   * @param digits number of digits to pad with zero
   * @see PApplet#nfs(float, int, int)
   * @see PApplet#nfp(float, int, int)
   * @see PApplet#nfc(float, int)
   * @see <a href="https://processing.org/reference/intconvert_.html">int(float)</a>
   */

  static public String[] nf(int nums[], int digits) {
    String formatted[] = new String[nums.length];
    for (int i = 0; i < formatted.length; i++) {
      formatted[i] = nf(nums[i], digits);
    }
    return formatted;
  }

  /**
   * @param num the number to format
   */
  static public String nf(int num, int digits) {
    if ((int_nf != null) &&
        (int_nf_digits == digits) &&
        !int_nf_commas) {
      return int_nf.format(num);
    }

    int_nf = NumberFormat.getInstance();
    int_nf.setGroupingUsed(false); // no commas
    int_nf_commas = false;
    int_nf.setMinimumIntegerDigits(digits);
    int_nf_digits = digits;
    return int_nf.format(num);
  }

  /**
   * ( begin auto-generated from nfc.xml )
   *
   * Utility function for formatting numbers into strings and placing
   * appropriate commas to mark units of 1000. There are two versions, one
   * for formatting ints and one for formatting an array of ints. The value
   * for the <b>digits</b> parameter should always be a positive integer.
   * <br/><br/>
   * For a non-US locale, this will insert periods instead of commas, or
   * whatever is apprioriate for that region.
   *
   * ( end auto-generated )
   * @webref data:string_functions
   * @param nums the numbers to format
   * @see PApplet#nf(float, int, int)
   * @see PApplet#nfp(float, int, int)
   * @see PApplet#nfs(float, int, int)
   */
  static public String[] nfc(int nums[]) {
    String formatted[] = new String[nums.length];
    for (int i = 0; i < formatted.length; i++) {
      formatted[i] = nfc(nums[i]);
    }
    return formatted;
  }


  /**
   * @param num the number to format
   */
  static public String nfc(int num) {
    if ((int_nf != null) &&
        (int_nf_digits == 0) &&
        int_nf_commas) {
      return int_nf.format(num);
    }

    int_nf = NumberFormat.getInstance();
    int_nf.setGroupingUsed(true);
    int_nf_commas = true;
    int_nf.setMinimumIntegerDigits(0);
    int_nf_digits = 0;
    return int_nf.format(num);
  }


  /**
   * number format signed (or space)
   * Formats a number but leaves a blank space in the front
   * when it's positive so that it can be properly aligned with
   * numbers that have a negative sign in front of them.
   */

  /**
   * ( begin auto-generated from nfs.xml )
   *
   * Utility function for formatting numbers into strings. Similar to
   * <b>nf()</b> but leaves a blank space in front of positive numbers so
   * they align with negative numbers in spite of the minus symbol. There are
   * two versions, one for formatting floats and one for formatting ints. The
   * values for the <b>digits</b>, <b>left</b>, and <b>right</b> parameters
   * should always be positive integers.
   *
   * ( end auto-generated )
  * @webref data:string_functions
  * @param num the number to format
  * @param digits number of digits to pad with zeroes
  * @see PApplet#nf(float, int, int)
  * @see PApplet#nfp(float, int, int)
  * @see PApplet#nfc(float, int)
  */
  static public String nfs(int num, int digits) {
    return (num < 0) ? nf(num, digits) : (' ' + nf(num, digits));
  }

  /**
   * @param nums the numbers to format
   */
  static public String[] nfs(int nums[], int digits) {
    String formatted[] = new String[nums.length];
    for (int i = 0; i < formatted.length; i++) {
      formatted[i] = nfs(nums[i], digits);
    }
    return formatted;
  }

  //

  /**
   * number format positive (or plus)
   * Formats a number, always placing a - or + sign
   * in the front when it's negative or positive.
   */
 /**
   * ( begin auto-generated from nfp.xml )
   *
   * Utility function for formatting numbers into strings. Similar to
   * <b>nf()</b> but puts a "+" in front of positive numbers and a "-" in
   * front of negative numbers. There are two versions, one for formatting
   * floats and one for formatting ints. The values for the <b>digits</b>,
   * <b>left</b>, and <b>right</b> parameters should always be positive integers.
   *
   * ( end auto-generated )
  * @webref data:string_functions
  * @param num the number to format
  * @param digits number of digits to pad with zeroes
  * @see PApplet#nf(float, int, int)
  * @see PApplet#nfs(float, int, int)
  * @see PApplet#nfc(float, int)
  */
  static public String nfp(int num, int digits) {
    return (num < 0) ? nf(num, digits) : ('+' + nf(num, digits));
  }
  /**
   * @param nums the numbers to format
   */
  static public String[] nfp(int nums[], int digits) {
    String formatted[] = new String[nums.length];
    for (int i = 0; i < formatted.length; i++) {
      formatted[i] = nfp(nums[i], digits);
    }
    return formatted;
  }



  //////////////////////////////////////////////////////////////

  // FLOAT NUMBER FORMATTING

  static private NumberFormat float_nf;
  static private int float_nf_left, float_nf_right;
  static private boolean float_nf_commas;

  /**
   * @param left number of digits to the left of the decimal point
   * @param right number of digits to the right of the decimal point
   */
  static public String[] nf(float nums[], int left, int right) {
    String formatted[] = new String[nums.length];
    for (int i = 0; i < formatted.length; i++) {
      formatted[i] = nf(nums[i], left, right);
    }
    return formatted;
  }

  static public String nf(float num, int left, int right) {
    if ((float_nf != null) &&
        (float_nf_left == left) &&
        (float_nf_right == right) &&
        !float_nf_commas) {
      return float_nf.format(num);
    }

    float_nf = NumberFormat.getInstance();
    float_nf.setGroupingUsed(false);
    float_nf_commas = false;

    if (left != 0) float_nf.setMinimumIntegerDigits(left);
    if (right != 0) {
      float_nf.setMinimumFractionDigits(right);
      float_nf.setMaximumFractionDigits(right);
    }
    float_nf_left = left;
    float_nf_right = right;
    return float_nf.format(num);
  }

  /**
   * @param right number of digits to the right of the decimal point
  */
  static public String[] nfc(float nums[], int right) {
    String formatted[] = new String[nums.length];
    for (int i = 0; i < formatted.length; i++) {
      formatted[i] = nfc(nums[i], right);
    }
    return formatted;
  }

  static public String nfc(float num, int right) {
    if ((float_nf != null) &&
        (float_nf_left == 0) &&
        (float_nf_right == right) &&
        float_nf_commas) {
      return float_nf.format(num);
    }

    float_nf = NumberFormat.getInstance();
    float_nf.setGroupingUsed(true);
    float_nf_commas = true;

    if (right != 0) {
      float_nf.setMinimumFractionDigits(right);
      float_nf.setMaximumFractionDigits(right);
    }
    float_nf_left = 0;
    float_nf_right = right;
    return float_nf.format(num);
  }


 /**
  * @param left the number of digits to the left of the decimal point
  * @param right the number of digits to the right of the decimal point
  */
  static public String[] nfs(float nums[], int left, int right) {
    String formatted[] = new String[nums.length];
    for (int i = 0; i < formatted.length; i++) {
      formatted[i] = nfs(nums[i], left, right);
    }
    return formatted;
  }

  static public String nfs(float num, int left, int right) {
    return (num < 0) ? nf(num, left, right) :  (' ' + nf(num, left, right));
  }

 /**
  * @param left the number of digits to the left of the decimal point
  * @param right the number of digits to the right of the decimal point
  */
  static public String[] nfp(float nums[], int left, int right) {
    String formatted[] = new String[nums.length];
    for (int i = 0; i < formatted.length; i++) {
      formatted[i] = nfp(nums[i], left, right);
    }
    return formatted;
  }

  static public String nfp(float num, int left, int right) {
    return (num < 0) ? nf(num, left, right) :  ('+' + nf(num, left, right));
  }



  //////////////////////////////////////////////////////////////

  // HEX/BINARY CONVERSION


  /**
   * ( begin auto-generated from hex.xml )
   *
   * Converts a byte, char, int, or color to a String containing the
   * equivalent hexadecimal notation. For example color(0, 102, 153) will
   * convert to the String "FF006699". This function can help make your geeky
   * debugging sessions much happier.
   * <br/> <br/>
   * Note that the maximum number of digits is 8, because an int value can
   * only represent up to 32 bits. Specifying more than eight digits will
   * simply shorten the string to eight anyway.
   *
   * ( end auto-generated )
   * @webref data:conversion
   * @param value the value to convert
   * @see PApplet#unhex(String)
   * @see PApplet#binary(byte)
   */
  static final public String hex(byte value) {
    return hex(value, 2);
  }

  static final public String hex(char value) {
    return hex(value, 4);
  }

  static final public String hex(int value) {
    return hex(value, 8);
  }
/**
 * @param digits the number of digits (maximum 8)
 */
  static final public String hex(int value, int digits) {
    String stuff = Integer.toHexString(value).toUpperCase();
    if (digits > 8) {
      digits = 8;
    }

    int length = stuff.length();
    if (length > digits) {
      return stuff.substring(length - digits);

    } else if (length < digits) {
      return "00000000".substring(8 - (digits-length)) + stuff;
    }
    return stuff;
  }

 /**
   * ( begin auto-generated from unhex.xml )
   *
   * Converts a String representation of a hexadecimal number to its
   * equivalent integer value.
   *
   * ( end auto-generated )
   *
   * @webref data:conversion
   * @param value String to convert to an integer
   * @see PApplet#hex(int, int)
   * @see PApplet#binary(byte)
   */
  static final public int unhex(String value) {
    // has to parse as a Long so that it'll work for numbers bigger than 2^31
    return (int) (Long.parseLong(value, 16));
  }

  //

  /**
   * Returns a String that contains the binary value of a byte.
   * The returned value will always have 8 digits.
   */
  static final public String binary(byte value) {
    return binary(value, 8);
  }

  /**
   * Returns a String that contains the binary value of a char.
   * The returned value will always have 16 digits because chars
   * are two bytes long.
   */
  static final public String binary(char value) {
    return binary(value, 16);
  }

  /**
   * Returns a String that contains the binary value of an int. The length
   * depends on the size of the number itself. If you want a specific number
   * of digits use binary(int what, int digits) to specify how many.
   */
  static final public String binary(int value) {
    return binary(value, 32);
  }

  /*
   * Returns a String that contains the binary value of an int.
   * The digits parameter determines how many digits will be used.
   */

 /**
   * ( begin auto-generated from binary.xml )
   *
   * Converts a byte, char, int, or color to a String containing the
   * equivalent binary notation. For example color(0, 102, 153, 255) will
   * convert to the String "11111111000000000110011010011001". This function
   * can help make your geeky debugging sessions much happier.
   * <br/> <br/>
   * Note that the maximum number of digits is 32, because an int value can
   * only represent up to 32 bits. Specifying more than 32 digits will simply
   * shorten the string to 32 anyway.
   *
   * ( end auto-generated )
  * @webref data:conversion
  * @param value value to convert
  * @param digits number of digits to return
  * @see PApplet#hex(int,int)
  * @see PApplet#unhex(String)
  */
  static final public String binary(int value, int digits) {
    String stuff = Integer.toBinaryString(value);
    if (digits > 32) {
      digits = 32;
    }

    int length = stuff.length();
    if (length > digits) {
      return stuff.substring(length - digits);

    } else if (length < digits) {
      int offset = 32 - (digits-length);
      return "00000000000000000000000000000000".substring(offset) + stuff;
    }
    return stuff;
  }

  //////////////////////////////////////////////////////////////

  // COLOR FUNCTIONS

  // moved here so that they can work without
  // the graphics actually being instantiated (outside setup)


  /**
   * ( begin auto-generated from color.xml )
   *
   * Creates colors for storing in variables of the <b>color</b> datatype.
   * The parameters are interpreted as RGB or HSB values depending on the
   * current <b>colorMode()</b>. The default mode is RGB values from 0 to 255
   * and therefore, the function call <b>color(255, 204, 0)</b> will return a
   * bright yellow color. More about how colors are stored can be found in
   * the reference for the <a href="color_datatype.html">color</a> datatype.
   *
   * ( end auto-generated )
   * @webref color:creating_reading
   * @param gray number specifying value between white and black
   * @see PApplet#colorMode(int)
   */
  public static final int color(int gray) {
    if (gray > 255) gray = 255; else if (gray < 0) gray = 0;
    return 0xff000000 | (gray << 16) | (gray << 8) | gray;
  }


  /**
   * @nowebref
   * @param fgray number specifying value between white and black
   */
  public static final int color(float fgray) {
    int gray = (int) fgray;
    if (gray > 255) gray = 255; else if (gray < 0) gray = 0;
    return 0xff000000 | (gray << 16) | (gray << 8) | gray;
  }


  /**
   * As of 0116 this also takes color(#FF8800, alpha)
   * @param alpha relative to current color range
   */
  public static final int color(int gray, int alpha) {
    if (alpha > 255) alpha = 255; else if (alpha < 0) alpha = 0;
    if (gray > 255) {
      // then assume this is actually a #FF8800
      return (alpha << 24) | (gray & 0xFFFFFF);
    } else {
      //if (gray > 255) gray = 255; else if (gray < 0) gray = 0;
      return (alpha << 24) | (gray << 16) | (gray << 8) | gray;
    }
  }


  /**
   * @nowebref
   */
  public static final int color(float fgray, float falpha) {
    int gray = (int) fgray;
    int alpha = (int) falpha;
    if (gray > 255) gray = 255; else if (gray < 0) gray = 0;
    if (alpha > 255) alpha = 255; else if (alpha < 0) alpha = 0;
    return (alpha << 24) | (gray << 16) | (gray << 8) | gray;
  }


  /**
   * @param v1 red or hue values relative to the current color range
   * @param v2 green or saturation values relative to the current color range
   * @param v3 blue or brightness values relative to the current color range
   */
  public static final int color(int v1, int v2, int v3) {
    if (v1 > 255) v1 = 255; else if (v1 < 0) v1 = 0;
    if (v2 > 255) v2 = 255; else if (v2 < 0) v2 = 0;
    if (v3 > 255) v3 = 255; else if (v3 < 0) v3 = 0;

    return 0xff000000 | (v1 << 16) | (v2 << 8) | v3;
  }


  public static final int color(int v1, int v2, int v3, int alpha) {
    if (alpha > 255) alpha = 255; else if (alpha < 0) alpha = 0;
    if (v1 > 255) v1 = 255; else if (v1 < 0) v1 = 0;
    if (v2 > 255) v2 = 255; else if (v2 < 0) v2 = 0;
    if (v3 > 255) v3 = 255; else if (v3 < 0) v3 = 0;

    return (alpha << 24) | (v1 << 16) | (v2 << 8) | v3;
  }


  public static final int color(float v1, float v2, float v3) {
    if (v1 > 255) v1 = 255; else if (v1 < 0) v1 = 0;
    if (v2 > 255) v2 = 255; else if (v2 < 0) v2 = 0;
    if (v3 > 255) v3 = 255; else if (v3 < 0) v3 = 0;

    return 0xff000000 | ((int)v1 << 16) | ((int)v2 << 8) | (int)v3;
  }


  public static final int color(float v1, float v2, float v3, float alpha) {
    if (alpha > 255) alpha = 255; else if (alpha < 0) alpha = 0;
    if (v1 > 255) v1 = 255; else if (v1 < 0) v1 = 0;
    if (v2 > 255) v2 = 255; else if (v2 < 0) v2 = 0;
    if (v3 > 255) v3 = 255; else if (v3 < 0) v3 = 0;

    return ((int)alpha << 24) | ((int)v1 << 16) | ((int)v2 << 8) | (int)v3;
  }


  /**
   * ( begin auto-generated from lerpColor.xml )
   *
   * Calculates a color or colors between two color at a specific increment.
   * The <b>amt</b> parameter is the amount to interpolate between the two
   * values where 0.0 equal to the first point, 0.1 is very near the first
   * point, 0.5 is half-way in between, etc.
   *
   * ( end auto-generated )
   *
   * @webref color:creating_reading
   * @usage web_application
   * @param c1 interpolate from this color
   * @param c2 interpolate to this color
   * @param amt between 0.0 and 1.0
   * @see PImage#blendColor(int, int, int)
   * @see PGraphics#color(float, float, float, float)
   * @see PApplet#lerp(float, float, float)
   */
  public int lerpColor(int c1, int c2, float amt) {
    return PGraphics.lerpColor(c1, c2, amt, RGB);
  }


  static public int blendColor(int c1, int c2, int mode) {
    return PImage.blendColor(c1, c2, mode);
  }



  //////////////////////////////////////////////////////////////

  //////////////////////////////////////////////////////////////

  // MAIN

  //  protected void createSurface() {
//    surface = g.createSurface();
//    if (surface == null) {
//      System.err.println("This renderer needs to be updated for Processing 3");
//      System.err.println("The createSurface() method returned null.");
//      System.exit(1);
//    }
//  }


//  /**
//   * Return a Canvas object that can be embedded into other Java GUIs.
//   * This is necessary because PApplet no longer subclasses Component.
//   *
//   * <pre>
//   * PApplet sketch = new EmbedSketch();
//   * Canvas canvas = sketch.getCanvas();
//   * // add the canvas object to your project and validate() it
//   * sketch.init()  // start the animation thread
//   */
//  public Component getComponent() {
//    g = createPrimaryGraphics();
//    surface = g.createSurface();
//    return surface.initComponent(this);
//  }

  //////////////////////////////////////////////////////////////

  /*
  public PShape beginRecord() {
    return g.beginRecord();
  }
  */

  //////////////////////////////////////////////////////////////

  //////////////////////////////////////////////////////////////

  // EVERYTHING BELOW THIS LINE IS AUTOMATICALLY GENERATED. DO NOT TOUCH!
  // This includes the Javadoc comments, which are automatically copied from
  // the PImage and PGraphics source code files.

  // public functions for processing.core

}
