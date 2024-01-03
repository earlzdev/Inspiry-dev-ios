package app.inspiry.views.vector

import app.inspiry.core.media.ScaleType
import app.inspiry.palette.model.PaletteLinearGradient

interface InnerVectorView {
    var lottieFrame: Int

    var onInitialized: ((Float, Int) -> Unit)?
    var onFailedToInitialize: ((Throwable?) -> Unit)?

    fun setScaleType(scaleType: ScaleType)
    fun setColorKeyPath(color: Int, vararg key: String)
    fun setGradientKeyPath(gradient: PaletteLinearGradient, vararg key: String)
    fun resetColorKeyPath(vararg key: String)
    fun setColorFilter(color: Int?)
    fun loadAnimation(originalSource: String, reduceBlur: Boolean)
    fun loadAnimation(originalSource: String, isLottieAnimEnabled: Boolean, reduceBlur: Boolean)
    fun loadSvg(originalSource: String)
    fun clearDisplayResource()

    val viewFps: Int
}