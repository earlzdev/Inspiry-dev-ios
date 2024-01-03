package app.inspiry.views.guideline

import app.inspiry.core.media.Alignment
import app.inspiry.views.InspView
import app.inspiry.views.template.InspTemplateView
import app.inspiry.views.template.TemplateMode
import dev.icerock.moko.graphics.Color

abstract class GuidelineManager {

    abstract val guidelineThreshold: Float
    abstract val guidelineThickness: Float

    abstract fun initGuideline(root: InspTemplateView,
                               targetEdge: Alignment,
                               align: Alignment,
                               orientation: GuideLine.Orientation,
                               offset: Int): GuideLine

    fun initGuidelines(root: InspTemplateView): MutableList<GuideLine> {
        val guidelines = mutableListOf<GuideLine>()
        guidelines.add(initGuideline(root, Alignment.center, Alignment.center, GuideLine.Orientation.VERTICAL, 0))
        guidelines.add(initGuideline(root, Alignment.center, Alignment.center, GuideLine.Orientation.HORIZONTAL, 0))

        /*val guidelineSideOffset = 20.dpToPxInt()
        guidelines.add(GuideLine(templateView, Gravity.LEFT, Gravity.LEFT, ORIENTATION_VERTICAL, guidelineSideOffset))
        guidelines.add(GuideLine(templateView, Gravity.RIGHT, Gravity.RIGHT, ORIENTATION_VERTICAL, guidelineSideOffset))
        guidelines.add(GuideLine(templateView, Gravity.TOP, Gravity.TOP, ORIENTATION_HORIZONTAL, guidelineSideOffset))
        guidelines.add(GuideLine(templateView, Gravity.BOTTOM, Gravity.BOTTOM, ORIENTATION_HORIZONTAL, guidelineSideOffset))*/

        return guidelines
    }

    fun hideOnTouchCancel(root: InspTemplateView, inspView: InspView<*>): Boolean {
        if (root.templateMode != TemplateMode.EDIT) return false

        var changed = false

        root.guidelines.forEach {
            if (it.isDisplayed) changed = true
            it.isDisplayed = false
        }

        if (changed) {
            inspView.movableTouchHelper?.guidelineMoveResistanceX = guidelineThreshold
            inspView.movableTouchHelper?.guidelineMoveResistanceY = guidelineThreshold
            root.invalidateGuidelines()
        }

        return changed
    }

    //return true if changed
    fun onViewMoved(root: InspTemplateView, inspView: InspView<*>): Boolean {
        if (root.templateMode != TemplateMode.EDIT) return false

        var changed = false
        var enabledNewOne = false
        for (it in root.guidelines) {

            if (!inspView.media.canMoveX() && it.orientation == GuideLine.Orientation.VERTICAL)
                continue
            if (!inspView.media.canMoveY() && it.orientation == GuideLine.Orientation.HORIZONTAL)
                continue

            val old = it.isDisplayed
            val coordsDiff = it.coordsDiff(inspView)
            it.isDisplayed = kotlin.math.abs(coordsDiff) < guidelineThreshold

            if (old != it.isDisplayed) {
                changed = true
                if (it.isDisplayed) {
                    enabledNewOne = true

                    when (it.orientation) {
                        GuideLine.Orientation.VERTICAL -> {
                            inspView.incrementTranslationX(coordsDiff.toFloat() / root.viewWidth)
                            inspView.movableTouchHelper?.guidelineMoveResistanceX = 0f
                        }
                        GuideLine.Orientation.HORIZONTAL -> {
                            inspView.incrementTranslationY(coordsDiff.toFloat() / root.viewHeight)
                            inspView.movableTouchHelper?.guidelineMoveResistanceY = 0f
                        }
                    }
                }
            }
        }

        if (enabledNewOne && VIBRATE_ON_ANCHOR) {
            inspView.view?.vibrateOnGuideline()
        }

        if (changed) {
            root.invalidateGuidelines()
        }

        return changed
    }

    companion object {
        val GUIDELINE_COLOR = Color(0x4c, 0xc0, 0xec, 0xff)
        const val VIBRATE_ON_ANCHOR = false

        val GUIDELINE_THRESHOLD = 7f
        val GUIDELINE_THICKNESS = 1f
    }
}