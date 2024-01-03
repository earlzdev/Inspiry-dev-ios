package app.inspiry.font.provider

import android.content.Context
import app.inspiry.font.model.UploadedFontPath
import app.inspiry.font.util.FontUtils
import app.inspiry.font.util.getSavedFontsDir
import java.io.File

class UploadedFontsProviderImpl(val context: Context) : UploadedFontsProvider {

    override fun getFonts(): List<UploadedFontPath> {

        val fonts = mutableListOf<UploadedFontPath>()
        val savedFontsDir = context.getSavedFontsDir()
        val savedFonts = savedFontsDir.list { dir, name ->
            FontUtils.isFont(name)
        }
        if (savedFonts != null) {
            fonts.addAll(savedFonts.map {
                UploadedFontPath(File(savedFontsDir, it).absolutePath)
            })
        }
        return fonts
    }
}