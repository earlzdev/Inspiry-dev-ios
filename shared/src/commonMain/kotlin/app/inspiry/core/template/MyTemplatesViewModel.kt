package app.inspiry.core.template

import app.inspiry.core.data.TemplatePath
import app.inspiry.core.data.UserSavedTemplatePath
import com.russhwolf.settings.Settings
import dev.icerock.moko.mvvm.viewmodel.ViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class MyTemplatesViewModel(
    private val templateReadWrite: TemplateReadWrite
) : ViewModel() {

    private val _templates = MutableStateFlow<MutableList<TemplatePath>?>(null)
    val templates: StateFlow<MutableList<TemplatePath>?> = _templates

    fun loadMyStories(finishHandler: (() -> Unit)? = null) {
        _templates.value = mutableListOf()
        viewModelScope.launch {
            val templatesRaw = withContext(Dispatchers.Default) {

                val res = mutableListOf<TemplatePath>()
                templateReadWrite.myStoriesPaths().mapTo(res) {
                    UserSavedTemplatePath(it)
                }
                res
            }

            _templates.value = templatesRaw
            finishHandler?.invoke()
        }
    }
}