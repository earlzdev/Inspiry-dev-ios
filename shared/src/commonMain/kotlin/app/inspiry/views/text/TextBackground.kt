package app.inspiry.views.text

import app.inspiry.core.media.MediaTextDefaults
import app.inspiry.palette.model.AbsPaletteColor
import app.inspiry.palette.model.PaletteColor
import app.inspiry.palette.model.PaletteLinearGradient
import app.inspiry.views.InspView
import app.inspiry.views.group.InspGroupView
import app.inspiry.views.media.InspMediaView
import app.inspiry.views.vector.InspVectorView


abstract class TextBackground {

    var defaultBackground: AbsPaletteColor? = null
        protected set

    var defaultAlpha: Float? = null
        protected set

    protected var wasChanged: Boolean = false

    abstract fun useColorBackground(color: Int, layerId: Int = 1)
    abstract fun getBackground(layer: Int = 1): AbsPaletteColor?
    abstract fun useGradientBackground(gradient: PaletteLinearGradient)
    abstract fun resetBackground(layer: Int = 1)
    abstract fun setAlphaToBackground(alpha: Float, layer: Int? = null)
    abstract fun getAlpha(layer: Int? = null): Float
    abstract fun gradientIsAvailable(): Boolean
    abstract fun colorLayersCount(): Int
    abstract fun gradientLayersCount(): Int
    abstract fun updateDefaults(defaults: MediaTextDefaults)

    fun onUserChange() {
        if (!wasChanged) wasChanged = true
    }

    companion object {

        fun create(inspTextView: InspTextView): TextBackground {

            val parent = inspTextView.findParentIfDependsOnIt()
            var textBackground: InspView<*>? = null

            parent?.let {
                textBackground = it.inspChildren.find { view ->
                    view is InspVectorView && view.media.vectorAsTextBg
                } ?: it.inspChildren.find { view ->
                    view is InspMediaView && view.media.imageAsTextBg
                } ?: parent
            }

            return when (textBackground) {
                is InspVectorView -> VectorAsTextBackground(textBackground as InspVectorView)
                is InspMediaView -> ImageAsTextBackground(textBackground as InspMediaView)
                is InspGroupView -> GroupAsTextBackground(textBackground as InspGroupView)
                else -> SimpleTextBackground(inspTextView)
            }
        }
    }
}
