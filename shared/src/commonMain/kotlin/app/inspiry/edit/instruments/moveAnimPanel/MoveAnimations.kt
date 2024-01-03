package app.inspiry.edit.instruments.moveAnimPanel

enum class MoveAnimations {
    NONE,
    RIGHT,
    LEFT,
    DOWN,
    UP,
    ZOOMIN,
    ZOOMOUT,
    FADE
}

fun MoveAnimations.icon(): String {
    return when (this) {
        MoveAnimations.NONE -> "ic_remove_color"
        MoveAnimations.RIGHT -> "move_anim_right"
        MoveAnimations.LEFT -> "move_anim_left"
        MoveAnimations.DOWN -> "move_anim_down"
        MoveAnimations.UP ->  "move_anim_up"
        MoveAnimations.ZOOMIN -> "move_anim_zoom_in"
        MoveAnimations.ZOOMOUT -> "move_anim_zoom_out"
        MoveAnimations.FADE -> "move_anim_fade"
    }
}