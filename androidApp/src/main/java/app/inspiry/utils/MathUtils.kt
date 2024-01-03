package app.inspiry.utils

import androidx.compose.ui.unit.IntSize
import app.inspiry.core.data.PointF
import app.inspiry.core.data.Size
import app.inspiry.core.data.Vector
import kotlin.math.atan2

fun IntSize.toCommonSize() = Size(this.width, this.height)
