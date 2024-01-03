package app.inspiry.core.media

import app.inspiry.core.animator.InspAnimator
import app.inspiry.core.opengl.programPresets.TemplateMask
import app.inspiry.core.serialization.AnimatorSerializer
import app.inspiry.core.serialization.MediaImageSerializer
import app.inspiry.core.serialization.StartFrameIntervalSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class SlidesData(
    val animatorsIn: List<@Serializable(with = AnimatorSerializer::class) InspAnimator>? = null,
    val animatorsOut: List<@Serializable(with = AnimatorSerializer::class) InspAnimator>? = null,
    val animatorsAll: List<@Serializable(with = AnimatorSerializer::class) InspAnimator>? = null,

    // only applied of startFrameInterval is not 0
    val startFrame: Int = 0,

    // here it can be only Int
    val minDuration: Int? = null,
    val delayBeforeEnd: Int? = null,
    val templateMask: TemplateMask? = null,

    val maxCount: Int = DEFAULT_SLIDES_MAX_COUNT,
    val duplicateSlides: Boolean = false,

    /**
     * 0 - don't increment startFrame
     * START_FRAME_INTERVAL_WHEN_NEXT_OUT_BEGINS
     * START_FRAME_INTERVAL_WHEN_THIS_OUT_ENDS
     * START_FRAME_INTERVAL_PAUSE_AFTER_ANIMATION_IN
     * positive - this will be startFrameInterval
     * negative - cannot be
     */
    @Serializable(with = StartFrameIntervalSerializer::class)
    val startFrameInterval: Int = START_FRAME_INTERVAL_WHEN_NEXT_OUT_BEGINS,

    val predefined: List<@Serializable(with = MediaImageSerializer::class) MediaImage> = emptyList(),
) {
    @Transient
    var unpacked = false

    companion object {
        const val START_FRAME_INTERVAL_WHEN_NEXT_OUT_BEGINS = 1000000
        const val START_FRAME_INTERVAL_WHEN_THIS_OUT_ENDS = 2000000
        const val START_FRAME_INTERVAL_PAUSE_AFTER_ANIMATION_IN = 3000000

        const val ADDITIONAL_SLIDE_DURATION = 60
        const val DEFAULT_FADE_DURATION = 30
        const val TEMPLATE_AFTER_SLIDES_DURATION = 60
    }
}


private const val DEFAULT_SLIDES_MAX_COUNT = 5
