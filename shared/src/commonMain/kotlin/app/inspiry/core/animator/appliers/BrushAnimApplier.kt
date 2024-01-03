package app.inspiry.core.animator.appliers

import app.inspiry.core.animator.clipmask.logic.ClipMaskType
import app.inspiry.views.InspView
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("brush")
class BrushAnimApplier(
    var lines: Int = 3,
    var direction: ClipAnimApplier.Direction = ClipAnimApplier.Direction.left_to_right,
    var inverse: Boolean = false,
    var reflection: Boolean = false,
    override var from: Float = 0f,
    override var to: Float = 1f,
) : AnimApplier(), FloatValuesAnimApplier {

    override fun onPreDraw(view: InspView<*>, value: Float) {
        view.animationHelper?.setClipMask(
            maskType = ClipMaskType.BRUSH,
            count = lines,
            direction = direction,
            inverse = inverse,
            progress = calcAnimValue(from, to, value),
            reflection = reflection
        )

    }
}