package app.inspiry.views.text

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.text.TextPaint
import android.util.TypedValue
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.view.doOnNextLayout
import androidx.core.view.updateLayoutParams
import androidx.core.widget.TextViewCompat
import androidx.core.widget.doAfterTextChanged
import app.inspiry.core.media.*
import app.inspiry.font.helpers.PlatformFontObtainerImpl
import app.inspiry.font.helpers.TypefaceObtainingException
import app.inspiry.font.model.FontData
import app.inspiry.media.setToTextExceptSize
import app.inspiry.utils.removeOnClickListener
import app.inspiry.utils.toGravity
import app.inspiry.views.touch.MovableTouchHelperAndroid
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.math.roundToInt

class InnerTextHolderAndroid(
    context: Context,
    val media: MediaText,
    private val unitConverter: BaseUnitsConverter
) : FrameLayout(context), InnerTextHolder, KoinComponent {

    val textView = GenericTextLayoutAndroid(context, media)

    lateinit var drawListener: (Canvas) -> Unit
    var movableTouchHelper: MovableTouchHelperAndroid? = null
    lateinit var canDraw: () -> Boolean

    override lateinit var onTextChanged: (String) -> Unit

    val platformFontObtainerImpl: PlatformFontObtainerImpl by inject()

    override fun doOnInnerTextLayout(action: () -> Unit) {
        post {
            textView.doOnNextLayout {
                action()
            }
            textView.requestLayout()
        }
    }

    override var radius: Float
        get() = textView.radius
        set(value) {
            textView.radius = value
        }

    init {
        setWillNotDraw(false)
        clipChildren = false
        clipToPadding = false
        clipToOutline = true

        media.setToTextExceptSize(textView, platformFontObtainerImpl)
        addView(
            textView, LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER_VERTICAL
            )
        )

        textView.doAfterTextChanged {
            onTextChanged.invoke(it.toString())
        }
        this.radius = media.radius
    }

    override var letterSpacing: Float
        get() = textView.letterSpacing
        set(value) {
            textView.letterSpacing = value
        }

    override fun setLineSpacing(spacing: Float) {
        textView.setLineSpacing(0f, spacing)
    }

    override var currentFrame: Int
        get() = textView.currentFrame
        set(value) {
            textView.currentFrame = value
        }
    override val durationIn: Int
        get() = textView.genericTextHelper.durationIn
    override val durationOut: Int
        get() = textView.genericTextHelper.durationOut

    @SuppressLint("RestrictedApi")
    override fun onParentSizeChanged(width: Int, height: Int) {
        post {
            //to prevent request layout calls.


            if (textView.autoSizeTextType == AppCompatTextView.AUTO_SIZE_TEXT_TYPE_NONE || media.isCircularText()) {

                textSize = unitConverter.convertUnitToPixelsF(media.textSize, width, height)
            }

            textView.templateHeight = height
            textView.templateWidth = width
        }
    }

    override fun setOnClickListener(onClick: (() -> Unit)?) {
        if (onClick == null) {
            this.removeOnClickListener()
        }
        else {
            this.setOnClickListener { v: View ->
                onClick()
            }
        }
    }


    override fun switchToAutoSizeMode() {
        media.maxLines?.let { textView.maxLines = it }
        TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(
            textView, 1, 250,
            1, TypedValue.COMPLEX_UNIT_SP
        )
        textView.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
    }

    override fun switchToWrapContentMode() {
        TextViewCompat.setAutoSizeTextTypeWithDefaults(
            textView,
            TextViewCompat.AUTO_SIZE_TEXT_TYPE_NONE
        )
        textView.layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
        updateLayoutParams {
            height = ViewGroup.LayoutParams.WRAP_CONTENT
            width = ViewGroup.LayoutParams.WRAP_CONTENT
        }
        textView.requestLayout()
    }

    override fun refresh() {
        textView.refresh()
    }


    override var text: String
        get() {
            return textView.fullText
        }
        set(value) {
            textView.fullText = value
            textView.recompute()
        }

    override fun onTextAlignmentChange(align: TextAlign) {
        if (textView.circularRadius != 0f) {
            textView.circularGravity = align
            textView.invalidate()
        } else textView.gravity = align.toGravity()
    }

    override fun setFont(data: FontData?) {
        try {
            textView.typeface = platformFontObtainerImpl.getTypefaceFromFontData(data)
        } catch (ignored: TypefaceObtainingException) {}
    }

    override fun onColorChanged() {
        textView.invalidate()
    }

    override fun setNewTextColor(color: Int) {
        textView.setTextColor(color)
        onColorChanged()
    }

    override fun setPadding(
        layoutPosition: LayoutPosition,
        parentWidth: Int,
        parentHeight: Int,
        unitsConverter: BaseUnitsConverter,
        additionalPad: Int
    ) {
        val fontHeight = textView.getFontHeight()

        textView.setPaddingRelative(
            additionalPad + unitsConverter.convertUnitToPixels(
                layoutPosition.paddingStart,
                parentWidth,
                parentHeight, true
            ) + (media.backgroundMarginLeft * fontHeight).roundToInt(),
            additionalPad + unitsConverter.convertUnitToPixels(
                layoutPosition.paddingTop,
                parentWidth,
                parentHeight, false
            ) + (media.backgroundMarginTop * fontHeight).roundToInt(),
            additionalPad + unitsConverter.convertUnitToPixels(
                layoutPosition.paddingEnd,
                parentWidth,
                parentHeight, true
            ) + (media.backgroundMarginRight * fontHeight).roundToInt(),
            additionalPad + unitsConverter.convertUnitToPixels(
                layoutPosition.paddingBottom,
                parentWidth,
                parentHeight, false
            ) + (media.backgroundMarginBottom * fontHeight).roundToInt()
        )
    }

    override var textSize: Float
        get() = textView.textSize
        set(value) {
            textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, value)
            updateCircularTextSize(value)
        }

    override fun calcDurations(includeStartTimeToOut: Boolean): Pair<Int, Int> {
        return textView.genericTextHelper.calcDurations(includeStartTimeToOut)
    }


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawListener(canvas)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        return movableTouchHelper?.onTouchMovable(event) == true || super.onTouchEvent(event)
    }

    override fun draw(canvas: Canvas) {
        if (canDraw())
            super.draw(canvas)
    }

    override fun updateCircularTextSize(textSize: Float) {
        if (textView.circularRadius != 0f) {
            adjustTextSize(textSize) {textView.circularTextSize =it}
        }
    }

    override fun updateCircularTextRadius() {

        textView.circularRadius = width / 2f

        onTextAlignmentChange(media.innerGravity)
    }

    private val tempPaint by lazy { TextPaint() }
    override fun adjustTextSize(newTextSize: Float, action: (textSize: Float) -> Unit) {

        //fixes a crash on android 7.1.1
        if (textView.layout == null) {
            action(newTextSize)
            return
        }

        tempPaint.set(textView.paint)
        tempPaint.textSize = newTextSize
        var circleLength = -1f
        var textwidth = 0f
        while ((textwidth > circleLength) && tempPaint.textSize > 1) {
            val fontSemiHeight = (tempPaint.fontMetrics.bottom - tempPaint.fontMetrics.top)
            circleLength = (textView.circularRadius - fontSemiHeight) * 2f * Math.PI.toFloat()
            textwidth = tempPaint.getRunAdvance(
                text,
                0,
                text.length,
                0,
                text.length,
                textView.layout.isRtlCharAt(0),
                text.length
            )
            if (textwidth > circleLength) tempPaint.textSize--
        }
        action(tempPaint.textSize)
    }
}