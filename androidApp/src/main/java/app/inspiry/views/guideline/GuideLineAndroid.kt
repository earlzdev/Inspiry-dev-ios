package app.inspiry.views.guideline

import android.graphics.Canvas
import android.graphics.Paint
import app.inspiry.core.media.Alignment
import app.inspiry.views.InspView
import app.inspiry.views.guideline.GuidelineManager.Companion.GUIDELINE_COLOR
import app.inspiry.views.template.InspTemplateView
import app.inspiry.views.template.InspTemplateViewAndroid
import app.inspiry.views.viewplatform.getAndroidView
import kotlin.math.roundToInt

class GuideLineAndroid(
    root: InspTemplateView,
    targetEdge: Alignment,
    align: Alignment,
    orientation: Orientation,
    offset: Int = 0
) : GuideLine(root, targetEdge, align, orientation, offset) {

    private val relativePosition = intArrayOf(0, 0)
    private val positionPreview = intArrayOf(0, 0)
    private val positionFrame = intArrayOf(0, 0)

    private val paint: Paint = Paint().also {
        it.color = GUIDELINE_COLOR.argb.toInt()
        it.flags = Paint.ANTI_ALIAS_FLAG
    }

    fun onDraw(canvas: Canvas, guidelineManager: GuidelineManager) {
        if (isDisplayed) {

            val coord = getGuidelineCoord()

            when (orientation) {
                Orientation.HORIZONTAL -> {
                    canvas.drawLine(0f, coord - guidelineManager.guidelineThickness / 2f,
                        root.viewWidth.toFloat(), coord + guidelineManager.guidelineThickness / 2f, paint)
                }
                Orientation.VERTICAL -> {
                    canvas.drawLine(coord - guidelineManager.guidelineThickness / 2f, 0f,
                        coord + guidelineManager.guidelineThickness / 2f, root.viewHeight.toFloat(), paint)
                }
            }
        }
    }

    override fun getPositionInParent(parent: InspTemplateView, child: InspView<*>): IntArray {

        val view = child.getAndroidView()
        val parentAndroid = (parent as InspTemplateViewAndroid).innerView
        val scale = parent.templateTransform.value.scale
        relativePosition[0] = view.left
        relativePosition[1] = view.top
        parentAndroid.getLocationInWindow(positionPreview)
        view.getLocationInWindow(positionFrame)

        relativePosition[0] = ((positionFrame[0] - positionPreview[0]) / scale).roundToInt()
        relativePosition[1] = ((positionFrame[1] - positionPreview[1]) / scale).roundToInt()
        return relativePosition
    }
}