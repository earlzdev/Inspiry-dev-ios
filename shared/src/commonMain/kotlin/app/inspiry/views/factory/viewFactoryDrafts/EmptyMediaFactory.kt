package app.inspiry.views.factory.viewFactoryDrafts

import app.inspiry.font.provider.FontsManager
import app.inspiry.core.media.BaseUnitsConverter
import app.inspiry.core.media.MediaImage
import app.inspiry.core.log.LoggerGetter
import app.inspiry.views.InspParent
import app.inspiry.views.factory.InspViewFactory
import app.inspiry.views.media.InspMediaView
import app.inspiry.views.template.InspTemplateView
import app.inspiry.views.touch.MovableTouchHelperFactory

class EmptyMediaFactory(val viewProvider: ViewProvider) :
    InspViewFactory<MediaImage, InspMediaView> {

    override fun create(
        media: MediaImage,
        parentInsp: InspParent,
        unitsConverter: BaseUnitsConverter,
        templateView: InspTemplateView,
        fontsManager: FontsManager,
        loggerGetter: LoggerGetter,
        movableTouchHelperFactory: MovableTouchHelperFactory
    ): InspMediaView {

        val inspView = viewProvider.getInspMediaView(
            media,
            parentInsp,
            unitsConverter,
            templateView,
            fontsManager,
            loggerGetter,
            movableTouchHelperFactory,
        )

        return inspView as InspMediaView
    }
}