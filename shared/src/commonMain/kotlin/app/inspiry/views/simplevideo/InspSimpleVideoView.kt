package app.inspiry.views.simplevideo

import app.inspiry.core.animator.helper.AbsAnimationHelper
import app.inspiry.core.data.FRAME_IN_MILLIS
import app.inspiry.core.log.KLogger
import app.inspiry.core.log.LoggerGetter
import app.inspiry.core.media.BaseUnitsConverter
import app.inspiry.core.media.MediaImage
import app.inspiry.views.InspParent
import app.inspiry.views.InspView
import app.inspiry.views.template.InspTemplateView
import app.inspiry.views.touch.MovableTouchHelperFactory
import app.inspiry.views.viewplatform.ViewPlatform
import kotlin.math.floor

class InspSimpleVideoView(
    media: MediaImage,
    parentInsp: InspParent?,
    view: ViewPlatform?,
    unitsConverter: BaseUnitsConverter,
    animationHelper: AbsAnimationHelper<*>?,
    loggerGetter: LoggerGetter,
    movableTouchHelperFactory: MovableTouchHelperFactory, val getPlayer: () -> BaseVideoPlayer,
    templateParent: InspTemplateView
) : InspView<MediaImage>(
    media, parentInsp, view, unitsConverter, animationHelper,
    loggerGetter,
    movableTouchHelperFactory, templateParent
) {

    var player: BaseVideoPlayer? = null

    override var duration = 0
        get() {
            return floor((player?.getDuration() ?: 0) / FRAME_IN_MILLIS).toInt()
        }

    override val logger: KLogger = loggerGetter.getLogger("InspSimpleVideoView")

    override fun onCurrentFrameChanged(newVal: Int, oldVal: Int) {

    }

    private var currentState = 0

    override fun getMinPossibleDuration(includeDelayBeforeEnd: Boolean): Int = duration

    companion object {
        const val STATE_NOTHING = 0
        const val STATE_IN_PROCESS = 1
        const val STATE_PREPARED = 2
    }

    override fun onAttach() {
        super.onAttach()
        refresh()
    }

    override fun onDetach() {
        super.onDetach()
        currentState = 0
        player?.release()
        player = null
    }

    fun onPrepared() {

        logger.debug { "onPrepared ${currentState}" }

        if (currentState != STATE_PREPARED) {

            currentState = STATE_PREPARED
            templateParentNullable?.childHasFinishedInitializing(this)
        }
    }

    override fun refresh() {
        logger.info {
            "refresh is called ${currentState}," +
                    " demoSource ${media.demoSource}, player ${player}"
        }

        if (media.demoSource == null)
            return

        if (currentState == STATE_NOTHING) {

            if (player == null) {
                player = getPlayer()
            }
            player?.onPrepared = ::onPrepared

            currentState = STATE_IN_PROCESS
            player?.prepare(media.demoSource!!)

        } else if (currentState == STATE_PREPARED) {
            templateParentNullable?.childHasFinishedInitializing(this)
        }
    }

    fun playVideoIfExists() {
        if (player?.isPlaying() == false)
            player?.play()
    }

    fun pauseVideoIfExists() {
        player?.pause()
    }

    override fun rememberInitialColors() {}

    override fun restoreInitialColors(layer: Int, isBack: Boolean) {}
}