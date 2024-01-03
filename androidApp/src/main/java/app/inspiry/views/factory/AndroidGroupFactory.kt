package app.inspiry.views.factory

import android.annotation.SuppressLint
import android.content.Context
import android.view.ViewGroup
import app.inspiry.animator.helper.AnimationHelperAndroid
import app.inspiry.font.provider.FontsManager
import app.inspiry.core.media.BaseUnitsConverter
import app.inspiry.core.media.GroupOrientation
import app.inspiry.core.media.MediaGroup
import app.inspiry.core.log.LoggerGetter
import app.inspiry.views.InspParent
import app.inspiry.views.group.*
import app.inspiry.views.template.InspTemplateView
import app.inspiry.views.template.TemplateMode
import app.inspiry.views.touch.MovableTouchHelperAndroid
import app.inspiry.views.touch.MovableTouchHelperFactory
import app.inspiry.views.viewplatform.ViewPlatformAndroid

class AndroidGroupFactory(val context: Context): InspViewFactory<MediaGroup, InspGroupView> {

    private fun initView(view: ViewGroup) {
        view.clipChildren = false
        view.clipToPadding = false
        view.clipToOutline = true
        view.setWillNotDraw(false)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun create(
        media: MediaGroup,
        parentInsp: InspParent,
        unitsConverter: BaseUnitsConverter,
        templateView: InspTemplateView,
        fontsManager: FontsManager,
        loggerGetter: LoggerGetter,
        movableTouchHelperFactory: MovableTouchHelperFactory
    ): InspGroupView {

        val innerView: InnerGroupViewAndroid

        if (media.orientation == GroupOrientation.Z) {
            innerView = InnerGroupZView(context, templateView, unitsConverter)
        } else {
            innerView = InnerGroupLinearView(context, media, templateView, unitsConverter)
        }

        val innerAndroidView = innerView as ViewGroup

        val animationHelper = AnimationHelperAndroid(media, innerView)

        val inspView = InspGroupViewAndroid(media, parentInsp,
            ViewPlatformAndroid(innerAndroidView), unitsConverter,
            animationHelper, innerView, loggerGetter, movableTouchHelperFactory, templateView)

        animationHelper.inspView = inspView

        innerView.mDrawAnimations = {
            animationHelper.drawAnimations(it, inspView.currentFrame)
        }

        innerView.mDrawOnGlCanvas = inspView::mayDrawOnGlCanvas

        innerAndroidView.setOnTouchListener { _, event ->
            (inspView.movableTouchHelper as? MovableTouchHelperAndroid?)?.onTouchMovable(event) != null
        }

        initView(innerAndroidView)

        return inspView
    }
}