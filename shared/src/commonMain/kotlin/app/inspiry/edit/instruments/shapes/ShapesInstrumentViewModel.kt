package app.inspiry.edit.instruments.shapes

import app.inspiry.core.analytics.AnalyticsManager
import app.inspiry.core.animator.clipmask.shape.ShapeType
import app.inspiry.core.animator.clipmask.shape.icon
import app.inspiry.core.ui.CommonMenu
import app.inspiry.edit.instruments.BottomInstrumentsViewModel
import app.inspiry.views.InspView
import app.inspiry.views.media.InspMediaView
import kotlinx.coroutines.flow.MutableStateFlow

class ShapesInstrumentViewModel(
    val inspView: InspMediaView,
    val analyticsManager: AnalyticsManager,
) : BottomInstrumentsViewModel {

    val aspectRatio = MutableStateFlow(inspView.viewWidth.toFloat() / inspView.viewHeight.toFloat())
    val currentView = MutableStateFlow(inspView)

    override fun onSelectedViewChanged(newSelected: InspView<*>?) {
        currentView.value = newSelected as? InspMediaView ?: return
        aspectRatio.value = inspView.viewWidth.toFloat() / inspView.viewHeight.toFloat()
        initialShape = currentView.value.shapeState.value
    }

    fun selectShape(shape: ShapeType?) {
        currentView.value.setNewShape(shape)
    }

    //for analytics only
    private var initialShape = currentView.value.shapeState.value
    override fun onHide() {
        super.onHide()
        currentView.value.shapeState.value?.let {
            if (initialShape != it) analyticsManager.onShapeChanged(it)
        }

    }

    fun getIcon(shape: ShapeType): String {
        return shape.icon()
    }

    companion object {
        val shapesList = ShapeType.values().toList()
    }
}