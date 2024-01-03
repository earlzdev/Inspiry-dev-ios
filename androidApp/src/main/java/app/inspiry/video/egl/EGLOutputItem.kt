package app.inspiry.video.egl

import android.graphics.Bitmap
import android.graphics.SurfaceTexture
import android.opengl.GLES20
import android.view.Surface
import android.view.TextureView
import android.view.View
import androidx.core.view.isVisible
import app.inspiry.helpers.K
import app.inspiry.utils.printDebug
import app.inspiry.video.CustomTextureView
import app.inspiry.video.grafika.EglCore
import app.inspiry.video.grafika.WindowSurface
import app.inspiry.video.renderThread
import java.io.File
import java.lang.ref.WeakReference

/**
 * Used to prepare and render on textureView
 */
class EGLOutputItem(
    private val eglCore: EglCore,
    textureView: CustomTextureView,
    val textureViewId: Long,
    private var lifecycleListener: LifecycleListener? = null
) : TextureView.SurfaceTextureListener, CustomTextureView.VisibilityListener {

    private var windowSurface: WindowSurface? = null
    private var textureViewRef = WeakReference(textureView)
    var lastError: Exception? = null
        private set

    private val isTextureAvailable: Boolean
        get() = textureViewRef.get()?.isAvailable == true


    val onLayoutChangeListener =
        View.OnLayoutChangeListener { v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
            lifecycleListener?.onSurfaceSizeChanged(textureViewId, v.width, v.height)
        }

    init {
        debug { "created isAvailable ${textureView.isAvailable} " +
                "isVisible ${textureView.isVisible}, lastVisible ${textureView.lastVisibilityVisible()}," +
                " isAttached ${textureView.isAttachedToWindow} parent ${textureView.parent != null}, width ${textureView.width}" }

        textureView.surfaceTextureListener = this
        textureView.addOnLayoutChangeListener(onLayoutChangeListener)
    }


    fun checkSurfaceTextureAvailable() {
        val textureView = textureViewRef.get() ?: return
        if (textureView.isAvailable) {
            onSurfaceTextureAvailable(
                textureView.surfaceTexture!!,
                textureView.width,
                textureView.height
            )
        }
    }

    fun saveAsFile(file: File, format: Bitmap.CompressFormat) {
        if (lastError != null || windowSurface == null) {
            throw IllegalArgumentException()
        }
        windowSurface?.saveFrame(file, format)
    }

    fun checkSize() = getTextureViewOrDestroy()?.checkSize()

    fun draw(drawProgram: (width: Int, height: Int) -> Unit) {

        if (lastError != null) return
        windowSurface?.run {
            val textureView = getTextureViewOrDestroy() ?: return
            if (!textureView.isShown || !textureView.isAvailable || !textureView.checkSize()) return

            makeCurrent()
            GLES20.glViewport(0, 0, width, height)
            drawProgram(width, height)
            swapBuffers()
            lifecycleListener?.onDrawFrame(textureViewId)

            // K.d(K.TAG_GLES_PROGRAM) { "drawProgram width ${width}, height ${height} hasError ${lastError}" }
        }
    }

    private fun TextureView.checkSize(): Boolean {
        if (width <= 1 || height <= 1) {
            post {
                if (width <= 1 || height <= 1) {
                    requestLayout()
                }
            }
            return false
        }
        return true
    }

    private fun getTextureViewOrDestroy() =
        textureViewRef.get() ?: kotlin.run {
            renderThread { onTextureViewDestroy() }
            null
        }

    private fun onTextureViewDestroy() {
        debug { "onTextureViewDestroy" }
        textureViewRef.get()?.removeOnLayoutChangeListener(onLayoutChangeListener)
        onRelease()
        lifecycleListener?.run {
            lifecycleListener = null
            onTextureViewDestroyed(textureViewId)
        }
    }

    fun removeNotUsedTextureView() {
        getTextureViewOrDestroy() // Used to destroy if need
    }

    fun removeTextureView() {
        textureViewRef.clear()
        renderThread { onTextureViewDestroy() }
    }

    private fun onRelease() {
        if (windowSurface == null) return
        debug { "onRelease" }
        windowSurface?.run {
            release()
            windowSurface = null
        }
        lifecycleListener?.onSurfaceReleased(textureViewId)
    }

    override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {

        debug { "onSurfaceTextureAvailableOuter, isTextureAvailable ${textureViewRef.get()?.isAvailable}" }

        renderThread {
            if (isTextureAvailable) {
                val textureView = getTextureViewOrDestroy() ?: return@renderThread
                textureView.registerLifecycleListener(this)
                debug { "onSurfaceTextureAvailable" }
                onCreate(surface)
            }
        }
    }

    private fun onCreate(surface: SurfaceTexture) {
        if (windowSurface != null || lifecycleListener == null) return
        try {
            windowSurface = WindowSurface(eglCore, Surface(surface), true)
        } catch (ex: Exception) {
            ex.printDebug()
            lastError = ex
        } finally {
            lifecycleListener?.onSurfaceCreated(textureViewId)
        }
    }

    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
        renderThread {
            val textureView = getTextureViewOrDestroy() ?: return@renderThread
            textureView.unregisterLifecycleListener()
            debug { "onSurfaceTextureDestroyed" }
            onTextureViewDestroy()
        }
        return true
    }

    override fun onViewVisible() {
        renderThread {
            if (isTextureAvailable) {
                debug { "onViewVisible" }
                lifecycleListener?.onSurfaceCreated(textureViewId)
            }
        }
    }

    override fun onViewInvisible() {
        renderThread {
            debug { "onViewInvisible" }
            lifecycleListener?.onSurfaceReleased(textureViewId)
        }
    }

    fun makeCurrent(): Boolean {
        windowSurface?.run {
            makeCurrent()
            return true
        }
        return false
    }

    override fun onSurfaceTextureSizeChanged(
        surface: SurfaceTexture,
        width: Int,
        height: Int
    ) {
        debug { "onSurfaceTextureSizeChanged: $width, $height" }
        //called in layoutListener
    }

    override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {
    }

    interface LifecycleListener {
        fun onSurfaceCreated(textureViewId: Long)
        fun onSurfaceReleased(textureViewId: Long)
        fun onTextureViewDestroyed(textureViewId: Long)
        fun onDrawFrame(textureViewId: Long)
        fun onSurfaceSizeChanged(textureViewId: Long, width: Int, height: Int)
    }

    private inline fun verbose(msg: () -> String) {
        K.v(K.TAG_GLES_OUTPUT_ITEM) { "$textureViewId ${msg()}" }
    }

    private inline fun debug(msg: () -> String) {
        K.d(K.TAG_GLES_OUTPUT_ITEM) { "$textureViewId ${msg()}" }
    }

}