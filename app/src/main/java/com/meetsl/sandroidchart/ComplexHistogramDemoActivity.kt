package com.meetsl.sandroidchart

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.meetsl.sandroidchart.widgets.ChartInfo
import kotlinx.android.synthetic.main.activity_complex_histogram.*

/**
 * @author : meetsl
 * date: 2020/2/4.
 * desc : default.
 */
class ComplexHistogramDemoActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_complex_histogram)
        val chartInfo = ChartInfo(
            "2020-02-12",
            ChartInfo.VerticalBean(
                "到货率评价",
                mutableListOf(15f, 25f, 32f, 48f, 35f, 22f, 45f, 71f),
                10
            ),
            ChartInfo.VerticalBean(
                "总金额（万元）",
                mutableListOf(375f, 625f, 775f, 1200f, 1250f, 2525f, 2500f, 325f),
                500
            )
        )
        chartInfo.verticalSpace = 60f
        chv_view.setChartInfo(chartInfo)
    }
}