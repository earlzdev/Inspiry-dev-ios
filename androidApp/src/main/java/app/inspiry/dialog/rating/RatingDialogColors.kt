package app.inspiry.dialog.rating

import app.inspiry.core.ui.DialogColors
import dev.icerock.moko.graphics.Color

interface RatingDialogColors: DialogColors {

    val starsBackground: Color
    val starsUpText: Color
    val focusedFeedbackBorder: Color
    val unfocusedFeedbackBorder: Color
    val feedbackHintText: Color
    val cancelButtonText: Color
    val submitButtonText: Color
}