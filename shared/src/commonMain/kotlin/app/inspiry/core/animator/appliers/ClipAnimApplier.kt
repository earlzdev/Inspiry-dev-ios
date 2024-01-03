package app.inspiry.core.animator.appliers

import app.inspiry.core.animator.clipmask.logic.ClipMaskType
import app.inspiry.core.animator.text.DrawBackgroundAnimParam
import app.inspiry.core.data.Rect
import app.inspiry.core.util.InspMathUtil.SIZE_MAX_VALUE
import app.inspiry.core.util.InspMathUtil.SIZE_MIN_VALUE
import app.inspiry.views.InspView
import app.inspiry.views.text.InnerGenericText
import kotlinx.serialization.Required
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.math.max


/**
 * One view can have 2 clipAnimApplier at the same time: Vertical and Horizontal, but only if both have clipDefault = false
 *
 * @param clipDefault - if true then we clip view to its bounds.
 * @param fadePadding - if we need to apply LinearGradient Shader. Percent of parent width
 */
@Serializable
@SerialName("clip")
class ClipAnimApplier(
    @Required
    var direction: Direction,
    val fadePadding: Float? = null,
    override var from: Float = 0f,
    override var to: Float = 1f,
    var clipDefault: Boolean = false,
    var stickCorners: Boolean = false
) : AnimApplier(), FloatValuesAnimApplier {

    private fun Rect.setIfCan(start: Int, end: Int, horizontal: Boolean, view: InspView<*>) {
        if (horizontal) {
            left = start
            right = end
            if (clipDefault) {
                top = view.paddingTop
                bottom = view.viewHeight - view.paddingBottom
            }
        } else {
            top = start
            bottom = end
            if (clipDefault) {
                left = view.paddingLeft
                right = view.viewWidth - view.paddingRight
            }
        }
    }

    private fun initClipBounds() =
        direction != Direction.circular && direction != Direction.circular_inverse

    override fun onPreDraw(view: InspView<*>, value: Float) {

        var clipBounds = view.mClipBounds
        if (clipBounds == null) {
            if (initClipBounds()) {
                clipBounds = Rect(SIZE_MIN_VALUE, SIZE_MIN_VALUE, SIZE_MAX_VALUE, SIZE_MAX_VALUE)
                view.mClipBounds = clipBounds
            }
        }

        val newVal = ((to - from) * value) + from

        when (direction) {
            Direction.circular -> {
                view.animationHelper?.circularOutlineClipRadiusDegree = newVal
            }
            Direction.circular_inverse -> {
                val x = (view.viewWidth - view.paddingLeft - view.paddingRight) / 2f
                val y = (view.viewHeight - view.paddingBottom - view.paddingTop) / 2f
                val radius = max(x, y) * newVal
                view.animationHelper?.setClipMask(
                    maskType = ClipMaskType.CIRCULAR,
                    x = x,
                    y = y,
                    radius = radius,
                    inverse = true
                )
            }

            Direction.top_to_bottom -> {
                clipBounds!!.setIfCan(
                    view.paddingTop,
                    (view.paddingTop + ((view.viewHeight - view.paddingTop - view.paddingBottom) * newVal)).toInt(),
                    false, view)
            }
            Direction.left_to_right -> {
                clipBounds!!.setIfCan(
                    view.paddingLeft,
                    view.paddingLeft + ((view.viewWidth - view.paddingLeft - view.paddingRight) * newVal)
                        .toInt(), true, view)
            }
            Direction.bottom_to_top -> {

                val paddingTop = view.paddingTop
                val heightMinusPadding = (view.viewHeight - paddingTop - view.paddingBottom).toDouble()
                clipBounds!!.setIfCan(paddingTop + ((1.0 - newVal) * heightMinusPadding).toInt(),
                    view.viewHeight - view.paddingBottom, false, view)
            }
            Direction.center_to_top_and_bottom -> {

                val paddingTop = view.paddingTop
                val heightMinusPadding = (view.viewHeight - paddingTop - view.paddingBottom).toDouble()

                clipBounds!!.setIfCan(paddingTop + ((0.5 - newVal / 2) * heightMinusPadding).toInt(),
                    view.paddingTop + ((view.viewHeight - paddingTop - view.paddingBottom) * (0.5f + newVal / 2)).toInt(),
                    false, view)
            }
            Direction.center_to_left_and_right -> {

                val paddingLeft = view.paddingLeft
                val widthMinusPadding =
                    (view.viewWidth - view.paddingLeft - view.paddingRight).toDouble()
                clipBounds!!.setIfCan(
                    paddingLeft + ((0.5 - newVal / 2) * widthMinusPadding).toInt(),
                    paddingLeft + ((view.viewWidth - paddingLeft - view.paddingRight) * (0.5f + newVal / 2)).toInt(),
                    true, view)
            }
            Direction.right_to_left -> {
                val paddingLeft = view.paddingLeft
                val widthMinusPadding =
                    (view.viewWidth - view.paddingLeft - view.paddingRight).toDouble()

                clipBounds!!.setIfCan(
                    paddingLeft + ((1.0 - newVal) * widthMinusPadding).toInt(),
                    view.viewWidth - view.paddingRight, true, view)
            }
            Direction.none -> {
                clipBounds!!.set(
                    view.paddingLeft,
                    view.paddingTop,
                    view.viewWidth - view.paddingRight,
                    view.viewHeight - view.paddingBottom
                )
            }
        }

        view.animationHelper?.clipStickingCorners = stickCorners
    }

    override fun transformText(
        param: DrawBackgroundAnimParam,
        value: Float,
        view: InnerGenericText<*>
    ) {

        val newVal = ((to - from) * value) + from

        if (fadePadding == null || fadePadding == 0f) {
            when (direction) {
                ClipAnimApplier.Direction.top_to_bottom -> {
                    param.setClipRect(0, 0, param.width, (newVal * param.height).toInt())
                }

                ClipAnimApplier.Direction.left_to_right -> {
                    param.setClipRect(0, 0, (param.width * newVal).toInt(), param.height)
                }

                ClipAnimApplier.Direction.bottom_to_top -> {
                    param.setClipRect(
                        0,
                        ((1 - newVal) * param.height).toInt(),
                        param.width,
                        param.height
                    )
                }

                ClipAnimApplier.Direction.center_to_top_and_bottom -> {

                    param.setClipRect(
                        0,
                        ((0.5 - newVal / 2) * param.height).toInt(),
                        param.width,
                        (param.height * (0.5f + newVal / 2)).toInt()
                    )

                }
                ClipAnimApplier.Direction.center_to_left_and_right -> {
                    param.setClipRect(
                        ((0.5 - newVal / 2) * param.width).toInt(),
                        0,
                        (param.width * (0.5f + newVal / 2)).toInt(),
                        param.height
                    )
                }
                ClipAnimApplier.Direction.right_to_left -> {
                    param.setClipRect(
                        (param.width * (1 - newVal)).toInt(),
                        0,
                        param.width,
                        param.height
                    )

                }
                ClipAnimApplier.Direction.none -> {
                    param.setClipRect(0, 0, param.width, param.height)
                }
                else -> throw IllegalArgumentException("unknown clip anim direction: $direction")
            }
        } else {
            throw IllegalStateException("unsupported param fadepadding")
//        val startColor = ArgbColorManager.colorWithoutAlpha(param.color)
//
//        val shaderWidth = fadePadding!! * view.templateWidth
//
//
//        when (direction) {
//            ClipAnimApplier.Direction.left_to_right -> {
//                val x0 = (param.width + shaderWidth) * newVal + param.left
//
//                param.shader = LinearGradient(
//                    x0 - shaderWidth,
//                    0f,
//                    x0,
//                    0f,
//                    startColor,
//                    PredefinedColors.TRANSPARENT,
//                    Shader.TileMode.CLAMP
//                )
//            }
//            ClipAnimApplier.Direction.right_to_left -> {
//
//                val x0 = (param.width + shaderWidth) * (1f - newVal) + param.left
//
//                param.shader = LinearGradient(
//                    x0,
//                    0f,
//                    x0 - shaderWidth,
//                    0f,
//                    startColor,
//                    PredefinedColors.TRANSPARENT,
//                    Shader.TileMode.CLAMP
//                )
//            }
//
//            ClipAnimApplier.Direction.top_to_bottom -> {
//
//                //Why it doesn't work without offset ???
//                val offset = -param.height * 1.1f
//
//                val y0 = (param.height + shaderWidth) * newVal + param.top + offset
//
//                param.shader = LinearGradient(
//                    0f,
//                    y0,
//                    0f,
//                    y0 - shaderWidth,
//                    PredefinedColors.TRANSPARENT,
//                    startColor,
//                    Shader.TileMode.CLAMP
//                )
//            }
//        }
        }
    }

    enum class Direction {
        top_to_bottom, left_to_right, bottom_to_top, center_to_top_and_bottom,
        center_to_left_and_right, right_to_left, circular, circular_inverse,
        //none means view bounds. Clip what is outside
        none

    }
}
