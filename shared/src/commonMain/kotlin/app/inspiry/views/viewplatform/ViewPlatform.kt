package app.inspiry.views.viewplatform

import app.inspiry.core.media.BaseUnitsConverter
import app.inspiry.core.media.LayoutPosition
import app.inspiry.core.media.Media
import app.inspiry.palette.model.PaletteLinearGradient

interface ViewPlatform {
    fun hideView()
    fun showView()
    fun invalidateRotationParentChanged()
    fun setElevation(value: Float)
    fun setPadding(layoutPosition: LayoutPosition, parentWidth: Int, parentHeight: Int, unitsConverter: BaseUnitsConverter)
    fun setMargin(layoutPosition: LayoutPosition, parentWidth: Int, parentHeight: Int, unitsConverter: BaseUnitsConverter)
    fun setBackground(media: Media)
    fun setBackground(gradient: PaletteLinearGradient)
    fun setBackgroundColor(color: Int)
    fun setAlpha(alpha: Float)
    fun vibrateOnGuideline()
    val paddingBottom: Int
    val paddingTop: Int
    val paddingRight: Int
    val paddingLeft: Int
    val width: Int
    val height: Int
    var translationX: Float
    var translationY: Float
    var rotation: Float
    var scaleY: Float
    var scaleX: Float
    fun doOnPreDraw(action: () -> Unit)
    fun clickZoneIncrease(addleft: Int, addright: Int, addTop: Int, addBottom: Int)
    fun invalidate()
    fun setSizeFromAnimation(widthFactor: Float, heightFactor: Float)
    fun changeSize(width: Float, height: Float)

    var onDetachListener: (() -> Unit)?
    var onAttachListener: (() -> Unit)?

    //w, h, oldW, oldH
    var onSizeChangeListener: ((Int, Int, Int, Int) -> Unit)?

    val x: Float
    val y: Float
}