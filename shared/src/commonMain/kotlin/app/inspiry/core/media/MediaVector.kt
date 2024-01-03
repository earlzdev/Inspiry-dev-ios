package app.inspiry.core.media

import app.inspiry.core.data.TouchAction
import app.inspiry.core.animator.InspAnimator
import app.inspiry.core.animator.clipmask.shape.ShapeType
import app.inspiry.core.serialization.*
import app.inspiry.palette.model.MediaPalette
import app.inspiry.palette.model.PaletteLinearGradient
import kotlinx.serialization.Required
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
@SerialName("vector")
class MediaVector(
    @Required
    var originalSource: String,
    var isLoopEnabled: Boolean? = null,
    @Serializable(with = LayoutPositionSerializer::class)
    override var layoutPosition: LayoutPosition, override var id: String? = null,
    override var translationX: Float = 0f,
    override var translationY: Float = 0f, override var rotation: Float = 0f,
    @Serializable(with = ColorSerializer::class)
    override var backgroundColor: Int = 0, override var textureIndex: Int? = null,
    @Serializable(with = MinDurationSerializer::class)
    override var minDuration: Int = 0,
    override var startFrame: Int = 0,
    override var delayBeforeEnd: Int = 0,
    override var animatorsIn: List<@Serializable(with = AnimatorSerializer::class) InspAnimator> = emptyList(),
    override var animatorsOut: List<@Serializable(with = AnimatorSerializer::class) InspAnimator> = emptyList(),
    override var animatorsAll: List<@Serializable(with = AnimatorSerializer::class) InspAnimator> = emptyList(),
    override var loopedAnimationInterval: Int? = null, override var canMoveY: Boolean? = null,
    override var canMoveX: Boolean? = null,
    override var isMovable: Boolean? = null,
    override var cornerRadiusPosition: CornerRadiusPosition? = null,
    val scaleType: ScaleType? = null,
    override var forPremium: Boolean = false,
    var mediaPalette: MediaPalette = MediaPalette(bgImageOrGradientCanBeSet = false),
    var staticFrameForEdit: Int? = null,
    override var backgroundGradient: PaletteLinearGradient? = null,
    override var dependsOnParent: Boolean = false,
    override var isSocialIcon: Boolean = false,
    var isLottieAnimEnabled: Boolean = true,
    override var keepAspect: Boolean = false,
    override var touchActions: List<TouchAction>? = null,
    override var isTemporaryMedia: Boolean = false,
    override var colorChangeDisabled: Boolean = false,
    var blurEasing: Boolean = false,
    override var shape: ShapeType? = null,
    ) : Media(), CanBeSocialIcon {

    var defaultSource: String? = null

    val vectorAsTextBg: Boolean
        get() = id?.startsWith("vectorAsTextBg") == true

    override fun toString(): String {
        return "MediaVector(originalSource='$originalSource', isLoopEnabled=$isLoopEnabled, mediaPalette=$mediaPalette)"
    }

    companion object {
        const val STATIC_FRAME_FOR_EDIT_LAST = -1
        const val STATIC_FRAME_FOR_EDIT_MIDDLE = -2
    }

    fun toMediaImage(newOriginalSource: String): Media {
        return MediaImage(
            layoutPosition = this.layoutPosition,
            translationX = this.translationX,
            translationY = this.translationY,
            id = this.id,
            rotation = this.rotation,
            backgroundColor = this.backgroundColor,
            textureIndex = this.textureIndex,
            minDuration =  this.minDuration,
            startFrame = this.startFrame,
            delayBeforeEnd = this.delayBeforeEnd,
            animatorsIn = this.animatorsIn,
            animatorsOut = this.animatorsOut,
            animatorsAll = this.animatorsAll,
            loopedAnimationInterval = this.loopedAnimationInterval,
            canMoveX = this.canMoveX,
            canMoveY = this.canMoveY,
            isMovable = this.isMovable,
            isSocialIcon = this.isSocialIcon,
            isEditable = false,
            cornerRadiusPosition = this.cornerRadiusPosition,
            dependsOnParent = this.dependsOnParent,
            touchActions = this.touchActions
        ).apply {
            originalSource = newOriginalSource
        }
    }
}
