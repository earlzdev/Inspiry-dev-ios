package app.inspiry.views.text

import app.inspiry.font.model.FontData
import app.inspiry.core.media.BaseUnitsConverter
import app.inspiry.core.media.LayoutPosition
import app.inspiry.core.media.TextAlign

interface InnerTextHolder {
    var radius: Float
    var letterSpacing: Float
    fun setLineSpacing(spacing: Float)
    var currentFrame: Int
    val durationIn: Int
    val durationOut: Int
    //in px
    var textSize: Float
    var text: String

    var onTextChanged: (String) -> Unit

    fun doOnInnerTextLayout(action: () -> Unit)


    fun onParentSizeChanged(width: Int, height: Int)
    fun setOnClickListener(onClick: (() -> Unit)?)
    fun switchToAutoSizeMode()
    fun switchToWrapContentMode()

    fun requestLayout()
    fun refresh()

    fun onTextAlignmentChange(align: TextAlign)
    fun setFont(data: FontData?)
    fun onColorChanged()
    fun setNewTextColor(color: Int)

    fun setPadding(layoutPosition: LayoutPosition, parentWidth: Int,
                   parentHeight: Int, unitsConverter: BaseUnitsConverter, additionalPad: Int)


    fun calcDurations(includeStartTimeToOut: Boolean): Pair<Int, Int>
    fun updateCircularTextRadius()
    fun updateCircularTextSize(textSize: Float)
    fun adjustTextSize(newTextSize: Float, action: (textSize: Float) -> Unit)
}