package app.inspiry.palette.model

import app.inspiry.core.animator.appliers.ColorType
import app.inspiry.core.media.*
import app.inspiry.core.util.ArgbColorManager
import kotlinx.serialization.Serializable

@Serializable
class MediaPalette(
    override val isAvailable: Boolean = true,
    override var mainColor: AbsPaletteColor? = null,
    override val bgImageOrGradientCanBeSet: Boolean = false,
    override var choices: MutableList<MediaPaletteChoice> = mutableListOf(),
    override var alpha: Float = 1f

) : BasePalette<MediaPaletteChoice>() {
    fun resetColors() {
        mainColor = null
        choices.forEach {
            it.color = null
        }
    }

    override fun toString(): String {
        return "MediaPalette(mainColor=$mainColor, isAvailable=$isAvailable, choices=$choices)"
    }

    override fun supportsBgImage(): Boolean {
        return false
    }

    override fun supportsAlpha(): Boolean {
        return true
    }

    override var backgroundImage: String?
        get() = null
        set(value) {}

    companion object {
        fun getFromMovable(media: Media): MediaPalette {
            return when (media) {
                is MediaVector -> media.mediaPalette
                is MediaPath -> MediaPalette(
                    mainColor = PaletteColor(media.backgroundColor),
                    bgImageOrGradientCanBeSet = false,
                    alpha = ArgbColorManager.alpha(media.backgroundColor) / 255f
                )
                is MediaImage -> MediaPalette(
                    mainColor = if (media.hasBackground()) media.backgroundGradient ?: PaletteColor(
                        media.backgroundColor
                    ) else media.colorFilter?.let { PaletteColor(it) },
                    bgImageOrGradientCanBeSet = media.hasBackground(),
                    alpha = media.alpha
                )
                is MediaGroup -> MediaPalette(
                    bgImageOrGradientCanBeSet = false,

                    )
                else -> throw IllegalStateException("unsupported media")
            }
        }
    }

    override fun setLinkedColor(colorType: ColorType, color: Int) {
        setColorForElement(colorType.name, color)
    }

    override fun getLinkedColor(colorType: ColorType): Int? {
        return getColorForElement(colorType.name)
    }

    override fun setColorForElement(elementName: String, color: Int) {
        choices.forEach { choice ->
            if (choice.elements.contains(elementName)) choice.color = color
        }
    }

    override fun getColorForElement(elementName: String): Int? {
        choices.forEach { choice ->
            val color = choice.color
            if (choice.elements.contains(elementName)) return color
        }
        return null
    }


}
