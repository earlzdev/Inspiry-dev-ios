package app.inspiry.core.animator.appliers

import app.inspiry.core.animator.text.DrawBackgroundAnimParam
import app.inspiry.views.InspView
import app.inspiry.views.text.InnerGenericText
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.math.ceil

@Serializable
@SerialName("blink")
class BlinkAnimApplier(val blinks: Int) : AnimApplier() {

    fun getAlpha(value: Float): Float {
        val intervals = blinks * 2
        val interval = ceil(intervals * value)

        return if (interval % 2 == 0f) {
            1f
        } else {
            0f
        }
    }

    override fun onPreDraw(view: InspView<*>, value: Float) {
        view.setNewAlpha(getAlpha(value))
    }

    override fun transformText(
        param: DrawBackgroundAnimParam,
        value: Float,
        view: InnerGenericText<*>
    ) {

        param.alpha = getAlpha(value)
    }
}