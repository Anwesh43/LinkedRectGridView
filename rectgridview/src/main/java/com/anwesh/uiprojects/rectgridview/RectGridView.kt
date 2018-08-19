package com.anwesh.uiprojects.rectgridview

/**
 * Created by anweshmishra on 19/08/18.
 */

import android.app.Activity
import android.view.View
import android.view.MotionEvent
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF

val nodes : Int = 5

fun Canvas.drawRectGrid(x : Float, y : Float, size : Float, sc : Float, paint : Paint) {
    val gap : Float = size / (nodes)
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
    val gap : Float = w / (nodes + 1)
    val sc1 : Float = Math.min(0.5f, scale) * 2
    val sc2 : Float = Math.min(0.5f, Math.max(0f, scale - 0.5f)) * 2
    val size : Float = 3 * gap / 4
    val hSize : Float = size / nodes
    val y : Float = -size/2 + i * hSize + hSize / 2
    save()
    translate(gap * i + gap / 2 + gap * sc1, h / 2)
    paint.color = Color.parseColor("#3F51B5")
    drawRectGrid(0f, y, size, sc2, paint)
    for (j in ((i + 1) .. (nodes - 1))) {
        drawRectGrid(0f, -size / 2 + j * hSize + hSize / 2, size, 0f, paint)
    }
    restore()
}

class RectGridView(ctx : Context) : View(ctx) {

    private val paint : Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val renderer : Renderer = Renderer(this)

    var onAnimationListener : OnAnimationListener? = null

    override fun onDraw(canvas : Canvas) {
        renderer.render(canvas, paint)
    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                renderer.handleTap()
            }
        }
        return true
    }

    fun addOnAnimationListener(onComplete : (Int) -> Unit, onReset : (Int) -> Unit) {
        onAnimationListener = OnAnimationListener(onComplete, onReset)
    }

    data class State(var scale : Float = 0f, var prevScale : Float = 0f, var dir : Float = 0f) {

        fun update(cb : (Float) -> Unit) {
            scale += 0.05f * this.dir
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

    class RGNode(var i : Int, val state : State = State()) {

        private var prev : RGNode? = null
        private var next : RGNode? = null

        fun addNeighbor() {
            if (i < nodes - 1) {
                next = RGNode(i + 1)
                next?.prev = this
            }
        }

        init {
            addNeighbor()
        }

        fun update(cb : (Int, Float) -> Unit) {
            state.update {
                cb(i, it)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            state.startUpdating(cb)
        }

        fun getNext(dir : Int, cb : () -> Unit) : RGNode {
            var curr : RGNode? = prev
            if (dir == 1) {
                curr = next
            }
            if (curr != null) {
                return curr
            }
            cb()
            return this
        }

        fun draw(canvas : Canvas, paint : Paint) {
            canvas.drawRGNode(i, state.scale, paint)
        }
    }

    data class LinkedRG(var i : Int) {
        private var curr : RGNode = RGNode(0)
        private var dir : Int = 1

        fun draw(canvas : Canvas, paint : Paint) {
            curr.draw(canvas, paint)
        }

        fun update(cb : (Int, Float) -> Unit) {
            curr.update {i, scl ->
                curr = curr.getNext(dir) {
                    dir *= -1
                }
                cb(i, scl)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            curr.startUpdating(cb)
        }
    }

    data class Renderer(var view : RectGridView) {

        private val animator : Animator = Animator(view)
        private val linkedRG : LinkedRG = LinkedRG(0)

        fun render(canvas : Canvas, paint : Paint) {
            canvas.drawColor(Color.parseColor("#BDBDBD"))
            linkedRG.draw(canvas, paint)
            animator.animate {
                linkedRG.update {i, scl ->
                    animator.stop()
                    when (scl) {
                        0f -> view.onAnimationListener?.onReset?.invoke(i)
                        1f -> view.onAnimationListener?.onComplete?.invoke(i)
                    }
                }
            }
        }

        fun handleTap() {
            linkedRG.startUpdating {
                animator.start()
            }
        }
    }

    companion object {
        fun create(activity : Activity) : RectGridView {
            val view : RectGridView = RectGridView(activity)
            activity.setContentView(view)
            return view
        }
    }

    data class OnAnimationListener(var onComplete : (Int) -> Unit, var onReset : (Int) -> Unit)
}