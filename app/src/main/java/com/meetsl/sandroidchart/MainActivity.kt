package com.meetsl.sandroidchart

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun pieChart(view: View) {
        startActivity(Intent(this, PieChartDemoActivity::class.java))
    }

    fun histogram(view: View) {
        startActivity(Intent(this, HistogramDemoActivity::class.java))
    }

    fun complexHistogram(view: View) {
        startActivity(Intent(this, ComplexHistogramDemoActivity::class.java))
    }

    fun circleProgress(view: View) {
        startActivity(Intent(this, CircleProgressActivity::class.java))
    }

}
