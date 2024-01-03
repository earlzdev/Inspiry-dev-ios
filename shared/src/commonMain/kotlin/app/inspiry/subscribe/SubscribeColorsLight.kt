package app.inspiry.subscribe

import dev.icerock.moko.graphics.Color

class SubscribeColorsLight: SubscribeColors {
    override val gradient1Start: Color
        get() = Color(0x64,0x73, 0xFF, 0xff)
    override val gradient1End: Color
        get() = Color(0x14, 0xCB, 0xF5, 0xff)
    override val headerGradientTop: Color
        get() = Color(0xe1, 0xe3, 0xe8, 0x00)
    override val headerGradientBottom: Color
        get() = Color(0x42, 0x50, 0x74, 0x80)
    override val radioButtonBorderInactiveColor: Color
        get() = Color(0xDF, 0xDF, 0xDF, 0xff)

    override val textOptionActiveA: Color
        get() = Color(0x37,0x4C, 0xD3, 0xff)
    override val textOptionInactiveA: Color
        get() = Color(0x82,0x82, 0x82, 0xff)
    override val optionBgInactiveA: Color
        get() = Color(0xF4,0xF9, 0xFF, 0xff)
    override val optionBgActiveA: Color
        get() = Color(0xAF,0xE2, 0xFF, 0xff)
    override val optionBorderColorA: Color
        get() = Color(0xE0,0xE9, 0xFF, 0xff)
    override val optionBorderWhiteA: Color
        get() = Color(0xF7,0xF7, 0xF7, 0xff)

    override val headerTextShadowColor: Color
        get() = Color(0x3D, 0x58, 0x8D, 0xCC)
    override val optionBorderColorB: Color
        get() = Color(0xEC,0xEC, 0xEC, 0xff)
    override val optionBgColorB: Color
        get() = Color(0xF3,0xF3, 0xF3, 0xff)
    override val textOptionColorB: Color
        get() = Color(0x82,0x82, 0x82, 0xff)
    override val textOptionDarkColorB: Color
        get() = Color(0x69,0x69, 0x69, 0xff)
}