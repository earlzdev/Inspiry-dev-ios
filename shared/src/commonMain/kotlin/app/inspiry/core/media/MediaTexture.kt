package app.inspiry.core.media

import app.inspiry.core.animator.InspAnimator
import app.inspiry.core.animator.clipmask.shape.ShapeType
import app.inspiry.core.data.Rect
import app.inspiry.core.data.TouchAction
import app.inspiry.core.opengl.ClipRegion
import app.inspiry.core.opengl.TextureMatrixData
import app.inspiry.core.serialization.*
import app.inspiry.core.util.getExt
import app.inspiry.palette.model.PaletteLinearGradient
import app.inspiry.views.media.ColorFilterMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("textureMedia")
class MediaTexture (
    @Serializable(with = LayoutPositionSerializer::class)
    override var layoutPosition: LayoutPosition,
    override var id: String? = null,
    override var translationX: Float = 0f,
    override var translationY: Float = 0f,
    override var rotation: Float = 0f,
    @Serializable(with = ColorSerializer::class)
    override var backgroundColor: Int = 0,
    override var textureIndex: Int? = null,
    @Serializable(with = MinDurationSerializer::class)
    override var minDuration: Int = 0,
    override var startFrame: Int = 0,
    override var delayBeforeEnd: Int = 0,
    override var animatorsIn: List<@Serializable(with = AnimatorSerializer::class) InspAnimator> = emptyList(),
    override var animatorsOut: List<@Serializable(with = AnimatorSerializer::class) InspAnimator> = emptyList(),
    override var animatorsAll: List<@Serializable(with = AnimatorSerializer::class) InspAnimator> = emptyList(),
    override var loopedAnimationInterval: Int? = null,
    override var canMoveY: Boolean? = null,
    override var canMoveX: Boolean? = null,
    override var isMovable: Boolean? = null,
    override var colorChangeDisabled: Boolean = false,
    var demoSource: String? = null,
    var textureSource: String? = null,

    var isEditable: Boolean = true,

    var demoOffsetX: Float = 0f,
    var demoOffsetY: Float = 0f,
    var demoScale: Float = 1f,

    var innerPivotX: Float = 0.5f,
    var innerPivotY: Float = 0.5f,

    override var cornerRadiusPosition: CornerRadiusPosition? = null,

    var isLoopEnabled: Boolean = false,

    var alpha: Float = 1f,

    override var forPremium: Boolean = false,

    override var backgroundGradient: PaletteLinearGradient? = null,

    override var dependsOnParent: Boolean = false,

    override var keepAspect: Boolean = false,
    override var touchActions: List<TouchAction>? = null,
    override var isTemporaryMedia: Boolean = false,

    var innerLayoutPosition: LayoutPosition? = null,

    override var shape: ShapeType? = null,

    ): Media() {

    val isVideo: Boolean
        get() = textureSource?.getExt().equals("mp4")

    var scaleType: ScaleType? = null
        set(value) {
            if (isVideo) throw IllegalStateException("this value is not supported for video. ID: $id")
            field = value
        }



    val clipTextures: List<ClipRegion>? = null
    val isPixelSizeAvailable: Boolean = false
    val isBlurEffectAvailable: Boolean = false
}