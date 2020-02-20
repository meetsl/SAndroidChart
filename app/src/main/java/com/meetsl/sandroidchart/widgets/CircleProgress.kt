package com.meetsl.sandroidchart.widgets

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.os.Build
import android.text.TextPaint
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.view.animation.LinearInterpolator
import com.meetsl.sandroidchart.R
import kotlin.math.ceil

/**
 * @author : ShiLong
 * date: 2019/5/20.
 * desc : default.
 */
class CircleProgress(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
    View(context, attrs, defStyleAttr) {
    private var defOuterRadius = 100f
    private var defInnerRadius = 80f
    private var defMaxProgress = 100
    private var totalAngle = 250
    private var outerRadius = 200f
    private var innerRadius = 180f
    private var progress = 0
    private var maxProgress = 100
    private var progressUnit: String? = "--"
    private var dateContent: String? = "--"
    private var positionContent: String? = "--"
    private var desc: String? = ""
    private var descRawLength = 13f
    private var descLineMargin = 10f
    private val secondProgressPaint = Paint()
    private val progressPaint = Paint()
    private val littleCirclePaint = Paint()
    private var textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)
    private var bgPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)

    private var progressTextColor: Int = Color.WHITE
    private var progressTextSize: Float = 0f
    private var progressUnitTextSize: Float = 0f
    private var progressUnitTextColor: Int = Color.WHITE
    private var dateTextSize: Float = 0f
    private var dateTextColor: Int = Color.WHITE
    private var dateMarginTop: Float = 0f
    private var locationTextSize: Float = 0f
    private var locationTextColor: Int = Color.WHITE
    private var locationMarginTop: Float = 0f
    private var descTextSize: Float = 0f
    private var descTextColor: Int = Color.WHITE
    private var descMarginTop: Float = 0f
    private var needHeight: Float = 0f
    private val drawMargin = 30f
    private var mLisenter: ProgressChangeListener? = null

    // 0~25 背景色
    private val colors1 = intArrayOf(Color.parseColor("#EE975F"), Color.parseColor("#E94851"))
    // 26~50 背景色
    private val colors2 = intArrayOf(Color.parseColor("#FDB647"), Color.parseColor("#FF603B"))
    // 51~75 背景色
    private val colors3 = intArrayOf(Color.parseColor("#79C657"), Color.parseColor("#61D0CD"))
    // 76~100 背景色
    private val colors4 = intArrayOf(Color.parseColor("#308DF5"), Color.parseColor("#5620E5"))

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context) : this(context, null)

    init {
        val displayMetrics = resources.displayMetrics
        //默认进度字体大小
        progressTextSize =
            TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 30f, displayMetrics)
        progressUnitTextSize =
            TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 18f, displayMetrics)
        dateTextSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12f, displayMetrics)
        locationTextSize =
            TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 14f, displayMetrics)
        descTextSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 14f, displayMetrics)
        dateMarginTop = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8f, displayMetrics)
        locationMarginTop =
            TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 3f, displayMetrics)
        descMarginTop = 0f
        val theme = context.theme
        val typedArray =
            theme.obtainStyledAttributes(attrs, R.styleable.CircleProgressStyle, defStyleAttr, 0)
        val indexCount = typedArray.indexCount
        var progressColor = Color.WHITE
        var secondProgressColor = Color.parseColor("#22000000")
        val textColor = typedArray.getColor(R.styleable.CircleProgressStyle_textColor, Color.WHITE)
        progressTextColor = textColor
        progressUnitTextColor = textColor
        dateTextColor = textColor
        locationTextColor = textColor
        descTextColor = textColor

        //单独颜色设置优先级高于统一设置
        progressTextColor =
            typedArray.getColor(R.styleable.CircleProgressStyle_progressTextColor, textColor)
        progressUnitTextColor =
            typedArray.getColor(R.styleable.CircleProgressStyle_progressUnitTextColor, textColor)
        dateTextColor = typedArray.getColor(R.styleable.CircleProgressStyle_dateTextColor, textColor)
        locationTextColor =
            typedArray.getColor(R.styleable.CircleProgressStyle_locationTextColor, textColor)
        descTextColor = typedArray.getColor(R.styleable.CircleProgressStyle_descTextColor, textColor)

        for (i in 0 until indexCount) {
            val attr = typedArray.getIndex(i)
            when (attr) {
                R.styleable.CircleProgressStyle_outerRadius -> {
                    outerRadius = typedArray.getDimension(attr, defOuterRadius)
                }
                R.styleable.CircleProgressStyle_innerRadius -> {
                    innerRadius = typedArray.getDimension(attr, defInnerRadius)
                }
                R.styleable.CircleProgressStyle_maxProgress -> {
                    maxProgress = typedArray.getInteger(attr, defMaxProgress)
                }
                R.styleable.CircleProgressStyle_rank_progress -> {
                    progress = typedArray.getInteger(attr, 0)
                }
                R.styleable.CircleProgressStyle_totalAngle -> {
                    totalAngle = typedArray.getInteger(attr, totalAngle)
                }
                R.styleable.CircleProgressStyle_progressColor -> {
                    progressColor = typedArray.getColor(attr, Color.WHITE)
                }
                R.styleable.CircleProgressStyle_secondProgressColor -> {
                    secondProgressColor = typedArray.getColor(attr, secondProgressColor)
                }
                R.styleable.CircleProgressStyle_date -> {
                    dateContent = typedArray.getString(attr)
                }
                R.styleable.CircleProgressStyle_location -> {
                    positionContent = typedArray.getString(attr)
                }
                R.styleable.CircleProgressStyle_desc -> {
                    desc = typedArray.getString(attr)
                }
                R.styleable.CircleProgressStyle_progressUnit -> {
                    progressUnit = typedArray.getString(attr)
                }
                R.styleable.CircleProgressStyle_progressTextSize -> {
                    progressTextSize = typedArray.getDimension(attr, 30f)
                }

                R.styleable.CircleProgressStyle_progressUnitTextSize -> {
                    progressUnitTextSize = typedArray.getDimension(attr, 18f)
                }
                R.styleable.CircleProgressStyle_dateTextSize -> {
                    dateTextSize = typedArray.getDimension(attr, 12f)
                }

                R.styleable.CircleProgressStyle_dateMarginTop -> {
                    dateMarginTop = typedArray.getDimension(attr, 8f)
                }
                R.styleable.CircleProgressStyle_locationTextSize -> {
                    locationTextSize = typedArray.getDimension(attr, 14f)
                }

                R.styleable.CircleProgressStyle_locationMarginTop -> {
                    locationMarginTop = typedArray.getDimension(attr, 3f)
                }
                R.styleable.CircleProgressStyle_descTextSize -> {
                    descTextSize = typedArray.getDimension(attr, 14f)
                }
                R.styleable.CircleProgressStyle_descMarginTop -> {
                    descMarginTop = typedArray.getDimension(attr, 0f)
                }
            }
        }
        typedArray.recycle()

        secondProgressPaint.isAntiAlias = true
        secondProgressPaint.color = secondProgressColor

        littleCirclePaint.isAntiAlias = true
        littleCirclePaint.color = secondProgressColor

        progressPaint.isAntiAlias = true
        progressPaint.color = progressColor

        bgPaint.isDither = true //启用抗颜色抖动（可以让渐变更平缓）

        textPaint.color = textColor
        calculateNeedHeight()
    }

    private fun calculateNeedHeight() {
        var descNeedHeight = descMarginTop + descTextSize
        if (desc != null) {
            val descRawNum = ceil(desc!!.length / descRawLength).toInt()
            descNeedHeight =
                descMarginTop + descTextSize * descRawNum + descLineMargin * (descRawNum - 1)
        }
        needHeight = 2 * outerRadius + paddingTop + paddingBottom + descNeedHeight + 2 * drawMargin
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        //高的定制
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = measureSize(Math.round(needHeight + 0.5f), heightMeasureSpec)
        setMeasuredDimension(width, height)
    }

    private fun measureSize(defaultSize: Int, measureSpec: Int): Int {
        var result: Int = defaultSize
        val mode = MeasureSpec.getMode(measureSpec)
        val specSize = MeasureSpec.getSize(measureSpec)
        if (mode == MeasureSpec.EXACTLY) {
            result = specSize
        } else if (mode == MeasureSpec.AT_MOST) {
            result = defaultSize
        }
        return result
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        val centerX = width / 2.0f
        val centerY = if (needHeight > height) {
            outerRadius + paddingTop + drawMargin
        } else {
            (height - needHeight) / 2 + outerRadius + paddingTop + drawMargin
        }
        val drawCircleRadius = (outerRadius - innerRadius) / 2
        val circleRadius = (outerRadius + innerRadius) / 2
        //画起始点，顺时针方向
        val startAngle = (totalAngle + 180.0) / 2
        //求循环画圆的个数
        val moveAngle = Math.asin((drawCircleRadius / circleRadius).toDouble()).toAngle() / 3
        val drawNum = (totalAngle / moveAngle).toInt()
        //画背景
//        drawViewBg(canvas, centerX)
        //画进度背景
        drawProgressBg(
            drawCircleRadius,
            canvas,
            centerX,
            circleRadius,
            centerY,
            startAngle,
            drawNum,
            moveAngle
        )
        //画进度
        drawProgress(
            drawNum,
            startAngle,
            centerX,
            circleRadius,
            centerY,
            canvas,
            drawCircleRadius,
            moveAngle
        )
        //画内容
        drawText(canvas, centerX, centerY)
    }

    private fun drawViewBg(canvas: Canvas?, centerX: Float) {
        val sweepGradient = when {
            progress >= 76 -> LinearGradient(
                centerX,
                0f,
                centerX,
                height.toFloat(),
                colors4,
                null,
                Shader.TileMode.CLAMP
            )
            progress >= 51 -> LinearGradient(
                centerX,
                0f,
                centerX,
                height.toFloat(),
                colors3,
                null,
                Shader.TileMode.CLAMP
            )
            progress >= 26 -> LinearGradient(
                centerX,
                0f,
                centerX,
                height.toFloat(),
                colors2,
                null,
                Shader.TileMode.CLAMP
            )
            progress >= 0 -> LinearGradient(
                centerX,
                0f,
                centerX,
                height.toFloat(),
                colors1,
                null,
                Shader.TileMode.CLAMP
            )
            else -> LinearGradient(
                centerX,
                0f,
                centerX,
                height.toFloat(),
                colors1,
                null,
                Shader.TileMode.CLAMP
            )
        }
        bgPaint.shader = sweepGradient
        canvas?.drawPaint(bgPaint)
    }

    private fun drawProgress(
        drawNum: Int,
        startAngle: Double,
        centerX: Float,
        circleRadius: Float,
        centerY: Float,
        canvas: Canvas?,
        drawCircleRadius: Float,
        moveAngle: Double
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            progressPaint.style = Paint.Style.STROKE
            progressPaint.strokeWidth = drawCircleRadius * 2
            littleCirclePaint.color = progressPaint.color
            val angle = (360 - totalAngle) / 2.0f + 90
            val sweepAngle = totalAngle * ((progress * 1f) / maxProgress)
            canvas?.drawArc(
                centerX - circleRadius,
                centerY - circleRadius,
                centerX + circleRadius,
                centerY + circleRadius,
                angle - 0.5f,
                sweepAngle + 1f,
                false,
                progressPaint
            )

            val startCircleAngle: Float
            val endCircleAngle: Float
            if (totalAngle > 180) {
                startCircleAngle = 360 - (totalAngle - 180) / 2f
                endCircleAngle = startCircleAngle + sweepAngle + 180
            } else {
                startCircleAngle = (180 - totalAngle) / 2f
                endCircleAngle = startCircleAngle + sweepAngle + 180
            }
            var drawCX = centerX + circleRadius * Math.cos(startAngle.toRadians()).toFloat()
            var drawCY = centerY - circleRadius * Math.sin(startAngle.toRadians()).toFloat()
            canvas?.drawArc(
                drawCX - drawCircleRadius,
                drawCY - drawCircleRadius,
                drawCX + drawCircleRadius,
                drawCY + drawCircleRadius, startCircleAngle, 180f, false, littleCirclePaint
            )

            val endAngle = angle + sweepAngle + 0.0
            drawCX = centerX + circleRadius * Math.cos(endAngle.toRadians()).toFloat()
            drawCY = centerY + circleRadius * Math.sin(endAngle.toRadians()).toFloat()
            canvas?.drawArc(
                drawCX - drawCircleRadius,
                drawCY - drawCircleRadius,
                drawCX + drawCircleRadius,
                drawCY + drawCircleRadius, endCircleAngle, 180f, false, littleCirclePaint
            )

            //画 indicator
            val percent = drawCircleRadius / circleRadius.toDouble()
            val indicatorAngle = endAngle + 2 * Math.asin(percent).toAngle()
            val indicatorCX =
                centerX + circleRadius * Math.cos(indicatorAngle.toRadians()).toFloat()
            val indicatorCY =
                centerY + circleRadius * Math.sin(indicatorAngle.toRadians()).toFloat()
            canvas?.drawCircle(indicatorCX, indicatorCY, drawCircleRadius - 2, littleCirclePaint)
        } else {
            //显示动画效果（后期），所以这里分开步骤绘制
            //指示圆点移动位置个数(指示圆点也占进度位置)
            val indicatorMoveNum = 5
            //画进度
            val progressDrawNum = (((progress * 1.0f) / maxProgress) * drawNum).toInt()
            var progressDrawAngle: Double = startAngle
            for (i in 0 until (progressDrawNum - indicatorMoveNum)) {
                //画
                val drawCX =
                    centerX + circleRadius * Math.cos(progressDrawAngle.toRadians()).toFloat()
                val drawCY =
                    centerY - circleRadius * Math.sin(progressDrawAngle.toRadians()).toFloat()
                canvas?.drawCircle(drawCX, drawCY, drawCircleRadius, progressPaint)
                //更新角度
                progressDrawAngle -= moveAngle
            }
            //进度添加一个指示圆点
            if (progressDrawNum > indicatorMoveNum)
                progressDrawAngle -= indicatorMoveNum * moveAngle
            val drawCX = centerX + circleRadius * Math.cos(progressDrawAngle.toRadians()).toFloat()
            val drawCY = centerY - circleRadius * Math.sin(progressDrawAngle.toRadians()).toFloat()
            canvas?.drawCircle(drawCX, drawCY, drawCircleRadius - 2, progressPaint)
        }
    }

    private fun drawProgressBg(
        drawCircleRadius: Float,
        canvas: Canvas?,
        centerX: Float,
        circleRadius: Float,
        centerY: Float,
        startAngle: Double,
        drawNum: Int,
        moveAngle: Double
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            secondProgressPaint.style = Paint.Style.STROKE
            secondProgressPaint.strokeWidth = drawCircleRadius * 2
            littleCirclePaint.color = secondProgressPaint.color

            val angle = (360 - totalAngle) / 2.0f + 90
            canvas?.drawArc(
                centerX - circleRadius,
                centerY - circleRadius,
                centerX + circleRadius,
                centerY + circleRadius,
                angle,
                totalAngle.toFloat(),
                false,
                secondProgressPaint
            )

            val startCircleAngle: Float
            val endCircleAngle: Float
            if (totalAngle > 180) {
                endCircleAngle = (totalAngle - 180) / 2f
                startCircleAngle = 360 - endCircleAngle
            } else {
                startCircleAngle = (180 - totalAngle) / 2f
                endCircleAngle = 360 - startCircleAngle
            }
            var drawCX = centerX + circleRadius * Math.cos(startAngle.toRadians()).toFloat()
            var drawCY = centerY - circleRadius * Math.sin(startAngle.toRadians()).toFloat()
            canvas?.drawArc(
                drawCX - drawCircleRadius,
                drawCY - drawCircleRadius,
                drawCX + drawCircleRadius,
                drawCY + drawCircleRadius, startCircleAngle, 180f, false, littleCirclePaint
            )
            val endAngle = (180 - totalAngle) / 2.0
            drawCX = centerX + circleRadius * Math.cos(endAngle.toRadians()).toFloat()
            drawCY = centerY - circleRadius * Math.sin(endAngle.toRadians()).toFloat()
            canvas?.drawArc(
                drawCX - drawCircleRadius,
                drawCY - drawCircleRadius,
                drawCX + drawCircleRadius,
                drawCY + drawCircleRadius, endCircleAngle, 180f, false, littleCirclePaint
            )
        } else {
            // 小于 21 secondProgress的绘制，透明度支持效果不好，由于叠加的原因
            var drawAngle = startAngle
            for (i in 0..drawNum) {
                //画
                val drawCX = centerX + circleRadius * Math.cos(drawAngle.toRadians()).toFloat()
                val drawCY = centerY - circleRadius * Math.sin(drawAngle.toRadians()).toFloat()
                canvas?.drawCircle(drawCX, drawCY, drawCircleRadius, secondProgressPaint)
                //更新角度
                drawAngle -= moveAngle
            }
        }
    }

    private fun drawText(canvas: Canvas?, centerX: Float, centerY: Float) {
        //画文字内容
        //计算文字内容位置
        textPaint.textSize = progressTextSize
        val progressTextWidth = textPaint.measureText("$progress")
        textPaint.textSize = progressUnitTextSize
        val unitWidth = textPaint.measureText(progressUnit)
        val unitMargin = 10f
        val totalWidth = progressTextWidth + unitWidth + unitMargin
        textPaint.textSize = progressTextSize
        textPaint.color = progressTextColor
        canvas?.drawText("$progress", centerX - totalWidth / 2, centerY, textPaint)
        textPaint.textSize = progressUnitTextSize
        textPaint.color = progressUnitTextColor
        canvas?.drawText(
            progressUnit ?: "--",
            centerX - totalWidth / 2 + progressTextWidth + unitMargin,
            centerY,
            textPaint
        )
        //画附加内容
        textPaint.textSize = dateTextSize
        textPaint.color = dateTextColor
        val dateWidth = textPaint.measureText(dateContent)
        canvas?.drawText(
            dateContent ?: "--",
            centerX - dateWidth / 2,
            centerY + dateTextSize + dateMarginTop,
            textPaint
        )
        textPaint.textSize = locationTextSize
        textPaint.color = locationTextColor
        val positionWidth = textPaint.measureText(positionContent)
        val posVertical =
            centerY + dateTextSize + locationTextSize + dateMarginTop + locationMarginTop
        canvas?.drawText(
            positionContent ?: "--",
            centerX - positionWidth / 2,
            posVertical,
            textPaint
        )
        textPaint.textSize = descTextSize
        textPaint.color = descTextColor
        if (desc != null) {
            val descRawNum = ceil(desc!!.length / descRawLength).toInt()
            val vertical =
                centerY + outerRadius * Math.abs(Math.cos(((360 - totalAngle) / 2.0).toRadians())).toFloat()
            val marginTop = Math.max(vertical, posVertical) + descMarginTop
            for (i in 1..descRawNum) {
                val startIndex = ((i - 1) * descRawLength).toInt()
                var endIndex = (i * descRawLength).toInt()
                if (endIndex > desc!!.length) {
                    endIndex = desc!!.length
                }
                val drawDesc = desc!!.substring(startIndex, endIndex)
                val descWidth = textPaint.measureText(drawDesc)
                val descY = marginTop + descTextSize * i + if (i > 1) descLineMargin else 0f
                canvas?.drawText(drawDesc, centerX - descWidth / 2, descY, textPaint)
            }
        }
    }

    private fun Double.toRadians(): Double {
        return (this / 180) * Math.PI
    }

    private fun Double.toAngle(): Double {
        return (this / Math.PI) * 180
    }

    fun setProgress(progress: Int) {
        if (this.progress != progress) {
            this.progress = progress
            mLisenter?.onProgress(progress)
            postInvalidate()
        }
    }

    fun setInfo(progress: Int, date: String, location: String, desc: String) {
        this.progress = progress
        this.dateContent = date
        this.positionContent = location
        this.desc = desc
        requestLayout()
    }

    fun smoothToProgress(progress: Int) {
        this.progress = progress
        val valueAnimator = ValueAnimator.ofInt(0, progress)
        valueAnimator.duration = 2000
        valueAnimator.interpolator = LinearInterpolator()
        valueAnimator.addUpdateListener {
            setProgress(it.animatedValue as Int)
        }
        valueAnimator.start()
    }

    fun setProgressTextColor(color: Int) {
        progressTextColor = color
        invalidate()
    }

    fun getMaxProgress(): Int {
        return maxProgress
    }

    fun getProgress(): Int {
        return progress
    }

    fun setListener(listener: ProgressChangeListener) {
        mLisenter = listener
    }

    interface ProgressChangeListener {
        fun onProgress(progress: Int)
    }
}