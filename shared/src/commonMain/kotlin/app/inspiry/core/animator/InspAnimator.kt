package app.inspiry.core.animator

import app.inspiry.core.animator.appliers.AnimApplier
import app.inspiry.core.animator.appliers.ChangeableAnimApplier
import app.inspiry.core.animator.appliers.MoveAnimApplier
import app.inspiry.core.animator.interpolator.InspInterpolator
import app.inspiry.core.animator.text.DrawBackgroundAnimParam
import app.inspiry.core.media.Media
import app.inspiry.core.serialization.AnimatorDurationSerializer
import app.inspiry.core.serialization.InterpolatorSerializer
import app.inspiry.core.util.WorkerThread
import app.inspiry.views.InspView
import app.inspiry.views.text.InnerGenericText
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

//startTime - for outAnimator it increases the whole animation time

@Serializable
class InspAnimator(
    var startFrame: Int = 0,
    @Serializable(with = AnimatorDurationSerializer::class)
    var duration: Int = 0,
    @Serializable(with = InterpolatorSerializer::class)
    val interpolator: InspInterpolator? = null,
    val animationApplier: AnimApplier
) {
    //we can use the same optimization for the first frame. Makes sense.
    @Transient
    var lastFrameAnimTriggered: Boolean = true


    override fun toString(): String {
        return "InspAnimator(startFrame=$startFrame, duration=$duration, " +
                "interpolator=${interpolator}, animationApplier=${animationApplier::class.simpleName})"
    }

    fun preDrawAnimation(view: InspView<*>, degree: Float) {
        animationApplier.onPreDraw(view, interpolator?.getInterpolation(degree) ?: degree)
    }

    @WorkerThread
    fun prepareAnimation(view: InspView<*>, degree: Float) {
        animationApplier.onPrepared(view, interpolator?.getInterpolation(degree) ?: degree)
    }

    /**
     * Used when animation data has been modified by the user
     * eg changed the colors for text animation
     */
    fun updateAnimationValues(media: Media) {
        if (animationApplier is ChangeableAnimApplier) animationApplier.onValuesChanged(media)
    }

    /**
     * @param isPrepared - used to able to call this method repeatedly
     *      Bad practice, but need to avoid extra code
     */
    fun applyAnimation(
        currentFrame: Int,
        animBeginning: Int,
        isPrepared: Boolean = false,
        applyAnimation: (Float) -> Unit
    ) {

        val actualFrame = currentFrame - animBeginning

        if (actualFrame >= 0) {

            if (actualFrame <= (duration + 1) || !lastFrameAnimTriggered) {
                val animInnerVal: Float

                if (actualFrame >= duration || duration == 1 || duration == 0) {
                    animInnerVal = 1f
                    if (!isPrepared) lastFrameAnimTriggered = true

                } else {
                    animInnerVal = (actualFrame.toFloat() / (duration - 1f))
                    lastFrameAnimTriggered = false
                }

                applyAnimation(animInnerVal)
            }
        }
    }

    fun applyAnimationText(
        param: DrawBackgroundAnimParam,
        animDegree: Float,
        view: InnerGenericText<*>
    ) {
        animationApplier.transformText(param, interpolator?.getInterpolation(animDegree)
            ?: animDegree, view)
    }

    companion object {
        const val DURATION_ALL = -1000000
        const val DURATION_IN = -2000000
        const val DURATION_AS_TEMPLATE = -3000000
    }
}