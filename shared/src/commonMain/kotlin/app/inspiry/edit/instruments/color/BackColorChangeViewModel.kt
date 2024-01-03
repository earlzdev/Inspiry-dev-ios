package app.inspiry.edit.instruments.color

import app.inspiry.core.analytics.AnalyticsManager
import app.inspiry.core.analytics.putString
import app.inspiry.core.util.ArgbColorManager
import app.inspiry.palette.model.PaletteLinearGradient
import app.inspiry.views.InspView
import app.inspiry.views.media.InspMediaView
import app.inspiry.views.text.InspTextView

class BackColorChangeViewModel(
    inspView: InspTextView, analyticsManager: AnalyticsManager
) : ColorDialogViewModel(inspView, analyticsManager) {

    val inspView: InspTextView
        get() = selectedView.value as? InspTextView
            ?: throw IllegalStateException("invalid Media type, id = (${selectedView.value?.media?.id})")

    override fun sendAnalyticsEvent() {

        if (!colorWasChanged()) return
        analyticsManager.sendEvent("text_color_changed", createParams = {
            colorsForAnalytics.forEach {
                val layer =
                    if (it.key == 0) "" else (it.key + 1).toString() //"..color" for layer 0, ..color2 for layer 1, ..color3 for layer 2 etc
                putString("new_bg_color$layer", ArgbColorManager.colorToString(it.value))
            }
            gradientsForAnalytics.forEach {
                val layer = if (it.key == 0) "" else (it.key + 1).toString()
                it.value.forEachIndexed { index, color ->
                    putString(
                        "new_bg_gradient${layer}_$index",
                        ArgbColorManager.colorToString(color)
                    )
                }
            }
            inspView.templateParent.template.originalData?.toBundleAnalytics(this)
        }
        )
    }

    override fun initDefaults() {
        alphaOneLayer.value = true
        inspView.mayInitDefaults()
        colorLayerCount.value = inspView.backColorsCount()
        gradientLayerCount.value = inspView.backGradientCount()
        paletteLayerCount.value = if (colorLayerCount.value > 1) 1 else 0
        super.initDefaults()
    }

    override fun onGradientSelected(layer: Int, gradientID: Int): PaletteLinearGradient? {
        super.onGradientSelected(layer, gradientID)?.let {
            inspView.setNewBackgroundGradient(it)
        }
        return null
    }

    fun setOpacityForLayer(layer: Int, value: Float) {
        inspView.setAlphaToBackground(value)
    }

    override fun onOpacityChanged(layer: Int, value: Float) {
        setOpacityForLayer(layer, value)
    }

    override fun onColorReset(layer: Int) {
        inspView.resetBackgroundColor(layer)
    }

    override fun onGradientReset(layer: Int) {
        inspView.resetBackgroundColor(layer)
    }

    override fun onPaletteReset(layer: Int) {
        repeat(colorLayerCount.value) {
            onColorReset(it)
        }
    }

    override fun onPickColor(layer: Int, color: Int) {
        paletteItems.updateCurrentColorForLayer(layer, color)
        inspView.userChangeBackgroundColorForLayer(color, layer)
    }

    override fun getCurrentAlphaForLayer(layer: Int): Float {
        return inspView.backgroundAlpha
    }

    override fun getCurrentColorForLayer(layer: Int): Int {
        val bg = inspView.getCurrentBackground(layer)
        if (bg != null && bg !is PaletteLinearGradient) return bg.getFirstColor()
        return 0
    }

    override fun getCurrentColorIndexForLayer(layer: Int, init: Boolean): Int {
        val color = getCurrentColorForLayer(layer)
        val index = paletteItems.getCurrentColorIndexForLayer(layer, color)
        if (init && index < 0 && color != 0) {
            paletteItems.updateCurrentColorForLayer(layer, color)
        }
        return index

    }

    override fun getCurrentGradientForLayer(layer: Int): PaletteLinearGradient? {
        val bg = inspView.getCurrentBackground()
        if (bg is PaletteLinearGradient) return bg
        return null
    }

    override val enableRoundnessChange = inspView.isSimpleTextBackground()
    override fun getRoundness(): Float {
        return inspView.getRoundness()
    }

    override fun onRoundnessChange(value: Float) {
        inspView.userChangeRadius(value)
    }

}