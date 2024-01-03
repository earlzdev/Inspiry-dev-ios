package app.inspiry.views.touch

import android.view.MotionEvent
import app.inspiry.core.data.TouchAction
import app.inspiry.utils.dpToPixels
import app.inspiry.views.InspView
import app.inspiry.views.guideline.GuidelineManager
import app.inspiry.views.template.TemplateMode
import kotlin.math.roundToInt

class MovableTouchHelperAndroid(
    inspView: InspView<*>,
    guidelineManager: GuidelineManager) :
    MovableTouchHelper(inspView, guidelineManager) {

    var lastTouchX: Float = 0f
    var lastTouchY: Float = 0f
    var handledTouchUp: Boolean = false

    override val viewMovedThreshold: Float by lazy { 6.dpToPixels() }

    private fun detectMoveEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                lastTouchX = event.rawX
                lastTouchY = event.rawY
            }
            MotionEvent.ACTION_MOVE -> {
                val x = event.rawX
                val y = event.rawY

                val deltaX = x - lastTouchX
                val deltaY = y - lastTouchY
                val templateScale = inspView.templateParent.templateTransform.value.scale
                mayPerformMovement(deltaX, inspView.media.canMoveX(), true, (inspView.templateParent.viewWidth * templateScale).roundToInt())
                mayPerformMovement(deltaY, inspView.media.canMoveY(), false, (inspView.templateParent.viewHeight* templateScale).roundToInt())

                if (viewMovedPixels > viewMovedThreshold) {
                    lastTouchY = y
                    lastTouchX = x
                }
            }
            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> {

                inspView.templateParentNullable?.run {
                    guidelineManager.hideOnTouchCancel(this, inspView)
                }

                if (viewMovedPixels > viewMovedThreshold) {
                    viewMovedPixels = 0f
                    return true
                }
                viewMovedPixels = 0f
            }
        }
        return false
    }

    fun onTouchMovable(event: MotionEvent): Boolean {

        var handled = super.touchHandled()
        if (handled) handledTouchUp = true

        if (inspView.isSelectedForEdit) handled = detectMoveEvent(event)

        if (event.action == MotionEvent.ACTION_UP && handledTouchUp) {
            handledTouchUp = false
            return true
        }
        if (handled || viewMovedPixels > viewMovedThreshold) return true

        return false
    }
}


