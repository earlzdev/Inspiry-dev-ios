package app.inspiry.views.factory.viewFactoryDrafts

import app.inspiry.core.data.Size
import app.inspiry.core.log.LoggerGetter
import app.inspiry.core.media.BaseUnitsConverter
import app.inspiry.core.template.TemplateReadWrite
import app.inspiry.edit.EditCoordinator
import app.inspiry.font.helpers.TextCaseHelper
import app.inspiry.font.provider.FontsManager
import app.inspiry.music.client.BaseAudioPlayer
import app.inspiry.music.client.EmptyAudioPlayer
import app.inspiry.palette.model.AbsPaletteColor
import app.inspiry.views.InspView
import app.inspiry.views.factory.ViewFromMediaFactory
import app.inspiry.views.guideline.GuidelineManager
import app.inspiry.views.infoview.InfoViewModel
import app.inspiry.views.template.InspTemplateView
import app.inspiry.views.template.TemplateMode
import app.inspiry.views.touch.MovableTouchHelperFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.json.Json
import okio.FileSystem

class InspTemplateViewCommon(
    loggerGetter: LoggerGetter,
    unitsConverter: BaseUnitsConverter,
    infoViewModel: InfoViewModel?,
    json: Json,
    textCaseHelper: TextCaseHelper,
    fontsManager: FontsManager,
    templateSaver: TemplateReadWrite,
    guidelineManager: GuidelineManager,
    viewsFactory: ViewFromMediaFactory,
    movableTouchHelperFactory: MovableTouchHelperFactory,
    fileSystem: FileSystem,
    initialTemplateMode: TemplateMode, override val viewWidth: Int, override val viewHeight: Int
) : InspTemplateView(
    loggerGetter,
    unitsConverter,
    infoViewModel,
    json,
    textCaseHelper,
    fontsManager,
    templateSaver,
    guidelineManager,
    viewsFactory,
    movableTouchHelperFactory,
    fileSystem,
    initialTemplateMode
) {

    // TODO
    override val viewScope: CoroutineScope = CoroutineScope(SupervisorJob())
    override val containerScope: CoroutineScope = CoroutineScope(Dispatchers.Main)

    override val currentSize: MutableStateFlow<Size?> = MutableStateFlow(getSize())

    override fun invalidateGuidelines() {

    }

    override fun initMusicPlayer(): BaseAudioPlayer {
        return EmptyAudioPlayer()
    }

    override val waitVideoSeek: StateFlow<Int> = MutableStateFlow(0)

    override fun isWindowVisible(): Boolean {
        return true
    }

    override fun post(action: () -> Unit) {

    }

    override val copyInspViewPlusTranslation: Float
        get() = COPY_INSP_VIEW_TRANSLATION_PLUS

    override fun setBackgroundColor(color: AbsPaletteColor?) {

    }

    override fun setBackgroundColor(color: Int) {

    }

    override fun addViewToHierarchy(view: InspView<*>) {

    }

    override fun addViewToHierarchy(index: Int, view: InspView<*>) {

    }
}