package app.inspiry.views.group

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.util.AttributeSet
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.constraintlayout.widget.ConstraintLayout
import app.inspiry.core.log.GlobalLogger
import app.inspiry.core.media.BaseUnitsConverter
import app.inspiry.core.media.TemplateFormat
import app.inspiry.views.InspView
import app.inspiry.views.androidhelper.InspFrameLayoutParams
import app.inspiry.views.androidhelper.InspLayoutParams
import app.inspiry.views.androidhelper.InspLayoutParams.Companion.TAG_INSP_VIEW
import app.inspiry.views.template.InspTemplateView
import app.inspiry.views.template.InspTemplateViewAndroid
import app.inspiry.views.template.TemplateMode
import kotlin.math.max

open class BaseGroupZView(
    context: Context,
    //null if this is inside InspTemplateView.
    val templateView: InspTemplateView?,
    val unitsConverter: BaseUnitsConverter
) : FrameLayout(context) {

    private val mMatchParentChildren by lazy { mutableListOf<View>() }


    var onDrawForeground: ((Canvas) -> Unit)? = null

    init {
        measureAllChildren = true
    }

    override fun onDrawForeground(canvas: Canvas) {
        super.onDrawForeground(canvas)
        onDrawForeground?.invoke(canvas)
    }

    private fun resolveChildrenLayoutParams(widthMeasureSpec: Int, heightMeasureSpec: Int) {

        val parentWidth = MeasureSpec.getSize(widthMeasureSpec)
        val parentHeight = MeasureSpec.getSize(heightMeasureSpec)

        resolveLayoutParams(parentWidth, parentHeight, templateView, unitsConverter)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        resolveChildrenLayoutParams(widthMeasureSpec, heightMeasureSpec)
        onMeasureSuperTwiked(widthMeasureSpec, heightMeasureSpec)
    }

    /**
     * It is almost the same as FrameLayout.onMeasure, except one little detail: we remeasure children which have match_parent.
     * Originally frameLayout measures them again only if there's > 1 such children (stupid).
     */
    private fun onMeasureSuperTwiked(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var count = childCount

        val measureMatchParentChildren =
            MeasureSpec.getMode(widthMeasureSpec) != MeasureSpec.EXACTLY ||
                    MeasureSpec.getMode(heightMeasureSpec) != MeasureSpec.EXACTLY
        mMatchParentChildren.clear()

        var maxHeight = 0
        var maxWidth = 0
        var childState = 0

        for (i in 0 until count) {
            val child = getChildAt(i)
            if (measureAllChildren || child.visibility != View.GONE) {
                measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, 0)
                val lp =
                    child.layoutParams as LayoutParams
                maxWidth = max(
                    maxWidth,
                    child.measuredWidth + lp.leftMargin + lp.rightMargin
                )
                maxHeight = max(
                    maxHeight,
                    child.measuredHeight + lp.topMargin + lp.bottomMargin
                )
                childState =
                    View.combineMeasuredStates(childState, child.measuredState)
                if (measureMatchParentChildren) {
                    if (lp.width == LayoutParams.MATCH_PARENT ||
                        lp.height == LayoutParams.MATCH_PARENT
                    ) {
                        mMatchParentChildren.add(child)
                    }
                }
            }
        }

        // Account for padding too

        // Account for padding too
        maxWidth += paddingLeft + paddingRight
        maxHeight += paddingTop + paddingBottom

        // Check against our minimum height and width

        // Check against our minimum height and width
        maxHeight = max(maxHeight, suggestedMinimumHeight)
        maxWidth = max(maxWidth, suggestedMinimumWidth)

        // Check against our foreground's minimum height and width

        // Check against our foreground's minimum height and width
        val drawable = foreground
        if (drawable != null) {
            maxHeight = max(maxHeight, drawable.minimumHeight)
            maxWidth = max(maxWidth, drawable.minimumWidth)
        }

        setMeasuredDimension(
            View.resolveSizeAndState(maxWidth, widthMeasureSpec, childState),
            View.resolveSizeAndState(
                maxHeight, heightMeasureSpec,
                childState shl View.MEASURED_HEIGHT_STATE_SHIFT
            )
        )

        count = mMatchParentChildren.size
        if (count > 0) {
            for (i in 0 until count) {
                val child: View = mMatchParentChildren.get(i)
                val lp = child.layoutParams as MarginLayoutParams
                val childWidthMeasureSpec: Int = if (lp.width == LayoutParams.MATCH_PARENT) {
                    val width = max(
                        0, measuredWidth
                                - paddingLeft - paddingRight
                                - lp.leftMargin - lp.rightMargin
                    )
                    MeasureSpec.makeMeasureSpec(
                        width, MeasureSpec.EXACTLY
                    )
                } else {
                    ViewGroup.getChildMeasureSpec(
                        widthMeasureSpec,
                        paddingLeft + paddingRight +
                                lp.leftMargin + lp.rightMargin,
                        lp.width
                    )
                }
                val childHeightMeasureSpec: Int = if (lp.height == LayoutParams.MATCH_PARENT) {
                    val height = max(
                        0, measuredHeight
                                - paddingTop - paddingBottom
                                - lp.topMargin - lp.bottomMargin
                    )
                    MeasureSpec.makeMeasureSpec(
                        height, MeasureSpec.EXACTLY
                    )
                } else {
                    ViewGroup.getChildMeasureSpec(
                        heightMeasureSpec,
                        paddingTop + paddingBottom +
                                lp.topMargin + lp.bottomMargin,
                        lp.height
                    )
                }
                child.measure(childWidthMeasureSpec, childHeightMeasureSpec)
            }
        }
    }
    fun applyFormat(templateFormat: TemplateFormat) {
        (layoutParams as? ConstraintLayout.LayoutParams)?.let {
            val ratio = when (templateFormat) {
                TemplateFormat.post -> "H, 4:5"
                TemplateFormat.square -> "H, 1:1"
                TemplateFormat.horizontal -> "H, 16:9"
                TemplateFormat.story -> "H, 9:16"
            }
            it.dimensionRatio = ratio
            requestLayout()
        }
    }

    @SuppressLint("RtlHardcoded")
    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        val count = childCount
        val parentLeft: Int = paddingLeft
        val parentRight: Int = right - left - paddingRight
        val parentTop: Int = paddingTop
        val parentBottom: Int = bottom - top - paddingBottom

        val parentWidth = parentRight - parentLeft
        val parentHeight = parentBottom - parentTop


        //Log.d(TAG_TEMPLATE, "onLayout, parentWidth = $parentWidth, parentHeight = $parentHeight")

        for (i in 0 until count) {
            val child = getChildAt(i)
            if (child != null && child.visibility != View.GONE) {
                val lp = child.layoutParams as InspFrameLayoutParams

                val childWidth = child.measuredWidth
                val childHeight = child.measuredHeight
                var childLeft: Int
                var childTop: Int
                var gravity = lp.gravity
                if (gravity == -1) {
                    gravity = Gravity.TOP or Gravity.START
                }
                val layoutDirection = layoutDirection
                val absoluteGravity = Gravity.getAbsoluteGravity(gravity, layoutDirection)
                val verticalGravity = gravity and Gravity.VERTICAL_GRAVITY_MASK

                val (childOffsetX, childOffsetY) = getOffsetsForChild(
                    lp, templateView, parentWidth,
                    parentHeight, unitsConverter
                )

                childLeft = when (absoluteGravity and Gravity.HORIZONTAL_GRAVITY_MASK) {
                    Gravity.CENTER_HORIZONTAL -> parentLeft + (parentRight - parentLeft - childWidth) / 2 + lp.leftMargin - lp.rightMargin + childOffsetX
                    Gravity.RIGHT -> {
                        parentRight - childWidth - lp.rightMargin - childOffsetX
                    }
                    Gravity.LEFT -> parentLeft + lp.leftMargin + childOffsetX
                    else -> parentLeft + lp.leftMargin + childOffsetX
                }

                childTop = when (verticalGravity) {
                    Gravity.TOP -> parentTop + lp.topMargin + childOffsetY
                    Gravity.CENTER_VERTICAL -> parentTop + (parentBottom - parentTop - childHeight) / 2 + lp.topMargin - lp.bottomMargin + childOffsetY
                    Gravity.BOTTOM -> parentBottom - childHeight - lp.bottomMargin - childOffsetY
                    else -> parentTop + lp.topMargin + childOffsetY
                }
                child.layout(childLeft, childTop, childLeft + childWidth, childTop + childHeight)
            }
        }
    }
}

internal fun getOffsetsForChild(
    lp: InspLayoutParams, templateView: InspTemplateView?,
    parentWidth: Int, parentHeight: Int, unitsConverter: BaseUnitsConverter
): Pair<Int, Int> {

    val parentWidthForCalc =
        if (lp.layoutPosition.relativeToParent || templateView == null) parentWidth else templateView.viewWidth
    val parentHeightForCalc =
        if (lp.layoutPosition.relativeToParent || templateView == null) parentHeight else templateView.viewHeight

    return unitsConverter.convertUnitToPixels(
        lp.layoutPosition.x, parentWidthForCalc,
        parentHeightForCalc, forHorizontal = true
    ) to unitsConverter.convertUnitToPixels(
        lp.layoutPosition.y,
        parentWidthForCalc, parentHeightForCalc, forHorizontal = false
    )
}

internal fun ViewGroup.resolveLayoutParams(
    parentWidth: Int, parentHeight: Int,
    templateView: InspTemplateView?, unitsConverter: BaseUnitsConverter
) {

    for (i in 0 until childCount) {
        val child = getChildAt(i)
        val lp = child.layoutParams as? InspLayoutParams? ?: continue

        val viewLp = child.layoutParams as ViewGroup.LayoutParams

        val parentWidthForCalc =
            if (lp.layoutPosition.relativeToParent || templateView == null) parentWidth else templateView.viewWidth
        val parentHeightForCalc =
            if (lp.layoutPosition.relativeToParent || templateView == null) parentHeight else templateView.viewHeight

        viewLp.width = unitsConverter.convertUnitToPixels(
            lp.layoutPosition.width,
            parentWidthForCalc,
            parentHeightForCalc,
            true
        )
        viewLp.height = unitsConverter.convertUnitToPixels(
            lp.layoutPosition.height,
            parentWidthForCalc,
            parentHeightForCalc,
            false
        )

        if (viewLp.height != ViewGroup.LayoutParams.WRAP_CONTENT &&
            viewLp.height != ViewGroup.LayoutParams.MATCH_PARENT
        ) {
            viewLp.height = (lp.heightFactor * viewLp.height).toInt()
        }

        if (viewLp.width != ViewGroup.LayoutParams.WRAP_CONTENT &&
            viewLp.width != ViewGroup.LayoutParams.MATCH_PARENT
        ) {
            viewLp.width = (lp.widthFactor * viewLp.width).toInt()
        }

        val inspView = child.getTag(TAG_INSP_VIEW) as? InspView<*>?

        if (inspView != null) {
            inspView.setPadding(
                lp.layoutPosition,
                parentWidthForCalc,
                parentHeightForCalc,
                unitsConverter
            )
            inspView.setMargin(
                lp.layoutPosition,
                parentWidthForCalc,
                parentHeightForCalc,
                unitsConverter
            )
        }

        else {
            child.setPaddingRelative(

                unitsConverter.convertUnitToPixels(
                    lp.layoutPosition.paddingStart, parentWidthForCalc,
                    parentHeightForCalc, true
                ),
                unitsConverter.convertUnitToPixels(
                    lp.layoutPosition.paddingTop,
                    parentWidthForCalc,
                    parentHeightForCalc,
                    false
                ),
                unitsConverter.convertUnitToPixels(
                    lp.layoutPosition.paddingEnd,
                    parentWidthForCalc,
                    parentHeightForCalc,
                    true
                ),
                unitsConverter.convertUnitToPixels(
                    lp.layoutPosition.paddingBottom, parentWidthForCalc,
                    parentHeightForCalc, false
                )
            )
        }
    }
}