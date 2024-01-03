package app.inspiry.core.opengl

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * The class is used to change texture overlay
 */
@Serializable
sealed class TextureMatrixData {
    abstract val name: Int
}


/**
 * Save aspect-ratio image setting
 */
@Serializable
@SerialName("aspect-ratio")
class AspectRatioTextureMatrixData(override val name: Int) : TextureMatrixData()

/**
 * Clip texture from image
 */
@Serializable
@SerialName("clip")
class ClipTextureMatrixData : TextureMatrixData {

    override val name: Int
    val left: Float
    val top: Float
    val right: Float
    val bottom: Float

    constructor(name: Int, left: Float, top: Float, right: Float, bottom: Float) {
        this.name = name
        this.left = left
        this.top = top
        this.right = right
        this.bottom = bottom
        throwIfIncorrect()
    }

    constructor(name: Int, clipRegion: ClipRegion) {
        this.name = name
        this.left = clipRegion.left
        this.top = clipRegion.top
        this.right = clipRegion.right
        this.bottom = clipRegion.bottom
        throwIfIncorrect()
    }

    private fun throwIfIncorrect() {
        require(
            left <= right && bottom <= top && left.isCorrect() && top.isCorrect() &&
                    right.isCorrect() && bottom.isCorrect()
        )
    }

    private fun Float.isCorrect() = this in 0F..1F
}

@Serializable
@SerialName("clipRegion")
class ClipRegion(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float
)

/**
 * Transform texture from image
 */
@Serializable
@SerialName("transform")
class TransformTextureMatrixData(override val name: Int) : TextureMatrixData()