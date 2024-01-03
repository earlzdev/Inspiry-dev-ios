package app.inspiry.core.animator.appliers

import app.inspiry.views.InspView
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("size")
class SizeAnimApplier(
    var fromWidth: Float = -1f,
    var fromHeight: Float = -1f,
    var toWidth: Float = -1f,
    var toHeight: Float = -1f,
    var fromCircle: Boolean = false, //this parameter can be specified instead of fromWidth or fromHeight
    var toCircle: Boolean = false ////this parameter can be specified instead of toWidth or toHeight
) : AnimApplier(), ToAsFromSwappableAnimApplier {

    override fun onPreDraw(view: InspView<*>, value: Float) {
        val width = view.viewWidth
        val height = view.viewHeight
        var startWidth = fromWidth
        var startHeight = fromHeight
        var endWidth = toWidth
        var endHeight = toHeight

        if (fromCircle) {
            //if fromCircle is specified instead of fromWidth,
            //startWidth will be calculated automatically
            if (fromWidth == -1f && height < width) startWidth = height * startHeight / width.toFloat()
            if (fromHeight == -1f && width < height) startHeight = width * startWidth / height.toFloat()
        }

        if (toCircle) {
            if (toWidth == -1f && height < width) endWidth = height * endHeight / width.toFloat()
            if (toHeight == -1f && width < height) endHeight = width * endWidth / height.toFloat()
        }

        val widthFactor =
            if (startWidth == -1f || endWidth == -1f) -1f else (endWidth - startWidth) * value + startWidth
        val heightFactor =
            if (startHeight == -1f || endHeight == -1f) -1f else (endHeight - startHeight) * value + startHeight

        view.view?.setSizeFromAnimation(widthFactor, heightFactor)
    }

    override fun setToAsFrom() {
        toWidth = fromWidth
        toHeight = fromHeight
        toCircle = fromCircle
    }
}