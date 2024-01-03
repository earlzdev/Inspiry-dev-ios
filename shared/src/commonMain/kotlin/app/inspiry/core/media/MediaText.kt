package app.inspiry.core.media

import app.inspiry.core.data.TouchAction
import app.inspiry.core.animator.InspAnimator
import app.inspiry.core.animator.TextAnimationParams
import app.inspiry.core.animator.appliers.ColorType
import app.inspiry.core.animator.clipmask.shape.ShapeType
import app.inspiry.font.model.FontData
import app.inspiry.font.model.FontDataSerializer
import app.inspiry.core.serialization.*
import app.inspiry.core.util.PredefinedColors
import app.inspiry.palette.model.MediaPalette
import app.inspiry.palette.model.PaletteLinearGradient
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
@SerialName("text")
class MediaText(
    @Serializable(with = LayoutPositionSerializer::class)
    override var layoutPosition: LayoutPosition,
    override var id: String? = null, override var translationX: Float = 0f,
    override var translationY: Float = 0f, override var rotation: Float = 0f,
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
    override var loopedAnimationInterval: Int? = null, override var canMoveY: Boolean? = null,
    override var canMoveX: Boolean? = null,
    override var cornerRadiusPosition: CornerRadiusPosition? = null,
    var text: String = "",
    var textSize: String = "0",
    var lineSpacing: Float = 1f,
    var letterSpacing: Float = 0f,

    @Serializable(with = FontDataSerializer::class)
    var font: FontData? = null,

    var innerGravity: TextAlign = TextAlign.left,

    @Serializable(with = ColorSerializer::class)
    var textColor: Int = PredefinedColors.BLACK_ARGB,

    @Serializable(with = TextAnimationParamsSerializer::class)
    var animationParamIn: TextAnimationParams? = null,

    @Serializable(with = TextAnimationParamsSerializer::class)
    var animationParamOut: TextAnimationParams? = null,

    var strokeWidth: Float? = null,
    var paintStyle: PaintStyle? = null,

    // this is for copy of text that is rendered behind the main text
    var shadowOffsetX: Float? = null,
    var shadowOffsetY: Float? = null,
    var shadowColors: MutableList<@Serializable(with = ColorSerializer::class) Int>? = null,

    override var dependsOnParent: Boolean = false,

    override var backgroundGradient: PaletteLinearGradient? = null,
    var textGradient: PaletteLinearGradient? = null,

    // this is for shadow like elevation effect
    var textShadowDx: Float? = null,
    var textShadowDy: Float? = null,
    @Serializable(with = ColorSerializer::class)
    var textShadowColor: Int? = null,
    var textShadowBlurRadius: Float? = null,
    @Serializable(with = ColorSerializer::class)
    var textStrokeColor: Int? = null,
    //those margins are calculated from x * fontHeight
    var backgroundMarginLeft: Float = 0f,
    var backgroundMarginTop: Float = 0f,
    var backgroundMarginRight: Float = 0f,
    var backgroundMarginBottom: Float = 0f,

    override var forPremium: Boolean = false,
    override var keepAspect: Boolean = false,
    override var touchActions: List<TouchAction>? = null,
    override var isTemporaryMedia: Boolean = false,
    var mediaPalette: MediaPalette? = null,

    override var colorChangeDisabled: Boolean = false,
    var maxLines: Int? = null,

    var backgroundStaticScale: Float = 0f,

    var defaults: MediaTextDefaults? = null,

    var radius: Float = 0f,

    var duplicate: String? = null,

    var blurRadius: Float = 0f, //0..1 value

    override var shape: ShapeType? = null,
    ) :
    Media() {


    fun lackBackgroundLineColor() = (animationParamIn?.backgroundAnimatorGroups?.all { it.animators.isEmpty() }
                .nullOrTrue() &&
                    animationParamOut?.backgroundAnimatorGroups?.all { it.animators.isEmpty() }
                        .nullOrTrue())


    override var isMovable: Boolean?
        get() = true
        set(value) {}


    fun hasBackground() = backgroundColor != 0 || backgroundGradient != null


    override fun preprocessPresetMediaText() {
        if (delayBeforeEnd == 0)
            delayBeforeEnd = DEFAULT_TEXT_DELAY_BEFORE_END

        if (animatorsOut.isEmpty() && animationParamOut == null) {
            animatorsOut = mutableListOf<InspAnimator>().also { it.add(getDefaultOutAnimator()) }
        }
    }

    fun multiplyTextSize(unitsConverter: BaseUnitsConverter, multiply: Float) {
        val oldSize = unitsConverter.convertUnitToPixelsF(textSize, 100, 100)
        val newSize = (oldSize * multiply)

        textSize = "$newSize/100w"
    }

    fun isCircularText(): Boolean = animationParamIn?.charsOnCircle == true

    override fun toString(): String {
        return "MediaText(text='$text', textSize='$textSize)"
    }

    fun hasShadow() = textShadowColor != null
    fun hasMulticolorShadow() = shadowColors != null
    fun hasStrokeColor() = textStrokeColor != null

    fun hasAnimatedColor(): Boolean {
        mediaPalette?.choices?.forEach {
            if (it.elements.contains(ColorType.ANIMATION_COLOR_1.name) || (it.elements.contains(ColorType.ANIMATION_COLOR_2.name))) {
                return true
            }
        }
        return false
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is MediaText) return false

        if (id != other.id) return false
        if (text != other.text) return false
        if (textSize != other.textSize) return false
        if (translationX != other.translationX) return false
        if (translationY != other.translationY) return false
        if (forPremium != other.forPremium) return false
        if (lineSpacing != other.lineSpacing) return false
        if (letterSpacing != other.letterSpacing) return false
        if (font != other.font) return false
        if (innerGravity != other.innerGravity) return false
        if (textColor != other.textColor) return false
        if (strokeWidth != other.strokeWidth) return false
        if (paintStyle != other.paintStyle) return false
        if (shadowOffsetX != other.shadowOffsetX) return false
        if (shadowOffsetY != other.shadowOffsetY) return false
        if (shadowColors != null) {
            if (other.shadowColors == null) return false
            if (shadowColors != other.shadowColors) return false
        } else if (other.shadowColors != null) return false
        if (dependsOnParent != other.dependsOnParent) return false
        if (backgroundGradient != other.backgroundGradient) return false
        if (textGradient != other.textGradient) return false
        if (textShadowDx != other.textShadowDx) return false
        if (textShadowDy != other.textShadowDy) return false
        if (textShadowColor != other.textShadowColor) return false
        if (textShadowBlurRadius != other.textShadowBlurRadius) return false
        if (backgroundMarginLeft != other.backgroundMarginLeft) return false
        if (backgroundMarginTop != other.backgroundMarginTop) return false
        if (backgroundMarginRight != other.backgroundMarginRight) return false
        if (backgroundMarginBottom != other.backgroundMarginBottom) return false

        return true
    }

    fun getColorForLayer(layer: Int): Int {
        mediaPalette?.choices?.let { return it[layer].color ?: 0 }

        if (layer == 0 && textGradient == null) return textColor
        if (layer == 1) {
            textShadowColor?.let { return it }
            shadowColors?.let { return it[layer -1] }
            textStrokeColor?.let { return it }
        }
        return 0
    }

    fun getGradientForLayer(layer: Int): PaletteLinearGradient? {
        if (layer == 0 && textGradient != null) return textGradient as PaletteLinearGradient
        return null
    }

    override fun hashCode(): Int {
        var result = text.hashCode()
        result = 31 * result + (id?.hashCode() ?: 0)
        result = 31 * result + translationX.hashCode()
        result = 31 * result + translationY.hashCode()
        result = 31 * result + textSize.hashCode()
        result = 31 * result + forPremium.hashCode()
        result = 31 * result + lineSpacing.hashCode()
        result = 31 * result + letterSpacing.hashCode()
        result = 31 * result + (font?.hashCode() ?: 0)
        result = 31 * result + innerGravity.hashCode()
        result = 31 * result + textColor
        result = 31 * result + (strokeWidth?.hashCode() ?: 0)
        result = 31 * result + (paintStyle?.hashCode() ?: 0)
        result = 31 * result + (shadowOffsetX?.hashCode() ?: 0)
        result = 31 * result + (shadowOffsetY?.hashCode() ?: 0)
        result = 31 * result + (shadowColors?.hashCode() ?: 0)
        result = 31 * result + dependsOnParent.hashCode()
        result = 31 * result + (backgroundGradient?.hashCode() ?: 0)
        result = 31 * result + (textGradient?.hashCode() ?: 0)
        result = 31 * result + (textShadowDx?.hashCode() ?: 0)
        result = 31 * result + (textShadowDy?.hashCode() ?: 0)
        result = 31 * result + (textShadowColor ?: 0)
        result = 31 * result + (textShadowBlurRadius?.hashCode() ?: 0)
        result = 31 * result + backgroundMarginLeft.hashCode()
        result = 31 * result + backgroundMarginTop.hashCode()
        result = 31 * result + backgroundMarginRight.hashCode()
        result = 31 * result + backgroundMarginBottom.hashCode()
        return result
    }


    companion object {
        const val DEFAULT_TEXT_SIZE_IN_TEMPLATE = "1/18m"
    }
}
