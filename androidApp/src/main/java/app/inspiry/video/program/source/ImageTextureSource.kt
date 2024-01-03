package app.inspiry.video.program.source

import android.graphics.Bitmap
import android.net.Uri
import app.inspiry.ap
import app.inspiry.media.TextureParams
import app.inspiry.utils.ImageUtils
import app.inspiry.video.gles.Program
import app.inspiry.video.gles.texture.TextureFactory
import app.inspiry.video.parseAssetsPathForAndroid
import kotlinx.coroutines.runBlocking
import org.koin.core.context.GlobalContext

class ImageTextureSource(
    private val bitmap: Bitmap?,
    val uri: String?
) : TextureSource() {

    override fun create(
        program: Program,
        textureIndex: Int,
        params: TextureParams,
        textureFactory: TextureFactory
    ) {

        var loadedBitmap: Bitmap? = null

        if (bitmap != null) {
            loadedBitmap = bitmap
        } else {

            loadedBitmap = runBlocking {
                ImageUtils.loadBitmapSync(
                    Uri.parse(uri?.parseAssetsPathForAndroid()), GlobalContext.get().get(), ap
                )
            }
            if (loadedBitmap == null) throw IllegalStateException("can't load bitmap from url ${uri}")
        }

        textureFactory.createBitmapTexture(program, textureIndex, loadedBitmap, params)
    }
}