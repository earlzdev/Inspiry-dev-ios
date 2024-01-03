package app.inspiry.dialog.rating

import dev.icerock.moko.graphics.Color

class RatingDialogLightColors : RatingDialogColors {
    override val starsBackground: Color
        get() = Color(0xf2f2f2ff)
    override val starsUpText: Color
        get() = Color(0x949494ff)
    override val focusedFeedbackBorder: Color
        get() = Color(0x888888ff)
    override val unfocusedFeedbackBorder: Color
        get() = Color(0xCCCCCCff)
    override val feedbackHintText: Color
        get() = Color(0xCCCCCCff)
    override val cancelButtonText: Color
        get() = Color(0x888888ff)
    override val submitButtonText: Color
        get() = Color(0x0000FFFF)
    override val background: Color
        get() = Color(0xFFFFFFFF)
    override val isLight: Boolean
        get() = true
}