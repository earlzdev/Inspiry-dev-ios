package app.inspiry.views.media

import dev.icerock.moko.resources.StringResource
import app.inspiry.MR

enum class ColorFilterMode {
    DISABLE, //color palette will be disabled
    DEFAULT, //old color filter
    DARKEN,
    LIGHTEN,
    MULTIPLY,
    SCREEN,
    ADD,
    OVERLAY
}

fun ColorFilterMode.stringResource(): StringResource {
    return when (this) {
        ColorFilterMode.DEFAULT -> MR.strings.color_filter_default
        ColorFilterMode.DARKEN -> MR.strings.color_filter_darken
        ColorFilterMode.LIGHTEN -> MR.strings.color_filter_lighten
        ColorFilterMode.MULTIPLY -> MR.strings.color_filter_multiply
        ColorFilterMode.SCREEN -> MR.strings.color_filter_screen
        ColorFilterMode.ADD -> MR.strings.color_filter_difference
        ColorFilterMode.OVERLAY -> MR.strings.color_filter_overlay
        else -> throw IllegalStateException("not implemented filter mode (${this.name})")
    }
}
