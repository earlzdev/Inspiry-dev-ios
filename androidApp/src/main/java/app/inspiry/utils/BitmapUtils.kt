package app.inspiry.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.media.MediaMetadataRetriever
import android.media.ThumbnailUtils
import android.net.Uri
import android.os.Build
import com.appsflyer.internal.bm
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream

object BitmapUtils {

    fun toInputStream(bitmap: Bitmap, quality: Int, format: Bitmap.CompressFormat): InputStream {
        return ByteArrayInputStream(toByteArray(bitmap, quality, format))
    }

    fun toByteArray(bitmap: Bitmap, quality: Int, format: Bitmap.CompressFormat): ByteArray {
        val baos = ByteArrayOutputStream()
        bitmap.compress(format, quality, baos);
        return baos.toByteArray()
    }

    fun getSeriesOfBitmaps(
        context: Context, videoUri: Uri,
        numThumbs: Int, thumbSize: Int
    ): Flow<Bitmap?> {

        return flow {
            MediaMetadataRetriever().use { mediaMetadataRetriever ->
                mediaMetadataRetriever.setDataSource(context, videoUri)

                val videoLengthInUs =
                    (mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                        ?.toLong() ?: 0L) * 1000L

                val interval = videoLengthInUs / numThumbs

                for (i in 0 until numThumbs) {
                    if (!currentCoroutineContext().isActive) break

                    var bitmap: Bitmap? =
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) mediaMetadataRetriever.getScaledFrameAtTime(
                            i * interval,
                            MediaMetadataRetriever.OPTION_CLOSEST_SYNC,
                            thumbSize,
                            thumbSize
                        )
                        else {
                            mediaMetadataRetriever.getFrameAtTime(
                                i * interval,
                                MediaMetadataRetriever.OPTION_CLOSEST_SYNC
                            )
                        }

                    if (bitmap != null) bitmap = ThumbnailUtils.extractThumbnail(
                        bitmap,
                        thumbSize,
                        thumbSize
                    )
                    emit(bitmap)
                }
            }
        }
    }
}
fun Bitmap.rotate(degree: Float): Bitmap {
    val matrix = Matrix().apply { postRotate(degree) }
    return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
}