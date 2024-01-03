package app.inspiry.animator.helper

import android.graphics.*
import android.view.View
import android.view.ViewOutlineProvider
import app.inspiry.core.animator.InspAnimator
import app.inspiry.core.animator.appliers.ClipAnimApplier
import app.inspiry.core.animator.clipmask.logic.ClipMaskType.*
import app.inspiry.core.animator.clipmask.MaskProvider
import app.inspiry.core.animator.clipmask.logic.ClipMaskType
import app.inspiry.core.animator.clipmask.shape.ShapeTransform
import app.inspiry.core.animator.helper.CommonAnimationHelper
import app.inspiry.core.media.CornerRadiusPosition
import app.inspiry.core.media.Media
import app.inspiry.core.util.InspMathUtil
import app.inspiry.core.util.InspMathUtil.SIZE_MAX_VALUE
import app.inspiry.core.util.InspMathUtil.SIZE_MIN_VALUE
import app.inspiry.views.InspView
import app.inspiry.views.androidhelper.InspLayoutParams
import app.inspiry.views.path.AndroidPath
import kotlin.math.max


class AnimationHelperAndroid(media: Media, val view: View) : CommonAnimationHelper<Canvas>(media) {

    override var inspView: InspView<*>? = null

    private val maskPath by lazy { AndroidPath() }

    init {
        if (media.shape != null) {
            clipMaskSettings.shape = media.shape
            mayInitMaskProvider()
        }
    }

    //for sticking corners in clip
    override var clipStickingCorners = false
        set(value) {
            field = value
            if (value) mayInitOutline()
        }

    override var circularOutlineClipRadiusDegree: Float? = null
        set(value) {
            field = value
            if (value != null)
                mayInitOutline()
        }

    private val edges = Rect()

    private fun mayInitOutline() {
        if (view.outlineProvider == null) {
            view.outlineProvider = object : ViewOutlineProvider() {
                override fun getOutline(view: View, outline: Outline) {

                    if (circularOutlineClipRadiusDegree != null) {

                        val width = view.width - view.paddingLeft - view.paddingRight
                        val height = view.height - view.paddingTop - view.paddingBottom

                        val radius =
                            (circularOutlineClipRadiusDegree!! * max(width, height) / 2f).toInt()

                        outline.setOval(
                            width / 2 - radius, height / 2 - radius,
                            width / 2 + radius, height / 2 + radius
                        )

                    } else {
                        val cornerRadius = inspView?.getCornerRadiusAbsolute() ?: 0f
                        if (cornerRadius != 0f) {
                            edges.set(
                                view.paddingLeft,
                                view.paddingTop,
                                view.width - view.paddingRight,
                                view.height - view.paddingBottom
                            )

                            if (clipStickingCorners) {
                                inspView?.mClipBounds?.let {
                                    if (it.bottom < SIZE_MAX_VALUE) edges.bottom = it.bottom
                                    if (it.top > SIZE_MIN_VALUE) edges.top = it.top
                                    if (it.left > SIZE_MIN_VALUE) edges.left = it.left
                                    if (it.right < SIZE_MAX_VALUE) edges.right = it.right
                                }
                            }

                            if (media.layoutPosition.isInWrapContentMode()) {
                                //setting the correct size for the size animator in wrap content mode
                                InspMathUtil.scaleAndCentering(
                                    edges,
                                    (view.layoutParams as InspLayoutParams).widthFactor,
                                    (view.layoutParams as InspLayoutParams).heightFactor
                                )
                            }

                            when (media.cornerRadiusPosition) {
                                CornerRadiusPosition.only_top -> edges.bottom += cornerRadius.toInt()
                                CornerRadiusPosition.only_bottom -> edges.top -= cornerRadius.toInt()
                                CornerRadiusPosition.only_left -> edges.right += cornerRadius.toInt()
                                CornerRadiusPosition.only_right -> edges.left -= cornerRadius.toInt()
                                null -> {}
                            }
                            outline.setRoundRect(
                                edges,
                                cornerRadius
                            )

                        } else {
                            outline.setRect(
                                view.paddingLeft,
                                view.top,
                                view.width - view.paddingRight,
                                view.bottom
                            )
                        }
                    }
                }
            }
        }
    }


    override fun drawAnimations(canvas: Canvas, currentFrame: Int) {

        if (inspView?.templateParent?.isInitialized?.value == true) {
            view.invalidateOutline()
            maskProvider?.let {
                canvas.clipPath(maskPath.path)
            }
            inspView?.mClipBounds?.let { canvas.clipRect(it) }
        }
    }

    override fun mayInitMaskProvider() {
        if (maskProvider == null) maskProvider = MaskProvider(maskPath, view.width, view.height)
        if (maskProvider?.height != view.height || maskProvider?.width != view.width)
            maskProvider?.updateSize(view.width, view.height)
    }


    override fun notifyMediaRotationChanged() {
        mayInitOutline()
    }

    override fun notifyViewCornerRadiusChanged() {
        mayInitOutline()
    }

    override fun notifyViewElevationChanged() {
        mayInitOutline()
    }



}


