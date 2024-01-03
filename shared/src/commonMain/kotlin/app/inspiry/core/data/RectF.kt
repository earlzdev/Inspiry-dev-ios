package app.inspiry.core.data

expect class RectF {

    constructor()
    constructor(left: Float, top: Float, right: Float, bottom: Float)

    fun set(left: Float, top: Float, right: Float, bottom: Float)

    fun set(rect: RectF)

    var left: Float
    var right: Float
    var top: Float
    var bottom: Float

    fun width(): Float
    fun height(): Float
    fun offset(dx: Float, dy: Float)

    fun centerX(): Float
    fun centerY(): Float
}