package app.inspiry.views.factory.viewFactoryDrafts

import app.inspiry.font.provider.FontsManager
import app.inspiry.core.media.BaseUnitsConverter
import app.inspiry.core.media.MediaImage
import app.inspiry.core.log.LoggerGetter
import app.inspiry.views.InspParent
import app.inspiry.views.factory.InspViewFactory
import app.inspiry.views.simplevideo.InspSimpleVideoView
import app.inspiry.views.template.InspTemplateView
import app.inspiry.views.touch.MovableTouchHelperFactory

class EmptySimpleVideoFactory(val viewProvider: ViewProvider) :
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

        val inspView = viewProvider.getInspVideoView(
            media,
            parentInsp,
            unitsConverter,
            templateView,
            fontsManager,
            loggerGetter,
            movableTouchHelperFactory,
        )

        return  inspView
    }
}