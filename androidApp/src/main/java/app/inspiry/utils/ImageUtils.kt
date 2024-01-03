package app.inspiry.utils

import android.content.Context
import android.content.pm.ActivityInfo
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Build
import android.renderscript.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.scale
import app.inspiry.ap
import app.inspiry.core.util.ImageUtil
import app.inspiry.edit.instruments.PickImageConfig
import app.inspiry.edit.instruments.PickedMediaType
import app.inspiry.helpers.K
import coil.ImageLoader
import coil.request.ImageRequest
import com.zhihu.matisse.Matisse
import com.zhihu.matisse.MimeType
import com.zhihu.matisse.engine.impl.CoilEngine
import com.zhihu.matisse.internal.entity.CaptureStrategy
import dev.icerock.moko.permissions.*
import org.koin.android.ext.android.get
import kotlin.math.roundToInt

object ImageUtils {

    private val renderScript: RenderScript by lazy { RenderScript.create(ap) }

    suspend fun loadBitmapSync(
        uri: Uri,
        imageLoader: ImageLoader,
        context: Context,
        modify: ImageRequest.Builder.() -> ImageRequest.Builder = { this }
    ): Bitmap? {

        val request = ImageRequest.Builder(context)
            .modify()
            .allowHardware(false)
            .crossfade(false)
            .data(uri).build()

        return (imageLoader.execute(request).drawable as? BitmapDrawable)?.bitmap
    }


    /**
     * @return true - is success
     */
    fun blurRenderScript(bitmap: Bitmap, radius: Float, scaleFactor: Float = 1f): Bitmap? {
        if (radius !in 0F..25F) return null

        if (radius == 0F) return null

        val originalWidth = bitmap.width
        val originalHeight = bitmap.height
        val inputBitmap = if (scaleFactor == 1f) bitmap.copy(
            bitmap.config.copyConfigDisallowHardware(),
            true
        ) else
            Bitmap.createScaledBitmap(
                bitmap, (originalWidth / scaleFactor).roundToInt(),
                (originalHeight / scaleFactor).roundToInt(), false
            )

        var input: Allocation? = null
        var output: Allocation? = null
        var script: Script? = null
        try {
            input = Allocation.createFromBitmap(
                renderScript, inputBitmap, Allocation.MipmapControl.MIPMAP_NONE,
                Allocation.USAGE_SCRIPT
            )
            output = Allocation.createTyped(renderScript, input.type)
            script = ScriptIntrinsicBlur.create(renderScript, Element.U8_4(renderScript))
                .apply {
                    setRadius(radius)
                    setInput(input)
                }
            script.forEach(output)
            output.copyTo(inputBitmap)

            if (scaleFactor != 1f) {
                val res = inputBitmap.scale(originalWidth, originalHeight, false)
                inputBitmap.recycle()
                return res
            } else {
                return inputBitmap
            }

        } catch (ex: Exception) {
            K.e(ex) { "Error during blur or bitmap" }
            return null
        } finally {
            input?.destroy()
            output?.destroy()
            script?.destroy()
        }
    }

    fun generatePickExactColorDrawable(): Drawable {
        val d = GradientDrawable()
        d.gradientType = GradientDrawable.SWEEP_GRADIENT
        d.colors = ImageUtil.sweepGradientIcon().toIntArray()
        d.shape = GradientDrawable.OVAL
        return d
    }


    private fun Bitmap.Config.copyConfigDisallowHardware(): Bitmap.Config {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (this == Bitmap.Config.HARDWARE) Bitmap.Config.ARGB_8888 else this
        } else {
            this
        }
    }

    fun getPermissionController(context: AppCompatActivity): PermissionsController {
        val pc = PermissionsController(applicationContext = context.applicationContext)
        pc.bind(
            context.lifecycle,
            context.supportFragmentManager
        )
        return pc
    }

    /**
     * Configuring media chooser, request permissions
     * @param config default = PickImageConfig(maxSelectable = 1, imageOnly = false, pickedMediaType = PickedMediaType.MEDIA, resultViewIndex = -1)
     * @return true if permissions are granted
     */

    suspend fun isMediaChooserPrepared(
        context: AppCompatActivity, config: PickImageConfig = PickImageConfig()
    ): Boolean {

        var mime = MimeType.ofAll()

        if (config.imageOnly) {
            mime = MimeType.ofImage() //media without video
        }

        Matisse
            .from(context)
            .choose(mime, false)
            .countable(false)
            .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
            .thumbnailScale(0.85f)
            .imageEngine(CoilEngine(context.get()))
            .capture(true)
            .captureStrategy(
                CaptureStrategy(
                    true,
                    "${context.packageName}.helpers.GenericFileProvider.all"
                )
            )
            .showPreview(false)
            .showSingleMediaType(config.imageOnly)
            .theme(com.zhihu.matisse.R.style.Matisse_White)
            .showPreview(false)
            .maxSelectable(config.maxSelectable)

        val permissionsController = getPermissionController(context)

        if (!permissionsController.isPermissionGranted(Permission.WRITE_STORAGE)) {
            try {
                permissionsController.providePermission(Permission.WRITE_STORAGE)
                return true

            } catch (deniedAlways: DeniedAlwaysException) {
                // Permission is always denied.
            } catch (denied: DeniedException) {
                // Permission was denied.
            } catch (e: RequestCanceledException) {

            }
        } else {
            return true
        }
        return false
    }

}
