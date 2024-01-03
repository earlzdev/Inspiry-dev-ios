package app.inspiry.views.timeline

import android.view.View

interface TimelineInsideView {

    var startTime: Double
    var duration: Double
    var minDuration: Double
    val view: View

    fun getTimeline() = view.selectParentUntil<TimelineView>()!!

    fun setCurrentTime(time: Double) {}

    fun isSelectedView() = getTimeline().selectedView == this
}

inline fun <reified T> View.selectParentUntil(): T? {
    var parent = parent
    while (parent != null) {
        if (parent is T) return parent
        else parent = parent.parent
    }
    return null
}