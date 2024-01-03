package app.inspiry.views.group

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.widget.LinearLayout
import app.inspiry.core.media.*
import app.inspiry.views.androidhelper.CanvasUtils
import app.inspiry.views.androidhelper.InspLayoutParams
import app.inspiry.views.androidhelper.InspLinearLayoutParams
import app.inspiry.views.template.InspTemplateView

@SuppressLint("ViewConstructor")
class InnerGroupLinearView(
    context: Context,
    val media: MediaGroup,
    val templateView: InspTemplateView,
    val unitsConverter: BaseUnitsConverter
) : LinearLayout(context), InnerGroupViewAndroid {

    override lateinit var mDrawAnimations: (Canvas) -> Unit
    override lateinit var mDrawOnGlCanvas: (Boolean) -> Boolean
    override var cornerRadius: Float = 0f

    private val canvasUtils: CanvasUtils by lazy { CanvasUtils() }

    init {
        orientation = if (media.orientation == GroupOrientation.H) HORIZONTAL else VERTICAL
    }

    override fun onDrawForeground(canvas: Canvas) {
        super.onDrawForeground(canvas)
        if (media.borderWidth != null) {
                canvasUtils.drawBorder(
                    canvas = canvas,
                    borderWidthString = media.borderWidth!!,
                    borderType = media.borderType ?: BorderStyle.outside,
                    borderColor = media.borderColor
                        ?: templateView.template.palette.mainColor?.getFirstColor() ?: Color.WHITE,
                    width = width,
                    height = height,
                    layoutParams = layoutParams as InspLayoutParams,
                    cornerRadiusPosition = media.cornerRadiusPosition,
                    cornerRadius = cornerRadius,
                    paddingStart = paddingStart,
                    paddingEnd = paddingEnd,
                    paddingBottom = paddingBottom,
                    paddingTop = paddingTop
                )
        }
    }

    override fun dispatchDraw(canvas: Canvas) {
        //Don't move it to draw method!
        mDrawAnimations(canvas)
        super.dispatchDraw(canvas)
    }

    override fun draw(canvas: Canvas) {
        if (!mDrawOnGlCanvas(true))
            super.draw(canvas)
    }

    override fun originalDraw(canvas: Canvas) {
        super.draw(canvas)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {

        resolveChildrenLayoutParams(widthMeasureSpec, heightMeasureSpec)
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    private fun resolveChildrenLayoutParams(widthMeasureSpec: Int, heightMeasureSpec: Int) {

        val parentWidth = MeasureSpec.getSize(widthMeasureSpec)
        val parentHeight = MeasureSpec.getSize(heightMeasureSpec)

        for (i in 0 until childCount) {
            val child = getChildAt(i)
            val lp = child.layoutParams as InspLinearLayoutParams
            val (childOffsetX, childOffsetY) = getOffsetsForChild(
                lp,
                templateView,
                parentWidth,
                parentHeight,
                unitsConverter
            )

            if (childOffsetX > 0) {
                lp.rightMargin = childOffsetX
                lp.leftMargin = 0
            } else {
                lp.leftMargin = -childOffsetX
                lp.rightMargin = 0
            }

            if (childOffsetY > 0) {
                lp.bottomMargin = childOffsetX
                lp.topMargin = 0
            } else {
                lp.topMargin = -childOffsetX
                lp.bottomMargin = 0
            }
        }

        resolveLayoutParams(parentWidth, parentHeight, templateView, unitsConverter)
    }
}