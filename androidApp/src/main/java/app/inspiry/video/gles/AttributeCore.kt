package app.inspiry.video.gles

import android.opengl.GLES20
import app.inspiry.video.grafika.GlUtil
import java.nio.Buffer

/**
 * Used to set program parameters
 */
class AttributeCore(private val programId: Int) {

    private val attributeSet = mutableListOf<Int>()

    fun setParameters() {
        setAttribute(POSITION_ATTRIBUTE, DRAWING_AREA_BUF)
        setAttribute(TEXTURE_COORD_ATTRIBUTE, TEXTURE_AREA_BUF)
        setUniformMat4(MVP_MATRIX_UNIFORM, GlUtil.IDENTITY_MATRIX)
    }

    private fun setAttribute(name: String, value: Buffer): Int {
        val location = GLES20.glGetAttribLocation(programId, name)
        GlUtil.checkGlError("glGetAttribLocation")
        GLES20.glVertexAttribPointer(location, 2, GLES20.GL_FLOAT,
                false, 8, value)
        GlUtil.checkGlError("glVertexAttribPointer")
        attributeSet.add(location)
        return location
    }

    fun setUniformMat4(name: String, value: FloatArray) {
        GLES20.glUniformMatrix4fv(name.uniformLocation(), 1, false, value, 0)
        GlUtil.checkGlError("glUniformMatrix4fv")
    }

    private fun String.uniformLocation(): Int {
        val location = GLES20.glGetUniformLocation(programId, this)
        GlUtil.checkGlError("glGetUniformLocation")
        return location
    }

    fun enableAttributes() {
        attributeSet.forEach {
            GLES20.glEnableVertexAttribArray(it)
            GlUtil.checkGlError("glEnableVertexAttribArray")
        }
    }

    fun disableAttributes() {
        attributeSet.forEach {
            GLES20.glDisableVertexAttribArray(it)
            GlUtil.checkGlError("glDisableVertexAttribArray")
        }
    }

    fun setUniform2f(name: String, x: Float, y: Float) {
        GLES20.glUniform2f(name.uniformLocation(), x, y)
        GlUtil.checkGlError("glUniform2f")
    }

    companion object {

        private const val MVP_MATRIX_UNIFORM = "uMVPMatrix"
        private const val POSITION_ATTRIBUTE = "aPosition"
        private const val TEXTURE_COORD_ATTRIBUTE = "aTextureCoord"

        private val DRAWING_AREA = floatArrayOf(
                -1f, -1f,
                1f, -1f,
                -1f, 1f,
                1f, 1f
        )
        private val TEXTURE_AREA = floatArrayOf(
                0.0f, 1.0f,
                1.0f, 1.0f,
                0.0f, 0.0f,
                1.0f, 0.0f
        )
        private val DRAWING_AREA_BUF = GlUtil.createFloatBuffer(DRAWING_AREA)
        private val TEXTURE_AREA_BUF = GlUtil.createFloatBuffer(TEXTURE_AREA)
    }
}