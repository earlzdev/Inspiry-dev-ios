package app.inspiry.media

data class TextureParams(
    val isPixelSizeAvailable: Boolean,
    val isBlurEffectAvailable: Boolean,
    val matrices: List<TextureMatrix<*>>
)