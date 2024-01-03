package app.inspiry.views.timeline

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.media.ThumbnailUtils
import android.net.Uri
import android.view.View
import android.view.ViewOutlineProvider
import androidx.core.graphics.withTranslation
import androidx.lifecycle.findViewTreeLifecycleOwner
import app.inspiry.R
import app.inspiry.helpers.K
import coil.ImageLoader
import coil.request.ImageRequest
import coil.size.Scale
import kotlinx.coroutines.*
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.ceil

class SeriesOfImagesView(context: Context) : View(context), KoinComponent {

    @Suppress("LeakingThis")
    private var bitmapList: MutableList<Bitmap?> = mutableListOf()
    private var bounds = Rect()
    private val uiScope = CoroutineScope(Dispatchers.Main)
    private var loadBitmapsJob: Job? = null
    private val imageLoader: ImageLoader by inject()

    var roundedCorners = 0f
        set(value) {
            field = value

            this.outlineProvider = object : ViewOutlineProvider() {
                override fun getOutline(var1: View, outline: Outline) {
                    outline.setRoundRect(bounds, value)
                }
            }
            clipToOutline = true
            invalidateOutline()
        }

    init {
        setBackgroundColor(0xffC4C4C4.toInt())
    }

    override fun onSizeChanged(w: Int, h: Int, oldW: Int, oldH: Int) {
        super.onSizeChanged(w, h, oldW, oldH)
        bounds.set(0, 0, width, height)
        invalidateOutline()


        val numThumbs = ceil((w.toDouble() / h)).toInt()

        if (w != oldW && numThumbs != bitmapList.size) getBitmaps(w, h, numThumbs)
    }

    fun clearBitmaps() {
        bitmapList.clear()
    }

    private fun getUrisToLoad(numThums: Int): MutableList<String> {
        val uris = selectParentUntil<TimelineView>()!!.templateView.mediaViews.mapNotNull {

            if (it.media.duplicate == null && it.media.isEditable)
                it.media.originalSource ?: it.media.demoSource
            else null
        }

        return if (numThums == uris.size) uris.toMutableList()
        else if (numThums < uris.size) uris.subList(0, numThums).toMutableList()
        else if (uris.isEmpty()) Collections.emptyList()
        else {
            val final = ArrayList<String>()
            duplicateUris(uris, final, numThums)
            final
        }
    }

    private fun duplicateUris(from: List<String>, to: MutableList<String>, numThums: Int) {

        if (from.isEmpty()) return

        while (true) {
            for (it in from) {
                to.add(it)
                if (to.size >= numThums) return
            }
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        clearRequests()
    }

    private fun clearRequests() {
        loadBitmapsJob?.cancel()
    }

    private fun getBitmaps(viewWidth: Int, viewHeight: Int, numThumbs: Int) {

        if (isInEditMode) {
            bitmapList.clear()
            val bitmap = ThumbnailUtils.extractThumbnail(
                BitmapFactory.decodeResource(
                    resources,
                    R.mipmap.ic_launcher
                )!!, viewHeight, viewHeight
            )
            for (i in 0 until numThumbs) bitmapList.add(bitmap)
            invalidate()
            return
        }

        clearRequests()

        if (numThumbs < bitmapList.size) {
            bitmapList = bitmapList.subList(0, numThumbs)
            invalidate()
            return
        }

        var urisToLoad = getUrisToLoad(numThumbs)

        if (bitmapList.size != 0) {
            urisToLoad = urisToLoad.subList(bitmapList.size, numThumbs)
        }

        /*K.i("timeline") {
            "urisToLoad ${urisToLoad}"
        }*/

        var job: Job? = null
        job = uiScope.launch() {

            for (it in urisToLoad) {

                val drawable =
                    withContext(Dispatchers.IO) {
                        try {
                            imageLoader.execute(
                                ImageRequest.Builder(context)
                                    .size(viewHeight, viewHeight)
                                    .data(Uri.parse(it))
                                    .lifecycle(findViewTreeLifecycleOwner())
                                    //.videoFrameMillis(positionInMillis * 1000)
                                    .scale(Scale.FILL)
                                    .build()
                            ).drawable
                        } catch (e: Exception) {
                            null
                        }
                    }

                K.d("SeriesOfImagesView") {
                    "imageLoaded ${drawable} isActive ${isActive}"
                }

                if (isActive) {
                    bitmapList.add((drawable as? BitmapDrawable?)?.bitmap)
                    invalidate()
                }
            }
        }
        this.loadBitmapsJob = job
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (parent != null) {

            var x = 0f
            val thumbSize = height
            for (bitmap in bitmapList) {
                if (bitmap != null) {
                    canvas.drawBitmap(bitmap, x, 0f, null)
                }
                x += thumbSize
            }
        }
    }
}