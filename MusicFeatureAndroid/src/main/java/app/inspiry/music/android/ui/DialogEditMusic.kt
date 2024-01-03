package app.inspiry.music.android.ui

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import app.inspiry.core.analytics.AnalyticsManager
import app.inspiry.core.data.InspResponseData
import app.inspiry.core.data.InspResponseLoading
import app.inspiry.core.log.KLogger
import app.inspiry.core.util.getDefaultViewContainer
import app.inspiry.music.InstrumentViewAndroid
import app.inspiry.music.android.R
import app.inspiry.music.android.client.BaseAudioStatePlayer
import app.inspiry.music.android.client.ExoAudioStatePlayer
import app.inspiry.music.android.databinding.DialogEditMusicBinding
import app.inspiry.music.android.waveform.CheapSoundFile
import app.inspiry.music.android.waveform.WaveFormData
import app.inspiry.music.android.waveform.WaveFormFactory
import app.inspiry.music.model.TemplateMusic
import app.inspiry.music.util.TrackUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.component.inject
import org.koin.core.parameter.parametersOf

class DialogEditMusic(val waveformDurationMillis: Double, val music: TemplateMusic) :
    WaveForm.StartPositionListener, WaveForm.WaveScrollListener, InstrumentViewAndroid, KoinComponent {

    private lateinit var binding: DialogEditMusicBinding
    private lateinit var scope: LifecycleCoroutineScope
    val analyticManager: AnalyticsManager by inject()

    private var initialStartTime: Long = 0
    private var initialVolume: Int = 100

    var callback: Callbacks? = null

    private lateinit var player: BaseAudioStatePlayer

    val logger: KLogger by inject {
        parametersOf("music")
    }


    override fun createView(context: Context): View {

        scope = (context as AppCompatActivity).lifecycleScope

        initialStartTime = music.trimStartTime
        initialVolume = music.volume
        val inflater = LayoutInflater.from(context)
        binding = DialogEditMusicBinding.inflate(inflater, context.getDefaultViewContainer(), false)

        binding.root.setOnClickListener {
            // to prevent touched outside
        }

        player = ExoAudioStatePlayer(
            context, get(), scope = scope
        )
        binding.waveForm.startPositionListener = this
        binding.waveForm.waveScrollListener = this

        context.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onPause(owner: LifecycleOwner) {
                player.pause()
                callback?.playPauseTemplate(-1, false)
            }
        })

        onViewCreated(context)
        return binding.root
    }


    private fun onViewCreated(context: Context) {

        logger.debug { "DialogEditMusic::onViewCreated trimStartTime ${music.trimStartTime}" }

        player.setLoop(true)
        player.prepare(music.url, position = music.trimStartTime.toDouble())
        //player.seekTo(music.trimStartTime)

        binding.volumeSongSeekBar.progress = music.volume
        if (binding.volumeSongSeekBar.progress == 0) binding.iconSoundOffImageView.setImageResource(
            R.drawable.ic_sound_off_wave_form_dialog_white
        )
        player.setVolume(music.volume / 100f)

        binding.volumeSongSeekBar.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, p1: Int, p2: Boolean) {
                player.setVolume(seekBar.progress / 100f)
                music.volume = seekBar.progress
                if (seekBar.progress == 0) binding.iconSoundOffImageView.setImageResource(R.drawable.ic_sound_off_wave_form_dialog_white)
                else binding.iconSoundOffImageView.setImageResource(R.drawable.ic_sound_off_wave_from_dialog)
            }

            @SuppressLint("UseCompatLoadingForDrawables")
            override fun onStartTrackingTouch(p0: SeekBar) {
                binding.volumeSongSeekBar.thumb =
                    context.getDrawable(
                        R.drawable.ic_volume_button_seek_bar_wave_from_dialog_button_touch
                    )
            }

            @SuppressLint("UseCompatLoadingForDrawables")
            override fun onStopTrackingTouch(p0: SeekBar) {
                binding.volumeSongSeekBar.thumb =
                    context.getDrawable(
                        R.drawable.ic_volume_button_seek_bar_wave_from_dialog
                    )
                callback?.onMusicVolumeChange(p0.progress)
            }
        })

        scope.launch(Dispatchers.Main) {

            var isPlaying = false

            binding.iconPlayFrameLayout.setOnClickListener {
                player.seekTo(binding.waveForm.playPosition)

                if (isPlaying) {
                    player.pause()
                    callback?.playPauseTemplate(-1, !isPlaying)
                } else {
                    player.seekTo(binding.waveForm.leftLinePositionToMillis)
                    player.play()
                    callback?.playPauseTemplate(0, !isPlaying)
                }

            }

            player.playingState.collect {
                isPlaying = it.isPlaying
                if (isPlaying) {
                    binding.iconPlayImageView.setImageResource(R.drawable.ic_pause_wave_from_dialog)
                } else {
                    binding.iconPlayImageView.setImageResource(R.drawable.ic_play_wave_from_dialog)
                }
            }
        }

        scope.launch(Dispatchers.Main) {

            player.currentTimeState.collect {
                binding.durationPlaySongTextView.text = TrackUtils.convertTimeToString(it)

                if (binding.waveForm.isInitialed && !binding.waveForm.isInScroll) {
                    val positionPlaySong = it
                    if (positionPlaySong >= binding.waveForm.rightLinePositionToMillis) {
                        binding.waveForm.playPositionTo(binding.waveForm.leftLinePositionToMillis)
                        player.seekTo(binding.waveForm.leftLinePositionToMillis)

                        if (player.isPlayWhenReady())
                            callback?.playPauseTemplate(0, true)

                    } else {
                        binding.waveForm.playPositionTo(positionPlaySong)
                    }
                }
            }
        }

        scope.launch(Dispatchers.Main) {

            player.durationState.collect {

                binding.waveForm.setDurations(it, waveformDurationMillis)
                loadWaveform(true)
            }
        }

        binding.backgroundGoToLibraryView.setOnClickListener {
            callback?.openMusicLibrary(music)
        }

        binding.backgroundSoundOffView.setOnClickListener {
            player.setVolume(0f)
            binding.volumeSongSeekBar.progress = 0
        }

        binding.durationPlaySongTextView.text = TrackUtils.convertTimeToString(music.trimStartTime)
        binding.waveForm.setInitialPosition(music.trimStartTime)
        showWaveformLoader()

        callback?.playPauseTemplate(0, false)
    }

    private fun showWaveformLoader() {
        binding.waveProgress.visibility = View.VISIBLE
        binding.waveForm.visibility = View.GONE
    }

    private fun showWaveform() {
        binding.waveProgress.visibility = View.GONE
        binding.waveForm.visibility = View.VISIBLE
    }

    private fun loadWaveform(cheapMethod: Boolean) {

        scope.launch {
            val flow = if (cheapMethod) CheapSoundFile.create(
                Uri.parse(music.url),
                get()
            )
            else WaveFormFactory.build {
                it.setDataSource(
                    get(),
                    Uri.parse(music.url),
                    null
                )
            }

            flow
                .flowOn(Dispatchers.IO)
                .catch {
                    logger.error(it)
                    if (cheapMethod) {
                        loadWaveform(false)
                    } else {
                        showWaveform()
                        binding.waveForm.setInitializedAsIs()
                    }
                }
                .collect {

                    if (it is InspResponseData<WaveFormData>) {
                        showWaveform()
                        val data = it
                        binding.waveForm.arrayGain = data.data.samples

                    } else if (it is InspResponseLoading) {
                        if (it.progress != null) {

                            binding.waveProgress.isIndeterminate = false
                            binding.waveProgress.progress = (it.progress!! * 100).toInt()
                        }
                    }
                }
        }
    }

    override fun onDestroyView() {
        analyticManager.onMusicEditDialogClose(music, initialStartTime, initialVolume)
        player.release()
    }

    override fun onStartPositionChanged(position: Long, fromUser: Boolean) {
        binding.startPlaySongTextView.text = TrackUtils.convertTimeToString(position)
        binding.durationPlaySongTextView.text =
            TrackUtils.convertTimeToString(binding.waveForm.playPosition)

        if (fromUser) {
            callback?.onStartTimeChange(position)
            music.trimStartTime = position
        }
    }

    var wasPlayingBeforeScrollWave = false

    override fun onWaveScrollChanged(isScroll: Boolean) {
        if (isScroll) {
            wasPlayingBeforeScrollWave =
                player.isPlayWhenReady() || wasPlayingBeforeScrollWave
            player.pause()

            callback?.playPauseTemplate(0L, false)
        } else {
            if (wasPlayingBeforeScrollWave) {
                player.seekTo(binding.waveForm.leftLinePositionToMillis)
                player.play()

                callback?.playPauseTemplate(0, true)
            } else {
                player.seekTo(binding.waveForm.playPosition)

                callback?.playPauseTemplate(
                    binding.waveForm.playPosition - binding.waveForm.leftLinePositionToMillis,
                    false
                )
            }

            wasPlayingBeforeScrollWave = false
        }
    }

    interface Callbacks {
        fun onMusicVolumeChange(volume: Int)
        fun onStartTimeChange(newStartTime: Long)
        fun openMusicLibrary(music: TemplateMusic)

        //-1 means from previous
        fun playPauseTemplate(startPosition: Long, play: Boolean)
    }

}
