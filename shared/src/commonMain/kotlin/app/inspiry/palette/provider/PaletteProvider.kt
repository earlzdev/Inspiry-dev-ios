package app.inspiry.palette.provider

import app.inspiry.palette.model.PaletteLinearGradient

interface PaletteProvider {
    fun getGradients(): MutableList<PaletteLinearGradient>
    fun getSingleColors(): List<Int>
    fun getTwoColors(): List<IntArray>
    fun getThreeColors(): List<IntArray>
}