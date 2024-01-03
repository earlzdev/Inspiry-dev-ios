package app.inspiry.utils

import android.graphics.Point
import android.graphics.PointF
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.view.ViewGroup
import android.widget.HorizontalScrollView
import android.widget.ScrollView
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.children
import androidx.core.view.updateLayoutParams
import app.inspiry.core.media.TemplateFormat
import app.inspiry.core.util.ArgbColorManager
import app.inspiry.core.util.PredefinedColors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.math.*

object ViewUtils {

    /**
     * Calculate the global coordinate of the central point of the view,
     *  taking into account rotation
     */
    fun getCenterLocationOnScreen(view: View): PointF {
        val location = getLocationOnScreen(view)
        val halfWidth = view.width / 2.0
        val halfHeight = view.height / 2.0
        val startAngle = atan(halfHeight / halfWidth)
        val viewAngle = Math.toRadians(view.rotation.toDouble())
        val rotationAngle = viewAngle + startAngle
        val radius = sqrt(halfWidth * halfWidth + halfHeight * halfHeight)
        val deltaX = cos(rotationAngle) * radius
        val deltaY = sin(rotationAngle) * radius
        return PointF(location.x + deltaX.toFloat(), location.y + deltaY.toFloat())
    }

    /**
     * Get top/left coordinate of the point of the view when rotation is 0
     */
    private fun getLocationOnScreen(view: View): Point {
        val location = IntArray(2)
        view.getLocationOnScreen(location)
        return Point(location[0], location[1])
    }

    fun getBackgroundColor(view: View, defaultColor: Int): Int {
        val backgroundColor = (view.background as? ColorDrawable)?.color
        if (backgroundColor != null) return backgroundColor
        val parentView = view.parent as? View ?: return defaultColor
        return getBackgroundColor(parentView, defaultColor)
    }
}

fun ScrollView.scrollToDescendantCompat(child: View, smooth: Boolean) {
    val mTempRect = Rect()
    child.getDrawingRect(mTempRect)

    mTempRect.offset(child.translationX.toInt(), child.translationY.toInt())

    /* Offset from child's local coordinates to ScrollView coordinates */

    offsetDescendantRectToMyCoords(
        child,
        mTempRect
    )

    val scrollDelta = computeScrollDeltaToGetChildRectOnScreen(mTempRect)

    if (scrollDelta != 0) {
        if (smooth) {
            smoothScrollBy(0, scrollDelta)
        } else {
            scrollBy(0, scrollDelta)
        }
    }
}

fun HorizontalScrollView.scrollToDescendantCompat(child: View, smooth: Boolean) {
    val mTempRect = Rect()
    child.getDrawingRect(mTempRect)

    /* Offset from child's local coordinates to ScrollView coordinates */

    offsetDescendantRectToMyCoords(
        child,
        mTempRect
    )

    val scrollDelta = computeScrollDeltaToGetChildRectOnScreen(mTempRect)

    if (scrollDelta != 0) {
        if (smooth) {
            smoothScrollBy(scrollDelta, 0)
        } else {
            scrollBy(scrollDelta, 0)
        }
    }
}

fun HorizontalScrollView.computeScrollDeltaToGetChildRectOnScreen(rect: Rect): Int {
    if (childCount == 0) return 0

    val width = width
    var screenLeft = scrollX
    var screenRight = screenLeft + width

    val fadingEdge = horizontalFadingEdgeLength

    // leave room for left fading edge as long as rect isn't at very left

    // leave room for left fading edge as long as rect isn't at very left
    if (rect.left > 0) {
        screenLeft += fadingEdge
    }

    // leave room for right fading edge as long as rect isn't at very right

    // leave room for right fading edge as long as rect isn't at very right
    if (rect.right < getChildAt(0).width) {
        screenRight -= fadingEdge
    }

    var scrollXDelta = 0

    if (rect.right > screenRight && rect.left > screenLeft) {
        // need to move right to get it in view: move right just enough so
        // that the entire rectangle is in view (or at least the first
        // screen size chunk).
        scrollXDelta += if (rect.width() > width) {
            // just enough to get screen size chunk on
            rect.left - screenLeft
        } else {
            // get entire rect at right of screen
            rect.right - screenRight
        }

        // make sure we aren't scrolling beyond the end of our content
        val right = getChildAt(0).right
        val distanceToRight = right - screenRight
        scrollXDelta = Math.min(scrollXDelta, distanceToRight)
    } else if (rect.left < screenLeft && rect.right < screenRight) {
        // need to move right to get it in view: move right just enough so that
        // entire rectangle is in view (or at least the first screen
        // size chunk of it).
        scrollXDelta -= if (rect.width() > width) {
            // screen size chunk
            screenRight - rect.right
        } else {
            // entire rect at left
            screenLeft - rect.left
        }

        // make sure we aren't scrolling any further than the left our content
        scrollXDelta = Math.max(scrollXDelta, -scrollX)
    }
    return scrollXDelta
}

/**
 * Compute the amount to scroll in the Y direction in order to get
 * a rectangle completely on the screen (or, if taller than the screen,
 * at least the first screen size chunk of it).
 *
 * @param rect The rect.
 * @return The scroll delta.
 */

fun ScrollView.computeScrollDeltaToGetChildRectOnScreen(rect: Rect): Int {
    if (getChildCount() == 0) return 0
    val height: Int = getHeight()
    var screenTop: Int = getScrollY()
    var screenBottom = screenTop + height
    val fadingEdge: Int = getVerticalFadingEdgeLength()

    // leave room for top fading edge as long as rect isn't at very top
    if (rect.top > 0) {
        screenTop += fadingEdge
    }

    // leave room for bottom fading edge as long as rect isn't at very bottom
    if (rect.bottom < getChildAt(0).getHeight()) {
        screenBottom -= fadingEdge
    }
    var scrollYDelta = 0
    if (rect.bottom > screenBottom && rect.top > screenTop) {
        // need to move down to get it in view: move down just enough so
        // that the entire rectangle is in view (or at least the first
        // screen size chunk).
        scrollYDelta += if (rect.height() > height) {
            // just enough to get screen size chunk on
            rect.top - screenTop
        } else {
            // get entire rect at bottom of screen
            rect.bottom - screenBottom
        }

        // make sure we aren't scrolling beyond the end of our content
        val bottom: Int = getChildAt(0).getBottom()
        val distanceToBottom = bottom - screenBottom
        scrollYDelta = Math.min(scrollYDelta, distanceToBottom)
    } else if (rect.top < screenTop && rect.bottom < screenBottom) {
        // need to move up to get it in view: move up just enough so that
        // entire rectangle is in view (or at least the first screen
        // size chunk of it).
        scrollYDelta -= if (rect.height() > height) {
            // screen size chunk
            screenBottom - rect.bottom
        } else {
            // entire rect at top
            screenTop - rect.top
        }

        // make sure we aren't scrolling any further than the top our content
        scrollYDelta = Math.max(scrollYDelta, -getScrollY())
    }
    return scrollYDelta
}

fun View.changePaddingToView(
    padLeft: Int = paddingLeft,
    padTop: Int = paddingTop,
    padRight: Int = paddingRight,
    padBottom: Int = paddingBottom
) {
    setPadding(padLeft, padTop, padRight, padBottom)
}

fun View.setMarginToView(
    marginTop: Int = 0,
    marginLeft: Int = 0,
    marginRight: Int = 0,
    marginBottom: Int = 0, requestLayout: Boolean = true
) {
    val lp = layoutParams as? ViewGroup.MarginLayoutParams?

    if (lp != null && (lp.topMargin != marginTop ||
                lp.bottomMargin != marginBottom || lp.leftMargin != marginLeft || lp.rightMargin != marginRight)
    ) {
        lp.topMargin = marginTop
        lp.bottomMargin = marginBottom
        lp.rightMargin = marginRight
        lp.leftMargin = marginLeft

        if (requestLayout)
            requestLayout()
    }
}

fun View.setHeight(height: Int) {
    val lp = layoutParams
    if (lp != null) {
        val oldHeight = layoutParams.height
        if (oldHeight != height) {
            layoutParams.height = height
            requestLayout()
        }
    }
}

/**
 * Autoscroll to index in LazyList
 */

fun LazyListState.autoScroll(
    scope: CoroutineScope,
    scrollToIndex: Int,
    durationMillis: Int = 500
) {
    val listState = this
    scope.launch {
        if (listState.layoutInfo.visibleItemsInfo.isNotEmpty() && scrollToIndex >= 0) {
            var firstVisible = listState.firstVisibleItemIndex
            var lastVisible = listState.layoutInfo.visibleItemsInfo.lastIndex + firstVisible
            if (scrollToIndex !in firstVisible..lastVisible) {
                listState.scrollToItem(scrollToIndex)
                firstVisible = listState.firstVisibleItemIndex
                lastVisible = listState.layoutInfo.visibleItemsInfo.lastIndex + firstVisible
            }
            val listSize = listState.layoutInfo.run { viewportEndOffset - viewportStartOffset }
            var targetVisibleIndex = scrollToIndex - firstVisible
            if (targetVisibleIndex < 0 || targetVisibleIndex >= listState.layoutInfo.visibleItemsInfo.size) targetVisibleIndex =
                0
            val currentSelectedOffset =
                listState.layoutInfo.visibleItemsInfo[targetVisibleIndex].offset
            val currentItemSize = listState.layoutInfo.visibleItemsInfo[targetVisibleIndex].size

            val newOffset = currentSelectedOffset - (listSize - currentItemSize) / 2f

            val firstVisibleOffset = listState.layoutInfo.visibleItemsInfo.first().offset
            val isEnd = firstVisible == 0 && firstVisibleOffset == 0 && newOffset <= 0
            val isStart =
                (listState.layoutInfo.totalItemsCount - 1) == lastVisible && listState.layoutInfo.visibleItemsInfo.last()
                    .run { offset == listSize - size } && newOffset >= 0

            if (!isEnd && !isStart) listState.animateScrollBy(
                newOffset + 0f, tween(
                    durationMillis = durationMillis,
                    easing = LinearOutSlowInEasing
                )
            )
        }
    }
}

// TODO: onClick listener is still not removed correctly.
fun View.removeOnClickListener() {
    if (hasOnClickListeners()) {
        setOnClickListener(null)
        isClickable = false

        // is it necessary?
        /* if (this is ViewGroup) {
            this.children.forEach {
                it.removeOnClickListener()
            }
        } */
    }

    if (onFocusChangeListener != null) {
        onFocusChangeListener = null
    }
}

fun Modifier.coloredShadow(
    color: Int = PredefinedColors.BLACK_ARGB,
    alpha: Float = 0.35f,
    borderRadius: Dp = 0.dp,
    shadowRadius: Dp = 10.dp,
    offsetY: Dp = 0.dp,
    offsetX: Dp = 0.dp
) = this.drawBehind {
    val transparentColor = ArgbColorManager.colorWithAlpha(color, 0f)
    val shadowColor = ArgbColorManager.colorWithAlpha(color, alpha)
    this.drawIntoCanvas {
        val paint = Paint()
        val frameworkPaint = paint.asFrameworkPaint()
        frameworkPaint.color = transparentColor
        frameworkPaint.setShadowLayer(
            shadowRadius.toPx(),
            offsetX.toPx(),
            offsetY.toPx(),
            shadowColor
        )
        it.drawRoundRect(
            0f,
            0f,
            this.size.width,
            this.size.height,
            borderRadius.toPx(),
            borderRadius.toPx(),
            paint
        )
    }
}

fun View.setRatioBasedOnFormat(
    format: TemplateFormat,
    storyFormatForH: Boolean,
    requestLayoutOnChange: Boolean = true
) {
    updateLayoutParams<ConstraintLayout.LayoutParams> {

        val newDimensionRatio = when (format) {
            TemplateFormat.post -> "H, 4:5"
            TemplateFormat.square -> "H, 1:1"
            TemplateFormat.horizontal -> "H, 16:9"
            TemplateFormat.story -> if (storyFormatForH) "H, 9:16" else "W, 9:16"
        }

        if (dimensionRatio != newDimensionRatio) {
            dimensionRatio = newDimensionRatio
            if (requestLayoutOnChange) requestLayout()
        }
    }
}