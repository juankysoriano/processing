import processing.Processing
import processing.core.PApplet
import processing.draw

fun main(args: Array<String>) {
    Processing.initialize(
        renderer = PApplet.P2D
    )
    val graphics = Processing.createGraphics(500, 500)
    graphics.draw {
        println("test1-")
        background(0)
        stroke(0, 1f)
        println("test1a")
        fill(0f, 255f, 0f, 127f)
        ellipse(150f, 150f, 150f, 150f)
        save("test1.jpg")
        println("test1")
    }
    graphics.draw {
        println("test2-")
        background(0)
        stroke(0, 1f)
        println("test2a")
        fill(255f, 255f, 0f, 127f)
        ellipse(150f, 150f, 150f, 150f)
        save("test2.jpg")
        println("test2")
    }

}
