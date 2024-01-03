package app.inspiry.views.androidhelper

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.View.OnLayoutChangeListener
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.view.animation.AnimationSet
import android.view.animation.PathInterpolator
import android.view.animation.ScaleAnimation
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.lifecycleScope
import app.inspiry.R
import app.inspiry.core.data.TouchAction
import app.inspiry.edit.EditWrapperHelper
import app.inspiry.utils.dpToPixels
import app.inspiry.utils.dpToPxInt
import app.inspiry.views.viewplatform.getAndroidView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.math.*

@SuppressLint("ViewConstructor")
class SelectedItemBorderAndroid(
    context: Context, private val editWrapperHelper: EditWrapperHelper
) : FrameLayout(context) {

    private var templateViewAndroid: ViewGroup? = null
    private val bordersView: BordersView

    init {
        setWillNotDraw(true)
        translationZ = 100f
        elevation = 100f
        outlineProvider = null

        bordersView = BordersView(context)
        bordersView.visibility = View.GONE
        addView(bordersView, LayoutParams(400, 500))
    }

    private val scope: CoroutineScope
        get() = findViewTreeLifecycleOwner()?.lifecycleScope ?: (context as AppCompatActivity).lifecycleScope

    init {
        initEditWrapper()
    }

    private fun initEditWrapper() {

        scope.launch {
            editWrapperHelper.editBounds.collect {
                redrawEditWrapper(it)
            }
        }

        scope.launch {
            editWrapperHelper.availableActions.collect {
                updateActionViews(it)
            }
        }

        scope.launch {
            editWrapperHelper.isWrapperVisible.collect {
                if (it) showView()
                else hideView()
            }
        }

        scope.launch {
            editWrapperHelper.boundsRotation.collect {
                bordersView.rotation = it
            }
        }

        scope.launch {
            editWrapperHelper.newViewSize.collect {
                val view = editWrapperHelper.templateView.selectedView?.getAndroidView()
                view?.updateLayoutParams {
                    width = it.first
                    height = it.second
                }
                view?.requestLayout()
            }
        }

        editWrapperHelper.onSelectedViewChanged { new, old ->
            old?.getAndroidView()?.removeOnLayoutChangeListener(onLayoutChangeListener)
            new?.getAndroidView()?.apply {
                post {
                    editWrapperHelper.updateBounds()
                    if (editWrapperHelper.borderAnimationEnabled) animateSelection()
                    else editWrapperHelper.animationWasIgnored()
                }
                addOnLayoutChangeListener(onLayoutChangeListener)
            }
        }
    }

    private val onLayoutChangeListener: OnLayoutChangeListener =
        OnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            editWrapperHelper.updateBounds()
        }

    private fun updateActionViews(availableActions: List<TouchAction>) {
        removeActionViews()
        if (availableActions.isEmpty()) return
        createActionViews(availableActions)

    }

    private fun showView() {
        val view = editWrapperHelper.templateView.selectedView ?: return
        val androidView = view.getAndroidView()

        val needDelay =
            androidView.isLaidOut || androidView.isInLayout || androidView.width == 0 || androidView.width == -1

        fun action() {
            bordersView.visibility = View.VISIBLE
            editWrapperHelper.updateBounds()
            animateSelection()
        }

        if (needDelay) {
            androidView.post {
                action()
            }
        } else {
            action()
        }
    }

    private fun hideView() {
        bordersView.visibility = View.GONE
    }

    private fun animateSelection() {
        val width = bordersView.layoutParams.width
        val height = bordersView.layoutParams.height

        val animationSet = AnimationSet(false)
        val scale = ScaleAnimation(1.3f, 1f, 1.3f, 1f, width / 2f, height / 2f)
        val fade = AlphaAnimation(0f, 1f)
        scale.duration = 250
        fade.duration = 300
        scale.interpolator = PathInterpolator(0.25f, 0.1f, 0.25f, 1f)
        animationSet.addAnimation(scale)
        animationSet.addAnimation(fade)
        bordersView.startAnimation(animationSet)
    }

    private fun removeActionViews() {
        bordersView.removeAllViews()
    }

    private fun createActionViews(availableActions: List<TouchAction>) {

        fun createActionView(icon: Int, gravity: Int): View {
            val actionView = ImageView(context)
            actionView.scaleType = ImageView.ScaleType.CENTER_INSIDE
            actionView.setImageResource(icon)
            actionView.setPadding(
                ACTION_ICON_PADDING,
                ACTION_ICON_PADDING,
                ACTION_ICON_PADDING,
                ACTION_ICON_PADDING
            )
            val lp =
                LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    gravity
                )
            bordersView.addView(actionView, lp)

            return actionView
        }

        if (availableActions.contains(TouchAction.button_close)) createActionView(
            R.drawable.layer_text_delete, Gravity.END or Gravity.TOP
        ).setOnClickListener {
            editWrapperHelper.removeAction()
        }

        if (availableActions.contains(TouchAction.button_duplicate))
            createActionView(
                R.drawable.layer_text_copy, Gravity.START or Gravity.TOP
            ).setOnClickListener {
                editWrapperHelper.copyAction()
            }
        if (availableActions.contains(TouchAction.button_scale))
            createActionView(
                R.drawable.layer_text_scale, Gravity.END or Gravity.BOTTOM
            ).setOnTouchListener(::onTouchSize)

        if (availableActions.contains(TouchAction.button_rotate))
            createActionView(
                R.drawable.layer_text_rotation, Gravity.START or Gravity.BOTTOM
            ).setOnTouchListener(::onTouchRotate)

    }


    private fun onTouchSize(v: View, event: MotionEvent): Boolean {

        val touchPoint = PointF(event.rawX, event.rawY)
        touchPoint.y -= templateViewAndroid?.y?.roundToInt() ?: 0 // touch position Y in template

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                editWrapperHelper.startScaleAction(touchPoint)
            }
            MotionEvent.ACTION_MOVE -> {
                editWrapperHelper.scaleAction(touchPoint)
            }
            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> {
                editWrapperHelper.finishScaleAction()
            }
        }
        return true
    }

    private fun onTouchRotate(v: View, event: MotionEvent): Boolean {
        val touchPoint = PointF(event.rawX, event.rawY)
        touchPoint.y -= templateViewAndroid?.y?.roundToInt() ?: 0 // touch position Y in template
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                editWrapperHelper.startRotateAction(touchPoint)
            }
            MotionEvent.ACTION_MOVE -> {
                editWrapperHelper.rotateAction(touchPoint)
            }
        }
        return true
    }

    private fun redrawEditWrapper(bounds: Rect) {
        val borderPadding = editWrapperHelper.borderPadding.dpToPxInt()
        val offsetX = bounds.left - borderPadding - borderInset
        val offsetY = bounds.top - borderPadding - borderInset

        bordersView.updateLayoutParams<LayoutParams> {
            width = bounds.width() + borderPadding * 2 + borderInset * 2
            height = bounds.height() + borderPadding * 2 + borderInset * 2
            leftMargin = offsetX
            topMargin = offsetY
        }
        bordersView.invalidate()
    }

    class BordersView(context: Context) : FrameLayout(context) {
        private val borderWidth = 1.4f.dpToPixels()
        private val selectedBorderPaint by lazy {
            Paint().apply {
                isAntiAlias = true
                color = Color.BLACK
                style = Paint.Style.STROKE
                strokeWidth = borderWidth
            }
        }

        init {
            setWillNotDraw(false)
            clipChildren = true
            clipToPadding = true
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)

            canvas.drawRect(
                borderWidth / 2.0f + borderInset,
                borderWidth / 2.0f + borderInset,
                width - borderWidth / 2.0f - borderInset,
                height - borderWidth / 2.0f - borderInset,
                selectedBorderPaint
            )
        }
    }

    companion object {
        val ACTION_ICON_PADDING = 6.dpToPxInt()
        val borderInset = 10.dpToPxInt() + ACTION_ICON_PADDING
    }
}