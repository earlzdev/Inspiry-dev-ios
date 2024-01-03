package app.inspiry.core.animator.clipmask.logic

import app.inspiry.core.animator.clipmask.PathMask
import app.inspiry.core.data.Geometry

class MaskCircle : PathMask<Geometry.Circle> {
    override fun getMaskArray(maskSettings: ClipMaskSettings): Array<Geometry.Circle> {
        return arrayOf(
            Geometry.Circle(
                centerX = maskSettings.x,
                centerY = maskSettings.y,
                radius = maskSettings.radius
            )
        )
    }
}
