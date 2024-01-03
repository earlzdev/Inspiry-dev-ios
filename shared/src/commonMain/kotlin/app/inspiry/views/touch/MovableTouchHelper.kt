package app.inspiry.views.touch

import app.inspiry.core.data.TouchAction
import app.inspiry.views.InspView
import app.inspiry.views.guideline.GuidelineManager
import app.inspiry.views.template.TemplateMode
import kotlin.math.abs

abstract class MovableTouchHelper(val inspView: InspView<*>,
                                  private val guidelineManager: GuidelineManager) {

    var guidelineMoveResistanceX: Float = guidelineManager.guidelineThreshold
    var guidelineMoveResistanceY: Float = guidelineManager.guidelineThreshold
    abstract val viewMovedThreshold: Float

    var viewMovedPixels: Float = 0f

    fun touchHandled(): Boolean {
        if (inspView.templateMode != TemplateMode.EDIT) return false

        if (inspView.media.isSocialImageOrVector() && !inspView.isSelectedForEdit) {
            inspView.templateParent.changeSelectedView(inspView)
        }
        if (!inspView.media.buttonIsAvailable(action = TouchAction.move)) return false

        var handled = false

        if (!inspView.isSelectedForEdit) {
            inspView.templateParent.changeSelectedView(inspView)
            handled = true
        }

        return handled
    }

    protected fun mayPerformMovement(delta: Float, canMove: Boolean, forX: Boolean, parentSize: Int) {
        var deltaVar = delta

        if (deltaVar != 0f && canMove) {
            viewMovedPixels += abs(deltaVar)

            if (viewMovedPixels > viewMovedThreshold) {

                val guidelineMoveResistance: Float =
                    if (forX) guidelineMoveResistanceX else guidelineMoveResistanceY

                if (guidelineMoveResistance < guidelineManager.guidelineThreshold) {

                    if (abs(guidelineMoveResistance + deltaVar) >= guidelineManager.guidelineThreshold) {
                        deltaVar += guidelineMoveResistance

                        if (forX)
                            guidelineMoveResistanceX = guidelineManager.guidelineThreshold
                        else
                            guidelineMoveResistanceY = guidelineManager.guidelineThreshold
                    } else {
                        if (forX)
                            guidelineMoveResistanceX += deltaVar.toInt()
                        else
                            guidelineMoveResistanceY += deltaVar.toInt()
                        deltaVar = 0f
                    }
                }

                if (deltaVar != 0f) {
                    if (forX) inspView.incrementTranslationX(deltaVar / parentSize)
                    else inspView.incrementTranslationY(deltaVar / parentSize)
                }
            }
        }
    }
}