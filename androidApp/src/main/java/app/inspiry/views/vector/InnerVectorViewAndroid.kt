package app.inspiry.views.vector

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.net.Uri
import android.view.MotionEvent
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import android.widget.Toast
import androidx.lifecycle.findViewTreeLifecycleOwner
import app.inspiry.core.media.ScaleType
import app.inspiry.core.util.ArgbColorManager
import app.inspiry.core.util.parseAssetsPath
import app.inspiry.helpers.K
import app.inspiry.media.toJava
import app.inspiry.palette.model.PaletteLinearGradient
import app.inspiry.export.viewmodel.getLottieFilesFolder
import app.inspiry.utils.TAG_TEMPLATE
import app.inspiry.video.SourceUtils
import app.inspiry.video.parseAssetsPathForAndroid
import app.inspiry.views.touch.MovableTouchHelperAndroid
import coil.ImageLoader
import coil.load
import com.airbnb.lottie.*
import com.airbnb.lottie.model.KeyPath
import com.airbnb.lottie.network.LottieFetchResult
import com.airbnb.lottie.value.LottieValueCallback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.IOException
import java.io.InputStream
import java.lang.IllegalStateException
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

class InnerVectorViewAndroid(context: Context, override val viewFps: Int) : FrameLayout(context),
    InnerVectorView, KoinComponent {

    val lottieView: LottieAnimationView = LottieAnimationView(context)

    lateinit var drawListener: (Canvas) -> Unit
    var movableTouchHelper: MovableTouchHelperAndroid? = null

    override var onInitialized: ((Float, Int) -> Unit)? = null
    override var onFailedToInitialize: ((Throwable?) -> Unit)? = null

    private var isAnimationEnabled = true

    private val imageLoader: ImageLoader by inject()

    override var lottieFrame: Int
        get() = lottieView.frame
        set(value) {
            if (isAnimationEnabled) lottieView.frame = value
        }


    init {
        setWillNotDraw(false)
        clipToOutline = true
        addView(lottieView, MATCH_PARENT, MATCH_PARENT)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        lottieView.removeLottieOnCompositionLoadedListener(onCompositionLoadListener)
    }

    override fun setScaleType(scaleType: ScaleType) {
        lottieView.scaleType = scaleType.toJava()
        if (scaleType == ScaleType.FIT_XY) {
            lottieView.minimumHeight = 0
            lottieView.minimumWidth = 0
        }
    }

    override fun draw(canvas: Canvas) {
        drawListener.invoke(canvas)
        super.draw(canvas)
    }

    override fun setColorKeyPath(color: Int, vararg key: String) {
        val filter = SimpleColorFilter(ArgbColorManager.colorWithoutAlpha(color))
        val keyPath = KeyPath(*key)
        val callback: LottieValueCallback<ColorFilter> = LottieValueCallback(filter)
        lottieView.addValueCallback(keyPath, LottieProperty.COLOR_FILTER, callback)
    }

    override fun setGradientKeyPath(gradient: PaletteLinearGradient, vararg key: String) {
        val keyPath = KeyPath(*key)
        val callback: LottieValueCallback<Array<Int>> =
            LottieValueCallback(gradient.colors.toTypedArray())
        lottieView.addValueCallback(keyPath, LottieProperty.GRADIENT_COLOR, callback)
    }

    override fun resetColorKeyPath(vararg key: String) {
        val keyPath = KeyPath(*key)
        val callback: LottieValueCallback<ColorFilter> = LottieValueCallback(null)
        lottieView.addValueCallback(keyPath, LottieProperty.COLOR_FILTER, callback)
    }

    override fun setColorFilter(color: Int?) {
        if (color == null) lottieView.colorFilter = null
        else lottieView.setColorFilter(color)
    }

    /**
     * @param originalSource        - json animation path
     * @param isLottieAnimEnabled   - enable animations (true) or show last frame (false)
     */
    override fun loadAnimation(
        originalSource: String,
        isLottieAnimEnabled: Boolean,
        removeBlur: Boolean
    ) {
        loadAnimation(originalSource, removeBlur)
        isAnimationEnabled = isLottieAnimEnabled
        if (!isLottieAnimEnabled) lottieView.progress = 1f //select last frame
    }

    override fun loadAnimation(originalSource: String, reduceBlur: Boolean) {
        lottieView.addLottieOnCompositionLoadedListener(onCompositionLoadListener)
        lottieView.setFailureListener {
            Toast.makeText(context, it.message, Toast.LENGTH_LONG).show()
            onFailedToInitialize?.invoke(it)
        }
        lottieView.setIgnoreDisabledSystemAnimations(true)
        lottieView.setCacheComposition(false)



        if (originalSource.isNotEmpty()) {
            if (reduceBlur) {
                val lottieFile = SourceUtils.readTextFromAssets(originalSource, context)
                    ?: throw IllegalStateException("lottie file not read $originalSource")
                lottieView.setAnimationFromJson(reduceBlur(lottieFile), null)
            } else lottieView.setAnimation(originalSource.parseAssetsPath())
        }
    }

    private fun reduceBlur(lottieJson: String): String {
        val gaussList = lottieJson.split(""""nm":"Blurriness"""")
        var gauss = false
        val res = gaussList.map {
            if (!gauss) {
                gauss = !gauss
                it
            } else {
                it.replaceFirst(regex = Regex(""""k":\d."""), """"k":0.2,""")
            }
        }.joinToString(""""nm":"Blurriness"""")
        return res
    }

    override fun loadSvg(originalSource: String) {

        lottieView.load(Uri.parse(originalSource.parseAssetsPathForAndroid()),
            imageLoader = imageLoader,
            builder = {
                crossfade(false).listener(onError = { request, error ->
                    onFailedToInitialize?.invoke(error.throwable)

                },
                    onSuccess = { request, metadata ->
                        onInitialized?.invoke(0f, 0)
                    })
                    .lifecycle(findViewTreeLifecycleOwner())
            })
    }

    override fun clearDisplayResource() {
        lottieView.setImageDrawable(null)
    }


    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        movableTouchHelper?.onTouchMovable(event)
        return movableTouchHelper != null
    }

    private val onCompositionLoadListener = LottieOnCompositionLoadedListener {

        onInitialized?.invoke(
            it.frameRate,
            (it.durationFrames * viewFps / it.frameRate).roundToInt()
        )

        K.d(TAG_TEMPLATE) {
            "LottieView:: onCompositionLoaded, listener ${onInitialized}" +
                    " durationFrames keyPaths ${lottieView.resolveKeyPath(KeyPath("**"))}"
        }
    }

    companion object {
        private val lottieHttp = OkHttpClient.Builder()
            .callTimeout(15, TimeUnit.SECONDS)
            .build()

        fun setUpLottie(context: Context) {
            Lottie.initialize(
                LottieConfig.Builder()
                    .setEnableSystraceMarkers(BuildConfig.DEBUG)
                    .setNetworkFetcher { url ->
                        val response = lottieHttp.newCall(
                            Request.Builder().url(url).get()
                                .build()
                        ).execute()
                        OkHttpLottieResult(response)
                    }
                    .setNetworkCacheDir(context.getLottieFilesFolder())
                    .build()
            )
        }
    }

    class OkHttpLottieResult(val response: Response) : LottieFetchResult {
        override fun close() {
            response.close()
        }

        override fun isSuccessful(): Boolean {
            return response.isSuccessful && response.body != null
        }

        override fun bodyByteStream(): InputStream {
            return response.body!!.byteStream()
        }

        override fun contentType(): String? {
            return response.body?.contentType()?.type
        }

        override fun error(): String? {
            return try {
                if (isSuccessful) null else """Unable to fetch ${response.request.url}. Failed with ${response.code}""".trimIndent()
            } catch (e: IOException) {
                null
            }
        }
    }
}