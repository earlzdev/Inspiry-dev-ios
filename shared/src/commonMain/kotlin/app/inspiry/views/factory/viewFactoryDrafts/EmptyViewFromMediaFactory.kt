package app.inspiry.views.factory.viewFactoryDrafts

import app.inspiry.font.provider.FontsManager
import app.inspiry.core.log.LoggerGetter
import app.inspiry.core.media.*
import app.inspiry.views.InspParent
import app.inspiry.views.InspView
import app.inspiry.views.factory.InspViewFactory
import app.inspiry.views.factory.ViewFromMediaFactory
import app.inspiry.views.simplevideo.InspSimpleVideoView
import app.inspiry.views.template.InspTemplateView
import app.inspiry.views.touch.MovableTouchHelperFactory

class EmptyViewFromMediaFactory(val viewProvider: ViewProvider) : ViewFromMediaFactory {

    override fun simpleVideo(
        media: MediaImage,
        parentInsp: InspParent,
        unitsConverter: BaseUnitsConverter,
        templateView: InspTemplateView,
        fontsManager: FontsManager,
        loggerGetter: LoggerGetter,
        movableTouchHelperFactory: MovableTouchHelperFactory
    ): InspSimpleVideoView {

        val simpleViewFactory = EmptySimpleVideoFactory(viewProvider)

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
            is MediaGroup -> EmptyGroupFactory(viewProvider)
            is MediaVector -> EmptyVectorFactory(viewProvider)
            is MediaText -> EmptyTextFactory(viewProvider)
            is MediaPath -> EmptyPathFactory(viewProvider)
            is MediaImage -> EmptyMediaFactory(viewProvider)
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