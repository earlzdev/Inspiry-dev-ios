package app.inspiry.onboarding

import app.inspiry.MR
import app.inspiry.core.analytics.AnalyticsManager
import app.inspiry.core.analytics.putBoolean
import app.inspiry.core.analytics.putString
import app.inspiry.core.animator.ANIMATOR_GROUP_ALL
import app.inspiry.core.animator.InspAnimator
import app.inspiry.core.animator.TextAnimationParams
import app.inspiry.core.animator.TextAnimatorGroups
import app.inspiry.core.animator.appliers.FadeAnimApplier
import app.inspiry.core.animator.appliers.MoveToXAnimApplier
import app.inspiry.core.animator.appliers.MoveToYAnimApplier
import app.inspiry.core.animator.interpolator.InspInterpolator
import app.inspiry.core.data.FRAME_IN_MILLIS
import app.inspiry.core.log.KLogger
import app.inspiry.core.manager.LicenseManager
import app.inspiry.core.log.LoggerGetter
import app.inspiry.core.media.*
import app.inspiry.font.model.FontData
import app.inspiry.font.model.InspFontStyle
import app.inspiry.palette.model.PaletteLinearGradient
import app.inspiry.removebg.RemovingBgColors
import app.inspiry.removebg.RemovingBgDimens
import app.inspiry.views.template.InspTemplateView
import com.russhwolf.settings.Settings
import dev.icerock.moko.resources.StringResource
import dev.icerock.moko.resources.desc.StringDesc

open class OnBoardingViewModel(
    private val settings: Settings,
    private val licenseManger: LicenseManager,
    private val analyticsManager: AnalyticsManager,
    private val quizSender: OnBoardingQuizSender?,
    private val type: OnBoardingType,
    loggerGetter: LoggerGetter,
    private var onFinish: () -> Unit,
    private var onOpenSubscribe: (source: String) -> Unit
) {
    val pagesData: List<OnBoardingData> = getAllPagesData(type)

    private val logger: KLogger = loggerGetter.getLogger("OnBoardingViewModel")

    lateinit var step: (() -> Int)
    lateinit var nextStep: () -> Unit
    lateinit var prevStep: () -> Unit

    fun doOnFinish(action: () -> Unit) {
        onFinish = action
    }

    fun doOnSubscribe(action: (String) -> Unit) {
        onOpenSubscribe = action
    }

    private val quizBig by lazy {
        OnBoardingDataQuiz(
            MR.strings.onboarding_quiz_2_title,
            choices = listOf(
                MR.strings.instrument_music,
                MR.strings.onboarding_quiz_2_option_2,
                MR.strings.onboarding_quiz_2_option_3,
                MR.strings.category_beauty,
                MR.strings.onboarding_quiz_2_option_5,
                MR.strings.onboarding_quiz_2_option_6,
                MR.strings.onboarding_quiz_2_option_7,
                MR.strings.onboarding_quiz_2_option_8,
                MR.strings.onboarding_quiz_2_option_9,
            ),
            singleChoice = false
        )
    }

    private fun getAllPagesData(type: OnBoardingType): List<OnBoardingData> {
        val list: MutableList<OnBoardingData> = getVideoPagesData()

        when (type) {
            OnBoardingType.QUIZ_AFTER_SHORT -> {
                list.addAll(getQuizPagesData())
            }
            OnBoardingType.BASIC -> {
            }
        }
        return list
    }

    init {
        licenseManger.restorePurchases()
    }

    fun onCreateFirst() {
        analyticsManager.onboardingOpen(type.name)
    }

    fun onClickContinue() {
        val step = step.invoke()

        if (step >= pagesData.size - 1) {
            finishOnboarding(fromBackPress = false)
        } else {
            nextStep()
        }
    }

    private fun finishOnboarding(fromBackPress: Boolean) {
        settings.putBoolean(KEY_ONBOARDING_FINISHED, true)

        logger.info { "finishOnboarding ${quizSender}" }

        analyticsManager.sendEvent("onboarding_finished") {
            putString("name", type.name)
            putBoolean("fromBackPress", fromBackPress)
            quizSender?.addParamsToAnalyticsQuizFinished(
                this,
                settings,
                analyticsManager,
                pagesData.filterIsInstance<OnBoardingDataQuiz>()
            )
        }

        if (licenseManger.hasPremiumState.value) {
            onFinish()
        } else {
            onOpenSubscribe(SOURCE_ONBOARDING + "_" + type)
        }
    }

    fun onBackPressed() {
        if (step.invoke() > 0) {
            prevStep()
        } else {
            finishOnboarding(fromBackPress = true)
        }
    }

    fun onFirstQuizSelected(index: Int) {
        quizSender!!.onFirstQuizSelected(index)
        onClickContinue()
    }

    fun onSecondQuizSelected(index: List<Int>, textSuggestion: String) {
        quizSender!!.onSecondQuizSelected(index, textSuggestion)
        onClickContinue()
    }

    enum class TitleAnimationType {
        WORDS, LINES_BOTTOM, LINES_RIGHT
    }

    enum class OnBoardingType {
        BASIC, QUIZ_AFTER_SHORT;

        val isQuiz: Boolean
            get() = this == QUIZ_AFTER_SHORT
    }

    companion object {
        const val KEY_ONBOARDING_FINISHED = "onboarding_finished"
        const val SOURCE_ONBOARDING = "onboarding"

        fun create(
            settings: Settings,
            licenseManager: LicenseManager,
            analyticsManager: AnalyticsManager,
            loggerGetter: LoggerGetter,
            stringToEnLocale: (StringResource) -> String,
            onFinish: () -> Unit,
            onOpenSubscribe: (source: String) -> Unit
        ): OnBoardingViewModel {
            val onboardingType: OnBoardingType = OnBoardingType.QUIZ_AFTER_SHORT

            val quizSender: OnBoardingQuizSender?

            if (onboardingType.isQuiz) {
                quizSender = OnBoardingQuizSender(stringToEnLocale, loggerGetter)
            } else {
                quizSender = null
            }

            return OnBoardingViewModel(
                settings,
                licenseManager,
                analyticsManager,
                quizSender,
                onboardingType,
                loggerGetter, onFinish, onOpenSubscribe
            )
        }

        fun initTemplate(templateView: InspTemplateView) {
            templateView.shouldHaveBackground = false
            templateView.loopAnimation = false
        }

        fun loadTemplate(
            templateView: InspTemplateView,
            title: String, textAlign: TextAlign,
            fontStyle: InspFontStyle,
            animType: TitleAnimationType,
            colors: OnBoardingColors
        ) {
            templateView.stopPlaying()
            templateView.loadTemplate(getTemplate(title, textAlign, fontStyle, animType, colors))
        }

        fun onVideoLooped(templateView: InspTemplateView) {
            templateView.stopPlaying()
            templateView.startPlaying()
        }

        fun getVideoPagesData(): MutableList<OnBoardingData> = mutableListOf(
            OnBoardingDataVideo(MR.assets.videos.onboarding.page_1, 1374, MR.strings.onboarding_text_1),
            OnBoardingDataVideo(MR.assets.videos.onboarding.page_2, 1500, MR.strings.onboarding_text_2)
        )

        fun getQuizPagesData(): List<OnBoardingDataQuiz> {

            return listOf(
                OnBoardingDataQuiz(
                    title = MR.strings.onboarding_quiz_1_title,
                    choices = listOf(
                        MR.strings.onboarding_quiz_1_option_1,
                        MR.strings.onboarding_quiz_1_option_2,
                        MR.strings.onboarding_quiz_1_option_3
                    ), singleChoice = true
                )
            )
        }

        fun getTemplate(
            title: String,
            textAlign: TextAlign,
            fontStyle: InspFontStyle,
            animType: TitleAnimationType,
            colors: OnBoardingColors
        ): Template {
            val mediaText = MediaText(
                text = title, textSize = "24sp",
                font = FontData(fontStyle = fontStyle),
                layoutPosition = LayoutPosition(LayoutPosition.MATCH_PARENT, LayoutPosition.WRAP_CONTENT, Alignment.center),
                textGradient = PaletteLinearGradient(
                    colors = listOf(
                        colors.textGradientStartColor.argb.toInt(),
                        colors.textGradientEndColor.argb.toInt()
                    )
                ),
                innerGravity = textAlign, minDuration = -10, startFrame = 5,
                animationParamIn = when (animType) {
                    TitleAnimationType.WORDS -> getWordsAnimationIn(wordsDelay = 6, inDuration = 10)
                    TitleAnimationType.LINES_BOTTOM -> getLinesAnimationIn1()
                    TitleAnimationType.LINES_RIGHT -> getLinesAnimationIn2()
                },
                animationParamOut = when (animType) {
                    TitleAnimationType.WORDS -> getWordsAnimationOut(wordsDelay = 6, outDuration = 10)
                    else -> null
                },
            )

            return Template(
                preferredDuration = Int.MAX_VALUE,
                medias = mutableListOf(mediaText)
            )
        }

        fun getLinesAnimationIn1() = TextAnimationParams(
            lineDelayMillis = 10 * FRAME_IN_MILLIS,
            textAnimatorGroups = listOf(
                TextAnimatorGroups(
                    group = ANIMATOR_GROUP_ALL,
                    animators = listOf(
                        InspAnimator(
                            duration = 12,
                            animationApplier = FadeAnimApplier(from = 0f, to = 1f)
                        ),
                        InspAnimator(
                            duration = 12,
                            interpolator = InspInterpolator.pathInterpolatorBy("flatIn25expOut"),
                            animationApplier = MoveToYAnimApplier(from = -0.5f, to = 0f)
                        )
                    )
                )
            )
        )

        private fun getLinesAnimationIn2() = TextAnimationParams(
            lineDelayMillis = 10 * FRAME_IN_MILLIS,
            textAnimatorGroups = listOf(
                TextAnimatorGroups(
                    group = ANIMATOR_GROUP_ALL,
                    animators = listOf(
                        InspAnimator(
                            duration = 12,
                            animationApplier = FadeAnimApplier(from = 0f, to = 1f)
                        ),
                        InspAnimator(
                            duration = 12,
                            interpolator = InspInterpolator.pathInterpolatorBy("flatIn25expOut"),
                            animationApplier = MoveToXAnimApplier(from = 0.3f, to = 0f)
                        )
                    )
                )
            )
        )


        fun getWordsAnimationIn(wordsDelay: Int, inDuration: Int) = TextAnimationParams(
            wordDelayMillis = wordsDelay * FRAME_IN_MILLIS,
            lineDelayMillis = wordsDelay * FRAME_IN_MILLIS,
            textAnimatorGroups = listOf(
                TextAnimatorGroups(
                    group = ANIMATOR_GROUP_ALL,
                    animators = listOf(
                        InspAnimator(
                            duration = inDuration,
                            animationApplier = FadeAnimApplier(from = 0f, to = 1f)
                        ),
                        InspAnimator(
                            duration = inDuration,
                            interpolator = InspInterpolator.pathInterpolatorBy("flatIn25expOut"),
                            animationApplier = MoveToYAnimApplier(from = -0.8f, to = 0f)
                        )
                    )
                )
            )
        )

        fun getWordsAnimationOut(wordsDelay: Int, outDuration: Int) = TextAnimationParams(
            wordDelayMillis = wordsDelay * FRAME_IN_MILLIS,
            lineDelayMillis = wordsDelay * FRAME_IN_MILLIS,
            textAnimatorGroups = listOf(
                TextAnimatorGroups(
                    group = ANIMATOR_GROUP_ALL,
                    animators = listOf(
                        InspAnimator(
                            duration = outDuration,
                            animationApplier = FadeAnimApplier(from = 1f, to = 0f)
                        ),
                        InspAnimator(
                            duration = outDuration,
                            interpolator = InspInterpolator.pathInterpolatorBy("cubicIn"),
                            animationApplier = MoveToYAnimApplier(from = 0f, to = 0.8f)
                        )
                    )
                )
            )
        )

        private fun getCharByCharAnimationIn() = TextAnimationParams(
            charDelayMillis = 3 * FRAME_IN_MILLIS,
            textAnimatorGroups = listOf(
                TextAnimatorGroups(
                    group = ANIMATOR_GROUP_ALL,
                    animators = listOf(
                        InspAnimator(
                            duration = 3,
                            animationApplier = FadeAnimApplier(from = 0f, to = 1f)
                        ),
                        InspAnimator(
                            duration = 3,
                            interpolator = InspInterpolator.pathInterpolatorBy("flatIn25expOut"),
                            animationApplier = MoveToXAnimApplier(from = 1f, to = 0f)
                        )
                    )
                )
            ),
            charDelayBetweenWords = false
        )

        fun getTemplateRemovingBg(text: String, colors: RemovingBgColors, dimens: RemovingBgDimens): Template {
            val mediaText = MediaText(
                text = text, textSize = dimens.animatingTextSize.toString() + "sp",
                font = FontData(fontPath = "hero", fontStyle = InspFontStyle.bold),
                layoutPosition = LayoutPosition(LayoutPosition.MATCH_PARENT, LayoutPosition.WRAP_CONTENT, Alignment.center),
                textGradient = PaletteLinearGradient(
                    colors = listOf(
                        colors.removingBgGradientStart.argb.toInt(),
                        colors.removingBgGradientEnd.argb.toInt()
                    )
                ),
                innerGravity = TextAlign.center,
                animationParamIn = getLinesAnimationIn1(),
                animationParamOut = getWordsAnimationOut(6, 10),
                delayBeforeEnd = 10
            )

            return Template(
                medias = mutableListOf(mediaText)
            )
        }
    }
}