package app.inspiry.views.factory

import android.content.Context
import app.inspiry.animator.helper.AnimationHelperAndroid
import app.inspiry.core.data.FPS
import app.inspiry.font.provider.FontsManager
import app.inspiry.core.media.BaseUnitsConverter
import app.inspiry.core.media.MediaVector
import app.inspiry.core.log.LoggerGetter
import app.inspiry.views.InspParent
import app.inspiry.views.template.InspTemplateView
import app.inspiry.views.touch.MovableTouchHelperAndroid
import app.inspiry.views.touch.MovableTouchHelperFactory
import app.inspiry.views.vector.InnerVectorViewAndroid
import app.inspiry.views.vector.InspVectorView
import app.inspiry.views.viewplatform.ViewPlatformAndroid

class AndroidVectorFactory(val context: Context) : InspViewFactory<MediaVector, InspVectorView> {

    override fun create(
        media: MediaVector,
        parentInsp: InspParent,
        unitsConverter: BaseUnitsConverter,
        templateView: InspTemplateView,
        fontsManager: FontsManager, loggerGetter: LoggerGetter,
        movableTouchHelperFactory: MovableTouchHelperFactory
    ): InspVectorView {

        val fps = FPS
        val innerView = InnerVectorViewAndroid(context, fps)
        val platformView = ViewPlatformAndroid(innerView)

        val animationHelper = AnimationHelperAndroid(media, innerView)
        val inspView = InspVectorView(
            media,
            parentInsp,
            platformView,
            unitsConverter,
            animationHelper,
            innerView,
            fps,
            loggerGetter,
            movableTouchHelperFactory,
            templateView
        )

        animationHelper.inspView = inspView

        innerView.movableTouchHelper = inspView.movableTouchHelper as? MovableTouchHelperAndroid?
        innerView.drawListener = { animationHelper.drawAnimations(it, inspView.currentFrame) }

        return inspView
    }
}