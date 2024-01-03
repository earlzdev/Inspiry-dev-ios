package app.inspiry.edit.instruments.color

import app.inspiry.core.analytics.AnalyticsManager
import app.inspiry.core.util.ArgbColorManager
import app.inspiry.palette.model.MediaPalette
import app.inspiry.palette.model.PaletteColor
import app.inspiry.palette.model.PaletteLinearGradient
import app.inspiry.views.InspView
import app.inspiry.views.group.InspGroupView
import app.inspiry.views.text.InspTextView

class GroupColorChangeViewModel(
    inspView: InspGroupView,
    analyticsManager: AnalyticsManager
) : ColorDialogViewModel(inspView, analyticsManager) {

    private val inspView: InspGroupView
        get() = selectedView.value as? InspGroupView
            ?: throw IllegalStateException("invalid Media type, id = (${selectedView.value?.media?.id})")

    override fun initDefaults() {
        alphaOneLayer.value = true
        colorLayerCount.value = 1
        gradientLayerCount.value = 0
        paletteLayerCount.value = 0
        super.initDefaults()
    }

    fun setOpacityForLayer(layer: Int, value: Float) {
        inspView.colorFilterForChilds(palette = MediaPalette(alpha = value))
        onChangedNotify(inspView)
    }

    override fun onOpacityChanged(layer: Int, value: Float) {
        setOpacityForLayer(layer, value)
    }

    override fun onColorReset(layer: Int) {
        inspView.resetColorFilterForChilds()
        onChangedNotify(inspView)
    }

    override fun onGradientReset(layer: Int) {
    }

    override fun onPaletteReset(layer: Int) {

    }

    override fun onPickColor(layer: Int, color: Int) {
        inspView.colorFilterForChilds(
            palette = MediaPalette(
                mainColor = PaletteColor(color),
                alpha = ArgbColorManager.alphaDegree(color)
            )
        )
        onChangedNotify(inspView)
    }

    override fun getCurrentColorForLayer(layer: Int): Int {
        return inspView.getCurrentColor() ?: 0
    }

    override fun getCurrentGradientForLayer(layer: Int): PaletteLinearGradient? {
        return null
    }

    override fun getCurrentAlphaForLayer(layer: Int): Float {
        return inspView.getCurrentAlpha()
    }

    override val enableBorderChange = false

    override val enableRoundnessChange = false

    override fun onBorderChange(value: Float) {}
    override fun onRoundnessChange(value: Float) {}

}