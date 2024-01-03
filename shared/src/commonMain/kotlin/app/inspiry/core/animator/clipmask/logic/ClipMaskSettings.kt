package app.inspiry.core.animator.clipmask.logic

import app.inspiry.core.animator.appliers.ClipAnimApplier
import app.inspiry.core.animator.clipmask.shape.ShapeTransform
import app.inspiry.core.animator.clipmask.shape.ShapeType

/**
 * data class with settings for any mask
 * each mask will use the parameters that it needs
 */
data class ClipMaskSettings(
    var maskType: ClipMaskType = ClipMaskType.NONE, //animated clip mask
    var x: Float = 0f,
    var y: Float = 0f,
    var radius: Float = 0f,
    var viewWidth: Float = 0f,
    var viewHeight: Float = 0f,
    var inverse: Boolean = false,
    var progress: Float = 0f,
    var count: Int = 0,
    var direction: ClipAnimApplier.Direction = ClipAnimApplier.Direction.left_to_right,
    var reflection: Boolean = false,
    var shape: ShapeType? = null,
    var shapeTransform: ShapeTransform = ShapeTransform(), //static shape or animated separately from the mask
)
