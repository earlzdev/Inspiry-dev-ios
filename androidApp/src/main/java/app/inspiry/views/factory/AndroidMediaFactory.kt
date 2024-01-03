package app.inspiry.views.factory

import android.content.Context
import app.inspiry.animator.helper.AnimationHelperAndroid
import app.inspiry.font.provider.FontsManager
import app.inspiry.core.media.BaseUnitsConverter
import app.inspiry.core.media.MediaImage
import app.inspiry.core.log.LoggerGetter
import app.inspiry.views.InspParent
import app.inspiry.views.media.InnerMediaViewAndroid
import app.inspiry.views.media.InspMediaView
import app.inspiry.views.template.InspTemplateView
import app.inspiry.views.template.TemplateMode
import app.inspiry.views.touch.MovableTouchHelperFactory
import app.inspiry.views.viewplatform.ViewPlatformAndroid
import okio.FileSystem

class AndroidMediaFactory(val context: Context) : InspViewFactory<MediaImage, InspMediaView> {

    override fun create(
        media: MediaImage,
        parentInsp: InspParent,
        unitsConverter: BaseUnitsConverter,
        templateView: InspTemplateView,
        fontsManager: FontsManager,
        loggerGetter: LoggerGetter,
        movableTouchHelperFactory: MovableTouchHelperFactory
    ): InspMediaView {

        val innerView = InnerMediaViewAndroid(context, media)
        val viewPlatform = ViewPlatformAndroid(innerView)

        val animationsHelper = AnimationHelperAndroid(media, innerView)

        val inspView = InspMediaView(
            media,
            parentInsp,
            viewPlatform,
            unitsConverter,
            animationsHelper,
            loggerGetter,
            innerView,
            movableTouchHelperFactory,
            templateView,
            FileSystem.SYSTEM
        )

        animationsHelper.inspView = inspView
        innerView.mediaView = inspView

        return inspView
    }
}