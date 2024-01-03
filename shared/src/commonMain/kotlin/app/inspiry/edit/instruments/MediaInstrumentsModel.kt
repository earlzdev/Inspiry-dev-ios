package app.inspiry.edit.instruments

import app.inspiry.core.manager.InspRemoteConfig
import app.inspiry.core.manager.LicenseManager
import app.inspiry.removebg.RemovingBgViewModel
import app.inspiry.views.InspView
import app.inspiry.views.media.InspMediaView
import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.MutableStateFlow

class MediaInstrumentsModel(
    mediaView: InspMediaView,
    val licenseManager: LicenseManager,
    val remoteConfig: InspRemoteConfig,
    val settings: Settings,
    val onRemoveBGAction: () -> Unit
) : BottomInstrumentsViewModel {
    val currentMedia = MutableStateFlow<InspMediaView?>(mediaView)

    override fun onSelectedViewChanged(newSelected: InspView<*>?) {
        currentMedia.value = newSelected as? InspMediaView
    }

    fun startRemoveBg() {
        onRemoveBGAction()
    }

    fun canRemoveBgOrOpenPromo(hasPremium: Boolean = licenseManager.hasPremiumState.value): Boolean {
        return if (hasPremium)
            true
        else {
            val removeBgTimes = settings.getInt(RemovingBgViewModel.KEY_NUM_PROCESSED_IMAGES)
            removeBgTimes < remoteConfig.getInt("remove_bg_free_tries")
        }
    }

    override fun onHide() {
        currentMedia.value = null
    }
}