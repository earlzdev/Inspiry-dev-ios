package app.inspiry.font.model

import app.inspiry.core.util.getFileName
import app.inspiry.font.provider.PlatformFontPathProvider
import app.inspiry.core.util.removeExt
import app.inspiry.core.util.removeScheme
import dev.icerock.moko.resources.FontResource

sealed class FontPath(
    val path: String,
    val displayName: String,
    val forPremium: Boolean
) {
    open fun supportsBold(platformFontPathProvider: PlatformFontPathProvider) = false
    open fun supportsLight(platformFontPathProvider: PlatformFontPathProvider) = false
    open fun supportsItalic(platformFontPathProvider: PlatformFontPathProvider) = false

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is FontPath) return false

        if (path != other.path) return false

        return true
    }

    override fun hashCode(): Int {
        return path.hashCode()
    }
}

class PredefinedFontPath(
    //path here is id to find necessary data.
    path: String,
    displayName: String = path.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() },
    val regularId: FontResource? = null,
    val italicId: FontResource? = null,
    val lightId: FontResource? = null,
    val boldId: FontResource? = null,
    forPremium: Boolean = false,
) : FontPath(path, displayName, forPremium) {

    override fun supportsBold(platformFontPathProvider: PlatformFontPathProvider): Boolean {
        return path == platformFontPathProvider.defaultFont().path || boldId != null
    }

    override fun supportsLight(platformFontPathProvider: PlatformFontPathProvider): Boolean {
        return path == platformFontPathProvider.defaultFont().path || lightId != null
    }

    override fun supportsItalic(platformFontPathProvider: PlatformFontPathProvider): Boolean {
        return path == platformFontPathProvider.defaultFont().path || italicId != null
    }

    fun getResourceByStyle(style: InspFontStyle): FontResource? {
        val result = when (style) {
            InspFontStyle.regular -> regularId
            InspFontStyle.bold -> boldId
            InspFontStyle.italic -> italicId
            InspFontStyle.light -> lightId
        }

        return result ?: regularId
    }
}

class UploadedFontPath(path: String) :
    FontPath("file://$path", path.getFileName().removeExt(), forPremium = true)