package app.inspiry.views.path

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.view.MotionEvent
import android.view.View
import app.inspiry.core.media.MediaPath
import app.inspiry.views.touch.MovableTouchHelperAndroid

class InnerViewAndroidPath(
    context: Context,
    override val media: MediaPath

) : View(context), InnerViewPath<AndroidPath> {

    override lateinit var drawPath: () -> AndroidPath?

    lateinit var drawListener: (Canvas) -> Unit

    var movableTouchHelper: MovableTouchHelperAndroid? = null

    init {
        clipToOutline = true
        setWillNotDraw(false)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        movableTouchHelper?.onTouchMovable(event)
        return movableTouchHelper != null
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val maxWidth = paddingLeft + paddingRight
        val maxHeight = paddingTop + paddingBottom

        setMeasuredDimension(
            resolveSizeAndState(maxWidth, widthMeasureSpec, 0),
            resolveSizeAndState(
                maxHeight, heightMeasureSpec,
                0 shl MEASURED_HEIGHT_STATE_SHIFT
            )
        )
    }

    override fun draw(canvas: Canvas) {
        drawListener.invoke(canvas)
        super.draw(canvas)
    }

    override fun onDraw(canvas: Canvas) {

        val p = drawPath()
        if (p != null) {
            canvas.drawPath(p.path, p.pathPaint)
        }
    }

    override fun invalidateColorOrGradient() {
        invalidate()
    }

}