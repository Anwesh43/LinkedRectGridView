package com.anwesh.uiprojects.rectgridview

/**
 * Created by anweshmishra on 19/08/18.
 */

import android.view.View
import android.view.MotionEvent
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF

val nodes : Int = 5

fun Canvas.drawRectGrid(x : Float, y : Float, size : Float, sc : Float, paint : Paint) {
    val gap : Float = size / nodes
    var xStart : Float = x - size/2
    val sizeSc : Float = (gap/3) * (1 - sc)
    save()
    for (i in 0..nodes - 1) {
        save()
        translate(xStart, y)
        drawRect(RectF(-sizeSc, -sizeSc, sizeSc, sizeSc), paint)
        xStart += gap
        restore()
    }
    restore()
}

fun Canvas.drawRGNode(i : Int, scale : Float, paint : Paint) {
    val w : Float = width.toFloat()
    val h : Float = height.toFloat()
    val gap : Float = w / nodes
    val sc1 : Float = Math.min(0.5f, scale) * 2
    val sc2 : Float = Math.min(0.5f, Math.max(0f, scale - 0.5f)) * 2
    val size : Float = 3 * gap / 4
    val hSize : Float = size / nodes
    val y : Float = -size/2 + i * hSize
    save()
    translate(gap * i + gap / 2 + gap * sc1, h / 2)
    paint.color = Color.parseColor("#3F51B5")
    drawRectGrid(0f, y, size, sc2, paint)
    for (j in ((i + 1) .. (nodes - 1))) {
        drawRectGrid(0f, y + j * hSize, size, 0f, paint)
    }
    restore()
}

class RectGridView(ctx : Context) : View(ctx) {

    private val paint : Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    override fun onDraw(canvas : Canvas) {

    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {

            }
        }
        return true
    }

    data class State(var scale : Float = 0f, var prevScale : Float = 0f, var dir : Float = 0f) {

        fun update(cb : (Float) -> Unit) {
            scale += 0.1f * this.dir
            if (Math.abs(this.scale - this.prevScale) > 1) {
                this.scale = this.prevScale + this.dir
                this.dir = 0f
                this.prevScale = this.scale
                cb(prevScale)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            if (dir == 0f) {
                dir = 1 - 2 * prevScale
                cb()
            }
        }
    }

    data class Animator(var view : View, var animated : Boolean = false) {

        fun animate(cb : () -> Unit) {
            if (animated) {
                cb()
                try {
                    Thread.sleep(50)
                    view.invalidate()
                } catch(Ex : Exception) {

                }
            }
        }

        fun start() {
            if (!animated) {
                animated = true
                view.postInvalidate()
            }
        }

        fun stop() {
            if (animated) {
                animated = false
            }
        }
    }
}