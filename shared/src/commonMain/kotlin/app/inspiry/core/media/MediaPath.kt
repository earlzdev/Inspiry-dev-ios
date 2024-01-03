package app.inspiry.core.media

import app.inspiry.core.animator.InspAnimator
import app.inspiry.core.animator.clipmask.shape.ShapeType
import app.inspiry.core.animator.interpolator.InspInterpolator
import app.inspiry.core.data.TouchAction
import app.inspiry.core.serialization.*
import app.inspiry.palette.model.PaletteLinearGradient
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
@SerialName("path")
class MediaPath(
    @Serializable(with = ColorSerializer::class)
    var color: Int? = null,
    var paintStyle: PaintStyle = PaintStyle.STROKE,
    val strokeWidth: String? = null,
    val strokeCap: String? = null,
    val movementsConnected: Boolean = true,
    var gradient: PaletteLinearGradient? = null,
    @Serializable(with = InterpolatorSerializer::class)
    var movementsInterpolator: InspInterpolator? = null,
    val movements: List<PathMovement> = listOf(),
    var alpha: Float = 1f,
    @Serializable(with = LayoutPositionSerializer::class)
    override var layoutPosition: LayoutPosition,
    override var id: String? = null, override var translationX: Float = 0f,
    override var translationY: Float = 0f, override var rotation: Float = 0f,
    @Serializable(with = ColorSerializer::class)
    override var backgroundColor: Int = 0,
    override var textureIndex: Int? = null,
    @Serializable(with = MinDurationSerializer::class)
    override var minDuration: Int = 0,
    override var startFrame: Int = 0, override var delayBeforeEnd: Int = 0,
    override var animatorsIn: List<@Serializable(with = AnimatorSerializer::class) InspAnimator> = emptyList(),
    override var animatorsOut: List<@Serializable(with = AnimatorSerializer::class) InspAnimator> = emptyList(),
    override var animatorsAll: List<@Serializable(with = AnimatorSerializer::class) InspAnimator> = emptyList(),
    override var loopedAnimationInterval: Int? = null, override var canMoveY: Boolean? = null,
    override var canMoveX: Boolean? = null, override var isMovable: Boolean? = null,
    override var cornerRadiusPosition: CornerRadiusPosition? = null,
    override var forPremium: Boolean = false,
    override var backgroundGradient: PaletteLinearGradient? = null,
    override var dependsOnParent: Boolean = false,
    override var keepAspect: Boolean = false,
    override var touchActions: List<TouchAction>? = null,
    override var isTemporaryMedia: Boolean = false,
    override var colorChangeDisabled: Boolean = false,
    override var shape: ShapeType? = null,
    ) : Media() {

    @Serializable(with = ColorSerializer::class)
    var initialColor: Int? = null

    val colorAsTextBrother: Boolean
        get() = id?.startsWith("colorAsTextBrother") == true


    override fun toString(): String {
        return "MediaPath(color=$color, paintStyle=$paintStyle, strokeWidth=$strokeWidth, movement=$movements)"
    }
}
