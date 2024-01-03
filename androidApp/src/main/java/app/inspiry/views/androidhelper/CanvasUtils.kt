package app.inspiry.views.androidhelper

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import app.inspiry.core.data.Rect
import app.inspiry.core.media.BaseUnitsConverter
import app.inspiry.core.media.BorderStyle
import app.inspiry.core.media.CornerRadiusPosition
import app.inspiry.core.util.InspMathUtil
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.math.min

class CanvasUtils() : KoinComponent {

    private val borderPath by lazy { Path() }
    private val borderRadius by lazy { floatArrayOf(0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f) }
    private var borderPaint: Paint? = null
    private val unitsConverter: BaseUnitsConverter by inject()

    private fun getBorderRadiusFilled(
        cornerRadius: Float,
        width: Int,
        height: Int,
        cornerRadiusPosition: CornerRadiusPosition?
    ): FloatArray {
        val borderRadius: Float = cornerRadius * min(width + 0.0, height + 0.0).toFloat() / 2.0f
        borderPath.reset()

        if (cornerRadiusPosition != CornerRadiusPosition.only_bottom) {
            this.borderRadius.fill(borderRadius, 0, 4)
        }
        if (cornerRadiusPosition != CornerRadiusPosition.only_top) {
            this.borderRadius.fill(borderRadius, 4, 8)
        }
        return this.borderRadius
    }

    fun drawBorder(
        canvas: Canvas,
        borderWidthString: String,
        borderType: BorderStyle,
        borderColor: Int,
        width: Int,
        height: Int,
        layoutParams: InspLayoutParams,
        cornerRadiusPosition: CornerRadiusPosition?,
        cornerRadius: Float,
        paddingStart: Int,
        paddingTop: Int,
        paddingBottom: Int,
        paddingEnd: Int
    ) {

        val borderWidth = unitsConverter.convertUnitToPixelsF(borderWidthString, width, height)
        if (borderPaint == null) {
            borderPaint = Paint()
            borderPaint!!.isAntiAlias = true
            borderPaint!!.style = Paint.Style.STROKE
        }
        borderPaint!!.color = borderColor
        borderPaint!!.strokeWidth = borderWidth
        borderPaint!!.strokeWidth = borderWidth

        val border = Rect(0, 0, width, height)

        if (layoutParams.layoutPosition.isInWrapContentMode())
        //setting the correct size for the size animator in wrap content mode
            InspMathUtil.scaleAndCentering(
                rect = border,
                factorX = layoutParams.widthFactor,
                factorY = layoutParams.heightFactor
            )

        val halfBorderWidth = borderWidth / 2f

        //for some reason visually radius differs from image
        val borderRadius =
            getBorderRadiusFilled(cornerRadius, width, height, cornerRadiusPosition)
        for ((index, r) in borderRadius.withIndex()) {
            borderRadius[index] = r / 1.04f
        }

        when (borderType) {
            BorderStyle.outside -> {
                borderPath.addRoundRect(
                    halfBorderWidth + border.left,
                    halfBorderWidth + border.top,
                    border.right - halfBorderWidth,
                    border.bottom - halfBorderWidth,
                    borderRadius,
                    Path.Direction.CW
                )

            }
            BorderStyle.inside -> {

                borderPath.addRoundRect(
                    halfBorderWidth + border.left + paddingStart,
                    halfBorderWidth + border.top + paddingTop,
                    border.right - paddingEnd - halfBorderWidth,
                    border.bottom - paddingBottom - halfBorderWidth,
                    borderRadius,
                    Path.Direction.CW
                )

            }
        }

        canvas.drawPath(borderPath, borderPaint!!)

    }
}