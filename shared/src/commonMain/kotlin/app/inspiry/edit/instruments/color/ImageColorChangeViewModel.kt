package app.inspiry.edit.instruments.color

import app.inspiry.core.analytics.AnalyticsManager
import app.inspiry.palette.model.PaletteLinearGradient
import app.inspiry.views.media.ColorFilterMode
import app.inspiry.views.media.InspMediaView

class ImageColorChangeViewModel(
    inspView: InspMediaView,
    analyticsManager: AnalyticsManager
) : ColorDialogViewModel(inspView, analyticsManager) {

    override val colorFilterList: MutableList<ColorFilterMode> = mutableListOf(
        ColorFilterMode.DEFAULT,
        ColorFilterMode.SCREEN,
        ColorFilterMode.MULTIPLY,
        ColorFilterMode.DARKEN,
        ColorFilterMode.OVERLAY,
        ColorFilterMode.LIGHTEN,
        ColorFilterMode.ADD
    )

    private val inspView: InspMediaView
        get() = selectedView.value as? InspMediaView
            ?: throw IllegalStateException("invalid Media type, id = (${selectedView.value?.media?.id})")

    override fun initDefaults() {
        paletteLayerCount.value = 0
        alphaOneLayer.value = true
        colorLayerCount.value = 1
        gradientLayerCount.value = if (hasBackground) 1 else 0
        currentColorFilter.value = inspView.media.colorFilterMode
        inspView.rememberInitialColors()
        super.initDefaults()
    }

    private val hasBackground: Boolean
        get() = inspView.media.hasBackground()

    override fun onGradientSelected(layer: Int, gradientID: Int): PaletteLinearGradient? {
        super.onGradientSelected(layer, gradientID)?.let {
            inspView.setNewBackgroundGradient(it)
        }
        return null
    }
    override fun onColorFilterChanged(newMode: ColorFilterMode) {
        inspView.media.colorFilterMode = newMode
        currentColorFilter.value = newMode
        super.onColorFilterChanged(newMode)
    }
    fun setOpacityForLayer(layer: Int, value: Float) {
        inspView.setColorFilter(null, value)
        onChangedNotify(inspView)
    }

    override fun onOpacityChanged(layer: Int, value: Float) {
        setOpacityForLayer(layer, value)
    }

    override fun onColorReset(layer: Int) {
        if (hasBackground) inspView.resetBackgroundColor()
        else inspView.setColorFilter(null, null)
        onChangedNotify(inspView)
    }

    override fun onGradientReset(layer: Int) {
        inspView.resetBackgroundColor()
        onChangedNotify(inspView)
    }

    override fun onPaletteReset(layer: Int) {

    }

    override fun onPickColor(layer: Int, color: Int) {
        paletteItems.updateCurrentColorForLayer(layer, color)
        if (hasBackground) inspView.setNewBackgroundColor(color)
        else inspView.setColorFilter(color, getCurrentAlphaForLayer(0))
        onChangedNotify(inspView)
    }

    override fun getCurrentColorForLayer(layer: Int): Int {
        return if (hasBackground) inspView.media.backgroundColor
        else inspView.media.colorFilter ?: 0
    }

    override fun getCurrentGradientForLayer(layer: Int): PaletteLinearGradient? {
        return if (hasBackground) inspView.media.backgroundGradient
        else null
    }

    override fun getCurrentAlphaForLayer(layer: Int): Float {
        return inspView.media.alpha
    }

    override fun getCurrentColorIndexForLayer(layer: Int, init: Boolean): Int {
        val color = getCurrentColorForLayer(layer)
        val index = paletteItems.getCurrentColorIndexForLayer(layer, color)
        if (init && index < 0 && color != 0) {
            paletteItems.updateCurrentColorForLayer(layer, color)
        }
        return index

    }

    override val enableBorderChange = false

    override val enableRoundnessChange = false

    override fun onBorderChange(value: Float) {}
    override fun onRoundnessChange(value: Float) {}

}