package app.inspiry.edit.instruments.moveAnimPanel

import app.inspiry.core.analytics.AnalyticsManager
import app.inspiry.core.animator.InspAnimator
import app.inspiry.core.animator.appliers.FadeAnimApplier
import app.inspiry.core.animator.appliers.MoveAnimApplier
import app.inspiry.core.animator.appliers.ScaleOuterAnimApplier
import app.inspiry.core.animator.interpolator.InspInterpolator
import app.inspiry.core.helper.PlayTemplateFlow
import app.inspiry.core.log.GlobalLogger
import app.inspiry.core.util.createDefaultScope
import app.inspiry.edit.instruments.BottomInstrumentsViewModel
import app.inspiry.views.InspView
import app.inspiry.views.media.InspMediaView
import kotlinx.coroutines.flow.MutableStateFlow
import app.inspiry.edit.instruments.moveAnimPanel.MoveAnimations.*
import app.inspiry.views.template.TemplateMode
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onCompletion

class MoveAnimInstrumentModel(
        val inspView: InspMediaView,
        val analyticsManager: AnalyticsManager,
    ) : BottomInstrumentsViewModel {

        val currentView = MutableStateFlow(inspView)

        override fun onSelectedViewChanged(newSelected: InspView<*>?) {
            currentView.value = newSelected as? InspMediaView ?: return
        }

        private var playingJob: Job? = null
        private var scope: CoroutineScope? = null
        var onFrameChanged: ((isFinished: Boolean) -> Unit)? = null //for ios redraw

        fun selectAnimation(anim: MoveAnimations?)  {
            val templateParent = currentView.value.templateParent
            if (scope != null) {
                val frame = templateParent.getFrameForEdit()
                currentView.value.currentFrame = frame
                currentView.value.animationHelper?.preDrawAnimations(frame)
                onFrameChanged?.invoke(false)
            }
            scope?.cancel()
            scope = createDefaultScope()
            currentView.value.media.animatorsIn = predefinedAnimations[anim] ?: emptyList()
            currentView.value.calcDurations()
            playingJob = scope?.launch {
                PlayTemplateFlow.create(0, DefaultAnimationDuration + 1, false)
                    .cancellable()
                    .onCompletion {
                        playingJob = null
                        onFrameChanged?.invoke(true)
                    }
                    .catch {
                        playingJob = null
                    }
                    .collect {
                        currentView.value.currentFrame = it
                        currentView.value.animationHelper?.preDrawAnimations(it)
                        onFrameChanged?.invoke(it>=DefaultAnimationDuration)
                    }
            }
        }

        //for analytics only
        private var initial = getCurrentAnim()
        override fun onHide() {
            scope?.cancel()
            val templateParent = currentView.value.templateParent
            if (templateParent.templateMode == TemplateMode.EDIT) {
                templateParent.setFrameForEdit()
                onFrameChanged?.invoke(true)
            }
            val anim = getCurrentAnim()
            if (initial != anim) analyticsManager.onAnimationChanged(anim)
            super.onHide()

        }
        fun getCurrentAnim(): MoveAnimations {
            val anim = currentView.value.media.animatorsIn.lastOrNull() ?: return NONE
            predefinedAnimations.forEach {
                val currentAnim = it.value.last()
                if (currentAnim.animationApplier.subCompare(anim.animationApplier) && currentAnim.duration == anim.duration) return it.key
            }
            return NONE
        }
        fun getIcon(anim: MoveAnimations): String {
            return anim.icon()
        }

        companion object {
            private const val AdditionalFadeDuration = 15
            private const val DefaultAnimationDuration = 35
            private const val DefaultFadeInterpolator = "0.8,0,0.6,1"
            private const val DefaultAnimInterpolator = "0.2,0,0,1"

            val animationsList = MoveAnimations.values().toList() //ios feature

            val predefinedAnimations: Map<MoveAnimations, List<InspAnimator>> = mapOf(
                NONE to listOf(InspAnimator(duration = 1, animationApplier = FadeAnimApplier())),
                LEFT to listOf(
                    InspAnimator(duration = AdditionalFadeDuration, interpolator = InspInterpolator.pathInterpolatorBy(DefaultFadeInterpolator), animationApplier = FadeAnimApplier()),
                    InspAnimator(duration = DefaultAnimationDuration, interpolator = InspInterpolator.pathInterpolatorBy(DefaultAnimInterpolator), animationApplier = MoveAnimApplier(fromX = 0.3f))
                ),
                RIGHT to listOf(
                    InspAnimator(duration = AdditionalFadeDuration, interpolator = InspInterpolator.pathInterpolatorBy(DefaultFadeInterpolator),animationApplier = FadeAnimApplier()),
                    InspAnimator(duration = DefaultAnimationDuration, interpolator = InspInterpolator.pathInterpolatorBy(DefaultAnimInterpolator), animationApplier = MoveAnimApplier(fromX = -0.3f))
                ),
                UP to listOf(
                    InspAnimator(duration = AdditionalFadeDuration, interpolator = InspInterpolator.pathInterpolatorBy(DefaultFadeInterpolator),animationApplier = FadeAnimApplier()),
                    InspAnimator(duration = DefaultAnimationDuration, interpolator = InspInterpolator.pathInterpolatorBy(DefaultAnimInterpolator), animationApplier = MoveAnimApplier(fromY = 0.3f))
                ),
                DOWN to listOf(
                    InspAnimator(duration = AdditionalFadeDuration, interpolator = InspInterpolator.pathInterpolatorBy(DefaultFadeInterpolator),animationApplier = FadeAnimApplier()),
                    InspAnimator(duration = DefaultAnimationDuration, interpolator = InspInterpolator.pathInterpolatorBy(DefaultAnimInterpolator), animationApplier = MoveAnimApplier(fromY = -0.3f))
                ),
                ZOOMIN to listOf(
                    InspAnimator(duration = AdditionalFadeDuration, interpolator = InspInterpolator.pathInterpolatorBy(DefaultFadeInterpolator),animationApplier = FadeAnimApplier()),
                    InspAnimator(duration = DefaultAnimationDuration, interpolator = InspInterpolator.pathInterpolatorBy(DefaultAnimInterpolator), animationApplier = ScaleOuterAnimApplier(fromX = 3f, fromY = 3f))
                ),
                ZOOMOUT to listOf(
                    InspAnimator(duration = AdditionalFadeDuration, interpolator = InspInterpolator.pathInterpolatorBy(DefaultFadeInterpolator),animationApplier = FadeAnimApplier()),
                    InspAnimator(duration = DefaultAnimationDuration, interpolator = InspInterpolator.pathInterpolatorBy(DefaultAnimInterpolator), animationApplier = ScaleOuterAnimApplier(fromX = 0.01f, fromY = 0.01f))
                ),
                FADE to listOf(
                    InspAnimator(duration = 15, interpolator = InspInterpolator.pathInterpolatorBy("1,0,0.8,1"), animationApplier = FadeAnimApplier())
                ),
            )
        }
    }