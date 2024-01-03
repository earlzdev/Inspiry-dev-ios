package app.inspiry.views.guideline

import app.inspiry.core.media.Alignment
import app.inspiry.utils.dpToPixels
import app.inspiry.views.template.InspTemplateView

class GuidelineManagerAndroid: GuidelineManager() {
    override val guidelineThreshold: Float
        get() = GUIDELINE_THRESHOLD.dpToPixels()
    override val guidelineThickness: Float
        get() = GUIDELINE_THICKNESS.dpToPixels()

    override fun initGuideline(
        root: InspTemplateView,
        targetEdge: Alignment,
        align: Alignment,
        orientation: GuideLine.Orientation,
        offset: Int
    ): GuideLine {
        return GuideLineAndroid(root, targetEdge, align, orientation, offset)
    }
}