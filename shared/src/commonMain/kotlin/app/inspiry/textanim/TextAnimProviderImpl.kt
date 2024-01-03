package app.inspiry.textanim

import app.inspiry.MR
import app.inspiry.core.template.MediaReadWrite
import dev.icerock.moko.resources.AssetResource

class TextAnimProviderImpl(private val mediaReadWrite: MediaReadWrite) : TextAnimProvider {

    override fun getCategories(): List<String> =
        listOf("1title", "2caption", "3simple", "4brush", "5swipeup", "6social")

    override fun getAnimations(category: String): List<MediaWithRes> {
        val resources: List<AssetResource> = when (category) {
            "1title" -> {
                listOf(
                    MR.assets.texts.title.NoAnimation,
                    MR.assets.texts.title.FadeTextSlideLayout,
                    MR.assets.texts.title.LineFrame,
                    MR.assets.texts.title.FadeWordsLayout,
                    MR.assets.texts.title.TitleWhite,
                    MR.assets.texts.title.LinesFallingUnderline,
                    MR.assets.texts.title.LabelWideLayout,
                    MR.assets.texts.title.FadeLayout,
                    MR.assets.texts.title.OutlineLayout,
                    MR.assets.texts.title.WordsDelaySlideLayout,
                    MR.assets.texts.title.BlinkGlow,
                    MR.assets.texts.title.BlinkChars,
                    MR.assets.texts.title.Circular
                )
            }
            "2caption" -> {

                listOf(
                    MR.assets.texts.caption.LabelMultipleOffset,
                    MR.assets.texts.caption.LinesFastSlide,
                    MR.assets.texts.caption.FrameYellow,
                    MR.assets.texts.caption.LabelMultiColor,
                    MR.assets.texts.caption.FrameWhite,
                    MR.assets.texts.caption.ElegantText,
                    MR.assets.texts.caption.SkewTitleWithLine,
                    MR.assets.texts.caption.EvenGradientUnderline,
                    MR.assets.texts.caption.BlueRoundedBg,
                    MR.assets.texts.caption.ParagraphLayout,
                )
            }
            "3simple" -> {

                listOf(
                    MR.assets.texts.simple.ScaleFromSmall,
                    MR.assets.texts.simple.RandomAlpha,
                    MR.assets.texts.simple.ScaleFromBig,
                    MR.assets.texts.simple.Typewriter,
                    MR.assets.texts.simple.LinesFalling,
                    MR.assets.texts.simple.FromLeft,
                    MR.assets.texts.simple.FromRight,
                    MR.assets.texts.simple.WordsRaising,
                    MR.assets.texts.simple.FromBottom,
                    MR.assets.texts.simple.Blinking,
                    MR.assets.texts.simple.TextFromTop,
                    MR.assets.texts.simple.LongTypewriter,
                    MR.assets.texts.simple.FloatingFromBottomShadow,
                )
            }

            "4brush" -> {
                listOf(
                    MR.assets.texts.brush.BrushPurpleWhite,
                    MR.assets.texts.brush.BrushCoffeeBrown,
                    MR.assets.texts.brush.BrushILoveYou,
                    MR.assets.texts.brush.BrushMeToo,
                    MR.assets.texts.brush.BrushYellowPurple,
                    MR.assets.texts.brush.BrushCoffeeBlue,
                    MR.assets.texts.brush.BrushBeautyAfter,
                    MR.assets.texts.brush.BrushBlueYellowCircle,
                    MR.assets.texts.brush.BrushRedDialog,
                    MR.assets.texts.brush.BrushFollowMePurple,
                )
            }

            "5swipeup" -> {
                listOf(
                    MR.assets.texts.swipeup.Swipeup3Colors,
                    MR.assets.texts.swipeup.SwipeupBusinessWhite,
                    MR.assets.texts.swipeup.SwipeupSimpleYellow,
                    MR.assets.texts.swipeup.SwipeupStickerPurpleYellow,
                    MR.assets.texts.swipeup.SwipeupStickerCyanFinger,
                    MR.assets.texts.swipeup.SwipeupStickerTextPath,
                    MR.assets.texts.swipeup.SwipeupRedPurpleBg,
                    MR.assets.texts.swipeup.SwipeupScalingCircle,
                )
            }

            "6social" -> {
                listOf(
                    MR.assets.texts.social.SocialContactFadeBg,
                    MR.assets.texts.social.SocialContactBorder,
                    MR.assets.texts.social.SocialContactWhite,
                )
            }
            else -> throw IllegalStateException(category)
        }

        return mediaReadWrite.openMediaTexts(resources)
    }
}