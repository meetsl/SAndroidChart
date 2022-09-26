package com.meetsl.sandroidchart

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_circle_progress.*

/**
 * @author : ShiLong
 * date: 2020/2/20.
 * desc : default.
 */
class SportProgressActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sport_progress)
        circle_progress.setInfo(
            80,
            "2020/2",
            "北京市",
            "本月供应商到货率得分80分环比上涨6分，同比上涨3分"
        )
    }

    fun start(view: View) {
        circle_progress.smoothToProgress(80)
    }
}