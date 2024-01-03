package app.inspiry.font.provider

import app.inspiry.font.model.UploadedFontPath

interface UploadedFontsProvider {

    fun getFonts(): List<UploadedFontPath>
}