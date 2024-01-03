package app.inspiry.core.media

import app.inspiry.core.data.TouchAction
import app.inspiry.core.animator.InspAnimator
import app.inspiry.core.animator.clipmask.shape.ShapeType
import app.inspiry.core.opengl.programPresets.TemplateMask
import app.inspiry.core.serialization.*
import app.inspiry.palette.model.PaletteLinearGradient
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("group")
class MediaGroup(
    val clipChildren: Boolean? = null,
    var medias: MutableList<@Serializable(with = MediaSerializer::class) Media> = mutableListOf(),
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
    override var forPremium: Boolean = false,

    override var cornerRadiusPosition: CornerRadiusPosition? = null,
    var orientation: GroupOrientation = GroupOrientation.Z,
    override var backgroundGradient: PaletteLinearGradient? = null,
    override var dependsOnParent: Boolean = false,
    override var keepAspect: Boolean = false,

    var borderType: BorderStyle? = null,
    @Serializable(with = ColorSerializer::class)
    var borderColor: Int? = null,
    var borderWidth: String? = null,
    override var touchActions: List<TouchAction>? = null,
    override var isTemporaryMedia: Boolean = false,
    override var colorChangeDisabled: Boolean = false,
    override var shape: ShapeType? = null,
) : Media(), TemplateMaskOwner {

    override var templateMask: TemplateMask? = null

    var slides: SlidesData? = null

    private fun hasSocialIcon(): Boolean = medias.any { it.isSocialImageOrVector() }

    override fun forPremium(): Boolean {
        return forPremium || medias.any { it.forPremium() }
    }

    /**
     * It means than when we perform operations like move,
     * scale, rotate, remove and so on, we treat this view differently
     */
    fun isGroupLinked(): Boolean = orientation != GroupOrientation.Z && !hasSocialIcon()

    //this group delayBeforeEnd will be set in InspAnimatingView::afterCalcDurations
    // (because this layer duration should be synced with the text)
    override fun preprocessPresetMediaText() {

        val selectedTextView = selectTextView()
        selectedTextView?.delayBeforeEnd = DEFAULT_TEXT_DELAY_BEFORE_END

        if (animatorsOut.isEmpty() && selectedTextView?.animatorsOut.isNullOrEmpty() && selectedTextView?.animationParamOut == null) {
            animatorsOut = mutableListOf<InspAnimator>().also { it.add(getDefaultOutAnimator()) }
        }
    }

    override fun toString(): String {
        return "MediaGroup(medias=${medias.size}, id=$id, textureIndex=$textureIndex, isMovable=$isMovable)"
    }
}
