package app.inspiry.views.timeline

import android.content.Context
import android.graphics.*
import android.util.TypedValue
import app.inspiry.R
import app.inspiry.utils.rotate


class TimelineFrameDrawer(val context: Context) {
    private val displayMetrics = context.resources.displayMetrics
    private val whiteFrameRound = 8F.toPixel()
    private val blackFrameStroke = 2F.toPixel()
    private val whiteFrameStroke = 2F.toPixel()
    private val blackFrameRound = 8F.toPixel()

    private val leftArrow = BitmapFactory.decodeResource(context.resources, ARROW_RES_ID)
    private val rightArrow = leftArrow.rotate(180F)

    private val frameArrowLocationX = -getArrowWidth()
    private val frameArrowLocationY = -leftArrow.height / 2

    fun getArrowWidth() = leftArrow.width + 1f.toPixel()

    private val whiteFramePaint = Paint().apply {
        isAntiAlias = true
        isFilterBitmap = true
        isDither = true
        color = Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = whiteFrameStroke
    }
    private val blackFramePaint = Paint(whiteFramePaint).apply {
        color = Color.BLACK
        style = Paint.Style.STROKE
        strokeWidth = blackFrameStroke
    }

    fun getStrokeWidth() = whiteFrameStroke

    private val whiteFrameLocation = PointF(-whiteFrameStroke / 2F, 0F)
    private val blackFrameLocation = PointF(
        -blackFrameStroke / 2F + whiteFrameStroke / 2f,
        blackFrameStroke / 2F + whiteFrameStroke / 2f
    )

    private fun Float.toPixel() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, this,
        displayMetrics
    )


    fun draw(
        canvas: Canvas,
        left: Float,
        top: Float,
        right: Float,
        bottom: Float,
        drawBlackFrame: Boolean, drawLeftArrow: Boolean, drawRightArrow: Boolean
    ) {

        fun drawRoundRect(location: PointF, round: Float, paint: Paint) {
            canvas.drawRoundRect(
                left + location.x, top + location.y,
                right - location.x, bottom - location.y, round, round, paint
            )
        }

        drawRoundRect(whiteFrameLocation, whiteFrameRound, whiteFramePaint)
        if (drawBlackFrame)
            drawRoundRect(blackFrameLocation, blackFrameRound, blackFramePaint)

        val frameArrowLocationY = (bottom - top) / 2F

        if (drawLeftArrow) {
            canvas.drawBitmap(
                leftArrow,
                left + frameArrowLocationX,
                frameArrowLocationY + this.frameArrowLocationY,
                null
            )
        }

        if (drawRightArrow)
            canvas.drawBitmap(
                rightArrow,
                right + 1f.toPixel(),
                frameArrowLocationY + this.frameArrowLocationY,
                null
            )
    }


    companion object {
        private const val ARROW_RES_ID = R.drawable.ic_arrow_timeline
    }
}