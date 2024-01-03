package app.inspiry.core.util

import android.content.Context
import android.view.ViewGroup
import android.widget.FrameLayout

fun Context.getDefaultViewContainer() = FrameLayout(this).apply {
    layoutParams = ViewGroup.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.MATCH_PARENT,
    )
}