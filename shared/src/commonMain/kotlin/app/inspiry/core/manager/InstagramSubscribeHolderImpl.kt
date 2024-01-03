package app.inspiry.core.manager

import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class InstagramSubscribeHolderImpl(val settings: Settings): InstagramSubscribeHolder {

    private val _subscribed: MutableStateFlow<Boolean> by lazy { MutableStateFlow(settings.getBoolean(
        SUBSCRIBED_TO_INST_KEY, false)) }

    override val subscribed: StateFlow<Boolean>
        get() = _subscribed

    override fun setSubscribed() {
        settings.putBoolean(SUBSCRIBED_TO_INST_KEY, true)
        _subscribed.value = true
    }

    companion object {
        private const val SUBSCRIBED_TO_INST_KEY = "subscribed_to_inst"
    }
}