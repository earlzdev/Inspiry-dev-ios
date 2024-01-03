package app.inspiry.core.data

actual class PointF {
    actual var x: Float
    actual var y: Float

    actual constructor() {
        this.x = 0f
        this.y = 0f
    }
    actual constructor(x: Float, y: Float) {
        this.x = x
        this.y = y
    }
}