package com.juankysoriano.processing.example

import processing.opengl.PGraphics2D

fun main(args: Array<String> ) {
    val graphics = PGraphics2D().apply {
        setSize(200, 200)
    }
    graphics.beginDraw()
    graphics.background(255f,0f,0f)
    graphics.endDraw()
    graphics.save("test.jpg")
}
