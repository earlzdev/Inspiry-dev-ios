package app.inspiry.edit.instruments.color

import dev.icerock.moko.graphics.Color

class TextColorDialogLightColors : TextPaletteDialogColors {
    override val background: Color
        get() = Color(0x29, 0x29, 0x29, 0xff)
    override val selectedPageText: Color
        get() = Color(0x20, 0x20, 0x20, 0xff)
    override val selectedPageTextBackground: Color
        get() = Color(0xE5, 0xE5, 0xE5, 0xff)
    override val pageText: Color
        get() = Color(0xC4, 0xC4, 0xC4, 0xff)
    override val selectedItemInnerBorder: Color
        get() = Color(0x02, 0x02, 0x02, 0xff)
    override val selectedItemOuterBorder: Color
        get() = Color(0xff, 0xff, 0xff, 0xff)
    override val paletteBorderColor: Color
        get() = Color(0xff, 0xff, 0xff, 0xff)
    override val addFromGallery: Color
        get() = Color(0xC4, 0xC4, 0xC4, 0xff)
    override val thumbColor: Color
        get() = Color(0xFF, 0xFF, 0xFF, 0xff)
    override val sliderLabelColor: Color
        get() = Color(0x82, 0x82, 0x82, 0xff)
    override val placeholderBackground: Color
        get() = Color(0x32, 0x32, 0x32, 0xff)
    override val incactiveColorFilterMode: Color
        get() = Color(0x82, 0x82, 0x82, 0xff)
    override val activeColorFilterMode: Color
        get() = Color(0xff, 0xff, 0xff, 0xff)
}