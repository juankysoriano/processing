package processing

import processing.core.PApplet
import processing.core.PConstants.JAVA2D
import processing.core.PGraphics
import processing.core.PImage
import java.awt.Image
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO

abstract class Processing {
    companion object {
        private var applet: PApplet? = null
        private var renderer: String = JAVA2D
        fun initialize(headless: Boolean = false,
                       openGL: Boolean = false,
                       renderer: String = JAVA2D
        ) {
            System.setProperty("java.awt.headless", headless.toString())
            System.setProperty("sun.java2d.opengl", openGL.toString())
            applet = object : PApplet() {
                override fun settings() {
                    size(0, 0, renderer)
                }

                override fun draw() {}
            }.apply {
                settings()
                initSurface()
                surface.setVisible(true)
            }
        }

        fun createGraphics(
            width: Int,
            height: Int,
            format: Format = Format.RGB,
            quality: Quality = Quality.LOW): PGraphics =
            createGraphics(PImage(width, height, format.value), quality)

        fun createGraphics(
            image: PImage,
            quality: Quality = Quality.LOW): PGraphics =
            checkNotNull(applet, { "Cannot create graphics before initialising processing" })
                .createGraphics(image.width, image.height, renderer)
                .apply {
                    this.image = image.image
                    smooth(quality.value)
                }

        fun trigger() {
            checkNotNull(applet, { "Cannot trigger graphics before initialising processing" }).apply {
                surface.startThread()
                surface.stopThread()
            }

        }
    }
}

inline fun PGraphics.draw(block: PGraphics.() -> Unit) {
    beginDraw()
    block(this)
    endDraw()
}

inline val PGraphics.area
    get() = width * height

val PGraphics.bytes: ByteArray
    get() = ByteArrayOutputStream().use {
        ImageIO.write(image.toBufferedImage(), "jpg", it)
        it.flush()
        it.toByteArray()
    }

fun PGraphics.configure(width: Int, height: Int, format: Format = Format.RGB, quality: Quality = Quality.LOW) {
    configure(
        image = PImage(width, height, format.value),
        quality = quality
    )
}

fun PGraphics.configure(image: PImage, quality: Quality) {
    smooth(quality.value)
    setSize(image.width, image.height)
    this.image = image.image
}

enum class Quality(val value: Int) {
    LOW(0), MID(1), HIGH(2)
}

enum class Format(val value: Int) {
    RGB(PImage.RGB), ARGB(PImage.ARGB)
}

private fun Image.toBufferedImage() = this as? BufferedImage ?: BufferedImage(
    getWidth(null),
    getHeight(null),
    BufferedImage.TYPE_INT_ARGB
).apply {
    createGraphics().apply {
        drawImage(this@toBufferedImage, 0, 0, null)
        dispose()
    }
}

sealed class Color(
    open val red: Int,
    open val green: Int,
    open val blue: Int,
    open val alpha: Int
) {
    data class Gray(
        val gray: Int = 0,
        override val alpha: Int = 255
    ) : Color(gray, gray, gray, alpha)

    data class RGB(
        override val red: Int = 0,
        override val green: Int = 0,
        override val blue: Int = 0,
        override val alpha: Int = 255
    ) : Color(red, green, blue, alpha)
}

fun PGraphics.background(color: Color) {
    background(color.red.toFloat(), color.green.toFloat(), color.blue.toFloat(), color.alpha.toFloat())
}

fun PGraphics.fill(color: Color) {
    fill(color.red.toFloat(), color.green.toFloat(), color.blue.toFloat(), color.alpha.toFloat())
}

fun PGraphics.stroke(color: Color, weight: Float = 1f) {
    stroke(color.red.toFloat(), color.green.toFloat(), color.blue.toFloat(), color.alpha.toFloat())
    strokeWeight(weight)
}
