package app.inspiry.font.helpers

import app.inspiry.font.model.*
import app.inspiry.font.provider.FontsManager
import app.inspiry.font.provider.PlatformFontPathProvider
import dev.icerock.moko.resources.FontResource

abstract class PlatformFontObtainer<Typeface>(
    val fontsManager: FontsManager,
    private val platformFontPathProvider: PlatformFontPathProvider
) {

    abstract fun createDefaultTypeface(fontStyle: InspFontStyle?): Typeface

    //throws if file is not found
    @Throws(Exception::class)
    protected abstract fun createFromFile(path: UploadedFontPath): Typeface

    protected abstract fun createResourceTypeface(fontResource: FontResource): Typeface

    @Throws(TypefaceObtainingException::class)
    fun getTypefaceFromPath(
        path: FontPath,
        style: InspFontStyle
    ): Typeface {

        return when (path) {
            is UploadedFontPath -> {

                try {
                    createFromFile(path)
                } catch (e: Exception) {
                    throw TypefaceObtainingException(e)
                }
            }
            is PredefinedFontPath -> {
                if (path == platformFontPathProvider.defaultFont())
                    createDefaultTypeface(style)
                else
                    createResourceTypeface(path.getResourceByStyle(style)!!)

            }
        }
    }

    @Throws(TypefaceObtainingException::class)
    fun getTypefaceFromFontData(
        fontData: FontData?,
    ): Typeface {

        val fontPath = fontData?.fontPath

        return if (fontData == null || fontPath.isNullOrEmpty()) {
            createDefaultTypeface(fontData?.fontStyle)

        } else {
            val fontPathData = fontsManager.getFontPathByIdWithFile(fontPath)

            getTypefaceFromPath(fontPathData, fontData.fontStyle ?: InspFontStyle.regular)
        }
    }


    inline fun getFromPathToastOnError(
        path: FontPath,
        style: InspFontStyle,
        onError: (TypefaceObtainingException) -> Unit
    ): Typeface {
        return try {
            getTypefaceFromPath(path, style)
        } catch (e: TypefaceObtainingException) {
            onError(e)
            createDefaultTypeface(style)
        }
    }
}