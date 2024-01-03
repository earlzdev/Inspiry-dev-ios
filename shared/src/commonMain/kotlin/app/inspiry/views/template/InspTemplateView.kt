package app.inspiry.views.template

import app.inspiry.core.analytics.AnalyticsManager
import app.inspiry.core.animator.helper.CommonAnimationHelper
import app.inspiry.core.data.*
import app.inspiry.core.helper.PlayTemplateFlow
import app.inspiry.core.log.KLogger
import app.inspiry.core.log.LoggerGetter
import app.inspiry.core.media.*
import app.inspiry.core.opengl.programPresets.TextureMaskHelper
import app.inspiry.core.opengl.programPresets.TextureMaskProvider
import app.inspiry.core.serialization.TemplateSerializer
import app.inspiry.core.template.MediaIdGenerator
import app.inspiry.core.template.MediaIdGeneratorImpl
import app.inspiry.core.template.TemplateReadWrite
import app.inspiry.core.template.TemplateUtils
import app.inspiry.core.util.*
import app.inspiry.edit.instruments.PickedMediaType
import app.inspiry.font.helpers.TextCaseHelper
import app.inspiry.font.provider.FontsManager
import app.inspiry.music.client.BaseAudioPlayer
import app.inspiry.music.model.TemplateMusic
import app.inspiry.palette.model.AbsPaletteColor
import app.inspiry.palette.model.TemplatePalette
import app.inspiry.textanim.TextAnimViewModel
import app.inspiry.views.*
import app.inspiry.views.factory.ViewFromMediaFactory
import app.inspiry.views.group.InspGroupView
import app.inspiry.views.guideline.GuideLine
import app.inspiry.views.guideline.GuidelineManager
import app.inspiry.views.infoview.InfoViewModel
import app.inspiry.views.media.InspMediaView
import app.inspiry.views.path.InspPathView
import app.inspiry.views.simplevideo.InspSimpleVideoView
import app.inspiry.views.slides.SlideUtilities
import app.inspiry.views.slides.SlidesUtilitiesImpl
import app.inspiry.views.text.InspTextView
import app.inspiry.views.touch.MovableTouchHelperFactory
import app.inspiry.views.vector.InspVectorView
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.serialization.json.Json
import okio.FileSystem
import okio.Path.Companion.toPath
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

abstract class InspTemplateView(
    val loggerGetter: LoggerGetter,
    val unitsConverter: BaseUnitsConverter,
    val infoViewModel: InfoViewModel?,
    val json: Json,
    val textCaseHelper: TextCaseHelper,
    val fontsManager: FontsManager,
    val templateSaver: TemplateReadWrite,
    val guidelineManager: GuidelineManager,
    val viewsFactory: ViewFromMediaFactory,
    val movableTouchHelperFactory: MovableTouchHelperFactory,
    val fileSystem: FileSystem,
    initialTemplateMode: TemplateMode
) : InspParent {

    val mediaViews: List<InspMediaView>
        get() = allViews.filterIsInstance<InspMediaView>()

    val allViews: MutableList<InspView<*>> = mutableListOf()

    val groupViews: List<InspGroupView> //todo remove it
        get() = allViews.filterIsInstance<InspGroupView>()

    override val inspChildren: List<InspView<*>>
        get() = allViews.filter { it.parentInsp is InspTemplateView }

    // null when view is not attached
    abstract val viewScope: CoroutineScope?

    // in android it is Activity.lifecycleScope
    abstract val containerScope: CoroutineScope

    lateinit var template: Template

    fun hasTemplateVariable() = this::template.isInitialized

    /**
     * nonnull after first onLayout
     */
    open val currentSize: MutableStateFlow<Size?> = MutableStateFlow(null)

    /**
     * Should we play this template over and over again or stop after first play.
     */
    var loopAnimation: Boolean = true

    /**
     * Every child of this template need to initialize itself.
     * For example: we only consider this template initialized when all InspMediaView load images
     */
    val childrenToInitialize: MutableSet<Media> = HashSet()
    var isInitialized: MutableStateFlow<Boolean> = MutableStateFlow(false)


    var currentFrame: Int = 0

    /**
     * if we wanted to play this template, but it is not initialized yet,
     * we can start playing after initialization happens
     */
    var waitToPlay: Boolean = false
    var isPlaying: MutableStateFlow<Boolean> = MutableStateFlow(false)
    var playingJob: Job? = null

    /**
     * when we are in MODE_DEMO, we display default images.
     * When we are in MODE_EDIT, we give ability to users to add image in EditActivity
     */
    var templateMode: TemplateMode = initialTemplateMode
        set(value) {
            field = value
            onTemplateModeHasChanged(value, reallyChanged = true)
        }

    /**
     * Recording - process of saving this template to video
     * waitToRecord - the same logic as @waitToPlay.
     */
    val isRecording: Boolean
        get() = recordMode != RecordMode.NONE

    var recordMode = RecordMode.NONE
        set(value) {
            mediaViews.forEach { it.setRecording(value != RecordMode.NONE) }
            field = value
        }

    /**
     * Simple flag if this InspTemplateView should set some background if template doesn't have one.
     */
    var shouldHaveBackground: Boolean = true

    /**
     * Changed when any of field, for example backgroundColor, is changed.
     * If template is changed then "save" option will be offered to user
     */
    var isChanged: MutableStateFlow<Boolean> = MutableStateFlow(false)

    /**
     * scale of template on the screen
     * and vertical offset relative to the parent view
     */

    val templateTransform: MutableStateFlow<TemplateTransform> =
        MutableStateFlow(TemplateTransform())

    /**
     * We add a little padding to each textView so that it looks better with background
     */
    val textsPadding: Int
        get() = currentSize.value?.width?.let { it / 300 } ?: 0

    var textViewsAlwaysVisible = false

    var updateFramesListener: ((Boolean) -> Unit)? = null

    var removeViewListener: ((InspView<*>) -> Unit)? = null

    var addTextListener: ((InspTextView) -> Unit)? = null

    var onSelectedViewMovedListener: (() -> Unit)? = null

    var selectedView: InspView<*>?
        set(value) {
            selectedViewInnerState.value = value
        }
        get() {
            return selectedViewInnerState.value
        }

    private val selectedViewInnerState = MutableStateFlow<InspView<*>?>(null)

    val selectedViewState: StateFlow<InspView<*>?> = selectedViewInnerState

    var musicPlayer: BaseAudioPlayer? = null

    val logger: KLogger by lazy {
        loggerGetter.getLogger("InspTemplateView")
    }


    abstract fun invalidateGuidelines()
    abstract fun initMusicPlayer(): BaseAudioPlayer

    // Int is the number of videos that we currently waiting for seek
    open val waitVideoSeek: StateFlow<Int> = MutableStateFlow(0)

    fun needToWaitVideoSeek() = waitVideoSeek.map { it > 0 }.distinctUntilChanged()

    abstract fun isWindowVisible(): Boolean
    abstract fun post(action: () -> Unit)
    abstract val copyInspViewPlusTranslation: Float
    abstract fun setBackgroundColor(color: AbsPaletteColor?)
    abstract fun setBackgroundColor(color: Int)

    // todo: dependency injection
    private val mediaIdGenerator: MediaIdGenerator by lazy {
        MediaIdGeneratorImpl()
    }

    // todo: dependency injection
    internal val slideUtilities: SlideUtilities by lazy {
        SlidesUtilitiesImpl(
            json,
            mediaIdGenerator
        )
    }

    fun getSize() = if (viewWidth == 0 || viewHeight == 0) {
        null
    } else {
        Size(viewWidth, viewHeight)
    }

    val guidelines: List<GuideLine> by lazy {
        guidelineManager.initGuidelines(this)
    }

    //they are initialized when template is initialized
    val maxFrames: Int
        get() {
            return if (!displayVideoDemo() && template.preferredDuration != 0) {
                template.preferredDuration

            } else allViews.maxByReturnMax { it.duration + it.media.startFrameRemoveShortcut() }
                ?: 0
        }

    private fun initMusic(playWhenReady: Boolean) {
        logger.debug {
            "playMusic musicPlayer ${musicPlayer}, music ${template.music}"
        }

        if (playWhenReady && playMusic()) return
        else if (musicPlayer != null) return

        template.music?.let { music ->
            if (musicPlayer == null && music.volume > 0) {

                musicPlayer = initMusicPlayer().also {
                    it.setVolume(music.volume / 100f)
                    val positionMs = (currentFrame * FRAME_IN_MILLIS).toLong() + music.trimStartTime
                    it.prepare(
                        music.url,
                        startPlayImmediately = playWhenReady,
                        position = positionMs.toDouble()
                    )
                }
            }
        }
    }

    private fun setTemplateTransform(
        scale: Float,
        verticalOffset: Int,
        topBarSize: Int,
        containerSize: Size,
        aspectRatio: Float
    ) {
        templateTransform.value = templateTransform.value.copy(
            scale = scale,
            verticalOffset = verticalOffset,
            staticOffset = topBarSize,
            containerSize = containerSize,
            aspectRatio = aspectRatio
        )
    }

    fun setTemplateTransform(templateTransform: TemplateTransform) {
        this.templateTransform.value = templateTransform
    }

    //update transforms when container changed
    fun refreshTemplateTransform(containerSize: Size, staticOffset: Int) {
        if (containerSize.height == 0 || templateMode != TemplateMode.EDIT) return
        val templateFormat =
            if (this::template.isInitialized) template.format else TemplateFormat.story
        val height = containerSize.width / templateFormat.aspectRatio()
        val vScale: Float = containerSize.height / height
        val hScale: Float = containerSize.width / containerSize.width.toFloat()
        val scale = min(vScale, hScale) * TEMPLATE_MAX_SCALE_WHEN_EDIT
        val offset: Int = if (vScale > 1) {
            (containerSize.height - height.roundToInt()) / 2
        } else 0

        setTemplateTransform(
            scale,
            offset,
            staticOffset,
            containerSize,
            templateFormat.aspectRatio()
        )
    }

    //update transforms when template was changed, (e.g. format)
    fun refreshTemplateTransform() {
        val transform = templateTransform.value
        refreshTemplateTransform(transform.containerSize, transform.staticOffset)
    }

    override fun addMediaToList(media: Media) {
        this.template.medias.add(media)
    }

    private fun playMusic(): Boolean {
        musicPlayer?.let {
            it.seekTo(
                (currentFrame * FRAME_IN_MILLIS).toLong() + (template.music?.trimStartTime
                    ?: 0L)
            )
            it.play()
            return true
        }
        return false
    }

    fun releaseMusic() {
        musicPlayer?.let {
            it.release()
            musicPlayer = null
        }
    }

    fun stopMusic() {
        musicPlayer?.pause()
    }


    fun changeSelectedView(value: InspView<*>?) {
        if (selectedView == value) return

        val old = selectedView
        selectedView = value

        old?.onSelectedChange(false)
        value?.onSelectedChange(true)

        logger.debug { "setSelectedView ${value}" }
    }


    val videoSelectedView: InspMediaView?
        get() {
            val view = selectedView
            if (view !is InspMediaView || !view.isVideo()) return null
            return view
        }

    fun isStatic() =
        template.preferredDuration == 0 && !template.medias.filterIsInstance<MediaImage>()
            .any { it.hasProgramOrVideo() } && getMinPossibleDuration() == 0 && !template.palette.isVideo()

    fun restoreRenderingInList() {
        mediaViews.forEach {
            it.restoreRenderingInList()
        }
        allViews.filterIsInstance<InspGroupView>().forEach {
            it.drawTemplateTextureSync()
        }
    }

    open fun removeViews() {
        stopPlaying()
        currentFrame = 0

        allViews.clear()
    }

    open fun addInspView(
        it: Media, parentInsp: InspParent, simpleVideo: Boolean
    ): InspView<*> {

        val inspView = if (simpleVideo)
            viewsFactory.simpleVideo(
                it as MediaImage,
                parentInsp,
                unitsConverter,
                this,
                fontsManager, loggerGetter, movableTouchHelperFactory
            )
        else
            viewsFactory.inspView(
                it,
                parentInsp,
                unitsConverter,
                this,
                fontsManager,
                loggerGetter,
                movableTouchHelperFactory
            )

        inspView.preRefresh()

        it.view = inspView
        return inspView
    }

    //@param newIndex. -1 for the first
    private fun innerAddMediaView(
        it: Media,
        addViewInto: InspParent,
        newIndex: Int = -1,
        simpleVideo: Boolean
    ): InspView<*> {

        val view = addInspView(it, addViewInto, simpleVideo)

        if (newIndex != -1)
            addViewInto.addViewToHierarchy(newIndex, view)
        else
            addViewInto.addViewToHierarchy(view)

        allViews.add(if (newIndex != -1) newIndex else allViews.size, view)

        view.hideView()

        return view
    }

    fun exchangeMedia(oldView: InspView<*>, mediaNew: Media): InspView<*> {

        val parent = oldView.parentInsp ?: return oldView
        val oldIndex = parent.indexOf(oldView)
        val view = innerAddMediaView(mediaNew, parent, simpleVideo = false, newIndex = oldIndex)
        if (view is InspMediaView) {
            waitInitialize(view)
            checkInitialized()
        }

        oldView.removeThisView()
        if (parent is InspGroupView) {
            parent.media.medias.add(oldIndex, mediaNew)
        } else template.medias.add(oldIndex, mediaNew)
        view.refresh()
        return view
    }

    fun replaceMediaWithVector(mediaView: InspMediaView, newOriginalSource: String): InspView<*> {
        val vectorMedia = mediaView.media.toMediaVector(newOriginalSource)
        val vectorView = exchangeMedia(mediaView, vectorMedia)
        changeSelectedView(vectorView)
        isChanged.value = true
        return vectorView
    }

    fun replaceVectorWithMedia(view: InspView<*>, newOriginalSource: String) {
        val vectorView = view as InspVectorView
        val imageMedia = vectorView.media.toMediaImage(newOriginalSource)
        exchangeMedia(vectorView, imageMedia)
        isChanged.value = true
    }

    fun addMediaView(
        it: Media,
        afterAdded: (InspView<*>) -> Unit,
        parent: InspParent = this, newIndex: Int = -1
    ) {
        if (it is MediaGroup) {

            val parent = innerAddMediaView(it, parent, newIndex, false) as InspGroupView
            it.medias.forEach {
                addMediaView(it, afterAdded, parent)
            }

        } else {
            val v = innerAddMediaView(it, parent, newIndex, false)
            afterAdded(v)
        }
    }

    internal fun addMediaViews(
        medias: List<Media>, parent: InspParent = this,
        afterAdded: (InspView<*>) -> Unit = {}
    ) {

        medias.forEach {
            addMediaView(it, afterAdded, parent = parent)
        }
        refreshRecursive(medias)
    }

    fun refreshRecursive(medias: List<Media>) {
        medias.forEach {
            it.view!!.refresh()
            if (it is MediaGroup) {
                refreshRecursive(it.medias)
            }
        }
    }

    fun onResumeOwner() {
        if (isPlaying.value) playMusic()
    }

    fun onPauseOwner() {
        stopMusic()
    }

    fun onStartOwner() {
        if (waitToPlay && isInitialized.value) {

            // because texture views are destroyed when we return
            if (mediaViews.any { it.media.hasProgramOrVideo() }) {
                loadTemplate(template)
            } else {
                startPlaying()
            }
        }
    }

    fun onStopOwner() {
        if (isPlaying.value && !waitToPlay) {
            stopPlaying()
            resetAnimation()
            releaseMusic()
            waitToPlay = true
        }
    }

    fun checkInitialized() {

        val isInitialized = childrenToInitialize.isEmpty()
        if (!isInitialized) {
            infoViewModel?.showLoadingTemplate()
            this.isInitialized.value = false
        } else {
            finishInitializing()
        }
    }

    fun displayVideoDemo() =
        (templateMode == TemplateMode.LIST_DEMO && template.videoDemo != null)

    internal fun needToInitAllChildren(medias: List<Media>) {
        medias.forEach {

            childrenToInitialize.add(it)
            if (it is MediaGroup) {
                needToInitAllChildren(it.medias)
            }
        }
    }

    private fun correctPreviewDurationForListDemo() {
        if (template.listDemoPreferredDuration != null)
            template.preferredDuration = template.listDemoPreferredDuration!!
    }

    private fun removeTemporaryMedias() {

        template.forEachMedias {
            val iterator = it.listIterator()

            while (iterator.hasNext()) {
                val media = iterator.next()

                if (media.isTemporaryMedia) {
                    iterator.remove()
                } else if (media is TemplateMaskOwner) {
                    media.templateMask?.unpacked = false
                }
            }
        }
    }

    open fun unpackTextures(template: Template) {
        TextureMaskHelper.maskProcessing(template = template, json = json)

        assignTextureIndexChildren()
        packTextureMediasInGroups()
    }

    fun unpackTemplateModels(template: Template) {
        mayAddBgImageFromPalette()

        // remove temporary medias that were previously generated by TemplateMaskProvider
        removeTemporaryMedias()
        slideUtilities.unpackAllSlides(template, templateMode)

        if (templateMode == TemplateMode.LIST_DEMO) correctPreviewDurationForListDemo()

        //if (templateMode == TemplateMode.EDIT)
        //generation of ids need in any modes for ios
        mediaIdGenerator.fillEmptyID(template.medias)

        unpackTextures(template = template)
    }

    fun removeChildrenRecursive(parentView: InspGroupView, removeSelf: Boolean = false) {

        parentView.children.filterIsInstance<InspGroupView>().forEach {
            removeChildrenRecursive(parentView = it, removeSelf = true)
        }

        parentView.children.toList().forEach {
            parentView.removeViewFromHierarchy(it, true)
            it.releaseInner()
        }

        if (removeSelf) {
            parentView.parentInsp?.removeViewFromHierarchy(parentView, true)
        }
    }

    open fun loadTemplate(template: Template) {

        removeViews()
        this.template = template

        onTemplateModeHasChanged(templateMode, reallyChanged = false)

        if (displayVideoDemo()) {
            loadDemoVideo()

        } else {
            if (templateMode == TemplateMode.LIST_DEMO) correctPreviewDurationForListDemo()

            mayAddBgImageFromPalette()
            // todo
//            if (!ignoreTextures) {
//                assignTextureIndexChildren()
//                packTextureMediasInGroups()
//            }

            unpackTemplateModels(template)

            childrenToInitialize.clear()
            val availableMedias = template.medias
            needToInitAllChildren(availableMedias)
            checkInitialized()
            addMediaViews(availableMedias)
        }
    }

    private fun assignTextureIndexChildren() {
        template.medias.forEach { mediaGroup ->
            if (mediaGroup is MediaGroup && mediaGroup.textureIndex != null) {
                mediaGroup.medias.forEachRecursive { innerMedia ->
                    innerMedia.textureIndex = mediaGroup.textureIndex
                }
            }
        }
    }

    private fun packTextureMediasInGroups() {

        val newMediaGroups = ArrayList<MediaGroup>()

        for (mediaChild in template.medias) {

            if (mediaChild.textureIndex != null && mediaChild !is MediaGroup) {

                val existingGroup =
                    newMediaGroups.find { it.textureIndex == mediaChild.textureIndex }

                if (existingGroup == null) {

                    val newGroup =
                        MediaGroup(
                            layoutPosition = LayoutPosition(
                                "match_parent", "match_parent",
                                Alignment.center
                            ),
                            medias = arrayListOf(mediaChild),
                            textureIndex = mediaChild.textureIndex
                        )
                    newMediaGroups.add(newGroup)

                } else {
                    existingGroup.medias.add(mediaChild)
                }
            }
        }
        newMediaGroups.sortBy { it.textureIndex!! }

        newMediaGroups.forEach {
            var firstIndex = template.medias.size

            it.medias.forEach {
                val index = template.medias.indexOf(it)
                if (firstIndex < index)
                    firstIndex = index
            }

            template.medias.add(firstIndex, it)
        }

        newMediaGroups.forEach {
            template.medias.removeAll(it.medias)
        }
    }

    private fun loadDemoVideo() {

        val media = MediaImage(layoutPosition = LayoutPosition("1w", "1h", Alignment.center))
        media.demoSource = template.videoDemo
        media.isVideo = true
        media.isEditable = false

        childrenToInitialize.clear()
        childrenToInitialize.add(media)
        checkInitialized()

        innerAddMediaView(media, this, simpleVideo = true).refresh()
    }

    private fun resetAnimation() {
        currentFrame = 0
        setFrameSyncInner(0)
    }

    fun prepareToVideoRecording() {
        recordMode = RecordMode.VIDEO
        setFrameAsInitial(0)

        /*mediaViews.forEach {
            it.restartVideoIfExists()
            it.playVideoIfExists(forcePlay = false)
        }*/
    }

    private fun canPlay(): Boolean =
        isInitialized.value && isWindowVisible() && viewScope != null

    fun onPlayPauseClick() {
        if (isPlaying.value) {
            stopPlaying()
        } else {
            startPlaying(false)
        }
    }

    open fun startPlaying(resetFrame: Boolean = true, mayPlayMusic: Boolean = true) {

        if (isPlaying.value) return

        val canPlay = canPlay()

        if (!isInitialized.value) {
            waitToPlay = true
            return

        } else if (isStatic()) {
            setFrameSync(0)
            setVideoFrameAsync(0, false)
            return

        } /*else if (BuildConfig.DEBUG) {
            setFrameForEdit()
            return
        }*/

        if (canPlay) {

            isPlaying.value = true
            waitToPlay = false

            if (displayVideoDemo()) {
                allViews.filterIsInstance<InspSimpleVideoView>()
                    .forEach { it: InspSimpleVideoView ->
                        it.playVideoIfExists()
                    }
                return
            }


            mediaViews.forEach {
                if (resetFrame) {
                    it.restoreRenderingInList()
                    it.restartVideoIfExists()
                }
                it.playVideoIfExists(forcePlay = false)
            }

            if (resetFrame)
                currentFrame = 0

            if (playingJob != null) throw IllegalStateException("thread is already launched")

            startPlayingJob()

            if (mayPlayMusic && (templateMode == TemplateMode.PREVIEW || templateMode == TemplateMode.EDIT)) {
                initMusic(true)
            }
        } else {
            if (!isInitialized.value) {
                infoViewModel?.showLoadingTemplate()
            }
            waitToPlay = true
        }
    }

    open fun startPlayingJob() {
        playingJob = viewScope?.launch {
            //todo
        }
    }

    open fun stopPlayingJob() {
        playingJob?.cancel()
        playingJob = null
    }

    fun setFrameSyncInner(frame: Int) {
        //we don't call it on allViews, to preserve the right order that is necessary for gl
        inspChildren.forEach {
            it.currentFrame = frame
        }
    }


    open fun setFrame(frame: Int) {
        currentFrame = frame

        setVideoFrameAsync(frame, sequential = true)

        if (currentFrame == 0) {
            musicPlayer?.seekTo(template.music?.trimStartTime ?: 0L)
        }
        updateFramesListener?.invoke(false)
        setFrameSyncInner(frame)
    }

    open fun onTemplateModeHasChanged(newMode: TemplateMode, reallyChanged: Boolean) {
        if (reallyChanged) {
            allViews.forEach {
                it.onTemplateModeHasChanged(newMode)
            }
            // to reset all animations
            setFrame(currentFrame)
        }
    }


    //TODO: it will have troubles with shortcut:
    // if some views in group have start time that is different from 0 (for example frame has 100 start time)
    // for solution we need to create another type of shortcut.

    private fun applyDurationToMedia(from: InspTextView, newMedia: Media) {
        val existingMedia = from.media

        newMedia.minDuration = existingMedia.minDuration
        newMedia.delayBeforeEnd = existingMedia.delayBeforeEnd

        if (newMedia is MediaGroup) {
            val newStartTime = from.getStartFrameShortCut()
            newMedia.startFrame += newStartTime
            newMedia.medias.forEach {
                if (it != existingMedia) {
                    it.minDuration = existingMedia.minDuration
                    it.delayBeforeEnd = existingMedia.delayBeforeEnd
                    it.startFrame += newStartTime
                }
            }
        } else {
            newMedia.startFrame = existingMedia.startFrame
        }
    }

    open fun applyStyleToText(existingText: InspTextView?, newMedia: Media) {

        val newMediaText = newMedia.selectTextView()

        newMedia.id = null
        if (newMedia is MediaGroup) {
            newMedia.medias.forEachRecursive {
                //todo this is wrong! may produce bug with same ids
                if (!(it is MediaVector && it.vectorAsTextBg))
                    it.id = null
            }
        }

        val existingMediaView: InspView<*>? =
            if (existingText?.parentDependsOnThisView() == true) existingText.parentInsp as InspGroupView else existingText

        if (newMediaText == null) {
            if (existingMediaView != null)
                removeInspView(existingMediaView)
            addMediaContent(newMedia)
            return
        }

        if (newMediaText.textSize.endsWith("w")) {
            newMediaText.textSize.replace('w', 'm')
        }

        newMediaText.multiplyTextSize(
            unitsConverter,
            TextAnimViewModel.TEXT_SIZE_IN_PREVIEW_MULTIPLIER
        )

        fun shouldChangeColor() =
            newMediaText.textColor == PredefinedColors.WHITE.argb.toInt()
                    && !newMediaText.hasBackground()
                    && !newMedia.childHasBgVector()
                    && !newMediaText.hasAnimatedColor()
                    && !newMediaText.hasMulticolorShadow()
                    && !newMedia.run {
                if (this is MediaGroup) {
                    this.backgroundColor != 0 || this.backgroundGradient != null
                } else {
                    false
                }
            }

        if (existingText != null) {

            newMediaText.text =
                textCaseHelper.setCaseBasedOnOther(existingText.media.text, newMediaText.text)
            applyDurationToMedia(existingText, newMedia)

            val parentExistingMediaOrThis = existingText.media.getParentGroupMedia()
            newMedia.setNewTranslationX(parentExistingMediaOrThis.translationX)
            newMedia.setNewTranslationY(parentExistingMediaOrThis.translationY)
            newMedia.textureIndex = parentExistingMediaOrThis.textureIndex
            newMedia.layoutPosition.alignBy = parentExistingMediaOrThis.layoutPosition.alignBy
            newMedia.layoutPosition.x = parentExistingMediaOrThis.layoutPosition.x
            newMedia.layoutPosition.y = parentExistingMediaOrThis.layoutPosition.y

            val existingMedia = existingText.media

            if (shouldChangeColor() &&
                !existingMedia.hasBackground() &&
                !parentExistingMediaOrThis.childHasBgVector()
            ) {
                newMediaText.textColor = existingMedia.textColor
            } else {
                template.palette.resetPaletteChoiceColor(existingMedia.id, true)
            }

            //dont set animatorOut if it was absent.
            if (existingMedia.animationParamOut == null && existingMedia.animatorsOut.isEmpty() &&
                newMediaText.animationParamOut == null && newMedia.animatorsOut.isEmpty()
            ) {
                newMedia.animatorsOut = ArrayList()
            }
        } else {
            if (shouldChangeColor()) {
                newMediaText.textColor = template.palette.defaultTextColor
            }
        }

        newMediaText.textSize =
            existingText?.media?.textSize ?: MediaText.DEFAULT_TEXT_SIZE_IN_TEMPLATE

        logger.info {
            "applyStyleToText text ${newMediaText.text}, hasVectorBg ${newMedia.childHasBgVector()}," +
                    " instance ${newMedia::class}, existing ${existingText}"
        }

        //remove out animation if this template has no out
        if (MAY_REMOVE_TEXT_OUT_ANIMATION) {
            val templateHasOutAnimation = allViews.any { it.durationOut > 0 }
            if (!templateHasOutAnimation) {
                newMediaText.animationParamOut = null
                if (newMediaText.animatorsOut.isNotEmpty()) {
                    newMediaText.animatorsOut = emptyList()
                }
                if (newMedia.animatorsOut.isNotEmpty()) {
                    newMedia.animatorsOut = emptyList()
                }
            }
        }

        if (existingMediaView != null)
            removeInspView(existingMediaView)

        val parentToAddViews: InspGroupView? = existingMediaView?.parentInsp as? InspGroupView?
        if (parentToAddViews == null)
            template.medias.add(newMedia)
        else
            parentToAddViews.media.medias.add(newMedia)


        needToInitAllChildren(listOf(newMedia))
        checkInitialized()

        logger.debug { "setSelectedView isInitialized ${isInitialized.value}" }

        //select textView when everything is initialized
        doWhenInitializedOnce {

            logger.debug { "setSelectedView doWhenInitializedOnce" }
            val textView = newMedia.selectTextView()?.view as? InspTextView?
            if (textView != null) {
                post { changeSelectedView(textView) }
            }
        }

        addMediaViews(listOf(newMedia), parentToAddViews ?: this) {
            if (it is InspTextView) {
                it.doOnRefresh = {

                    if (existingText == null) {
                        it.setTextAnimationTiming()
                    }

                    //because actual duration of new style may be different,
                    //so we need to clip duration to min/max values.
                    it.setTextDuration(it.duration)
                    it.currentFrame = currentFrame
                }
            } else if (it is InspPathView) {
                it.currentFrame = currentFrame
            }
        }

        isChanged.value = true
    }


    fun getCurrentTime() = this.currentFrame * FRAME_IN_MILLIS

    fun getTimeMs(frame: Int) = frame * FRAME_IN_MILLIS

    fun getFrame(timeMs: Double) = (timeMs / FRAME_IN_MILLIS).toInt()

    fun setFrameSync(frame: Int) {

        this.currentFrame = frame
        setFrameSyncInner(frame)
    }

    @WorkerThread
    open fun prepareAnimation(frame: Int) {
        this.currentFrame = frame
        allViews.forEach {
            it.prepareAnimation(frame)
        }
    }

    fun setVideoFrameAsync(frame: Int, sequential: Boolean) {
        mediaViews.forEach { it.drawVideoFrameAsync(frame, sequential) }
    }

    @WorkerThread
    fun setVideoFrameSync(frame: Int, sequential: Boolean) {
        mediaViews.forEach { it.drawVideoFrameSync(frame, sequential) }
    }

    @AnyThread
    fun stopPlaying() {
        stopMusic()
        if (isPlaying.value) {
            isPlaying.value = false
            mediaViews.forEach { it.pauseVideoIfExists() }
            waitToPlay = false

            stopPlayingJob()

            if (displayVideoDemo()) {
                allViews.filterIsInstance<InspSimpleVideoView>()
                    .forEach {
                        it.pauseVideoIfExists()
                    }
            }
        }
    }

    /**
     * means that children are initialized
     */
    fun childHasFinishedInitializing(inspView: InspView<*>) {
        logger.info {
            "childHasFinishedInitializing ${inspView.media}, " +
                    "wasInitialized ${isInitialized.value}, needToInit ${childrenToInitialize.size}"
        }

        inspView.isInitialized.value = true

        if (childrenToInitialize.remove(inspView.media)) {
            if (childrenToInitialize.isEmpty()) {
                finishInitializing()
            }
        }
    }

    fun interruptImagesLoading() {
        val iter = childrenToInitialize.iterator()
        while (iter.hasNext()) {
            val item = iter.next()
            if (item is MediaImage)
                iter.remove()
        }

        mediaViews.forEach {
            it.interruptImageLoading()
        }
        if (childrenToInitialize.isEmpty()) {
            removeLoadingImagesView()
            isInitialized.value = true
        }
    }

    private fun removeLoadingImagesView() {
        infoViewModel?.removeInfoView()
        showChildViews()
    }

    fun unbind(resetBg: Boolean = true) {
        infoViewModel?.cancelPendingProgressBar()
        stopPlaying()
        currentFrame = 0
        if (resetBg)
            maySetDefaultBg()
    }

    fun showErrorView(e: Throwable?) {
        e?.let { logger.error(it) }

        stopPlaying()
        infoViewModel?.showErrorAndButtonRetry(e)

        hideChildViews()
        selectedView = null
    }

    /**
     * TextureView should be visible (when programCreator is not null)
     */
    private fun hideChildViews() {
        allViews.forEach { it.hideView() }
        maySetDefaultBg()
    }

    private fun maySetDefaultBg() {
        setBackgroundColor(if (shouldHaveBackground) PredefinedColors.WHITE_ARGB else PredefinedColors.TRANSPARENT)
    }

    private fun showChildViews() {
        showAllViews()
        applyPalette(applyBgImage = false)
    }

    private fun showAllViews() {
        allViews.forEach {
            it.animationHelper?.preDrawAnimations(it.currentFrame)
            it.showView()
        }
    }

    protected open fun innerFinishInitializing() {
        showChildViews()
        infoViewModel?.removeInfoView()
        isInitialized.value = true

        if (waitToPlay) startPlaying()
    }

    private fun finishInitializing() {

        logger.debug {
            "finishInitializing waitToPlay = $waitToPlay, " +
                    "allViews = ${allViews.size}, size ${currentSize.value}, width ${viewWidth}, height ${viewHeight}"
        }

        if (currentSize.value == null) {
            doWhenHasTemplateSizeKnown(::innerFinishInitializing)
        } else {
            innerFinishInitializing()
        }
    }


    fun onFrameUpdated(value: Int) {
        if (value == Int.MAX_VALUE) {
            stopPlaying()
        } else setFrame(value)
    }

    fun getSelectableMediaViews(): List<InspMediaView> {
        return mediaViews.filter { it.media.isEditable && it.media.duplicate == null && it.media.id != MEDIA_ID_BACKGROUND && !it.isInSlides() }
    }

    val allTextViews: List<InspTextView>
        get() = allViews.filterIsInstance<InspTextView>()

    fun printDebugInfo() {
        logger.debug {
            "printTemplateDebugInfo, isPlaying = ${isPlaying.value}, " +
                    "leftToInitialize ${childrenToInitialize.size} ${childrenToInitialize}"
        }
    }

    //there's 2 ways to calculate proper time:
    //1) before we make out
    //2) after appear
    //we use here the second

    //works when template is initialized
    fun getFrameForEdit(exclude: Media? = null): Int {

        val duration = getDuration()
        if (duration == 0)
            return 0

        if (exclude == null && template.timeForEdit != null) return template.timeForEdit!!

        var staticTime = Int.MAX_VALUE

        if (CALCULATE_STATIC_FRAME_START) {
            staticTime = 0

            for (it in allViews) {
                if (it == exclude) {
                    continue
                }

                val innerTimeStart = it.getStartFrameShortCut() + it.durationIn

                staticTime = max(staticTime, innerTimeStart)
            }


            //K.i("text") { "predefinedTimeForEdit " + template.timeForEdit + " duration = " + duration + " staticTime = $staticTime" }

        } else {
            for (it in allViews) {
                if (it == exclude) {
                    continue
                }

                val innerTimeBeforeOut =
                    it.media.delayBeforeEnd + it.durationOut

                val whenStartOutAnimationInner =
                    it.duration + it.getStartFrameShortCut() - innerTimeBeforeOut

                staticTime = min(staticTime, whenStartOutAnimationInner)
            }

            if (staticTime == Int.MAX_VALUE) staticTime = duration
        }
        return staticTime
    }


    fun setFrameForEdit() {
        if (isStatic()) {
            currentFrame = 0
            return
        }

        val frame = getFrameForEdit()
        setFrameAsInitial(frame)
    }

    open fun setFrameAsInitial(frame: Int) {
        setFrameSync(frame)
    }

    private fun removeReferenceRecursive(view: InspView<*>) {
        allViews.remove(view)
        if (view is InspGroupView) {
            view.children.forEach {
                removeReferenceRecursive(it)
            }
        }
    }

    override fun removeViewFromHierarchy(view: InspView<*>, removeFromTemplateViews: Boolean) {
        template.medias.remove(view.media)
        if (removeFromTemplateViews) allViews.remove(view)
    }

    /**
     * this method is used in order to delete empty parent groups
     */
    private fun findChildToRemove(child: InspView<*>): InspView<*> {
        if ((child.parentInsp as? InspGroupView?)?.children?.size == 1) {
            return findChildToRemove(child.parentInsp as InspGroupView)
        } else {
            return child
        }
    }

    fun removeInspView(child: InspView<*>) {

        val childToRemove = findChildToRemove(child)
        childToRemove.parentInsp?.removeViewFromHierarchy(childToRemove)

        removeReferenceRecursive(childToRemove)

        if (childToRemove.media.textureIndex != null) {
            (childToRemove as? InspGroupView?)?.invalidateRedrawProgram()
        }

        changeSelectedView(null)
        removeViewListener?.invoke(childToRemove)
    }

    open fun copyInspView(inspView: InspView<*>) {

        val copiedMedia: Media
        val addToParent: InspGroupView?

        var inspViewMaybeParent: InspView<*> = inspView

        if (inspViewMaybeParent.parentDependsOnThisView()) {
            val tempParent = (inspView.parentInsp as InspGroupView)
            inspViewMaybeParent = tempParent
            copiedMedia = tempParent.media.copy(json)
            addToParent = tempParent.parentInsp as? InspGroupView?
            if (copiedMedia is MediaGroup) {
                copiedMedia.medias.forEachRecursive {
                    it.id += COPY_ID_POSTFIX
                    if (it is MediaText && it.duplicate != null) it.duplicate += COPY_ID_POSTFIX
                }
            }
        } else {
            copiedMedia = inspViewMaybeParent.media.copy(json)
            addToParent = inspViewMaybeParent.parentInsp as? InspGroupView?
        }
        copiedMedia.id += COPY_ID_POSTFIX
        val plusTranslation =
            max(inspViewMaybeParent.viewHeight / 2f, copyInspViewPlusTranslation)
        if (inspView.media.canMoveX())
            copiedMedia.setNewTranslationX(
                copiedMedia.translationX + (plusTranslation / (addToParent
                    ?: this).viewWidth)
            )
        if (inspView.media.canMoveY())
            copiedMedia.setNewTranslationY(
                copiedMedia.translationY + (plusTranslation / (addToParent
                    ?: this).viewHeight)
            )


        val parent: InspParent = addToParent ?: this
        parent.addMediaToList(copiedMedia)

        needToInitAllChildren(listOf(copiedMedia))
        checkInitialized()

        val currentIndex = parent.indexOf(inspViewMaybeParent) + 1

        addMediaView(copiedMedia, { copiedView ->
            if (copiedView is InspTextView) {
                copiedView.doOnRefresh = {
                    addTextListener?.invoke(copiedView)
                    copiedView.invalidateParentIfTexture()
                    copiedView.setSelected()
                }
            }

        }, parent = parent) //, newIndex = currentIndex

        refreshRecursive(listOf(copiedMedia))

        isChanged.value = true
    }


    fun getMinPossibleDuration(): Int {
        return allViews.maxByReturnMax { it.getMinPossibleDuration(true) + it.media.startFrameRemoveShortcut() }
            ?: 0
    }

    fun getDuration() = maxFrames

    fun minMaxDuration(): Pair<Int, Int> {
        return max(getMinPossibleDuration(), TemplateUtils.TEMPLATE_MIN_POSSIBLE_FRAMES) to
                min(
                    TemplateUtils.TEMPLATE_MAX_POSSIBLE_FRAMES,
                    template.maxDuration ?: Int.MAX_VALUE
                )
    }

    fun getInitialDuration(): Int {
        return template.initialDuration ?: template.preferredDuration
    }

    fun setNewDuration(newDuration: Int): Pair<Int, Int> {
        val minMax = minMaxDuration()
        if (template.initialDuration == null) template.initialDuration = template.preferredDuration
        template.preferredDuration =
            max(
                min(newDuration, minMax.second),
                minMax.first
            )

        allViews.forEach {
            it.afterCalcDurations(it.durationIn, it.durationOut, it.duration)
        }

        return minMax
    }

    open fun changeOrderOfViews(first: Media, second: Media) {

        template.medias.swap(first, second)

        setFrameSync(currentFrame)
        isChanged.value = true
    }

    fun checkPreferredDurationAfterInnerChange() {
        if (template.preferredDuration < getMinPossibleDuration()) {
            setNewDuration(getMinPossibleDuration())
        }
    }

    fun setDemosAsImages() {
        mediaViews.forEach {
            if (it.media.demoSource != null && (it.media.originalSource == null ||
                        (it.media.originalSource?.getScheme() == FileUtils.FILE_SCHEME && !fileSystem.exists(
                            (it.media.originalSource?.removeScheme() ?: "").toPath()
                        ))) && !it.media.isVideo
            ) {
                it.onNewImagePicked(it.media.demoSource!!, false)
                it.setUpMatrix()
            }
        }
    }

    fun changeFormat(format: TemplateFormat) {
        if (format == template.format) return

        template.format = format
        isChanged.value = true
        post {
            mediaViews.forEach {
                it.setDisplayMedias(false)
            }
        }
    }

    fun onPickedBackgroundImage(uri: String, videoStartMs: Int?) {
        template.palette.mainColor = null
        template.palette.backgroundImage = uri
        template.palette.backgroundVideoStartMs = videoStartMs
        template.palette.backgroundVideoLooped = if (videoStartMs == null) null else false

        applyPalette(true, true, checkInitializedWhenApplyingImage = true)
    }

    fun getNumOfGlViewsToDraw(callback: () -> Unit): Int {
        var res = 0
        mediaViews.forEach {
            if (it.media.hasProgramOrVideo()) {
                it.innerMediaView?.framePreparedCallback = callback
                res++
            }
        }
        return res
    }

    fun drawTemplateTexturesSync() {
        allViews.forEach {
            if (it.media.textureIndex != null && it is InspGroupView) {
                it.drawTemplateTextureSync()
            }
        }
    }

    fun waitInitialize(view: InspView<*>, loadingOnlyImages: Boolean = false) {

        if (!childrenToInitialize.contains(view.media)) {
            childrenToInitialize.add(view.media)

            if (isInitialized.value) {
                if (loadingOnlyImages)
                    infoViewModel?.showLoadingImages()
                else
                    infoViewModel?.showLoadingTemplate()

                this.isInitialized.value = false
                hideChildViews()
            }
        }

        logger.info {
            "waitInitialize after childrenToInitialize ${childrenToInitialize.size}, " +
                    "isInitialized ${isInitialized.value}, media ${view.media}}"
        }
    }

    fun onInsideViewMoved(inspView: InspView<*>, changedElseAnim: Boolean) {
        if (templateMode == TemplateMode.EDIT) {
            if (changedElseAnim) {
                guidelineManager.onViewMoved(this@InspTemplateView, inspView)
            }
            onSelectedViewMovedListener?.invoke()
        }
    }

    private fun checkMusicPlayerNull() {
        if (musicPlayer != null) {
            logger.warning { "music Player should be null here. Since it is non null only when TimelinePanel is opened. And these methods are only called When MusicEditDialog is present." }
            releaseMusic()
        }
    }

    fun onMusicVolumeChange(newVolume: Int) {
        checkMusicPlayerNull()
        template.music?.volume = newVolume
        isChanged.value = true
    }

    fun onMusicStartChange(newStartTime: Long) {
        checkMusicPlayerNull()
        template.music?.trimStartTime = newStartTime
        isChanged.value = true
    }

    fun setNewMusic(newMusic: TemplateMusic?) {
        template.music = newMusic
        isChanged.value = true
    }

    fun unloadTemplate() {
        unbind()
        removeViews()
        template = Template()
    }

    fun addMediaContent(content: Media) {
        //The template is initialized when all views are initialized.
        //Template should not be initialized right now because we have not added view yet.
        //If this is not done, then when you first select a video, its duration will be unknown yet
        isInitialized.value = false
        content.id = null
        if (content is MediaGroup) {
            content.medias.forEachRecursive { it.id = null }
        }

        template.medias.add(content)

        if (content.id == null) mediaIdGenerator.fillEmptyID(template.medias)

        needToInitAllChildren(listOf(content))

        addMediaView(content, {

            //logger.debug { "addSticker: setSelectedView${it}" }

            //sticker will select itself
            if (it is InspTextView)
                changeSelectedView(it)
        })

        refreshRecursive(listOf(content))
        isChanged.value = true
    }


    private fun getMusicAudioTrackForRecord(): OriginalAudioTrack? {
        assertInitialized()

        return template.music?.let {
            if (it.volume > 0) {

                val totalDuration = maxFrames.frameToTimeUs()

                return OriginalAudioTrack(
                    0L,
                    totalDuration, it.trimStartTime * 1000L,
                    it.url, false, it.volume / 100f
                )

            } else null
        }
    }

    fun getOriginalAudioDataForRecordOnlyMusic(): OriginalAudioData? {
        assertInitialized()

        return getMusicAudioTrackForRecord()?.let {
            OriginalAudioData(listOf(it), maxFrames.frameToTimeUs(), it.volume)
        }
    }

    fun getOriginalAudioDataForRecordAllTracks(): OriginalAudioData? {
        assertInitialized()

        val totalDuration = maxFrames.frameToTimeUs()

        val tracks = mutableListOf<OriginalAudioTrack>()

        for (it in allViews) {
            //maybe is video has audio is wrong here. We need to ensure that template is initialized for using it.
            if (it is InspMediaView && !it.isSocialIcon()) {

                val displaySource = it.media.getDisplaySource()
                val hasAudio =
                    it.isVideo() && displaySource != null && it.isVideoHasAudio(false) && it.media.videoVolume != null
                            && it.media.videoVolume?.value != 0f

                if (hasAudio) {
                    val res = OriginalAudioTrack(
                        it.getViewTimeOffsetUs(),
                        it.getDurationForTrimmingMillis() * 1000L,
                        (it.media.videoStartTimeMs?.value ?: 0) * 1000L,
                        displaySource!!,
                        it.media.isLoopEnabled == true,
                        it.media.videoVolume!!.value
                    )
                    tracks.add(res)
                }
            }
        }

        getMusicAudioTrackForRecord()?.let {
            tracks.add(it)
        }

        return if (tracks.size > 0) {
            OriginalAudioData(tracks, totalDuration, 1f)
        } else null
    }

    fun assertInitialized() {
        if (!isInitialized.value) {
            throw IllegalStateException("the template should be initialized to use this func")
        }
    }

    private fun waitInitializeBunchWithDuplicates(views: List<InspMediaView>) {

        for (v in views) {

            val roleModel = v.findRoleModelMedia()
            if (roleModel != null) {
                roleModel.findDuplicatedMedias().forEach {
                    waitInitialize(it, loadingOnlyImages = true)
                }
            } else {
                waitInitialize(v, loadingOnlyImages = true)

                v.findDuplicatedMedias().forEach {
                    waitInitialize(it, loadingOnlyImages = true)
                }
            }
        }
    }

    fun loadBunchMedias(
        viewThatWasSelected: InspMediaView,
        sources: List<PickMediaResult>,
        analyticsManager: AnalyticsManager
    ) {

        val mediaViews: MutableList<InspMediaView>
        if (viewThatWasSelected.media.isSocialIcon) mediaViews =
            this.mediaViews.toMutableList()
        else {
            mediaViews = getSelectableMediaViews().toMutableList()
        }

        if (template.imgOrderById) mediaViews.sortBy { it.media.id }
        else {
            mediaViews.remove(viewThatWasSelected)
            mediaViews.sortWith { o1, o2 ->
                if (!o1.media.hasUserSource() && o2.media.hasUserSource()) -1
                else if (o1.media.hasUserSource() && !o2.media.hasUserSource()) 1
                else 0
            }
            //place image on which we click first
            mediaViews.add(0, viewThatWasSelected)
        }
        var viewIndex = 0
        var textureIndex = 0
        var hasVideo = false

        //TODO: this is not compatible with textureIndex stuff.
        // but currently not a problem because there's no templates currently
        // where one MediaImage has more than two image textures.
        val mediaViewsThatWeSetImagesTo = mediaViews.subList(0, sources.size)
        if (mediaViewsThatWeSetImagesTo.size > 0) isInitialized.value = false
        waitInitializeBunchWithDuplicates(mediaViewsThatWeSetImagesTo)

        for (source in sources) {

            val isVideo = source.type == PickedMediaType.VIDEO
            val mediaView: InspMediaView = mediaViews[viewIndex]

            if (isVideo) {
                hasVideo = true

                mediaView.insertNewVideo(
                    0, source.uri, textureIndex,
                    false, true
                )
            } else {
                mediaView.onNewImagePicked(
                    source.uri,
                    viewIndex == 0, textureIndex = textureIndex
                )
            }

            textureIndex++
            if (textureIndex == mediaView.sourceCount) {
                viewIndex++
                textureIndex = 0
            }
        }

        analyticsManager.onMediaSelected(hasVideo, sources.size, template.originalData!!)
    }

    fun fillEmptyMedias() {
        fun getSelectableMedias(): MutableList<MediaImage> {
            val images = mutableListOf<MediaImage>()
            template.medias.forEachRecursive {
                if (it is MediaImage && it.isEditable &&
                    it.duplicate == null && it.id != MEDIA_ID_BACKGROUND &&
                    !it.isVideo
                )
                    images.add(it)
            }
            return images
        }

        val selectableMedias = getSelectableMedias()
        val emptyMedias =
            selectableMedias.filter { it.originalSource == null || it.originalSource?.isEmpty() == true }
        val mediasWithSource =
            selectableMedias.filter { it.originalSource?.isNotEmpty() == true && it.isMovable.nullOrFalse() }
                .sortedBy { it.id }
        if (emptyMedias.isEmpty() || mediasWithSource.isEmpty()) return
        var sourceIndex = 0
        emptyMedias.forEach {
            it.originalSource = mediasWithSource[sourceIndex].originalSource!!
            mediaViews.find { mv -> mv.media == it }?.clearMediaOnEdit = true
            sourceIndex++
            if (sourceIndex >= mediasWithSource.size) sourceIndex = 0
        }
    }

    private fun rememberInitialColorByIds(ids: List<String>) {
        allViews.filter { ids.contains(it.media.id) }.forEach { it.rememberInitialColors() }
    }

    fun restoreOriginalColorsWithoutDefault(layer: Int) {
        val ids: List<Pair<String, Boolean>> = template.palette.choices[layer].elements
            .filter { it.id != null }
            .map {
                Pair(
                    it.id!!,
                    it.type.equals(other = "elementBackgroundColor", ignoreCase = true)
                )
            }

        ids.forEach { pair ->
            allViews.find { it.media.id == pair.first }?.restoreInitialColors(0, pair.second)
        }
    }

    fun maySaveInitial() {
        if (template.initialPalette == null) {
            template.initialPalette = template.palette.copyViaJson(json)
            template.palette.choices.forEach { choice ->
                if (choice.color == null) {
                    val ids: List<String> =
                        choice.elements.filter { it.id != null }.map { it.id!! }
                    rememberInitialColorByIds(ids)
                }
            }
        }
    }

    private var _templateEditAction: ((TemplateEditIntent, InspView<*>?) -> Unit)? = null

    fun onTemplateEditAction(action: (TemplateEditIntent, InspView<*>?) -> Unit) {
        _templateEditAction = action
    }

    fun editAction(intent: TemplateEditIntent, inspView: InspView<*>?) {
        _templateEditAction?.invoke(intent, inspView)
    }

    fun getCopyOfTemplate(): Template {
        return json.decodeFromString(
            TemplateSerializer, json.encodeToString(
                TemplateSerializer, template
            )
        )
    }

    companion object {
        const val COPY_INSP_VIEW_TRANSLATION_PLUS = 30f
        const val TEMPLATE_MAX_SCALE_WHEN_EDIT = 0.95f
    }
}

inline fun InspTemplateView.doWhenHasTemplateSizeKnown(crossinline action: suspend () -> Unit) {
    containerScope.collectUntil(currentSize, condition = { it != null }, action = action)
}

inline fun InspTemplateView.doWhenInitializedOnce(crossinline action: suspend () -> Unit) {
    containerScope.collectUntil(isInitialized, condition = { it }, action = action)
}

fun <T> MutableList<T>.swap(first: T, second: T) {
    val i = indexOf(first)
    val j = indexOf(second)
    set(i, set(j, get(i)))
}

fun List<Media>.forEachRecursive(action: (Media) -> Unit) {
    forEach {
        action(it)
        if (it is MediaGroup) {
            it.medias.forEachRecursive(action)
        }
    }
}

fun List<Media>.forEachRecursiveWithParent(
    parentGroup: MediaGroup?,
    action: (Media, MediaGroup?) -> Unit
) {
    forEach {
        action(it, parentGroup)
        if (it is MediaGroup) {
            it.medias.forEachRecursiveWithParent(it, action)
        }
    }
}


const val CALCULATE_STATIC_FRAME_START = true
const val MAY_REMOVE_TEXT_OUT_ANIMATION = false
const val PALETTE_ID_ALL_TEXTS = "all_texts"
const val MEDIA_ID_BACKGROUND = "background"
const val COPY_ID_POSTFIX = "_copy"
