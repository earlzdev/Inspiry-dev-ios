package app.inspiry.core.util

object ArgbColorManager : ColorManager() {
    override fun colorToString(color: Int): String {
        return "#${alpha(color).toStringComponent()}${red(color).toStringComponent()}${green(color).toStringComponent()}${
            blue(
                color
            ).toStringComponent()
        }"
    }

    override fun getFromHSL(hsl: ColorHSL, alpha: Float): Int {

        val rgb = getRGBfromHSL(hsl)

        return ArgbColorManager.color(rgb.first, rgb.second, rgb.third, alpha)

    }

    fun getInverted(color: Int) = color xor(0x00FFFFFF)

    override fun red(color: Int): Int = secondColorComponent(color)

    override fun green(color: Int): Int = thirdColorComponent(color)

    override fun blue(color: Int): Int = forthColorComponent(color)

    override fun alpha(color: Int): Int = firstColorComponent(color)

    override fun color(red: Int, green: Int, blue: Int, alpha: Int): Int {
        return blue or
                green.shl(8) or
                red.shl(16) or
                alpha.shl(24)
    }
}