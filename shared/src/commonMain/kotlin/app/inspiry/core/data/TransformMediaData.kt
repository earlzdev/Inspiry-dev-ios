package app.inspiry.core.data

/**
 * @param scale - newWidth, newHeight = (imageWidth, imageHeight) * scaleFactor
 * @param translateX - translateX = viewWidth * translateXFactor
 * @param translateY - translateY = viewHeight * translateYFactor
 */
data class TransformMediaData(
    val scale: Float,
    val translateX: Float,
    val translateY: Float,
    val rotate: Float
) {
    override fun toString(): String {
        return "TransformMediaData(centerScaleFactor=$scale, centerTranslateXFactor=$translateX, centerTranslateYFactor=$translateY, centerRotateFactor=$rotate)"
    }
}