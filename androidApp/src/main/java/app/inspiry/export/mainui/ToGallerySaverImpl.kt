package app.inspiry.export.mainui

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import app.inspiry.core.log.KLogger
import app.inspiry.core.util.getExt
import app.inspiry.utils.getUriForIntent
import app.inspiry.utils.printDebug
import app.inspiry.utils.toastError
import com.soywiz.klock.DateFormat
import com.soywiz.klock.DateTime
import com.soywiz.klock.format
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okio.Sink
import okio.buffer
import okio.sink
import okio.source
import java.io.File
import java.io.FileOutputStream

class ToGallerySaverImpl(val logger: KLogger): ToGallerySaver {

    @Suppress("DEPRECATION")
    override suspend fun saveToGalleryAsync(
        context: Context, saveAsPicture: Boolean,
        file: File, mimeType: String, templateName: String
    ): Uri? {

        var finalUri: Uri? = null
        try {

            if (Build.VERSION.SDK_INT >= 29) {
                finalUri = withContext(Dispatchers.IO) {
                    // otherwise the bitmap is black on huawei phones
                    delay(100)

                    val values = ContentValues()
                    values.put(
                        MediaStore.MediaColumns.RELATIVE_PATH,
                        "${Environment.DIRECTORY_DCIM}/Inspiry"
                    )

                    values.put(MediaStore.MediaColumns.DATE_TAKEN, System.currentTimeMillis())
                    values.put(MediaStore.MediaColumns.IS_PENDING, true)
                    values.put(
                        MediaStore.MediaColumns.DISPLAY_NAME,
                        getTemplateDisplayFileName(file, templateName)
                    )
                    values.put(MediaStore.MediaColumns.MIME_TYPE, mimeType)

                    val cr = context.contentResolver
                    val uri = cr.insert(
                        if (saveAsPicture) MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                        else MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values
                    )!!

                    var wrote = false

                    try {
                        val stream = cr.openOutputStream(uri)!!

                        file.copyTo(stream.sink())
                        wrote = true

                    } finally {
                        if (!wrote)
                            cr.delete(uri, null, null)
                        else {
                            if (wrote) {
                                values.put(MediaStore.MediaColumns.IS_PENDING, false)
                                cr.update(uri, values, null, null)
                            }
                        }
                    }
                    uri
                }

            } else {

                val newFile = withContext(Dispatchers.IO) {
                    // otherwise the bitmap is black on huawei phones
                    delay(200)
                    val newParent = File(
                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES),
                        "Inspiry"
                    )

                    newParent.mkdirs()

                    val newFile = File(
                        newParent,
                        getTemplateDisplayFileName(file, templateName)
                    )

                    file.copyTo(FileOutputStream(newFile, false).sink())
                    newFile
                }
                finalUri = newFile.getUriForIntent(context)


                logger.info { "saveToGallery newFile ${newFile.absolutePath}" }

            }

        } catch (e: Exception) {
            e.toastError()
            e.printDebug()
        }

        logger.info { "saveToGallery finalUri ${finalUri}" }

        return finalUri
    }

    private fun getTemplateDisplayFileName(file: File, templateName: String): String {
        val simpleDateFormat = DateFormat("hh_mm_ss_dd_MM_YY")
        val dateString = simpleDateFormat.format(DateTime.now())
        return "${templateName}_$dateString.${file.name.getExt()}"
    }

    private fun File.copyTo(to: Sink) {
        this.source().use { a ->
            to.buffer().use { b -> b.writeAll(a) }
        }
    }
}