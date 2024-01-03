package app.inspiry.utilities

fun dev.icerock.moko.graphics.Color.toCColor(): androidx.compose.ui.graphics.Color {
    return  androidx.compose.ui.graphics.Color(this.argb)
}
