package app.inspiry.palette

import app.inspiry.palette.model.BasePalette
import app.inspiry.palette.model.PaletteLinearGradient
import app.inspiry.palette.model.PaletteMultiColor
import app.inspiry.core.analytics.AnalyticsManager
import app.inspiry.core.analytics.putBoolean
import app.inspiry.core.analytics.putString
import app.inspiry.core.util.ArgbColorManager
import app.inspiry.core.util.PredefinedColors

object PaletteUtils {

    fun currentSingleColor(palette: BasePalette<*>) =
        if (palette.mainColor is PaletteLinearGradient || palette.backgroundImage != null) PredefinedColors.TRANSPARENT else
            palette.choices.getOrNull(0)?.color ?: palette.mainColor?.getFirstColor()
            ?: PredefinedColors.TRANSPARENT


    fun isPaletteChangedOnlyFirst(
        paletteWhenOpenedDialog: BasePalette<*>,
        current: BasePalette<*>
    ): Boolean {

        if (current.choices.size == 1) {
            return true
        }
        if (current.choices.subList(
                1,
                current.choices.size
            ) == paletteWhenOpenedDialog.choices.subList(1, paletteWhenOpenedDialog.choices.size)
        ) {
            return true
        }

        return false
    }
}

fun BasePalette<*>.getCurrentPaletteMultiColor(): PaletteMultiColor {
    return PaletteMultiColor(choices.map { it.color ?: PredefinedColors.TRANSPARENT })
}

fun AnalyticsManager.sendAnalyticsPaletteClosed(
    current: BasePalette<*>,
    paletteWhenOpenedDialog: BasePalette<*>
) {
    if ((current.backgroundImage != null && current.backgroundImage != paletteWhenOpenedDialog.backgroundImage) ||
        (current.mainColor != null && current.mainColor != paletteWhenOpenedDialog.mainColor) ||
        (current.choices != paletteWhenOpenedDialog.choices)
    ) {

        sendEvent("palette_changed") {
            if (current.backgroundImage != null) {
                putBoolean("is_from_gallery_picked", true)
            } else if (current.mainColor != null) {
                (current.mainColor as? PaletteLinearGradient?)?.colors?.forEachIndexed { index, i ->
                    putString("gradient_color_$index", ArgbColorManager.colorToString(i))
                }
            } else if (PaletteUtils.isPaletteChangedOnlyFirst(paletteWhenOpenedDialog, current)) {
                putString(
                    "new_single_color",
                    ArgbColorManager.colorToString(PaletteUtils.currentSingleColor(current))
                )

            } else {
                current.choices.forEachIndexed { index, choice ->
                    putString("palette_color_$index", choice.color?.let { ArgbColorManager.colorToString(it) })
                }
            }
        }
    }
}

fun String?.getViewAndLayerIdOfVector(): Pair<String?, String?> {
    val split = this?.split('.')

    val viewId: String?
    val layerId: String?

    if (split?.size == 2) {
        viewId = split.getOrNull(0)
        layerId = split.getOrNull(1)
    } else {
        viewId = this
        layerId = null
    }
    return viewId to layerId
}
