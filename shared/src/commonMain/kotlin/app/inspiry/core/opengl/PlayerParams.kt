package app.inspiry.core.opengl

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed class PlayerParams

@Serializable
@SerialName("decoder")
data class VideoPlayerParams(
    var viewStartTimeUs: Long,
    var videoStartTimeUs: Long,
    var totalDurationUs: Long,
    val isLoopEnabled: Boolean,
    var volume: Float
) : PlayerParams() {
    override fun toString(): String {
        return "DecoderPlayerParams(viewStartTimeMillis=${viewStartTimeUs / 1000}, videoStartTimeMillis=${videoStartTimeUs / 1000}, totalDurationMillis=${totalDurationUs / 1000}, isLoopEnabled=$isLoopEnabled)"
    }
}