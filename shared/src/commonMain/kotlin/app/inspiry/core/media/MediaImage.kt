package app.inspiry.core.media

import app.inspiry.core.animator.InspAnimator
import app.inspiry.core.animator.appliers.ScaleInnerAnimApplier
import app.inspiry.core.animator.appliers.ScaleOuterAnimApplier
import app.inspiry.core.animator.clipmask.shape.ShapeType
import app.inspiry.core.data.TouchAction
import app.inspiry.core.opengl.TextureCreator
import app.inspiry.core.opengl.programPresets.TemplateMask
import app.inspiry.core.serialization.*
import app.inspiry.core.util.PickMediaResult
import app.inspiry.edit.instruments.PickedMediaType
import app.inspiry.palette.model.PaletteLinearGradient
import app.inspiry.removebg.RemoveBgFileManager
import app.inspiry.views.InspView
import app.inspiry.views.maxByReturnMax
import app.inspiry.views.media.ColorFilterMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlin.math.max
import kotlin.math.min


@Serializable
@SerialName("image")
class MediaImage(
    @Serializable(with = LayoutPositionSerializer::class)
    override var layoutPosition: LayoutPosition = LayoutPosition(
        LayoutPosition.MATCH_PARENT,
        LayoutPosition.MATCH_PARENT
    ),
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
    var isLogo: Boolean = false,

    var demoSource: String? = null,
    var borderType: BorderStyle? = null,
    @Serializable(with = ColorSerializer::class)
    var borderColor: Int? = null,
    var borderWidth: String? = null,

    var isEditable: Boolean = true,
    var duplicate: String? = null,
    var isVideo: Boolean = false,
    var innerImageRotation: Float = 0f,

    var demoOffsetX: Float = 0f,
    var demoOffsetY: Float = 0f,
    var demoScale: Float = 1f,

    var innerPivotX: Float = 0.5f,
    var innerPivotY: Float = 0.5f,

    override var cornerRadiusPosition: CornerRadiusPosition? = null,

    @Serializable(with = MutableStateFlowIntSerializer::class)
    var videoStartTimeMs: MutableStateFlow<Int>? = null,
    @Serializable(with = MutableStateFlowIntSerializer::class)
    var videoEndTimeMs: MutableStateFlow<Int>? = null,
    var isLoopEnabled: Boolean? = null,

    @Serializable(with = ColorSerializer::class)
    var colorFilter: Int? = null,

    var alpha: Float = 1f,

    override var forPremium: Boolean = false,

    override var backgroundGradient: PaletteLinearGradient? = null,

    override var dependsOnParent: Boolean = false,

    override var isSocialIcon: Boolean = false,

    var imageAsTextBg: Boolean = false,

    @Serializable(with = MutableStateFlowFloatSerializer::class)
    var videoVolume: MutableStateFlow<Float>? = null,
    override var keepAspect: Boolean = false,
    var colorFilterMode: ColorFilterMode = ColorFilterMode.MULTIPLY,
    override var touchActions: List<TouchAction>? = null,
    val removeBgOnInsert: Boolean = false,
    val makeMovableWhenRemoveBg: Boolean = true,
    override var isTemporaryMedia: Boolean = false,
    override var colorChangeDisabled: Boolean = false,

    override var shape: ShapeType? = null,
    var slidesEnabled: Boolean = false,
    ) : Media(), TemplateMaskOwner, CanBeSocialIcon {
    override var templateMask: TemplateMask? = null

    var initialColors: InitialMediaColors? = null

    var innerImageScale: Float = 1f
        set(value) {
            field = checkIsFloatValid(value, fallback = 1f)
        }

    @Transient
    var programCreator: ProgramCreator? = null

    //1 means frame width/height
    var innerImageOffsetX: Float = 0f
        set(value) {
            field = checkIsFloatValid(value)
        }
    var innerImageOffsetY: Float = 0f
        set(value) {
            field = checkIsFloatValid(value)
        }

    var defaultSource: String? = null

    var undoRemoveBgData: UndoRemoveBgData? = null

    var originalSource: String? = null
        set(value) {
            field = value
            innerImageOffsetX = 0f
            innerImageOffsetY = 0f
            innerImageScale = 1f
            innerImageRotation = 0f
        }

    var scaleType: ScaleType? = null
        set(value) {
            if (isVideo) throw IllegalStateException("this value is not supported for video. ID: $id")
            field = value
        }

    /**
     * part of the image with alpha <= the threshold value
     * will not process clicks
     * @sample touchAlphaThreshold = 1f // the image will not handle any taps
     * @sample touchAlphaThreshold = 0.5f //if transparency of the area where tap occurred is less than 0.5, the click will not be handled
     * possible values 0..1
     */
    var touchAlphaThreshold: Float? = null

    fun resetColorFilter() {
        colorFilter = initialColors?.colorFilter
        initialColors?.alpha?.let { alpha = it }
    }

    fun hasUserSource() = originalSource != null

    fun hasNoEditableProgram() = programCreator != null && !isEditable

    fun hasProgramOrVideo() = programCreator != null || (isVideo && originalSource != null)

    fun hasProgram() = programCreator != null

    fun hasEditableProgram() = programCreator != null && isEditable

    fun hasVideo() = isVideo && originalSource != null || programHasVideo()

    fun programHasVideo() =
        programCreator?.textures?.any { it.type == TextureCreator.Type.VIDEO || it.type == TextureCreator.Type.VIDEO_EDIT }
            ?: false

    override fun getFilesToClean(): List<String> {

        originalSource?.let { if (RemoveBgFileManager.isRemovedBgFile(it)) return listOf(it) }

        return super.getFilesToClean()
    }

    fun getDisplaySourceNonNull(): String = getDisplaySource()!!
    fun getDisplaySource(): String? = originalSource ?: demoSource

    private fun getAnimatorScaleMax(): Float {

        fun forAnimatorsScaleInner(animators: List<InspAnimator>) =
            animators.filter { it.animationApplier is ScaleInnerAnimApplier }
                .maxByReturnMax {
                    max(
                        (it.animationApplier as ScaleInnerAnimApplier).from,
                        it.animationApplier.to
                    )
                } ?: 1f

        fun forAnimatorsScale(animators: List<InspAnimator>) =
            animators.filter { it.animationApplier is ScaleOuterAnimApplier }
                .maxByReturnMax {
                    val x = max(
                        (it.animationApplier as ScaleOuterAnimApplier).fromX,
                        it.animationApplier.toX
                    )
                    val y = max(
                        it.animationApplier.fromY,
                        it.animationApplier.toY
                    )
                    max(x, y)
                } ?: 1f



        return max(
            forAnimatorsScale(animatorsIn) * forAnimatorsScaleInner(animatorsIn),
            forAnimatorsScale(animatorsAll) * forAnimatorsScaleInner(animatorsAll)
        )
    }

    fun hasBackground() = !isEditable && (backgroundGradient != null || backgroundColor != 0)

    fun getScaledSize(
        unitsConverter: BaseUnitsConverter,
        size: Int,
        forWidth: Boolean,
        decreaseImageSize: Boolean = false
    ): Int {

        if (hasUserSource() || demoSource != null) {

            val animatorScale = getAnimatorScaleMax()

            val newScale =
                (if (hasUserSource()) innerImageScale else demoScale) * animatorScale

            if (decreaseImageSize) {

                val screenSize = if (forWidth) unitsConverter.getScreenWidth()
                else unitsConverter.getScreenHeight()

                return min(size * newScale, screenSize / 4f).toInt()

            } else if (newScale > 1.01f) {

                val newSize = (size * newScale).toInt()

                val screenSize = if (forWidth) unitsConverter.getScreenWidth()
                else unitsConverter.getScreenHeight()

                return min(
                    newSize, screenSize
                )
            }
        }

        return size
    }

    override fun toString(): String {
        return "MediaImage(hasProgram = ${hasProgram()}, demoSource=$demoSource, duplicate=$duplicate, id=$id, originalSource=$originalSource, isVideo=$isVideo)"
    }

    fun getVideoTimeOffsetUs(): Long = videoStartTimeMs?.value?.let { it * 1000L } ?: 0L

    fun toMediaVector(newOriginalSource: String): Media {
        return MediaVector(
            originalSource = newOriginalSource,
            layoutPosition = this.layoutPosition,
            translationX = this.translationX,
            translationY = this.translationY,
            id = this.id,
            rotation = this.rotation,
            backgroundColor = this.backgroundColor,
            textureIndex = this.textureIndex,
            minDuration = this.minDuration,
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
            cornerRadiusPosition = this.cornerRadiusPosition,
            isLottieAnimEnabled = false,
            dependsOnParent = this.dependsOnParent,
            touchActions = this.touchActions
        )
    }

    fun isColorFilterDisabled() = colorFilterMode == ColorFilterMode.DISABLE

    companion object {
        fun getLogoOrImageFromPath(result: PickMediaResult): MediaImage {

            val isVideo = result.type == PickedMediaType.VIDEO

            val layoutPosition =
                when {
                    result.size.height < result.size.width -> LayoutPosition(
                        width = MEDIA_SIZE_AFTER_ADDED_WIDTH.toString(),
                        height = (MEDIA_SIZE_AFTER_ADDED_WIDTH * result.size.height / result.size.width).toString(),
                        alignBy = Alignment.center
                    )
                    result.size.height > result.size.width -> LayoutPosition(
                        height = MEDIA_SIZE_AFTER_ADDED_HEIGHT.toString(),
                        width = (MEDIA_SIZE_AFTER_ADDED_HEIGHT * result.size.width / result.size.height).toString(),
                        alignBy = Alignment.center
                    )
                    else -> LayoutPosition(
                        MEDIA_SIZE_AFTER_ADDED_SQUARE,
                        MEDIA_SIZE_AFTER_ADDED_SQUARE,
                        Alignment.center
                    )
                }


            val imageFromPath = MediaImage(
                layoutPosition = layoutPosition,
                isMovable = true,
                touchActions = InspView.getDefaultMovableTouchActions(),
                keepAspect = true,
                removeBgOnInsert = false,
                isVideo = isVideo,
                colorFilterMode = if (isVideo) ColorFilterMode.DISABLE else ColorFilterMode.MULTIPLY,
                isEditable = true,
                isLogo = true
            )
            if (isVideo) {
                imageFromPath.videoVolume = MutableStateFlow(1f)
                imageFromPath.videoStartTimeMs = MutableStateFlow(0)

            }
            imageFromPath.originalSource = result.uri
            return imageFromPath
        }

        private const val MEDIA_SIZE_AFTER_ADDED_WIDTH = 964
        private const val MEDIA_SIZE_AFTER_ADDED_HEIGHT = 1080
        private const val MEDIA_SIZE_AFTER_ADDED_SQUARE = "850"
    }
}
