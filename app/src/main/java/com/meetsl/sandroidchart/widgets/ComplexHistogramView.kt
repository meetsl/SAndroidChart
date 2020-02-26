package com.meetsl.sandroidchart.widgets

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import java.lang.IllegalArgumentException
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * @author : meetsl
 * date: 2020/2/4.
 * desc : default.
 */
class ComplexHistogramView(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
    View(context, attrs, defStyleAttr) {
    private val horList = mutableListOf<Triple<Int, Int, Int>>()
    private var descList = mutableListOf("金额", "占比")
    private var leftDataList: List<Float> =
        mutableListOf(
            0f, 0f, 0f, 0f, 0f,
            0f, 0f, 0f, 0f, 0f,
            0f, 0f, 0f, 0f, 0f,
            0f, 0f, 0f, 0f, 0f,
            0f, 0f, 0f
        )
    private var rightDataList: List<Float> = mutableListOf(
        0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f,
        0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f
    )
    private val verList = mutableListOf<Int>()
    private val verRightList = mutableListOf<Int>()
    private var verticalSpace = 80f
    private var horizontalSpace = 30f
    private var pillarWidth = 40f
    private var horizontalMargin = 20f
    private var verticalMargin = 40f
    private var descMargin = 60f
    private var textVerticalMargin = 5f
    private var yUnit = 5
    private var yRightUnit = 5
    private var verticalTextSize = 32f
    private var descTextSize = 38f
    private var descCircleRadius = 12f
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
    private val linePosList = mutableListOf<Float>()
    private val coordinateLines = mutableListOf<Float>()
    private val pillarRectList = mutableListOf<RectF>()
    private val percentPointList = mutableListOf<PointF>()
    private val verTextPosList = mutableListOf<Float>()
    private val verRightTextPosList = mutableListOf<Float>()
    private val horTextPosList = mutableListOf<Float>()
    private val horValueTextPosList = mutableListOf<Float>()
    private val horYearMonthPosList = mutableListOf<STriple<Float, Float, String>>()
    private var chartNeedWidth = 0f
    private var chartNeedHeight = 0f
    private var maxShowPillarNum = 12
    private var startShowPillarX = 0f
    private var endShowPillarX = 0f
    private var isDrawPillarValue = false
    private var horFormat = 2 // 1: year; 2: month; 3: day;
    private var leftFormat = 1 // 1 数字 2 百分比
    private var rightFormat = 1 // 1 数字 2 百分比
    private var year = 2019
    private var month = 8
    private var day = 25
    private var chartRectF = RectF() //坐标矩形位置
    private var windowInfo: SWindowInfo? = null
    private var pillarColor = Color.parseColor("#3877E1")
    private var percentColor = Color.parseColor("#E84742")
    private var textColor = Color.BLACK

    init {
        typographic()
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
        descPaint.color = textColor
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
        descPaint.color = textColor
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
        shadowPath.moveTo(chartRectF.left, chartRectF.bottom)
        linePath.moveTo(chartRectF.left, chartRectF.bottom)
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
                        val y = k * chartRectF.left + b
                        shadowPath.lineTo(chartRectF.left, y)
                        shadowPath.lineTo(percentPoint.x, percentPoint.y)
                        linePath.moveTo(chartRectF.left, y)
                        linePath.lineTo(percentPoint.x, percentPoint.y)
                        firstFit = false
                    } else {
                        val preIndex = if (i - 2 < 0) 0 else i - 2
                        val nextIndex =
                            if (i + 1 > percentPointList.size - 1) percentPointList.size - 1 else i + 1
                        val controlAX =
                            percentPointList[i - 1].x + (percentPoint.x - percentPointList[preIndex].x) / 6
                        var controlAY =
                            percentPointList[i - 1].y + (percentPoint.y - percentPointList[preIndex].y) / 6
                        controlAY = controlAY.coerceAtMost(chartRectF.bottom)
                        val controlBX =
                            percentPoint.x - (percentPointList[nextIndex].x - percentPointList[i - 1].x) /6
                        var controlBY =
                            percentPoint.y - (percentPointList[nextIndex].y - percentPointList[i - 1].y) /6
                        controlBY = controlBY.coerceAtMost(chartRectF.bottom)
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
            val y = k * chartRectF.right + b
            shadowPath.lineTo(chartRectF.right, y)
            linePath.lineTo(chartRectF.right, y)
            shadowPath.lineTo(chartRectF.right, chartRectF.bottom)
        } else {
            shadowPath.lineTo(percentPointList[lastShowIndex].x, chartRectF.bottom)
        }
        canvas.drawPath(shadowPath, brokenShaderPaint)
        canvas.drawPath(linePath, brokenLinePaint)
        //绘制Y坐标文字
        for (i in 0 until verList.size) {
            val text = if (leftFormat == 1) "${verList[i]}" else "${verList[i]}%"
            canvas.drawText(
                text,
                verTextPosList[2 * i],
                verTextPosList[2 * i + 1],
                textPaint
            )
            val rightText = if (rightFormat == 1) "${verRightList[i]}" else "${verRightList[i]}%"
            canvas.drawText(
                rightText,
                verRightTextPosList[2 * i],
                verRightTextPosList[2 * i + 1],
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
            val drawX = horTextPosList[2 * i]
            val item = horList[i]
            if (drawX in startShowPillarX..endShowPillarX) {
                canvas.drawText(
                    getHorYearMonthStr(item),
                    drawX,
                    horTextPosList[2 * i + 1] + 2 * verticalTextSize,
                    textPaint
                )
                break
            }
        }
        if (isDrawPillarValue) {
            //绘制x坐标 value文字
            for (i in leftDataList.indices) {
                val text = if (leftFormat == 1) "${leftDataList[i]}" else "${leftDataList[i]}%"
                val drawX = horValueTextPosList[2 * i]
                if (drawX in startShowPillarX..endShowPillarX)
                    canvas.drawText(
                        text,
                        drawX,
                        horValueTextPosList[2 * i + 1],
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

    private var startX = 0f
    private var startY = 0f
    private var lastMoveX = 0f
    private var lastMoveY = 0f
    private var endX = 0f
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                windowInfo = null
                startX = event.x
                startY = event.y
                lastMoveX = event.x
                lastMoveY = event.y
                endX = event.x
            }
            MotionEvent.ACTION_MOVE -> {
                endX = event.x
                val endY = event.y
                val distanceX = abs(endX - lastMoveX)
                val distanceY = abs(endY - lastMoveY)
                if (distanceY > distanceX) {
                    return false
                }
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
                lastMoveX = event.x
                lastMoveY = event.y
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

    private fun typographic() {
        clear()
        val density = context.resources.displayMetrics.density
        horizontalSpace = 6.5f * density
        pillarWidth = 10 * density
        val leftDataMax = (leftDataList.max()!! + 0.499f).roundToInt()
        val verLeftNum =
            if (leftDataMax % yUnit == 0) leftDataMax / yUnit else leftDataMax / yUnit + 1
        val rightDataMax = (rightDataList.max()!! + 0.499f).roundToInt()
        val verRightNum =
            if (rightDataMax % yRightUnit == 0) rightDataMax / yRightUnit else rightDataMax / yRightUnit + 1
        var showNum = verLeftNum.coerceAtLeast(verRightNum)
        showNum = showNum.coerceAtLeast(5)
        for (i in 0..showNum) {
            val element = i * yUnit
            verList.add(0, element)
            val rightElement = i * yRightUnit
            verRightList.add(0, rightElement)
        }
        val calendar = Calendar.getInstance()
        calendar.set(year, month - 1, day)
        for (i in leftDataList.indices) {
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
        //第一个值最大，对应字符串长度最长
        val maxVerticalTextWidth =
            textPaint.measureText(if (leftFormat == 1) "${verList[0]}" else "${verList[0]}%")
        val maxVerticalRightTextWidth =
            textPaint.measureText(if (rightFormat == 1) "${verRightList[0]}" else "${verRightList[0]}%")
        val chartLeftMargin = horizontalMargin + maxVerticalTextWidth + 10 //10 文字位置右侧距图标的距离
        windowHeight = verticalTextSize * 2 + textVerticalMargin + 2 * windowPadding
        val chartTopMargin =
            (verticalMargin + descTextSize + descMargin).coerceAtLeast(windowHeight + 2 * windowMargin)
        val chartWidth =
            leftDataList.size.coerceAtMost(maxShowPillarNum) * (pillarWidth + 2 * horizontalSpace)
        val chartHeight = (verList.size - 1) * verticalSpace
        startShowPillarX = chartLeftMargin
        endShowPillarX = startShowPillarX + chartWidth
        chartNeedWidth = chartLeftMargin + chartWidth + maxVerticalRightTextWidth + horizontalMargin
        chartNeedHeight =
            chartTopMargin + verticalMargin + chartHeight + verticalTextSize * 3 + textVerticalMargin
        chartRectF.left = chartLeftMargin
        chartRectF.top = chartTopMargin
        chartRectF.right = chartLeftMargin + chartWidth
        chartRectF.bottom = chartTopMargin + chartHeight
        //初始化画笔
        initPaint()
        for (i in 0 until verList.size) {
            val text = if (leftFormat == 1) "${verList[i]}" else "${verList[i]}%"
            val textX = horizontalMargin + maxVerticalTextWidth - textPaint.measureText(text)
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
        for (i in leftDataList.indices) {
            val left = i * (2 * horizontalSpace + pillarWidth) + horizontalSpace + chartLeftMargin
            val top =
                (chartHeight + chartTopMargin) - (leftDataList[i] / yUnit.toFloat()) * verticalSpace
            val right = left + pillarWidth
            val bottom = chartTopMargin + chartHeight - 1 //-1不压抽线
            val rectF = RectF(left, top, right, bottom)
            pillarRectList.add(rectF)
            //计算值的文字位置
            val valueText = if (leftFormat == 1) "${leftDataList[i]}" else "${leftDataList[i]}%"
            val valueTextX = left + pillarWidth / 2 - textPaint.measureText(valueText) / 2
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
                (chartHeight + chartTopMargin) - (rightDataList[i] / yRightUnit.toFloat()) * verticalSpace
            percentPointList.add(PointF(pointLeft, pointTop))
        }
    }

    private fun initPaint() {
        textPaint.textSize = verticalTextSize
        textPaint.color = textColor
        textPaint.isAntiAlias = true
        descPaint.isAntiAlias = true
        descPaint.textSize = descTextSize
        pillarPaint.color = pillarColor
        pillarPaint.isAntiAlias = true
        brokenShaderPaint.shader = LinearGradient(
            0f, chartRectF.top,
            0f, chartRectF.bottom,
            percentColor, Color.TRANSPARENT, Shader.TileMode.CLAMP
        )
        brokenShaderPaint.isAntiAlias = true
        brokenLinePaint.color = percentColor
        brokenLinePaint.isAntiAlias = true
        brokenLinePaint.style = Paint.Style.STROKE
        brokenLinePaint.strokeWidth = 4f
        windowPaint.isAntiAlias = true
        windowPaint.color = pillarColor
    }

    private fun clear() {
        horList.clear()
        verList.clear()
        verRightList.clear()
        linePosList.clear()
        coordinateLines.clear()
        pillarRectList.clear()
        percentPointList.clear()
        verTextPosList.clear()
        verRightTextPosList.clear()
        horTextPosList.clear()
        horValueTextPosList.clear()
        horYearMonthPosList.clear()
        windowInfo = null
    }

    private fun createDataWindow(targetX: Float) {
        //计算点击位置所在柱状图
        for (i in pillarRectList.indices) {
            val pillarRect = pillarRectList[i]
            val matchLeft = pillarRect.left - horizontalSpace
            val matchRight = pillarRect.right + horizontalSpace
            if (targetX in matchLeft..matchRight) {
                val timeText = getHorFormatStr(horList[i])
                val leftValueText =
                    if (leftFormat == 1) "${leftDataList[i]}" else "${leftDataList[i]}%"
                val rightValueText =
                    if (rightFormat == 1) "${rightDataList[i]}" else "${rightDataList[i]}%"
                val valueText = "${descList[0]}:$leftValueText ${descList[1]}:$rightValueText"
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

    fun setChartInfo(chartInfo: ChartInfo) {
        try {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = dateFormat.parse(chartInfo.date)
            if (date != null) {
                val instance = Calendar.getInstance()
                instance.time = date
                year = instance.get(Calendar.YEAR)
                month = instance.get(Calendar.MONTH) + 1
                day = instance.get(Calendar.DAY_OF_MONTH)
            }
            chartInfo.showFormat?.let {
                horFormat = it
            }
            chartInfo.verticalSpace?.let {
                verticalSpace = it
            }
            chartInfo.textColor?.let {
                textColor = Color.parseColor(it)
            }
            chartInfo.textSize?.let {
                verticalTextSize = TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_SP,
                    it,
                    resources.displayMetrics
                )
            }
            chartInfo.descTextSize?.let {
                descTextSize = TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_SP,
                    it,
                    resources.displayMetrics
                )
            }
            descList = mutableListOf(chartInfo.left.desc, chartInfo.right.desc)
            leftDataList = chartInfo.left.datas
            rightDataList = chartInfo.right.datas
            yUnit = chartInfo.left.unit
            yRightUnit = chartInfo.right.unit
            chartInfo.left.dataFormat?.let {
                leftFormat = it
            }
            chartInfo.right.dataFormat?.let {
                rightFormat = it
            }
            chartInfo.left.color?.let {
                pillarColor = Color.parseColor(it)
            }
            chartInfo.right.color?.let {
                percentColor = Color.parseColor(it)
            }
            typographic()
            requestLayout()
        } catch (exception: ParseException) {
            throw IllegalArgumentException("The format of The chart's date must be 'yyyy-MM-dd'")
        } catch (t: Throwable) {
            t.printStackTrace()
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

data class ChartInfo(val date: String, val left: VerticalBean, var right: VerticalBean) {
    var showFormat: Int? = null
    var textColor: String? = null
    var textSize: Float? = null
    var descTextSize: Float? = null
    var verticalSpace: Float? = null

    data class VerticalBean(val desc: String, val datas: List<Float>, val unit: Int) {
        var dataFormat: Int? = null
        var color: String? = null
    }
}

