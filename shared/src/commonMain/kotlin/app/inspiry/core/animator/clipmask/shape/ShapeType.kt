package app.inspiry.core.animator.clipmask.shape

enum class ShapeType() {
    NOTHING,
    SQUARE,
    CIRCLE,
    ROUNDED_RECT,
    MORE_ROUNDED_RECT,
    TRIANGLE,
    HEXAGON,
    STAR_FIVE,
    STAR_MULTI,
    WINDOW,
    OVAL,
    FLY,
    HEART,



    //STAR_SQUARE,

}

fun ShapeType.isSquared() =
    this != ShapeType.ROUNDED_RECT && this != ShapeType.MORE_ROUNDED_RECT && this != ShapeType.WINDOW && this != ShapeType.OVAL && this != ShapeType.NOTHING

fun ShapeType.icon(): String {
    return when (this) {
        ShapeType.NOTHING -> "ic_remove_color"
        ShapeType.SQUARE -> "ic_shape_square"
        ShapeType.CIRCLE -> "ic_shape_circle"
        ShapeType.ROUNDED_RECT -> "ic_shape_rectangle"
        ShapeType.MORE_ROUNDED_RECT ->  "ic_shape_soft"
        ShapeType.TRIANGLE -> "ic_shape_triangle"
        ShapeType.HEXAGON -> "ic_shape_hexagon"
        ShapeType.STAR_FIVE -> "ic_shape_star"
        ShapeType.STAR_MULTI -> "ic_shape_multi_star"
        ShapeType.WINDOW -> "ic_shape_window"
        ShapeType.OVAL -> "ic_shape_ellipse"
        ShapeType.FLY -> "ic_shape_fly"
        ShapeType.HEART -> "ic_shape_heart"
    }
}