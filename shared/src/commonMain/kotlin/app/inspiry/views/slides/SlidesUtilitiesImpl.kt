package app.inspiry.views.slides

import app.inspiry.core.animator.InspAnimator
import app.inspiry.core.animator.appliers.FadeAnimApplier
import app.inspiry.core.animator.appliers.ToAsFromSwappableAnimApplier
import app.inspiry.core.log.GlobalLogger
import app.inspiry.core.manager.DebugManager
import app.inspiry.core.manager.DurationCalculator
import app.inspiry.core.media.*
import app.inspiry.core.serialization.MediaSerializer
import app.inspiry.core.template.MediaIdGenerator
import app.inspiry.core.util.PickMediaResult
import app.inspiry.edit.instruments.PickedMediaType
import app.inspiry.views.group.InspGroupView
import app.inspiry.views.media.InspMediaView
import app.inspiry.views.template.TemplateMode
import app.inspiry.views.template.forEachRecursive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.json.Json

class SlidesUtilitiesImpl(
    private val json: Json,
    private val mediaIdGenerator: MediaIdGenerator
) : SlideUtilities {

    override fun unpackSlides(
        parent: MediaGroup,
        medias: List<Media>,
        slides: SlidesData,
        templateMode: TemplateMode
    ): List<MediaImage> {

        checkAllInnerMediasAreImages(medias)
        val mediaToUnpack = medias.ifEmpty { slides.predefined }
        return if (templateMode == TemplateMode.LIST_DEMO) {

            unpackInner(parent, mediaToUnpack, slides) { media ->

                if (media.demoSource.isNullOrEmpty()) {
                    throw IllegalStateException("$media should have demoSource")
                }
                true
            }

        } else {
            val res = unpackInner(parent, mediaToUnpack, slides) { media ->
                !media.originalSource.isNullOrEmpty()
            }

            // ensure we have at least one image for edit/preview
            if (res.isEmpty()) {
                val emptyImage = if (slides.predefined.isNotEmpty()) slides.predefined.first()
                    .copy(json) as MediaImage else MediaImage()

                copySlideToMedia(slides, emptyImage)

                if (slides.startFrameInterval != 0) {
                    applyStartFrameToMedia(
                        emptyImage,
                        slides.startFrame,
                        startFrameInterval = 0,
                        durationOut = 0,
                        previousDuration = 0
                    )
                }
                res.add(emptyImage)
            }
            res
        }
    }

    override fun unpackAllSlides(template: Template, templateMode: TemplateMode) {
        if (templateMode == TemplateMode.EDIT) unpackMediaToSlides(template.medias)
        template.medias.forEachRecursive {
            if (it is MediaGroup && it.slides != null && it.slides?.unpacked == false) {
                it.medias.removeAll { media -> media !is MediaImage }
                it.medias = unpackSlides(it, it.medias, it.slides!!, templateMode).toMutableList()
                it.slides?.unpacked = true
            }
        }
    }

    private fun unpackMediaToSlides(medias: MutableList<Media>) {
        val mediaToUnpack = mutableListOf<Pair<Int, MediaImage>>()
        medias.forEachIndexed { index, media ->
            if (media is MediaImage && media.slidesEnabled) {
                mediaToUnpack.add(Pair(index, media))
            }
        }
        mediaToUnpack.forEach {
            val newGroup =
                MediaGroup(
                    id = "_AUTO_SLIDES_${it.first}",
                    layoutPosition = LayoutPosition(
                        "match_parent", "match_parent",
                        Alignment.center
                    ),
                    medias = arrayListOf()
                )
            it.second.apply {

                if (animatorsIn.none { it.animationApplier is FadeAnimApplier }) {
                    val newAnimator = animatorsIn.toMutableList()
                    newAnimator.add(
                        InspAnimator(
                            duration = 1,
                            animationApplier = FadeAnimApplier()
                        )
                    )
                    animatorsIn = newAnimator
                }
            }

            newGroup.slides = SlidesData(
                startFrame = it.second.startFrame,
                maxCount = 8,
                startFrameInterval = SlidesData.START_FRAME_INTERVAL_PAUSE_AFTER_ANIMATION_IN,
                duplicateSlides = false,
                predefined = mutableListOf(it.second.apply {
                    slidesEnabled = false
                    id = null
                })
            )
            medias.removeAt(it.first)
            medias.add(it.first, newGroup)
        }
    }

    override fun onRemoveSlide(
        parent: MediaGroup,
        medias: List<Media>, slides: SlidesData,
        templateMode: TemplateMode, toRemove: MediaImage
    ): List<MediaImage> {

        val newMedias = medias.toMutableList()

        // delete this media and its duplicates or original if it is a duplicate
        newMedias.removeAll {
            (it.id != null && it.id == toRemove.id) ||
                    (toRemove.id != null && toRemove.id == (it as MediaImage).duplicate) ||
                    (toRemove.duplicate != null && toRemove.duplicate == it.id) ||
                    it == toRemove
        }
        return unpackSlides(parent, newMedias, slides, templateMode)
    }

    override fun addSlides(
        parent: MediaGroup,
        medias: List<Media>,
        slides: SlidesData,
        templateMode: TemplateMode,
        sourcesToAdd: List<PickMediaResult>
    ): List<MediaImage> {

        fun checkLength(size: Int) {
            if (size > slides.maxCount)
                throw IllegalStateException("${size}-size cannot exceed ${slides.maxCount}")
        }

        checkLength(sourcesToAdd.size)

        val availableSlots = slides.maxCount - medias.size
        val toReplace = sourcesToAdd.size - availableSlots

        val mediaToUnpackLocal = if (toReplace == medias.size) {
            mutableListOf()
        } else {
            medias.toMutableList()
        }

        if (toReplace > 0 && toReplace != medias.size) {
            val indexToReplaceFrom = medias.size - toReplace
            for (index in indexToReplaceFrom until mediaToUnpackLocal.size) {
                mediaToUnpackLocal.removeAt(index)
            }
        }

        for (sourceToAdd in sourcesToAdd) {

            /**
             * Algorithm of taking predefined media for copy:
             * the next media should be from the same order. If it exceeds the size,
             * then take first again and so on
             * todo: test
             */
            val predefinedIndexToTake = if (slides.predefined.size <= mediaToUnpackLocal.size) {
                if (slides.predefined.isNotEmpty()) {
                    mediaToUnpackLocal.size % slides.predefined.size
                } else {
                    null
                }
            } else mediaToUnpackLocal.size

            val predefinedMedia =
                if (predefinedIndexToTake == null) MediaImage() else slides.predefined[predefinedIndexToTake].copy(
                    json
                ) as MediaImage

            predefinedMedia.originalSource = sourceToAdd.uri
            predefinedMedia.isVideo = sourceToAdd.type == PickedMediaType.VIDEO

            mediaToUnpackLocal.add(predefinedMedia)
        }

        if (DebugManager.isDebug)
            checkLength(mediaToUnpackLocal.filter { (it as MediaImage).duplicate == null }.size)
        GlobalLogger.debug("addSlides") {"return unpacked.."}
        return unpackSlides(parent, mediaToUnpackLocal, slides, templateMode)
    }

    private fun addDuplicates(medias: MutableList<MediaImage>) {

        // ensure we have ids
        //todo fixme bug: ID generation within group can create copies of IDs from medias outside the group
        mediaIdGenerator.fillEmptyID(medias)

        medias.addAll(0, medias.map { makeAsDuplicate(it) })
    }

    private fun makeAsDuplicate(mediaImage: MediaImage): MediaImage {

        if (mediaImage.id == null) {
            throw IllegalStateException("id cannot be null")
        }

        val copy = mediaImage.copy(json) as MediaImage
        copy.id = "duplicate_${mediaImage.id}"
        copy.duplicate = mediaImage.id

        copy.animatorsIn.forEach {
            it.duration = 0
            if (it.animationApplier is ToAsFromSwappableAnimApplier) {
                it.animationApplier.setToAsFrom()
            }
        }

        copy.animatorsOut.forEach {
            it.duration = 0
            if (it.animationApplier is ToAsFromSwappableAnimApplier) {
                it.animationApplier.setToAsFrom()
            }
        }

        copy.startFrame = 0

        return copy
    }


    private fun copySlideToMedia(slides: SlidesData, mediaImage: MediaImage) {
        slides.animatorsIn?.let {
            mediaImage.animatorsIn = it
        }
        slides.animatorsOut?.let {
            mediaImage.animatorsOut = it
        }
        slides.animatorsAll?.let {
            mediaImage.animatorsAll = it
        }
        slides.minDuration?.let {
            mediaImage.minDuration = it
        }
        slides.delayBeforeEnd?.let {
            mediaImage.delayBeforeEnd = it
        }
        slides.templateMask?.let {
            mediaImage.templateMask = it
        }
    }


    private fun unpackInner(
        parent: MediaGroup,
        medias: List<Media>,
        slides: SlidesData,
        additionalAction: (MediaImage) -> Boolean
    ): MutableList<MediaImage> {

        val result = mutableListOf<MediaImage>()

        var startFrame: Int = slides.startFrame
        var previousDuration: Int? = null

        val mediasWithoutDuplicates = medias.filter { (it as MediaImage).duplicate == null }
        for ((index, media) in mediasWithoutDuplicates.withIndex()) {
            if (media !is MediaImage) {
                throw IllegalStateException("can\'t have ${media::class.simpleName} inside")
            }
            if (!additionalAction(media)) {
                continue
            }

            val copy = media.copy(json) as MediaImage

            copySlideToMedia(slides, copy)

            if (slides.startFrameInterval != 0) {

                val durationIn = DurationCalculator.calcDurationIn(copy.animatorsIn)
                val durationOut = DurationCalculator.calcDurationOut(copy.animatorsOut)

                /**
                 * it helps templates to add default animation (e.g. BlankWhite)
                 */
                if (durationIn <= 1 && result.size > 0) {
                    copy.animatorsIn.maxByOrNull { it.duration }?.duration =
                        SlidesData.DEFAULT_FADE_DURATION
                }
                val duration = DurationCalculator.getMinPossibleDuration(
                    copy, durationIn, durationOut
                )

                startFrame = applyStartFrameToMedia(
                    copy,
                    startFrame,
                    if (index == 0) 0 else slides.startFrameInterval,
                    durationOut,
                    previousDuration
                )
                previousDuration = duration
            }
            result.add(copy)
        }

        if (slides.duplicateSlides) {
            addDuplicates(result)
        }

        return result
    }

    /**
     * @param startFrame is startFrame that should be applied
     * @param previousDuration is needed to calculate increment of startFrame
     * @param durationOut is also needed to calculate increment of startFrame
     * @return new startFrame value
     */
    private fun applyStartFrameToMedia(
        media: MediaImage,
        startFrame: Int,
        startFrameInterval: Int,
        durationOut: Int,
        previousDuration: Int?
    ): Int {

        val additionalStartFrame: Int = if (startFrameInterval < 0) {
            throw IllegalStateException("this method should not be called if startFrameInterval is negative")

        } else if (startFrameInterval == SlidesData.START_FRAME_INTERVAL_WHEN_NEXT_OUT_BEGINS) {

            if (previousDuration != null) {
                previousDuration - durationOut
            } else 0

        } else if (startFrameInterval == SlidesData.START_FRAME_INTERVAL_PAUSE_AFTER_ANIMATION_IN) {

            if (previousDuration != null) {
                previousDuration - durationOut + SlidesData.ADDITIONAL_SLIDE_DURATION
            } else 0

        } else if (startFrameInterval == SlidesData.START_FRAME_INTERVAL_WHEN_THIS_OUT_ENDS) {
            previousDuration ?: 0

        } else if (startFrameInterval > SlidesData.START_FRAME_INTERVAL_WHEN_NEXT_OUT_BEGINS) {

            throw IllegalStateException("cannot be more than ${SlidesData.START_FRAME_INTERVAL_WHEN_NEXT_OUT_BEGINS}")
        } else {
            startFrameInterval
        }

        val newStartFrame = startFrame + additionalStartFrame

        media.startFrame = newStartFrame

        media.templateMask?.textures?.forEach {
            it.startFrame = newStartFrame
        }

        return newStartFrame
    }

    override fun replaceSlides(
        slidesParent: SlidesParent,
        sources: List<PickMediaResult>,
        onReplaced: () -> Unit
    ) {
        val groupParent = slidesParent.group
        val templateView = groupParent.templateParent
        templateView.isChanged.value = true
        slidesParent.group.media.slides?.unpacked = false

        val medias = groupParent.media.medias

        medias.removeAll { it !is MediaImage }

        val templateParent = (slidesParent as? InspGroupView)?.templateParent
            ?: throw IllegalStateException("replaceSlides: slidesParent is not group")
        GlobalLogger.debug("replaceSlides") {"clear parent inspviews: $medias"}
        slidesParent.inspChildren.toList().forEach {
            if (it is InspGroupView) {
                templateParent.removeChildrenRecursive(it, true)
            }
            if (it is InspMediaView && it.media.duplicate != null) {
                groupParent.removeViewFromHierarchy(it)
                templateParent.allViews.remove(it)
            }
        }

        GlobalLogger.debug("replaceSlides") {"create new medias.."}
        val addedMedias = addSlides(
            parent = slidesParent.media,
            medias = slidesParent.group.media.medias,
            slides = slidesParent.group.media.slides!!,
            templateMode = TemplateMode.EDIT,
            sourcesToAdd = sources
        )
        GlobalLogger.debug("replaceSlides") {"removeChildrenFromGroup"}
        templateParent.removeChildrenRecursive(slidesParent.group)

        addedMedias.forEach {
            if (it.isVideo) {
                if (it.videoStartTimeMs == null) {
                    it.videoStartTimeMs = MutableStateFlow(0)
                }
                if (it.videoVolume == null) {
                    it.videoVolume = MutableStateFlow(1f)
                }
            }
        }
        medias.addAll(0, addedMedias)

        with(templateView) {

            unpackTemplateModels(templateView.template)


            needToInitAllChildren(slidesParent.group.media.medias)
            checkInitialized()

            addMediaViews(
                slidesParent.group.media.medias,
                slidesParent.group
            ) {

                if (it.media == slidesParent.group.media.medias.last()) {
                    val slideDuration =
                        slidesParent.group.calculateLastFrame() + SlidesData.TEMPLATE_AFTER_SLIDES_DURATION
                    val templateDuration = getDuration()
                    if (templateDuration < slideDuration) setNewDuration(slideDuration)
                    if (templateDuration > slideDuration) {
                        template.initialDuration?.let { initial -> setNewDuration(initial) }
                    }
                    onReplaced()
                }
            }
        }
    }

    override fun appendNewSlides(
        slidesParent: SlidesParent,
        source: List<PickMediaResult>,
        onFinished: () -> Unit
    ) {
        replaceSlides(slidesParent, source) { onFinished() }
    }

    override fun moveSlides(
        slidesParent: SlidesParent,
        movedID: String,
        toIndex: Int, //-1 for remove
        onFinished: () -> Unit
    ) {
        val movedIndex = slidesParent.getSlidesViews().indexOfFirst { it.media.id == movedID }
        val movedMedia = slidesParent.getSlidesMedia().find { it.id == movedID }
            ?: throw IllegalStateException("slide not found ID ($movedID)")
        val templateView = slidesParent.group.templateParent
        slidesParent.inspChildren.toList().forEach {
            if (it is InspGroupView) {
                templateView.removeChildrenRecursive(it, true)
            }
            if (it is InspMediaView && it.media.duplicate != null) {
                slidesParent.group.removeViewFromHierarchy(it, true)
            }
        }

        slidesParent.group.media.medias.remove(movedMedia)
        if (toIndex >= 0) slidesParent.group.media.medias.add(toIndex, movedMedia)
        replaceSlides(slidesParent.group, emptyList()) {
            if (toIndex < 0) slidesParent.selectSlide(movedIndex)
            onFinished()
        }

    }

    private fun checkAllInnerMediasAreImages(medias: List<Media>) {
        for (m in medias) {
            if (m !is MediaImage)
                throw IllegalStateException(
                    "can\'t have ${m::class.simpleName} inside ${
                        json.encodeToJsonElement(
                            MediaSerializer,
                            m
                        )
                    }"
                )
        }
    }
}