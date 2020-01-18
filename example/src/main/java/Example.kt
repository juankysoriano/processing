import processing.core.PApplet
import processing.core.PGraphics

fun main(args: Array<String>) {
    Processing.run(
        700,
        700,
        PApplet.P2D
    ) { graphics ->
        graphics.beginDraw()
        graphics.background(0)
        graphics.stroke(0, 1f)
        graphics.fill(0f, 255f, 0f, 127f)
        graphics.ellipse(150f, 150f, 150f, 150f)
        graphics.save("test.jpg")
        graphics.endDraw()
    }
}


abstract class Processing {
    companion object {
        fun run(
            width: Int,
            height: Int,
            renderer: String = PApplet.JAVA2D,
            block: (PGraphics) -> Unit
        ) {
            object : PApplet() {
                override fun settings() {
                    size(0, 0, renderer)
                    smooth()
                }
                override fun draw() {
                    block(createGraphics(width, height, renderer))
                }
            }.apply {
                settings()
                initSurface()
                surface.setVisible(true)
                surface.startThread()
                surface.stopThread()
            }
        }
    }
}
