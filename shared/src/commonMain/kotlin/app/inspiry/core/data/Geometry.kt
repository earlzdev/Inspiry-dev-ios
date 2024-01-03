package app.inspiry.core.data

sealed class Geometry {

    data class Rectangle(
        var left: Float,
        var top: Float,
        var right: Float,
        var bottom: Float,
        var roundBorderRadius: Float
    )

    data class Circle(
        var centerX: Float,
        var centerY: Float,
        var radius: Float
    )

    data class Point(
        var x: Float,
        var y: Float
    )
}