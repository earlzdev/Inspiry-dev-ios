package app.inspiry.views.factory.viewFactoryDrafts

import app.inspiry.core.log.LoggerGetter
import app.inspiry.core.media.*
import app.inspiry.font.provider.FontsManager
import app.inspiry.views.InspParent
import app.inspiry.views.group.InspGroupView
import app.inspiry.views.media.InspMediaView
import app.inspiry.views.path.InspPathView
import app.inspiry.views.simplevideo.InspSimpleVideoView
import app.inspiry.views.template.InspTemplateView
import app.inspiry.views.text.InspTextView
import app.inspiry.views.touch.MovableTouchHelperFactory
import app.inspiry.views.vector.InspVectorView


interface ViewProvider {

    fun getInspGroupView(
        media: MediaGroup,
        parentInsp: InspParent,
        unitsConverter: BaseUnitsConverter,
        templateView: InspTemplateView,
        fontsManager: FontsManager,
        loggerGetter: LoggerGetter,
        movableTouchHelperFactory: MovableTouchHelperFactory
    ): InspGroupView

    //TODO remove duplicate arguments from fun, move to viewProvider constructor
    fun getInspMediaView(
        media: MediaImage,
        parentInsp: InspParent,
        unitsConverter: BaseUnitsConverter,
        templateView: InspTemplateView,
        fontsManager: FontsManager,
        loggerGetter: LoggerGetter,
        movableTouchHelperFactory: MovableTouchHelperFactory
    ): InspMediaView

    fun getInspVideoView(
        media: MediaImage,
        parentInsp: InspParent,
        unitsConverter: BaseUnitsConverter,
        templateView: InspTemplateView,
        fontsManager: FontsManager,
        loggerGetter: LoggerGetter,
        movableTouchHelperFactory: MovableTouchHelperFactory
    ): InspSimpleVideoView

    fun getInspPathView(
        media: MediaPath,
        parentInsp: InspParent,
        unitsConverter: BaseUnitsConverter,
        templateView: InspTemplateView,
        fontsManager: FontsManager,
        loggerGetter: LoggerGetter,
        movableTouchHelperFactory: MovableTouchHelperFactory
    ): InspPathView

    fun getInspTextView(
        media: MediaText,
        parentInsp: InspParent,
        unitsConverter: BaseUnitsConverter,
        templateView: InspTemplateView,
        fontsManager: FontsManager,
        loggerGetter: LoggerGetter,
        movableTouchHelperFactory: MovableTouchHelperFactory
    ): InspTextView

    fun getInspVectorView(
        media: MediaVector,
        parentInsp: InspParent,
        unitsConverter: BaseUnitsConverter,
        templateView: InspTemplateView,
        fontsManager: FontsManager,
        loggerGetter: LoggerGetter,
        movableTouchHelperFactory: MovableTouchHelperFactory
    ): InspVectorView
}