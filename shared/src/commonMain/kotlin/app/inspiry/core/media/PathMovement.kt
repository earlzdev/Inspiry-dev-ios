package app.inspiry.core.media

import app.inspiry.core.animator.interpolator.InspInterpolator
import app.inspiry.core.serialization.InterpolatorSerializer
import app.inspiry.views.maxByReturnMax
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient


@Serializable
sealed class PathMovement {
    abstract val startFrame: Int
    abstract val duration: Int

    @Serializable(with = InterpolatorSerializer::class)
    abstract val interpolator: InspInterpolator?
}

@Serializable
@SerialName("close")
class PathCloseMovement(override val startFrame: Int = 0, override val duration: Int = 0) : PathMovement() {
    @Transient
    override val interpolator: InspInterpolator? = null
}

@Serializable
@SerialName("line")
class PathLineMovement(
    val fromX: Float = 0f,
    val toX: Float = 0f,
    val fromY: Float = 0f,
    val toY: Float = 0f,
    override val startFrame: Int = 0, override val duration: Int = 0,
    @Serializable(with = InterpolatorSerializer::class)
    override val interpolator: InspInterpolator? = null
) :
    PathMovement()


@Serializable
@SerialName("strokeWidth")
class StrokeWidthMovement(
    val from: String? = null,
    val to: String? = null,
    override val startFrame: Int = 0, override val duration: Int = 0,
    @Serializable(with = InterpolatorSerializer::class)
    override val interpolator: InspInterpolator? = null
) : PathMovement()

fun List<PathMovement>.getMovementsDuration() = maxByReturnMax { it.duration + it.startFrame } ?: 0