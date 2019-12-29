import processing.core.PApplet
import processing.core.PGraphics
import kotlin.system.measureTimeMillis

fun main(args: Array<String>) {
    Processing.run(
        700,
        700,
        PApplet.P3D
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
            renderer: String = PApplet.P2D,
            block: (PGraphics) -> Unit
        ) {
            object : PApplet() {
                override fun settings() {
                    measureTimeMillis {
                        size(0, 0, renderer)
                        smooth()
                    }.apply {
                        println("Settings: $this")
                    }

                }

                override fun draw() {
                    measureTimeMillis {
                        block(createGraphics(width, height, renderer))
                    }.apply {
                        println("Draw: $this")
                    }
                    exit()
                }
            }.apply {
                measureTimeMillis {
                    PApplet.runSketch(this)
                }.apply { println("Run sketch: $this") }
            }
        }
    }
}
