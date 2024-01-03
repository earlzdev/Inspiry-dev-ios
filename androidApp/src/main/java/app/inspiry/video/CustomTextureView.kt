package app.inspiry.video

import android.content.Context
import android.util.AttributeSet
import android.view.TextureView
import android.view.View

class CustomTextureView : TextureView {

    private var lifecycleListener: VisibilityListener? = null
    private var lastVisibility: Int = -1

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    fun registerLifecycleListener(visibilityListener: VisibilityListener) {
        lastVisibility = visibility
        this.lifecycleListener = visibilityListener
    }

    fun unregisterLifecycleListener() {
        lifecycleListener = null
    }

    fun lastVisibilityVisible() = lastVisibility == View.VISIBLE

    override fun onVisibilityChanged(changedView: View, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)
        if (lastVisibility != visibility && lifecycleListener != null) {
            if (visibility == View.VISIBLE) lifecycleListener?.onViewVisible()
            else lifecycleListener?.onViewInvisible()
            lastVisibility = visibility
        }
    }

    interface VisibilityListener {
        fun onViewVisible()
        fun onViewInvisible()
    }
}