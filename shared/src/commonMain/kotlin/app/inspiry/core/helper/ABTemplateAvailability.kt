package app.inspiry.core.helper

import app.inspiry.core.media.TemplateAvailability
import dev.icerock.moko.resources.AssetResource

interface ABTemplateAvailability {
    fun getTemplateAvailability(
        current: TemplateAvailability,
        originalTemplatePath: AssetResource
    ): TemplateAvailability
}