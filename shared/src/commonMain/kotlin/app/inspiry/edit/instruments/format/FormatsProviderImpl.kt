package app.inspiry.edit.instruments.format

import app.inspiry.MR
import app.inspiry.core.media.TemplateFormat

class FormatsProviderImpl : FormatsProvider {
    override fun getFormats(): List<DisplayTemplateFormat> {
        return listOf(
            DisplayTemplateFormat(13, 23, MR.strings.format_story, false, TemplateFormat.story),
            DisplayTemplateFormat(14, 18, MR.strings.format_post, true, TemplateFormat.post),
            DisplayTemplateFormat(15, 15, MR.strings.format_square, true, TemplateFormat.square),
            DisplayTemplateFormat(23, 12, MR.strings.format_horizontal, true, TemplateFormat.horizontal),
        )
    }
}