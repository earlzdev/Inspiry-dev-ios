package app.inspiry.palette.model

import app.inspiry.core.animator.appliers.ColorType
import app.inspiry.core.media.Template
import app.inspiry.core.serialization.ColorSerializer
import app.inspiry.core.util.PredefinedColors
import app.inspiry.views.template.PALETTE_ID_ALL_TEXTS
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * @param backgroundVideoStartMs if it is non null, then backgroundImage is actually a video
 */
@Serializable
class TemplatePalette(
    override val isAvailable: Boolean = true,
    override var choices: MutableList<TemplatePaletteChoice> = mutableListOf(),
    override var mainColor: AbsPaletteColor? = null,
    override var bgImageOrGradientCanBeSet: Boolean = true,

    @Serializable(with = ColorSerializer::class) val defaultTextColor: Int = PredefinedColors.BLACK_ARGB,
    override var backgroundImage: String? = null,
    //null if non video
    var backgroundVideoStartMs: Int? = null,
    var backgroundVideoLooped: Boolean? = null

) : BasePalette<TemplatePaletteChoice>() {


    fun isVideo() = backgroundVideoStartMs != null

    companion object {

        fun getEmpty() = TemplatePalette()

        fun withBackgroundColor(color: Int): TemplatePalette {
            return TemplatePalette(
                choices = mutableListOf(
                    TemplatePaletteChoice(
                        color,
                        listOf(PaletteChoiceElement("background", null))
                    )
                )
            )
        }
    }

    fun copyViaJson(json: Json): TemplatePalette {
        return json.decodeFromString(TemplatePalette.serializer(), json.encodeToString(TemplatePalette.serializer(), this))
    }

    override var alpha: Float
        get() = 1f
        set(value) {}

    fun resetPaletteChoices(newPalette: MediaPalette, viewId: String?) {
        if (viewId == null) return

        newPalette.choices.forEach {
            if (it.color != null) {
                it.elements.forEach {

                    resetPaletteChoiceColor("$viewId.$it", false)
                }
            }
        }
    }

    fun getColor(id: String, type: String): Int? {
        val isText = type == "textColor"
        choices.forEach { choice ->
            choice.elements.find { (it.id == id || (id == PALETTE_ID_ALL_TEXTS && isText)) && it.type == type }
                ?.let { return choice.color }
        }
        return null
    }

    fun resetPaletteChoiceColor(id: String?, isText: Boolean) {
        if (isAvailable) {
            var processed = false
            if (id != null) {
                choices.filter { it.elements.any { el -> el.id == id || el.id?.contains("AsTextBg*$id") == true} }
                    .forEach {
                        it.color = null
                        processed = true
                        it.color = null
                    }
            }

            if (!processed && isText) {
                val choiceShortcut =
                    choices.find { it.elements.any { it.id == PALETTE_ID_ALL_TEXTS } }

                choiceShortcut?.color = null
            }
        }
    }


    override fun supportsAlpha(): Boolean {
        return false
    }

    fun getBackgroundColor(): Int {
        val mainColor = mainColor
        return if (mainColor is PaletteColor)
            mainColor.color
        else {
            choices.find { it.color != null && it.elements.any { it.type == "background" } }?.color
                ?: PredefinedColors.WHITE_ARGB
        }
    }

    override fun toString(): String {
        return "Palette(isAvailable=$isAvailable, defaultTextColor=$defaultTextColor," +
                " bgImageOrGradientCanBeSet=$bgImageOrGradientCanBeSet, backgroundImage=$backgroundImage, background=$mainColor)"
    }

    override fun supportsBgImage(): Boolean = true


    override fun hashCode(): Int {
        var result = backgroundImage?.hashCode() ?: 0
        result = 31 * result + (mainColor?.hashCode() ?: 0)
        result = 31 * result + choices.hashCode()
        return result
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TemplatePalette) return false

        if (choices != other.choices) return false
        if (mainColor != other.mainColor) return false
        if (backgroundImage != other.backgroundImage) return false

        return true
    }

    override fun getLinkedColor(colorType: ColorType): Int? {
        TODO("Not yet implemented")
    }

    override fun getColorForElement(elementName: String): Int? {
        TODO("Not yet implemented")
    }

    override fun setLinkedColor(colorType: ColorType, color: Int) {
        TODO("Not yet implemented")
    }

    override fun setColorForElement(elementName: String, color: Int) {
        TODO("Not yet implemented")
    }

}