package app.inspiry.video.program

import android.content.Context
import android.graphics.Bitmap
import android.view.ViewGroup
import app.inspiry.helpers.K
import app.inspiry.core.media.ProgramCreator
import app.inspiry.core.opengl.TextureCreator
import app.inspiry.video.SourceUtils
import app.inspiry.video.gles.Program
import app.inspiry.video.gles.texture.TextureFactory
import app.inspiry.core.data.TransformMediaData
import java.lang.IllegalStateException

/**
 * Used to deferred program creation
 */
class DeferredProgramCreator(val programCreator: ProgramCreator) {

    private val textures: List<DeferredTextureCreator> = programCreator.textures.map {
        val deferredTexture = DeferredTextureCreator(it)

        val sourceUri = it.source
        when {
            sourceUri != null && it.type.isVideo() -> deferredTexture.setVideo(
                sourceUri,
                0L, 0f,
                0L,
                0L
            )
            sourceUri != null && it.type.isImage() -> deferredTexture.setImage(uri = sourceUri)
        }

        deferredTexture
    }

    val isReady: Boolean
        get() = textures.all { it.isInitialized }

    fun createProgramAndTextures(
        isRecording: Boolean,
        context: Context
    ): Pair<Program, TextureFactory> {
        var vertexShader = ""
        var fragmentShader = ""
        if (programCreator.hasShaderPath() && !programCreator.hasShaderGenerator()) {
            vertexShader = read(programCreator.vertexShader, context)
            fragmentShader =
                read(programCreator.fragmentShader, context).replaceTextureTypesIfNeed()
        } else {
            programCreator.shaderSource?.let {
                vertexShader = it.getVertexShader(read(it.getVertexShaderPath(), context))
                fragmentShader = it.getFragmentShader(read(it.getFragmentShaderPath(), context))
                    .replaceTextureTypesIfNeed()
            }
        }
        if (fragmentShader.isEmpty() || vertexShader.isEmpty()) throw IllegalStateException("Shader is empty! $programCreator}")
        val program =
            Program(
                vertexShader,
                fragmentShader,
                isRecording
            )
        val textureFactory = TextureFactory(program, isRecording)

        try {
            textures.forEach {
                it.createTexture(program, textureFactory)
            }
        } catch (e: Exception) {
            program.lastError = e
        }

        return program to textureFactory
    }


    private fun String.replaceTextureTypesIfNeed(): String {
        var fragmentShader: String = this
        textures.filter { it.isImage }
            .forEach {
                fragmentShader = replaceTextureImageType(fragmentShader, it.textureCreator.name)
            }
        return fragmentShader
    }

    private fun replaceTextureImageType(fragmentShader: String, name: Int) = fragmentShader
        .replace(
            String.format(VIDEO_TEXTURE_DECLARATION, name),
            String.format(IMAGE_TEXTURE_DECLARATION, name)
        )

    private fun read(source: String, context: Context) =
        SourceUtils.readTextFromAssets(source, context)!!

    private fun findTexture(index: Int): DeferredTextureCreator {
        K.d("json") {
            "findTexture ${index}, programTextures ${programCreator.textures}, deferredTextures ${textures}"
        }

        return textures.filter { it.isEditable }[index]
    }


    fun setImage(bitmap: Bitmap? = null, textureIndex: Int? = null, uri: String? = null) {
        findTexture(textureIndex ?: 0).setImage(bitmap, uri)
    }

    fun setVideo(
        videoUri: String, videoVolume: Float, videoTimeOffsetUs: Long = 0L, textureIndex: Int = 0,
        viewStartTimeUs: Long = 0, totalDurationUs: Long = 0
    ) {
        findTexture(textureIndex).setVideo(
            videoUri,
            videoTimeOffsetUs,
            videoVolume,
            viewStartTimeUs,
            totalDurationUs
        )
    }

    fun getTemplateTextures() = textures
        .filter { it.textureCreator.type == TextureCreator.Type.TEMPLATE }


    fun setTemplate(containerView: ViewGroup, textureName: Int) {
        getTemplateTextures().forEach {
            if (it.textureCreator.name == textureName) {
                it.setTemplate(containerView)
            }
        }
    }

    fun getEditableTextureCreator() =
        textures.firstOrNull { it.isTransformTexture }

    fun setTransform(transformMediaData: TransformMediaData) {
        getEditableTextureCreator()?.setTransform(transformMediaData)
    }


    companion object {
        private const val VIDEO_TEXTURE_DECLARATION = "uniform samplerExternalOES sTexture%d;"
        private const val IMAGE_TEXTURE_DECLARATION = "uniform sampler2D sTexture%d;"
    }
}