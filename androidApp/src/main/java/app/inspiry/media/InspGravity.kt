package app.inspiry.media

import android.view.Gravity
import app.inspiry.core.media.Alignment

fun Alignment.toAndroidGravity(): Int {
    return when (this) {
        Alignment.top_start -> Gravity.START or Gravity.TOP
        Alignment.top_end -> Gravity.TOP or Gravity.END
        Alignment.top_center -> Gravity.TOP or Gravity.CENTER_HORIZONTAL
        Alignment.center_start -> Gravity.CENTER_VERTICAL or Gravity.START
        Alignment.center -> Gravity.CENTER
        Alignment.center_end -> Gravity.CENTER_VERTICAL or Gravity.END
        Alignment.bottom_start -> Gravity.START or Gravity.BOTTOM
        Alignment.bottom_end -> Gravity.BOTTOM or Gravity.END
        Alignment.bottom_center -> Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
    }
}