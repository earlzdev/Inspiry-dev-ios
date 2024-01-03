package app.inspiry.views.factory.viewFactoryDrafts

import app.inspiry.font.provider.FontsManager
import app.inspiry.core.media.BaseUnitsConverter
import app.inspiry.core.media.MediaText
import app.inspiry.core.log.LoggerGetter
import app.inspiry.views.InspParent
import app.inspiry.views.factory.InspViewFactory
import app.inspiry.views.template.InspTemplateView
import app.inspiry.views.text.InspTextView
import app.inspiry.views.touch.MovableTouchHelperFactory

class EmptyTextFactory(val viewProvider: ViewProvider) : InspViewFactory<MediaText, InspTextView> {

    override fun create(
        media: MediaText,
        parentInsp: InspParent,
        unitsConverter: BaseUnitsConverter,
        templateView: InspTemplateView,
        fontsManager: FontsManager,
        loggerGetter: LoggerGetter,
        movableTouchHelperFactory: MovableTouchHelperFactory
    ): InspTextView {

        val inspView = viewProvider.getInspTextView(
            media,
            parentInsp,
            unitsConverter,
            templateView,
            fontsManager,
            loggerGetter,
            movableTouchHelperFactory,
        )

        return inspView
    }
}