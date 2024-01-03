package app.inspiry.edit.instruments.color

import app.inspiry.core.analytics.AnalyticsManager
import app.inspiry.core.util.ArgbColorManager
import app.inspiry.palette.model.PaletteColor
import app.inspiry.palette.model.PaletteLinearGradient
import app.inspiry.palette.model.TemplatePalette
import app.inspiry.palette.sendAnalyticsPaletteClosed
import app.inspiry.views.template.InspTemplateView
import app.inspiry.views.template.applyPalette
import kotlinx.serialization.json.Json

class TemplatePaletteChangeViewModel(
    val templateView: InspTemplateView, analyticsManager: AnalyticsManager, val json: Json
) : ColorDialogViewModel(inspView = null, analyticsManager) {

    val templatePalette = templateView.template.palette

    lateinit var paletteWhenOpenedDialog: TemplatePalette

    override fun sendAnalyticsEvent() {
        analyticsManager.sendAnalyticsPaletteClosed(templatePalette, paletteWhenOpenedDialog)
    }

    override fun initDefaults() {
        templateView.maySaveInitial()
        alphaOneLayer.value = false
        colorLayerCount.value = if (templatePalette.choices.isEmpty()) 1 else templatePalette.choices.size
        gradientLayerCount.value = if (templatePalette.bgImageOrGradientCanBeSet) 1 else 0
        paletteLayerCount.value = if (colorLayerCount.value > 1) 1 else 0
        super.initDefaults()
        paletteWhenOpenedDialog = templatePalette.copyViaJson(json)
    }

    override fun onSingleMediaSelected(uri: String, isVideo: Boolean) {

        templateView.onPickedBackgroundImage(
            uri,
            if (isVideo) 0 else null
        )

        analyticsManager.onMediaSelected(
            isVideo,
            mediaCount = 1,
            originalTemplateData = templateView.template.originalData!!
        )

    }

    override fun getCurrentImageBackground(): String? {
        return templatePalette.backgroundImage
    }

    /**
     * this may be useful in the future
     */
    private fun getGradientLayersCount(): Int {
        if (!templatePalette.bgImageOrGradientCanBeSet) return 0
        if (templatePalette.choices.size == 0) return 1
        var gradientCount = 0
        templatePalette.choices.forEach { choice ->
            val count =
                choice.elements.count { it.type.contains("background") || it.type.contains("text") }
            if (count > 0) gradientCount++ else return gradientCount
        }
        return gradientCount
    }

    fun setBackgroundGradient(gradient: PaletteLinearGradient) {
        templatePalette.mainColor = gradient
        if (templatePalette.choices.isNotEmpty()) {
            templatePalette.choices[0].color = gradient.getFirstColor()
        }
        templatePalette.backgroundImage = null
        templateView.applyPalette()
    }

    override fun onGradientSelected(layer: Int, gradientID: Int): PaletteLinearGradient? {
        super.onGradientSelected(layer, gradientID)?.let {
            setBackgroundGradient(it)
        }
        return null
    }

    private fun setOpacityForLayer(layer: Int, value: Float) {
        val gradient = getCurrentGradientForLayer(0)
        if (layer == 0 && gradient != null) {
            gradient.colors.forEachIndexed { index, c ->
                templatePalette.mainColor = PaletteLinearGradient(
                    gradient.orientation,
                    gradient.colors.map { ArgbColorManager.colorWithAlpha(it, value) })
            }
            templateView.applyPalette()
            return
        }
        val color = getCurrentColorForLayer(layer)
        onPickColor(layer, ArgbColorManager.colorWithAlpha(color, value))
    }

    override fun onOpacityChanged(layer: Int, value: Float) {
        setOpacityForLayer(layer, value)
    }

    override fun onColorReset(layer: Int) {
        val initialPalette = templateView.template.initialPalette
            ?: throw IllegalStateException("Initial palette is null before revert")

        val color = getPaletteColorForLayer(initialPalette, layer)
        if (color == 0) {
            if (templatePalette.choices.size > 0) {
                templatePalette.choices[layer].color = null
                templateView.restoreOriginalColorsWithoutDefault(layer = layer)
            }
        } else if (initialPalette.choices.lastIndex >= layer) onPickColor(layer, color)

        if (layer == 0) {
            val originalGradient = getPaletteGradient(initialPalette)
            if (originalGradient != null) {
                setBackgroundGradient(originalGradient)
                return
            }
            templateView.template.initialPalette?.backgroundImage?.let {
                onSingleMediaSelected(
                    it,
                    templateView.template.initialPalette!!.backgroundVideoStartMs != null
                )
                return
            }
            onPickColor(0, color)
        }
    }

    override fun onGradientReset(layer: Int) {
        onColorReset(layer)
    }

    override fun onPaletteReset(layer: Int) {
        repeat(colorLayerCount.value) {
            onColorReset(it)
        }
    }

    override fun onPickColor(layer: Int, color: Int) {
        if (layer == 0) {
            templatePalette.mainColor = PaletteColor(color)
            if (templatePalette.bgImageOrGradientCanBeSet) {
                templatePalette.backgroundImage = null
            }
        }
        paletteItems.updateCurrentColorForLayer(layer, color)
        if (templatePalette.choices.isEmpty()) {
            templatePalette.mainColor = PaletteColor(color)
        } else {
            templatePalette.choices[layer].color = color
        }

        templateView.applyPalette()
        templateView.isChanged.value = true
    }

    override fun getCurrentAlphaForLayer(layer: Int): Float {
        if (layer == 0) {
            val gradient = getCurrentGradientForLayer(0)
            if (gradient != null) return ArgbColorManager.alphaDegree(gradient.getFirstColor())
        }
        return ArgbColorManager.alphaDegree(getCurrentColorForLayer(layer))
    }

    private fun getPaletteGradient(palette: TemplatePalette): PaletteLinearGradient? {
        val mainColor = palette.mainColor
        return if (mainColor is PaletteLinearGradient) mainColor else null
    }

    private fun getPaletteColorForLayer(palette: TemplatePalette, layer: Int): Int {
        if (palette.choices.isEmpty()) {
            val p = palette.mainColor
            if (p !is PaletteLinearGradient) return p?.getFirstColor() ?: 0
        } else {
            return palette.choices[layer].color ?: 0
        }
        return 0
    }

    override fun getCurrentColorForLayer(layer: Int) =
        getPaletteColorForLayer(templatePalette, layer)

    override fun getCurrentColorIndexForLayer(layer: Int, init: Boolean): Int {
        val color = getCurrentColorForLayer(layer)
        val index = paletteItems.getCurrentColorIndexForLayer(layer, color)
        if (init && index < 0 && color != 0) {
            paletteItems.updateCurrentColorForLayer(layer, color)
        }
        return index
    }

    override fun customImageChoiceIsAvailable() = templatePalette.bgImageOrGradientCanBeSet

    override fun getCurrentGradientForLayer(layer: Int) = getPaletteGradient(templatePalette)

}