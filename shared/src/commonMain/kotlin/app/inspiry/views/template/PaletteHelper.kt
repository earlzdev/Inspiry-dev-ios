package app.inspiry.views.template

import app.inspiry.MR
import app.inspiry.core.data.PredefinedTemplatePath
import app.inspiry.core.media.*
import app.inspiry.core.template.findMediaRecursive
import app.inspiry.core.util.PredefinedColors
import app.inspiry.palette.getViewAndLayerIdOfVector
import app.inspiry.palette.model.AbsPaletteColor
import app.inspiry.palette.model.PaletteColor
import app.inspiry.palette.model.PaletteLinearGradient
import app.inspiry.views.group.InspGroupView
import app.inspiry.views.path.InspPathView
import app.inspiry.views.text.InspTextView
import app.inspiry.views.vector.InspVectorView
import dev.icerock.moko.resources.getAssetByFilePath
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

fun InspTemplateView.mayAddBgImageFromPalette() {
    if (template.palette.backgroundImage != null) {
        val existingBg = template.medias.find { it.id == "background" }
        if (existingBg == null) {
            val newMedia =
                getBackgroundMediaPalette(
                    template.palette.backgroundImage!!,
                    template.palette.backgroundVideoStartMs,
                    template.palette.backgroundVideoLooped
                )
            template.medias.add(0, newMedia)
        }
    }
}


fun InspTemplateView.setGradientForElement(
    type: String,
    id: String?,
    gradient: PaletteLinearGradient
) {
    when (type) {
        "textColor" -> {
            if (id == PALETTE_ID_ALL_TEXTS) {
                allTextViews.forEach {
                    it.setNewTextGradient(gradient)
                }
            } else {
                val view =
                    allViews.find { it.media.id == id } as? InspTextView?
                view?.setNewTextGradient(gradient)
            }
        }
        "pathColor" -> {
            val view = allViews.find { it.media.id == id } as? InspPathView
            view?.setNewGradient(gradient)
        }
        "elementBackgroundColor" -> {
            val view = allViews.find { it.media.id == id } as? InspTextView?
            view?.setNewBackgroundGradient(gradient)
        }
    }
}

fun InspTemplateView.setColorForElement(type: String, id: String?, choiceColor: Int) {

    when (type) {
        "background" -> {
            //setBackgroundColor(choiceColor)
        }
        "vector" -> {
            val (viewId: String?, layerId: String?) = id.getViewAndLayerIdOfVector()

            val view = allViews.find { it.media.id == viewId } as? InspVectorView?

            if (layerId != null) {
                view?.setColorForLayer(layerId, choiceColor)
            } else {

                view?.setColorFilter(choiceColor)
            }

        }
        "image" -> {
            val view = mediaViews.find { it.media.id == id }
            view?.setColorFilter(choiceColor, 1f)
        }
        "textColor" -> {
            if (id == PALETTE_ID_ALL_TEXTS) {
                allTextViews.forEach {
                    it.setNewTextColor(choiceColor)
                }
            } else {
                val view =
                    allViews.find { it.media.id == id } as? InspTextView?
                view?.setNewTextColor(choiceColor)
            }
        }
        "pathColor" -> {
            val view = allViews.find { it.media.id == id } as? InspPathView
            view?.setNewColor(choiceColor)
        }
        "elementBackgroundColor" -> {
            allViews.find { it.media.id == id }
                ?.setNewBackgroundColor(choiceColor)
        }
        "borderColor" -> {
            val view = mediaViews.find { it.media.id == id }
            view?.setNewBorderColor(choiceColor)
        }
        else -> throw IllegalStateException("unknown element ${type}")
    }
}

fun InspTemplateView.applyPalette(
    applyBgImage: Boolean = true,
    applyBgColor: Boolean = true,
    checkInitializedWhenApplyingImage: Boolean = false
) {

    val p = template.palette

    if (applyBgImage) {
        val existingBg = mediaViews.find { it.media.id == "background" }
        if (p.backgroundImage != null) {

            if (existingBg != null) {
                val backgroundImage = p.backgroundImage!!
                if (existingBg.media.originalSource != backgroundImage) {
                    if (p.backgroundVideoStartMs != null) {
                        existingBg.insertNewVideo(
                            p.backgroundVideoStartMs!!, backgroundImage, 0,
                            p.backgroundVideoLooped ?: false, false
                        )

                    } else {
                        existingBg.onNewImagePicked(backgroundImage, false)
                    }
                }

            } else {

                val bgMedia =
                    getBackgroundMediaPalette(
                        p.backgroundImage!!,
                        p.backgroundVideoStartMs,
                        p.backgroundVideoLooped
                    )
                addMediaView(bgMedia, {

                    if (checkInitializedWhenApplyingImage) {
                        waitInitialize(it)
                        checkInitialized()
                    }
                    it.refresh()

                }, newIndex = 0)
            }
        } else if (existingBg != null) {
            removeInspView(existingBg)
        }
    }

    if (applyBgColor) {

        logger.debug { "applyPaletteBg ${p.mainColor}" }

        for (it in p.choices) {
            val choiceColor = it.color ?: continue
            it.elements.forEach { element ->
                val color: Int = element.colorFilter?.applyFilter(choiceColor) ?: choiceColor
                setColorForElement(element.type, element.id, color)
            }
        }

        if (p.mainColor is PaletteLinearGradient) {
            setBackgroundColor(p.mainColor)

        } else if (p.backgroundImage != null) {
            setBackgroundColor(null)
        } else {
            val bgColor = template.palette.getBackgroundColor()
            val lacksBgColor = bgColor == PredefinedColors.TRANSPARENT

            if (lacksBgColor && shouldHaveBackground) {
                setBackgroundColor(PredefinedColors.WHITE_ARGB)
                mediaViews.forEach { it.templateBackgroundChanged() }
                allViews.filterIsInstance<InspGroupView>()
                    .forEach { it.templateBackgroundChanged() }

            } else if (shouldHaveBackground || !lacksBgColor) {
                setBackgroundColor(bgColor)
                mediaViews.forEach { it.templateBackgroundChanged() }
                allViews.filterIsInstance<InspGroupView>()
                    .forEach { it.templateBackgroundChanged() }
            }
        }
    }
}

fun InspTemplateView.revertPaletteColors() {

    containerScope.launch {

        val originalTemplate = withContext(Dispatchers.Default) {
            templateSaver.loadTemplateFromPath(PredefinedTemplatePath(MR.assets.getAssetByFilePath(template.getOriginalPath())))
        }
        val originalPalette = originalTemplate.palette

        val currentTemplate = template
        currentTemplate.palette = originalPalette

        if (!originalPalette.choices.all { it.color != null }) {

            originalPalette.choices.forEach {
                if (it.color == null) {
                    it.elements.forEach {
                        val color = findColorOfElement(template, it.type, it.id)
                        if (color != null) {

                            if (color is PaletteLinearGradient) {
                                setGradientForElement(
                                    it.type,
                                    it.id,
                                    color
                                )
                            } else {
                                setColorForElement(
                                    it.type,
                                    it.id,
                                    color.getFirstColor()
                                )
                            }

                        } else if (it.type == "image") {
                            val view = mediaViews.find { view -> view.media.id == it.id }
                            view?.setColorFilter(null, 1f)
                        } else if (it.type == "vector") {
                            val view =
                                allViews.find { view -> view.media.id == it.id } as? InspVectorView?
                            view?.setColorFilter(null)
                        }
                    }
                }
            }
        }
        applyPalette(true, true, checkInitializedWhenApplyingImage = true)
        isChanged.value = true
    }
}


private fun findColorOfElement(template: Template, type: String, id: String?): AbsPaletteColor? {
    return when (type) {
        "textColor" -> {

            if (id != PALETTE_ID_ALL_TEXTS) {
                val text = template.findMediaRecursive(id!!) as? MediaText?
                text?.textGradient ?: text?.textColor?.let { PaletteColor(it) }

            } else {
                null
            }
        }
        "pathColor" -> {
            val media = template.findMediaRecursive(id!!) as? MediaPath?
            media?.gradient ?: media?.color?.let { PaletteColor(it) }
        }
        "elementBackgroundColor" -> {
            val element = template.findMediaRecursive(id!!)
            if (element is MediaText && element.backgroundGradient != null) {
                element.backgroundGradient
            } else
                element?.backgroundColor?.let { PaletteColor(it) }
        }
        "borderColor" -> {
            when (val media = template.findMediaRecursive(id!!)) {
                is MediaImage -> media.borderColor?.let { PaletteColor(it) }
                is MediaGroup -> media.borderColor?.let { PaletteColor(it) }
                else -> null
            }

        }
        else -> null
    }
}

private fun getBackgroundMediaPalette(
    uri: String,
    videoStartMs: Int?,
    isLoopEnabled: Boolean?
): MediaImage {
    val media = MediaImage(
        layoutPosition = LayoutPosition(
            "match_parent",
            "match_parent", Alignment.center
        ), id = MEDIA_ID_BACKGROUND
    )

    media.originalSource = uri
    media.isEditable = true
    if (videoStartMs != null) {
        media.isVideo = true
        media.videoStartTimeMs = MutableStateFlow(videoStartMs)
        media.isLoopEnabled = isLoopEnabled
    }

    return media
}