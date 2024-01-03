package app.inspiry.core.template

import app.inspiry.core.data.InspResponse
import app.inspiry.core.data.InspResponseData
import app.inspiry.core.data.InspResponseError
import app.inspiry.core.data.TemplatePath
import app.inspiry.core.media.Template
import dev.icerock.moko.mvvm.viewmodel.ViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TemplateViewModel(val templateReadWrite: TemplateReadWrite) : ViewModel() {

    private val _template = MutableStateFlow<InspResponse<Template>?>(null)
    val template: StateFlow<InspResponse<Template>?> = _template

    // template can be changed in InspTemplateView internally, so we need to update the reference in onDestroy
    // of activities where it is used
    fun updateTemplate(template: Template) {
        _template.value = InspResponseData(template)
    }

    fun loadTemplate(path: TemplatePath, skipIfLoaded: Boolean = false) {
        if (skipIfLoaded && template.value != null) return

        viewModelScope.launch {
            val templatesRaw = withContext(Dispatchers.Default) {
                try {
                    InspResponseData(templateReadWrite.loadTemplateFromPath(path))
                } catch (e: Exception) {
                    InspResponseError(e)
                }
            }
            _template.emit(templatesRaw)
        }
    }
}
