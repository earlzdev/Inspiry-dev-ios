package app.inspiry.core.animator.appliers

import app.inspiry.core.animator.text.DrawBackgroundAnimParam
import app.inspiry.core.media.Media
import app.inspiry.core.media.MediaText
import app.inspiry.core.serialization.ColorSerializer
import app.inspiry.core.util.ArgbColorManager
import app.inspiry.views.InspView
import app.inspiry.views.text.InnerGenericText
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("backgroundColor")
class BackgroundColorAnimApplier(
    @Serializable(with = ColorSerializer::class) var from: Int? = null,
    @Serializable(with = ColorSerializer::class) var to: Int? = null,
    @Serializable(with = ColorSerializer::class) var fromEnd: Int? = null,
    @Serializable(with = ColorSerializer::class) var toEnd: Int? = null,
    var fromColor: ColorType? = null,
    var toColor: ColorType? = null
) : ChangeableAnimApplier, AnimApplier(), ToAsFromSwappableAnimApplier {

    override fun onPreDraw(view: InspView<*>, value: Float) {
        if (from == null || to == null) onValuesChanged(view.media)
        val color = ArgbColorManager.colorDistance(from!!, to!!, value)
        view.setBackgroundColorFromAnimation(color)
    }

    override fun onValuesChanged(media: Media) {
        if (media is MediaText) {
            fromColor?.let { from = media.mediaPalette?.getLinkedColor(it) }
            toColor?.let { to = media.mediaPalette?.getLinkedColor(it) }
            if (from == null || to == null) throw IllegalStateException("type or color must be defined")
        }
    }

    override fun setToAsFrom() {
        to = from
        toEnd = fromEnd
        toColor = fromColor
    }

    override fun transformText(
        param: DrawBackgroundAnimParam,
        value: Float,
        view: InnerGenericText<*>
    ) {
        if (from == null || to == null) onValuesChanged(view.media)

        if (fromEnd != null && toEnd != null) {

            val startColor = ArgbColorManager.colorDistance(from!!, to!!, value)
            val endColor = ArgbColorManager.colorDistance(fromEnd!!, toEnd!!, value)

            //TODO: remove typecast?
            //(view as GenericTextLayoutAndroid).animateGradientParam(startColor, endColor, param)


        } else {
            param.color = ArgbColorManager.colorDistance(from!!, to!!, value)
        }

    }
}