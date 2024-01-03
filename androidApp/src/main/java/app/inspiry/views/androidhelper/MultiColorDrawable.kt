package app.inspiry.views.androidhelper

import android.graphics.*
import android.graphics.drawable.Drawable
import androidx.core.graphics.withClip
import app.inspiry.utils.dpToPixels

class MultiColorDrawable(val colors: List<Int>) : Drawable() {

    val paint = Paint().also { it.isAntiAlias = true }
    private val clipPath = Path()
    var isActivatedDrawn = false

    override fun draw(canvas: Canvas) {

        isActivatedDrawn = state.any { it == android.R.attr.state_activated }

        val outsideBorder = 2f.dpToPixels()
        val paddingSide = 0f//1.5f.dpToPixels()

        val bounds = bounds

        if (isActivatedDrawn) {
            paint.color = Color.WHITE
            paint.strokeWidth = 0f
            paint.style = Paint.Style.FILL

            val roundingRadius = (bounds.bottom - bounds.top - paddingSide * 2) / 3.6f
            canvas.drawRoundRect(bounds.left.toFloat() + paddingSide, bounds.top.toFloat(), bounds.right.toFloat() - paddingSide,
                bounds.bottom.toFloat(), roundingRadius, roundingRadius, paint)
        }


        val right = bounds.right - paddingSide - outsideBorder
        val bottom = bounds.bottom - outsideBorder

        val left = bounds.left + paddingSide + outsideBorder
        val top = bounds.top + outsideBorder

        val roundingRadius = (bottom - top) / 3.6f
        clipPath.reset()
        clipPath.addRoundRect(paddingSide + outsideBorder, outsideBorder,
            right, bottom, roundingRadius, roundingRadius, Path.Direction.CW)

        paint.strokeWidth = 0f
        paint.style = Paint.Style.FILL
        canvas.withClip(clipPath) {
            val barHeight = bottom / colors.size.toFloat()

            for ((index, color) in colors.withIndex()) {
                paint.color = color
                canvas.drawRect(left, barHeight * index + top, right,
                    barHeight * (index + 1) + top, paint)
            }
        }

        if (isActivatedDrawn) {
            paint.color = Color.BLACK
            val strokeWidth = 1.dpToPixels()
            paint.strokeWidth = strokeWidth
            paint.style = Paint.Style.STROKE
            canvas.drawRoundRect(left + strokeWidth / 2f, top + strokeWidth / 2f, right - strokeWidth / 2f, bottom - strokeWidth / 2f, roundingRadius, roundingRadius, paint)
        }
    }

    override fun onStateChange(state: IntArray): Boolean {
        return state.any { it == android.R.attr.state_activated } xor isActivatedDrawn
    }

    override fun isStateful(): Boolean {
        return true
    }

    override fun setAlpha(alpha: Int) {
        paint.alpha = alpha
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        paint.colorFilter = colorFilter
    }

    override fun getOpacity(): Int {
        return PixelFormat.OPAQUE
    }
}