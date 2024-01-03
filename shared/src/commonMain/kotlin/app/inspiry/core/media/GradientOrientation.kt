package app.inspiry.core.media

enum class GradientOrientation(val angle: Float) {

    TOP_BOTTOM(90f),

    /** draw the gradient from the top-right to the bottom-left */
    TR_BL(225f),

    /** draw the gradient from the right to the left */
    RIGHT_LEFT(270f),

    /** draw the gradient from the bottom-right to the top-left */
    BR_TL(315f),

    /** draw the gradient from the bottom to the top */
    BOTTOM_TOP(0f),

    /** draw the gradient from the bottom-left to the top-right */
    BL_TR(45f),

    /** draw the gradient from the left to the right */
    LEFT_RIGHT(90f),

    /** draw the gradient from the top-left to the bottom-right */
    TL_BR(135f),

}