package app.inspiry.font.provider

import app.inspiry.font.model.PredefinedFontPath

interface PlatformFontPathProvider {
    fun getRobotoFont(): PredefinedFontPath
    fun getSfProFont(): PredefinedFontPath
    fun defaultFont(): PredefinedFontPath
}