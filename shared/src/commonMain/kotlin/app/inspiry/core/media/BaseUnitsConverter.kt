package app.inspiry.core.media

import app.inspiry.core.util.InspMathUtil

abstract class BaseUnitsConverter {


    fun units(units: String?, multiply: Double, divide: Double,
              canvasWidth: Int, canvasHeight: Int, forHorizontal: Boolean? = null): Float {
        return when (units) {
            "min", "m" -> (canvasHeight.coerceAtMost(canvasWidth) * multiply) / divide
            "tms" -> canvasHeight - (canvasHeight.coerceAtMost(canvasHeight) * multiply) / divide
            "rms" -> canvasWidth - (canvasHeight.coerceAtMost(canvasHeight) * multiply) / divide
            "h" -> (canvasHeight * multiply) / divide
            "w" -> (canvasWidth * multiply) / divide
            "px" -> multiply / divide
            "dp" -> (multiply / divide).toFloat().convertDp()
            "sp" -> (multiply / divide).toFloat().convertSp()
            // s, square is deprecated
            "s", "square" -> ((canvasHeight + canvasWidth) / 2.0) * multiply / divide
            "sw" -> (((canvasWidth + canvasHeight) * DEFAULT_WIDTH.toDouble()) / (DEFAULT_WIDTH + DEFAULT_HEIGHT)) * (multiply / divide)
            "sh" -> (((canvasWidth + canvasHeight) * DEFAULT_HEIGHT.toDouble()) / (DEFAULT_WIDTH + DEFAULT_HEIGHT)) * (multiply / divide)
            null -> when {
                forHorizontal == null -> 0
                forHorizontal -> (canvasWidth * multiply) / 1080
                else -> (canvasHeight * multiply) / 1920
            }
            else -> throw IllegalStateException("unknown unit $units")
        }.toFloat()
    }

    open fun Float.convertSp(): Float {
        return this
    }

    open fun Float.convertDp(): Float {
        return this
    }

    abstract fun getMatchParentValue(): Float
    abstract fun getWrapContentValue(): Float

    abstract fun getScreenHeight(): Int
    abstract fun getScreenWidth(): Int

    fun unitsMultiply(value: String, multiplier: Float): String {
        val vu = findValueAndUnits(arg = value)
        var units = vu.first
        if (units == null) units = ""
        return "${InspMathUtil.roundDoubleTo(vu.second * multiplier,5)}$units"
    }

    private fun findValueAndUnits(arg: String): Pair<String?, Double> {
        var indexOfNotNumber = -1
        var i = 0
        for (c in arg) {
            if (!c.isDigit() && c != '.' && c != '-') {
                indexOfNotNumber = i
                break
            }
            i++
        }

        var units: String? = null
        val value: Double
        if (indexOfNotNumber == -1) {
            value = arg.toDouble()
        } else {
            value = arg.substring(0, indexOfNotNumber).toDouble()
            units = arg.substring(indexOfNotNumber)
        }

        return Pair(units, value)
    }

    fun convertUnitToPixelsF(value: String?, screenWidth: Int, screenHeight: Int, fallback: Float = 0f, forHorizontal: Boolean? = null): Float {

        if (value.isNullOrBlank()) return fallback
        else if (value == "wrap_content") return getWrapContentValue()
        else if (value == "match_parent") return getMatchParentValue()

        val split = value.split('/')
        val multiply = split[0]

        if (split.size == 2) {
            val valueAndUnits = findValueAndUnits(split[1])
            return units(valueAndUnits.first, multiply.toDouble(), valueAndUnits.second, screenWidth, screenHeight, forHorizontal)

        } else {

            val valueAndUnits = findValueAndUnits(multiply)
            return units(valueAndUnits.first, valueAndUnits.second, 1.0, screenWidth, screenHeight, forHorizontal)
        }

    }

    fun convertUnitToPixels(value: String?, screenWidth: Int, screenHeight: Int, forHorizontal: Boolean? = null) =
        convertUnitToPixelsF(value, screenWidth, screenHeight, forHorizontal = forHorizontal).toInt()
}

private const val DEFAULT_WIDTH = 1080
private const val DEFAULT_HEIGHT = 1920