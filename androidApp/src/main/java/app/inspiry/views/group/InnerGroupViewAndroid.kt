package app.inspiry.views.group

import android.graphics.Canvas

interface InnerGroupViewAndroid {
    fun originalDraw(canvas: Canvas)

    var mDrawAnimations: (Canvas) -> Unit
    //(fromOnDraw: Boolean) -> Boolean
    var mDrawOnGlCanvas: (Boolean) -> Boolean

    var cornerRadius: Float
}