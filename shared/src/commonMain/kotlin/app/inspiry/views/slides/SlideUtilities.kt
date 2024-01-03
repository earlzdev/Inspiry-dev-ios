package app.inspiry.views.slides

import app.inspiry.core.media.*
import app.inspiry.core.util.PickMediaResult
import app.inspiry.views.template.TemplateMode

interface SlideUtilities {

    fun unpackAllSlides(template: Template, templateMode: TemplateMode)

    fun onRemoveSlide(
        parent: MediaGroup,
        medias: List<Media>,
        slides: SlidesData,
        templateMode: TemplateMode,
        toRemove: MediaImage
    ): List<MediaImage>

    fun addSlides(
        parent: MediaGroup,
        medias: List<Media>,
        slides: SlidesData,
        templateMode: TemplateMode,
        sourcesToAdd: List<PickMediaResult>
    ): List<MediaImage>

    fun unpackSlides(
        parent: MediaGroup,
        medias: List<Media>,
        slides: SlidesData,
        templateMode: TemplateMode
    ): List<MediaImage>

    fun replaceSlides(
        slidesParent: SlidesParent,
        sources: List<PickMediaResult>,
        onReplaced: () -> Unit = {}
    )

    fun moveSlides(
        slidesParent: SlidesParent,
        movedID: String,
        toIndex: Int,
        onFinished: () -> Unit = {}
    )

    fun appendNewSlides(
        slidesParent: SlidesParent,
        source: List<PickMediaResult>,
        onFinished: () -> Unit = {}
    )
}
