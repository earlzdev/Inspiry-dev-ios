package app.inspiry.core.data

actual class RectF {
    actual var left: Float
    actual var top: Float
    actual var right: Float
    actual var bottom: Float

    actual constructor() {
        this.left = 0f
        this.top = 0f
        this.right = 0f
        this.bottom = 0f
    }

    actual constructor(left: Float, top: Float, right: Float, bottom: Float) {
        this.left = left
        this.top = top
        this.right = right
        this.bottom = bottom
    }

    actual fun set(left: Float, top: Float, right: Float, bottom: Float) {
        this.left = left
        this.top = top
        this.right = right
        this.bottom = bottom
    }

    actual fun set(rect: RectF) {
        this.left = rect.left
        this.top = rect.top
        this.right = rect.right
        this.bottom = rect.bottom
    }

    actual fun width(): Float {
        return right - left
    }

    actual fun height(): Float {
        return bottom - top
    }

    actual fun offset(dx: Float, dy: Float) {
        left += dx
        right += dx
        top += dy
        bottom += dy
    }
    actual fun centerX() = (this.left + this.right) / 2
    actual fun centerY() = (this.top + this.bottom) / 2
}