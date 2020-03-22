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
class PieChartView(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
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
        percentTextSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 13f, resources.displayMetrics)
        descTextSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 11f, resources.displayMetrics)
        //水平延长线的长度
        drawHorizontalLineLength = 16 * density
        //延长斜线的长度
        initLength = 20 * density
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
                //绘制desc内容
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
                    linePaint.textSize = percentTextSize
                    canvas.drawText(it.percentText, it.percentTextX, it.percentTextY, linePaint)
                    linePaint.textSize = descTextSize
                    canvas.drawText(it.desc, it.descTextX, it.descTextY, linePaint)
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
                linePaint.textSize = percentTextSize
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
                            radius - (percentTextSize * textRawNum) / 2 + percentTextSize * (i + 1)
                        canvas.drawText(subText, startX, startY, linePaint)
                    }
                } else {
                    val textWidth = linePaint.measureText(mInnerText)
                    val startX = (2 * radius - textWidth) / 2
                    val startY = radius + percentTextSize / 2
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
                    angle == 0f || angle == 360f -> 5 // +x 边界
                    angle == 180f -> 6 //-x限边界
                    angle == 90f -> 7 // +y 限边界
                    else -> 8 // -y 边界
                }
            }
            var maxY = 0f //绘制最大Y
            var minY = 0f //绘制最小Y
            //计算绘制描述内容位置
            // 象限的遍历，5,6,7,8代表在坐标轴上
            for (i in 1..8) {
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
                for (index in unCorrectPiePartList.indices) {
                    val piePart = unCorrectPiePartList[index]
                    val coorAngle = ((piePart.startAngle + piePart.sweepAngle / 2) * Math.PI) / 180
                    val x = cos(coorAngle).toFloat() * (radius + strokeWidth / 2) + radius
                    val y = sin(coorAngle).toFloat() * (radius + strokeWidth / 2) + radius
                    piePart.dStartPoint = PointF(x, y)
                    //延伸线段
                    val tempMiddleX = cos(coorAngle).toFloat() * (radius + strokeWidth / 2 + initLength) + radius
                    val tempMiddleY = sin(coorAngle).toFloat() * (radius + strokeWidth / 2 + initLength) + radius
                    val frontPirPart = if (index > 0) unCorrectPiePartList[index - 1] else null
                    piePart.dMiddlePoint = correctDescRectF(PointF(tempMiddleX, tempMiddleY), piePart.quadrant, frontPirPart)
                    val endPointX: Float
                    var endPointY: Float
                    if (piePart.quadrant == 1 || piePart.quadrant == 4) {
                        endPointX = piePart.dMiddlePoint!!.x + drawHorizontalLineLength
                        endPointY = piePart.dMiddlePoint!!.y
                    } else {
                        if (piePart.dStartPoint!!.x == piePart.dMiddlePoint!!.x) {
                            // y 轴上竖直延伸
                            if (piePart.dStartPoint!!.y > piePart.dMiddlePoint!!.y) {
                                endPointX = piePart.dMiddlePoint!!.x
                                endPointY = piePart.dMiddlePoint!!.y - drawHorizontalLineLength
                            } else {
                                endPointX = piePart.dMiddlePoint!!.x
                                endPointY = piePart.dMiddlePoint!!.y + drawHorizontalLineLength
                            }
                        } else {
                            endPointX = piePart.dMiddlePoint!!.x - drawHorizontalLineLength
                            endPointY = piePart.dMiddlePoint!!.y
                        }
                    }
                    //对Y象限显示修正
                    if (piePart.quadrant == 7 && endPointY < maxY) {
                        endPointY = maxY + percentTextSize
                    }
                    if (piePart.quadrant == 8 && endPointY > minY) {
                        endPointY = minY - descTextSize
                    }
                    if (endPointY > maxY) {
                        maxY = endPointY + descTextSize
                    }
                    if (endPointY < minY) {
                        minY = endPointY - percentTextSize
                    }
                    piePart.dEndPoint = PointF(endPointX, endPointY)
                    descCount++
                    //文字位置
                    val percentText = "${piePart.percent * 100}"
                    val dotIndex = percentText.indexOf('.')
                    if (dotIndex > 0) {
                        val zeroNum = percentText.substring(dotIndex + 1).length
                        piePart.percentText = "${percentText.substring(0, dotIndex + min(zeroNum, dotNum) + 1)}%"
                    }
                    linePaint.textSize = percentTextSize
                    val percentTextWidth = linePaint.measureText(piePart.percentText)
                    linePaint.textSize = descTextSize
                    val descTextWidth = linePaint.measureText(piePart.desc)
                    val (percentX, descX) =
                        if (piePart.quadrant == 1 || piePart.quadrant == 4)
                            Pair(piePart.dEndPoint!!.x + 3, piePart.dEndPoint!!.x)
                        else
                            Pair(piePart.dEndPoint!!.x - percentTextWidth - 3, piePart.dEndPoint!!.x - descTextWidth - 3)
                    piePart.percentTextX = percentX
                    piePart.descTextX = descX
                    piePart.percentTextY = piePart.dEndPoint!!.y
                    piePart.descTextY = piePart.dEndPoint!!.y + descTextSize
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

    private fun correctDescRectF(pointF: PointF, quadrant: Int, frontPiePart: PiePart?): PointF {
        //x,y轴上情况的的处理
        if (quadrant >= 5) {
            return pointF
        }
        //象限起始边界位置冲突描述修正，1、2象限下移避免交集
        if (descCount == 0) {
            val k = (pointF.y - radius) / (pointF.x - radius)
            if ((k * 1000000000).toInt() != 0) { //精度的处理，判断是否是坐标上
                if (quadrant == 1 || quadrant == 2) {
                    //1、2象限无论是否有象限值，都向下移动。没有象限点的情况下，保证与3、4象限起始位置不冲突
                    val value = pointF.y - radius - percentTextSize - descTextSize
                    if (value < 0) {
                        pointF.y = pointF.y - value
                    }
                } else {
                    val value = pointF.y - radius + percentTextSize + descTextSize
                    val pXBorderSize = piePartList.filter { it.quadrant == 5 }.size
                    val nXBorderSize = piePartList.filter { it.quadrant == 6 }.size
                    // 3、4象限有象限点才向上移动
                    if (value > 0) {
                        if ((quadrant == 3 && nXBorderSize > 0) || quadrant == 4 && pXBorderSize > 0)
                            pointF.y = pointF.y - value
                    }
                }
            }
            return pointF
        }
        //判断与前一个是否有碰撞，避让纠正位置
        if (frontPiePart != null) {
            if (frontPiePart.quadrant == 1 || frontPiePart.quadrant == 2) {
                val targetPos = pointF.y - percentTextSize
                val minus = targetPos - frontPiePart.descTextY
                if (minus <= 0) {
                    pointF.y = pointF.y - minus
                }
            }
            if (frontPiePart.quadrant == 3 || frontPiePart.quadrant == 4) {
                val targetPos = pointF.y + descTextSize
                val minus = targetPos - (frontPiePart.percentTextY - percentTextSize)
                if (minus >= 0) {
                    pointF.y = pointF.y - minus
                }
            }
        }
        return pointF
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

    fun addPiePart(color: Int, ratio: Float, desc: String, innerText: String = "") {
        mInnerText = innerText
        piePartList.add(PiePart(color, ratio, desc))
        typographic()
    }

    fun addPieParts(list: List<Triple<Int, Float, String>>, innerText: String = "") {
        mInnerText = innerText
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