package app.inspiry.core.data

expect class Rect {

    constructor()
    constructor(left: Int, top: Int, right: Int, bottom: Int)

    fun set(left: Int, top: Int, right: Int, bottom: Int)

    fun set(rect: Rect)

    var left: Int
    var right: Int
    var top: Int
    var bottom: Int

    fun width(): Int
    fun height(): Int
    fun offset(dx: Int, dy: Int)

    fun centerX(): Int
    fun centerY(): Int
}