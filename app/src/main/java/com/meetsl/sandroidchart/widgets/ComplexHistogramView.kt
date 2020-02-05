package com.meetsl.sandroidchart.widgets

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import java.util.*
import kotlin.math.roundToInt

/**
 * @author : meetsl
 * date: 2020/2/4.
 * desc : default.
 */
class ComplexHistogramView(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
    View(context, attrs, defStyleAttr) {
    private var horList = mutableListOf<Triple<Int, Int, Int>>()
    var horDataList =
        mutableListOf(
            800, 1200, 1600, 2500, 2000, 1000, 3300, 3800, 4200, 3300, 3800, 4200, 1200,
            1600, 2500, 2000, 1000, 3300, 3800, 4200, 3300, 3800, 4200
        )
    var horPercentDataList = mutableListOf(
        3f, 4f, 8f, 10f, 12f, 18f, 16f, 4f, 8f, 10f, 12f, 18f, 16f, 4f,
        10f, 12f, 18f, 16f, 4f, 8f, 10f, 12f, 18f, 16f, 4f
    )
    var verList = mutableListOf<Int>()
    var verRightList = mutableListOf<Int>()
    var verticalSpace = 80f
    var horizontalSpace = 30f
    var pillarWidth = 40f
    var horizontalMargin = 20f
    var verticalMargin = 40f
    var textVerticalMargin = 5f
    var yUnit = 1000
    var yRightUnit = 5
    var verticalTextSize = 32f
    private val linePaint = Paint()
    private val brokenShaderPaint = Paint()
    private val brokenLinePaint = Paint()
    private val pillarPaint = Paint()
    private val textPaint = Paint()
    private val linePath = Path()
    var linePosList = mutableListOf<Float>()
    var coordinateLines = mutableListOf<Float>()
    private val pillarRectList = mutableListOf<RectF>()
    private val percentPointList = mutableListOf<PointF>()
    private val verTextPosList = mutableListOf<Float>()
    private val verRightTextPosList = mutableListOf<Float>()
    private val horTextPosList = mutableListOf<Float>()
    private val horValueTextPosList = mutableListOf<Float>()
    private val horYearMonthPosList = mutableListOf<STriple<Float, Float, String>>()
    var chartNeedWidth = 0f
    var chartNeedHeight = 0f
    var maxShowPillarNum = 8
    var startShowPillarX = 0f
    var endShowPillarX = 0f
    var isDrawPillarValue = false
    var horFormat = 2 // 1: year; 2: month; 3: day;
    private var year = 2019
    private var month = 8
    private var day = 25
    private lateinit var originalPoint: PointF //原点坐标
    private lateinit var maxShowXPoint: PointF //X轴最大坐标点

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
        for (i in verList.indices) {
            val rightElement = i * yRightUnit
            verRightList.add(0, rightElement)
        }
        val calendar = Calendar.getInstance()
        calendar.set(year, month - 1, day)
        for (i in horDataList.indices) {
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH) + 1
            val day = calendar.get(Calendar.DAY_OF_MONTH)
            val item = Triple(year, month, day)
            when (horFormat) {
                1 -> calendar.add(Calendar.YEAR, 1)
                2 -> calendar.add(Calendar.MONTH, 1)
                3 -> calendar.add(Calendar.DAY_OF_MONTH, 1)
            }
            horList.add(item)
        }
        textPaint.textSize = verticalTextSize
        textPaint.color = Color.BLACK
        textPaint.isAntiAlias = true
        //第一个值最大，对应字符串长度最长
        val maxVerticalTextWidth = textPaint.measureText(verList[0].toString())
        val maxVerticalRightTextWidth = textPaint.measureText("${verRightList[0]}%")
        val chartLeftMargin = horizontalMargin + maxVerticalTextWidth + 10 //10 文字位置右侧距图标的距离
        val chartWidth =
            horDataList.size.coerceAtMost(maxShowPillarNum) * (pillarWidth + 2 * horizontalSpace)
        val chartHeight = (verList.size - 1) * verticalSpace
        startShowPillarX = chartLeftMargin
        endShowPillarX = startShowPillarX + chartWidth
        chartNeedWidth = chartLeftMargin + chartWidth + maxVerticalRightTextWidth + horizontalMargin
        chartNeedHeight =
            verticalMargin * 2 + chartHeight + verticalTextSize * 3 + textVerticalMargin
        originalPoint = PointF(chartLeftMargin, verticalMargin + chartHeight)
        maxShowXPoint = PointF(chartLeftMargin + chartWidth, verticalMargin + chartHeight)
        for (i in 0 until verList.size) {
            val textX =
                horizontalMargin + maxVerticalTextWidth - textPaint.measureText(verList[i].toString())
            val textY = verticalMargin + i * verticalSpace + verticalTextSize / 2
            verTextPosList.add(textX)
            verTextPosList.add(textY)
            val rightTextX = chartLeftMargin + chartWidth + 10
            verRightTextPosList.add(rightTextX)
            verRightTextPosList.add(textY)
        }
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
            val top =
                (chartHeight + verticalMargin) - (horDataList[i] / yUnit.toFloat()) * verticalSpace
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
            val horValue = horList[i]
            val textX =
                left + pillarWidth / 2 - textPaint.measureText(getHorFormatStr(horValue)) / 2
            val textY = verticalMargin + chartHeight + verticalTextSize + textVerticalMargin
            horTextPosList.add(textX)
            horTextPosList.add(textY)
            //横坐标年月标识
            //起始和月份或者日为1的标识年或者年月
            if (horValue.second == 1 || horValue.third == 1) {
                val drawText = getHorYearMonthStr(horValue)
                val item = STriple(textX, textY + 2 * verticalTextSize, drawText)
                horYearMonthPosList.add(item)
            }
            //计算折线点位置
            //计算折线路径
            val pointLeft = left + pillarWidth / 2
            val pointTop =
                (chartHeight + verticalMargin) - (horPercentDataList[i] / yRightUnit.toFloat()) * verticalSpace
            percentPointList.add(PointF(pointLeft, pointTop))
        }
        pillarPaint.color = Color.parseColor("#3877E1")
        pillarPaint.isAntiAlias = true
        brokenShaderPaint.shader = LinearGradient(
            0f, verticalMargin,
            0f, originalPoint.y,
            Color.parseColor("#E84742"), Color.TRANSPARENT, Shader.TileMode.CLAMP
        )
        brokenShaderPaint.isAntiAlias = true
        brokenLinePaint.color = Color.parseColor("#E84742")
        brokenLinePaint.isAntiAlias = true
        brokenLinePaint.strokeWidth = 5f
    }

    private fun getHorYearMonthStr(horValue: Triple<Int, Int, Int>): String {
        return when (horFormat) {
            2 -> "${horValue.first}年"
            3 -> "${horValue.first}年${horValue.second}月"
            else -> "${horValue.first}年"
        }
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
        for (i in pillarRectList.indices) {
            val pillarRect = pillarRectList[i]
            val drawX = pillarRect.left
            if (drawX in startShowPillarX..endShowPillarX)
                canvas.drawRect(pillarRect, pillarPaint)

        }
        //绘制折线图
        linePath.reset()
        linePath.moveTo(originalPoint.x, originalPoint.y)
        var firstFit = true
        var lastShowIndex = 0
        for (i in percentPointList.indices) {
            val percentPoint = percentPointList[i]
            val pointX = percentPoint.x
            if (pointX in startShowPillarX..endShowPillarX) {
                if (i > 0) {
                    val preHidePoint = percentPointList[i - 1]
                    if (firstFit) {
                        val k =
                            (percentPoint.y - preHidePoint.y) / (percentPoint.x - preHidePoint.x)
                        val b = percentPoint.y - k * percentPoint.x
                        val y = k * originalPoint.x + b
                        linePath.lineTo(originalPoint.x, y)
                        canvas.drawLine(
                            originalPoint.x,
                            y,
                            percentPoint.x,
                            percentPoint.y,
                            brokenLinePaint
                        )
                        firstFit = false
                    } else {
                        canvas.drawLine(
                            preHidePoint.x,
                            preHidePoint.y,
                            percentPoint.x,
                            percentPoint.y,
                            brokenLinePaint
                        )
                    }
                }
                lastShowIndex = i
                linePath.lineTo(percentPoint.x, percentPoint.y)
            }
        }
        if (lastShowIndex < percentPointList.size - 1) {
            val percentPoint = percentPointList[lastShowIndex]
            val nextHidePoint = percentPointList[lastShowIndex + 1]
            val k = (nextHidePoint.y - percentPoint.y) / (nextHidePoint.x - percentPoint.x)
            val b = percentPoint.y - k * percentPoint.x
            val y = k * maxShowXPoint.x + b
            linePath.lineTo(maxShowXPoint.x, y)
            canvas.drawLine(percentPoint.x, percentPoint.y, maxShowXPoint.x, y, brokenLinePaint)
        }
        linePath.lineTo(maxShowXPoint.x, maxShowXPoint.y)
        canvas.drawPath(linePath, brokenShaderPaint)
        //绘制Y坐标文字
        for (i in 0 until verList.size) {
            val text = verList[i].toString()
            canvas.drawText(
                text,
                verTextPosList[i + i * 1],
                verTextPosList[i + i * 1 + 1],
                textPaint
            )
            val rightText = "${verRightList[i]}%"
            canvas.drawText(
                rightText,
                verRightTextPosList[i + i * 1],
                verRightTextPosList[i + i * 1 + 1],
                textPaint
            )
        }
        //绘制X坐标文字
        for (i in horList.indices) {
            val drawX = horTextPosList[i + i * 1]
            if (drawX in startShowPillarX..endShowPillarX)
                canvas.drawText(
                    getHorFormatStr(horList[i]),
                    drawX,
                    horTextPosList[i + i * 1 + 1],
                    textPaint
                )
        }
        //绘制横坐标年月标识
        for (i in horYearMonthPosList.indices) {
            val item = horYearMonthPosList[i]
            if (item.first in startShowPillarX..endShowPillarX) {
                canvas.drawText(item.third, item.first, item.second, textPaint)
            }
        }
        //起始始终显示年或者年月
        for (i in horList.indices) {
            val drawX = horTextPosList[i + i * 1]
            val item = horList[i]
            if (drawX in startShowPillarX..endShowPillarX) {
                canvas.drawText(
                    getHorYearMonthStr(item),
                    drawX,
                    horTextPosList[i + i * 1 + 1] + 2 * verticalTextSize,
                    textPaint
                )
                break
            }
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
                var moveX = (endX - startX) / resources.displayMetrics.density
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
                        val percentPoint = percentPointList[i]
                        percentPoint.x += moveX
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
                    //更新X轴坐标年月标识文字位置
                    horYearMonthPosList.forEach {
                        it.first += moveX
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

    private fun getHorFormatStr(value: Triple<Int, Int, Int>): String {
        return when (horFormat) {
            1 -> "${value.first}".substring(2) + "年"
            2 -> "${value.second}" + "月"
            3 -> "${value.third}" + "日"
            else -> "${value.second}" + "月"
        }
    }
}

data class STriple<A, B, C>(
    var first: A,
    var second: B,
    var third: C
)

