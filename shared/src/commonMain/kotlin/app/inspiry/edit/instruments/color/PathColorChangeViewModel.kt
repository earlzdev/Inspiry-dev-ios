package app.inspiry.edit.instruments.color

import app.inspiry.core.analytics.AnalyticsManager
import app.inspiry.core.media.PaintStyle
import app.inspiry.core.util.ArgbColorManager
import app.inspiry.palette.model.PaletteLinearGradient
import app.inspiry.views.InspView
import app.inspiry.views.group.InspGroupView
import app.inspiry.views.path.InspPathView

class PathColorChangeViewModel(
    inspView: InspPathView,
    analyticsManager: AnalyticsManager
) : ColorDialogViewModel(inspView, analyticsManager) {


    private val inspView: InspPathView
        get() = selectedView.value as? InspPathView
            ?: throw IllegalStateException("invalid Media type, id = (${selectedView.value?.media?.id})")

    override fun initDefaults() {
        alphaOneLayer.value = true
        colorLayerCount.value = 1
        gradientLayerCount.value = 0
        paletteLayerCount.value = 0
        super.initDefaults()
    }


    fun setOpacityForLayer(layer: Int, value: Float) {
        onPickColor(layer, ArgbColorManager.colorWithAlpha(getCurrentColorForLayer(layer), value))
    }

    override fun onOpacityChanged(layer: Int, value: Float) {
        setOpacityForLayer(layer, value)
    }

    override fun onColorReset(layer: Int) {
        if (inspView.media.paintStyle != PaintStyle.STROKE) inspView.setColorFilter(null)
        else inspView.restoreInitialColors(layer, false)
        onChangedNotify(inspView)
    }

    override fun onGradientReset(layer: Int) {
    }

    override fun onPaletteReset(layer: Int) {

    }

    override fun onPickColor(layer: Int, color: Int) {
        paletteItems.updateCurrentColorForLayer(layer, color)

        if (inspView.media.paintStyle != PaintStyle.STROKE)
            inspView.setColorFilter(color)
        else inspView.setNewColor(color)

        onChangedNotify(inspView)
    }

    override fun getCurrentColorForLayer(layer: Int): Int {
        return inspView.getCurrentColor() ?: 0
    }

    override fun getCurrentGradientForLayer(layer: Int): PaletteLinearGradient? {
        return null
    }

    override fun getCurrentAlphaForLayer(layer: Int): Float {
        return ArgbColorManager.alphaDegree(getCurrentColorForLayer(0))
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