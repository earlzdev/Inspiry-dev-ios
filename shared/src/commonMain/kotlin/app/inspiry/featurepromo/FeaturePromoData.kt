package app.inspiry.featurepromo

import app.inspiry.MR
import app.inspiry.core.media.*
import app.inspiry.core.serialization.MediaSerializer
import app.inspiry.font.model.FontData
import app.inspiry.font.model.InspFontStyle
import app.inspiry.onboarding.OnBoardingDataVideo
import app.inspiry.onboarding.OnBoardingViewModel
import app.inspiry.palette.model.PaletteLinearGradient
import dev.icerock.moko.resources.AssetResource
import dev.icerock.moko.resources.StringResource
import kotlinx.serialization.Serializable


class FeaturePromoData(
    video: AssetResource,
    videoHeight: Int,
    text: StringResource,
    val subtitle: StringResource,
    val id: String
) : OnBoardingDataVideo(video, videoHeight, text) {

    companion object {
        val removeBgPromoData: FeaturePromoData
            get() = FeaturePromoData(
                MR.assets.videos.promo.remove_back,
                videoHeight = 1920,
                text = MR.strings.remove_bg_promo_title,
                subtitle = MR.strings.remove_bg_promo_subtitle,
                id = "remove_bg"
            )

        inline fun removeBgPromoTemplate(
            colors: FeaturePromoColors,
            textToString: (StringResource) -> String
        ): Template {

            val mediaTopText = MediaText(
                layoutPosition = LayoutPosition(
                    LayoutPosition.MATCH_PARENT,
                    LayoutPosition.WRAP_CONTENT,
                    alignBy = Alignment.top_center
                ),
                font = FontData(fontPath = "gilroy", fontStyle = InspFontStyle.bold),
                textSize = "38sp",
                textGradient = PaletteLinearGradient(
                    colors = listOf(
                        colors.buttonGradientStart.argb.toInt(),
                        colors.buttonGradientEnd.argb.toInt()
                    )
                ),
                innerGravity = TextAlign.center,
                text = textToString(MR.strings.remove_bg_promo_title),
                startFrame = 10,
                animationParamIn = OnBoardingViewModel.getWordsAnimationIn(wordsDelay = 5, inDuration = 8),
                animationParamOut = OnBoardingViewModel.getWordsAnimationOut(wordsDelay = 5, outDuration = 8),
                delayBeforeEnd = 33
            )

            val mediaBottomText = MediaText(
                layoutPosition = LayoutPosition(
                    LayoutPosition.MATCH_PARENT,
                    LayoutPosition.WRAP_CONTENT,
                    alignBy = Alignment.top_center
                ),
                font = FontData(fontPath = "gilroy", fontStyle = InspFontStyle.bold),
                textSize = "24sp",
                textColor = colors.templateTextSubtitle.argb.toInt(),
                innerGravity = TextAlign.center,
                text = textToString(MR.strings.remove_bg_promo_subtitle),
                startFrame = 17,
                animationParamIn = OnBoardingViewModel.getWordsAnimationIn(wordsDelay = 5, inDuration = 8),
                animationParamOut = OnBoardingViewModel.getWordsAnimationOut(wordsDelay = 5, outDuration = 8),
                delayBeforeEnd = 33
            )

            val mediaGroup = MediaGroup(
                medias = mutableListOf(mediaTopText, mediaBottomText),
                layoutPosition = LayoutPosition(
                    LayoutPosition.MATCH_PARENT,
                    LayoutPosition.WRAP_CONTENT,
                    alignBy = Alignment.top_center
                ),
                orientation = GroupOrientation.V
            )


            return Template(
                preferredDuration = Int.MAX_VALUE,
                medias = mutableListOf(mediaGroup)
            )
        }
    }
}


