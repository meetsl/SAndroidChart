package com.zybang.sports.widget

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import androidx.core.content.res.ResourcesCompat
import com.zybang.bfsports.R
import com.zybang.sports.utils.SafeScreenUtil
import kotlin.math.abs

/**
 * Created on 2022/7/20 4:04 下午
 * @author shilong
 *
 * desc: 首页进度条
 */
class SportProgressBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private val textSize12 =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12f, resources.displayMetrics)
    private val textSize11 =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 11f, resources.displayMetrics)
    private val textSize9 =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 9f, resources.displayMetrics)
    private val runningRectF = RectF()
    private val targetRectF = RectF()
    private val topRectF = RectF()
    private val lineNumRectF = RectF()
    private val topValidRectF = RectF()
    private val path = Path()
    private var progressPaint = Paint()
    private var descPaint = Paint()
    private var progressHeight = SafeScreenUtil.dp2px(12f).toFloat()
    private val topDescHeight: Float
    private var topDescMarginBottom = SafeScreenUtil.dp2px(10f).toFloat()
    private val bottomNumDescHeight: Float
    private val bottomLineDescHeight: Float
    private var bottomDescMarginTop = SafeScreenUtil.dp2px(10f).toFloat()
    private var bottomLineDescMarginTop = SafeScreenUtil.dp2px(3f).toFloat()
    private var maxProgress = 300f
    private var progress = 0f
    private var originProgress = 0f
    private var topProgress = 0f
    private var targetProgress = 0f
    private var targetDesc = ""
    private var thumbSize = progressHeight * 1.8f
    private var mGradient: LinearGradient? = null
    private val mRunBitmap: Bitmap
    private val mTargetBitmap: Bitmap
    private val mTopitmap: Bitmap
    private var mDescNumArray: Array<Pair<String, Float>>? = null
    private var mDescNormalColor: Int
    private var mDescSelectedColor: Int
    private var mTrans3WhiteColor: Int
    private var mTrans6WhiteColor: Int
    private var mProgressBgColor: Int
    private var mProgressStartColor: Int
    private var mProgressEndColor: Int
    private var mValueAnimator = ValueAnimator()
    private var forceUpdate = false
    private var descMargin = 0f

    init {
        progressPaint.isAntiAlias = true
        progressPaint.color = Color.BLACK
        progressPaint.style = Paint.Style.FILL
        progressPaint.strokeWidth = 15f

        descPaint.isAntiAlias = true
        descPaint.color = Color.BLACK
        descPaint.style = Paint.Style.FILL
        descPaint.isFakeBoldText = true
        descPaint.textSize = textSize12
        val fontMetrics = descPaint.fontMetrics
        topDescHeight = abs(fontMetrics.ascent)
        bottomNumDescHeight = abs(fontMetrics.ascent)
        bottomLineDescHeight = abs(fontMetrics.ascent)
        mRunBitmap = BitmapFactory.decodeResource(resources, R.drawable.home_ic_running_man)
        mTargetBitmap = BitmapFactory.decodeResource(resources, R.drawable.home_ic_target_score)
        mTopitmap = BitmapFactory.decodeResource(resources, R.drawable.home_ic_top_score)
        mDescNormalColor =
            ResourcesCompat.getColor(resources, R.color.home_sport_progress_desc_normal_color, null)
        mDescSelectedColor = ResourcesCompat.getColor(
            resources,
            R.color.home_sport_progress_desc_selected_color,
            null
        )
        mTrans3WhiteColor = ResourcesCompat.getColor(resources, R.color.trans_3_white, null)
        mTrans6WhiteColor = ResourcesCompat.getColor(resources, R.color.trans_6_white, null)
        mProgressBgColor =
            ResourcesCompat.getColor(resources, R.color.home_sport_progress_bg_color, null)
        mProgressStartColor =
            ResourcesCompat.getColor(resources, R.color.home_sport_progress_start_color, null)
        mProgressEndColor =
            ResourcesCompat.getColor(resources, R.color.home_sport_progress_end_color, null)

        mValueAnimator.duration = 2000
        descMargin = descPaint.measureText("0")
    }

    private fun startAnim() {
        mValueAnimator.removeAllUpdateListeners()
        mValueAnimator.cancel()
        mValueAnimator.setFloatValues(0f, originProgress.coerceAtMost(maxProgress))
        mValueAnimator.addUpdateListener { animator ->
            this.progress = animator.animatedValue as Float
            invalidate()
        }
        mValueAnimator.start()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val mode = MeasureSpec.getMode(heightMeasureSpec)
        if (mode == MeasureSpec.UNSPECIFIED || mode == MeasureSpec.AT_MOST) {
            val topHeight = topDescHeight + topDescMarginBottom
            val bottomHeight =
                bottomNumDescHeight + bottomDescMarginTop + bottomLineDescHeight + bottomLineDescMarginTop
            val totalHeight = progressHeight + topHeight + bottomHeight
            val newHeightMeasureSpec =
                MeasureSpec.makeMeasureSpec(totalHeight.toInt(), MeasureSpec.EXACTLY)
            setMeasuredDimension(widthMeasureSpec, newHeightMeasureSpec)
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        if (changed) {
            updateGradient(left.toFloat(), right.toFloat())
            invalidate()
        }
    }

    fun setNumArray(numArray: Array<Pair<String, Float>>) {
        numArray.forEachIndexed { index, pair ->
            val originPair = mDescNumArray?.getOrNull(index)
            if (originPair?.first != pair.first || originPair.second != pair.second) {
                forceUpdate = true
                return@forEachIndexed
            }
        }
        this.mDescNumArray = numArray
    }

    fun setMaxProgress(maxProgress: Int) {
        forceUpdate = this.maxProgress != maxProgress.toFloat()
        this.maxProgress = maxProgress.toFloat()
    }

    fun setProgressSmooth(progress: Int) {
        if (this.originProgress.toInt() == progress && !forceUpdate)
            return
        this.originProgress = progress.toFloat()
        updateGradient(0f, width.toFloat())
        startAnim()
        forceUpdate = false
    }

    fun setProgress(progress: Int) {
        if (this.originProgress.toInt() == progress && !forceUpdate)
            return
        this.originProgress = progress.toFloat()
        this.progress = this.originProgress
        updateGradient(0f, width.toFloat())
        invalidate()
        forceUpdate = false
    }

    fun setTargetProgress(desc: String, target: Int) {
        forceUpdate = this.targetProgress != target.toFloat() || this.targetDesc != desc
        this.targetProgress = target.toFloat()
        this.targetDesc = desc
    }

    fun setTopProgress(top: Int) {
        forceUpdate = this.topProgress != top.toFloat()
        this.topProgress = top.toFloat()
    }

    private fun updateGradient(left: Float, right: Float) {
        val width = right - left
        val percent = originProgress / maxProgress
        val endX = (thumbSize / 2).coerceAtLeast(width * percent)
        mGradient = LinearGradient(
            0f,
            0f,
            endX,
            0f,
            intArrayOf(mProgressStartColor, mProgressEndColor),
            floatArrayOf(0f, 1f),
            Shader.TileMode.CLAMP
        )
    }

    override fun draw(canvas: Canvas?) {
        super.draw(canvas)
        path.reset()
        drawProgressBg(canvas)
        drawProgress(canvas)
        drawDesc(canvas)
    }

    /**
     * 绘制进度
     */
    private fun drawProgress(canvas: Canvas?) {
        val canvasWidth = width
        path.rewind()
        if (mGradient != null) {
            progressPaint.shader = mGradient
        }
        progressPaint.color = Color.WHITE
        val topHeight = topDescHeight + topDescMarginBottom
        //进度
        val percent = progress / maxProgress
        val rectWidth = canvasWidth * percent - thumbSize / 2
        if (rectWidth <= thumbSize / 2) {
            path.addCircle(
                thumbSize / 2,
                topHeight + progressHeight / 2,
                thumbSize / 2,
                Path.Direction.CW
            )
            canvas?.drawPath(path, progressPaint)
            drawRunningMan(thumbSize / 2, topHeight + progressHeight / 2, canvas)
            return
        }
        path.addArc(0f, topHeight, progressHeight, topHeight + progressHeight, 90f, 180f)
        path.addRect(
            progressHeight / 2,
            topHeight,
            rectWidth,
            topHeight + progressHeight,
            Path.Direction.CW
        )
        path.addCircle(rectWidth, topHeight + progressHeight / 2, thumbSize / 2, Path.Direction.CW)

        val controlDistance = thumbSize * 1f
        val controlX1Factor = 0.46f
        val controlX2Factor = 0.1f
        val controlY2Factor = 0.03f
        var topStartPointX = rectWidth - controlDistance
        if (topStartPointX > 0f) {
            //上弧线
            topStartPointX = topStartPointX.coerceAtLeast(progressHeight / 2)
            val topStartPointY = topHeight
            val topEndPointX = rectWidth
            val topEndPointY = topHeight - (thumbSize - progressHeight) / 2

            val topControlPointX1 =
                topStartPointX + (topEndPointX - topStartPointX) * controlX1Factor
            val topControlPointY1 = topStartPointY

            val topControlPointX2 = topEndPointX - (topEndPointX - topStartPointX) * controlX2Factor
            val topControlPointY2 =
                topEndPointY - (topEndPointX - topStartPointX) * controlY2Factor + 0.48f
            path.moveTo(topStartPointX, topStartPointY)
            path.cubicTo(
                topControlPointX1,
                topControlPointY1,
                topControlPointX2,
                topControlPointY2,
                topEndPointX,
                topEndPointY
            )
            //下弧线
            var bottomStartPointX = rectWidth - controlDistance
            bottomStartPointX = bottomStartPointX.coerceAtLeast(progressHeight / 2)
            val bottomStartPointY = topHeight + progressHeight
            val bottomEndPointX = rectWidth
            val bottomEndPointY = topHeight + progressHeight + (thumbSize - progressHeight) / 2
            val bottomControl1PointX =
                bottomStartPointX + (bottomEndPointX - bottomStartPointX) * controlX1Factor
            val bottomControl1PointY = bottomStartPointY
            val bottomControl2PointX =
                bottomEndPointX - (bottomEndPointX - bottomStartPointX) * controlX2Factor
            val bottomControl2PointY =
                bottomEndPointY + (bottomEndPointX - bottomStartPointX) * controlY2Factor
            path.lineTo(bottomEndPointX, bottomEndPointY)
            path.cubicTo(
                bottomControl2PointX,
                bottomControl2PointY,
                bottomControl1PointX,
                bottomControl1PointY,
                bottomStartPointX,
                bottomStartPointY
            )
            path.lineTo(topStartPointX, topStartPointY)
            //控制点显示
//            canvas?.drawCircle(topControlPointX1, topControlPointY1, 2f, descPaint)
//            canvas?.drawCircle(topControlPointX2, topControlPointY2 - 15, 2f, descPaint)
//            canvas?.drawCircle(bottomControl1PointX, bottomControl1PointY, 2f, descPaint)
//            canvas?.drawCircle(bottomControl2PointX, bottomControl2PointY + 15, 2f, descPaint)
        }
        canvas?.drawPath(path, progressPaint)
        //绘制进度小人
        val thumbCenterX = rectWidth
        val thumpCenterY = topHeight + progressHeight / 2
        val firstCircleCenterX = thumbCenterX - progressHeight * 2
        if (firstCircleCenterX > progressHeight / 8) {
            descPaint.color = mTrans3WhiteColor
            canvas?.drawCircle(
                firstCircleCenterX,
                thumpCenterY,
                progressHeight / 8,
                descPaint
            )
        }
        val secondCircleCenterX = thumbCenterX - progressHeight * 1.2f
        if (secondCircleCenterX > progressHeight / 6) {
            descPaint.color = mTrans6WhiteColor
            canvas?.drawCircle(
                secondCircleCenterX,
                thumpCenterY,
                progressHeight / 6,
                descPaint
            )
        }
        drawRunningMan(thumbCenterX, thumpCenterY, canvas)
    }

    private fun drawRunningMan(thumbCenterX: Float, thumpCenterY: Float, canvas: Canvas?) {
        if (!mRunBitmap.isRecycled) {
            descPaint.color = Color.WHITE
            val iconLeft = thumbCenterX - thumbSize / 2 + progressHeight / 10
            val iconTop = thumpCenterY - thumbSize / 2 + progressHeight / 10
            val iconRight = thumbCenterX + thumbSize / 2 - progressHeight / 10
            val iconBottom = thumpCenterY + thumbSize / 2 - progressHeight / 10
            runningRectF.set(iconLeft, iconTop, iconRight, iconBottom)
            canvas?.drawBitmap(
                mRunBitmap,
                null,
                runningRectF,
                descPaint
            )
        }
    }

    /**
     * 绘制进度背景
     */
    private fun drawProgressBg(canvas: Canvas?) {
        path.rewind()
        progressPaint.color = mProgressBgColor
        val canvasWidth = width.toFloat()
        val topHeight = topDescHeight + topDescMarginBottom
        path.addArc(0f, topHeight, progressHeight, topHeight + progressHeight, 90f, 180f)
        path.addArc(
            canvasWidth - progressHeight,
            topHeight,
            canvasWidth,
            topHeight + progressHeight,
            270f,
            180f
        )
        path.moveTo(progressHeight / 2, topHeight)
        path.lineTo(canvasWidth - progressHeight / 2, topHeight)
        path.lineTo(canvasWidth - progressHeight / 2, topHeight + progressHeight)
        path.lineTo(progressHeight / 2, topHeight + progressHeight)
        canvas?.drawPath(path, progressPaint)
    }

    private fun drawDesc(canvas: Canvas?) {
        //当前进度位置
        descPaint.textSize = textSize12
        val percentX = progress / maxProgress * width
        val progressText =
            if (progress >= maxProgress) "${originProgress.toInt()}" else "${progress.toInt()}"
        val progressTextWidth = descPaint.measureText(progressText)
        var progressDescX = percentX - thumbSize / 2 - progressTextWidth / 2
        var progressEndX =
            (progressDescX + progressTextWidth).coerceAtLeast(progressTextWidth)
        progressDescX = when {
            progressEndX >= width -> {
                progressEndX = width.toFloat()
                width - progressTextWidth
            }
            else -> {
                progressDescX.coerceAtLeast(thumbSize / 2 - progressTextWidth / 2).coerceAtLeast(0f)
            }
        }

        descPaint.color = mDescNormalColor
        val topHeight =
            topDescHeight + topDescMarginBottom + progressHeight + bottomDescMarginTop + bottomNumDescHeight
        val descent = descPaint.fontMetrics?.descent ?: 0f
        val textY = topHeight - descent
        descPaint.textSize = textSize11
        val unitText = "个数"
        val unitWidth = descPaint.measureText(unitText)
        canvas?.drawText(unitText, 0, 2, 0f, textY, descPaint)
        descPaint.textSize = textSize12
        descPaint.color = mDescSelectedColor
        if (originProgress != 0f) {
            canvas?.drawText(
                progressText,
                0,
                progressText.length,
                progressDescX,
                topDescHeight,
                descPaint
            )
        }
        val targetSize = progressHeight * 3 / 2
        val targetX = targetProgress / maxProgress * width - targetSize / 2
        val targetY = topDescHeight + topDescMarginBottom + progressHeight
        targetRectF.set(targetX, targetY - targetSize, targetX + targetSize, targetY)
        if (targetRectF.right > width) {
            val delta = targetRectF.right - width
            targetRectF.left = targetRectF.left - delta
            targetRectF.right = targetRectF.right - delta
        }
        val topSize = progressHeight * 5 / 4
        val topX = topProgress / maxProgress * width - topSize / 2
        val topY = topDescHeight + topDescMarginBottom - (topSize - progressHeight) / 2
        topRectF.set(topX, topY, topX + topSize, topY + topSize)
        if (topRectF.right > width) {
            val delta = topRectF.right - width
            topRectF.left = topRectF.left - delta
            topRectF.right = topRectF.right - delta
        }
        val targetText = "${targetProgress.toInt()}"
        val targetTextWidth = descPaint.measureText(targetText)
        var targetDrawX = targetRectF.centerX() - targetTextWidth * 0.8f
        descPaint.textSize = textSize9
        val targetDescWidth = descPaint.measureText(targetDesc)
        var targetDescDrawX = targetDrawX + (targetTextWidth - targetDescWidth) / 2
        val targetDescDrawY = textY + bottomLineDescHeight + bottomLineDescMarginTop
        targetDrawX = targetDrawX.coerceAtLeast(0f)
        targetDrawX = targetDrawX.coerceAtMost(width - targetTextWidth)
        targetDescDrawX = targetDescDrawX.coerceAtLeast(0f)
        targetDescDrawX = targetDescDrawX.coerceAtMost(width - targetTextWidth)

        val targetLineDiffHeight = progressHeight * 5 / 24
        val targetLineDrawX = targetProgress / maxProgress * width - progressHeight / 12
        lineNumRectF.set(
            targetLineDrawX,
            topDescHeight + topDescMarginBottom + targetLineDiffHeight,
            targetLineDrawX + progressHeight / 6,
            topDescHeight + topDescMarginBottom + progressHeight - targetLineDiffHeight
        )
        topValidRectF.set(
            topRectF.left - descMargin,
            topRectF.top,
            topRectF.right + descMargin,
            topRectF.bottom)

        descPaint.color = mDescNormalColor
        descPaint.textSize = textSize12
        val topText = resources.getString(R.string.home_progress_top_desc, topProgress.toInt())
        val topTextWidth = descPaint.measureText(topText)
        var topDescX = topRectF.centerX() - topTextWidth / 2
        topDescX = topDescX.coerceAtMost(width - topTextWidth)
        topDescX = topDescX.coerceAtLeast(0f)

        if (!mTargetBitmap.isRecycled && targetRectF.left > progressEndX + descMargin
            && !targetRectF.intersect(topValidRectF) && targetRectF.right <= width
        ) {
            canvas?.drawBitmap(mTargetBitmap, null, targetRectF, descPaint)
            descPaint.color = mDescNormalColor
            descPaint.textSize = textSize12
            canvas?.drawText(
                targetText,
                0,
                targetText.length,
                targetDrawX,
                textY,
                descPaint
            )
            descPaint.textSize = textSize9
            canvas?.drawText(
                targetDesc,
                0,
                targetDesc.length,
                targetDescDrawX,
                targetDescDrawY,
                descPaint
            )
        } else {
            descPaint.color = mDescNormalColor
            descPaint.textSize = textSize12
            canvas?.drawText(
                targetText,
                0,
                targetText.length,
                targetDrawX + targetTextWidth * 0.2f,
                textY,
                descPaint
            )
            descPaint.textSize = textSize9
            canvas?.drawText(
                targetDesc,
                0,
                targetDesc.length,
                targetDescDrawX + targetTextWidth * 0.2f,
                targetDescDrawY,
                descPaint
            )
            if (lineNumRectF.left > progressEndX && (!lineNumRectF.intersect(topRectF) || topDescX <= progressEndX + descMargin)) {
                descPaint.color = Color.WHITE
                canvas?.drawRoundRect(
                    lineNumRectF,
                    progressHeight / 12,
                    progressHeight / 12,
                    descPaint
                )
            }
        }

        if (!mTopitmap.isRecycled && topDescX > progressEndX + descMargin && topRectF.right <= width) {
            canvas?.drawBitmap(mTopitmap, null, topRectF, descPaint)
            descPaint.color = mDescSelectedColor
            descPaint.textSize = textSize12
            canvas?.drawText(
                topText,
                0,
                topText.length,
                topDescX,
                topDescHeight,
                descPaint
            )
        }
        if (mDescNumArray?.isNotEmpty() == true) {
            mDescNumArray?.forEach { pair ->
                val numDesc = pair.first
                val num = pair.second
                val numText = "${num.toInt()}"
                val numPercentX = num / maxProgress * width
                descPaint.textSize = textSize12
                val numWidth = descPaint.measureText(numText)
                var numDrawX = numPercentX - numWidth / 2
                numDrawX = numDrawX.coerceAtLeast(0f)
                numDrawX = numDrawX.coerceAtMost(width - numWidth)
                val numDrawEndX = numDrawX + numWidth
                val numLineDiffHeight = progressHeight * 5 / 24
                val numLineDrawX = numPercentX - progressHeight / 12
                lineNumRectF.set(
                    numLineDrawX,
                    topDescHeight + topDescMarginBottom + numLineDiffHeight,
                    numLineDrawX + progressHeight / 6,
                    topDescHeight + topDescMarginBottom + progressHeight - numLineDiffHeight
                )
                val progressNumEndX = progressDescX + progressTextWidth
                val numDescDrawY = textY + bottomLineDescHeight + bottomLineDescMarginTop
                descPaint.textSize = textSize9
                val numDescWidth = descPaint.measureText(numDesc)
                var numDescDrawX = numPercentX - numDescWidth / 2
                numDescDrawX = numDescDrawX.coerceAtLeast(0f)
                numDescDrawX = numDescDrawX.coerceAtMost(width - numWidth)
                if (numDrawX > unitWidth) {
                    if (numDrawX > targetDescDrawX + targetDescWidth + descMargin || numDrawEndX < targetDescDrawX - descMargin) {
                        descPaint.color = mDescNormalColor
                        descPaint.textSize = textSize12
                        canvas?.drawText(numText, 0, numText.length, numDrawX, textY, descPaint)
                        descPaint.textSize = textSize9
                        canvas?.drawText(
                            numDesc,
                            0,
                            numDesc.length,
                            numDescDrawX,
                            numDescDrawY,
                            descPaint
                        )
                        if (numDrawX > progressNumEndX && !lineNumRectF.intersect(topRectF)
                            && !lineNumRectF.intersect(targetRectF)
                        ) {
                            descPaint.color = Color.WHITE
                            canvas?.drawRoundRect(
                                lineNumRectF,
                                progressHeight / 12,
                                progressHeight / 12,
                                descPaint
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        mValueAnimator.removeAllUpdateListeners()
        mValueAnimator.cancel()
    }
}