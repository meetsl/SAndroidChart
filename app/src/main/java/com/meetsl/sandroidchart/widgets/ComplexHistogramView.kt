package com.meetsl.sandroidchart.widgets

import android.annotation.SuppressLint
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
    private var descList = mutableListOf("金额", "占比")
    var horDataList =
        mutableListOf(
            800, 1200, 1600, 2500, 5000, 1000, 3300, 3800, 4200, 3300, 3800, 4200, 1200,
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
    var descMargin = 40f
    var textVerticalMargin = 5f
    var yUnit = 1000
    var yRightUnit = 5
    var verticalTextSize = 32f
    var descTextSize = 38f
    var descCircleRadius = 12f
    private val windowPadding = 20f
    private val windowMargin = 20f
    private var windowHeight = 0f
    private val linePaint = Paint()
    private val brokenShaderPaint = Paint()
    private val brokenLinePaint = Paint()
    private val pillarPaint = Paint()
    private val textPaint = Paint()
    private val descPaint = Paint()
    private val shadowPath = Path()
    private val linePath = Path()
    private val arrowPath = Path()
    private val arrowSize = 20f
    private val windowShadowPaint = Paint()
    private val windowPaint = Paint()
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
    private var originalPoint: PointF //原点坐标
    private var maxShowXPoint: PointF //X轴最大坐标点
    private var chartRectF = RectF()
    private var windowInfo: SWindowInfo? = null
    val pillarColor = Color.parseColor("#3877E1")
    val percentColor = Color.parseColor("#E84742")

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
        windowHeight = verticalTextSize * 2 + textVerticalMargin + 2 * windowPadding
        val chartTopMargin =
            (verticalMargin + descTextSize + descMargin).coerceAtLeast(windowHeight + 2 * windowMargin)
        val chartWidth =
            horDataList.size.coerceAtMost(maxShowPillarNum) * (pillarWidth + 2 * horizontalSpace)
        val chartHeight = (verList.size - 1) * verticalSpace
        startShowPillarX = chartLeftMargin
        endShowPillarX = startShowPillarX + chartWidth
        chartNeedWidth = chartLeftMargin + chartWidth + maxVerticalRightTextWidth + horizontalMargin
        chartNeedHeight =
            chartTopMargin + verticalMargin + chartHeight + verticalTextSize * 3 + textVerticalMargin
        originalPoint = PointF(chartLeftMargin, chartTopMargin + chartHeight)
        maxShowXPoint = PointF(chartLeftMargin + chartWidth, chartTopMargin + chartHeight)
        chartRectF.left = chartLeftMargin
        chartRectF.top = chartTopMargin
        chartRectF.right = maxShowXPoint.x
        chartRectF.bottom = maxShowXPoint.y
        for (i in 0 until verList.size) {
            val textX =
                horizontalMargin + maxVerticalTextWidth - textPaint.measureText(verList[i].toString())
            val textY = chartTopMargin + i * verticalSpace + verticalTextSize / 2
            verTextPosList.add(textX)
            verTextPosList.add(textY)
            val rightTextX = chartLeftMargin + chartWidth + 10
            verRightTextPosList.add(rightTextX)
            verRightTextPosList.add(textY)
        }
        //计算图标线段位置
        for (i in 0 until verList.size - 1) {
            val posY = i * verticalSpace + chartTopMargin
            val endX = chartLeftMargin + chartWidth
            linePosList.add(chartLeftMargin)
            linePosList.add(posY)
            linePosList.add(endX)
            linePosList.add(posY)
        }
        //Y轴
        coordinateLines.add(chartLeftMargin)
        coordinateLines.add(chartTopMargin)
        coordinateLines.add(chartLeftMargin)
        coordinateLines.add(chartHeight + chartTopMargin)
        //X轴
        coordinateLines.add(chartLeftMargin)
        coordinateLines.add(chartHeight + chartTopMargin)
        coordinateLines.add(chartLeftMargin + chartWidth)
        coordinateLines.add(chartHeight + chartTopMargin)
        linePaint.isAntiAlias = true
        //计算柱状位置和x轴文字文字
        for (i in 0 until horDataList.size) {
            val left = i * (2 * horizontalSpace + pillarWidth) + horizontalSpace + chartLeftMargin
            val top =
                (chartHeight + chartTopMargin) - (horDataList[i] / yUnit.toFloat()) * verticalSpace
            val right = left + pillarWidth
            val bottom = chartTopMargin + chartHeight - 1 //-1不压抽线
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
            val textY = chartTopMargin + chartHeight + verticalTextSize + textVerticalMargin
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
                (chartHeight + chartTopMargin) - (horPercentDataList[i] / yRightUnit.toFloat()) * verticalSpace
            percentPointList.add(PointF(pointLeft, pointTop))
        }
        pillarPaint.color = pillarColor
        pillarPaint.isAntiAlias = true
        brokenShaderPaint.shader = LinearGradient(
            0f, chartTopMargin,
            0f, originalPoint.y,
            percentColor, Color.TRANSPARENT, Shader.TileMode.CLAMP
        )
        brokenShaderPaint.isAntiAlias = true
        brokenLinePaint.color = percentColor
        brokenLinePaint.isAntiAlias = true
        brokenLinePaint.style = Paint.Style.STROKE
        brokenLinePaint.strokeWidth = 5f
        descPaint.isAntiAlias = true
        descPaint.textSize = descTextSize
        windowPaint.isAntiAlias = true
        windowPaint.color = pillarColor
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
        if (dx > 0f) {
            canvas.translate(dx, 0f)
        }
        //绘制图标描述
        descPaint.color = pillarColor
        val circleY = descTextSize / 3
        canvas.drawCircle(
            chartRectF.left + circleY,
            chartRectF.top - descMargin - circleY,
            descCircleRadius,
            descPaint
        )
        canvas.drawText(
            descList[0],
            chartRectF.left + 2 * circleY + textVerticalMargin,
            chartRectF.top - descMargin,
            descPaint
        )
        descPaint.color = percentColor
        val textWidth = descPaint.measureText(descList[0])
        canvas.drawCircle(
            chartRectF.left + 2 * circleY + textVerticalMargin + textWidth + horizontalSpace + circleY,
            chartRectF.top - descMargin - circleY,
            descCircleRadius,
            descPaint
        )
        canvas.drawText(
            descList[1],
            chartRectF.left + 2 * circleY + textVerticalMargin + textWidth + horizontalSpace + 2 * circleY + textVerticalMargin,
            chartRectF.top - descMargin,
            descPaint
        )
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
        shadowPath.reset()
        linePath.reset()
        shadowPath.moveTo(originalPoint.x, originalPoint.y)
        linePath.moveTo(originalPoint.x, originalPoint.y)
        var firstFit = true
        var lastShowIndex = 0
        for (i in percentPointList.indices) {
            val percentPoint = percentPointList[i]
            val pointX = percentPoint.x
            if (pointX in startShowPillarX..endShowPillarX) {
                if (i > 0) {
                    if (firstFit) {
                        val preHidePoint = percentPointList[i - 1]
                        val k =
                            (percentPoint.y - preHidePoint.y) / (percentPoint.x - preHidePoint.x)
                        val b = percentPoint.y - k * percentPoint.x
                        val y = k * originalPoint.x + b
                        shadowPath.lineTo(originalPoint.x, y)
                        shadowPath.lineTo(percentPoint.x, percentPoint.y)
                        linePath.moveTo(originalPoint.x, y)
                        linePath.lineTo(percentPoint.x, percentPoint.y)
                        firstFit = false
                    } else {
                        val preIndex = if (i - 2 < 0) 0 else i - 2
                        val nextIndex =
                            if (i + 1 > percentPointList.size - 1) percentPointList.size - 1 else i + 1
                        val controlAX =
                            percentPointList[i - 1].x + (percentPoint.x - percentPointList[preIndex].x) / 4
                        val controlAY =
                            percentPointList[i - 1].y + (percentPoint.y - percentPointList[preIndex].y) / 4
                        val controlBX =
                            percentPoint.x - (percentPointList[nextIndex].x - percentPointList[i - 1].x) / 4
                        val controlBY =
                            percentPoint.y - (percentPointList[nextIndex].y - percentPointList[i - 1].y) / 4
                        shadowPath.cubicTo(
                            controlAX,
                            controlAY,
                            controlBX,
                            controlBY,
                            percentPoint.x,
                            percentPoint.y
                        )
                        linePath.cubicTo(
                            controlAX,
                            controlAY,
                            controlBX,
                            controlBY,
                            percentPoint.x,
                            percentPoint.y
                        )
                    }
                } else {
                    shadowPath.lineTo(percentPoint.x, percentPoint.y)
                    linePath.lineTo(percentPoint.x, percentPoint.y)
                    if (firstFit)
                        firstFit = false
                }
                lastShowIndex = i
            }
        }
        if (lastShowIndex < percentPointList.size - 1) {
            val percentPoint = percentPointList[lastShowIndex]
            val nextHidePoint = percentPointList[lastShowIndex + 1]
            val k = (nextHidePoint.y - percentPoint.y) / (nextHidePoint.x - percentPoint.x)
            val b = percentPoint.y - k * percentPoint.x
            val y = k * maxShowXPoint.x + b
            shadowPath.lineTo(maxShowXPoint.x, y)
            linePath.lineTo(maxShowXPoint.x, y)
            shadowPath.lineTo(maxShowXPoint.x, maxShowXPoint.y)
        } else {
            shadowPath.lineTo(percentPointList[lastShowIndex].x, maxShowXPoint.y)
        }
        canvas.drawPath(shadowPath, brokenShaderPaint)
        canvas.drawPath(linePath, brokenLinePaint)
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
        //显示Desc Window
        if (windowInfo != null) {
            val shadowSize = 2f
            val windowRectF = windowInfo!!.windowRectF
            canvas.drawRoundRect(windowRectF, 5f, 5f, windowShadowPaint)
            windowRectF.inset(shadowSize, shadowSize)
            canvas.drawRoundRect(windowRectF, 5f, 5f, windowPaint)
            arrowPath.reset()
            val arrowPointF = windowInfo!!.arrowPointF
            arrowPath.moveTo(arrowPointF.x, arrowPointF.y)
            arrowPath.lineTo(arrowPointF.x - arrowSize / 2, windowRectF.bottom)
            arrowPath.lineTo(arrowPointF.x + arrowSize / 2, windowRectF.bottom)
            arrowPath.reset()
            arrowPath.moveTo(arrowPointF.x, arrowPointF.y - shadowSize)
            arrowPath.lineTo(arrowPointF.x - (arrowSize - shadowSize) / 2, windowRectF.bottom)
            arrowPath.lineTo(arrowPointF.x + (arrowSize - shadowSize) / 2, windowRectF.bottom)
            canvas.drawPath(arrowPath, windowPaint)
            canvas.drawText(
                windowInfo!!.timeText,
                windowRectF.left + windowPadding,
                windowRectF.top + windowPadding + verticalTextSize * 2 / 3,
                textPaint
            )
            canvas.drawText(
                windowInfo!!.valueText,
                windowRectF.left + windowPadding,
                windowRectF.top + windowPadding + verticalTextSize * 2 / 3 + textVerticalMargin + verticalTextSize,
                textPaint
            )
        }
    }

    var startX = 0f
    var endX = 0f
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                windowInfo = null
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
                //画布移动后，点击位置修正
                val dx = (width - chartNeedWidth) / 2
                var clickCharX = event.x
                if (dx > 0f) {
                    clickCharX -= dx
                }
                if (stopX >= -0.5f && stopX <= 0.5f && chartRectF.contains(clickCharX, event.y)) {
                    createDataWindow(clickCharX)
                }
                postInvalidate()
            }
        }
        return true
    }

    private fun createDataWindow(targetX: Float) {
        //计算点击位置所在柱状图
        for (i in pillarRectList.indices) {
            val pillarRect = pillarRectList[i]
            val matchLeft = pillarRect.left - horizontalSpace
            val matchRight = pillarRect.right + horizontalSpace
            if (targetX in matchLeft..matchRight) {
                val timeText = getHorFormatStr(horList[i])
                val valueText =
                    "${descList[0]}:${horDataList[i]} ${descList[1]}:${horPercentDataList[i]}%"
                val arrowPositionX = pillarRect.centerX()
                val arrowPositionY = pillarRect.top.coerceAtMost(percentPointList[i].y)
                val textWidth = textPaint.measureText(valueText)
                val windowWidth = textWidth + 2 * windowPadding
                val overRightX = chartNeedWidth - arrowPositionX - windowWidth / 2 //右边空间是否能放下window
                val translateLeftX = if (overRightX < 0) overRightX else 0f //向左平移距离
                val overLeftX = arrowPositionX - horizontalMargin - windowWidth / 2
                val translateRightX = if (overLeftX < 0) -overLeftX else 0f //向右平移距离
                val windowLeft = arrowPositionX - windowWidth / 2 + translateLeftX + translateRightX
                val windowBottom = arrowPositionY - windowMargin
                val windowRectF = RectF(
                    windowLeft,
                    windowBottom - windowHeight,
                    windowLeft + windowWidth,
                    windowBottom
                )
                val arrowPointF = PointF(arrowPositionX, arrowPositionY)
                if (windowInfo == null) {
                    windowInfo = SWindowInfo(timeText, valueText, windowRectF, arrowPointF)
                } else {
                    windowInfo?.apply {
                        this.timeText = timeText
                        this.valueText = valueText
                        this.windowRectF = windowRectF
                        this.arrowPointF = arrowPointF
                    }
                }
                val radialGradient = RadialGradient(
                    windowRectF.centerX(), windowRectF.centerY(), windowRectF.width(),
                    intArrayOf(pillarColor, Color.TRANSPARENT),
                    floatArrayOf(0f, 0.8f),
                    Shader.TileMode.CLAMP
                )
                windowShadowPaint.shader = radialGradient
                break
            }
        }
    }

    private fun getHorFormatStr(value: Triple<Int, Int, Int>): String {
        return when (horFormat) {
            1 -> "${value.first}".substring(2) + "年"
            2 -> "${value.second}" + "月"
            3 -> "${value.third}" + "日"
            else -> "${value.second}" + "月"
        }
    }

    private fun getHorYearMonthStr(horValue: Triple<Int, Int, Int>): String {
        return when (horFormat) {
            2 -> "${horValue.first}年"
            3 -> "${horValue.first}年${horValue.second}月"
            else -> "${horValue.first}年"
        }
    }
}

data class STriple<A, B, C>(
    var first: A,
    var second: B,
    var third: C
)

data class SWindowInfo(
    var timeText: String,
    var valueText: String,
    var windowRectF: RectF,
    var arrowPointF: PointF,
    var isShowing: Boolean = false
)

