package app.inspiry.palette

import app.inspiry.core.util.ArgbColorManager
import app.inspiry.palette.model.AbsPaletteColor
import app.inspiry.palette.model.BasePalette
import app.inspiry.palette.model.PaletteColor
import app.inspiry.palette.model.PaletteMultiColor
import dev.icerock.moko.mvvm.viewmodel.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow

class PaletteViewModel : ViewModel() {

    val alpha = MutableStateFlow(1f)

    fun setSingleColor(color: Int, palette: BasePalette<*>) {
        alpha.value = ArgbColorManager.alpha(color) / 255f

        if (palette.choices.isEmpty()) {
            palette.mainColor = PaletteColor(color)
        } else {
            palette.choices[0].color = color
            palette.mainColor = null
        }

        if (palette.bgImageOrGradientCanBeSet) {
            palette.backgroundImage = null
        }
    }

    fun onGradientSelected(gradient: AbsPaletteColor, palette: BasePalette<*>) {
        alpha.value = ArgbColorManager.alpha(gradient.getFirstColor()) / 255f

        palette.mainColor = gradient
        if (palette.choices.isNotEmpty()) {
            palette.choices[0].color = gradient.getFirstColor()
        }
        palette.backgroundImage = null
    }

    fun onPickedPaletteMultiColor(it: AbsPaletteColor, palette: BasePalette<*>) {

        alpha.value = ArgbColorManager.alpha(it.getFirstColor()) / 255f

        palette.mainColor = null
        palette.backgroundImage = null

        val picked = it as PaletteMultiColor
        palette.choices.forEachIndexed { index, paletteChoice ->
            paletteChoice.color = picked.colors[index]
        }
    }
}