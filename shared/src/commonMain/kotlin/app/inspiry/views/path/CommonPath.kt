package app.inspiry.views.path

import app.inspiry.core.animator.interpolator.InspInterpolator
import app.inspiry.core.data.RectF
import app.inspiry.core.media.*
import app.inspiry.palette.model.PaletteLinearGradient
import app.inspiry.views.InspView
import kotlin.math.min

abstract class CommonPath {
    abstract fun moveTo(x: Float, y: Float)
    abstract fun lineTo(x: Float, y: Float)
    abstract fun quadTo(x: Float, y: Float, x2: Float, y2: Float)

    abstract fun close()
    abstract fun reset()
    abstract fun refreshGradient(gradient: PaletteLinearGradient?, width: Int, height: Int)

    abstract fun setPathCornerRadius(absoluteRadius: Float)
    abstract fun refreshPathColor(color: Int)
    abstract fun setStrokeWidth(strokeWidth: Float)
    abstract fun refreshStyle(color: Int?, alpha: Float, strokeCap: String?, paintStyle: PaintStyle)

    abstract fun updateFillType(inverse: Boolean)

    open fun movePath(
        unitsConverter: BaseUnitsConverter,
        actualFrame: Float, movement: List<PathMovement>,
        pathLinePercents: MutableList<Float>, movementsConnected: Boolean,
        commonInterpolator: InspInterpolator?, v: InspView<*>
    ) {

        if (movementsConnected) pathLinePercents.clear()

        val view = v.view ?: return
        val widthPadded = view.width - view.paddingLeft - view.paddingRight
        val heightPadded = view.height - view.paddingTop - view.paddingBottom

        var actualFrameInterpolated = actualFrame

        if (commonInterpolator != null) {
            val duration = movement.getMovementsDuration()
            var percent = actualFrameInterpolated / duration.toFloat()
            percent = commonInterpolator.getInterpolation(percent)
            actualFrameInterpolated = percent * duration
        }

        for ((index, it) in movement.withIndex()) {
            if (actualFrameInterpolated >= it.startFrame) {

                var percent =
                    if (it.duration == 0 || it.duration == 1) 1f
                    else min(
                        1f,
                        (actualFrameInterpolated - it.startFrame) / (it.duration.toFloat() - 1f)
                    )

                if (it.interpolator != null) percent = it.interpolator!!.getInterpolation(percent)

                if (it is PathLineMovement) {

                    if (movementsConnected)
                        pathLinePercents.add(percent)

                    if (index == 0 || !movementsConnected)
                        moveTo(
                            it.fromX * widthPadded + view.paddingLeft,
                            it.fromY * heightPadded + view.paddingTop
                        )

                    val toX = (it.toX - it.fromX) * percent + it.fromX
                    val toY = (it.toY - it.fromY) * percent + it.fromY

                    lineTo(toX * widthPadded + view.paddingLeft, toY * heightPadded + view.paddingTop)

                } else if (it is StrokeWidthMovement) {

                    val templateParent = v.templateParent
                    val parentWidth = templateParent.viewWidth
                    val parentHeight = templateParent.viewHeight
                    val from = unitsConverter.convertUnitToPixelsF(it.from, parentWidth, parentHeight)
                    val to = unitsConverter.convertUnitToPixelsF(it.to, parentWidth, parentHeight)

                    val delta = (to - from) * percent

                    val strokeWidth = delta + from

                    setStrokeWidth(strokeWidth)

                    if (strokeWidth == 0f) {
                        reset()
                        break
                    }
                } else if (it is PathCloseMovement) {
                    close()
                }
            } else if (movementsConnected && it is PathLineMovement)
                pathLinePercents.add(0f)
        }


        if (movementsConnected) {
            if ((pathLinePercents.minOrNull() ?: 0f) >= 0.99f) close()
        }
    }

    abstract fun isEmpty(): Boolean

    abstract fun addRoundRect(left: Float, top: Float, right: Float, bottom: Float, rx: Float, ry: Float)
    abstract fun addCircle(centerX: Float, centerY: Float, radius: Float)
    abstract fun addOval(left: Float, right: Float, top: Float, bottom: Float)
}