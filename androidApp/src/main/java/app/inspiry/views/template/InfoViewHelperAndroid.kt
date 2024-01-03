package app.inspiry.views.template

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ProgressBar
import android.widget.TextView
import app.inspiry.BuildConfig
import app.inspiry.R
import app.inspiry.core.data.InspResponseError
import app.inspiry.core.data.InspResponseLoading
import app.inspiry.core.media.Alignment
import app.inspiry.core.media.LayoutPosition
import app.inspiry.helpers.K
import app.inspiry.utils.printDebug
import app.inspiry.views.androidhelper.createLayoutParams
import app.inspiry.views.infoview.InfoViewColors
import app.inspiry.views.infoview.InfoViewModel
import app.inspiry.views.infoview.InfoViewModelImpl
import dev.icerock.moko.graphics.colorInt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class InfoViewHelperAndroid(private val templateView: InspTemplateView,
                            private val templateAndroidView: ViewGroup,
                            private val infoViewModel: InfoViewModel,
                            private val colors: InfoViewColors,
                            private val displayText: Boolean) {

    private var infoView: View? = null

    fun collectChanges(viewScope: CoroutineScope) {
        viewScope.launch {
            infoViewModel.state.collect {
                K.i("infoView") {
                    "collect ${it}"
                }
                when (it) {
                    is InspResponseError -> showErrorAndButtonRetry(it.throwable)
                    is InspResponseLoading -> if (it.progress == InfoViewModelImpl.VALUE_IMAGES) showImagesLoading()
                    else showTemplateLoading()
                    else -> removeInfoView()
                }
            }
        }
    }

    private fun showTemplateLoading() {

        if (BuildConfig.DEBUG && templateView.isInitialized.value)
            throw IllegalStateException()

        val root = addInfoView(R.layout.info_template_progress)
        root.findViewById<TextView>(R.id.button).visibility =
            View.GONE
    }

    private fun showImagesLoading() {
        if (BuildConfig.DEBUG && templateView.childrenToInitialize.isEmpty())
            throw IllegalStateException()

        addInfoView(R.layout.info_template_progress).let {
            it.findViewById<TextView>(R.id.button)
                .setOnClickListener {
                    templateView.interruptImagesLoading()
                }
        }
    }

    private fun removeInfoView() {

        //post to fix crash on xiaomi devices
        if (infoView != null) {
            try {

                templateAndroidView.removeView(infoView!!)
            } catch (t: Throwable) {

                infoView?.post {
                    infoView?.let {
                        templateAndroidView.removeView(it)
                    }
                }
            } finally {
                infoView = null
            }
        }
    }

    private fun addInfoView(viewId: Int): View {
        val container = FrameLayout(templateAndroidView.context)
        LayoutInflater.from(templateAndroidView.context).inflate(viewId, container, true)
        infoView = container

        container.findViewById<TextView>(R.id.textView)
            ?.setTextColor(colors.text.colorInt())

        container.findViewById<TextView>(R.id.textView)
            ?.visibility = if (displayText) View.VISIBLE else View.GONE

        if (BuildConfig.DEBUG && viewId == R.layout.info_template_progress) {
            container.findViewById<ProgressBar>(R.id.progress).setOnClickListener {
                templateView.printDebugInfo()
            }
        }

        val lp = templateView.createLayoutParams(LayoutPosition("1w", "1h", Alignment.center))
        templateAndroidView.addView(container, lp)

        return container
    }

    private fun showErrorAndButtonRetry(e: Throwable?) {
        e?.printDebug()

        removeInfoView()

        val container = addInfoView(R.layout.info_template_error)

        if (e != null) {
            container.findViewById<TextView>(R.id.textView)?.text = e.message
        }

        val button = infoView?.findViewById<View>(R.id.button)
        if (templateView.hasTemplateVariable()) {
            button?.setOnClickListener {
                templateView.loadTemplate(templateView.template)
            }
        } else {
            button?.visibility = View.GONE
        }

    }
}