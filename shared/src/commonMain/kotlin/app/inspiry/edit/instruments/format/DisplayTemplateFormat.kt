package app.inspiry.edit.instruments.format

import app.inspiry.core.media.TemplateFormat
import dev.icerock.moko.resources.StringResource

class DisplayTemplateFormat(
    val iconWidthDp: Int, val iconHeightDp: Int,
    val text: StringResource, val premium: Boolean, val templateFormat: TemplateFormat
)