package app.inspiry.animator

import android.graphics.*
import app.inspiry.core.animator.text.DrawTextAnimParam
import app.inspiry.core.data.Rect
import app.inspiry.core.util.ArgbColorManager
import kotlin.math.min

class DrawTextAnimParamAndroid : DrawTextAnimParam() {

    var xfermode: Xfermode? = null
    var clipPath: Path? = null
    var shader: Shader? = null

    override fun nullify() {
        super.nullify()
        xfermode = null
        shader = null
    }

    override fun copy(
        copied: DrawTextAnimParam,
        shadowLeftOffset: Int,
        shadowTopOffset: Int
    ): DrawTextAnimParam {

        super.copy(copied, shadowLeftOffset, shadowTopOffset)
        copied as DrawTextAnimParamAndroid
        copied.xfermode = xfermode
        copied.shader = shader
        copied.clipPath = clipPath
        return copied
    }

    private fun applyCircle(canvas: Canvas): Float {

        val charX = width / 2f + left - textPositionStart
        val progress = charX / textFullWidth
        canvas.rotate(
            360 * progress / circularLengthFactor + angleShift,
            canvas.width / 2f,
            canvas.height / 2f
        )
        canvas.translate(0f, -top + height / 2f)

        return canvas.width / 2f - width / 2f
    }

    fun applyToCanvas(canvas: Canvas, paint: Paint, tempRect: Rect) {
        canvas.save()
        if (blurRadius > 0) {
            paint.maskFilter =
                BlurMaskFilter(paint.textSize * blurRadius, BlurMaskFilter.Blur.NORMAL)
        } else {
            paint.maskFilter = null
        }
        paint.xfermode = xfermode
        val leftpos = if (circleRadius != 0f) applyCircle(canvas) else left
        val oldAlphaFloat = ArgbColorManager.alpha(color) / 255f
        paint.color = ArgbColorManager.applyAlphaToColor(color, alpha * oldAlphaFloat)
        canvas.translate(left + (width / 2).toFloat(), top)
        canvas.rotate(rotate)
        canvas.scale(scaleX, scaleY)
        canvas.translate(-(left + (width / 2).toFloat()), -top)

        if (clipRect != null || cornersRadius != 0f) {
            val top = lineTop + paddingTop

            var percentOfClip = (clipRect!!.width() / width.toFloat())
            percentOfClip = min(percentOfClip * 20, 1f)

            val additionalPadToPreventClip = height * 0.1f * percentOfClip

            tempRect.set(
                clipRect!!.left + (left - additionalPadToPreventClip).toInt(),
                clipRect!!.top + top,
                clipRect!!.right + (left + additionalPadToPreventClip).toInt(),
                clipRect!!.bottom + top
            )

            if (cornersRadius != 0f) {
                if (clipPath == null) {
                    clipPath = Path()
                } else {
                    clipPath!!.reset()
                }
                val absoluteRadius = cornersRadius * (width + height) / 2f

                clipPath!!.addRoundRect(
                    tempRect.left.toFloat(),
                    tempRect.top.toFloat(),
                    tempRect.right.toFloat(),
                    tempRect.bottom.toFloat(),
                    absoluteRadius,
                    absoluteRadius,
                    Path.Direction.CW
                )
                canvas.clipPath(clipPath!!)


            } else {
                canvas.clipRect(tempRect)
            }

        }

        canvas.translate(translateX, translateY)
        paint.shader = shader
        canvas.drawTextRun(
            charSequence,
            startTextToRender,
            startTextToRender + lengthTextToRender,
            0,
            charSequence.length,
            leftpos,
            top,
            isRtl,
            paint
        )
        canvas.restore()
    }

}