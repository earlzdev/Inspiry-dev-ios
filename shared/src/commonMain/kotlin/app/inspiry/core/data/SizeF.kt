package app.inspiry.core.data

data class SizeF(val width: Float, val height: Float) {
    override fun toString(): String {
        return "Size(width=$width, height=$height)"
    }
}