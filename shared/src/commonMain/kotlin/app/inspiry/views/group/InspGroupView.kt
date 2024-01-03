package app.inspiry.views.group

import app.inspiry.core.animator.helper.AbsAnimationHelper
import app.inspiry.core.log.GlobalLogger
import app.inspiry.core.log.KLogger
import app.inspiry.core.log.LoggerGetter
import app.inspiry.core.media.BaseUnitsConverter
import app.inspiry.core.media.Media
import app.inspiry.core.media.MediaGroup
import app.inspiry.core.media.MediaImage
import app.inspiry.core.util.ArgbColorManager
import app.inspiry.palette.model.MediaPalette
import app.inspiry.views.InspParent
import app.inspiry.views.InspView
import app.inspiry.views.media.InspMediaView
import app.inspiry.views.path.InspPathView
import app.inspiry.views.slides.SlidesParent
import app.inspiry.views.template.InspTemplateView
import app.inspiry.views.touch.MovableTouchHelperFactory
import app.inspiry.views.vector.InspVectorView
import app.inspiry.views.viewplatform.ViewPlatform
import kotlin.math.max

open class InspGroupView(
    media: MediaGroup, parentInsp: InspParent?, view: ViewPlatform?,
    unitsConverter: BaseUnitsConverter,
    animationHelper: AbsAnimationHelper<*>?, loggerGetter: LoggerGetter,
    touchHelperFactory: MovableTouchHelperFactory, templateParent: InspTemplateView
) : InspView<MediaGroup>(
    media, parentInsp,
    view, unitsConverter, animationHelper, loggerGetter, touchHelperFactory,
    templateParent
), InspParent, SlidesParent {

    val children: MutableList<InspView<*>> = mutableListOf()

    val socialIconOrNull: InspView<*>?
        get() {
            return children.find { it.isSocialIcon() }
        }

    override val group: InspGroupView
        get() = this

    override val maxSlides: Int
        get() = media.slides?.maxCount ?: 0

    val hasSocialIcon: Boolean
        get() = socialIconOrNull != null

    override val logger: KLogger = loggerGetter.getLogger("InspGroupView")

    override val inspChildren: List<InspView<*>>
        get() = children

    override fun onCurrentFrameChanged(newVal: Int, oldVal: Int) {
        inspChildren.forEach {
            it.currentFrame = newVal
        }
        super.onCurrentFrameChanged(newVal, oldVal)
    }

    override fun releaseInner() {
        children.clear()
        super.releaseInner()
    }

    override fun addViewToHierarchy(view: InspView<*>) {
        children.add(view)
    }

    override fun addViewToHierarchy(index: Int, view: InspView<*>) {
        children.add(index, view)
    }

    fun showFirstIfSlide() {
        if (media.slides == null) return
        getSlidesViews().forEachIndexed { index, inspMediaView ->
            if (index>0) inspMediaView.hideView()
            else inspMediaView.showOnTopForEdit()
        }
    }

    override fun removeViewFromHierarchy(view: InspView<*>, removeFromTemplateViews: Boolean) {
        media.medias.remove(view.media)
        children.remove(view)
        if (removeFromTemplateViews) {
            templateParent.allViews.remove(view)
        }
    }

    override fun addMediaToList(media: Media) {
        this.media.medias.add(media)
    }

    override fun refresh() {
        super.refresh()

        calcDurations()
        afterCalcDurations(durationIn, durationOut, duration)

        templateParent.childHasFinishedInitializing(this)

    }

    open fun invalidateRedrawProgram(delay: Long = 0L, instantly: Boolean = false) {

    }

    override fun getSlidesCount(ignorePlaceHolder: Boolean): Int {
        return inspChildren.filterIsInstance<InspMediaView>()
            .filter { !it.isDuplicate() && (it.media.originalSource != null || !ignorePlaceHolder) }.size
    }

    override fun selectLastSlide() {
        getSlidesViews().last().setSelected()
        templateParent.setFrameForEdit()
    }

    override fun selectSlide(slideIndex: Int) {
        val size = getSlidesCount(true)
        if (size == 0) return
        val selectIndex = if ((size - 1) < slideIndex) slideIndex - 1 else slideIndex
        getSlidesViews().getOrNull(selectIndex)?.setSelected()
    }

    override fun selectFirstSlide() {
        getSlidesViews().firstOrNull()?.setSelected()
    }

    fun getCurrentAlpha(): Float {
        this.children.forEach { view ->
            when (view) {
                is InspPathView -> {
                    return view.media.alpha
                }

                is InspMediaView -> {
                    return view.media.alpha
                }
                is InspGroupView -> {
                    return view.getCurrentAlpha()
                }
            }

        }
        return 1f
    }

    fun getCurrentColor(): Int? {
        this.children.forEach { view ->
            when (view) {
                is InspPathView -> {
                    view.getCurrentColor()?.let { return it }
                }

                is InspMediaView -> {
                    if (view.media.hasBackground()) {
                        return view.media.backgroundColor
                    } else view.media.colorFilter?.let { return it }
                }
                is InspGroupView -> {
                    return view.getCurrentColor()
                }
            }

        }
        return null
    }

    /**
     * setting the color for a group sets the color of all child elements except text
     * maybe the text doesn't need to be excluded?
     */
    fun colorFilterForChilds(palette: MediaPalette) {
        templateParent.isChanged.value = true
        this.children.forEach { view ->
            when (view) {
                is InspVectorView -> {
                    view.applyPalette(palette)
                    templateParent.template.palette.resetPaletteChoiceColor(
                        view.media.id,
                        isText = false
                    )
                }
                is InspPathView -> {
                    view.rememberInitialColors()
                    view.setNewColor(
                        color = ArgbColorManager.colorWithAlpha(
                            palette.mainColor?.getFirstColor() ?: view.media.color ?: 0,
                            palette.alpha
                        ),
                    )
                    templateParent.template.palette.resetPaletteChoiceColor(
                        view.media.id,
                        isText = false
                    )
                }

                is InspMediaView -> {
                    view.rememberInitialColors()
                    if (view.media.hasBackground()) {
                        view.setNewBackgroundColor(
                            ArgbColorManager.colorWithoutAlpha(
                                palette.mainColor?.getFirstColor() ?: view.media.backgroundColor
                            )
                        )
                        view.setColorFilter(null, palette.alpha)
                    } else view.setColorFilter(
                        color = palette.mainColor?.getFirstColor(),
                        alpha = palette.alpha
                    )
                    templateParent.template.palette.resetPaletteChoiceColor(
                        view.media.id,
                        isText = false
                    )
                }
                is InspGroupView -> {
                    view.colorFilterForChilds(palette)
                }
            }

        }
    }

    override fun calculateLastFrame(): Int {
        var lastFrame = 0
        inspChildren.forEach {
            it.calcDurations()
            val duration = max(it.duration, it.durationIn)
            val childDuration =
                if (it is InspGroupView) it.calculateLastFrame() else (duration + it.getStartFrameShortCut())
            if (childDuration > lastFrame) lastFrame = childDuration
        }
        return lastFrame
    }

    override fun getSlidesMedia(): List<MediaImage> {
        return media.medias.filterIsInstance<MediaImage>().filter { it.duplicate == null }
    }

    override fun getSlidesViews(): List<InspMediaView> {
        return children.filterIsInstance<InspMediaView>().filter { it.media.duplicate == null }
    }

    fun resetColorFilterForChilds() {
        this.children.forEach { view ->
            when (view) {
                is InspMediaView -> {
                    view.setColorFilter(null, null)
                }
                is InspVectorView -> {
                    view.setColorFilter(null)
                }
                is InspPathView -> {
                    view.restoreInitialColors(0, false)
                }
                is InspGroupView -> {
                    view.resetColorFilterForChilds()
                }
            }
        }
    }

    fun drawTemplateTextureSync() {
        mayDrawOnGlCanvas(false)
    }

    //if return false then no gl drawing has happened. The group may draw itself in a usual way
    //if recording is true and fromOnDraw then return true instantly.

    open fun mayDrawOnGlCanvas(fromOnDraw: Boolean): Boolean {
        return false
    }

    override fun rememberInitialColors() {

    }

    override fun restoreInitialColors(layer: Int, isBack: Boolean) {

    }
}