package app.inspiry.views.factory

import android.content.Context
import android.view.TextureView
import app.inspiry.core.animator.helper.getEmptyAnimationHelper
import app.inspiry.font.provider.FontsManager
import app.inspiry.core.media.BaseUnitsConverter
import app.inspiry.core.media.MediaImage
import app.inspiry.core.log.LoggerGetter
import app.inspiry.views.InspParent
import app.inspiry.views.simplevideo.InspSimpleVideoView
import app.inspiry.views.simplevideo.SimpleExoVideoPlayer
import app.inspiry.views.template.InspTemplateView
import app.inspiry.views.template.TemplateMode
import app.inspiry.views.touch.MovableTouchHelperFactory
import app.inspiry.views.viewplatform.ViewPlatformAndroid

class AndroidSimpleVideoFactory(val context: Context) :
    InspViewFactory<MediaImage, InspSimpleVideoView> {

    override fun create(
        media: MediaImage,
        parentInsp: InspParent,
        unitsConverter: BaseUnitsConverter,
        templateView: InspTemplateView,
        fontsManager: FontsManager,
        loggerGetter: LoggerGetter,
        movableTouchHelperFactory: MovableTouchHelperFactory
    ): InspSimpleVideoView {
        val v = TextureView(context)
        val viewPlatform = ViewPlatformAndroid(v)

        return InspSimpleVideoView(
            media,
            parentInsp,
            viewPlatform,
            unitsConverter,
            getEmptyAnimationHelper(),
            loggerGetter,
            movableTouchHelperFactory, {
                SimpleExoVideoPlayer(context, v)
            },
            templateView
        )
    }
}