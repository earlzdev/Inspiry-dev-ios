package app.inspiry.media

import android.graphics.RectF
import android.opengl.Matrix
import app.inspiry.core.opengl.AspectRatioTextureMatrixData
import app.inspiry.core.opengl.ClipTextureMatrixData
import app.inspiry.core.opengl.TextureMatrixData
import app.inspiry.core.opengl.TransformTextureMatrixData
import app.inspiry.helpers.K
import app.inspiry.video.gles.AttributeCore
import app.inspiry.core.data.TransformMediaData
import kotlinx.serialization.Transient
import kotlin.math.max


fun TextureMatrixData.createTextureMatrix(): TextureMatrix<*> {
    return when (this) {
        is AspectRatioTextureMatrixData -> AspectRatioTextureMatrix(this)
        is ClipTextureMatrixData -> ClipTextureMatrix(this)
        is TransformTextureMatrixData -> TransformTextureMatrix(this)
    }
}
/**
 * The class is used to change texture overlay
 */
sealed class TextureMatrix<Data: TextureMatrixData> {
    abstract val data: Data

    private val openGlName by lazy { getTextureMatrixName(data.name) }
    protected val matrix by lazy { FloatArray(16) }


    fun setUniformMat4(
        attributeCore: AttributeCore,
        viewWidth: Int,
        viewHeight: Int,
        textureWidth: Int,
        textureHeight: Int,
        textureRotation: Float
    ) {
        Matrix.setIdentityM(matrix, 0)
        transformMatrix(viewWidth, viewHeight, textureWidth, textureHeight, textureRotation)
        attributeCore.setUniformMat4(openGlName, matrix)
    }

    abstract fun transformMatrix(
        viewWidth: Int,
        viewHeight: Int,
        textureWidth: Int,
        textureHeight: Int,
        textureRotation: Float
    )

    protected fun scale(scaleWidth: Float, scaleHeight: Float) {
        Matrix.scaleM(matrix, 0, scaleWidth, scaleHeight, 1F)
    }

    protected fun translate(translateX: Float, translateY: Float) {
        Matrix.translateM(matrix, 0, translateX, translateY, 0F)
    }

    protected fun rotate(angle: Float) {
        Matrix.rotateM(matrix, 0, angle, 0f, 0f, -1f)
    }

    protected fun clip(left: Float, top: Float, right: Float, bottom: Float) {
        translate(left, (1 - top))
        scale(right - left, top - bottom)
    }

    private fun getTextureMatrixName(index: Int) =
        String.format(TEXTURE_MATRIX_VARIABLE_NAME, index)

    companion object {
        private const val TEXTURE_MATRIX_VARIABLE_NAME = "uTextureMatrix%d"
    }
}


class AspectRatioTextureMatrix(override val data: AspectRatioTextureMatrixData) : TextureMatrix<AspectRatioTextureMatrixData>() {

    override fun transformMatrix(
        viewWidth: Int,
        viewHeight: Int,
        textureWidth: Int,
        textureHeight: Int,
        textureRotation: Float
    ) {
        val textureAspectRatio = textureHeight / textureWidth.toFloat()
        val viewAspectRatio = viewHeight / viewWidth.toFloat()
        var scaleWidth = 1F
        var scaleHeight = 1F
        if (textureAspectRatio > viewAspectRatio) {
            scaleHeight = viewAspectRatio / textureAspectRatio
        } else {
            scaleWidth = textureAspectRatio / viewAspectRatio
        }
        val translateX = (1 - scaleWidth) / 2
        val translateY = (1 - scaleHeight) / 2
        K.v(K.TAG_TEXTURE_MATRIX) {
            "viewWidth = $viewWidth" +
                    ", viewHeight = $viewHeight" +
                    ", textureWidth = $textureWidth" +
                    ", textureHeight = $textureHeight" +
                    ", translateX = $translateX" +
                    ", translateY = $translateX" +
                    ", scaleWidth = $scaleWidth" +
                    ", scaleHeight = $scaleHeight" +
                    ", textureAspectRatio = $textureAspectRatio" +
                    ", viewAspectRatio = $viewAspectRatio"
        }
        translate(translateX, translateY)
        scale(scaleWidth, scaleHeight)
    }
}

class ClipTextureMatrix(
    override val data: ClipTextureMatrixData
) : TextureMatrix<ClipTextureMatrixData>() {

    override fun transformMatrix(viewWidth: Int, viewHeight: Int, textureWidth: Int, textureHeight: Int,
                                 textureRotation: Float) {
        clip(data.left, data.top, data.right, data.bottom)
    }
}

class TransformTextureMatrix(override val data: TransformTextureMatrixData) : TextureMatrix<TransformTextureMatrixData>() {

    @Transient
    private var textureTransformData: TransformMediaData? = null

    fun setTransform(textureTransformData: TransformMediaData) {
        this.textureTransformData = textureTransformData
    }

    override fun transformMatrix(viewWidth: Int, viewHeight: Int, textureWidth: Int, textureHeight: Int,
                                 textureRotation: Float) {
        if (textureTransformData == null) return

        var baseScaleFactor = 1F
        var baseTranslateX = 0F
        var baseTranslateY = 0F

        fun setupCenterCrop() {
            val scaleFactorWidth = viewWidth / textureWidth.toFloat()
            val scaleFactorHeight = viewHeight / textureHeight.toFloat()
            baseScaleFactor = max(scaleFactorWidth, scaleFactorHeight)
            if (scaleFactorHeight > scaleFactorWidth) {
                baseTranslateX = (viewWidth - textureWidth * baseScaleFactor) / 2F
            } else baseTranslateY = (viewHeight - textureHeight * baseScaleFactor) / 2F
        }

        fun TransformMediaData.getTextureBounds(): RectF {
            val scaleFactor = scale * baseScaleFactor
            val translateX =
                (translateX * viewWidth + baseTranslateX) / (textureWidth * scaleFactor)
            val translateY =
                (translateY * viewHeight + baseTranslateY) / (textureHeight * scaleFactor)
            return RectF(-translateX, 1F + translateY, viewWidth / (textureWidth * scaleFactor) - translateX,
                1F - viewHeight / (textureHeight * scaleFactor) + translateY)
        }

        setupCenterCrop()

        textureTransformData?.run {
            val bounds = getTextureBounds()

            scale(bounds.right - bounds.left, bounds.top - bounds.bottom)

            val rotation = (rotate+textureRotation)%360
            val cx = 0.5f/(bounds.right-bounds.left)
            val cy = 0.5f/(bounds.top-bounds.bottom)

            translate(cx,cy)
                val outputAspect = viewWidth / viewHeight.toFloat()

                scale(1 / outputAspect, 1f)
                rotate(rotation)
                scale(outputAspect, 1f)

            translate(bounds.left / (bounds.right - bounds.left)-cx, (1 - bounds.top) / (bounds.top - bounds.bottom)-cy)
        }
    }
}