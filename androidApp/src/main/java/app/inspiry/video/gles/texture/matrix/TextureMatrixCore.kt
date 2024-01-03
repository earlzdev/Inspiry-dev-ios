package app.inspiry.video.gles.texture.matrix

import app.inspiry.core.data.TransformMediaData
import app.inspiry.media.TextureMatrix
import app.inspiry.media.TransformTextureMatrix
import app.inspiry.video.gles.AttributeCore

fun List<TextureMatrix<*>>.hasTransformMatrix() = any { it is TransformTextureMatrix }

fun List<TextureMatrix<*>>.setTransform(textureTransformData: TransformMediaData) {
    val textureMatrix = filterIsInstance<TransformTextureMatrix>()
    require(textureMatrix.size == 1) { "Transform operation unavailable" }
    textureMatrix[0].setTransform(textureTransformData)
}

fun List<TextureMatrix<*>>.setUniformMat4(
    attributeCore: AttributeCore,
    viewWidth: Int,
    viewHeight: Int,
    textureWidth: Int,
    textureHeight: Int,
    textureRotation: Float
) {
    forEach {
        it.setUniformMat4(attributeCore, viewWidth, viewHeight, textureWidth, textureHeight, textureRotation)
    }
}