package app.inspiry.edit.instruments.color

import app.inspiry.palette.model.PaletteLinearGradient
import app.inspiry.palette.provider.PaletteProviderImpl

class PaletteItems(val colorsCount: Int) {

    private val colorList = mutableListOf(0).apply {
        addAll(PaletteProviderImpl().getSingleColors())
    }
    private val gradientList = mutableListOf(PaletteLinearGradient()).apply {
        addAll(PaletteProviderImpl().getGradients())
    }
    private val twoColorsPaletteList = PaletteProviderImpl().getTwoColors()
    private val threeColorsPaletteList = PaletteProviderImpl().getThreeColors()

    private val currentColorsInLayers = mutableMapOf<Int, Int>()
    private val currentGradientInLayers = mutableMapOf<Int, PaletteLinearGradient>()

    fun getColorListForLayer(layer: Int): List<Int> {
        colorList[0] = currentColorsInLayers[layer] ?: 0
        return colorList
    }

    fun getGradientListForLayer(layer: Int): List<PaletteLinearGradient> {
        gradientList[0] = currentGradientInLayers[layer] ?: PaletteLinearGradient()
        return gradientList.toList()
    }
     fun getPaletteList(): List<IntArray> {
         return when (colorsCount) {
             2 -> twoColorsPaletteList
             3 -> threeColorsPaletteList
             else -> arrayListOf(intArrayOf(0))
         }
     }

    fun getCurrentColorIndexForLayer(layer: Int, color: Int): Int {

        val index = colorList.indexOf(color)
        if (color == 0) return Int.MIN_VALUE
        return if (index<0 && currentColorsInLayers[layer] == color) 0 else if (index >= 0) index else Int.MIN_VALUE
    }

    fun getCurrentGradientIndexForLayer(layer: Int, gradient: PaletteLinearGradient): Int {
        val index = gradientList.indexOf(gradient)
        if (index<0 && currentGradientInLayers[layer] == gradient) return 0
        if (index >= 0) {
            return index
        }

        return Int.MIN_VALUE
    }
    fun getCurrentPaletteIndex(colors: IntArray): Int {

        getPaletteList().forEachIndexed { index, p ->
            var equal = true
            p.forEachIndexed { layer,  color ->
                if (color != colors[layer]) {
                    equal = false
                }
            }
            if (equal) return index

        }
        return Int.MIN_VALUE
    }

    fun hasAdditionalColor(layer: Int): Boolean {
        return currentColorsInLayers[layer] != null
    }
    fun hasAdditionalGradient(layer: Int): Boolean {
        return currentGradientInLayers[layer] != null
    }
    fun getAdditionalColor(layer: Int): Int {
        return currentColorsInLayers[layer] ?: 0
    }
    fun getAdditionalGradient(layer: Int): PaletteLinearGradient {
        return currentGradientInLayers[layer] ?: PaletteLinearGradient()
    }
    fun updateCurrentGradientForLayer(layer: Int, gradient: PaletteLinearGradient) {
        if (getCurrentGradientIndexForLayer(layer, gradient) < 0) currentGradientInLayers[layer] = gradient
    }

    fun updateCurrentColorForLayer(layer: Int, color: Int) {
        if (getCurrentColorIndexForLayer(layer, color) < 0) currentColorsInLayers[layer] = color
    }

    fun getIndices(type: ColorDialogPage, size: Int): List<Int> {
        val startId = if (type == ColorDialogPage.COLOR) COLOR_PICKER_ITEM else REMOVE_ITEM
        return (startId until size).toList()
    }

    fun getElements(type: ColorDialogPage, layer: Int, addIsKnown: (Boolean) -> Unit): List<Any> {
        val addElement: Boolean
        val elements = when (type) {
            ColorDialogPage.COLOR -> {
                val colors = getColorListForLayer(layer)
                addElement = hasAdditionalColor(layer)
                colors
            }
            ColorDialogPage.GRADIENT -> {
                val gradients = getGradientListForLayer(layer)
                addElement = hasAdditionalGradient(layer)
                gradients

            }
            ColorDialogPage.PALETTE -> {
                addElement = true
                getPaletteList()
            }
            else -> throw IllegalStateException("unsupported elements type for color selector: ${type.name}")
        }
        addIsKnown(addElement)

        return elements
    }

    companion object {
        const val COLOR_PICKER_ITEM = -2
        const val REMOVE_ITEM = -1
    }
}