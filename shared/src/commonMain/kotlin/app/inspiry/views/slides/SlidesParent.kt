package app.inspiry.views.slides

import app.inspiry.core.media.MediaImage
import app.inspiry.views.InspView
import app.inspiry.views.group.InspGroupView
import app.inspiry.views.media.InspMediaView

interface SlidesParent {
    val inspChildren: List<InspView<*>>
    val group: InspGroupView
    val maxSlides: Int

    fun getSlidesCount(ignorePlaceHolder: Boolean = false): Int
    fun selectLastSlide()
    fun selectSlide(slideIndex: Int)
    fun getSlidesMedia(): List<MediaImage>
    fun getSlidesViews(): List<InspMediaView>

    fun selectFirstSlide()
}