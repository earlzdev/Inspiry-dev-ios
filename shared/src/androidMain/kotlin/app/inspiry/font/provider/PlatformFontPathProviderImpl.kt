package app.inspiry.font.provider

import app.inspiry.font.model.PredefinedFontPath
import app.inspiry.projectutils.R
import dev.icerock.moko.resources.FontResource

class PlatformFontPathProviderImpl : PlatformFontPathProvider {

    private val _robotoFont = PredefinedFontPath(FontsManager.ROBOTO_FONT_PATH)
    private val sfFont = PredefinedFontPath(
        FontsManager.SF_FONT_PATH,
        FontsManager.SF_FONT_NAME,
        regularId = FontResource(R.font.sf_pro_display_regular),
        italicId = FontResource(R.font.sf_pro_display_italic),
        boldId = FontResource(R.font.sf_pro_display_bold),
        lightId = FontResource(R.font.sf_pro_display_light)
    )

    override fun getRobotoFont(): PredefinedFontPath {
        return _robotoFont
    }

    override fun getSfProFont(): PredefinedFontPath {
        return sfFont
    }

    override fun defaultFont(): PredefinedFontPath {
        return getRobotoFont()
    }
}