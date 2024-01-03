package app.inspiry.stickers.util

import android.content.Context
import app.inspiry.R
import app.inspiry.stickers.providers.*

object StickerAndroidUtil {

    fun localizeTab(id: String, context: Context): String {

        return when (id) {
            ArrowStickerCategory.stickersId -> context.getString(app.inspiry.projectutils.R.string.string_category_arrow)
            BeautyStickerCategory.stickersId -> context.getString(app.inspiry.projectutils.R.string.category_beauty)
            BrushStickerCategory.stickersId -> context.getString(app.inspiry.projectutils.R.string.sticker_category_brush)
            PaperStickerCategory.stickersId -> context.getString(app.inspiry.projectutils.R.string.category_paper)
            SocialStickerCategory.stickersId -> context.getString(app.inspiry.projectutils.R.string.sticker_category_social)
            HalloweenStickerCategory.stickersId -> context.getString(app.inspiry.projectutils.R.string.category_halloween)
            ChristmasStickerCategory.stickersId -> context.getString(app.inspiry.projectutils.R.string.category_christmas)

            else -> throw IllegalStateException("unknown id: $id")
        }
    }
}