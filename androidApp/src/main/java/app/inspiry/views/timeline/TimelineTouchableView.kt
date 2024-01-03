package app.inspiry.views.timeline

import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.widget.Toast
import app.inspiry.BuildConfig
import app.inspiry.core.data.FRAME_IN_MILLIS
import app.inspiry.utils.dpToPixels
import app.inspiry.utils.getPostMessageCompat
import kotlin.math.abs
import kotlin.math.max

interface TimelineTouchableView : TimelineInsideView {

    var startedTouchArrow: Int
    var lastMoveX: Float
    var lastMoveY: Float
    val frameDrawer: TimelineFrameDrawer

    //null means false. Non null - direction
    var inMovementNearEdge: Boolean?

    //vibrate only one time during touch.
    //-1 moved left
    //1 moved right
    //0 didnt vibrate
    var vibratedOnTouch: Int
    var vibrationDelta: Float

    fun setWidthAndX()
    fun setWidthAndX(offsetForContent: Float, arrowWidth: Float) {
        val timeline = getTimeline()
        val percentStartXPassed = startTime / timeline.duration
        val containsWidths = timeline.calcWidthOfViews()
        val borderStartX = (containsWidths * percentStartXPassed).toFloat()

        view.translationX = borderStartX + offsetForContent - arrowWidth

        val newWidth = ((duration / timeline.duration) * containsWidths + arrowWidth * 2).toInt()

        if (view.layoutParams.width != newWidth) {
            view.layoutParams.width = newWidth
            onNewSizeSetImmediately(newWidth, view.layoutParams.height)
            view.requestLayout()
        }
    }

    fun onNewSizeSetImmediately(width: Int, height: Int) {}

    private fun stopMovementNearEdge() {
        inMovementNearEdge = null
        view.handler.removeCallbacksAndMessages(view)
    }

    fun onTouchUpCancel(event: MotionEvent) {
        vibratedOnTouch = 0
        stopMovementNearEdge()

        if (isSelectedView() && startedTouchArrow.touchedArrow()) {
            getTimeline().templateParamChanged()
        }
    }

    fun getSeekTime(deltaX: Float): Double {
        val timeline = getTimeline()
        val widthOfViews = timeline.calcWidthOfViews()
        val percentSeek = deltaX / widthOfViews

        return timeline.duration * percentSeek
    }

    fun Int.touchedArrow() = this != 0


    //-1 left
    //0 none
    //1 right
    fun isTouchArrow(event: MotionEvent, frameDrawer: TimelineFrameDrawer): Int {
        val touchArrowOffset =
            frameDrawer.getArrowWidth() + TimelineTextView.additionalTouchArrowStart + TimelineTextView.additionalTouchArrowEnd

        if (isSelectedView()) {
            if (event.x < touchArrowOffset) {
                return -1
            } else if (event.x > (view.width - touchArrowOffset)) {
                return 1
            }
        }

        return 0
    }


    fun isOutOfSightStartForRightArrow(containsWidths: Double): Boolean {
        val timeline = getTimeline()
        val percentEndXPassed = (startTime + duration) / timeline.duration
        val borderEndX =
            (containsWidths * percentEndXPassed).toFloat() - BORDER_OFFSET_FOR_OUT_OF_SIGHT

        return timeline.scrollX - timeline.width / 2 > borderEndX
    }

    fun mayVibrate(
        durationDiff: Double,
        startTimeDiff: Double,
        movingArrow: Int,
        deltaX: Float
    ) {

        //reset if we move into different direction
        //val newVibrateOnTouch = if (moveLeft) -1 else 1
        //if (vibratedOnTouch != 0 && vibratedOnTouch != newVibrateOnTouch)
        //    vibratedOnTouch = 0
    }

    fun checkOutOfSightAndMove(
        oldDuration: Double,
        oldStartTime: Double, recursively: Boolean, movingArrow: Int, originalDeltaX: Float,
        recursiveMethod: (Float) -> Unit
    ) {

        if (!recursively && inMovementNearEdge != null) {
            return
        }

        val durationDiff = oldDuration - duration
        val startTimeDiff = oldStartTime - startTime

        val moveLeft = originalDeltaX > 0
        //startTimeDiff > 0 || durationDiff > 0 && !(startTimeDiff < 0.0 && durationDiff > 0.0)

        mayVibrate(durationDiff, startTimeDiff, movingArrow, originalDeltaX)

        val timeline = getTimeline()
        val containsWidths = timeline.calcWidthOfViews()
        val moveOutOfSight: Boolean

        moveOutOfSight = when (movingArrow) {
            1 -> {
                if (moveLeft) {

                    isOutOfSightStartForRightArrow(containsWidths)

                } else {

                    val percentEndXPassed = (startTime + duration) / timeline.duration
                    val borderEndX =
                        (containsWidths * percentEndXPassed).toFloat() + TimelineView.ARROW_WIDTH + BORDER_OFFSET_FOR_OUT_OF_SIGHT

                    timeline.scrollX + timeline.width / 2 < borderEndX
                }
            }
            -1 -> {
                if (moveLeft) {
                    val percentStartXPassed = startTime / timeline.duration
                    val borderStartX =
                        (containsWidths * percentStartXPassed).toFloat() - TimelineView.ARROW_WIDTH - BORDER_OFFSET_FOR_OUT_OF_SIGHT

                    timeline.scrollX - timeline.width / 2 > borderStartX

                } else {

                    val percentStartXPassed = startTime / timeline.duration
                    val borderStartX =
                        (containsWidths * percentStartXPassed).toFloat() + TimelineView.ARROW_WIDTH + BORDER_OFFSET_FOR_OUT_OF_SIGHT

                    timeline.scrollX + timeline.width / 2 < borderStartX
                }
            }
            else -> {
                if (moveLeft) {
                    val percentStartXPassed = startTime / timeline.duration
                    val borderStartX =
                        (containsWidths * percentStartXPassed).toFloat() - TimelineView.ARROW_WIDTH - BORDER_OFFSET_FOR_OUT_OF_SIGHT

                    timeline.scrollX - timeline.width / 2 > borderStartX

                } else {

                    val percentEndXPassed = (startTime + duration) / timeline.duration
                    val borderEndX =
                        (containsWidths * percentEndXPassed).toFloat() + TimelineView.ARROW_WIDTH + BORDER_OFFSET_FOR_OUT_OF_SIGHT

                    timeline.scrollX + timeline.width / 2 < borderEndX
                }
            }
        }

        if (moveOutOfSight && (durationDiff != 0.0 || startTimeDiff != 0.0)) {

            /*K.i("moveOutOfSight") {
                "durationDiff ${durationDiff}, moveLeft = $moveLeft, arrow = ${movingArrow}"
            }*/

            if (moveLeft != inMovementNearEdge) {
                stopMovementNearEdge()
            }

            inMovementNearEdge = moveLeft
            view.handler.sendMessageDelayed(view.handler.getPostMessageCompat({

                var nextMovePixels = (-5f).dpToPixels()
                if (moveLeft) nextMovePixels = -nextMovePixels
                recursiveMethod.invoke(nextMovePixels)

            }, view), FRAME_IN_MILLIS.toLong())

            var moveAmountMillis = max(abs(startTimeDiff), abs(durationDiff))
            if (moveLeft) moveAmountMillis = -moveAmountMillis

            val moveAmountPixels =
                ((moveAmountMillis / timeline.duration) * containsWidths).toFloat()

            timeline.seekPositionOn(
                moveAmountPixels,
                0f, false, false
            )

        } else {
            stopMovementNearEdge()

            getTimeline().seekPositionOn(0f, 0f, false, false)
            //setWidthAndX()
        }
    }

    fun vibrate(moveDeltaX: Float) {
        this.vibrationDelta += moveDeltaX

        if (abs(vibrationDelta) > VIBRATION_DELTA_THRESHOLD && vibratedOnTouch == 0) {

            val moveLeft = moveDeltaX > 0
            vibratedOnTouch = if (moveLeft) -1 else 1
            vibrationDelta = 0f

            view.performHapticFeedback(
                HapticFeedbackConstants.KEYBOARD_TAP,
                HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING
            )

            if (BuildConfig.DEBUG) {
                Toast.makeText(view.context, "Vibrate", Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        val BORDER_OFFSET_FOR_OUT_OF_SIGHT = 10.dpToPixels()
        val VIBRATION_DELTA_THRESHOLD = 15.dpToPixels()
    }
}