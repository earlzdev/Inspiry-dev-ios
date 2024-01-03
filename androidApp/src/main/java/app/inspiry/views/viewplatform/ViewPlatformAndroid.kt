package app.inspiry.views.viewplatform

import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.view.HapticFeedbackConstants
import android.view.TouchDelegate
import android.view.View
import android.view.ViewGroup
import androidx.core.view.doOnPreDraw
import androidx.core.view.updateLayoutParams
import app.inspiry.core.data.Rect
import app.inspiry.core.media.BaseUnitsConverter
import app.inspiry.core.media.LayoutPosition
import app.inspiry.core.media.Media
import app.inspiry.core.util.PredefinedColors
import app.inspiry.media.toJava
import app.inspiry.palette.model.PaletteLinearGradient
import app.inspiry.utils.setMarginToView
import app.inspiry.views.InspView
import app.inspiry.views.androidhelper.InspLayoutParams
import kotlin.math.roundToInt

class ViewPlatformAndroid(val view: View): ViewPlatform {

    override var onDetachListener: (() -> Unit)? = null
    override var onAttachListener: (() -> Unit)? = null
    override var onSizeChangeListener: ((Int, Int, Int, Int) -> Unit)? = null

    var viewForBackground: View = view

    init {
        view.outlineProvider = null

        view.addOnAttachStateChangeListener(object: View.OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(v: View?) {
                onAttachListener?.invoke()
            }

            override fun onViewDetachedFromWindow(v: View?) {
                onDetachListener?.invoke()
            }
        })

        view.addOnLayoutChangeListener { v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
            onSizeChangeListener?.invoke(
                right - left,
                bottom - top,
                oldRight - oldLeft,
                oldBottom - oldTop
            )
        }
    }
    override fun hideView() {
        view.visibility = View.INVISIBLE
    }

    override fun showView() {
        view.visibility = View.VISIBLE
    }

    override fun invalidateRotationParentChanged() {
        view.invalidate()
    }

    override fun setElevation(value: Float) {
        view.elevation = value
    }

    override fun setPadding(
        layoutPosition: LayoutPosition,
        parentWidth: Int,
        parentHeight: Int,
        unitsConverter: BaseUnitsConverter
    ) {
        view.setPaddingRelative(
            unitsConverter.convertUnitToPixels(layoutPosition.paddingStart, parentWidth, parentHeight, true),
            unitsConverter.convertUnitToPixels(layoutPosition.paddingTop, parentWidth, parentHeight, false),
            unitsConverter.convertUnitToPixels(layoutPosition.paddingEnd, parentWidth, parentHeight, true),
            unitsConverter.convertUnitToPixels(layoutPosition.paddingBottom, parentWidth, parentHeight, false),
        )
    }

    override fun setMargin(
        layoutPosition: LayoutPosition,
        parentWidth: Int,
        parentHeight: Int,
        unitsConverter: BaseUnitsConverter
    ) {
        view.setMarginToView(
            unitsConverter.convertUnitToPixels(layoutPosition.marginTop, parentWidth, parentHeight, false),
            unitsConverter.convertUnitToPixels(layoutPosition.marginLeft, parentWidth, parentHeight, true),
            unitsConverter.convertUnitToPixels(layoutPosition.marginRight, parentWidth, parentHeight, true),
            unitsConverter.convertUnitToPixels(layoutPosition.marginBottom, parentWidth, parentHeight, false),
        )
    }

    override fun setBackgroundColor(color: Int) {
        viewForBackground.setBackgroundColor(color)
    }

    override fun setBackground(gradient: PaletteLinearGradient) {
        val drawable =
            GradientDrawable(
                gradient.orientation.toJava(),
                gradient.colors.toIntArray()
            )
        if (Build.VERSION.SDK_INT >= 29 && gradient.offsets != null)
            drawable.setColors(gradient.colors.toIntArray(), gradient.offsets)

        viewForBackground.background = drawable
    }

    override fun setBackground(media: Media) {
        if (media.backgroundGradient != null) {
            setBackground(media.backgroundGradient!!)
        } else {
            viewForBackground.background =
                if (media.backgroundColor == PredefinedColors.TRANSPARENT) null
                else ColorDrawable(media.backgroundColor)
        }
    }

    override fun setAlpha(alpha: Float) {
        view.alpha = alpha
    }

    override fun vibrateOnGuideline() {
        view.performHapticFeedback(
            HapticFeedbackConstants.KEYBOARD_TAP,
            HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING
        )
    }

    override val paddingBottom: Int
        get() = view.paddingBottom
    override val paddingTop: Int
        get() = view.paddingTop
    override val paddingRight: Int
        get() = view.paddingRight
    override val paddingLeft: Int
        get() = view.paddingLeft

    override val width: Int
        get() = view.width
    override val height: Int
        get() = view.height

    override val x: Float
        get() = view.x
    override val y: Float
        get() = view.y

    override var translationX: Float
        get() = view.translationX
        set(value) {
            view.translationX = value
        }
    override var translationY: Float
        get() = view.translationY
        set(value) {
            view.translationY = value
        }
    override var rotation: Float
        get() = view.rotation
        set(value) {
            view.rotation = value
        }
    override var scaleY: Float
        get() = view.scaleY
        set(value) {
            view.scaleY = value
        }
    override var scaleX: Float
        get() = view.scaleX
        set(value) {
            view.scaleX = value
        }

    override fun doOnPreDraw(action: () -> Unit) {
        view.doOnPreDraw { action() }
    }

    //this will be useful, but not used right now
    override fun clickZoneIncrease(addleft: Int, addright: Int, addTop: Int, addBottom: Int) {
        val parent = view.parent as View
        parent.post {
            val touchZone = Rect()
            view.getHitRect(touchZone)
            touchZone.apply {
                left -= addleft
                right += addright
                top -= addTop
                bottom += addBottom
            }
            parent.touchDelegate = TouchDelegate(touchZone, view)
        }
    }

    override fun invalidate() {
        view.invalidate()
    }

    override fun changeSize(width: Float, height: Float) {
        view.updateLayoutParams<ViewGroup.LayoutParams> {
            this.width = width.roundToInt()
            this.height = height.roundToInt()
        }
    }

    override fun setSizeFromAnimation(widthFactor: Float, heightFactor: Float) {
        if (view.layoutParams == null) return
        val lp = view.layoutParams as InspLayoutParams

        var changed = false
        if (widthFactor != -1f) {
            val oldWidthFactor = lp.widthFactor
            lp.widthFactor = widthFactor
            if (oldWidthFactor != widthFactor) changed = true
        }

        if (heightFactor != -1f) {
            val oldHeightFactor=  lp.heightFactor
            lp.heightFactor = heightFactor
            if (oldHeightFactor != heightFactor) changed = true
        }

        if (changed) view.requestLayout()
    }
}

fun InspView<*>.getAndroidView(): View {
    return (this.view as ViewPlatformAndroid).view
}