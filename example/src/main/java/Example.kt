import processing.Processing
import processing.core.PApplet
import processing.draw

fun main(args: Array<String>) {
    Processing.initialize(
        headless = true,
        openGL = true,
        renderer = PApplet.JAVA2D
    )
    Processing.createGraphics(500, 500).draw {
        background(0)
        stroke(0, 1f)
        fill(0f, 255f, 0f, 127f)
        ellipse(150f, 150f, 150f, 150f)
        save("test.jpg")
    }
    Processing.trigger()
}
