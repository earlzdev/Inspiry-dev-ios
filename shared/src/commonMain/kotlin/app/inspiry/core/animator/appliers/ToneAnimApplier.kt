package app.inspiry.core.animator.appliers

import app.inspiry.core.animator.text.DrawBackgroundAnimParam
import app.inspiry.core.media.Media
import app.inspiry.core.media.MediaText
import app.inspiry.core.serialization.ColorSerializer
import app.inspiry.core.util.ArgbColorManager
import app.inspiry.views.text.InnerGenericText
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("tone")
class ToneAnimApplier(
    @Serializable(with = ColorSerializer::class) var from: Int? = null,
    @Serializable(with = ColorSerializer::class) var to: Int? = null,
    var fromColor: ColorType? = null,
    var toColor: ColorType? = null
) : ChangeableAnimApplier, AnimApplier(), ToAsFromSwappableAnimApplier {

    override fun onValuesChanged(media: Media) {
        if (media is MediaText) {
            fromColor?.let { from = media.mediaPalette?.getLinkedColor(it) }
            toColor?.let { to = media.mediaPalette?.getLinkedColor(it) }
            if (from == null || to == null) throw IllegalStateException("type or color must be defined")
        }
    }

    override fun transformText(
        param: DrawBackgroundAnimParam,
        value: Float,
        view: InnerGenericText<*>
    ) {
        if (from == null || to == null) onValuesChanged(view.media)

        param.color = ArgbColorManager.colorDistance(to!!, from!!, value)
    }
    override fun setToAsFrom() {
        to = from
        toColor = fromColor
    }
}