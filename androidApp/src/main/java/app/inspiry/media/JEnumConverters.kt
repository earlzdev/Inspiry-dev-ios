package app.inspiry.media

import android.graphics.Paint
import android.graphics.drawable.GradientDrawable
import android.widget.ImageView
import app.inspiry.core.media.GradientOrientation
import app.inspiry.core.media.PaintStyle
import app.inspiry.core.media.ScaleType

fun PaintStyle.toJava() = Paint.Style.valueOf(name)
fun GradientOrientation.toJava() = GradientDrawable.Orientation.valueOf(name)
fun ScaleType.toJava() = ImageView.ScaleType.valueOf(name)