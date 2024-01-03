package app.inspiry.views.androidhelper

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.RenderEffect
import android.graphics.Shader
import android.util.AttributeSet

class BlurImageViewS : BaseBlurImageView {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    @SuppressLint("NewApi")
    override fun setBlurRadius(radius: Float, async: Boolean) {
        setRenderEffect(
            if (radius == 0f) null else RenderEffect.createBlurEffect(
                radius,
                radius,
                Shader.TileMode.MIRROR
            )
        )
    }
}