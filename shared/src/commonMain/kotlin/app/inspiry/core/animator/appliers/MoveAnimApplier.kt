package app.inspiry.core.animator.appliers

import app.inspiry.core.animator.ViewRelativity
import app.inspiry.core.animator.text.DrawBackgroundAnimParam
import app.inspiry.views.InspView
import app.inspiry.views.text.InnerGenericText
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("move")
class MoveAnimApplier(
    var fromX: Float = 0f,
    var fromY: Float = 0f,
    var toX: Float = 0f,
    var toY: Float = 0f,
    var relativity: ViewRelativity = ViewRelativity.PARENT,
) : AnimApplier(), ToAsFromSwappableAnimApplier {

    override fun onPreDraw(view: InspView<*>, value: Float) {

        if (fromX != 0f || toX != 0f)
            preDrawX(fromX, toX, relativity, value, view)

        if (fromY != 0f || toY != 0f)
            preDrawY(fromY, toY, relativity, value, view)

    }

    companion object {
        fun preDrawY(
            fromY: Float, toY: Float, relativity: ViewRelativity,
            value: Float, animView: InspView<*>
        ) {

            val trY = (toY - fromY) * value + fromY

            val result = when (relativity) {
                ViewRelativity.PARENT -> (trY * (animView.parentInsp?.viewHeight ?: 0)) / animView.templateParent.viewHeight
                ViewRelativity.ROOT -> trY
                ViewRelativity.SELF -> (trY * animView.viewHeight) / animView.templateParent.viewHeight
            }

            animView.animationHelper?.animationTranslationY = result
            animView.updateTranslationY(false)
        }

        fun preDrawX(
            fromX: Float, toX: Float, relativity: ViewRelativity,
            value: Float, animView: InspView<*>
        ) {

            val trX = (toX - fromX) * value + fromX
            val result = when (relativity) {
                ViewRelativity.PARENT -> (trX * (animView.parentInsp?.viewWidth ?: 0)) / animView.templateParent.viewWidth
                ViewRelativity.ROOT -> trX
                ViewRelativity.SELF -> (trX * animView.viewWidth) / animView.templateParent.viewWidth
            }

            animView.animationHelper?.animationTranslationX = result
            animView.updateTranslationX(false)
        }
    }


    override fun transformText(
        param: DrawBackgroundAnimParam,
        value: Float,
        view: InnerGenericText<*>
    ) {

        if (fromX != 0f || toX != 0f) param.translateX =
            ((toX - fromX) * value + fromX) * param.width
        if (fromY != 0f || toY != 0f) param.translateY =
            ((toY - fromY) * value + fromY) * param.height
    }

    override fun setToAsFrom() {
        toX = fromX
        toY = fromY

    }
}