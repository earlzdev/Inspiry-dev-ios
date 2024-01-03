package app.inspiry.video.program.source

import app.inspiry.core.opengl.VideoPlayerParams
import app.inspiry.video.gles.Program
import app.inspiry.video.gles.texture.TextureFactory
import app.inspiry.media.TextureParams

class DecoderTextureSource(
    val uri: String,
    private val playerParams: VideoPlayerParams
) : TextureSource() {

    override fun create(
        program: Program,
        textureIndex: Int,
        params: TextureParams,
        textureFactory: TextureFactory
    ) {
        textureFactory.createDecoderTexture(program, textureIndex, uri, params, playerParams)
    }
}