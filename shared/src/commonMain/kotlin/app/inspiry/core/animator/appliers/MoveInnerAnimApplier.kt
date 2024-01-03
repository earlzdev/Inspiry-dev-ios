package app.inspiry.core.animator.appliers

import app.inspiry.views.InspView
import app.inspiry.views.media.InspMediaView
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("move_inner")
class MoveInnerAnimApplier(var fromX: Float = 0f, var fromY: Float = 0f,
                           var toX: Float = 0f, var toY: Float = 0f) :
    AnimApplier(), ToAsFromSwappableAnimApplier {

    override fun onPreDraw(view: InspView<*>, value: Float) {
        val translationX = ((toX - fromX) * value + fromX) * view.viewWidth
        val translationY = ((toY - fromY) * value + fromY) * view.viewHeight
        (view as InspMediaView).setTranslateInner(translationX, translationY)
    }

    override fun setToAsFrom() {
        toX = fromX
        toY = fromY
    }
}