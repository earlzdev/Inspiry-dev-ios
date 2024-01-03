package app.inspiry.views.group

import android.content.Context
import android.graphics.Canvas
import app.inspiry.core.media.BaseUnitsConverter
import app.inspiry.views.template.InspTemplateView

class InnerGroupZView(
    context: Context,
    templateView: InspTemplateView,
    unitsConverter: BaseUnitsConverter,
) : BaseGroupZView(context, templateView, unitsConverter), InnerGroupViewAndroid {

    override lateinit var mDrawAnimations: (Canvas) -> Unit
    override lateinit var mDrawOnGlCanvas: (Boolean) -> Boolean
    override var cornerRadius: Float = 0f

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

}