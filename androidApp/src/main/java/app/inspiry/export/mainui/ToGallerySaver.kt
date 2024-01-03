package app.inspiry.export.mainui

import android.content.Context
import android.net.Uri
import java.io.File

interface ToGallerySaver {
    suspend fun saveToGalleryAsync(
        context: Context, saveAsPicture: Boolean,
        file: File, mimeType: String, templateName: String): Uri?
}