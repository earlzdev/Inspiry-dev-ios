package app.inspiry.edit.instruments.color

import app.inspiry.MR
import app.inspiry.core.analytics.AnalyticsManager
import app.inspiry.core.ui.CommonMenu
import app.inspiry.core.util.ArgbColorManager
import app.inspiry.edit.instruments.BottomInstrumentsViewModel
import app.inspiry.edit.instruments.color.PaletteItems.Companion.COLOR_PICKER_ITEM
import app.inspiry.edit.instruments.color.PaletteItems.Companion.REMOVE_ITEM
import app.inspiry.palette.model.PaletteLinearGradient
import app.inspiry.views.InspView
import app.inspiry.views.media.ColorFilterMode
import app.inspiry.views.text.InspTextView
import kotlinx.coroutines.flow.MutableStateFlow


abstract class ColorDialogViewModel(inspView: InspView<*>? = null, val analyticsManager: AnalyticsManager) : BottomInstrumentsViewModel {

    var selectedView: MutableStateFlow<InspView<*>?> = MutableStateFlow(inspView)

    override fun onSelectedViewChanged(newSelected: InspView<*>?) {
        selectedView.value = newSelected
        initDefaults()
    }

    var selectedPage = MutableStateFlow(ColorDialogPage.COLOR)
    var colorPickerShow: ((Int) -> Unit)? = null

    var colorLayerCount: MutableStateFlow<Int> = MutableStateFlow(0) //count of color layers
    var gradientLayerCount: MutableStateFlow<Int> = MutableStateFlow(0) //count of gradient layers
    var paletteLayerCount: MutableStateFlow<Int> = MutableStateFlow(0) //count of palette layers (currently one supported, but there may be more)
    var alphaOneLayer: MutableStateFlow<Boolean> = MutableStateFlow(false) //if true - transparency will change in the view, otherwise each color will change separately
    var currentColorFilter: MutableStateFlow<ColorFilterMode?> = MutableStateFlow(null)

    var colorsForAnalytics: MutableMap<Int, Int> = mutableMapOf() //layer, color
    var gradientsForAnalytics: MutableMap<Int, IntArray> = mutableMapOf() //gradient colors
    var paletteForAnalytics: MutableList<Int> = mutableListOf() //palette colors

    fun colorWasChanged(): Boolean =
        colorsForAnalytics.isNotEmpty() || gradientsForAnalytics.isNotEmpty()

    var pageLayerCount = MutableStateFlow(0)

    fun updatePageLauerCount() {
        pageLayerCount.value = when (selectedPage.value) {
            ColorDialogPage.COLOR -> colorLayerCount.value
            ColorDialogPage.GRADIENT -> colorLayerCount.value
            ColorDialogPage.PALETTE -> paletteLayerCount.value
            ColorDialogPage.OPACITY -> if (!alphaOneLayer.value) colorLayerCount.value else 1
            ColorDialogPage.IMAGE -> 2
            ColorDialogPage.ROUNDNESS -> 1
            ColorDialogPage.COLOR_PICKER -> 0
        }
    }

    lateinit var paletteItems: PaletteItems

    open fun initDefaults() {
        selectedPage.value = ColorDialogPage.COLOR
        paletteItems = PaletteItems(colorLayerCount.value)

        repeat(gradientLayerCount.value) { layer ->
            val gradient = getCurrentGradientForLayer(layer)
            gradient?.let {
                val index = getCurrentGradientIndexForLayer(layer, true)
                if (index >= 0 && selectedPage.value == ColorDialogPage.COLOR) selectedPage.value =
                    ColorDialogPage.GRADIENT
            }
        }

        val colors = IntArray(colorLayerCount.value).mapIndexed { layer, _ ->
            val color = getCurrentColorForLayer(layer)
            val index = paletteItems.getCurrentColorIndexForLayer(layer, color)
            if (index < 0 && color != 0) paletteItems.updateCurrentColorForLayer(layer, color)

            color
        }

        if (paletteIsAvailable()) {
            val index = paletteItems.getCurrentPaletteIndex(colors.toIntArray())
            if (index >= 0 && selectedPage.value == ColorDialogPage.COLOR) selectedPage.value =
                ColorDialogPage.PALETTE
        }
        getCurrentImageBackground()?.let { selectedPage.value = ColorDialogPage.IMAGE }

        updatePageLauerCount()
    }

    fun getCurrentIndexForLayer(type: ColorDialogPage, layer: Int): Int {
        return when(type) {
            ColorDialogPage.COLOR -> getCurrentColorIndexForLayer(layer)
            ColorDialogPage.GRADIENT -> getCurrentGradientIndexForLayer(layer)
            ColorDialogPage.PALETTE -> getCurrentPaletteIndexForLayer(layer)
            else -> throw IllegalStateException("get current index for layer: unknown type ($type)")
        }
    }

    open fun onColorFilterChanged(newMode: ColorFilterMode) {
        val currentColor = getCurrentColorForLayer(0)
        onPickColor(0, currentColor)
    }

    open fun onSingleMediaSelected(uri: String, isVideo: Boolean) {}
    open fun onGradientSelected(layer: Int, gradientID: Int): PaletteLinearGradient? {
        if (gradientID < 0) {
            gradientsForAnalytics.remove(layer)
            onGradientReset(layer)
            return null
        }
        val gradient = paletteItems.getGradientListForLayer(layer)[gradientID]
        gradientsForAnalytics[layer] = gradient.colors.toIntArray()
        return gradient
    }
    open val colorFilterList = mutableListOf<ColorFilterMode>()

    abstract fun onOpacityChanged(layer: Int, value: Float)
    abstract fun onColorReset(layer: Int)
    abstract fun onGradientReset(layer: Int)
    abstract fun onPaletteReset(layer: Int)
    abstract fun onPickColor(layer: Int, color: Int)
    abstract fun getCurrentColorForLayer(layer: Int): Int
    abstract fun getCurrentGradientForLayer(layer: Int): PaletteLinearGradient?

    open fun paletteIsAvailable() = paletteLayerCount.value > 0
    open fun gradientIsAvailable() = gradientLayerCount.value > 0
    open fun colorIsAvailable() = true
    open fun customImageChoiceIsAvailable() = false
    open fun colorFilterIsAvailable() = colorFilterList.isNotEmpty()

    open val enableBorderChange = false

    open val enableRoundnessChange = false

    open fun onBorderChange(value: Float) {}
    open fun getRoundness(): Float = 0f
    open fun onRoundnessChange(value: Float) {}

    open fun onPaletteSelected(layer: Int, paletteID: Int) {
        if (paletteID < 0) {
            onPaletteReset(layer)
            return
        }

        val palette = paletteItems.getPaletteList()[paletteID]
        val offset = palette.size
        val startColorLayer = offset * layer
        palette.forEachIndexed { index, color ->
            onPickColor(startColorLayer + index, color)
        }
    }

    open fun getCurrentGradientIndexForLayer(layer: Int, init: Boolean = false): Int {
        val gradient = getCurrentGradientForLayer(layer) ?: return Int.MIN_VALUE
        var index = paletteItems.getCurrentGradientIndexForLayer(layer, gradient)
        if (init && index < 0) {
            paletteItems.updateCurrentGradientForLayer(layer, gradient)
            index = 0
        }

        return index
    }

    open fun getCurrentColorIndexForLayer(layer: Int, init: Boolean = false): Int {
        val color = getCurrentColorForLayer(layer)
        val index = paletteItems.getCurrentColorIndexForLayer(layer, color)
        if (init && index < 0) {
            paletteItems.updateCurrentColorForLayer(layer, color)
        }
        return index
    }

    open fun getCurrentAlphaForLayer(layer: Int): Float {
        val gradient = getCurrentGradientForLayer(layer)
        gradient?.let {
            return ArgbColorManager.alphaDegree(gradient.getFirstColor())
        }
        return ArgbColorManager.alphaDegree(getCurrentColorForLayer(layer))
    }

    open fun getCurrentPaletteIndexForLayer(layer: Int, init: Boolean = false): Int {
        if (colorLayerCount.value <2 || colorLayerCount.value >3) return 0
        val colors =
            IntArray(colorLayerCount.value).mapIndexed { index, _ -> getCurrentColorForLayer(index) }
        return paletteItems.getCurrentPaletteIndex(colors.toIntArray())
    }

    open fun getCurrentImageBackground(): String? = null

    open fun onColorSelected(layer: Int, colorID: Int) {
        if (colorID == COLOR_PICKER_ITEM) {
            selectedPage.value = ColorDialogPage.COLOR_PICKER
            colorPickerShow?.invoke(layer)
            return

        }
        if (colorID == REMOVE_ITEM) {
            colorsForAnalytics.remove(layer)
            onColorReset(layer)
            return
        }
        val color = paletteItems.getColorListForLayer(layer)[colorID]
        colorsForAnalytics[layer] = color
        onPickColor(layer, color)
    }

    fun onChangedNotify(inspView: InspView<*>) {
        inspView.templateParent.template.palette.resetPaletteChoiceColor(
            inspView.media.id,
            inspView is InspTextView
        )
        inspView.templateParent.isChanged.value = true
    }

    fun hasAdditionalSliders() = enableRoundnessChange

    fun onPageSelected(page: ColorDialogPage) {
        selectedPage.value = page
        updatePageLauerCount()
    }

    companion object {
        val pages = CommonMenu<ColorDialogPage>().apply {
            setTextMenuItem(
                item = ColorDialogPage.COLOR,
                text = MR.strings.instrument_text_color
            )
            setTextMenuItem(
                item = ColorDialogPage.GRADIENT,
                text = MR.strings.category_gradient
            )
            setTextMenuItem(
                item = ColorDialogPage.PALETTE,
                text = MR.strings.instrument_palette
            )
            setTextMenuItem(
                item = ColorDialogPage.IMAGE,
                text = MR.strings.palette_option_image
            )
            setTextMenuItem(
                item = ColorDialogPage.OPACITY,
                text = MR.strings.instrument_opacity
            )
            setMenuItem(
                item = ColorDialogPage.ROUNDNESS,
                text = MR.strings.instrument_round,
                icon = "ic_round_icon"
            )
        }
    }
}

enum class ColorDialogPage {
    COLOR,
    GRADIENT,
    PALETTE,
    OPACITY,
    COLOR_PICKER,
    ROUNDNESS,
    IMAGE
}