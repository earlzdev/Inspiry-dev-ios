package app.inspiry.views.touch

import app.inspiry.views.InspView

interface MovableTouchHelperFactory {
    fun create(view: InspView<*>): MovableTouchHelper
}