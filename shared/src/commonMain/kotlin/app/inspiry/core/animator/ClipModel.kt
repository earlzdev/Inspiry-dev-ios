package app.inspiry.core.animator

import app.inspiry.core.animator.appliers.ClipAnimApplier
import app.inspiry.core.data.RectF

data class ClipModel (
    val frame: RectF = RectF(),
    val direction: ClipAnimApplier.Direction
        )