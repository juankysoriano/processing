import processing.core.PApplet
import processing.core.PConstants

fun main(args: Array<String>) {
    val test: PApplet = object : PApplet() {
        override fun draw() {
            background(255f, 0f, 0f)
            ellipse(0f, 0f, 10f, 10f)
        }

        override fun settings() {
            size(500, 500, PConstants.P2D)
        }
    }
    PApplet.runSketch(arrayOf(" "), test)
}
