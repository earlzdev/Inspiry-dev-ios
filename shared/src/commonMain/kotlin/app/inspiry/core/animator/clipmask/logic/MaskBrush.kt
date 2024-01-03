package app.inspiry.core.animator.clipmask.logic

import app.inspiry.core.animator.appliers.ClipAnimApplier.Direction
import app.inspiry.core.animator.clipmask.PathMask
import app.inspiry.core.data.Geometry
import kotlin.math.min

class MaskBrush : PathMask<Geometry.Rectangle> {

    override fun getMaskArray(maskSettings: ClipMaskSettings): Array<Geometry.Rectangle> {

        var width = maskSettings.viewWidth
        var height = maskSettings.viewHeight

        if (maskSettings.direction == Direction.bottom_to_top ||
            maskSettings.direction == Direction.top_to_bottom
        ) {
            width = height.also { height = width }
        }
        val reflection = maskSettings.reflection
        val rectList = ArrayList<Geometry.Rectangle>()
        val lineTime = 1f / maskSettings.count
        val linesTime = maskSettings.progress / lineTime
        val currentLine = linesTime.toInt()
        val currentLineProgress = linesTime - currentLine
        val lineHeight = height / maskSettings.count

        for (i in 0 until currentLine) {
            var left = 0f
            var top = i * lineHeight
            var right = width
            var bottom = i * lineHeight + lineHeight

            if (reflection) {
                top = height - top
                bottom = height - bottom
            }

            if (needRotate(maskSettings.direction)) {
                left = top.also { top = left }
                right = bottom.also { bottom = right }
            }
            rectList.add(
                Geometry.Rectangle(
                    left = left,
                    top = top,
                    right = right,
                    bottom = bottom,
                    roundBorderRadius = 0f
                )
            )
        }

        var currentDirection = maskSettings.direction
        if (currentLine % 2 != 0) {
            when (maskSettings.direction) {
                Direction.right_to_left -> currentDirection =
                    Direction.left_to_right
                Direction.top_to_bottom -> currentDirection =
                    Direction.bottom_to_top
                Direction.bottom_to_top -> currentDirection =
                    Direction.top_to_bottom
                else -> currentDirection = Direction.right_to_left
            }
        }

        var correctDirect = 0f

        if (currentDirection == Direction.right_to_left ||
            currentDirection == Direction.bottom_to_top
        ) {
            correctDirect = width - width * currentLineProgress
        }

        val radius = min(lineHeight, width * currentLineProgress) / 2f
        var left = correctDirect - radius
        var top = currentLine * lineHeight
        var right = width * currentLineProgress + correctDirect + radius
        var bottom = currentLine * lineHeight + lineHeight

        if (reflection) {
            top = height - top
            bottom = height - bottom
        }

        if (needRotate(direction = maskSettings.direction)) {
            left = top.also { top = left }
            right = bottom.also { bottom = right }
        }
        val r = Geometry.Rectangle(left, top, right, bottom, radius)
        rectList.add(r)

        return rectList.toTypedArray()
    }

    private fun needRotate(direction: Direction): Boolean {
        if (direction == Direction.bottom_to_top ||
            direction == Direction.top_to_bottom
        ) return true
        return false
    }
}
