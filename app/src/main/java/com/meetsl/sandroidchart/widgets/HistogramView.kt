package com.meetsl.sandroidchart.widgets

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.roundToInt

/**
 * @author : ShiLong
 * date: 2020/2/4.
 * desc : default.
 */
class HistogramView(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
    View(context, attrs, defStyleAttr) {
    var horList =
        mutableListOf("8月", "9月", "10月", "11月", "12月", "1月", "2月", "3月", "4月", "5月", "6月", "7月")
    var horDataList =
        mutableListOf(800, 1200, 1600, 2500, 2000, 1000, 3300, 3800, 4200, 3300, 3800, 4200)
    var verList = mutableListOf<Float>()
    var verticalSpace = 80f
    var horizontalSpace = 30f
    var pillarWidth = 40f
    var horizontalMargin = 20f
    var verticalMargin = 40f
    var textVerticalMargin = 5f
    var yUnit = 1000f
    var verticalTextSize = 32f
    val linePaint = Paint()
    val pillarPaint = Paint()
    val textPaint = Paint()
    var linePosList = mutableListOf<Float>()
    var coordinateLines = mutableListOf<Float>()
    val pillarRectList = mutableListOf<RectF>()
    val verTextPosList = mutableListOf<Float>()
    val horTextPosList = mutableListOf<Float>()
    val horValueTextPosList = mutableListOf<Float>()
    var chartNeedWidth = 0f
    var chartNeedHeight = 0f
    var maxShowPillarNum = 8
    var startShowPillarX = 0f
    var endShowPillarX = 0f
    var isDrawPillarValue = true

    init {
        val sorted = horDataList.sorted()
        var count = 0
        while (true) {
            val element = count * yUnit
            verList.add(0, element)
            if (element >= sorted.last()) {
                break
            }
            count++
        }
        textPaint.textSize = verticalTextSize
        textPaint.color = Color.BLACK
        textPaint.isAntiAlias = true
        //第一个值最大，对应字符串长度最长
        val maxVerticalTextWidth = textPaint.measureText(verList[0].toString())
        for (i in 0 until verList.size) {
            val textX =
                horizontalMargin + maxVerticalTextWidth - textPaint.measureText(verList[i].toString())
            val textY = verticalMargin + i * verticalSpace + verticalTextSize / 2
            verTextPosList.add(textX)
            verTextPosList.add(textY)
        }
        val chartLeftMargin = horizontalMargin + maxVerticalTextWidth + 10 //10 文字位置右侧距图标的距离
        startShowPillarX = chartLeftMargin
        val chartWidth =
            horList.size.coerceAtMost(maxShowPillarNum) * (pillarWidth + 2 * horizontalSpace)
        endShowPillarX = startShowPillarX + chartWidth
        val chartHeight = (verList.size - 1) * verticalSpace
        chartNeedWidth = chartLeftMargin + chartWidth + horizontalMargin
        chartNeedHeight = verticalMargin * 2 + chartHeight + verticalTextSize + textVerticalMargin
        //计算图标线段位置
        for (i in 0 until verList.size - 1) {
            val posY = i * verticalSpace + verticalMargin
            val endX = chartLeftMargin + chartWidth
            linePosList.add(chartLeftMargin)
            linePosList.add(posY)
            linePosList.add(endX)
            linePosList.add(posY)
        }
        //Y轴
        coordinateLines.add(chartLeftMargin)
        coordinateLines.add(verticalMargin)
        coordinateLines.add(chartLeftMargin)
        coordinateLines.add(chartHeight + verticalMargin)
        //X轴
        coordinateLines.add(chartLeftMargin)
        coordinateLines.add(chartHeight + verticalMargin)
        coordinateLines.add(chartLeftMargin + chartWidth)
        coordinateLines.add(chartHeight + verticalMargin)
        linePaint.isAntiAlias = true
        //计算柱状位置和x轴文字文字
        for (i in 0 until horDataList.size) {
            val left = i * (2 * horizontalSpace + pillarWidth) + horizontalSpace + chartLeftMargin
            val top = (chartHeight + verticalMargin) - (horDataList[i] / yUnit) * verticalSpace
            val right = left + pillarWidth
            val bottom = verticalMargin + chartHeight - 1 //-1不压抽线
            val rectF = RectF(left, top, right, bottom)
            pillarRectList.add(rectF)
            //计算值的文字位置
            val valueTextX = left + pillarWidth / 2 - textPaint.measureText("${horDataList[i]}") / 2
            val valueTextY = top - textVerticalMargin
            horValueTextPosList.add(valueTextX)
            horValueTextPosList.add(valueTextY)
            //计算 x 轴文字坐标
            val textX = left + pillarWidth / 2 - textPaint.measureText(horList[i]) / 2
            val textY = verticalMargin + chartHeight + verticalTextSize + textVerticalMargin
            horTextPosList.add(textX)
            horTextPosList.add(textY)
        }
        pillarPaint.color = Color.parseColor("#3877E1")
        //计算折线路径

    }

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context) : this(context, null)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val mode = MeasureSpec.getMode(heightMeasureSpec)
        if (mode == MeasureSpec.AT_MOST || mode == MeasureSpec.UNSPECIFIED) {
            setMeasuredDimension(widthMeasureSpec, chartNeedHeight.roundToInt())
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val dx = (width - chartNeedWidth) / 2
        if (dx > horizontalMargin) {
            canvas.translate(dx, 0f)
        }
        //绘制坐标内分隔线
        linePaint.color = Color.RED
        canvas.drawLines(linePosList.toFloatArray(), linePaint)
        //绘制坐标线
        linePaint.color = Color.BLACK
        canvas.drawLines(coordinateLines.toFloatArray(), linePaint)
        //绘制柱图
        pillarRectList.forEach {
            val drawX = it.left
            if (drawX in startShowPillarX..endShowPillarX)
                canvas.drawRect(it, pillarPaint)
        }
        //绘制Y坐标文字
        for (i in 0 until verList.size) {
            val text = verList[i].toString()
            canvas.drawText(
                text,
                verTextPosList[i + i * 1],
                verTextPosList[i + i * 1 + 1],
                textPaint
            )
        }
        //绘制X坐标文字
        for (i in 0 until horList.size) {
            val drawX = horTextPosList[i + i * 1]
            if (drawX in startShowPillarX..endShowPillarX)
                canvas.drawText(
                    horList[i],
                    drawX,
                    horTextPosList[i + i * 1 + 1],
                    textPaint
                )
        }
        if (isDrawPillarValue) {
            //绘制x坐标 value文字
            for (i in 0 until horDataList.size) {
                val drawX = horValueTextPosList[i + i * 1]
                if (drawX in startShowPillarX..endShowPillarX)
                    canvas.drawText(
                        "${horDataList[i]}",
                        drawX,
                        horValueTextPosList[i + i * 1 + 1],
                        textPaint
                    )
            }
        }
    }

    var startX = 0f
    var endX = 0f
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                startX = event.x
                endX = event.x
            }
            MotionEvent.ACTION_MOVE -> {
                endX = event.x
                var moveX = endX - startX
                if (maxShowPillarNum < pillarRectList.size) { //没有全部显示
                    //左移、边界判断
                    if (moveX > 0) {
                        val first = pillarRectList.first()
                        val newX = first.left + moveX
                        val pillarLeftEdgeX = startShowPillarX + horizontalSpace
                        if (newX > pillarLeftEdgeX) {
                            moveX = pillarLeftEdgeX - first.left
                        }
                    }
                    //右移、边界判断
                    if (moveX < 0) {
                        val last = pillarRectList.last()
                        val newX = last.right + moveX
                        val pillarRightEdgeX = endShowPillarX - horizontalSpace
                        if (newX < pillarRightEdgeX) {
                            moveX = pillarRightEdgeX - last.right
                        }
                    }
                    //更新柱状图位置
                    for (i in pillarRectList.indices) {
                        val pillarRect = pillarRectList[i]
                        pillarRect.left += moveX
                        pillarRect.right += moveX
                    }
                    //更新X轴文字位置
                    for (i in horTextPosList.indices) {
                        if (i % 2 == 0) {
                            horTextPosList[i] += moveX
                        }
                    }
                    //更新X轴柱状图值文字位置
                    for (i in horValueTextPosList.indices) {
                        if (i % 2 == 0) {
                            horValueTextPosList[i] += moveX
                        }
                    }
                    postInvalidate()
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                endX = event.x
                val stopX = endX - startX
            }
        }
        return true
    }
}