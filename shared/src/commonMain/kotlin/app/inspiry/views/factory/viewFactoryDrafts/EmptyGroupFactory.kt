package app.inspiry.views.factory.viewFactoryDrafts

import app.inspiry.font.provider.FontsManager
import app.inspiry.core.media.BaseUnitsConverter
import app.inspiry.core.media.MediaGroup
import app.inspiry.core.log.LoggerGetter
import app.inspiry.views.InspParent
import app.inspiry.views.factory.InspViewFactory
import app.inspiry.views.group.*
import app.inspiry.views.template.InspTemplateView
import app.inspiry.views.touch.MovableTouchHelperFactory

class EmptyGroupFactory(val viewProvider: ViewProvider): InspViewFactory<MediaGroup, InspGroupView> {

    override fun create(
        media: MediaGroup,
        parentInsp: InspParent,
        unitsConverter: BaseUnitsConverter,
        templateView: InspTemplateView,
        fontsManager: FontsManager,
        loggerGetter: LoggerGetter,
        movableTouchHelperFactory: MovableTouchHelperFactory
    ): InspGroupView {

        val inspView = viewProvider.getInspGroupView(media, parentInsp,
            unitsConverter,
            templateView,
            fontsManager,
            loggerGetter, movableTouchHelperFactory)

        return inspView
    }
}