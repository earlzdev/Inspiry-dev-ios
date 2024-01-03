package app.inspiry.edit

import app.inspiry.core.data.OriginalTemplateData
import app.inspiry.core.data.TemplatePath
import app.inspiry.core.data.UserSavedTemplatePath
import app.inspiry.core.manager.DebugManager

sealed class ExportAction

class ExportActionForPremium(val source: String) : ExportAction()
class ExportActionSave(
    val isStatic: Boolean,
    var templatePath: TemplatePath,
    val originalTemplateData: OriginalTemplateData
) : ExportAction()