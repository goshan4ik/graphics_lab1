package com.sfedu.mmcs

import tornadofx.*
import javafx.application.Application
import javafx.geometry.Insets
import javafx.scene.canvas.Canvas
import javafx.scene.control.TextField
import javafx.scene.layout.Pane
import javafx.scene.paint.Color
import javafx.stage.Stage
import java.lang.Double.max
import java.lang.Double.min
import kotlin.math.cos
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

class DrawingView: View() {

    private var leftXInput: TextField by singleAssign()
    private var rightXInput: TextField by singleAssign()

    private var canvas: Canvas by singleAssign()

    override val root = Pane()
    init {
        with (root) {
            addChildIfPossible(
                vbox {
                    hbox {
                        leftXInput = textfield {
                            hboxConstraints {
                                margin = Insets(15.0)
                            }
                            label("LeftX")
                        }
                        rightXInput = textfield {
                            hboxConstraints {
                                margin = Insets(15.0)
                            }
                            label("LeftX")
                        }
                        button("Draw") {
                            hboxConstraints {
                                margin = Insets(15.0)
                            }
                            action {
                                val chart = Chart(leftXInput.text.toDouble(), rightXInput.text.toDouble(), canvas.width.toInt(), canvas.height.toInt(), {x: Double -> cos(x)})
                                val functionChart = chart.drawChart()
                                val previous = functionChart[0]
                                val gc = canvas.graphicsContext2D
                                gc.clearRect(0.0, 0.0, canvas.width, canvas.height)
                                gc.lineWidth = 1.0
                                gc.fill = Color.BLACK
                                gc.moveTo(previous.x.toDouble(), previous.y.toDouble())
                                gc.beginPath()

                                for (i in 1 until functionChart.size) {
                                    val current = functionChart[i]
                                    gc.lineTo(current.x.toDouble(), current.y.toDouble())
                                }
                                gc.stroke()
                                drawXAxis(chart)
                                drawYAxis(chart)
                                gc.closePath()
                            }
                        }
                        button("Clear") {
                            hboxConstraints {
                                margin = Insets(15.0)
                            }
                            action {
                                val gc = canvas.graphicsContext2D
                                gc.fill = Color.WHITE
                                gc.fillRect(0.0, 0.0, canvas.width, canvas.height)
//                                gc.clearRect(0.0, 0.0, canvas.width, canvas.height)
                            }
                        }
                    }
                    canvas = canvas {
                        vboxConstraints {
                            margin = Insets(15.0)
                        }
                        width = 1200.0
                        height = 600.0
                    }
                }
            )
        }
    }

    private fun drawXAxis(chart: Chart) {
        val xAxis = chart.calcXAxis()
        val gc = canvas.graphicsContext2D
        gc.lineWidth = 1.0
        gc.moveTo(xAxis.x0.toDouble(), xAxis.y0.toDouble())
        gc.lineTo(xAxis.x1.toDouble(), xAxis.y1.toDouble())

        var x = 0.0
        val notchY0 = if (xAxis.y0 + 5 <= canvas.height) {
            (xAxis.y0 + 5).toDouble()
        } else {
            xAxis.y0.toDouble()
        }
        val notchY1 = if (xAxis.y0 - 5 >= 0) {
            (xAxis.y0 - 5).toDouble()
        } else {
            xAxis.y0.toDouble()
        }
        val step = xAxis.step
        while (x < canvas.width) {
            gc.moveTo(x, notchY0)
            gc.lineTo(x, notchY1)
            x += step
        }
        gc.stroke()
    }

    private fun drawYAxis(chart: Chart) {
        val yAxis = chart.calcYAxis()
        val gc = canvas.graphicsContext2D
        gc.lineWidth = 1.0
        gc.moveTo(yAxis.x0.toDouble(), yAxis.y0.toDouble())
        gc.lineTo(yAxis.x1.toDouble(), yAxis.y1.toDouble())

        var y = 0.0
        val notchX0 = if (yAxis.x0 + 5 <= canvas.width) {
            (yAxis.x0 + 5).toDouble()
        } else {
            yAxis.x0.toDouble()
        }
        val notchX1 = if (yAxis.x0 - 5 >= 0) {
            (yAxis.x0 - 5).toDouble()
        } else {
            yAxis.x0.toDouble()
        }
        val step = yAxis.step
        while (y < canvas.height) {
            gc.moveTo(notchX0, y)
            gc.lineTo(notchX1, y)
            y += step
        }
        gc.stroke()
    }

}

class Point(val x: Double, val y: Double)

class IntPoint(val x: Int, val y: Int)

class Chart(private val left: Double, private val right: Double, private val canvasWidth: Int, private val canvasHeight: Int,
            private val function: (Double) -> Double
) {
    private var graphicWidth = right - left
    private var graphicHeight = 0.0
    private lateinit var functionValues: MutableList<Point>
    private var minValue = 0.0
    private var maxValue = 0.0

    //TODO rename
    fun drawChart(): List<IntPoint> {
        val xStep = (right - left) / canvasWidth
        calcFunctionValues(xStep)
        return functionValues.map { point ->
            val x = canvasWidth * (point.x - left) / graphicWidth
            val y = canvasHeight * (maxValue - point.y) / graphicHeight
            IntPoint(x.roundToInt(), y.roundToInt())
        }
    }

    private fun calcFunctionValues(step: Double) {
        functionValues = ArrayList(canvasWidth)
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
            val x = (canvasWidth * -left / graphicWidth).roundToInt()
            Axis(x, 0, x, canvasHeight, step)
        } else if (left >= 0) {
            Axis(0, 0, 0, canvasHeight, step)
        } else {
            Axis(canvasWidth, 0, canvasWidth, canvasHeight, step)
        }
    }

    fun calcXAxis(): Axis {
        val step = canvasWidth / graphicWidth
        return if (minValue < 0 && 0 < maxValue) {
            val y = (canvasHeight * (maxValue) / graphicHeight).roundToInt()
            Axis(0, y, canvasWidth, y, step)
        } else if (maxValue >= 0) {
            Axis(0, canvasHeight, canvasWidth, canvasHeight, step)
        } else {
            Axis(0, 0, canvasWidth, 0, step)
        }
    }

}

class Axis(val x0: Int, val y0: Int, val x1:Int, val y1: Int, val step: Double)