package app.inspiry.dialog

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.doOnLayout
import androidx.lifecycle.lifecycleScope
import app.inspiry.databinding.PanelTimelineBinding
import app.inspiry.core.analytics.AnalyticsManager
import app.inspiry.core.analytics.putBoolean
import app.inspiry.core.analytics.putInt
import app.inspiry.core.util.getDefaultViewContainer
import app.inspiry.edit.instruments.TimeLineInstrumentModel
import app.inspiry.music.InstrumentViewAndroid
import app.inspiry.views.template.TemplateMode
import app.inspiry.views.text.InspTextView
import app.inspiry.views.timeline.TimelineView
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class TimelinePanel(val viewModel: TimeLineInstrumentModel): InstrumentViewAndroid, KoinComponent {

    private val templateView = viewModel.templateView
    lateinit var timelineView: TimelineView

    var templateOldDuration = 0
    lateinit var textOldDurations: List<Int>
    lateinit var textOldStarts: List<Int>

    lateinit var binding: PanelTimelineBinding
    private val analyticManager: AnalyticsManager by inject()

    override fun createView(context: Context): View {
        val inflater = LayoutInflater.from(context)
        templateView.textViewsAlwaysVisible = false
        templateView.setFrameForEdit()

        binding = PanelTimelineBinding.inflate(inflater, context.getDefaultViewContainer(), false)

        timelineView = TimelineView(
            context,
            templateView
        )

        viewModel.onViewChanged { it
            timelineView.onSelectedViewChange(it)
        }

        binding.container.addView(
            timelineView,
            0,
            FrameLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
        )

        (context as AppCompatActivity).lifecycleScope.launch {
            templateView.isPlaying.collect {
                invalidateIconPlaying()
            }
        }

        templateView.updateFramesListener = {
            setCurrentTime(it)
        }
        binding.iconPlayPause.setOnClickListener {
            templateView.onPlayPauseClick()
        }
        onViewCreated()
        return binding.root

    }

    private fun invalidateIconPlaying() {
        binding.iconPlayPause.isActivated = templateView.isPlaying.value
    }

    override fun onDestroyView() {


        if (templateView.templateMode == TemplateMode.EDIT) {
            templateView.releaseMusic()
            templateView.stopPlaying()
            templateView.setFrameForEdit()
            templateView.textViewsAlwaysVisible = true
        }

        templateView.updateFramesListener = null

        val templateNewDuration = templateView.getDuration()
        val texts = templateView.allViews.filterIsInstance(InspTextView::class.java)
        val textNewDurations = texts.map { it.duration }
        val textNewStarts = texts.map { it.media.startFrame }

        if (templateOldDuration != templateNewDuration || textNewDurations != textOldDurations || textNewStarts != textOldStarts) {

            analyticManager.sendEvent("timeline_changed", createParams = {

                if (templateOldDuration != templateNewDuration) {
                    putInt("new_template_duration", templateNewDuration)
                }
                putBoolean("text_start_changed", textNewStarts != textOldStarts)
                putBoolean("text_duration_changed", textNewDurations != textOldDurations)

                templateView.template.originalData!!.toBundleAnalytics(this)
            })
        }
    }


    private fun onViewCreated() {

        templateOldDuration = templateView.getDuration()
        val texts = templateView.allViews.filterIsInstance(InspTextView::class.java)
        textOldDurations = texts.map { it.duration }
        textOldStarts = texts.map { it.media.startFrame }

        templateView.addTextListener = { inspView ->
            if (!inspView.isDuplicate()) {
                timelineView.addInsideText(inspView)
                timelineView.updateTextsScrollViewHeight()
            }
        }

        timelineView.doOnLayout {
            setCurrentTime(false)
        }

        // for unknown reason text is not visible without this line at startup. Even thou setCurrentTime(false) is called above
        binding.textCurrentTime.text = millisToTimer(templateView.getCurrentTime())
    }

    private fun setCurrentTime(fromUser: Boolean) {
        val currentTime = templateView.getCurrentTime()
        binding.textCurrentTime.text = millisToTimer(currentTime)
        timelineView.updatePosition(currentTime, fromUser)
    }

    fun durationMayChanged() {
        setCurrentTime(false)
    }

    companion object {
        fun millisToTimer(millis: Double): String {
            val builder = StringBuilder()

            val secondsInDouble = millis / 1000
            val hours = (millis / 3600000).toInt()
            val minutes = (millis % 3600000 / 60000).toInt()
            val seconds = (secondsInDouble).toInt() % 60
            val partSeconds = ((secondsInDouble - secondsInDouble.toInt()) * 10.0).toInt()

            if (hours > 0) builder.append(hours).append(":")
            if (minutes > 0) {
                if (minutes < 10) builder.append("0")
                builder.append(minutes)
                builder.append(":")
            }
            if (seconds < 10) builder.append("0")
            builder.append(seconds)

            builder.append(".").append(partSeconds)

            return builder.toString()
        }
    }
}

