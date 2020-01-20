import processing.Processing
import processing.Quality
import processing.core.PApplet
import processing.draw
import kotlin.random.Random

fun main(args: Array<String>) {
    Processing.initialize(
        renderer = PApplet.P2D
    )
    val graphics = Processing.createGraphics(
        width = 500,
        height = 500,
        quality = Quality.HIGH
    )
    graphics.draw {
        background(0)
        stroke(0, 255f)
        fill(0f, 255f, 0f, 255f)
        ellipse(150f, 150f, 150f, 150f)
        save("test1.jpg")
    }
    graphics.draw {
        background(0)
        stroke(0, 255f)
        fill(255f, 255f, 0f, 255f)
        ellipse(150f, 150f, 150f, 150f)
        save("test2.jpg")
    }
}
