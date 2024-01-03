package app.inspiry.video.program.source

import android.view.ViewGroup
import app.inspiry.video.gles.Program
import app.inspiry.video.gles.texture.TextureFactory
import app.inspiry.media.TextureParams

class TemplateTextureSource(
    private val containerView: ViewGroup
) : TextureSource() {

    override fun create(
        program: Program,
        textureIndex: Int,
        params: TextureParams,
        textureFactory: TextureFactory
    ) {
        textureFactory.createTemplateTexture(
            program,
            textureIndex,
            containerView,
            params
        )
    }
}