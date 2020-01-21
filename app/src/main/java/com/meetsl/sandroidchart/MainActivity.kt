package com.meetsl.sandroidchart

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        pcv_view.addPiePart(Color.BLUE, 1f, "10-20岁")
    }

    fun addParts(view: View) {
        val list = mutableListOf<Triple<Int, Float, String>>()
        list.add(Triple(Color.YELLOW, 1f, "20-30岁"))
        list.add(Triple(Color.WHITE, 1f, "30-40岁"))
        list.add(Triple(Color.RED, 1f, "40-50岁"))
        pcv_view.addPieParts(list)
    }

    fun clearParts(view: View) {
        pcv_view.clearPieParts()
    }

}
