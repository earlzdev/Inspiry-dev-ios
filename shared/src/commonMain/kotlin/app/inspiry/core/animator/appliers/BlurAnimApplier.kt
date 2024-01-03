package app.inspiry.core.animator.appliers

import app.inspiry.core.util.WorkerThread
import app.inspiry.views.InspView
import app.inspiry.views.media.InspMediaView
import kotlinx.serialization.Required
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("blur")
class BlurAnimApplier(
    override var from: Float = 0f,
    @Required
    override var to: Float
) : AnimApplier(), FloatValuesAnimApplier {

    override fun onPreDraw(view: InspView<*>, value: Float) {
        (view as? InspMediaView)
            ?.setBlurRadius(getBlurValue(value), async = true)
    }

    private fun getBlurValue(value: Float): Float {
        val degree =  (((to - from) * value + from) * 10f).toInt()
        return degree / 10f
    }


    // called when we render view.
    @WorkerThread
    override fun onPrepared(view: InspView<*>, value: Float) {
        (view as? InspMediaView)
            ?.setBlurRadius(getBlurValue(value), async = false)
    }
}