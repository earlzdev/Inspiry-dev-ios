package app.inspiry.core.animator.appliers

import app.inspiry.core.animator.text.DrawBackgroundAnimParam
import app.inspiry.views.InspView
import app.inspiry.views.text.InnerGenericText
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("scale")
class ScaleOuterAnimApplier(var fromX: Float = 1f, var toX: Float = 1f,
                            var fromY: Float = 1f, var toY: Float = 1f) :
    AnimApplier(), ToAsFromSwappableAnimApplier {

    override fun onPreDraw(view: InspView<*>, value: Float) {
        view.view?.scaleX = (toX - fromX) * value + fromX
        view.view?.scaleY = (toY - fromY) * value + fromY
    }

    override fun transformText(
        param: DrawBackgroundAnimParam,
        value: Float,
        view: InnerGenericText<*>
    ) {
        param.scaleX = (toX - fromX) * value + fromX
        param.scaleY = (toY - fromY) * value + fromY
    }
    override fun setToAsFrom() {
        toX = fromX
        toY = fromY
    }
}