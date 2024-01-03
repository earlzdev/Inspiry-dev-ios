package app.inspiry.views

import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.*
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.platform.InspectorValueInfo
import androidx.compose.ui.platform.debugInspectorInfo
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.isSatisfiedBy
import kotlin.math.roundToInt

@Stable
fun Modifier.aspectRatioWithBounds(
    /*@FloatRange(from = 0.0, fromInclusive = false)*/
    ratio: Float,
    matchHeightConstraintsFirst: Boolean = false
) = this.then(
    AspectRatioModifier(
        ratio,
        matchHeightConstraintsFirst,
        debugInspectorInfo {
            name = "aspectRatioWithBounds"
            properties["ratio"] = ratio
            properties["matchHeightConstraintsFirst"] = matchHeightConstraintsFirst
        }
    )
)

private class AspectRatioModifier(
    val aspectRatio: Float,
    val matchHeightConstraintsFirst: Boolean,
    inspectorInfo: InspectorInfo.() -> Unit
) : LayoutModifier, InspectorValueInfo(inspectorInfo) {
    init {
        require(aspectRatio > 0) { "aspectRatio $aspectRatio must be > 0" }
    }

    override fun MeasureScope.measure(
        measurable: Measurable,
        constraints: Constraints
    ): MeasureResult {
        val size = constraints.findSize()
        val wrappedConstraints = if (size != IntSize.Zero) {
            Constraints.fixed(size.width, size.height)
        } else {
            constraints
        }
        val placeable = measurable.measure(wrappedConstraints)
        return layout(placeable.width, placeable.height) {
            placeable.placeRelative(0, 0)
        }
    }

    override fun IntrinsicMeasureScope.minIntrinsicWidth(
        measurable: IntrinsicMeasurable,
        height: Int
    ) = if (height != Constraints.Infinity) {
        (height * aspectRatio).roundToInt()
    } else {
        measurable.minIntrinsicWidth(height)
    }

    override fun IntrinsicMeasureScope.maxIntrinsicWidth(
        measurable: IntrinsicMeasurable,
        height: Int
    ) = if (height != Constraints.Infinity) {
        (height * aspectRatio).roundToInt()
    } else {
        measurable.maxIntrinsicWidth(height)
    }

    override fun IntrinsicMeasureScope.minIntrinsicHeight(
        measurable: IntrinsicMeasurable,
        width: Int
    ) = if (width != Constraints.Infinity) {
        (width / aspectRatio).roundToInt()
    } else {
        measurable.minIntrinsicHeight(width)
    }

    override fun IntrinsicMeasureScope.maxIntrinsicHeight(
        measurable: IntrinsicMeasurable,
        width: Int
    ) = if (width != Constraints.Infinity) {
        (width / aspectRatio).roundToInt()
    } else {
        measurable.maxIntrinsicHeight(width)
    }

    private fun Constraints.findSize(): IntSize {
        if (!matchHeightConstraintsFirst) {
            tryMaxWidth().also { if (it != IntSize.Zero) return it }
            tryMaxHeight().also { if (it != IntSize.Zero) return it }
            tryMinWidth().also { if (it != IntSize.Zero) return it }
            tryMinHeight().also { if (it != IntSize.Zero) return it }
            tryMaxWidth(enforceConstraints = false).also { if (it != IntSize.Zero && it.height <= maxHeight) return it }
            tryMaxHeight(enforceConstraints = false).also { if (it != IntSize.Zero && it.width <= maxWidth) return it }
            tryMinWidth(enforceConstraints = false).also { if (it != IntSize.Zero) return it }
            tryMinHeight(enforceConstraints = false).also { if (it != IntSize.Zero) return it }
        } else {
            tryMaxHeight().also { if (it != IntSize.Zero) return it }
            tryMaxWidth().also { if (it != IntSize.Zero) return it }
            tryMinHeight().also { if (it != IntSize.Zero) return it }
            tryMinWidth().also { if (it != IntSize.Zero) return it }
            tryMaxHeight(enforceConstraints = false).also { if (it != IntSize.Zero && it.width <= maxWidth) return it }
            tryMaxWidth(enforceConstraints = false).also { if (it != IntSize.Zero && it.height <= maxHeight) return it }
            tryMinHeight(enforceConstraints = false).also { if (it != IntSize.Zero) return it }
            tryMinWidth(enforceConstraints = false).also { if (it != IntSize.Zero) return it }
        }
        return IntSize.Zero
    }

    private fun Constraints.tryMaxWidth(enforceConstraints: Boolean = true): IntSize {
        val maxWidth = this.maxWidth
        if (maxWidth != Constraints.Infinity) {
            val height = (maxWidth / aspectRatio).roundToInt()
            if (height > 0) {
                val size = IntSize(maxWidth, height)
                if (!enforceConstraints || isSatisfiedBy(size)) {
                    return size
                }
            }
        }
        return IntSize.Zero
    }

    private fun Constraints.tryMaxHeight(enforceConstraints: Boolean = true): IntSize {
        val maxHeight = this.maxHeight
        if (maxHeight != Constraints.Infinity) {
            val width = (maxHeight * aspectRatio).roundToInt()
            if (width > 0) {
                val size = IntSize(width, maxHeight)
                if (!enforceConstraints || isSatisfiedBy(size)) {
                    return size
                }
            }
        }
        return IntSize.Zero
    }

    private fun Constraints.tryMinWidth(enforceConstraints: Boolean = true): IntSize {
        val minWidth = this.minWidth
        val height = (minWidth / aspectRatio).roundToInt()
        if (height > 0) {
            val size = IntSize(minWidth, height)
            if (!enforceConstraints || isSatisfiedBy(size)) {
                return size
            }
        }
        return IntSize.Zero
    }

    private fun Constraints.tryMinHeight(enforceConstraints: Boolean = true): IntSize {
        val minHeight = this.minHeight
        val width = (minHeight * aspectRatio).roundToInt()
        if (width > 0) {
            val size = IntSize(width, minHeight)
            if (!enforceConstraints || isSatisfiedBy(size)) {
                return size
            }
        }
        return IntSize.Zero
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        val otherModifier = other as? AspectRatioModifier ?: return false
        return aspectRatio == otherModifier.aspectRatio &&
                matchHeightConstraintsFirst == other.matchHeightConstraintsFirst
    }

    override fun hashCode(): Int =
        aspectRatio.hashCode() * 31 + matchHeightConstraintsFirst.hashCode()

    override fun toString(): String = "AspectRatioModifierWithBounds(aspectRatio=$aspectRatio)"
}
