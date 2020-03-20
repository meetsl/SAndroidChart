package com.meetsl.sandroidchart.widgets

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import java.util.concurrent.Executors
import kotlin.math.*

/**
 * @author : meetsl
 * date: 2020/1/19.
 * desc : default.
 */
class NoCorrectPieChartView(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
    View(context, attrs, defStyleAttr) {
    private lateinit var viewRectF: RectF
    private var mainPaint = Paint()
    private var innerPaint = Paint()
    private var linePaint = Paint()
    private var innerShadowPaint = Paint()
    private val piePartList = mutableListOf<PiePart>()
    private var piePath = Path()
    private var radius = 0f
    private var strokeWidth = 0f
    private var percentTextSize = 0f
    private var descTextSize = 0f
    private var mInnerText = ""
    private var maxDescTextLength = 9.0

    //水平延长线的长度
    private var drawHorizontalLineLength = 0f

    //延长斜线的长度
    private var initLength = 0f
    private var centerX = 0f
    private var centerY = 0f
    private var chartViewBound: RectF? = null

    //象限统计计数
    private var descCount = 0
    private val executor = Executors.newFixedThreadPool(3)
    private var drawInnerText = true

    //小数位数
    private var dotNum = 1

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context) : this(context, null)

    init {
        val density = resources.displayMetrics.density
        radius = 60 * density
        strokeWidth = 25 * density
        percentTextSize =
            TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 11f, resources.displayMetrics)
        descTextSize =
            TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 13f, resources.displayMetrics)
        //水平延长线的长度
        drawHorizontalLineLength = 16 * density
        //延长斜线的长度
        initLength = 25 * density
        typographic()
        mainPaint.style = Paint.Style.STROKE
        mainPaint.isAntiAlias = true

        innerPaint.color = Color.RED
        innerPaint.style = Paint.Style.STROKE
        innerPaint.isAntiAlias = true
        innerPaint.strokeWidth = density

        linePaint.strokeWidth = 1.5f * density
        linePaint.isAntiAlias = true

        innerShadowPaint.color = Color.parseColor("#33000000")
        innerShadowPaint.isAntiAlias = true
        innerShadowPaint.style = Paint.Style.STROKE
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val mode = MeasureSpec.getMode(heightMeasureSpec)
        if (mode == MeasureSpec.AT_MOST || mode == MeasureSpec.UNSPECIFIED) {
            //高度扩充  strokeWidth * 2
            if (chartViewBound != null) {
                val height = chartViewBound!!.height() + strokeWidth * 2 + 0.5f
                setMeasuredDimension(widthMeasureSpec, height.roundToInt())
            }
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        centerX = w / 2f - radius
        centerY = h / 2f - radius
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        //画布移动，使绘制显示在中心
        canvas.translate(centerX, centerY)
        if (piePartList.isNotEmpty()) {
            piePartList.forEach {
                //绘制饼
                mainPaint.color = it.color
                piePath.addArc(viewRectF, it.startAngle, it.sweepAngle)
                canvas.drawPath(piePath, mainPaint)
                piePath.reset()
                //绘制内容
                linePaint.color = it.color
                if (it.dStartPoint != null && it.dMiddlePoint != null && it.dEndPoint != null) {
                    canvas.drawLine(
                        it.dStartPoint!!.x, it.dStartPoint!!.y,
                        it.dMiddlePoint!!.x, it.dMiddlePoint!!.y, linePaint
                    )
                    canvas.drawLine(
                        it.dMiddlePoint!!.x, it.dMiddlePoint!!.y,
                        it.dEndPoint!!.x, it.dEndPoint!!.y, linePaint
                    )
                    //绘制desc内容
                    linePaint.textSize = descTextSize
                    val descText = it.desc
                    if (descText.length > maxDescTextLength) {
                        val textRawNum = ceil(descText.length / maxDescTextLength).toInt()
                        for (i in 0 until textRawNum) {
                            val startIndex = i * maxDescTextLength.toInt()
                            val endIndex =
                                ((i + 1) * maxDescTextLength.toInt()).coerceAtMost(descText.length)
                            val subText = descText.substring(startIndex, endIndex)
                            val startX = it.descTextX
                            val startY = it.descTextY + i * descTextSize
                            canvas.drawText(subText, startX, startY, linePaint)
                        }
                        //绘制百分比
                        linePaint.textSize = percentTextSize
                        canvas.drawText(
                            it.percentText,
                            it.percentTextX,
                            it.descTextY + (textRawNum - 1) * descTextSize + percentTextSize,
                            linePaint
                        )
                    } else {
                        canvas.drawText(it.desc, it.descTextX, it.descTextY, linePaint)
                    }
                    //绘制百分比
                    linePaint.textSize = percentTextSize
                    canvas.drawText(it.percentText, it.percentTextX, it.percentTextY, linePaint)
                }
            }
            //绘制内圈阴影，立体显示
            piePath.reset()
            val shadowLeft = strokeWidth / 2 - innerShadowPaint.strokeWidth / 2
            val shadowRight = 2 * radius - shadowLeft
            viewRectF.set(shadowLeft, shadowLeft, shadowRight, shadowRight)
            piePath.addArc(viewRectF, 0f, 360f)
            canvas.drawPath(piePath, innerShadowPaint)
            if (drawInnerText) {
                linePaint.textSize = descTextSize
                linePaint.color = Color.BLACK
                if (mInnerText.length > 6) { //一行显示六个
                    val textRawNum = ceil(mInnerText.length / 6.0).toInt()
                    for (i in 0 until textRawNum) {
                        val startIndex = i * 6
                        val endIndex = ((i + 1) * 6).coerceAtMost(mInnerText.length)
                        val subText = mInnerText.substring(startIndex, endIndex)
                        val textWidth = linePaint.measureText(subText)
                        val startX = (2 * radius - textWidth) / 2
                        val startY =
                            radius - (descTextSize * textRawNum) / 2 + descTextSize * (i + 1)
                        canvas.drawText(subText, startX, startY, linePaint)
                    }
                } else {
                    val textWidth = linePaint.measureText(mInnerText)
                    val startX = (2 * radius - textWidth) / 2
                    val startY = radius + descTextSize / 2
                    canvas.drawText(mInnerText, startX, startY, linePaint)
                }
            } else {
                piePath.reset()
                //绘制
                //绘制内部虚线圆
                for (i in 1..2) {
                    val left = strokeWidth / 2 + strokeWidth * i
                    val right = 2 * radius - left
                    viewRectF.set(left, left, right, right)
                    drawInner(viewRectF, 30 - (i - 1) * 8)
                }
                canvas.drawPath(piePath, innerPaint)
            }
        }
        piePath.reset()
        //恢复绘制矩形
        viewRectF.set(0f, 0f, 2 * radius, 2 * radius)
    }

    /**
     * 绘制排版
     */
    private fun typographic() {
        mainPaint.strokeWidth = strokeWidth
        innerShadowPaint.strokeWidth = strokeWidth / 6
        //圆绘制所在矩形位置
        viewRectF = RectF(0f, 0f, 2 * radius, 2 * radius)
        executor.submit {
            //计算总角度
            var totalPart = 0f
            piePartList.forEach {
                totalPart += it.ratio
                it.dStartPoint = null
                it.dMiddlePoint = null
                it.dEndPoint = null
            }
            //计算角度和象限
            var startTemp = 0f
            piePartList.forEach {
                it.percent = (it.ratio / totalPart)
                val percentAngle = 360 * it.percent
                it.startAngle = startTemp
                it.sweepAngle = percentAngle
                startTemp += percentAngle
                // 设置象限
                val angle = it.startAngle + it.sweepAngle / 2
                it.quadrant = when {
                    angle > 0f && angle < 90f -> 1
                    angle > 90f && angle < 180f -> 2
                    angle > 180f && angle < 270f -> 3
                    angle > 270f && angle < 360f -> 4
                    else -> 5 //象限边界
                }
            }
            //计算绘制描述内容位置
            for (i in 1..5) {
                descCount = 0
                val unCorrectPiePartList =
                    piePartList.filter { it.quadrant == i && it.dEndPoint == null }
                        .sortedBy {
                            // 1、3象限角度正序计算描述位置 2、4象限倒序计算描述位置
                            if (it.quadrant == 1 || it.quadrant == 3)
                                it.startAngle
                            else
                                -it.startAngle
                        }
                unCorrectPiePartList.forEach {
                    val coorAngle = ((it.startAngle + it.sweepAngle / 2) * Math.PI) / 180
                    val x = cos(coorAngle).toFloat() * radius + radius
                    val y = sin(coorAngle).toFloat() * radius + radius
                    it.dStartPoint = PointF(x, y)
                    //延伸线段
                    val tempMiddleX = cos(coorAngle).toFloat() * (radius + initLength) + radius
                    val tempMiddleY = sin(coorAngle).toFloat() * (radius + initLength) + radius
                    it.dMiddlePoint = PointF(tempMiddleX, tempMiddleY)
                    val endPointX: Float
                    val endPointY: Float
                    if (it.quadrant == 1 || it.quadrant == 4) {
                        endPointX = it.dMiddlePoint!!.x + drawHorizontalLineLength
                        endPointY = it.dMiddlePoint!!.y
                    } else {
                        if (it.dStartPoint!!.x == it.dMiddlePoint!!.x) {
                            // y 轴上竖直延伸
                            if (it.dStartPoint!!.y > it.dMiddlePoint!!.y) {
                                endPointX = it.dMiddlePoint!!.x
                                endPointY = it.dMiddlePoint!!.y - drawHorizontalLineLength
                            } else {
                                endPointX = it.dMiddlePoint!!.x
                                endPointY = it.dMiddlePoint!!.y + drawHorizontalLineLength
                            }
                        } else {
                            endPointX = it.dMiddlePoint!!.x - drawHorizontalLineLength
                            endPointY = it.dMiddlePoint!!.y
                        }
                    }
                    it.dEndPoint = PointF(endPointX, endPointY)

                    descCount++
                    //文字位置
                    val percentText = "${it.percent * 100}"
                    val dotIndex = percentText.indexOf('.')
                    if (dotIndex > 0) {
                        val zeroNum = percentText.substring(dotIndex + 1).length
                        val endIndex = dotIndex + if (dotNum > 0) min(zeroNum, dotNum) + 1 else 0
                        it.percentText = "${percentText.substring(0, endIndex)}%"
                    }
                    linePaint.textSize = percentTextSize
                    val percentTextWidth = linePaint.measureText(it.percentText)
                    linePaint.textSize = descTextSize
                    val maxSubDescText =
                        it.desc.substring(0, it.desc.length.coerceAtMost(maxDescTextLength.toInt()))
                    val descTextWidth = linePaint.measureText(maxSubDescText)
                    val (descX, percentX) =
                        if (it.quadrant == 1 || it.quadrant == 4)
                            Pair(it.dEndPoint!!.x, it.dEndPoint!!.x)
                        else
                            Pair(
                                it.dEndPoint!!.x - descTextWidth,
                                it.dEndPoint!!.x - percentTextWidth
                            )
                    it.descTextX = descX
                    it.percentTextX = percentX
                    it.descTextY = it.dEndPoint!!.y
                    it.percentTextY = it.dEndPoint!!.y + percentTextSize
                    val descText = it.desc
                    if (descText.length > maxDescTextLength) {
                        val textRawNum = ceil(descText.length / maxDescTextLength).toInt()
                        it.percentTextY =
                            it.descTextY + (textRawNum - 1) * descTextSize + percentTextSize
                    } else {
                        it.percentTextY = it.dEndPoint!!.y + percentTextSize
                    }
                }
            }
            chartViewBound = getPieChartViewBound()
            //重新绘制
            this.post {
                requestLayout()
            }
        }
    }

    private fun drawInner(rectF: RectF, num: Int) {
        val total = 360f
        val drawNum = num * 2
        val sweepAngle = (total / drawNum)
        for (i in 0 until drawNum) {
            val startAngle = sweepAngle * 2 * i
            piePath.addArc(rectF, startAngle, sweepAngle)
        }
    }

    /**
     * 获取 view 边界
     */
    private fun getPieChartViewBound(): RectF {
        var minX = 0f
        var minY = 0f
        var maxX = 2 * radius
        var maxY = 2 * radius
        val bound = RectF(minX, minY, maxX, maxY)
        if (piePartList.isNotEmpty()) {
            piePartList.forEach {
                val tempMinX = min(it.descTextX, it.percentTextX)
                if (tempMinX < minX) {
                    minX = tempMinX
                }
                val tempMinY = min(it.descTextY, it.percentTextY - percentTextSize)
                if (tempMinY < minY) {
                    minY = tempMinY
                }
                val tempMaxX = max(it.descTextX, it.percentTextX)
                if (tempMaxX > maxX) {
                    maxX = tempMaxX
                }
                val tempMaxY = max(it.descTextY, it.percentTextY - percentTextSize)
                if (tempMaxY > maxY) {
                    maxY = tempMaxY
                }
            }
            bound.set(minX, minY, maxX, maxY)
        }
        return bound
    }

    fun addPiePart(
        color: Int,
        ratio: Float,
        desc: String,
        innerText: String = "",
        maxDescLength: Int = 8
    ) {
        mInnerText = innerText
        maxDescTextLength = maxDescLength.toDouble()
        piePartList.add(PiePart(color, ratio, desc))
        typographic()
    }

    fun addPieParts(
        list: List<Triple<Int, Float, String>>,
        innerText: String = "",
        maxDescLength: Int = 8
    ) {
        mInnerText = innerText
        maxDescTextLength = maxDescLength.toDouble()
        list.forEach {
            piePartList.add(PiePart(it.first, it.second, it.third))
        }
        typographic()
    }

    fun setChartInfo(
        list: List<Triple<Int, Float, String>>,
        radius: Int = 60,
        circleWidth: Int = 25,
        descTextSize: Float = 13f,
        percentTextSize: Float = 11f,
        innerText: String = "",
        maxDescLength: Int = 8,
        roundNum: Int = 1
    ) {
        val density = resources.displayMetrics.density
        this.radius = radius * density
        this.strokeWidth = circleWidth * density
        this.descTextSize = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP,
            descTextSize,
            resources.displayMetrics
        )
        this.percentTextSize = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP,
            percentTextSize,
            resources.displayMetrics
        )
        this.dotNum = roundNum
        mInnerText = innerText
        maxDescTextLength = maxDescLength.toDouble()
        list.forEach {
            piePartList.add(PiePart(it.first, it.second, it.third))
        }
        typographic()
    }

    fun clearPieParts() {
        piePartList.clear()
        this.postInvalidate()
    }

    private class PiePart(val color: Int, val ratio: Float, val desc: String) {
        var startAngle: Float = 0f
        var sweepAngle: Float = 0f
        var percent: Float = 0f
        var percentText: String = ""
        var quadrant: Int = 1
        var dStartPoint: PointF? = null
        var dMiddlePoint: PointF? = null
        var dEndPoint: PointF? = null
        var percentTextX = 0f
        var percentTextY = 0f
        var descTextX = 0f
        var descTextY = 0f
    }
}