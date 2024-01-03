package app.inspiry.core.animator.helper

import app.inspiry.core.animator.InspAnimator
import app.inspiry.core.animator.appliers.ClipAnimApplier
import app.inspiry.core.animator.appliers.MoveInnerAnimApplier
import app.inspiry.core.animator.clipmask.MaskProvider
import app.inspiry.core.animator.clipmask.logic.ClipMaskSettings
import app.inspiry.core.animator.clipmask.logic.ClipMaskType
import app.inspiry.core.animator.clipmask.shape.ShapeType

import app.inspiry.core.media.Media
import app.inspiry.core.media.MediaImage
import app.inspiry.core.media.MediaText
import app.inspiry.core.util.InspMathUtil.SIZE_MAX_VALUE
import app.inspiry.core.util.InspMathUtil.SIZE_MIN_VALUE
import app.inspiry.views.InspView
import app.inspiry.views.media.InspMediaView
import app.inspiry.views.template.TemplateMode
import kotlin.native.concurrent.ThreadLocal


//TODO: circular dependency with inspView
abstract class CommonAnimationHelper<CANVAS>(val media: Media):
    AbsAnimationHelper<CANVAS> {

    abstract var inspView: InspView<*>?

    override var animationTranslationX: Float = 0f
    override var animationTranslationY: Float = 0f
    override var animationRotation: Float = 0f
    override var circularOutlineClipRadiusDegree: Float? = null

    override val clipMaskSettings = ClipMaskSettings(maskType = ClipMaskType.NONE, shape = media.shape)
    override var maskProvider: MaskProvider? = null

//    abstract val drawAnimations: MutableList<() -> Unit>
//    abstract val resetAnimations: MutableList<InspAnimator>

    override fun resetLastTimeTriggeredEnd() {
        media.animatorsAll.forEach { it.lastFrameAnimTriggered = false }
        media.animatorsIn.forEach { it.lastFrameAnimTriggered = false }
        media.animatorsOut.forEach { it.lastFrameAnimTriggered = false }
    }

    //reset: Boolean, thisAnimOutBeginning: Int, cTime: Int
    private inline fun onIterateAnimatorsOut(
        currentTime: Int, totalViewDuration: Int, startTime: Int,
        onlyReset: Boolean,
        on: (Int, InspAnimator) -> Unit
    ) {
        for (it in media.animatorsOut) {
            val thisAnimOutBeginning = totalViewDuration - it.duration + startTime
            if (currentTime >= thisAnimOutBeginning) {
                if (!onlyReset)
                    on(thisAnimOutBeginning, it)
            } else {
                if (onlyReset)
                    on(0, it)
            }
        }
    }

    private fun transformPlaceholders() {
        inspView?.let { inspView ->
            //This is used in order to display placeholder images correctly
            if (inspView.mClipBounds != null && inspView.templateMode == TemplateMode.EDIT &&
                inspView is InspMediaView && (inspView.media as MediaImage).originalSource == null &&
                !inspView.media.isHasAnimApplier<MoveInnerAnimApplier>()
            ) {

                val clip = inspView.mClipBounds!!
                var translationX = 0
                if (clip.left != SIZE_MIN_VALUE && clip.right != SIZE_MAX_VALUE)
                    translationX =
                        clip.left - ((inspView.viewWidth - inspView.paddingRight) - clip.right)

                var translationY = 0
                if (clip.top != SIZE_MIN_VALUE && clip.bottom != SIZE_MAX_VALUE)
                    translationY =
                        clip.top - ((inspView.viewHeight - inspView.paddingBottom) - clip.bottom)

                (inspView as InspMediaView).setTranslateInner(
                    translationX.toFloat() / 2,
                    translationY.toFloat() / 2
                )
            }
        }
    }

    override fun notifyAnimationParameterChanged() {
        media.animatorsAll.forEach { it.updateAnimationValues(media) }
        media.animatorsIn.forEach { it.updateAnimationValues(media) }
        media.animatorsOut.forEach { it.updateAnimationValues(media) }
        if (media is MediaText) media.animationParamIn?.let {
            it.textAnimatorGroups.forEach { group ->
                group.animators.forEach { anim ->
                    anim.updateAnimationValues(media)
                }
            }
            it.backgroundAnimatorGroups.forEach { group ->
                group.animators.forEach { anim ->
                    anim.updateAnimationValues(media)
                }
            }
        }
    }

    override fun onSizeChanged() {
        inspView?.let { inspView ->
            resetLastTimeTriggeredEnd()
            maskProvider?.updateSize(inspView.viewWidth, inspView.viewHeight)
            maskProvider?.updateMask(maskSettings = clipMaskSettings)
            preDrawAnimations(inspView.currentFrame)
        }
    }
    override fun preDrawAnimations(currentFrame: Int) {
        inspView?.let { inspView ->
            drawAll(currentFrame, {
                it.animationApplier.onPreDraw(inspView, 0f)

            }, { animator, startFrame, newCurFrame ->

                animator.applyAnimation(newCurFrame, startFrame) {
                    animator.preDrawAnimation(inspView, it)
                }
            })

            transformPlaceholders()
        }
    }

    override fun prepareAnimation(frame: Int) {
        inspView?.let { inspView ->
            drawAll(frame, {
                it.animationApplier.onPrepared(inspView, 0f)

            }, { animator, startTime, newCurTime ->
                animator.applyAnimation(newCurTime, startTime, isPrepared = true) {
                    animator.prepareAnimation(inspView, it)
                }
            })
        }
    }


    private inline fun drawAll(
        originalCurrentFrame: Int, resetDraw: (InspAnimator) -> Unit,
        crossinline draw: (InspAnimator, Int, Int) -> Unit
    ) {
        val inspView = this.inspView ?: return
        if (!inspView.isAttached) return

        val startTime = inspView.getStartFrameShortCut()

        //1. Order is important
        //first we need to reset animations. Then to draw.
        //creation of new arrayList is necessary to prevent
        // concurrentModificationException


        //2. maybe loop animation
        var maybeLoopedCTime = originalCurrentFrame
        val totalViewDuration = inspView.duration

        val loopedAnimationInterval = media.loopedAnimationInterval
        if (loopedAnimationInterval != null) {

            val totalAnimationDuration = inspView.durationIn

            if (maybeLoopedCTime > totalAnimationDuration + startTime && maybeLoopedCTime < totalViewDuration + startTime) {

                val time = maybeLoopedCTime - startTime
                val cycleDuration = totalAnimationDuration + loopedAnimationInterval
                val currentCycle = (time / cycleDuration)
                maybeLoopedCTime -= currentCycle * cycleDuration
                //totalViewDuration = totalAnimationDuration
            }

            /*if (media.animatorsOut.isNotEmpty()) {

                K.i("CommonAnimHelper") {
                    "current ${originalCurrentFrame}, startTime ${startTime}, " +
                            "viewDuration $totalViewDuration, " +
                            "animDuration ${totalAnimationDuration}," +
                            " durationOut ${inspView.durationOut}, templateDuration ${inspView.templateParent.getDuration()}"
                }
            }*/
        }


        if (originalCurrentFrame >= startTime) {
            for (animator in media.animatorsAll) {
                if (originalCurrentFrame >= animator.startFrame + startTime) {

                    drawAnimations.add {
                        if (animator.duration < 0) {
                            val oldDuration = animator.duration

                            animator.duration = when (animator.duration) {
                                InspAnimator.DURATION_IN -> inspView.durationIn
                                InspAnimator.DURATION_ALL -> totalViewDuration
                                InspAnimator.DURATION_AS_TEMPLATE -> inspView.templateParent.getDuration() - animator.startFrame - startTime
                                else -> {
                                    inspView.templateParent.getDuration() -
                                            animator.startFrame - startTime + animator.duration
                                }
                            }

                            draw(animator, startTime + animator.startFrame, originalCurrentFrame)

                            animator.duration = oldDuration

                        } else {
                            draw(animator, startTime + animator.startFrame, originalCurrentFrame)
                        }
                    }

                } else {
                    resetAnimations.add(animator)
                    resetDraw(animator)
                }
            }

            for (it in media.animatorsIn) {
                if (maybeLoopedCTime >= it.startFrame + startTime) {

                    drawAnimations.add {
                        draw(it, startTime + it.startFrame, maybeLoopedCTime)
                    }

                } else {
                    resetAnimations.add(it)
                }
            }

        } else {
            for (it in media.animatorsAll) {
                resetAnimations.add(it)
            }
            for (it in media.animatorsIn) {
                resetAnimations.add(it)
            }

            resetLastTimeTriggeredEnd()
        }

        onIterateAnimatorsOut(
            originalCurrentFrame,
            totalViewDuration,
            startTime,
            true
        ) { thisTimeOutBeginning, animator ->
            resetDraw(animator)
        }

        resetAnimations.sortWith(resetAnimationsComparator)

        for (anim in resetAnimations) {
            resetDraw(anim)
        }

        for (drawAnimation in drawAnimations) {
            drawAnimation()
        }

        onIterateAnimatorsOut(
            originalCurrentFrame,
            totalViewDuration,
            startTime,
            false
        ) { thisTimeOutBeginning, animator ->
            draw(animator, thisTimeOutBeginning, originalCurrentFrame)
        }

        drawAnimations.clear()
        resetAnimations.clear()
    }

    override fun setClipMask(
        maskType: ClipMaskType,
        x: Float,
        y: Float,
        radius: Float,
        viewWidth: Float,
        viewHeight: Float,
        inverse: Boolean,
        progress: Float,
        count: Int,
        direction: ClipAnimApplier.Direction,
        reflection: Boolean
    ) {
        clipMaskSettings.apply {
            this.maskType = maskType
            this.x = x
            this.y = y
            this.radius = radius
            this.viewWidth = viewWidth
            this.viewHeight = viewHeight
            this.inverse = inverse
            this.progress = progress
            this.count = count
            this.direction = direction
            this.reflection = reflection
        }
        if (hasClipPath()) {
            mayInitMaskProvider()
            maskProvider?.updateMask(maskSettings = clipMaskSettings)
        }
    }

    override fun hasClipPath(): Boolean {
        return clipMaskSettings.maskType != ClipMaskType.NONE || (clipMaskSettings.shape != null && clipMaskSettings.shape != ShapeType.NOTHING)
    }

    override fun shapeTransform(
        xOffset: Float?,
        yOffset: Float?,
        scaleWidth: Float?,
        scaleHeight: Float?,
        rotation: Float?
    ) {
        mayInitMaskProvider()
        clipMaskSettings.shapeTransform.apply {
            xOffset?.let { this.xOffset = it }
            yOffset?.let { this.yOffset = it }
            scaleWidth?.let { this.scaleWidth = it }
            scaleHeight?.let { this.scaleHeight = it }
            rotation?.let { this.rotation = it }
        }
        if (clipMaskSettings.shape != null) {
            maskProvider?.updateMask(maskSettings = clipMaskSettings)
        }
    }
    val drawAnimations: MutableList<() -> Unit> = _drawAnimations

    val resetAnimations: MutableList<InspAnimator> = _resetAnimations

    override fun hasAnimatedClipMask(): Boolean {
        return clipMaskSettings.maskType != ClipMaskType.NONE
    }

    companion object {
        val resetAnimationsComparator = Comparator<InspAnimator> { o1, o2 ->
            o2.startFrame.compareTo(o1.startFrame)

        }
    }
}

@ThreadLocal
private val _drawAnimations: MutableList<() -> Unit> = mutableListOf()
@ThreadLocal
private val _resetAnimations: MutableList<InspAnimator> = mutableListOf()