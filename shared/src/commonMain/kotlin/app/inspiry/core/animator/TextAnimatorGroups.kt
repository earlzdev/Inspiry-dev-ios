package app.inspiry.core.animator

import app.inspiry.core.animator.text.DrawBackgroundAnimParam
import app.inspiry.core.data.FRAME_IN_MILLIS
import app.inspiry.core.serialization.AnimatorSerializer
import app.inspiry.views.text.InnerGenericText
import kotlinx.serialization.Serializable
import kotlin.math.min

//used to apply animation to a specific line, word
@Serializable
class TextAnimatorGroups(
    val group: String,
    val animators: List<@Serializable(with = AnimatorSerializer::class) InspAnimator>
) {

    fun shouldApplyAnimation(partIndex: Int, partsCount: Int, group: String = this.group): Boolean {
        if (group == ANIMATOR_GROUP_ALL) return true

        if (group == ANIMATOR_GROUP_FIRST && partIndex == 0) return true

        if (group == ANIMATOR_GROUP_EVEN && partIndex % 2 == 1) return true

        if (group == ANIMATOR_GROUP_EVEN_OR_FIRST && (partsCount == 1 || partIndex % 2 == 1)) return true

        if (group == ANIMATOR_GROUP_UNEVEN && partIndex % 2 == 0) return true

        if (group == ANIMATOR_GROUP_UNEVEN_NOT_FIRST && (partsCount != 1 && partIndex % 2 == 0)) return true

        return false
    }

    fun showApplyShadowAnimation(partIndex: Int, partsCount: Int): Boolean {
        val s = group.split('_')
        if (s.size > 1) {
            return shouldApplyAnimation(partIndex, partsCount, s[1])
        }
        return false
    }
}

fun List<TextAnimatorGroups>.applyAnimationText(
    time: Double,
    partStartTime: Double,
    backgroundAnimParam: DrawBackgroundAnimParam,
    partIndex: Int, partsCount: Int,
    view: InnerGenericText<*>, shadowMode: Boolean, out: Boolean
) {
    var time = time
    if (out) time -= (view.media.delayBeforeEnd * FRAME_IN_MILLIS)

    forEach { group ->

        val needToApplyAnimation =
            if (shadowMode) group.showApplyShadowAnimation(partIndex, partsCount)
            else group.shouldApplyAnimation(
                partIndex, partsCount
            )

        if (needToApplyAnimation) {

            //first reset animations in reverse order (first animator should be reset at last)
            if (group.animators.isNotEmpty()) {
                for (i in (group.animators.size - 1) downTo 0) {
                    val animator = group.animators[i]

                    if (time < partStartTime + (animator.startFrame * FRAME_IN_MILLIS)) {
                        animator.applyAnimationText(backgroundAnimParam, 0f, view)
                    }
                }
            }

            //draw animations
            group.animators.forEach { animator ->

                if (time < partStartTime + (animator.startFrame * FRAME_IN_MILLIS)) {

                    //nothing. Already reset it above

                } else if (animator.duration == 0) {

                    animator.applyAnimationText(backgroundAnimParam, 1f, view)

                } else {

                    var animDegree =
                        ((time - partStartTime - (animator.startFrame * FRAME_IN_MILLIS) + FRAME_IN_MILLIS) /
                                ((animator.duration) * FRAME_IN_MILLIS)).toFloat()

                    animDegree = min(animDegree, 1f)

                    animator.applyAnimationText(backgroundAnimParam, animDegree, view)

                }
            }
        }
    }
}

const val ANIMATOR_GROUP_ALL = "all"
const val ANIMATOR_GROUP_EVEN = "even"
const val ANIMATOR_GROUP_EVEN_OR_FIRST = "even_or_first"
const val ANIMATOR_GROUP_UNEVEN_NOT_FIRST = "uneven_not_first"
const val ANIMATOR_GROUP_UNEVEN = "uneven"
const val ANIMATOR_GROUP_FIRST = "first"


