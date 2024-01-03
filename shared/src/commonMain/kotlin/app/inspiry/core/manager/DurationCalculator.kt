package app.inspiry.core.manager

import app.inspiry.core.animator.InspAnimator
import app.inspiry.core.media.Media
import app.inspiry.views.maxByReturnMax
import kotlin.math.max

object DurationCalculator {

    fun calcDurationIn(animatorsIn: List<InspAnimator>): Int {
        return animatorsIn.maxByReturnMax { it.duration + it.startFrame } ?: 0
    }

    fun calcDurationOut(animatorsOut: List<InspAnimator>): Int {
        return animatorsOut.maxByReturnMax { it.duration } ?: 0
    }

    /**
     * When we getting minPossibleDuration for loop we don't need delayBeforeEnd to make proper loop. And need in other cases
     */
    fun getMinPossibleDuration(media: Media, durationIn: Int, durationOut: Int, includeDelayBeforeEnd: Boolean = true): Int {
        //val allDuration = media.animatorsAll.maxByReturnMax { it.duration + it.startTime } ?: 0.0
        val allDuration = 0
        val minDuration = media.minDuration

        return max(max((durationIn + durationOut) + (media.animatorsOut.maxByReturnMax { it.startFrame }
            ?: 0), allDuration) + (if (includeDelayBeforeEnd) media.delayBeforeEnd else 0),
            minDuration)
    }
}