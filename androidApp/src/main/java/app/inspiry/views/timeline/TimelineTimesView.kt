package app.inspiry.views.timeline

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.view.View
import app.inspiry.dialog.TimelinePanel
import app.inspiry.utils.dpToPixels

class TimelineTimesView(context: Context) : View(context), TimelineInsideView {

    private val supposedTextWith = 20.dpToPixels()
    private val circleY = 8.dpToPixels()
    private val textY = 11.dpToPixels()
    private val textPaint = Paint().also {
        it.color = 0xff828282.toInt()
        it.textSize = 8.dpToPixels()
        it.style = Paint.Style.FILL
    }
    private val circleRadius = 1.dpToPixels()
    override var startTime: Double = 0.0

    override var duration: Double
        get() = getTimeline().duration
        set(value) {}

    override var minDuration: Double = 0.0
    override val view: View
        get() = this


    private fun calcTextX(num: Int, widthForOneSection: Float) =
        -supposedTextWith / 2 + (widthForOneSection * num)

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (parent != null) {
            val timeline = getTimeline()

            val thisWidth = timeline.calcWidthOfViews()
            val completeWidth = thisWidth + timeline.width / 2f + supposedTextWith / 2f

            val widthForOneSection = (thisWidth / (duration / 1000.0)).toFloat()

            val numberOfTexts = (completeWidth / widthForOneSection).toInt()


            (0..numberOfTexts + 1).forEach {

                val textX = calcTextX(it, widthForOneSection)
                canvas.drawText(TimelinePanel.millisToTimer(it * 1000.0), textX, textY, textPaint)

                val textNextX = calcTextX(it + 1, widthForOneSection)

                val circleX = (textNextX - textX) / 2 + textX + supposedTextWith / 2

                // TODO: anr here is reported. Explore why.
                canvas.drawCircle(
                    circleX,
                    circleY,
                    circleRadius,
                    textPaint
                )
            }

        }
    }
}