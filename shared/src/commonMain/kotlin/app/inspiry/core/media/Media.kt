package app.inspiry.core.media

import app.inspiry.core.animator.InspAnimator
import app.inspiry.core.animator.appliers.AnimApplier
import app.inspiry.core.animator.appliers.FadeAnimApplier
import app.inspiry.core.animator.clipmask.shape.ShapeType
import app.inspiry.core.data.TouchAction
import app.inspiry.core.manager.DebugManager
import app.inspiry.core.serialization.*
import app.inspiry.palette.model.PaletteLinearGradient
import app.inspiry.views.InspView
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.Json

@Serializable
sealed class Media {
    @Serializable(with = LayoutPositionSerializer::class)
    abstract var layoutPosition: LayoutPosition
    abstract var id: String?
    abstract var translationX: Float
        protected set
    abstract var translationY: Float
        protected set

    abstract var rotation: Float

    @Serializable(with = ColorSerializer::class)
    abstract var backgroundColor: Int
    abstract var backgroundGradient: PaletteLinearGradient?
    abstract var textureIndex: Int?

    //min duration can be decreased for InspTextView in timeline. For others not.
    @Serializable(with = MinDurationSerializer::class)
    abstract var minDuration: Int

    //We dont use this startTime in calculating duration of outAnimations
    abstract var startFrame: Int

    //is added to total duration.
    //if there's out animator, then this is added between out and other durations. Otherwise it adds delay before end.

    abstract var delayBeforeEnd: Int
    abstract var animatorsIn: List<@Serializable(with = AnimatorSerializer::class) InspAnimator>
    abstract var animatorsOut: List<@Serializable(with = AnimatorSerializer::class) InspAnimator>

    /**
     * These animators can have dynamic durations + it doesn't count in total duration of a media.
     * + they don't loop.
     */
    abstract var animatorsAll: List<@Serializable(with = AnimatorSerializer::class) InspAnimator>

    //if non null - loop animation
    abstract var loopedAnimationInterval: Int?

    abstract var canMoveY: Boolean?
    abstract var canMoveX: Boolean?

    //if true then it can be selected and moved like textView
    abstract var isMovable: Boolean?
    //keep aspect when media scale
    abstract var keepAspect: Boolean

    abstract var cornerRadiusPosition: CornerRadiusPosition?

    abstract var forPremium: Boolean

    abstract var dependsOnParent: Boolean

    abstract var touchActions: List<TouchAction>?

    abstract var isTemporaryMedia: Boolean

    abstract var colorChangeDisabled: Boolean

    abstract var shape: ShapeType?

    fun startFrameRemoveShortcut(): Int = if (startFrame < 0) 0 else startFrame

    @Transient
    var view: InspView<*>? = null

    fun copy(json: Json): Media {
        return json.decodeFromString(MediaSerializer, json.encodeToString(MediaSerializer, this))
    }

    fun canMoveY() = canMoveY == null || canMoveY!!
    fun canMoveX() = canMoveX == null || canMoveX!!

    fun buttonIsAvailable(action: TouchAction): Boolean = touchActions?.contains(action) ?: false

    open fun getFilesToClean(): List<String> = emptyList()

    inline fun <reified T : AnimApplier> isHasAnimApplier() =
        animatorsIn.any { it.animationApplier is T } ||
                animatorsOut.any { it.animationApplier is T } ||
                animatorsAll.any { it.animationApplier is T }

    protected fun checkIsFloatValid(value: Float, fallback: Float = 0f): Float {
        return if (value.isNaN() || value.isInfinite()) {
            if (DebugManager.isDebug) {
                throw IllegalStateException("newTranslation is invalid $value")
            } else {
                fallback
            }
        } else
            value
    }
    fun setNewTranslationX(value: Float) {
        this.translationX = checkIsFloatValid(value)
    }
    fun setNewTranslationY(value: Float) {
        this.translationY = checkIsFloatValid(value)
    }

    fun isSocialImageOrVector() = this is CanBeSocialIcon && this.isSocialIcon

    override fun toString(): String {
        return "Media(layoutPosition=$layoutPosition, backgroundColor=$backgroundColor, animatorsIn=$animatorsIn, animatorsOut=$animatorsOut, animatorsAll=$animatorsAll)"
    }

    fun getParentGroupMedia(): Media {
        return view?.getParentGroupOrThis()?.media ?: this
    }


    open fun preprocessPresetMediaText() {

    }

    open fun forPremium(): Boolean {
        return forPremium
    }


    fun getDefaultOutAnimator(): InspAnimator {
        return InspAnimator(0, 9, null, FadeAnimApplier(1f, 0f))
    }

    fun selectTextView() = if (this is MediaText) this else if (this is MediaGroup)
        medias.find { it is MediaText } as? MediaText? else null

    // maybe add recursion
    fun childHasBgVector(): Boolean {
        if (this is MediaGroup) {

            for (m in medias) {
                when {
                    m is MediaGroup -> {
                        if (m.childHasBgVector())
                            return true
                    }
                    m is MediaVector && m.vectorAsTextBg -> {
                        return true
                    }
                    m is MediaImage && m.imageAsTextBg -> {
                        return true
                    }
                }
            }
        }
        return false
    }


    fun forAllMedias(media: Media = this, action: (Media) -> Unit) {

        action(media)

        if (media is MediaGroup) {
            media.medias.forEach {
                forAllMedias(it, action)
            }
        }
    }

    fun hasChildText(): Boolean {

        var foundText = false

        forAllMedias {
            if (it is MediaText) {
                foundText = true
                return@forAllMedias
            }
        }

        return foundText
    }


    companion object {

        const val MIN_DURATION_AS_TEMPLATE = -1 * 1000000
        const val DEFAULT_TEXT_DELAY_BEFORE_END = 60
    }
}

fun Boolean?.nullOrTrue() = this == null || this
fun Boolean?.nullOrFalse() = this == null || !this
