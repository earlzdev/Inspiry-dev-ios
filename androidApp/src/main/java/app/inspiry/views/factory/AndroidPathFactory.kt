package app.inspiry.views.factory

import android.content.Context
import app.inspiry.animator.helper.AnimationHelperAndroid
import app.inspiry.font.provider.FontsManager
import app.inspiry.core.media.BaseUnitsConverter
import app.inspiry.core.media.MediaPath
import app.inspiry.core.log.LoggerGetter
import app.inspiry.views.InspParent
import app.inspiry.views.path.AndroidPath
import app.inspiry.views.path.InnerViewAndroidPath
import app.inspiry.views.path.InspPathView
import app.inspiry.views.template.InspTemplateView
import app.inspiry.views.template.TemplateMode
import app.inspiry.views.touch.MovableTouchHelperAndroid
import app.inspiry.views.touch.MovableTouchHelperFactory
import app.inspiry.views.viewplatform.ViewPlatformAndroid

class AndroidPathFactory(val context: Context) : InspViewFactory<MediaPath, InspPathView> {

    override fun create(
        media: MediaPath,
        parentInsp: InspParent,
        unitsConverter: BaseUnitsConverter,
        templateView: InspTemplateView,
        fontsManager: FontsManager,
        loggerGetter: LoggerGetter,
        movableTouchHelperFactory: MovableTouchHelperFactory
    ): InspPathView {
        val innerView = InnerViewAndroidPath(context, media)
        val platformView = ViewPlatformAndroid(innerView)

        val animationHelper = AnimationHelperAndroid(media, innerView)

        val inspView = InspPathView(
            media,
            parentInsp,
            platformView,
            unitsConverter,
            animationHelper,
            AndroidPath(),
            innerView,
            loggerGetter,
            movableTouchHelperFactory,
            templateView
        )

        animationHelper.inspView = inspView

        innerView.drawListener = { animationHelper.drawAnimations(it, inspView.currentFrame) }
        innerView.drawPath = { inspView.drawPath() as AndroidPath? }
        innerView.movableTouchHelper = inspView.movableTouchHelper as? MovableTouchHelperAndroid?

        return inspView
    }
}