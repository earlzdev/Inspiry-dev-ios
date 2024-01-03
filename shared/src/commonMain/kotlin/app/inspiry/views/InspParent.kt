package app.inspiry.views

import app.inspiry.core.media.Media

interface InspParent {

    fun addViewToHierarchy(view: InspView<*>)
    fun addViewToHierarchy(index: Int, view: InspView<*>)
    fun removeViewFromHierarchy(view: InspView<*>, removeFromTemplateViews: Boolean = false)
    fun addMediaToList(media: Media)

    val viewWidth: Int
    val viewHeight: Int

    val inspChildren: List<InspView<*>>

    fun indexOf(child: InspView<*>): Int {
        return inspChildren.indexOf(child)
    }

    fun calculateLastFrame(): Int { return -1}
}