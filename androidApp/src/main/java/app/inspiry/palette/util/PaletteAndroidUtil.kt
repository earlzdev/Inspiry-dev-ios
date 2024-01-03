package app.inspiry.palette.util

import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.os.Build
import app.inspiry.media.toJava
import app.inspiry.palette.model.AbsPaletteColor
import app.inspiry.palette.model.PaletteColor
import app.inspiry.palette.model.PaletteLinearGradient
import app.inspiry.palette.model.PaletteMultiColor
import app.inspiry.views.androidhelper.MultiColorDrawable

fun AbsPaletteColor.getDrawable(): Drawable {
    return when (this) {
        is PaletteMultiColor -> MultiColorDrawable(colors)
        is PaletteLinearGradient -> {
            val gradient = GradientDrawable()
            gradient.orientation = orientation.toJava()
            if (offsets != null && Build.VERSION.SDK_INT >= 29) {
                gradient.setColors(colors.toIntArray(), offsets)
            } else
                gradient.colors = colors.toIntArray()
            gradient
        }
        is PaletteColor -> GradientDrawable().also { it.setColor(color) }

    }
}

fun AbsPaletteColor.getDrawableForList(): Drawable {
    return when (this) {
        is PaletteColor, is PaletteLinearGradient -> getDrawable().also {
            (it as GradientDrawable).shape = GradientDrawable.OVAL
        }
        else -> getDrawable()
    }
}