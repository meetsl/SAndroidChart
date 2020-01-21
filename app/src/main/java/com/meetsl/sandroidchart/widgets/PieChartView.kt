package com.meetsl.sandroidchart.widgets

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
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
    private var radius = 200f
    private var strokeWidth = 60f
    private var viewRectF: RectF
    private var mainPaint = Paint()
    private var innerPaint = Paint()
    private var linePaint = Paint()
    private val piePartList = mutableListOf<PiePart>()
    private var piePath = Path()
    private val percentTextSize = 45f
    private var descTextSize = 35f
    //延长斜线的长度
    private val initLength = 80
    //水平延长线的长度
    private val drawHorizontalLineLength = 50
    private var centerX = 0f
    private var centerY = 0f
    private var chartViewBound: RectF? = null
    //象限统计计数
    private var descCount = 0
    private val executor = Executors.newFixedThreadPool(3)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context) : this(context, null)

    init {
        typographic()
        //圆绘制所在矩形位置
        viewRectF = RectF(0f, 0f, 2 * radius, 2 * radius)
        mainPaint.style = Paint.Style.STROKE
        mainPaint.isAntiAlias = true
        mainPaint.strokeWidth = strokeWidth

        innerPaint.color = Color.RED
        innerPaint.style = Paint.Style.STROKE
        innerPaint.isAntiAlias = true
        innerPaint.strokeWidth = 3f

        linePaint.strokeWidth = 5f
        linePaint.isAntiAlias = true
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
            //绘制内部虚线圆
            for (i in 1..2) {
                val left = strokeWidth / 2 + strokeWidth * i
                val right = 2 * radius - left
                viewRectF.set(left, left, right, right)
                drawInner(viewRectF, 30 - (i - 1) * 8)
            }
            canvas.drawPath(piePath, innerPaint)
        }
        piePath.reset()
        //恢复绘制矩形
        viewRectF.set(0f, 0f, 2 * radius, 2 * radius)
    }

    /**
     * 绘制排版
     */
    private fun typographic() {
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
                    it.dMiddlePoint =
                        correctDescRectF(PointF(tempMiddleX, tempMiddleY), it.quadrant)
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
                        val dotNum = percentText.substring(dotIndex).length
                        it.percentText = "${percentText.substring(0, dotIndex + min(dotNum, 3))}%"
                    }
                    linePaint.textSize = percentTextSize
                    val percentTextWidth = linePaint.measureText(it.percentText)
                    linePaint.textSize = descTextSize
                    val descTextWidth = linePaint.measureText(it.desc)
                    val (percentX, descX) =
                        if (it.quadrant == 1 || it.quadrant == 4)
                            Pair(it.dEndPoint!!.x, it.dEndPoint!!.x)
                        else
                            Pair(
                                it.dEndPoint!!.x - percentTextWidth,
                                it.dEndPoint!!.x - descTextWidth
                            )
                    it.percentTextX = percentX
                    it.descTextX = descX
                    it.percentTextY = it.dEndPoint!!.y
                    it.descTextY = it.dEndPoint!!.y + descTextSize
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

    private fun correctDescRectF(pointF: PointF, quadrant: Int): PointF {
        //x,y轴上情况的的处理
        if (quadrant == 5) {
            return pointF
        }
        //象限起始边界位置冲突描述修正，1、2象限下移避免交集
        if (descCount == 0 && (quadrant == 1 || quadrant == 2)) {
            val k = (pointF.y - radius) / (pointF.x - radius)
            if ((k * 1000000).toInt() != 0) { //精度的处理，判断是否是坐标上
                val b = radius - k * radius
                pointF.y = pointF.y + (percentTextSize - descTextSize)
                pointF.x = (pointF.y - b) / k
            }
            return pointF
        }
        //判断是否有碰撞的描述块，避让纠正位置
        val correctPiePartQuadrantList =
            piePartList.filter { it.quadrant == quadrant && it.dEndPoint != null }
                .sortedBy {
                    // 1、3象限角度正序计算描述位置 2、4象限倒序计算描述位置
                    if (it.quadrant == 1 || it.quadrant == 3)
                        it.startAngle
                    else
                        -it.startAngle
                }
        correctPiePartQuadrantList.forEach {
            val k = (pointF.y - radius) / (pointF.x - radius)
            val b = radius - k * radius
            if (it.quadrant == 1 || it.quadrant == 2) {
                val targetPos = pointF.y - percentTextSize
                val minus = targetPos - it.descTextY
                if (minus <= 0) {
                    pointF.y = pointF.y - minus
                }
            }
            if (it.quadrant == 3 || it.quadrant == 4) {
                val targetPos = pointF.y + descTextSize
                val minus = targetPos - (it.percentTextY - percentTextSize)
                if (minus >= 0) {
                    pointF.y = pointF.y - minus
                }
            }
            pointF.x = (pointF.y - b) / k
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

    fun addPiePart(color: Int, ratio: Float, desc: String) {
        piePartList.add(PiePart(color, ratio, desc))
        typographic()
    }

    fun addPieParts(list: List<Triple<Int, Float, String>>) {
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