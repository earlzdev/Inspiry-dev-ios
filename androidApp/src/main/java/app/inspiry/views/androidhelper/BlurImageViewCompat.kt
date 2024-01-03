package app.inspiry.views.androidhelper

import android.content.Context
import android.graphics.Bitmap
import android.util.AttributeSet
import android.view.View
import androidx.annotation.WorkerThread
import app.inspiry.utils.ImageUtils
import java.util.concurrent.Executors

class BlurImageViewCompat : BaseBlurImageView {

    // Blur
    @Volatile
    private var blurRadius: Float = 0f
    private var sourceBitmap: Bitmap? = null
    private val blurExecutor by lazy { Executors.newSingleThreadExecutor() }


    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {
        setLayerType(View.LAYER_TYPE_HARDWARE, null)
    }

    override fun setImageBitmap(bm: Bitmap?) {
        sourceBitmap = bm

        if (isBlurDisabled()) super.setImageBitmap(bm)
        else setBlurImageBitmapAsync()
    }

    @WorkerThread
    private fun blurPrepare(): Bitmap? {
        return sourceBitmap?.let {
            ImageUtils.blurRenderScript(it, blurRadius)
        }
    }

    override fun setBlurRadius(radius: Float, async: Boolean) {
        if (sourceBitmap == null) return

        if (blurRadius != radius) {
            blurRadius = radius

            if (radius <= 1f) {
                if (async) post {
                    setImageBitmap(sourceBitmap)
                } else {
                    setImageBitmap(sourceBitmap)
                }
            } else {
                if (async) {
                    setBlurImageBitmapAsync()
                } else {

                    setBitmap(blurPrepare(), async = false)
                }
            }
        }
    }

    private fun setBitmap(bitmap: Bitmap?, async: Boolean) {
        bitmap?.run {
            if (async) post { super.setImageBitmap(this) }
            else super.setImageBitmap(this)
        }
    }

    private fun isBlurDisabled() = sourceBitmap == null || blurRadius == 0f

    private fun setBlurImageBitmapAsync() {
        val blurRadius = blurRadius

        blurExecutor.execute {
            if (blurRadius == this.blurRadius) {
                val bitmap = blurPrepare()
                if (blurRadius == this.blurRadius) {
                    setBitmap(bitmap ?: sourceBitmap, async = true)
                } else {
                    bitmap?.recycle()
                }
            }
        }
    }
}