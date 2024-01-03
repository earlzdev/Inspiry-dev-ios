package app.inspiry.helpers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import app.inspiry.core.template.TemplateReadWrite
import app.inspiry.core.template.TemplateViewModel

class TemplateViewModelFactory(
    private val templateReadWrite: TemplateReadWrite
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return TemplateViewModel(templateReadWrite) as T
    }
}