package app.inspiry.font.helpers

import android.content.Context
import android.graphics.Typeface
import app.inspiry.font.model.InspFontStyle
import app.inspiry.font.model.UploadedFontPath
import app.inspiry.font.provider.FontsManager
import app.inspiry.font.provider.PlatformFontPathProvider
import app.inspiry.core.util.removeScheme
import dev.icerock.moko.resources.FontResource
import java.io.File
import java.io.FileNotFoundException

class PlatformFontObtainerImpl(
    val context: Context,
    fontsManager: FontsManager,
    platformFontPathProvider: PlatformFontPathProvider
) : PlatformFontObtainer<Typeface>(fontsManager, platformFontPathProvider) {

    override fun createDefaultTypeface(fontStyle: InspFontStyle?): Typeface {
        val fontFamily: String
        val style: Int

        when (fontStyle ?: InspFontStyle.regular) {
            InspFontStyle.regular -> {
                fontFamily = "sans-serif"
                style = Typeface.NORMAL
            }
            InspFontStyle.bold -> {
                fontFamily = "sans-serif"
                style = Typeface.BOLD
            }
            InspFontStyle.italic -> {
                fontFamily = "sans-serif"
                style = Typeface.ITALIC
            }
            InspFontStyle.light -> {
                fontFamily = "sans-serif-light"
                style = Typeface.NORMAL
            }
        }

        return Typeface.create(fontFamily, style)
    }

    override fun createFromFile(path: UploadedFontPath): Typeface {
        val file = File(path.path.removeScheme())
        if (file.exists())
            return Typeface.createFromFile(file)
        else {
            throw FileNotFoundException(file.path)
        }
    }

    override fun createResourceTypeface(fontResource: FontResource): Typeface {
        return fontResource.getTypeface(context)!!
    }
}