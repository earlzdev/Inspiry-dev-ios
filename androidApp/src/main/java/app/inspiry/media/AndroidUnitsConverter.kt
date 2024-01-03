package app.inspiry.media

import android.content.Context
import android.view.ViewGroup
import app.inspiry.core.media.BaseUnitsConverter
import app.inspiry.utils.dpToPixels
import app.inspiry.utils.spToPixels

class AndroidUnitsConverter(val context: Context): BaseUnitsConverter() {

    override fun Float.convertSp(): Float {
        return context.spToPixels(this)
    }

    override fun Float.convertDp(): Float {
        return context.dpToPixels(this)
    }

    override fun getMatchParentValue(): Float {
        return ViewGroup.LayoutParams.MATCH_PARENT.toFloat()
    }

    override fun getWrapContentValue(): Float {
        return ViewGroup.LayoutParams.WRAP_CONTENT.toFloat()
    }

    override fun getScreenHeight(): Int {
        return context.resources.displayMetrics.heightPixels
    }

    override fun getScreenWidth(): Int {
        return context.resources.displayMetrics.widthPixels
    }
}