package app.inspiry.onboarding

import app.inspiry.core.analytics.AnalyticsManager
import app.inspiry.core.analytics.putBoolean
import app.inspiry.core.analytics.putString
import app.inspiry.core.log.KLogger
import app.inspiry.core.log.LoggerGetter
import com.russhwolf.settings.Settings
import dev.icerock.moko.resources.StringResource

class OnBoardingQuizSender(private val stringToEnLocale: (StringResource) -> String, loggerGetter: LoggerGetter) {

    private var firstQuizSelectedIndex: Int? = null
    private var secondQuizIndexes: List<Int>? = null
    private var secondQuizSuggest: String = ""

    private val logger: KLogger = loggerGetter.getLogger("OnBoardingQuizSender")

    fun onFirstQuizSelected(index: Int) {
        this.firstQuizSelectedIndex = index
    }

    fun onSecondQuizSelected(index: List<Int>, textSuggestion: String) {
        this.secondQuizIndexes = index
        this.secondQuizSuggest = textSuggestion
    }


    private fun StringResource.process(): String {
        return stringToEnLocale(this).lowercase().replace(' ', '_')
    }

    fun addParamsToAnalyticsQuizFinished(
        mutableMap: MutableMap<String, Any?>,
        settings: Settings, analyticsManager: AnalyticsManager, datas: List<OnBoardingDataQuiz>
    ) {

        logger.info { "addParamsToAnalyticsQuizFinished ${secondQuizSuggest}, indexes ${secondQuizIndexes}" }
        if (datas.isEmpty()) throw IllegalStateException("unexpected datas size ${datas.size}")

        with(mutableMap) {
            firstQuizSelectedIndex?.let {
                val purposeOfUsageInspiry = datas[0].choices[it].process()
                putString(KEY_PURPOSE_OF_USAGE_INSPIRY, purposeOfUsageInspiry)

                analyticsManager.setUserProperty(
                    KEY_PURPOSE_OF_USAGE_INSPIRY,
                    purposeOfUsageInspiry
                )
                settings.putString(KEY_PURPOSE_OF_USAGE_INSPIRY, purposeOfUsageInspiry)
            }

            if (secondQuizSuggest.isNotBlank()) {
                putString("area_of_usage_suggest", secondQuizSuggest)
            }

            secondQuizIndexes?.let { selectedIndexes ->

                val choices = datas[1].choices
                for ((index, choice) in choices.withIndex()) {
                    val name = choice.process()

                    putBoolean("area_$name", index in selectedIndexes)
                }
            }
        }
    }

    override fun toString(): String {
        return "OnBoardingQuizSender(firstQuizSelectedIndex=$firstQuizSelectedIndex, secondQuizIndexes=${secondQuizIndexes?.toList()}, secondQuizSuggest='$secondQuizSuggest')"
    }


    companion object {
        const val KEY_PURPOSE_OF_USAGE_INSPIRY = "purpose_of_usage_inspiry"
    }
}