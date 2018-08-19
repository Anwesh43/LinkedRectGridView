package com.anwesh.uiprojects.linkedrectgridview

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import com.anwesh.uiprojects.rectgridview.RectGridView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val view : RectGridView = RectGridView.create(this)
        fullScreen()
        view.addOnAnimationListener({createToast("${it + 1} animation completed")}, {createToast("${it + 1} animation is reset")})
    }

    private fun createToast(msg : String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}

fun MainActivity.fullScreen() {
    supportActionBar?.hide()
    window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
}