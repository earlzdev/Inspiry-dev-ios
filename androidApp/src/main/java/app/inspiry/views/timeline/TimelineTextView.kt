package app.inspiry.views.timeline

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.text.StaticLayout
import android.text.TextPaint
import android.text.TextUtils
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import androidx.core.graphics.withTranslation
import app.inspiry.core.data.FRAME_IN_MILLIS
import app.inspiry.core.media.MediaText
import app.inspiry.utils.dpToPixels
import app.inspiry.views.text.InspTextView
import app.inspiry.views.timeline.TimelineView.Companion.ARROW_WIDTH
import app.inspiry.views.viewplatform.getAndroidView
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min


@SuppressLint("ViewConstructor")
class TimelineTextView(context: Context, var mediaText: MediaText) : View(context), TimelineTouchableView {

    private val textMargin = 11.dpToPixels()
    private val textY = 8.dpToPixels()
    private val bgRounding = 8.dpToPixels()
    private val bgPaint = Paint().also {
        it.color = 0xffC4C4C4.toInt()
    }
    private val textPaint = TextPaint().also {
        it.color = 0xff303030.toInt()
        it.textSize = 11.dpToPixels()
        it.style = Paint.Style.FILL
    }
    override var startedTouchArrow: Int = 0
    override var lastMoveX: Float = 0f
    override var lastMoveY: Float = 0f
    override val frameDrawer: TimelineFrameDrawer = TimelineFrameDrawer(context)
    override var inMovementNearEdge: Boolean? = null
    override var vibratedOnTouch: Int = 0
    override var vibrationDelta: Float = 0f

    private var mTextLayout: StaticLayout? = null

    override var startTime: Double = mediaText.view!!.getStartFrameShortCut() * FRAME_IN_MILLIS
        set(value) {
            field = value
            mediaText.startFrame = (value / FRAME_IN_MILLIS).toInt()
            (mediaText.view as? InspTextView)?.doForDuplicates {
                it.media.startFrame = (value / FRAME_IN_MILLIS).toInt()
            }

        }

    override var duration: Double = 0.0

    //this code is necessary because we need to keep track of duration with higher precision.
    fun setNewTextDuration(value: Double) {
        val (_, maxDuration, minDuration) = (mediaText.view as InspTextView)
            .setTextDuration((value / FRAME_IN_MILLIS).toInt())

        duration = min(max(value, minDuration * FRAME_IN_MILLIS), maxDuration * FRAME_IN_MILLIS)
    }

    fun refreshDuration() {
        duration = mediaText.view!!.duration * FRAME_IN_MILLIS
    }

    override var minDuration: Double
        set(value) {}
        get() = mediaText.view!!.getMinPossibleDuration(true) * FRAME_IN_MILLIS


    override val view: View
        get() = this

    var lastPreDoubleTap = 0L

    init {
        isFocusable = true

        setOnClickListener {
            getTimeline().setSelectedView(this)
        }
        refreshDuration()
    }

    private fun calibrateStartTime(startTime: Double): Double {
        val maxStartTime = getTimeline().duration - duration
        return max(0.0, min(startTime, maxStartTime))
    }

    override fun setCurrentTime(time: Double) {
        super.setCurrentTime(time)
        setWidthAndX()
    }

    override fun onNewSizeSetImmediately(width: Int, height: Int) {

        clipBounds = Rect(
            -ARROW_WIDTH * 2,
            -(frameDrawer.getStrokeWidth() / 2).toInt(),
            width + ARROW_WIDTH * 2,
            height + (frameDrawer.getStrokeWidth() / 2).toInt()
        )
    }

    override fun setWidthAndX() {
        setWidthAndX(
            getTimeline().offsetForContentText,
            frameDrawer.getArrowWidth() + additionalTouchArrowStart
        )
    }

    private fun createTextLayout(textWidth: Int) = StaticLayout.Builder.obtain(
        mediaText.text,
        0, mediaText.text.length, textPaint, textWidth
    )
        .setEllipsize(TextUtils.TruncateAt.END).setMaxLines(1).build()

    private fun mayRefreshTextLayout(textWidth: Int) {
        if (textWidth <= 0) return

        if (mTextLayout == null || textWidth != mTextLayout!!.width || mediaText.text != mTextLayout!!.text.toString()) {
            mTextLayout = createTextLayout(textWidth)
        }
    }

    override fun checkOutOfSightAndMove(
        oldDuration: Double,
        oldStartTime: Double,
        recursively: Boolean,
        movingArrow: Int, originalDeltaX: Float,
        recursiveMethod: (Float) -> Unit
    ) {

        (mediaText.view as InspTextView).changeStartTimeDurationForGroup(
            startTime - oldStartTime, duration - oldDuration
        )


        super.checkOutOfSightAndMove(
            oldDuration,
            oldStartTime,
            recursively,
            movingArrow, originalDeltaX,
            recursiveMethod
        )
    }

    private val mTouchSlop = ViewConfiguration.get(context).scaledTouchSlop
    var inMoveMode = false
    val doubleTapTimeout = ViewConfiguration.getDoubleTapTimeout()

    override fun onTouchEvent(event: MotionEvent): Boolean {

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                lastMoveX = event.rawX
                lastMoveY = event.rawY
                startedTouchArrow = isTouchArrow(event, frameDrawer)

                if (startedTouchArrow == 0 && isSelectedView()) {
                    if ((System.currentTimeMillis() - lastPreDoubleTap) < doubleTapTimeout)
                        mediaText.view?.getAndroidView()?.performClick()
                    else
                        lastPreDoubleTap = System.currentTimeMillis()
                }

                return if (startedTouchArrow != 0 && !isSelectedView()) {
                    view.parent?.requestDisallowInterceptTouchEvent(false)
                    false
                } else {

                    if (startedTouchArrow != 0) getTimeline().resetStartFrame()
                    view.parent?.requestDisallowInterceptTouchEvent(true)
                    true
                }
            }
            MotionEvent.ACTION_MOVE -> {
                val deltaX = lastMoveX - event.rawX
                val deltaY = lastMoveY - event.rawY

                if (startedTouchArrow.touchedArrow()) {
                    if (isSelectedView()) {
                        stretchView(startedTouchArrow, deltaX, false)
                        lastMoveX = event.rawX
                        return true

                    } else {
                        return false
                    }

                } else if (isSelectedView()) {

                    if (abs(deltaY) > height * 0.65f && getTimeline().canChangeViewsOrder) {
                        getTimeline().changeOrderViews(this, deltaY > 0)
                        lastMoveY = event.rawY
                    }

                    if (!inMoveMode && abs(deltaX) >= mTouchSlop) {
                        enterMoveMode()
                        lastMoveX = event.rawX
                    }
                    if (inMoveMode) {
                        moveView(deltaX, false)
                        lastMoveX = event.rawX
                    }

                    return true

                } else if (abs(deltaY) >= mTouchSlop || abs(deltaX) >= mTouchSlop / 2) {
                    parent?.requestDisallowInterceptTouchEvent(false)
                    return false
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {

                onTouchUpCancel(event)

                if (event.action == MotionEvent.ACTION_UP &&
                    !inMoveMode && (lastMoveY - event.rawY) < mTouchSlop * 3 && (lastMoveX - event.rawX) < mTouchSlop * 3
                ) {
                    performClick()
                }

                if (inMoveMode) {
                    endMoveMode()
                    return true
                }
            }
        }
        return isSelectedView()
    }

    private fun endMoveMode() {
        inMoveMode = false
        getTimeline().templateParamChanged()
        invalidate()
    }


    private fun stretchView(arrow: Int, deltaX: Float, recursively: Boolean) {
        val seekTime = getSeekTime(deltaX)

        val oldDuration = duration
        val oldStartTime = startTime


        if (arrow == -1) {

            // move right
            if (seekTime < 0) {

                setNewTextDuration(oldDuration + seekTime)

                val seekDelta = duration - oldDuration

                startTime = calibrateStartTime(oldStartTime - seekDelta)

            }
            //move left
            else {

                startTime = max(oldStartTime - seekTime, 0.0)

                val seekDelta = oldStartTime - startTime

                setNewTextDuration(oldDuration + seekDelta)
            }

        } else {
            setNewTextDuration(oldDuration - seekTime)
        }

        checkOutOfSightAndMove(oldDuration, oldStartTime, recursively, arrow, deltaX) {
            stretchView(
                arrow,
                it,
                true
            )
        }
    }

    override fun mayVibrate(
        durationDiff: Double,
        startTimeDiff: Double,
        movingArrow: Int,
        deltaX: Float
    ) {
        super.mayVibrate(durationDiff, startTimeDiff, movingArrow, deltaX)

        val moveLeft = deltaX > 0

        if (durationDiff == 0.0 && startTimeDiff == 0.0) {

            if (moveLeft && (duration == minDuration || startTime == 0.0)) {
                vibrate(deltaX)
            } else if (!moveLeft && duration + startTime == getTimeline().duration) {
                vibrate(deltaX)
            }
        }
    }

    private fun moveView(deltaX: Float, recursively: Boolean) {
        val oldStartTime = startTime
        startTime = calibrateStartTime(startTime - getSeekTime(deltaX))

        checkOutOfSightAndMove(duration, oldStartTime, recursively, 0, deltaX) {
            moveView(
                it,
                true
            )
        }
    }

    private fun enterMoveMode() {
        inMoveMode = true
        getTimeline().resetStartFrame()
        invalidate()
    }


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (parent != null && width > 0) {

            val offset = additionalTouchArrowStart + frameDrawer.getArrowWidth()

            canvas.drawRoundRect(
                offset,
                0f,
                width.toFloat() - offset,
                height.toFloat(),
                bgRounding,
                bgRounding,
                bgPaint
            )

            mayRefreshTextLayout((width - textMargin * 2 - offset * 2).toInt())

            canvas.withTranslation(textMargin + offset, textY) {
                mTextLayout?.draw(canvas)
            }

            if (isSelected) {
                frameDrawer.draw(
                    canvas, offset, 0f,
                    width.toFloat() - offset, height.toFloat(), false,
                    !inMoveMode, !inMoveMode
                )
            }
        }
    }

    companion object {

        val additionalTouchArrowEnd = 5.dpToPixels()
        val additionalTouchArrowStart = 5.dpToPixels()
    }
}