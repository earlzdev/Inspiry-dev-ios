package app.inspiry.utils

import android.view.Gravity
import app.inspiry.R
import app.inspiry.core.media.TextAlign

fun TextAlign.toGravity() = (when (this) {
        TextAlign.start -> Gravity.START
        TextAlign.left -> Gravity.LEFT
        TextAlign.center -> Gravity.CENTER_HORIZONTAL
        TextAlign.right -> Gravity.RIGHT
        TextAlign.end -> Gravity.END
    }) or Gravity.CENTER_VERTICAL