package app.inspiry.views.touch

import app.inspiry.views.InspView
import app.inspiry.views.guideline.GuidelineManagerAndroid

class MovableTouchHelperFactoryImpl: MovableTouchHelperFactory {
    override fun create(view: InspView<*>): MovableTouchHelper {
        return MovableTouchHelperAndroid(view, GuidelineManagerAndroid())
    }
}