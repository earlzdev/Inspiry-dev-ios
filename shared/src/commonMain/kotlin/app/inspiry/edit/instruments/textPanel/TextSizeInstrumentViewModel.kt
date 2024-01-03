package app.inspiry.edit.instruments.textPanel

import app.inspiry.core.analytics.AnalyticsManager
import app.inspiry.core.analytics.putInt
import app.inspiry.edit.instruments.BottomInstrumentsViewModel
import app.inspiry.views.InspView
import app.inspiry.views.text.InspTextView
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.math.sqrt

class TextSizeInstrumentViewModel(
    inspView: InspTextView,
    val analyticsManager: AnalyticsManager
) :
    BottomInstrumentsViewModel {

    val selectedView = MutableStateFlow(inspView)

    override fun sendAnalyticsEvent() {
        if (newSizes.isNotEmpty()) {
            analyticsManager.sendEvent("text_sized_changed", createParams = {
                newSizes[0]?.let {
                    putInt("text_size", (it * 1000).toInt())
                }
                newSizes[1]?.let {
                    putInt("letter_spacing", (it * 1000).toInt())
                }
                newSizes[2]?.let {
                    putInt("line_spacing", (it * 1000).toInt())
                }
                selectedView.value.templateParent.template.originalData!!.toBundleAnalytics(this)
            })
        }
    }

    private val newSizes: MutableMap<Int, Float> = mutableMapOf()
    private fun getTextSizeProgress(): Float =
        convertTextSizeToProgress(
            selectedView.value.textView?.textSize ?: 0f,
            selectedView.value.templateParent.viewWidth
        )

    private fun minimalSpacingValue() =
        if (selectedView.value.media.isCircularText()) MINIMAL_LETTER_SPACING_FOR_CIRCULAR else MINIMAL_LETTER_SPACING

    private fun calculatePositiveProgress(spacing: Float): Float =
        (MAXIMAL_LETTER_SPACING + spacing) / (2 * MAXIMAL_LETTER_SPACING)

    private fun calculateNegativeNegativeProgress(spacing: Float): Float {
        val minimal = minimalSpacingValue()
        return (minimal - spacing) / (2 * minimal)
    }

    private fun getLetterSpacingProgress(): Float {
        val letterSpacing = selectedView.value.media.letterSpacing
        return if (letterSpacing < 0) calculateNegativeNegativeProgress(letterSpacing) else calculatePositiveProgress(
            letterSpacing
        )
    }

    private fun getLineSpacingProgress(): Float = selectedView.value.media.lineSpacing / 2f

    val textSizeState = MutableStateFlow(getTextSizeProgress())
    val letterSpacingState = MutableStateFlow(getLetterSpacingProgress())
    val lineSpacingState = MutableStateFlow(getLineSpacingProgress())

    override fun onSelectedViewChanged(newSelected: InspView<*>?) {
        selectedView.value = newSelected as? InspTextView ?: return
        textSizeState.value = getTextSizeProgress()
        letterSpacingState.value = getLetterSpacingProgress()
        lineSpacingState.value = getLineSpacingProgress()
    }

    fun onTextSizeChanged(value: Float) {
        newSizes[0] = value
        selectedView.value.setNewTextSize(
            convertProgressToTextSize(
                value,
                selectedView.value.templateParent.viewWidth
            )
        )
        textSizeState.value = value
    }

    private fun linearProgress(value: Float, multiplier: Float): Float =
        (value * 2 - 1) * multiplier

    fun onLetterSpacingChanged(value: Float) {
        val minimal = minimalSpacingValue()
        val v =
            if (value >= 0.5f) linearProgress(value, MAXIMAL_LETTER_SPACING) else linearProgress(
                value, -minimal
            )
        newSizes[1] = v
        selectedView.value.setNewLetterSpacing(v)
        letterSpacingState.value = value
    }

    fun onLineSpacingChanged(value: Float) {
        newSizes[2] = value
        selectedView.value.setNewLineSpacing(value * 2f)
        lineSpacingState.value = value
    }

    private fun convertTextSizeToProgress(textSize: Float, templateWidth: Int): Float {
        val progress = (2f * textSize - 0.05f * templateWidth) / (0.8f * templateWidth)

        return textSizeInterpolatorOut(if (progress < 0f) 0f else progress)
    }

    private fun convertProgressToTextSize(progress: Float, templateWidth: Int): Float {

        val interpolatedProgress = textSizeInterpolatorIn(progress)

        return ((0.8f * interpolatedProgress + 0.05f) * templateWidth) / 2f
    }

    private fun textSizeInterpolatorOut(x: Float): Float {
        return sqrt(x)
    }

    private fun textSizeInterpolatorIn(x: Float): Float {
        return x * x
    }

    companion object {
        const val MINIMAL_LETTER_SPACING = -0.5f
        const val MINIMAL_LETTER_SPACING_FOR_CIRCULAR = -1f
        const val MAXIMAL_LETTER_SPACING = 1f
    }

}