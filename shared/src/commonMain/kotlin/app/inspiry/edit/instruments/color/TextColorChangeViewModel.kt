package app.inspiry.edit.instruments.color


import app.inspiry.core.analytics.AnalyticsManager
import app.inspiry.core.analytics.putString
import app.inspiry.core.util.ArgbColorManager
import app.inspiry.palette.model.PaletteLinearGradient
import app.inspiry.views.text.InspTextView

class TextColorChangeViewModel(
    inspView: InspTextView, analyticsManager: AnalyticsManager
) : ColorDialogViewModel(inspView, analyticsManager) {

    val textView: InspTextView
        get() = selectedView.value as? InspTextView
            ?: throw IllegalStateException("invalid Media type, id = (${selectedView.value?.media?.id})")

    override fun sendAnalyticsEvent() {

        if (!colorWasChanged()) return
        analyticsManager.sendEvent("text_color_changed", createParams = {
            colorsForAnalytics.forEach {
                val layer =
                    if (it.key == 0) "" else (it.key + 1).toString() //"..color" for layer 0, ..color2 for layer 1, ..color3 for layer 2 etc
                putString("new_text_color$layer", ArgbColorManager.colorToString(it.value))
            }
            gradientsForAnalytics.forEach {
                val layer = if (it.key == 0) "" else (it.key + 1).toString()
                it.value.forEachIndexed { index, color ->
                    putString(
                        "new_text_gradient${layer}_$index",
                        ArgbColorManager.colorToString(color)
                    )
                }
            }
            textView.templateParent.template.originalData?.toBundleAnalytics(this)
        }
        )
    }

    override fun initDefaults() {
        textView.mayInitDefaults()
        colorLayerCount.value = textView.colorLayerCount()
        gradientLayerCount.value = textView.gradientslayerCount()
        paletteLayerCount.value = if (colorLayerCount.value > 1) 1 else 0
        super.initDefaults()
    }

    override fun onGradientSelected(layer: Int, gradientID: Int): PaletteLinearGradient? {
        super.onGradientSelected(layer, gradientID)?.let {
            textView.setNewTextGradient(it)
        }
        return null
    }

    fun setOpacityForLayer(layer: Int, value: Float) {
        val gradient = getCurrentGradientForLayer(layer)
        gradient?.let {
            textView.setNewTextGradient(it.getWithAlpha(value) as PaletteLinearGradient)
            return
        }
        val color = getCurrentColorForLayer(layer)
        textView.setColorForLayer(layer, ArgbColorManager.colorWithAlpha(color, value))
    }

    override fun onOpacityChanged(layer: Int, value: Float) {
        setOpacityForLayer(layer, value)
    }

    override fun onColorReset(layer: Int) {
        textView.resetColor(layer)
    }

    override fun onGradientReset(layer: Int) {
        textView.resetColor(layer)
    }

    override fun onPaletteReset(layer: Int) {
        repeat(colorLayerCount.value) {
            onColorReset(it)
        }
    }

    override fun onPickColor(layer: Int, color: Int) {
        paletteItems.updateCurrentColorForLayer(layer, color)
        textView.setColorForLayer(layer, color)
    }

    override fun getCurrentColorForLayer(layer: Int): Int {
        return textView.getColorForLayer(layer)
    }

    override fun getCurrentGradientForLayer(layer: Int): PaletteLinearGradient? {
        return textView.getGradientForLayer(layer)
    }

}