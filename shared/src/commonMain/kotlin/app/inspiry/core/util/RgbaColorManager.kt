package app.inspiry.core.util

object RgbaColorManager : ColorManager() {
    override fun red(color: Int): Int = firstColorComponent(color)

    override fun green(color: Int): Int = secondColorComponent(color)

    override fun blue(color: Int): Int = thirdColorComponent(color)

    override fun alpha(color: Int): Int = forthColorComponent(color)

    override fun color(red: Int, green: Int, blue: Int, alpha: Int): Int {
        return alpha or
                blue.shl(8) or
                green.shl(16) or
                red.shl(24)
    }

    override fun colorToString(color: Int): String {
        return "#${
            red(color).toStringComponent()
        }${
            green(color).toStringComponent()
        }${
            blue(color).toStringComponent()
        }${alpha(color).toStringComponent()}"
    }

    override fun getFromHSL(hsl: ColorHSL, alpha: Float): Int {

        val rgb = getRGBfromHSL(hsl)

        return RgbaColorManager.color(rgb.first, rgb.second, rgb.third, alpha)

    }

}