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
            left = ChartInfo.VerticalBean(
                "到货率评价",
                mutableListOf(15.001f, 25f, 32f, 48f, 35f, 22f, 45f, 71f, 22f, 45f, 71f, 22f),
                10
            ),
            //如果设置了 right，showType可以不设置并且设置为1、2将无效
            right = ChartInfo.VerticalBean(
                "总金额（万元）",
                mutableListOf(
                    375f,
                    625f,
                    0f,
                    1200f,
                    1250f,
                    0f,
                    2500f,
                    5f,
                    300f,
                    1500f,
                    325f,
                    1200f
                ),
                500
            )
        )
        chv_view.setChartInfo(chartInfo)


        val chartInfo1 = ChartInfo(
            "2020-02-12",
            ChartInfo.VerticalBean(
                "到货率评价",
                mutableListOf(15.001f, 25f, 32f, 48f, 35f, 22f),
                10
            )
        )
        chartInfo1.isDrawPillarValue = true
        chartInfo1.maxShowPillarNum = 6
        chartInfo1.pillarWidth = 15f
        chartInfo1.horizontalSpace = 15f
//        chartInfo1.showType = 1 //默认是柱状图
        chv_view1.setChartInfo(chartInfo1)

        val chartInfo2 = ChartInfo(
            "2020-02-12",
            left = ChartInfo.VerticalBean(
                "到货率评价",
                mutableListOf(15.001f, 25f, 32f, 48f, 35f, 22f, 45f, 71f, 22f, 45f, 71f, 22f),
                10
            )
        )
        chartInfo2.showType = 2
        chv_view2.setChartInfo(chartInfo2)
    }
}