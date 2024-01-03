package app.inspiry.core.animator.clipmask.shape


data class ShapeTransform (
    var xOffset: Float = 0f, // -1..1
    var yOffset: Float = 0f,// -1..1
    var scaleWidth: Float = 1f,
    var scaleHeight: Float = 1f,
    var rotation: Float = 0f //shape rotation in degrees
        )