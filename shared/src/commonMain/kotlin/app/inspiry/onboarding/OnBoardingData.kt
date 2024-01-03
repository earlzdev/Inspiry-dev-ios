package app.inspiry.onboarding

import dev.icerock.moko.resources.AssetResource
import dev.icerock.moko.resources.StringResource

sealed class OnBoardingData

class OnBoardingDataQuiz(
    val title: StringResource,
    val choices: List<StringResource>,
    val singleChoice: Boolean
): OnBoardingData()

open class OnBoardingDataVideo(val video: AssetResource, val videoHeight: Int, val text: StringResource) :
    OnBoardingData() {
    open val videoWidth: Int
        get() = 1080
}