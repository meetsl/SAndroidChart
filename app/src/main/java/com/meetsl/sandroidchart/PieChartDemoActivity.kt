package com.meetsl.sandroidchart

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_pie_chart.*

/**
 * @author : meetsl
 * date: 2020/2/4.
 * desc : default.
 */
class PieChartDemoActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pie_chart)
        pcv_view.addPiePart(Color.BLUE, 1f, "10-20岁")
    }

    fun addParts(view: View) {
        val list = mutableListOf<Triple<Int, Float, String>>()
//        list.add(Triple(Color.YELLOW, 97f, "20-30岁20-30岁20-30岁20-30岁"))
        list.add(Triple(Color.WHITE, 1f, "30-40岁30-40岁30-40岁30-40岁30-40岁30-40岁"))
        list.add(Triple(Color.RED, 1f, "40-50岁40-50岁40-50岁40-50岁40-50岁40-50岁"))
        list.add(Triple(Color.GREEN, 1f, "50-60岁50-60岁50-60岁50-60岁50-60岁50-60岁"))
        pcv_view.setChartInfo(list, radius = 40, roundNum = 2, textColor = "#223344")
    }

    fun clearParts(view: View) {
        pcv_view.clearPieParts()
    }
}