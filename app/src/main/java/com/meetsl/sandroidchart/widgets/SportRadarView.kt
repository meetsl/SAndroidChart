package com.zybang.sports.widget

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import java.net.URLEncoder
import kotlin.math.*

/**
 * Created on 2022/9/20 6:48 下午
 * @author shilong
 *
 * desc: 雷达图
 */
class SportRadarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : View(context, attrs, defStyleAttr) {
    private var mStartAngle = PI * 3 / 2
    private var mItems: List<RadarItem>? = null
    private val mKeyPoints = mutableMapOf<String, PointF>()
    private val mProgressPoints = mutableMapOf<String, PointF>()
    private val mDescRectFs = mutableMapOf<String, RectF>()
    private val mPaint = Paint()
    private var mBgOuterColor = Color.parseColor("#FFDBCC")
    private var mProgressColor = Color.parseColor("#FF5A1E")
    private var mBgInnerColor = Color.parseColor("#FFEFE0")
    private var mShadowColor = Color.parseColor("#33FFA971")
    private var mValueTextColor = Color.parseColor("#141414")
    private var mNameTextColor = Color.parseColor("#9DA0A3")
    private val mDensity = resources?.displayMetrics?.density ?: 1f
    private var mOuterCircleStrokeWidth = mDensity * 1.65f
    private var mOuterLittleCircleRadius = mDensity * 2.2f
    private var mOuterCircleRadius = mDensity * 42f
    private var mInnerLineWidth = mDensity * 0.41f
    private var mDefaultCircleRadius = mDensity * 3f
    private var mProgressLineWidth = mDensity * 3f
    private var mDescMargin = mDensity * 5f
    private val textSize12 =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 10f, resources.displayMetrics)
    private val textSize18 =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 16f, resources.displayMetrics)
    private val mPath = Path()
    private var mValueAnimator = ValueAnimator()

    //view大小
    private val mViewBoundRectF = RectF()
    private var mValueTypeface: Typeface? = null
    private var isSmoothInit = false

    init {
        mPaint.isAntiAlias = true
        mPaint.style = Paint.Style.STROKE
        mPaint.color = Color.BLACK
        val defaultBoundSize = mOuterCircleRadius + mOuterLittleCircleRadius
        mViewBoundRectF.set(
            -defaultBoundSize,
            -defaultBoundSize,
            defaultBoundSize,
            defaultBoundSize
        )
        mValueAnimator.duration = 1000
        //调试数据
        /*mItems = mutableListOf(
            RadarItem("跳绳", 80, 100, 1),
            RadarItem("50米跑", 77, 100, 1),
            RadarItem("BMI", 100, 100, 1),
            RadarItem("肺活量", 5, 100, 1),
            RadarItem("坐位体前屈", 99, 100, 1),
            RadarItem("50*8往返跑", 60, 100, 1),
            RadarItem("1分钟仰卧起坐", 70, 100, 1),
        )

        calculateKeyPoints()*/
    }

    private fun calculateKeyPoints() {
        mItems?.let { list ->
            val size = list.size
            mKeyPoints.clear()
            mProgressPoints.clear()
            mDescRectFs.clear()
            val perAngle = PI * 2 / size.coerceAtLeast(1)
            list.forEachIndexed { index, radarItem ->
                val angle = mStartAngle - perAngle * index
                var pointX = mOuterCircleRadius * cos(angle).toFloat()
                var pointY = mOuterCircleRadius * sin(angle).toFloat()
                //精度处理
                if (abs(pointX) < 0.00001f) {
                    pointX = 0f
                }
                if (abs(pointY) < 0.00001f) {
                    pointY = 0f
                }
                mKeyPoints[radarItem.itemId] = PointF(pointX, pointY)
                calculateProgressPoint(radarItem)
                if (mValueTypeface != null) {
                    mPaint.typeface = mValueTypeface
                }
                val valueHeight = getTextHeight(textSize18)
                val valueWidth = mPaint.measureText(radarItem.getValueText())
                mPaint.typeface = null
                val nameHeight = getTextHeight(textSize12)
                val nameWidth = mPaint.measureText(radarItem.name)
                val maxWidth = valueWidth.coerceAtLeast(nameWidth)
                var scale = 1.3f
                var rectCenterX = pointX * scale
                var rectCenterY = pointY * scale
                val descHeight = nameHeight + valueHeight + mDescMargin
                val descRectF = RectF(
                    rectCenterX - maxWidth / 2,
                    rectCenterY - descHeight / 2,
                    rectCenterX + maxWidth / 2,
                    rectCenterY + descHeight / 2
                )
//                var count = 0
                while (isIntersect(descRectF)) {
                    scale += 0.05f
                    rectCenterX = pointX * scale
                    rectCenterY = pointY * scale
                    descRectF.set(
                        rectCenterX - maxWidth / 2,
                        rectCenterY - descHeight / 2,
                        rectCenterX + maxWidth / 2,
                        rectCenterY + descHeight / 2
                    )
//                    Log.i("SportRadarView", "${radarItem.name} ${count++}")
                }
                mDescRectFs[radarItem.itemId] = descRectF
                if (descRectF.left < mViewBoundRectF.left) {
                    mViewBoundRectF.left = descRectF.left
                }
                if (descRectF.right > mViewBoundRectF.right) {
                    mViewBoundRectF.right = descRectF.right
                }
                if (descRectF.top < mViewBoundRectF.top) {
                    mViewBoundRectF.top = descRectF.top
                }
                if (descRectF.bottom > mViewBoundRectF.bottom) {
                    mViewBoundRectF.bottom = descRectF.bottom
                }
            }
            val absLeft = abs(mViewBoundRectF.left)
            val absRight = abs(mViewBoundRectF.right)
            if (absLeft >= absRight) {
                if (mViewBoundRectF.right != 0f) {
                    mViewBoundRectF.right = mViewBoundRectF.right / absRight * absLeft
                }
            } else {
                if (mViewBoundRectF.left != 0f) {
                    mViewBoundRectF.left = mViewBoundRectF.left / absLeft * absRight
                }
            }

            val absTop = abs(mViewBoundRectF.top)
            val absBottom = abs(mViewBoundRectF.bottom)
            if (absTop >= absBottom) {
                if (absBottom != 0f) {
                    mViewBoundRectF.bottom = mViewBoundRectF.bottom / absBottom * absTop
                }
            } else {
                if (absTop != 0f) {
                    mViewBoundRectF.top = mViewBoundRectF.top / absTop * absBottom
                }
            }
        }
    }

    private fun calculateProgressPoint(radarItem: RadarItem) {
        val keyPointF = mKeyPoints[radarItem.itemId]
        if (keyPointF != null) {
            val percent = radarItem.tempValue() / radarItem.maxValue * 1.0f
            val targetX = keyPointF.x * percent
            val targetY = keyPointF.y * percent
            mProgressPoints[radarItem.itemId] = PointF(targetX, targetY)
        }
    }

    private fun isIntersect(descRectF: RectF): Boolean {
        val r2 =
            (mOuterCircleRadius + mOuterLittleCircleRadius) * (mOuterCircleRadius + mOuterLittleCircleRadius)
        val centerX = descRectF.centerX()
        val centerY = descRectF.centerY()
        return if (centerX > 0f && centerY > 0f) {
            descRectF.left * descRectF.left + descRectF.top * descRectF.top <= r2
        } else if (centerX < 0f && centerY > 0f) {
            descRectF.right * descRectF.right + descRectF.top * descRectF.top <= r2
        } else if (centerX < 0f && centerY < 0f) {
            descRectF.right * descRectF.right + descRectF.bottom * descRectF.bottom <= r2
        } else if (centerX == 0f && centerY < 0f) {
            descRectF.bottom * descRectF.bottom <= r2
        } else if (centerX == 0f && centerY > 0f) {
            descRectF.top * descRectF.top <= r2
        } else if (centerX > 0f && centerY == 0f) {
            descRectF.left * descRectF.left <= r2
        } else if (centerX < 0f && centerY == 0f) {
            descRectF.right * descRectF.right <= r2
        } else {
            descRectF.left * descRectF.left + descRectF.bottom * descRectF.bottom <= r2
        }
    }

    private fun getTextHeight(textSize: Float): Float {
        mPaint.textSize = textSize
        val fontMetrics = mPaint.fontMetrics
        if (fontMetrics != null) {
            return abs(fontMetrics.ascent) + abs(fontMetrics.descent)
        }
        return textSize
    }

    fun setRadarInfo(radarItems: List<RadarItem>) {
        isSmoothInit = true
        mItems = radarItems
        calculateKeyPoints()
        requestLayout()
    }

    fun smoothRadar(radarItems: List<RadarItem>) {
        mItems = radarItems
        calculateKeyPoints()
        requestLayout()
        startSmoothAnim()
    }

    private fun startSmoothAnim() {
        isSmoothInit = true
        mValueAnimator.removeAllUpdateListeners()
        mValueAnimator.cancel()
        mValueAnimator.setFloatValues(0f, 1f)
        mValueAnimator.addUpdateListener { animator ->
            val animatedValue = animator.animatedValue as Float
            mItems?.forEach { radarItem ->
                radarItem.tempValue = animatedValue * radarItem.intValue()
                calculateProgressPoint(radarItem)
            }
            invalidate()
        }
        mValueAnimator.start()
    }

    fun setValueTypeface(fontPath: String) {
        val mgr = context.assets
        if (mgr != null) {
            mValueTypeface = Typeface.createFromAsset(mgr, fontPath)
            calculateKeyPoints()
            requestLayout()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val widthSpecMode = MeasureSpec.getMode(widthMeasureSpec)
        val heightSpecMode = MeasureSpec.getMode(heightMeasureSpec)
        if (heightSpecMode == MeasureSpec.AT_MOST || heightSpecMode == MeasureSpec.UNSPECIFIED || widthSpecMode == MeasureSpec.AT_MOST || widthSpecMode == MeasureSpec.UNSPECIFIED) {
            val realHeightSpec =
                MeasureSpec.makeMeasureSpec(mViewBoundRectF.height().toInt(), MeasureSpec.EXACTLY)
            val realWidthSpec =
                MeasureSpec.makeMeasureSpec(mViewBoundRectF.width().toInt(), MeasureSpec.EXACTLY)
            setMeasuredDimension(realWidthSpec, realHeightSpec)
        }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.translate(abs(mViewBoundRectF.left), abs(mViewBoundRectF.top))
//        mPaint.style = Paint.Style.STROKE
//        canvas?.drawRect(mViewBoundRectF, mPaint)
        drawBg(canvas)
        drawProgress(canvas)
        drawDesc(canvas)
    }

    /**
     * 绘制圆形背景
     */
    private fun drawBg(canvas: Canvas?) {
        mPaint.style = Paint.Style.STROKE
        mPaint.color = mBgInnerColor
        mPaint.strokeWidth = mInnerLineWidth
        for (i in 1..4) {
            canvas?.drawCircle(0f, 0f, mOuterCircleRadius * i / 5, mPaint)
        }
        mKeyPoints.forEach {
            canvas?.drawLine(0f, 0f, it.value.x, it.value.y, mPaint)
        }
        mPaint.color = mBgOuterColor
        mPaint.style = Paint.Style.STROKE
        mPaint.strokeWidth = mOuterCircleStrokeWidth
        canvas?.drawCircle(0f, 0f, mOuterCircleRadius, mPaint)
        mPaint.style = Paint.Style.FILL
        mKeyPoints.forEach {
            canvas?.drawCircle(it.value.x, it.value.y, mOuterLittleCircleRadius, mPaint)
        }
    }

    /**
     * 绘制数值多边形
     */
    private fun drawProgress(canvas: Canvas?) {
        if (!isSmoothInit) {
            return
        }
        val filterList = mItems?.filter { it.intValue() > 0 }
        val validSize = filterList?.size ?: 0
        mPaint.color = mProgressColor
        if (validSize == 0) {
            canvas?.drawCircle(0f, 0f, mDefaultCircleRadius, mPaint)
        } else if (validSize > 2) {
            mPath.rewind()
            if (filterList != null) {
                for (i in 0 until validSize) {
                    val triplePoints = getTriplePoints(i, filterList, validSize)
                    if (triplePoints != null) {
                        val moveUnit = 7
                        val newPoint1X: Float
                        val newPoint1Y: Float
                        if (triplePoints.first.x == triplePoints.second.x) {
                            val moveDistance =
                                if (triplePoints.second.y > triplePoints.first.y) -moveUnit else moveUnit
                            newPoint1X = triplePoints.second.x
                            newPoint1Y = triplePoints.second.y + moveDistance
                        } else {
                            val k1 =
                                (triplePoints.second.y - triplePoints.first.y) / (triplePoints.second.x - triplePoints.first.x)
                            val b1 = triplePoints.second.y - triplePoints.second.x * k1
                            val dx = sqrt(moveUnit * moveUnit / (1 + k1 * k1))
                            val moveDistance =
                                if (triplePoints.second.x > triplePoints.first.x) -dx else dx
                            newPoint1X = triplePoints.second.x + moveDistance
                            newPoint1Y = newPoint1X * k1 + b1
                        }

                        val newPoint2X: Float
                        val newPoint2Y: Float
                        if (triplePoints.third.x == triplePoints.second.x) {
                            val moveDistance =
                                if (triplePoints.second.y > triplePoints.third.y) -moveUnit else moveUnit
                            newPoint2X = triplePoints.second.x
                            newPoint2Y = triplePoints.second.y + moveDistance
                        } else {
                            val k2 =
                                (triplePoints.second.y - triplePoints.third.y) / (triplePoints.second.x - triplePoints.third.x)
                            val b2 = triplePoints.second.y - triplePoints.second.x * k2
                            val dx = sqrt(moveUnit * moveUnit / (1 + k2 * k2))
                            val moveDistance =
                                if (triplePoints.second.x > triplePoints.third.x) -dx else dx
                            newPoint2X = triplePoints.second.x + moveDistance
                            newPoint2Y = newPoint2X * k2 + b2
                        }
                        //调试代码
//                        mPaint.color = mProgressColor
//                        canvas?.drawCircle(newPoint1X, newPoint1Y, 1f, mPaint)
//                        canvas?.drawCircle(triplePoints.second.x, triplePoints.second.y, 1f, mPaint)
//                        mPaint.color = Color.BLUE
                        canvas?.drawCircle(newPoint2X, newPoint2Y, 1f, mPaint)
                        val centerX1 = (triplePoints.first.x + triplePoints.second.x) / 2
                        val centerY1 = (triplePoints.first.y + triplePoints.second.y) / 2
                        val centerX2 = (triplePoints.second.x + triplePoints.third.x) / 2
                        val centerY2 = (triplePoints.second.y + triplePoints.third.y) / 2
                        //调试代码
//                        mPaint.color = Color.GREEN
//                        canvas?.drawCircle(centerX1, centerY1, 1f, mPaint)
//                        canvas?.drawCircle(centerX2, centerY2, 1f, mPaint)
                        if (i == 0) {
                            mPath.moveTo(centerX1, centerY1)
                        }
                        mPath.lineTo(newPoint1X, newPoint1Y)
                        mPath.quadTo(
                            triplePoints.second.x,
                            triplePoints.second.y,
                            newPoint2X,
                            newPoint2Y
                        )
                        mPath.lineTo(centerX2, centerY2)
                    }
                }
            }
            mPaint.style = Paint.Style.FILL
            mPaint.color = mShadowColor
            canvas?.drawPath(mPath, mPaint)
            mPaint.style = Paint.Style.STROKE
            mPaint.color = mProgressColor
            mPaint.strokeWidth = mProgressLineWidth
            canvas?.drawPath(mPath, mPaint)
        } else {
            mPaint.strokeWidth = mProgressLineWidth
            canvas?.drawCircle(0f, 0f, mDefaultCircleRadius, mPaint)
            filterList?.forEach { radarItem ->
                val pointF = mProgressPoints[radarItem.itemId]
                if (pointF != null) {
                    canvas?.drawLine(0f, 0f, pointF.x, pointF.y, mPaint)
                    canvas?.drawCircle(pointF.x, pointF.y, mProgressLineWidth / 2, mPaint)
                }
            }
        }
    }

    /**
     * 绘制文字描述
     */
    private fun drawDesc(canvas: Canvas?) {
        mPaint.style = Paint.Style.FILL
        mItems?.forEach { radarItem ->
            val descRectF = mDescRectFs[radarItem.itemId]
            if (descRectF != null) {
                val top = descRectF.top
                if (mValueTypeface != null) {
                    mPaint.typeface = mValueTypeface
                }
                mPaint.color = mValueTextColor
                val valueTextY = top + getTextHeight(textSize18) - mPaint.descent()
                val valueText = radarItem.getValueText()
                val valueTextWidth = mPaint.measureText(valueText)
                val valueTextX = descRectF.left + (descRectF.width() - valueTextWidth) / 2
                canvas?.drawText(valueText, valueTextX, valueTextY, mPaint)
                mPaint.typeface = null
                mPaint.color = mNameTextColor
                val nameTextHeight = getTextHeight(textSize12)
                val nameText = radarItem.name
                val nameTextWidth = mPaint.measureText(nameText)
                val nameTextX = descRectF.left + (descRectF.width() - nameTextWidth) / 2
                val nameTextY = valueTextY + mDescMargin + nameTextHeight
                canvas?.drawText(nameText, nameTextX, nameTextY, mPaint)
                //调试代码
                /*mPaint.style = Paint.Style.STROKE
                mPaint.strokeWidth = 2f
                canvas?.drawRect(descRectF, mPaint)

                canvas?.drawRect(
                    valueTextX,
                    valueTextY - getTextHeight(textSize18) + mPaint.descent(),
                    valueTextX + valueTextWidth,
                    valueTextY,
                    mPaint
                )*/
            }
        }
    }


    private fun getTriplePoints(
        i: Int,
        filterList: List<RadarItem>,
        validSize: Int
    ): Triple<PointF, PointF, PointF>? {
        val preRadarItem: RadarItem
        val nextRadarItem: RadarItem
        when (i) {
            0 -> {
                preRadarItem = filterList[validSize - 1]
                nextRadarItem = filterList[i + 1]
            }
            validSize - 1 -> {
                preRadarItem = filterList[i - 1]
                nextRadarItem = filterList[0]
            }
            else -> {
                preRadarItem = filterList[i - 1]
                nextRadarItem = filterList[i + 1]
            }
        }
        val radarItem: RadarItem = filterList[i]
        val preProgressPoint = mProgressPoints[preRadarItem.itemId]
        val progressPoint = mProgressPoints[radarItem.itemId]
        val nextProgressPoint = mProgressPoints[nextRadarItem.itemId]
        if (preProgressPoint != null && progressPoint != null && nextProgressPoint != null) {
            return Triple(preProgressPoint, progressPoint, nextProgressPoint)
        }
        return null
    }

    /**
     * 雷达图条目数据
     */
    data class RadarItem(
        val name: String,
        private val value: Int,
        val maxValue: Int = 100,
        val status: Int = 1
    ) {
        var itemId = ""
            private set
        var tempValue: Float = value.toFloat()

        init {
            val encodeName = try {
                URLEncoder.encode(name, "utf-8")
            } catch (e: Throwable) {
                name
            }
            itemId = "${encodeName}-${value}-${maxValue}-${status}-${System.currentTimeMillis()}"
        }

        fun getValueText(): String {
            return if (status == 0 && value == 0) "--" else "$value"
        }

        fun intValue(): Int {
            return value.coerceAtMost(maxValue)
        }

        fun tempValue(): Float {
            return tempValue.coerceAtMost(maxValue.toFloat())
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        mValueAnimator.removeAllUpdateListeners()
        mValueAnimator.cancel()
    }
}