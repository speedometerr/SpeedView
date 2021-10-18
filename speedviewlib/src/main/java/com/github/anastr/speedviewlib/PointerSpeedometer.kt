package com.github.anastr.speedviewlib

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import com.github.anastr.speedviewlib.components.Style
import com.github.anastr.speedviewlib.components.indicators.SpindleIndicator
import com.github.anastr.speedviewlib.util.getRoundAngle
import java.lang.Float.min

/**
 * this Library build By Anas Altair
 * see it on [GitHub](https://github.com/anastr/SpeedView)
 */
open class PointerSpeedometer @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0,
) : Speedometer(context, attrs, defStyleAttr) {

    private val speedometerPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val overSpeedometerPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val pointerPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val pointerBackPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val circlePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val speedometerRect = RectF()

    private var speedometerColor = 0xFF37872F.toInt()
    private var overSpeedometerColor = 0xFF9B2020.toInt()
    private var pointerColor = 0xFFFFFFFF.toInt()

    private var withPointer = false
    private var _recommendedSpeed = 0f

    var recommendedSpeed: Float
        get() = _recommendedSpeed
        set(recommendedSpeed) {
            _recommendedSpeed = recommendedSpeed
            if (isAttachedToWindow)
                invalidate()
        }

    /**
     * change the color of the center circle.
     */
    var centerCircleColor: Int
        get() = circlePaint.color
        set(centerCircleColor) {
            circlePaint.color = centerCircleColor
            if (isAttachedToWindow)
                invalidate()
        }

    /**
     * change the width of the center circle.
     */
    var centerCircleRadius = dpTOpx(12f)
        set(centerCircleRadius) {
            field = centerCircleRadius
            if (isAttachedToWindow)
                invalidate()
        }

    /**
     * enable to draw circle pointer on speedometer arc.
     *
     * this will not make any change for the Indicator.
     *
     * true: draw the pointer,
     * false: don't draw the pointer.
     */
    var isWithPointer: Boolean
        get() = withPointer
        set(withPointer) {
            this.withPointer = withPointer
            if (isAttachedToWindow)
                 invalidate()
        }

    init {
        init()
        initAttributeSet(context, attrs)
    }

    override fun defaultGaugeValues() {
        super.speedometerWidth = dpTOpx(10f)
        super.textColor = 0xFFFFFFFF.toInt()
        super.speedTextColor = 0xFFFFFFFF.toInt()
        super.unitTextColor = 0xFFFFFFFF.toInt()
        super.speedTextSize = dpTOpx(24f)
        super.unitTextSize = dpTOpx(11f)
        super.speedTextTypeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }

    override fun defaultSpeedometerValues() {
        super.marksNumber = 8
        super.marksPadding = speedometerWidth + dpTOpx(12f)
        super.markStyle = Style.ROUND
        super.markHeight = dpTOpx(5f)
        super.markWidth = dpTOpx(2f)
        indicator = SpindleIndicator(context)
        indicator.apply {
            width = dpTOpx(16f)
            color = 0xFFFFFFFF.toInt()
        }
        super.backgroundCircleColor = 0xff48cce9.toInt()
    }

    private fun init() {
        speedometerPaint.style = Paint.Style.STROKE
        speedometerPaint.strokeCap = Paint.Cap.BUTT
        overSpeedometerPaint.style = Paint.Style.STROKE
        overSpeedometerPaint.strokeCap = Paint.Cap.BUTT
        circlePaint.color = 0xFFFFFFFF.toInt()
    }

    private fun initAttributeSet(context: Context, attrs: AttributeSet?) {
        if (attrs == null) {
            initAttributeValue()
            return
        }
        val a = context.theme.obtainStyledAttributes(attrs, R.styleable.PointerSpeedometer, 0, 0)

        speedometerColor = a.getColor(R.styleable.PointerSpeedometer_sv_speedometerColor, speedometerColor)
        overSpeedometerColor = a.getColor(R.styleable.PointerSpeedometer_sv_overSpeedometerColor, overSpeedometerColor)
        pointerColor = a.getColor(R.styleable.PointerSpeedometer_sv_pointerColor, pointerColor)
        circlePaint.color = a.getColor(R.styleable.PointerSpeedometer_sv_centerCircleColor, circlePaint.color)
        centerCircleRadius = a.getDimension(R.styleable.SpeedView_sv_centerCircleRadius, centerCircleRadius)
        withPointer = a.getBoolean(R.styleable.PointerSpeedometer_sv_withPointer, withPointer)
        a.recycle()
        initAttributeValue()
    }

    private fun initAttributeValue() {
        pointerPaint.color = pointerColor
    }


    override fun onSizeChanged(w: Int, h: Int, oldW: Int, oldH: Int) {
        super.onSizeChanged(w, h, oldW, oldH)

        val risk = speedometerWidth * .5f + dpTOpx(8f) + padding.toFloat()
        speedometerRect.set(risk, risk, size - risk, size - risk)

        updateRadial()
        updateBackgroundBitmap()
    }

    private fun initDraw() {
        speedometerPaint.strokeWidth = speedometerWidth
        speedometerPaint.color = speedometerColor
        overSpeedometerPaint.strokeWidth = speedometerWidth
        overSpeedometerPaint.color = overSpeedometerColor
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        initDraw()

        val recommendedDegree = getDegreeAtSpeed(minOf(recommendedSpeed, maxSpeed))
        var sweepAngle = (recommendedDegree - getStartDegree())
        canvas.drawArc(speedometerRect, getStartDegree().toFloat(), sweepAngle, false, speedometerPaint)
        if(recommendedSpeed > 0 && currentSpeed > recommendedSpeed) {
            sweepAngle = (getEndDegree() - getStartDegree()) * getOffsetSpeed()  - (recommendedDegree - getStartDegree())
            canvas.drawArc(speedometerRect, recommendedDegree, sweepAngle, false, overSpeedometerPaint)
        }

        drawMarks(canvas)

        if (withPointer) {
            canvas.save()
            canvas.rotate(90 + degree, size * .5f, size * .5f)
            canvas.drawCircle(size * .5f, speedometerWidth * .5f + dpTOpx(8f) + padding.toFloat(), speedometerWidth * .5f + dpTOpx(8f), pointerBackPaint)
            canvas.drawCircle(size * .5f, speedometerWidth * .5f + dpTOpx(8f) + padding.toFloat(), speedometerWidth * .5f + dpTOpx(1f), pointerPaint)
            canvas.restore()
        }

        drawSpeedUnitText(canvas)
        drawIndicator(canvas)

        val c = centerCircleColor
        circlePaint.color = Color.argb((Color.alpha(c) * .5f).toInt(), Color.red(c), Color.green(c), Color.blue(c))
        canvas.drawCircle(size * .5f, size * .5f, centerCircleRadius + dpTOpx(6f), circlePaint)
        circlePaint.color = c
        canvas.drawCircle(size * .5f, size * .5f, centerCircleRadius, circlePaint)

        drawNotes(canvas)
    }

    override fun updateBackgroundBitmap() {
        val c = createBackgroundBitmapCanvas()
        initDraw()

        if (tickNumber > 0)
            drawTicks(c)
        else
            drawDefMinMaxSpeedPosition(c)
    }

    private fun updateRadial() {
        val centerColor = Color.argb(160, Color.red(pointerColor), Color.green(pointerColor), Color.blue(pointerColor))
        val edgeColor = Color.argb(10, Color.red(pointerColor), Color.green(pointerColor), Color.blue(pointerColor))
        val pointerGradient = RadialGradient(size * .5f, speedometerWidth * .5f + dpTOpx(8f) + padding.toFloat(), speedometerWidth * .5f + dpTOpx(8f), intArrayOf(centerColor, edgeColor), floatArrayOf(.4f, 1f), Shader.TileMode.CLAMP)
        pointerBackPaint.shader = pointerGradient
    }

    fun getSpeedometerColor(): Int {
        return speedometerColor
    }

    fun setSpeedometerColor(speedometerColor: Int) {
        this.speedometerColor = speedometerColor
        if (isAttachedToWindow)
            invalidate()
    }

    fun getPointerColor(): Int {
        return pointerColor
    }

    fun setPointerColor(pointerColor: Int) {
        this.pointerColor = pointerColor
        pointerPaint.color = pointerColor
        updateRadial()
        if (isAttachedToWindow)
            invalidate()
    }
}
