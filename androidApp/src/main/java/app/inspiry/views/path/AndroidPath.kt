package app.inspiry.views.path

import android.graphics.*
import app.inspiry.core.log.GlobalLogger
import app.inspiry.core.media.PaintStyle
import app.inspiry.media.toJava
import app.inspiry.palette.model.PaletteLinearGradient
import kotlin.math.roundToInt

class AndroidPath: CommonPath() {

    val path = Path()
    val pathPaint: Paint = Paint().also { it.isAntiAlias = true }

    override fun moveTo(x: Float, y: Float) {
        path.moveTo(x, y)
    }

    override fun lineTo(x: Float, y: Float) {
        path.lineTo(x, y)
    }

    override fun quadTo(x: Float, y: Float, x2: Float, y2: Float) {
        path.quadTo(x, y, x2, y2)
    }

    override fun close() {
        path.close()
    }

    override fun reset() {
        path.reset()
    }

    override fun refreshGradient(gradient: PaletteLinearGradient?, width: Int, height: Int) {

        if (gradient != null) {
            val (x0, x1, y0, y1) = gradient.getShaderCoords(
                0f,
                0f,
                width.toFloat(),
                height.toFloat()
            )
            pathPaint.shader =
                LinearGradient(
                    x0,
                    y0,
                    x1,
                    y1,
                    gradient.colors.toIntArray(),
                    gradient.offsets,
                    Shader.TileMode.CLAMP
                )
        } else {
            pathPaint.shader = null
        }
    }

    override fun setPathCornerRadius(absoluteRadius: Float) {
        pathPaint.pathEffect = CornerPathEffect(absoluteRadius)
    }

    override fun refreshPathColor(color: Int) {
        pathPaint.color = color
    }

    override fun setStrokeWidth(strokeWidth: Float) {
        pathPaint.strokeWidth = strokeWidth
    }

    override fun refreshStyle(
        color: Int?,
        alpha: Float,
        strokeCap: String?,
        paintStyle: PaintStyle
    ) {
        if (color != null) {
            pathPaint.color = color
            pathPaint.alpha = (alpha * 255).roundToInt()
            pathPaint.style = paintStyle.toJava()
        }

        if (strokeCap != null)
            pathPaint.strokeCap = Paint.Cap.valueOf(strokeCap)
    }

    override fun updateFillType(inverse: Boolean) {
        path.fillType = if (inverse) Path.FillType.INVERSE_WINDING
        else Path.FillType.WINDING
    }

    override fun isEmpty(): Boolean {
        return path.isEmpty
    }

    override fun addRoundRect(
        left: Float,
        top: Float,
        right: Float,
        bottom: Float,
        rx: Float,
        ry: Float
    ) {

        path.addRoundRect(
            left,
            top,
            right,
            bottom,
            rx,
            ry,
            Path.Direction.CW
        )
    }

    override fun addOval(left: Float, right: Float, top: Float, bottom: Float) {
        path.addOval(left, top, right, bottom, Path.Direction.CW)
    }

    override fun addCircle(centerX: Float, centerY: Float, radius: Float) {
        path.addCircle(
            centerX,
            centerY,
            radius,
            Path.Direction.CW
        )
    }
}