package com.sfedu.mmcs

import tornadofx.*
import javafx.application.Application
import javafx.stage.Stage
import java.lang.Double.max
import java.lang.Double.min
import kotlin.math.roundToInt


class World : App() {
    override val primaryView = DrawingView::class
    override fun start(stage: Stage) {
        super.start(stage)
        stage.width = 1300.0
        stage.height = 1200.0
    }
}

fun main(args: Array<String>) {
    Application.launch(World::class.java, *args)
}

class Chart(private val left: Double, private val right: Double,
            private val canvasWidth: Double, private val canvasHeight: Double,
            private val function: (Double) -> Double
) {
    private var graphicWidth = right - left
    private var graphicHeight = 0.0
    private lateinit var functionValues: MutableList<Point>
    private var minValue = 0.0
    private var maxValue = 0.0

    fun build(): List<Point> {
        val xStep = (right - left) / canvasWidth
        calcFunctionValues(xStep)
        return functionValues.map { point ->
            val x = canvasWidth * (point.x - left) / graphicWidth
            val y = canvasHeight * (maxValue - point.y) / graphicHeight
            Point(x, y)
        }
    }

    private fun calcFunctionValues(step: Double) {
        functionValues = ArrayList(canvasWidth.roundToInt())
        var x = left
        while(x <= right) {
            val y = function(x)
            functionValues.add(Point(x, y))
            minValue = min(minValue, y)
            maxValue = max(maxValue, y)
            x += step
        }
        graphicHeight = maxValue - minValue
    }

    fun calcYAxis(): Axis {
        val step = canvasHeight / graphicHeight
        return if (left < 0 && 0 < right) {
            val x = canvasWidth * -left / graphicWidth
            Axis(x, 0.0, x, canvasHeight, step)
        } else if (left >= 0) {
            Axis(0.0, 0.0, 0.0, canvasHeight, step)
        } else {
            Axis(canvasWidth, 0.0, canvasWidth, canvasHeight, step)
        }
    }

    fun calcXAxis(): Axis {
        val step = canvasWidth / graphicWidth
        return if (minValue < 0 && 0 < maxValue) {
            val y = canvasHeight * (maxValue) / graphicHeight
            Axis(0.0, y, canvasWidth, y, step)
        } else if (maxValue >= 0) {
            Axis(0.0, canvasHeight, canvasWidth, canvasHeight, step)
        } else {
            Axis(0.0, 0.0, canvasWidth, 0.0, step)
        }
    }

}

class Point(val x: Double, val y: Double)

class Axis(val x0: Double, val y0: Double, val x1:Double, val y1: Double, val step: Double)