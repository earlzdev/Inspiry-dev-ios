package app.inspiry.core.animator.helper

import app.inspiry.core.animator.appliers.ClipAnimApplier
import app.inspiry.core.animator.clipmask.MaskProvider
import app.inspiry.core.animator.clipmask.logic.ClipMaskSettings
import app.inspiry.core.animator.clipmask.logic.ClipMaskType
import app.inspiry.core.animator.clipmask.shape.ShapeTransform
import app.inspiry.core.data.Rect
import kotlinx.coroutines.flow.MutableStateFlow

interface AbsAnimationHelper<CANVAS> {
    var animationTranslationX: Float
    var animationTranslationY: Float
    var animationRotation: Float

    var circularOutlineClipRadiusDegree: Float?
    var clipStickingCorners: Boolean

    var maskProvider: MaskProvider?
    val clipMaskSettings: ClipMaskSettings
    fun resetLastTimeTriggeredEnd()
    fun notifyMediaRotationChanged()
    fun notifyViewCornerRadiusChanged()
    fun notifyViewElevationChanged()
    fun notifyAnimationParameterChanged()

    fun prepareAnimation(frame: Int)

    fun drawAnimations(canvas: CANVAS, currentFrame: Int)
    fun preDrawAnimations(currentFrame: Int)

    fun hasAnimatedClipMask(): Boolean
    fun mayInitMaskProvider()

    fun hasClipPath(): Boolean
    fun onSizeChanged()

    fun setClipMask(
        maskType: ClipMaskType = ClipMaskType.NONE,
        x: Float = 0f,
        y: Float = 0f,
        radius: Float = 0f,
        viewWidth: Float = 0f,
        viewHeight: Float = 0f,
        inverse: Boolean = false,
        progress: Float = 0f,
        count: Int = 0,
        direction: ClipAnimApplier.Direction = ClipAnimApplier.Direction.left_to_right,
        reflection: Boolean = false
    )

    fun shapeTransform(
        xOffset: Float? = null,
        yOffset: Float? = null,
        scaleWidth: Float? = null,
        scaleHeight: Float? = null,
        rotation: Float? = null
    )
}

fun getEmptyAnimationHelper(): AbsAnimationHelper<Any> {
    return object : AbsAnimationHelper<Any> {
        override var animationTranslationX: Float
            get() = 0f
            set(value) {}
        override var animationTranslationY: Float
            get() = 0f
            set(value) {}
        override var circularOutlineClipRadiusDegree: Float?
            get() = 0f
            set(value) {}
        override var clipStickingCorners: Boolean
            get() = false
            set(value) {}
        override var animationRotation: Float
            get() = 0f
            set(value) {}
        override var clipMaskSettings: ClipMaskSettings = ClipMaskSettings()

        override fun resetLastTimeTriggeredEnd() {

        }

        override fun notifyMediaRotationChanged() {

        }

        override fun notifyViewCornerRadiusChanged() {

        }

        override fun notifyViewElevationChanged() {

        }

        override fun prepareAnimation(frame: Int) {

        }

        override fun drawAnimations(canvas: Any, currentFrame: Int) {

        }

        override fun preDrawAnimations(currentFrame: Int) {

        }

        override fun hasClipPath(): Boolean {
            return false
        }

        override fun notifyAnimationParameterChanged() {

        }

        override fun setClipMask(
            maskType: ClipMaskType,
            x: Float,
            y: Float,
            radius: Float,
            viewWidth: Float,
            viewHeight: Float,
            inverse: Boolean,
            progress: Float,
            count: Int,
            direction: ClipAnimApplier.Direction,
            reflection: Boolean
        ) {

        }


        override fun mayInitMaskProvider() {}

        override fun onSizeChanged() {

        }

        override var maskProvider: MaskProvider? = null

        override fun shapeTransform(
            xOffset: Float?,
            yOffset: Float?,
            scaleWidth: Float?,
            scaleHeight: Float?,
            rotation: Float?
        ) {

        }

        override fun hasAnimatedClipMask(): Boolean {
            return false
        }

    }
}