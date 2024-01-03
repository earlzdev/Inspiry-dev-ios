package app.inspiry.core.data

actual class Rect {
    actual var left: Int
    actual var top: Int
    actual var right: Int
    actual var bottom: Int

    actual constructor() {
        this.left = 0
        this.top = 0
        this.right = 0
        this.bottom = 0
    }

    actual constructor(left: Int, top: Int, right: Int, bottom: Int) {
        this.left = left
        this.top = top
        this.right = right
        this.bottom = bottom
    }

    actual fun set(left: Int, top: Int, right: Int, bottom: Int) {
        this.left = left
        this.top = top
        this.right = right
        this.bottom = bottom
    }

    actual fun set(rect: Rect) {
        this.left = rect.left
        this.top = rect.top
        this.right = rect.right
        this.bottom = rect.bottom
    }

    actual fun width(): Int {
        return right - left
    }

    actual fun height(): Int {
        return bottom - top
    }

    actual fun offset(dx: Int, dy: Int) {
        left += dx
        right += dx
        top += dy
        bottom += dy
    }
    actual fun centerX() = (this.left + this.right) / 2
    actual fun centerY() = (this.top + this.bottom) / 2
}