package app.inspiry.edit.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.viewinterop.AndroidView
import app.inspiry.core.data.FRAME_IN_MILLIS
import app.inspiry.edit.instruments.FullScreenTools
import app.inspiry.edit.instruments.InstrumentsManager
import app.inspiry.music.InstrumentViewAndroid
import app.inspiry.music.android.ui.DialogEditMusic
import app.inspiry.music.model.TemplateMusic
import app.inspiry.views.template.TemplateMode
import kotlin.math.roundToInt


@Composable
fun MusicPanelUI(instrumentsManager: InstrumentsManager) {
    val templateView = instrumentsManager.templateView
    val panel: InstrumentViewAndroid = DialogEditMusic(
        templateView.getDuration() * FRAME_IN_MILLIS,
        templateView.template.music!!
    ).apply {
        callback = object : DialogEditMusic.Callbacks {
            override fun onMusicVolumeChange(volume: Int) {
                templateView.onMusicVolumeChange(volume)
            }

            override fun onStartTimeChange(newStartTime: Long) {
                templateView.onMusicStartChange(newStartTime)
            }

            override fun openMusicLibrary(music: TemplateMusic) {
                instrumentsManager.selectFullScreenTool(FullScreenTools.MUSIC, false)
            }

            override fun playPauseTemplate(startPosition: Long, play: Boolean) {
                val startFrame = (startPosition / FRAME_IN_MILLIS).roundToInt()
                if (play) {
                    if (startPosition > 0L) {
                        templateView.setVideoFrameAsync(startFrame, false)
                        templateView.setFrameSync(startFrame)
                    }
                    templateView.stopPlaying()
                    templateView.startPlaying(
                        resetFrame = startPosition == 0L,
                        mayPlayMusic = false
                    )
                } else {
                    if (startPosition != -1L) {
                        templateView.setVideoFrameAsync(startFrame, false)
                        templateView.setFrameSync(startFrame)
                    }
                    templateView.stopPlaying()
                }
            }
        }
    }
    DisposableEffect(Unit) {
        onDispose {
            if (templateView.templateMode == TemplateMode.EDIT) {
                templateView.stopPlaying()
                templateView.setFrameForEdit()
            }
                panel.onDestroyView()
        }
    }
    AndroidView(factory = { context ->
        return@AndroidView panel.createView(context)
    })
}