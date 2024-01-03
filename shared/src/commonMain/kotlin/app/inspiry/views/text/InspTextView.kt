package app.inspiry.views.text

import app.inspiry.core.animator.InspAnimator
import app.inspiry.core.animator.appliers.ColorType
import app.inspiry.core.animator.appliers.RadiusAnimApplier
import app.inspiry.core.animator.helper.AbsAnimationHelper
import app.inspiry.core.animator.helper.CommonAnimationHelper
import app.inspiry.core.data.FRAME_IN_MILLIS
import app.inspiry.core.log.GlobalLogger
import app.inspiry.core.log.KLogger
import app.inspiry.core.log.LoggerGetter
import app.inspiry.core.manager.DebugManager
import app.inspiry.core.media.*
import app.inspiry.core.template.TemplateUtils
import app.inspiry.font.model.FontData
import app.inspiry.font.provider.FontsManager
import app.inspiry.palette.model.*
import app.inspiry.views.InspParent
import app.inspiry.views.InspView
import app.inspiry.views.group.InspGroupView
import app.inspiry.views.maxByReturnMax
import app.inspiry.views.path.InspPathView
import app.inspiry.views.template.InspTemplateView
import app.inspiry.views.template.TemplateEditIntent
import app.inspiry.views.template.TemplateMode
import app.inspiry.views.touch.MovableTouchHelperFactory
import app.inspiry.views.viewplatform.ViewPlatform
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt


class InspTextView(
    media: MediaText,
    parentInsp: InspParent?,
    view: ViewPlatform?,
    unitsConverter: BaseUnitsConverter,
    animationHelper: AbsAnimationHelper<*>?,
    val fontsManager: FontsManager,
    var textView: InnerTextHolder?,
    loggerGetter: LoggerGetter, touchHelperFactory: MovableTouchHelperFactory,
    templateParent: InspTemplateView
) : InspView<MediaText>(
    media,
    parentInsp,
    view,
    unitsConverter,
    animationHelper,
    loggerGetter,
    touchHelperFactory, templateParent
) {

    var doOnRefresh: (() -> Unit)? = null
    override val logger: KLogger = loggerGetter.getLogger("InspTextView ${media.text}")

    override var currentFrame: Int = 0
        set(value) {
            onCurrentFrameChangedViewThatCanHide(value, canHideAfter = true) { newValue ->
                field = newValue
                textView?.currentFrame = newValue
            }
        }

    override fun onTemplateModeHasChanged(newMode: TemplateMode) {
        super.onTemplateModeHasChanged(newMode)
        setOnClickInnerText()
    }

    override fun onTemplateSizeChanged(width: Int, height: Int) {
        super.onTemplateSizeChanged(width, height)
        textView?.onParentSizeChanged(width, height)
    }

    private fun setOnClickInnerText() {
        if (templateMode == TemplateMode.EDIT) {
            textView?.setOnClickListener {

                val isSelected = isSelectedForEdit
                if (!isSelected) {
                    //select template on down event
                } else {
                    templateParent.editAction(TemplateEditIntent.TEXT_EDIT, this)
                }
            }
        } else {
            textView?.setOnClickListener(null)
        }
    }

    override fun refresh() {
        super.refresh()

        if (media.layoutPosition.width != "wrap_content" && media.layoutPosition.height != "wrap_content")
            textView?.switchToAutoSizeMode()
        else textView?.switchToWrapContentMode()

        setOnClickInnerText()

        textView?.onTextChanged = {
            templateParentNullable?.isChanged?.value = true
            media.text = it
        }
        maySetColorForBrothers()

        textView?.doOnInnerTextLayout {
            textView?.refresh()
            calcDurations()

            templateParent.childHasFinishedInitializing(this)
            doOnRefresh?.invoke()
            doOnRefresh = null
        }
    }

    override fun calcDurations() {
        super.calcDurations()

        durationIn = max(textView?.durationIn ?: 0, durationIn)
        durationOut = max(textView?.durationOut ?: 0, durationOut)

        duration = max(
            duration + media.delayBeforeEnd,
            durationIn + durationOut + media.delayBeforeEnd
        )

        afterCalcDurations(durationIn, durationOut, duration)
    }

    override fun afterCalcDurations(durationIn: Int, durationOut: Int, durationTotal: Int) {

        val minDurationShortcut = resolveMinDurationShortcut()
        if (minDurationShortcut != null)
            duration = minDurationShortcut

        //set duration for other elements (synchronization)
        else if (parentDependsOnThisView()) {

            val parentGroup = parentInsp as InspGroupView

            //TODO: it is not the best place to do it for reasons:
            // 1. calcDurations uselessly called twice on init
            // 2. afterCalcDuration is called on refresh, so maybe dependent views haven't calculated their durations.
            // 3. Firebase reports StackOverflowException here. This can be recursive.
            fun setDelayBeforeEnd(it: InspView<*>) {
                if (it != this@InspTextView) {
                    val diffInDuration = duration - (it.duration)
                    it.media.delayBeforeEnd += diffInDuration

                    it.calcDurations()
                }
            }

            setDelayBeforeEnd(parentGroup)
            parentGroup.children.forEach(::setDelayBeforeEnd)
        }
    }

    override fun setPadding(
        layoutPosition: LayoutPosition,
        parentWidth: Int,
        parentHeight: Int,
        unitsConverter: BaseUnitsConverter
    ) {
        val additionalPad = templateParent.textsPadding

        textView?.setPadding(
            layoutPosition,
            parentWidth,
            parentHeight,
            unitsConverter,
            additionalPad
        )
    }

    private fun socialIconAutoResize() {
        val icon = findParentIfDependsOnItNotLinked()?.socialIconOrNull ?: return
        val scale = maxOf(userScaleX, userScaleY * SOCIAL_ICON_AUTO_SIZE_RATIO)
        with(icon) {
            userScaleX = scale
            userScaleY = scale
            applyScaleX()
            applyScaleY()
            animationHelper?.resetLastTimeTriggeredEnd()
            animationHelper?.preDrawAnimations(currentFrame)
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldW: Int, oldH: Int) {
        super.onSizeChanged(w, h, oldW, oldH)

        if (media.isCircularText() && oldW != 0 && oldH != 0) {
            textView?.updateCircularTextRadius()
            textView?.updateCircularTextSize(
                unitsConverter.convertUnitToPixelsF(
                    media.textSize,
                    parentWidth,
                    parentHeight
                )
            )
        }

        if (isSelectedForEdit) {
            if (media.layoutPosition.isInWrapContentMode() && oldW != 0 && oldH != 0) {
                val deltaX = w / oldW.toFloat()
                val deltaY = h / oldH.toFloat()
                userScaleX *= deltaX
                userScaleY *= deltaY
            }
            socialIconAutoResize()
        }
    }

    private fun mayReplaceFontIfCyrillic(fontData: FontData?) {

        if (fontData != null) {
            if (fontsManager.mayReplaceFontIfCyrillic(media.text, fontData)) {
                textView?.setFont(fontData)
            }
        }
    }

    fun doForDuplicates(action: (InspTextView) -> Unit) {
        if (media.duplicate == null) {
            templateParent.allTextViews.filter {
                it.isDuplicate() && it.media.duplicate == media.id
            }.forEach {
                action(it)
            }
        }
    }

    fun setNewText(text: String?) {

        doForDuplicates {
            it.setNewText(text)
        }

        if (text.isNullOrBlank()) removeThisView()
        else {
            val newText = if (media.isCircularText()) text.replace("\n", "") else text
            media.text = newText
            mayReplaceFontIfCyrillic(media.font)
            textView?.text = newText
            calcDurations()
            invalidateParentIfTexture()
        }
    }


    override fun maySwitchToAutosizeMode() {
        if (media.layoutPosition.width == "wrap_content" || media.layoutPosition.height == "wrap_content") {
            textView?.switchToAutoSizeMode()
        }
    }

    private fun maySwitchToWrapContentMode() {
        if (media.layoutPosition.width != "wrap_content" && media.layoutPosition.height != "wrap_content") {

            media.layoutPosition.width = "wrap_content"
            media.layoutPosition.height = "wrap_content"

            textView?.switchToWrapContentMode()
        }
    }

    override fun onSelectedChange(isSelected: Boolean) {

        if (isSelected) {
            currentFrame = getStaticFrameForEdit()

        } else if (templateParentNullable != null) {
            currentFrame = templateParent.currentFrame

        }
    }

    override fun getStaticFrameForEdit() =
        max(textView?.durationIn ?: 0, durationIn) + getStartFrameShortCut()


    fun toggleInnerTextGravity() {
        val newAlignment = media.innerGravity.toggle()
        textView?.onTextAlignmentChange(newAlignment)
        media.innerGravity = newAlignment
        templateParent.isChanged.value = true
        invalidateParentIfTexture()
    }

    private var _textBackground: TextBackground? = null
    private val textBackground: TextBackground
        get() {
            if (_textBackground == null) _textBackground = TextBackground.create(this)
            return _textBackground!!
        }

    fun isSimpleTextBackground() = textBackground is SimpleTextBackground
    fun getCurrentBackground(layer: Int = 1): AbsPaletteColor? = textBackground.getBackground(layer)

    val backgroundAlpha: Float
        get() = textBackground.getAlpha()

    fun setAlphaToBackground(value: Float) {
        templateParent.isChanged.value = true
        textBackground.setAlphaToBackground(value)
    }

    private fun maySetColorForBrothers() {

        val gr = findParentIfDependsOnIt()

        gr?.media?.medias?.filter { it is MediaPath && it.colorAsTextBrother }?.forEach {
            val pathView = it.view as? InspPathView?
            if (pathView != null) {
                if (media.textGradient != null)
                    pathView.setNewGradient(media.textGradient!!)
                else {
                    pathView.media.gradient = null
                    pathView.setNewColor(media.textColor)
                }
            } else {
                val mediaPath = it as MediaPath
                mediaPath.color = media.textGradient?.getFirstColor() ?: media.textColor
                mediaPath.gradient = media.textGradient
            }
        }
    }

    fun setNewTextGradient(gradient: PaletteLinearGradient) {
        if (media.isCircularText() && gradient.orientation != GradientOrientation.BOTTOM_TOP) gradient.orientation =
            GradientOrientation.BOTTOM_TOP
        media.textColor = gradient.getFirstColor()
        media.textGradient = gradient
        textView?.onColorChanged()

        maySetColorForBrothers()
        invalidateParentIfTexture()
        notifyColorChanged()
    }

    fun setNewTextColor(color: Int) {

        media.textColor = color
        media.textGradient = null

        textView?.setNewTextColor(color)

        maySetColorForBrothers()
        invalidateParentIfTexture()

    }

    private fun updatePaletteColor(colorType: ColorType, color: Int) {
        media.mediaPalette?.let {
            it.setLinkedColor(colorType, color)
            if (colorType.name.contains("ANIMATION")) (animationHelper as CommonAnimationHelper).notifyAnimationParameterChanged()
        }
    }

    private fun updatePaletteColorByLayer(layer: Int, color: Int) {
        media.mediaPalette?.choices?.let {
            it[layer].color = color

            //There are no such cases yet, but this will fix possible bugs in the future.
            if (it[layer].elements.contains(ColorType.STROKE_COLOR.name)) setNewStrokeColor(color)
            if (it[layer].elements.contains(ColorType.SHADOW_COLOR.name)) setNewShadowColor(color)
            if (it[layer].elements.contains(ColorType.BACK_COLOR.name)) setNewBackgroundColor(color)
            if (it[layer].elements.contains(ColorType.MAIN_COLOR.name)) setNewTextColor(color)

        }
    }


    private fun setNewShadowColor(color: Int) {
        media.textShadowColor = color
        updatePaletteColor(ColorType.SHADOW_COLOR, color)
        invalidateParentIfTexture()
    }

    private fun setNewStrokeColor(color: Int) {
        media.textStrokeColor = color
        updatePaletteColor(ColorType.STROKE_COLOR, color)
        invalidateParentIfTexture()
    }

    private fun setOneOfShadowColors(shadowColorIndex: Int, color: Int) {
        media.shadowColors?.let { it[shadowColorIndex] = color }
    }

    private fun notifyColorChanged() {
        templateParent.let {
            it.template.palette.resetPaletteChoiceColor(media.id, true)
            it.isChanged.value = true
        }
    }

    override fun setNewBackgroundGradient(gradient: PaletteLinearGradient) {
        notifyColorChanged()
        templateParent.isChanged.value = true
        textBackground.onUserChange()
        textBackground.useGradientBackground(gradient)
    }

    override fun setNewBackgroundColor(color: Int) {
        textBackground.useColorBackground(color)

    }

    fun userChangeBackgroundColorForLayer(color: Int, layer: Int) {
        notifyColorChanged()
        if (templateParent.isChanged.value) {
            textBackground.onUserChange()
            templateParent.template.palette.resetPaletteChoiceColor(media.id, true)
        }
        textBackground.useColorBackground(color, layer)

    }

    override fun scaleMargin(scaleX: Float?, scaleY: Float?) {
        super.scaleMargin(scaleX, scaleY)
        if (isSelectedForEdit) findParentIfDependsOnItNotLinked()?.socialIconOrNull?.scaleMargin(
            scaleX = userScaleX,
            scaleY = userScaleY
        )
    }

    fun setNewTextSize(convertTextSize: Float) {
        doForDuplicates {
            it.setNewTextSize(convertTextSize)
        }
        if (!media.isCircularText()) maySwitchToWrapContentMode()
        media.textSize = (convertTextSize / templateParent.viewWidth).toString() + "m"
        textView?.textSize = convertTextSize
        textView?.onColorChanged()
        templateParent.isChanged.value = true
        invalidateParentIfTexture()
    }

    fun setNewLetterSpacing(spacing: Float) {
        doForDuplicates {
            it.setNewLetterSpacing(spacing)
        }
        media.letterSpacing = spacing
        if (!media.isCircularText()) textView?.letterSpacing = spacing
        textView?.onColorChanged()

        templateParent.isChanged.value = true
        invalidateParentIfTexture()
    }

    fun setNewLineSpacing(spacing: Float) {
        doForDuplicates {
            it.setNewLineSpacing(spacing)
        }
        if (media.isCircularText()) return
        media.lineSpacing = spacing
        textView?.setLineSpacing(spacing)
        textView?.onColorChanged()
        templateParent.isChanged.value = true
        invalidateParentIfTexture()
    }


    fun onCapsModeChanged(text: String) {
        doForDuplicates {
            it.onCapsModeChanged(text)
        }
        //I have no idea why, but after first time isAllCaps doesn't work

        media.text = text
        textView?.text = media.text
        templateParentNullable?.isChanged?.value = true

        //it doesn't work without this ugly hack.
        if (media.textureIndex != null) {
            invalidateParentIfTexture(0, instantly = false)
            invalidateParentIfTexture(50, instantly = true)
        }
    }

    fun onFontChanged(fontData: FontData?) {
        doForDuplicates {
            it.onFontChanged(fontData)
        }
        media.font = fontData
        textView?.setFont(fontData)
        templateParentNullable?.isChanged?.value = true
        invalidateParentIfTexture()
    }


    //we calc duration in a usual way, with one exception - we remove delayBeforeEnd and we remove start times of outAnimators
    override fun getMinPossibleDuration(includeDelayBeforeEnd: Boolean): Int {

        val durationIn = media.animatorsIn.maxByReturnMax { it.duration + it.startFrame } ?: 0
        val allDuration = media.animatorsAll.maxByReturnMax { it.duration + it.startFrame } ?: 0

        val innerTextDuration = textView?.calcDurations(false)

        val totalInDuration = max(innerTextDuration?.first ?: 0, durationIn)


        val durationOut = media.animatorsOut.maxByReturnMax { it.duration } ?: 0
        val totalOutDuration = max(innerTextDuration?.second ?: 0, durationOut)

        //we dont consider media.minDuration here.
        var duration = max(durationIn + durationOut, allDuration)
        duration = max(duration, totalInDuration + totalOutDuration)
        return duration
    }

    private fun calibrateDuration(newDuration: Int): Triple<Int, Int, Int> {
        val minimalPossibleDuration = getMinPossibleDuration(true)
        val maxPossibleDuration = templateParent.getDuration() - getStartFrameShortCut()

        var newDurationCalibrated = max(minimalPossibleDuration, newDuration)
        newDurationCalibrated = min(maxPossibleDuration, newDurationCalibrated)

        return Triple(newDurationCalibrated, maxPossibleDuration, minimalPossibleDuration)
    }

    /**
     * durationInner = durationIn + durationOut.
     */
    fun setTextDuration(newDuration: Int): Triple<Int, Int, Int> {
        doForDuplicates { it.setTextDuration(newDuration) }
        val calibrated = calibrateDuration(newDuration)
        GlobalLogger.debug("timeline") { "set duration ${media.id} $newDuration calibrated $calibrated" }
        /*K.i("text-anim") {
            "setTextDuration $newDuration, attachToTheEnd ${duration == calibrated.second} " +
                    "current ${duration}, " +
                    "calibrated = $calibrated startTime = ${media.startFrame}" +
                    " minPossibleDuration ${getMinPossibleDuration()}" +
                    " maxDuration = ${templateParent.getDuration() - media.startFrameRemoveShortcut()}" +
                    " templateDuration = ${templateParent.getDuration()}"
        }*/

        if (duration != calibrated.first) {
            if (media.startFrame < 0)
                media.startFrame = getStartFrameShortCut()

            //attach to the end
            if (calibrated.first == calibrated.second) {
                media.minDuration = Media.MIN_DURATION_AS_TEMPLATE
                media.delayBeforeEnd = 0
            }
            //remove minDuration if it was present
            else {
                media.minDuration = 0
                media.delayBeforeEnd = calibrated.first - calibrated.third
            }
            calcDurations()
        }

        return calibrated
    }

    /**
     * Tripple<Duration, StartTime, TemplateFrameForEdit>
     */
    fun getTextAnimationTiming(): Triple<Int, Int, Int> {

        val templateDuration = templateParent.getDuration()
        if (templateDuration == 0) {
            return Triple(0, 0, 0)
        }

        var newTextDuration = templateDuration - durationIn
        newTextDuration = max(newTextDuration, TemplateUtils.NEW_TEXT_MIN_FRAMES)
        newTextDuration = min(newTextDuration, templateDuration)

        val timeAfterAllInAnimations = templateParent.getFrameForEdit(this.media)

        var newStartTime = timeAfterAllInAnimations - durationIn
        if (newStartTime + newTextDuration > templateDuration)
            newStartTime = templateDuration - newTextDuration

        return Triple(newTextDuration, max(0, newStartTime), timeAfterAllInAnimations)
    }

    /**
     * We make this text appear at the same time as template appears
     */
    fun setTextAnimationTiming() {
        val (newDuration, newStartTime, timeAfterAllAnimations) = getTextAnimationTiming()

        setTextDuration(newDuration)
        if (newStartTime > 0)
            setNewStartTime(newStartTime, timeAfterAllAnimations)
    }


    private fun setNewStartTime(
        newStartTime: Int,
        timeAfterAllInAnimations: Int = templateParent.getFrameForEdit(this.media)
    ) {

        if (parentDependsOnThisView()) {

            val newStartTime = if (newStartTime < 0) 0 else newStartTime

            val parentGroup = parentInsp as InspGroupView
            parentGroup.media.startFrame += newStartTime
            parentGroup.currentFrame = timeAfterAllInAnimations

            parentGroup.children.forEach {
                it.media.startFrame += newStartTime
                it.currentFrame = timeAfterAllInAnimations
            }
        } else {
            media.startFrame = newStartTime
            currentFrame = timeAfterAllInAnimations
        }
    }

    private fun framesPlusDeltaTimeMillis(frames: Int, delta: Double) =
        (((frames * FRAME_IN_MILLIS) + delta) / FRAME_IN_MILLIS).roundToInt()

    fun changeStartTimeDurationForGroup(deltaStartTime: Double, deltaDuration: Double) {

        if (parentDependsOnThisView()) {

            val parentGroup = parentInsp as InspGroupView
            parentGroup.media.startFrame =
                framesPlusDeltaTimeMillis(parentGroup.media.startFrame, deltaStartTime)
            parentGroup.media.delayBeforeEnd += framesPlusDeltaTimeMillis(
                parentGroup.media.delayBeforeEnd,
                deltaDuration
            )
            parentGroup.calcDurations()

            parentGroup.children.forEach { inspView ->
                if (inspView != this@InspTextView && !inspView.isDuplicate()) {

                    inspView.media.startFrame =
                        framesPlusDeltaTimeMillis(parentGroup.media.startFrame, deltaStartTime)
                    inspView.media.delayBeforeEnd += framesPlusDeltaTimeMillis(
                        parentGroup.media.delayBeforeEnd,
                        deltaDuration
                    )
                    inspView.calcDurations()

                    /*K.i("changeStartDurationForGroup") {
                        "startTime = ${media.startFrame}, duration = $duration. " +
                                "element startTime = ${inspView.media.startFrame}, duration = ${inspView.duration}"
                    }*/
                }
            }
        }
    }

    override fun isDuplicate(): Boolean {
        return media.duplicate != null
    }

    override fun refreshBackgroundColor() {
        if (media.lackBackgroundLineColor()) {
            super.refreshBackgroundColor()
        }
    }

    override fun setInnerCornerRadius(radius: Float) {
        super.setInnerCornerRadius(radius)
        textView?.radius = radius
    }

    fun redrawTextAfterColorChange() {
        animationHelper?.notifyAnimationParameterChanged()
        animationHelper?.resetLastTimeTriggeredEnd()
        animationHelper?.preDrawAnimations(currentFrame)
        textView?.onColorChanged()
        maySetColorForBrothers()
        invalidateParentIfTexture()
    }

    fun setColorForLayer(layerID: Int, color: Int) {
        notifyColorChanged()
        var currentLayer = 0
        if (media.mediaPalette != null) {
            updatePaletteColorByLayer(layerID, color)
            redrawTextAfterColorChange()
            return
        }
        if (layerID == 0) {
            setNewTextColor(color)
            return
        }
        if (layerID == 1) {
            when {
                media.hasShadow() -> {
                    setNewShadowColor(color)
                    currentLayer++
                }
                media.hasMulticolorShadow() -> {
                    setOneOfShadowColors(layerID - 1, color)
                    currentLayer++
                }
                media.hasStrokeColor() -> {
                    setNewStrokeColor(color)
                    currentLayer++
                }
            }
            redrawTextAfterColorChange()
        }
        doForDuplicates {
            currentLayer++
            if (currentLayer == layerID) {
                it.setColorForLayer(0, color)
                if (it.media.textShadowColor != null) it.setColorForLayer(1, color)
            }
        }
    }

    fun getColorForLayer(layer: Int): Int {
        templateParent.allTextViews.filter { it.isDuplicate() }
            .forEachIndexed { i, it ->
                if (i == layer - 1) return it.media.getColorForLayer(0)
            }
        return media.getColorForLayer(layer)

    }

    fun getGradientForLayer(layer: Int): PaletteLinearGradient? = media.getGradientForLayer(layer)

    override fun gradientslayerCount(): Int {
        media.mediaPalette?.let { return if (it.bgImageOrGradientCanBeSet) it.choices.size else 0 }
        return 1
    }

    override fun colorLayerCount(): Int {
        media.mediaPalette?.choices?.let { return it.size }
        var layersCount = 1
        media.textShadowColor?.let { layersCount++ }
        media.textStrokeColor?.let { layersCount++ }
        media.shadowColors?.let { layersCount += it.size }
        doForDuplicates {
            layersCount++
        }
        return layersCount
    }

    fun resetColor(layer: Int) {
        val defaults = media.defaults
        if (defaults == null && DebugManager.isDebug) throw IllegalStateException("it shouldn't be null here. (resetColor: currentState is null)")
        defaults?.let { def ->
            def.mediaPalette?.let { it ->
                setColorForLayer(
                    layer,
                    it.choices[layer].color ?: templateParent.template.palette.defaultTextColor
                )
                return
            }
            if (layer == 0) {
                def.textGradient?.let { gradient ->
                    setNewTextGradient(gradient)
                    return
                }
                setNewTextColor(def.textColor)
            }
            if (layer >= 1) {
                def.shadowColors?.let {
                    setColorForLayer(layer, it[layer - 1])
                    return
                }
                setColorForLayer(layer, def.textShadowColor ?: def.textStrokeColor ?: 0)
            }

        }

        var currentLayer = 1
        doForDuplicates {
            if (layer == currentLayer) {
                it.resetColor(0)
                if (it.media.hasShadow()) it.resetColor(1)
            }
            currentLayer++
        }

    }

    fun resetBackgroundColor(layer: Int) {
        textBackground.resetBackground(layer)
    }

    fun backColorsCount() = textBackground.colorLayersCount()
    fun backGradientCount(): Int = textBackground.gradientLayersCount()

    fun isLinesBackground(): Boolean {
        media.animationParamIn?.let {
            return (it.lineDelayMillis > 0.0 || it.charDelayMillis > 0.0 || it.wordDelayMillis > 0.0) && it.backgroundAnimatorGroups.isNotEmpty()
        }
        return false
    }

    fun userChangeRadius(newRadius: Float) {
        if (!isLinesBackground()) {
            val radiusAnimApplier = findRadiusAnimator(true)

            radiusAnimApplier?.to = newRadius
            radiusAnimApplier?.from = newRadius
        } else {
            media.radius = newRadius * 0.5f
            textView?.radius = newRadius * 0.5f
        }

        textView?.doOnInnerTextLayout {

            redrawTextAfterColorChange()

        }
    }

    private fun findRadiusAnimator(addIfNothing: Boolean = false): RadiusAnimApplier? {
        val backgroundMedia = (findParentIfDependsOnIt() ?: this).media
        val animation =
            backgroundMedia.animatorsIn.findLast { it.animationApplier is RadiusAnimApplier }?.animationApplier
                ?: return if (addIfNothing) {
                    val newAnimation = RadiusAnimApplier()

                    backgroundMedia.animatorsIn = backgroundMedia.animatorsIn.toMutableList().also {
                        it.add(
                            InspAnimator(animationApplier = newAnimation)
                        )
                    }
                    newAnimation
                } else null

        return animation as RadiusAnimApplier
    }

    fun getRoundness(): Float {
        return if (isLinesBackground()) (textView?.radius ?: 0f) / 0.5f else findRadiusAnimator()?.to ?: 0f
    }

    override fun releaseInner() {
        textView = null
        super.releaseInner()
    }

    fun mayInitDefaults() {
        rememberInitialColors()
    }

    override fun rememberInitialColors() {
        doForDuplicates {
            it.rememberInitialColors()
        }
        if (media.defaults == null) {
            val bg = textBackground.getBackground()
            media.defaults = MediaTextDefaults(
                mediaPalette = media.mediaPalette?.let {
                    MediaPalette(
                        it.isAvailable,
                        it.mainColor,
                        it.bgImageOrGradientCanBeSet,
                        mutableListOf(),
                        it.alpha
                    )
                },
                textShadowColor = media.textShadowColor,
                textStrokeColor = media.textStrokeColor,
                textColor = media.textColor,
                backgroundColor = if (bg != null && bg !is PaletteLinearGradient) bg.getFirstColor() else 0,
                shadowColors = media.shadowColors?.toMutableList(),
                textGradient = media.textGradient,
                backgroundGradient = if (bg is PaletteLinearGradient) bg else null,
            )
            media.mediaPalette?.choices?.forEach {
                media.defaults?.mediaPalette?.choices?.add(
                    MediaPaletteChoice(
                        it.color,
                        it.elements
                    )
                )
            }
        }

        textBackground.updateDefaults(media.defaults!!)
    }

    override fun restoreInitialColors(layer: Int, isBack: Boolean) {
        if (isBack) resetBackgroundColor(layer)
        else resetColor(layer)
    }
}
