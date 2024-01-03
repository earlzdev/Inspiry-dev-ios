package app.inspiry.views.androidhelper

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView

abstract class BaseBlurImageView: AppCompatImageView {
    abstract fun setBlurRadius(radius: Float, async: Boolean)

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
}