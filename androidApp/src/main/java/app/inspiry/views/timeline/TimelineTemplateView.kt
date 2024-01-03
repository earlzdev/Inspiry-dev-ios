package app.inspiry.views.timeline

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import android.widget.TextView
import app.inspiry.core.data.FRAME_IN_MILLIS
import app.inspiry.dialog.TimelinePanel
import app.inspiry.utils.dpToPixels
import app.inspiry.utils.dpToPxInt
import app.inspiry.views.template.InspTemplateView
import kotlin.math.max
import kotlin.math.min

@SuppressLint("ViewConstructor")
class TimelineTemplateView(context: Context, val templateView: InspTemplateView) : FrameLayout(context), TimelineTouchableView {

    override var startTime: Double = 0.0

    override var duration: Double = templateView.getDuration() * FRAME_IN_MILLIS
        set(value) {
            val (minDuration, maxDuration) = templateView.setNewDuration((value / FRAME_IN_MILLIS).toInt())

            field = min(max(minDuration * FRAME_IN_MILLIS, value), maxDuration * FRAME_IN_MILLIS)
            innerTextView.text = TimelinePanel.millisToTimer(field)
        }

    override var minDuration = 0.0
    override val view: View
        get() = this


    private val innerTextView: TextView
    private val innerBitmapsView: SeriesOfImagesView

    override val frameDrawer = TimelineFrameDrawer(context)
    override var inMovementNearEdge: Boolean? = null
    override var vibratedOnTouch: Int = 0
    override var vibrationDelta: Float = 0f
    override var lastMoveX: Float = 0f
    override var lastMoveY: Float = 0f
    override var startedTouchArrow = 0


    init {

        clipChildren = false
        setWillNotDraw(false)

        innerBitmapsView = SeriesOfImagesView(context)
        innerBitmapsView.roundedCorners = 8f.dpToPixels()
        addView(innerBitmapsView, MATCH_PARENT, MATCH_PARENT)

        innerTextView = TextView(context)
        innerTextView.textSize = 10.5f
        innerTextView.setTextColor(Color.WHITE)
        innerTextView.setPadding(8.dpToPxInt(), 3.dpToPxInt(), 8.dpToPxInt(), 3.dpToPxInt())
        innerTextView.background = 0xcc4f4f4f.toInt().createRectWithRoundedCorners(3.dpToPixels())
        innerTextView.typeface = Typeface.DEFAULT_BOLD

        addView(innerTextView, LayoutParams(WRAP_CONTENT, WRAP_CONTENT).also {
            it.gravity = Gravity.CENTER_VERTICAL
            it.marginStart = 11.dpToPxInt()
            it.marginEnd = 11.dpToPxInt()
        })

        setOnClickListener {
            getTimeline().setSelectedView(this)
        }

        val offset = TimelineTextView.additionalTouchArrowStart + frameDrawer.getArrowWidth()
        setPadding(offset.toInt(), 0, offset.toInt(), 0)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        innerTextView.text = TimelinePanel.millisToTimer(duration)
    }

    override fun setWidthAndX() {
        setWidthAndX(
            getTimeline().offsetForContent,
            frameDrawer.getArrowWidth() + TimelineTextView.additionalTouchArrowStart
        )
    }

    override fun setCurrentTime(time: Double) {
        super.setCurrentTime(time)
        setWidthAndX()

        val timeline = getTimeline()
        val textTranslations = max(timeline.scrollX - timeline.width / 2f, 0f)

        val maxTextTranslation = timeline.calcWidthOfViews().toFloat() - (innerTextView.width +
                (innerTextView.layoutParams as MarginLayoutParams).marginEnd)

        innerTextView.translationX = min(textTranslations, maxTextTranslation)
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        if (isSelectedView()) {

            frameDrawer.draw(
                canvas, view.paddingLeft.toFloat(), 0f,
                view.width.toFloat() - view.paddingRight.toFloat(),
                view.height.toFloat(), false, false, true
            )
        }
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        return true
    }

    override fun Int.touchedArrow(): Boolean {
        return if (DISPLAY_LEFT_ARROW) {
            this != 0
        } else this == 1
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {

        if (isSelectedView()) {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    startedTouchArrow = isTouchArrow(event, frameDrawer)
                    lastMoveX = event.rawX
                    if (startedTouchArrow.touchedArrow()) {
                        parent.requestDisallowInterceptTouchEvent(true)
                        getTimeline().resetStartFrame()
                    }
                }
                MotionEvent.ACTION_MOVE -> {
                    if (startedTouchArrow.touchedArrow()) stretchView(
                        startedTouchArrow,
                        lastMoveX - event.rawX, false
                    )
                    lastMoveX = event.rawX
                }

                MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> {
                    onTouchUpCancel(event)
                    val handled = startedTouchArrow.touchedArrow()
                    startedTouchArrow = 0
                    return handled
                }
            }


            return startedTouchArrow.touchedArrow()
        }

        return super.onTouchEvent(event)
    }

    override fun isOutOfSightStartForRightArrow(containsWidths: Double): Boolean {

        return getTimeline().scrollX > containsWidths
    }

    override fun mayVibrate(
        durationDiff: Double,
        startTimeDiff: Double,
        movingArrow: Int,
        deltaX: Float
    ) {
        super.mayVibrate(durationDiff, startTimeDiff, movingArrow, deltaX)

        val moveLeft = deltaX > 0
        if (durationDiff == 0.0) {
            if ((duration == minDuration && moveLeft) || (duration == ((templateView.template.maxDuration
                    ?: -1) * FRAME_IN_MILLIS) && !moveLeft)
            )
                vibrate(deltaX)
        }
    }

    private fun stretchView(arrow: Int, deltaX: Float, recursively: Boolean) {
        val seekTime = getSeekTime(deltaX)

        val oldDuration = duration

        if (arrow == -1) {
            duration += seekTime

        } else {
            duration -= seekTime
        }

        val deltaDuration = duration - oldDuration

        if (deltaDuration != 0.0) {

            val timeline = getTimeline()
            timeline.checkTextViewsDurations()

            timeline.insideViews.filterIsInstance(TimelineTextView::class.java).forEach {
                it.refreshDuration()
                it.setWidthAndX()
            }
        }

        checkOutOfSightAndMove(oldDuration, 0.0, recursively, arrow, deltaX) {
            stretchView(
                arrow,
                it,
                true
            )
        }

    }

    companion object {
        const val DISPLAY_LEFT_ARROW = false
    }
}
fun Int.createRectWithRoundedCorners(rounding: Float): GradientDrawable {
    val shape = GradientDrawable()
    shape.cornerRadius = rounding
    shape.setColor(this)
    return shape
}