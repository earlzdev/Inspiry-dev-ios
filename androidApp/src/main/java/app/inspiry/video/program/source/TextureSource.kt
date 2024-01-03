package app.inspiry.video.program.source

import app.inspiry.video.gles.Program
import app.inspiry.video.gles.texture.TextureFactory
import app.inspiry.media.TextureParams

abstract class TextureSource {

    /**
     * @return true - if texture has been successfully created
     */
    @Throws
    abstract fun create(
        program: Program,
        textureIndex: Int,
        params: TextureParams,
        textureFactory: TextureFactory
    )
}