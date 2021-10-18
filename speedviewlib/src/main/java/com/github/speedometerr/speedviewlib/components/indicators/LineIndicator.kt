package com.github.speedometerr.speedviewlib.components.indicators

import android.content.Context
import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path

/**
 * this Library build By Anas Altair
 * see it on [GitHub](https://github.com/anastr/SpeedView)
 */
class LineIndicator(context: Context, private val length: Float) : Indicator<LineIndicator>(context) {

    private val indicatorPath = Path()

    init {
        require(length in 0f..1f) { "Length must be between [0,1]." }
        width = dpTOpx(8f)
    }

    override fun getBottom(): Float {
        return getCenterY() * length
    }

    override fun draw(canvas: Canvas, degree: Float) {
        canvas.save()
        canvas.rotate(90f + degree, getCenterX(), getCenterY())
        canvas.drawPath(indicatorPath, indicatorPaint)
        canvas.restore()
    }

    override fun updateIndicator() {
        indicatorPath.reset()
        indicatorPath.moveTo(getCenterX(), speedometer!!.padding.toFloat())
        indicatorPath.lineTo(getCenterX(), getCenterY() * length)

        indicatorPaint.style = Paint.Style.STROKE
        indicatorPaint.strokeWidth = width
        indicatorPaint.color = color
    }

    override fun setWithEffects(withEffects: Boolean) {
        if (withEffects && !speedometer!!.isInEditMode) {
            indicatorPaint.maskFilter = BlurMaskFilter(15f, BlurMaskFilter.Blur.SOLID)
        } else {
            indicatorPaint.maskFilter = null
        }
    }
}
