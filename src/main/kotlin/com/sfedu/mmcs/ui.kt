package com.sfedu.mmcs

import javafx.geometry.Insets
import javafx.scene.canvas.Canvas
import javafx.scene.control.TextField
import javafx.scene.layout.Pane
import javafx.scene.paint.Color
import tornadofx.*
import kotlin.math.cos

class DrawingView: View() {

    private val INSET = 15.0
    private val NOTCH_SIZE = 5.0

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
                                margin = Insets(INSET)
                            }
                            label("LeftX")
                        }
                        rightXInput = textfield {
                            hboxConstraints {
                                margin = Insets(INSET)
                            }
                            label("LeftX")
                        }
                        button("Draw") {
                            hboxConstraints {
                                margin = Insets(INSET)
                            }
                            action {
                                val leftX = leftXInput.text.toDouble()
                                val rightX = rightXInput.text.toDouble()
                                if (leftX >= rightX) {
                                    throw RuntimeException("левая граница должна быть строго меньше правой")
                                }
                                val chart = Chart(leftX, rightX, canvas.width, canvas.height, {x: Double -> cos(x) })
                                val functionChart = chart.build()
                                val previous = functionChart[0]
                                val gc = canvas.graphicsContext2D
                                gc.clearRect(0.0, 0.0, canvas.width, canvas.height)
                                gc.lineWidth = 1.0
                                gc.fill = Color.BLACK
                                gc.moveTo(previous.x, previous.y)
                                gc.beginPath()

                                for (i in 1 until functionChart.size) {
                                    val current = functionChart[i]
                                    gc.lineTo(current.x, current.y)
                                }
                                gc.stroke()
                                drawXAxis(chart)
                                drawYAxis(chart)
                                gc.closePath()
                            }
                        }
                        button("Clear") {
                            hboxConstraints {
                                margin = Insets(INSET)
                            }
                            action {
                                val gc = canvas.graphicsContext2D
                                gc.clearRect(0.0, 0.0, canvas.width, canvas.height)
                            }
                        }
                    }
                    canvas = canvas {
                        vboxConstraints {
                            margin = Insets(INSET)
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
        gc.moveTo(xAxis.x0, xAxis.y0)
        gc.lineTo(xAxis.x1, xAxis.y1)

        var x = 0.0
        val notchY0 = if (xAxis.y0 + NOTCH_SIZE <= canvas.height) {
            (xAxis.y0 + NOTCH_SIZE)
        } else {
            xAxis.y0
        }
        val notchY1 = if (xAxis.y0 - NOTCH_SIZE >= 0) {
            (xAxis.y0 - NOTCH_SIZE)
        } else {
            xAxis.y0
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
        gc.moveTo(yAxis.x0, yAxis.y0)
        gc.lineTo(yAxis.x1, yAxis.y1)

        var y = 0.0
        val notchX0 = if (yAxis.x0 + NOTCH_SIZE <= canvas.width) {
            yAxis.x0 + NOTCH_SIZE
        } else {
            yAxis.x0
        }
        val notchX1 = if (yAxis.x0 - NOTCH_SIZE >= 0) {
            yAxis.x0 - NOTCH_SIZE
        } else {
            yAxis.x0
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