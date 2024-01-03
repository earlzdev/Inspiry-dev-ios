package app.inspiry.subscribe

import dev.icerock.moko.graphics.Color

interface SubscribeColors {
    val gradient1Start: Color
    val gradient1End: Color
    val headerGradientTop: Color
    val headerGradientBottom: Color
    val radioButtonBorderInactiveColor: Color

    val textOptionActiveA: Color
    val textOptionInactiveA: Color
    val optionBgInactiveA: Color
    val optionBgActiveA: Color
    val optionBorderColorA: Color
    val optionBorderWhiteA: Color

    val headerTextShadowColor: Color
    val optionBorderColorB: Color
    val optionBgColorB: Color
    val textOptionColorB: Color
    val textOptionDarkColorB: Color
}