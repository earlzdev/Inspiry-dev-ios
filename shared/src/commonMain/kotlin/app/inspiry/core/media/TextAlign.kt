package app.inspiry.core.media

enum class TextAlign {
    left, start, center, right, end;

    fun toggle() = when(this) {
        left, start -> center
        center -> right
        right, end -> left
    }
}

fun TextAlign.getAlignIcon() = when (this) {
    TextAlign.left, TextAlign.start -> "ic_instrument_align_left"
    TextAlign.right, TextAlign.end -> "ic_instrument_align_right"
    TextAlign.center -> "ic_instrument_align_center"
}