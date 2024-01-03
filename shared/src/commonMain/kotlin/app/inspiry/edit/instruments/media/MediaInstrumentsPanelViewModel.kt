package app.inspiry.edit.instruments.media

import app.inspiry.core.analytics.AnalyticsManager
import app.inspiry.core.manager.LicenseManager
import app.inspiry.core.ui.CommonMenu
import app.inspiry.edit.instruments.BottomInstrumentsViewModel
import app.inspiry.slide.*
import app.inspiry.views.InspView
import app.inspiry.views.media.InspMediaView
import kotlinx.coroutines.flow.MutableStateFlow

class MediaInstrumentsPanelViewModel(
    val inspView: InspMediaView,
    val analyticsManager: AnalyticsManager,
    val licenseManager: LicenseManager,
) : BottomInstrumentsViewModel {

    /**
     * public for ios
     */
    val activeMediaInstrument = MutableStateFlow<MediaInstrumentType?>(null)
    private val currentView = MutableStateFlow(inspView)
    private var onSelect: (MediaInstrumentType?, InspMediaView) -> Unit = { _, _ -> }

    val menu: MutableStateFlow<CommonMenu<MediaInstrumentType>> = MutableStateFlow(createMenu())

    private fun createMenu(): CommonMenu<MediaInstrumentType> {
        val availableItems = currentView.value.getAvailableInstrumentTypes()
        return CommonMenu<MediaInstrumentType>().apply {
            availableItems.forEach {
                this.setMenuItem(
                    item = it,
                    text = it.text(),
                    icon = it.icon(),
                    mayBeSelected = it != MediaInstrumentType.REMOVE_BG && it != MediaInstrumentType.REPLACE
                )
            }
        }
    }

    override fun onSelectedViewChanged(newSelected: InspView<*>?) {
        currentView.value = newSelected as? InspMediaView ?: return
        menu.value = createMenu()
        if (!menu.value.contains(activeMediaInstrument.value)) {
                selectInstrument(null)
            }
    }

    override fun onAdditionalPanelClosed() {
        activeMediaInstrument.value = null
    }

    fun selectInstrument(mediaInstrument: MediaInstrumentType?) {
        activeMediaInstrument.value =
            if (mediaInstrument == activeMediaInstrument.value) null else mediaInstrument
        onSelect(activeMediaInstrument.value, currentView.value)
    }

    fun showPremiumBadge(hasPremium: Boolean, mediaInstrument: MediaInstrumentType): Boolean {
        return !hasPremium && mediaInstrument.forPremium()
    }

    fun onInstrumentClick(mediaInstrument: MediaInstrumentType) {
        selectInstrument(mediaInstrument)
    }

    fun setOnSelect(action: (MediaInstrumentType?, InspMediaView) -> Unit) {
        onSelect = action
    }

}