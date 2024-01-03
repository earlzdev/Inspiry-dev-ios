package app.inspiry.stickers.util

import app.inspiry.core.animator.InspAnimator
import app.inspiry.core.animator.appliers.FadeAnimApplier
import app.inspiry.core.media.*
import app.inspiry.palette.model.MediaPalette
import app.inspiry.palette.model.MediaPaletteChoice

object StickerProviderUtil {

    fun getDefaultMediaSticker(
        originalSource: String,
        forPremium: Boolean, isLoopEnabled: Boolean, mediaPalette: MediaPalette, staticFrameForEdit: Int?,
        defaultStickerSize: String
    ): Media {

        return MediaVector(
            originalSource = originalSource,
            staticFrameForEdit = staticFrameForEdit,
            isMovable = true, minDuration = Media.MIN_DURATION_AS_TEMPLATE,
            isLoopEnabled = isLoopEnabled,
            forPremium = forPremium, mediaPalette = mediaPalette,
            layoutPosition = LayoutPosition(
                width = defaultStickerSize,
                height = defaultStickerSize, alignBy = Alignment.center
            ),
            animatorsIn = getDefaultAnimatorsIn()
        )
    }

    fun getDefaultAnimatorsIn() = mutableListOf(InspAnimator(duration = 15, animationApplier = FadeAnimApplier()))

    fun getMediaPaletteForItems(items: Array<String>): MediaPalette {

        val choices = mutableListOf<MediaPaletteChoice>()
        items.mapTo(choices) {
            MediaPaletteChoice(elements = listOf(it))
        }
        return MediaPalette(choices = choices)
    }

    fun defaultPaletteItems(num: Int): Array<String> {
        return Array(num) {
            val index = it + 1
            "Color_$index"
        }
    }

    const val PREVIEW_STICKER_SIZE = "0.75w"
}