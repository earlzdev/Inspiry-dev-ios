package app.inspiry.core.animator

import app.inspiry.core.animator.interpolator.InspInterpolator
import app.inspiry.core.data.FRAME_IN_MILLIS
import app.inspiry.core.serialization.InterpolatorSerializer
import app.inspiry.core.media.PartInfo
import app.inspiry.core.media.TextPartType
import app.inspiry.views.maxByReturnMax
import kotlinx.serialization.Serializable
import kotlin.math.max
import kotlin.math.roundToInt

@Serializable
class TextAnimationParams(
    var textAnimatorGroups: List<TextAnimatorGroups> = emptyList(),
    var backgroundAnimatorGroups: List<TextAnimatorGroups> = emptyList(),

    var charDelayMillis: Double = 0.0,
    var wordDelayMillis: Double = 0.0,
    var lineDelayMillis: Double = 0.0,

    @Serializable(with = InterpolatorSerializer::class)
    val charInterpolator: InspInterpolator? = null,
    @Serializable(with = InterpolatorSerializer::class)
    val wordInterpolator: InspInterpolator? = null,
    @Serializable(with = InterpolatorSerializer::class)
    val lineInterpolator: InspInterpolator? = null,

    val shuffle: Boolean = false,
    val charDelayBetweenWords: Boolean = false,
    val reverse: Boolean = false,
    var charsOnCircle: Boolean = false,
) {
    //apple default constructor
    constructor() : this(
        textAnimatorGroups = emptyList(),
        backgroundAnimatorGroups = emptyList(),

        charDelayMillis = 0.0,
        wordDelayMillis = 0.0,
        lineDelayMillis = 0.0,
        charInterpolator = null,
        wordInterpolator = null,
        lineInterpolator = null,

        shuffle = false,
        charDelayBetweenWords = false,
        reverse = false,
        charsOnCircle = false,
    )


    private fun applyInterpolatorToPart(
        interpolator: InspInterpolator?,
        parts: List<PartInfo>
    ) {

        if (interpolator != null && parts.isNotEmpty()) {
            val maxStartTime = parts.last().startTime

            for (part in parts) {

                val percentStart = part.startTime / maxStartTime
                val newPercent = interpolator.getInterpolation(percentStart.toFloat())

                part.startTime = (newPercent * maxStartTime)
            }
        }
    }

    fun applyInterpolators(
        allChars: MutableList<PartInfo>,
        allWords: MutableList<PartInfo>,
        allLines: MutableList<PartInfo>
    ) {
        applyInterpolatorToPart(charInterpolator, allChars)
        applyInterpolatorToPart(wordInterpolator, allWords)
        applyInterpolatorToPart(lineInterpolator, allLines)
    }

    //TODO: probably we need to consider in which group our animator is.
    // In a case when we will use animators with different durations
    fun calcDuration(
        charsCount: Int,
        wordCount: Int,
        lineCount: Int,
        includeStartTime: Boolean
    ): Int {

        val animatorsDurations = textAnimatorGroups.maxByReturnMax {
            it.animators.maxByReturnMax { (if (includeStartTime) it.startFrame else 0) + it.duration }
                ?: 0
        } ?: 0
        val backgroundAnimatorDurations = backgroundAnimatorGroups
            .maxByReturnMax {
                it.animators.maxByReturnMax { (if (includeStartTime) it.startFrame else 0) + it.duration }
                    ?: 0
            }
            ?: 0

        val sumPartsDelay =
            (lineCount - 1.0) * lineDelayMillis + (wordCount - 1.0) * wordDelayMillis + (charsCount - 1.0) * charDelayMillis


        return max(
            backgroundAnimatorDurations,
            animatorsDurations
        ) + (sumPartsDelay / FRAME_IN_MILLIS).roundToInt()
    }

    override fun toString(): String {
        return "TextAnimationParams(textAnimatorGroups=${textAnimatorGroups.size}, backgroundAnimatorGroups=${backgroundAnimatorGroups.size}, charDelayMillis=$charDelayMillis, wordDelayMillis=$wordDelayMillis, lineDelayMillis=$lineDelayMillis, shuffle=$shuffle, charDelayBetweenWords=$charDelayBetweenWords, reverse=$reverse)"
    }


    val textPartType: TextPartType
        get() = when {
            charDelayMillis != 0.0 || charsOnCircle -> TextPartType.character
            wordDelayMillis != 0.0 -> TextPartType.word
            lineDelayMillis != 0.0 -> TextPartType.line
            else -> TextPartType.line
        }

}