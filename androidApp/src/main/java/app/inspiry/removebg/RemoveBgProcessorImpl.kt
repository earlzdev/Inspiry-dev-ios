package app.inspiry.removebg

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.core.graphics.alpha
import app.inspiry.core.data.Size
import app.inspiry.helpers.K
import app.inspiry.helpers.NativeKeys
import app.inspiry.utils.BitmapUtils
import app.inspiry.utils.ImageUtils
import coil.ImageLoader
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.http.Headers
import io.ktor.http.content.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class RemoveBgProcessorImpl(
    private val httpClient: HttpClient,
    private val context: Context, private val imageLoader: ImageLoader
) : RemoveBgProcessor {

    override suspend fun removeBg(
        originalFile: String,
        saveToFile: String
    ): Size {

        val format = determineFormat(originalFile)

        val input = loadOriginalFileToByteArray(originalFile, format)

        val fileRequestBody =
            input.toRequestBody(contentType = "image/${format.name}".toMediaType())

        val requestBody: RequestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("image_file", "image.${format.name}", fileRequestBody)
            .build()

        val request: Request = Request.Builder()
            .header("x-api-key", NativeKeys().stringFromJNI1())
            //format of returned image. We need always png.
            .header("format", "png")
            .url(RemoveBgProcessor.ENDPOINT)
            .post(requestBody)
            .build()

        val response: Response = OkHttpClient().newCall(request).execute()

        if (response.isSuccessful) {
            val bitmap = response.body?.byteStream().use { inputStream ->
                BitmapFactory.decodeStream(inputStream)
            }
            val trimmedBitmap = K.debugTime(
                "RemoveBgProcessor",
                { "%d takes to trim bitmap of ${bitmap.width}, ${bitmap.height}" }) {
                bitmap.trim()
            }

            if (trimmedBitmap != bitmap)
                bitmap.recycle()

            val output = File(saveToFile)
            output.outputStream().use { fileStream ->
                trimmedBitmap.compress(Bitmap.CompressFormat.PNG, 100, fileStream)
            }
            val size = Size(trimmedBitmap.width, trimmedBitmap.height)
            trimmedBitmap.recycle()
            return size
        } else {
            throw IllegalStateException("response is unsuccessful")
        }
    }

    override fun getSizeOfExistingFile(file: String): Size {
        val f = File(file)

        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(file, options)

        K.i("getSizeOfExistingFile") {
            "file ${file}, exists ${f.exists()}, ${options.outWidth}"
        }
        return Size(options.outWidth, options.outHeight)
    }

    /*
    Unfortunately this version doesn't work.
    The downloaded file is not in valid format for unknown reason.
    I believe it is a trouble with ktor. Too bad, because it could be multiplatform.
     */
    suspend fun removeBgKtor(
        originalFile: String,
        saveToFile: String
    ) {

        val format = determineFormat(originalFile)
        val input = loadOriginalFileToByteArray(originalFile, format)

        val parts: List<PartData> = formData {

            append("image_file", input, Headers.build {
                append(HttpHeaders.ContentType, "image/${format.name}")
                append(HttpHeaders.ContentDisposition, "filename=image.${format.name}")
            })
        }

        val response: HttpResponse =
            httpClient.submitFormWithBinaryData(RemoveBgProcessor.ENDPOINT, parts) {
                header("format", "png")
                header("x-api-key", NativeKeys().stringFromJNI1())
            }

        val output = File(saveToFile)
        val byteArrayBody: ByteArray = response.receive()
        output.writeBytes(byteArrayBody)
    }

    /**
     * Trims a bitmap borders of a given color.
     * TODO: implement it in c++ or even better with c++ renderScript like in
     * https://github.com/android/renderscript-intrinsics-replacement-toolkit
     * Actually current speed is not bad, since we cut small part of the image.
     * On samsung galaxy s9+ it is around 30-400ms
     *
     * play with alphaThreshold, check if it doesn't cut parts of the image.
     */
    private fun Bitmap.trim(alphaThreshold: Int = 30): Bitmap {

        var top = height
        var bottom = 0
        var right = width
        var left = 0

        var buffer = IntArray(width)

        fun checkBuffer(buffer: IntArray): Boolean {
            for (i in buffer) {
                if (i.alpha > alphaThreshold) {
                    return false
                }
            }
            return true
        }

        for (y in bottom until top) {
            getPixels(buffer, 0, width, 0, y, width, 1)
            if (!checkBuffer(buffer)) {
                bottom = y
                break
            }
        }

        for (y in top - 1 downTo bottom) {
            getPixels(buffer, 0, width, 0, y, width, 1)
            if (!checkBuffer(buffer)) {
                top = y
                break
            }
        }

        val heightRemaining = top - bottom
        buffer = IntArray(heightRemaining)

        for (x in left until right) {
            getPixels(buffer, 0, 1, x, bottom, 1, heightRemaining)
            if (!checkBuffer(buffer)) {
                left = x
                break
            }
        }

        for (x in right - 1 downTo left) {
            getPixels(buffer, 0, 1, x, bottom, 1, heightRemaining)
            if (!checkBuffer(buffer)) {
                right = x
                break
            }
        }
        return Bitmap.createBitmap(this, left, bottom, right - left, top - bottom)
    }

    private suspend fun loadOriginalFileToByteArray(
        originalFile: String,
        format: RemoveBgProcessor.Format
    ): ByteArray {
        val bitmap: Bitmap?
        try {
            bitmap = ImageUtils.loadBitmapSync(Uri.parse(originalFile), imageLoader, context) {
                size(RemoveBgProcessor.MAX_SIZE, RemoveBgProcessor.MAX_SIZE)
            }

            return BitmapUtils.toByteArray(
                bitmap!!, 100, if (format == RemoveBgProcessor.Format.png)
                    Bitmap.CompressFormat.PNG else Bitmap.CompressFormat.JPEG
            )
        } finally {
            // we don't recycle bitmap, because glide can cache it.
            //bitmap?.recycle()
        }
    }


    override fun determineFormat(originalFile: String): RemoveBgProcessor.Format {
        val uri = Uri.parse(originalFile)

        val extension: String?
        //Check uri format to avoid null
        if (uri.scheme.equals(ContentResolver.SCHEME_CONTENT)) {
            //If scheme is a content
            val mime = MimeTypeMap.getSingleton()
            extension = mime.getExtensionFromMimeType(context.contentResolver.getType(uri))
        } else {
            //If scheme is a File
            //This will replace white spaces with %20 and also other special characters. This will avoid returning null values on file name with spaces and special characters.
            extension = MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(File(uri.path)).toString())
        }

        return if (extension.equals(
                "png",
                ignoreCase = true
            )
        ) RemoveBgProcessor.Format.png else RemoveBgProcessor.Format.jpg
    }
}