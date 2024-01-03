package app.inspiry.views.factory

import android.content.Context
import app.inspiry.font.provider.FontsManager
import app.inspiry.core.log.LoggerGetter
import app.inspiry.core.media.*
import app.inspiry.views.InspParent
import app.inspiry.views.InspView
import app.inspiry.views.simplevideo.InspSimpleVideoView
import app.inspiry.views.template.InspTemplateView
import app.inspiry.views.template.TemplateMode
import app.inspiry.views.touch.MovableTouchHelperFactory

class ViewFromMediaAndroidFactory(val context: Context) : ViewFromMediaFactory {

    override fun simpleVideo(
        media: MediaImage,
        parentInsp: InspParent,
        unitsConverter: BaseUnitsConverter,
        templateView: InspTemplateView,
        fontsManager: FontsManager,
        loggerGetter: LoggerGetter,
        movableTouchHelperFactory: MovableTouchHelperFactory
    ): InspSimpleVideoView {

        val simpleViewFactory = AndroidSimpleVideoFactory(context)

        return simpleViewFactory.create(
            media, parentInsp, unitsConverter, templateView, fontsManager, loggerGetter, movableTouchHelperFactory
        )
    }

    @Suppress("UNCHECKED_CAST")
    override fun inspView(
        media: Media,
        parentInsp: InspParent,
        unitsConverter: BaseUnitsConverter,
        templateView: InspTemplateView,
        fontsManager: FontsManager,
        loggerGetter: LoggerGetter,
        movableTouchHelperFactory: MovableTouchHelperFactory
    ): InspView<*> {

        val factory: InspViewFactory<Media, InspView<Media>> = when (media) {
            is MediaGroup -> AndroidGroupFactory(context)
            is MediaVector -> AndroidVectorFactory(context)
            is MediaText -> AndroidTextFactory(context)
            is MediaPath -> AndroidPathFactory(context)
            is MediaImage -> AndroidMediaFactory(context)
            else -> throw IllegalStateException()
        } as InspViewFactory<Media, InspView<Media>>

        return factory.create(
            media,
            parentInsp,
            unitsConverter,
            templateView,
            fontsManager,
            loggerGetter,
            movableTouchHelperFactory
        )
    }
}