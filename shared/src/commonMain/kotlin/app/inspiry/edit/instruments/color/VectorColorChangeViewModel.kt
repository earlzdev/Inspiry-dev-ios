package app.inspiry.edit.instruments.color

import app.inspiry.core.analytics.AnalyticsManager
import app.inspiry.core.log.GlobalLogger
import app.inspiry.palette.model.PaletteLinearGradient
import app.inspiry.views.InspView
import app.inspiry.views.text.InspTextView
import app.inspiry.views.vector.InspVectorView
import kotlinx.coroutines.flow.MutableStateFlow

class VectorColorChangeViewModel(
    inspView: InspVectorView,
    analyticsManager: AnalyticsManager
) : ColorDialogViewModel(inspView, analyticsManager) {

    private val inspView: InspVectorView
        get() = selectedView.value as? InspVectorView
            ?: throw IllegalStateException("invalid Media type, id = (${selectedView.value?.media?.id})")

    override fun initDefaults() {
        alphaOneLayer.value = true
        colorLayerCount.value = inspView.colorLayerCount()
        paletteLayerCount.value = if (colorLayerCount.value > 1) 1 else 0
        super.initDefaults()
        if (paletteIsAvailable()) {
            selectedPage.value = ColorDialogPage.PALETTE
            updatePageLauerCount()
        }
    }

    fun setOpacityForLayer(layer: Int, value: Float) {
        inspView.templateParent.isChanged.value = true
        inspView.setAlpha(alpha = value)
    }

    override fun onOpacityChanged(layer: Int, value: Float) {
        setOpacityForLayer(layer, value)
    }

    override fun onColorReset(layer: Int) {
        inspView.setColorForLayer(layer, null)
    }

    override fun onGradientReset(layer: Int) {
    }

    override fun onPaletteReset(layer: Int) {
        repeat(colorLayerCount.value) {
            inspView.setColorForLayer(it, null)

        }
    }

    override fun onPickColor(layer: Int, color: Int) {
        paletteItems.updateCurrentColorForLayer(layer, color)
        inspView.setColorForLayer(layer, color)
        inspView.templateParent.template.palette.resetPaletteChoiceColor(inspView.media.id, false)
        inspView.templateParent.isChanged.value = true
    }

    override fun getCurrentColorForLayer(layer: Int): Int {
        return inspView.getColorForLayer(layer) ?: 0
    }

    override fun getCurrentGradientForLayer(layer: Int): PaletteLinearGradient? {
        return null
    }

    override fun getCurrentAlphaForLayer(layer: Int): Float {
        return inspView.media.mediaPalette.alpha
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