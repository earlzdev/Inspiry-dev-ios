package app.inspiry.views.androidhelper

import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import app.inspiry.R
import app.inspiry.core.media.GroupOrientation
import app.inspiry.core.media.LayoutPosition
import app.inspiry.media.toAndroidGravity
import app.inspiry.views.InspParent
import app.inspiry.views.group.InspGroupView
import app.inspiry.views.template.InspTemplateView

interface InspLayoutParams {
    val layoutPosition: LayoutPosition
    var widthFactor: Float
    var heightFactor: Float


    companion object {
        const val TAG_INSP_VIEW = com.afollestad.materialdialogs.input.R.id.ghost_view_holder
    }
}

class InspFrameLayoutParams(
    override val layoutPosition: LayoutPosition,
    override var widthFactor: Float = 1f,
    override var heightFactor: Float = 1f
) :
    FrameLayout.LayoutParams(
        WRAP_CONTENT,
        WRAP_CONTENT,
        layoutPosition.alignBy.toAndroidGravity()
    ), InspLayoutParams

class InspLinearLayoutParams(
    override val layoutPosition: LayoutPosition,
    override var widthFactor: Float = 1f,
    override var heightFactor: Float = 1f
) :
    LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT), InspLayoutParams {
        init {
            gravity = layoutPosition.alignBy.toAndroidGravity()
        }
    }


fun InspParent.createLayoutParams(layoutPosition: LayoutPosition): ViewGroup.LayoutParams {

    if (this is InspTemplateView || (this is InspGroupView && this.media.orientation == GroupOrientation.Z)) {
        return InspFrameLayoutParams(layoutPosition)
    } else {
        return InspLinearLayoutParams(layoutPosition)
    }
}

