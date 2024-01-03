package app.inspiry.core.template

import app.inspiry.core.animator.TextAnimationParams
import app.inspiry.core.data.TemplatePath
import app.inspiry.core.helper.ABTemplateAvailability
import app.inspiry.core.media.*
import app.inspiry.core.serialization.TemplateSerializer
import app.inspiry.core.util.PredefinedColors
import kotlinx.serialization.json.Json

object TemplateUtils {
    const val TEMPLATE_MAX_POSSIBLE_FRAMES = 5000
    const val TEMPLATE_MIN_POSSIBLE_FRAMES = 30

    const val NEW_TEXT_MIN_FRAMES = 90

    fun parseTemplate(
        str: String, path: TemplatePath,
        json: Json, abTemplateAvailability: ABTemplateAvailability
    ): Template {

        val template = json.decodeFromString(deserializer = TemplateSerializer, str)
        val availability: TemplateAvailability = template.availability
        val newOriginalTemplatePath = path.getOriginalPathAsset(template)

        template.availability =
            abTemplateAvailability.getTemplateAvailability(availability, newOriginalTemplatePath)
        postProcessMedias(template.medias)

        return template
    }

    private fun TextAnimationParams.mayCopyBgAnimators() {
        if (textPartType == TextPartType.line && backgroundAnimatorGroups.isNullOrEmpty()) {
            backgroundAnimatorGroups = textAnimatorGroups
        }
    }

    fun postProcessMedia(it: Media) {
        if (it is MediaGroup) {
            postProcessMedias(it.medias)
        } else if (it is MediaText) {

            if (it.backgroundColor == PredefinedColors.TRANSPARENT) {
                it.animationParamIn?.mayCopyBgAnimators()
                it.animationParamOut?.mayCopyBgAnimators()
            }
        }
    }

    private fun postProcessMedias(medias: List<Media>) {
        medias.forEach {
            postProcessMedia(it)
        }
    }
}

fun Template.findMediaRecursive(id: String, medias: List<Media> = this.medias): Media? {
    medias.forEach {
        if (it.id == id) return it
        if (it is MediaGroup) {
            val r = findMediaRecursive(id, it.medias)
            if (r != null) return r
        }
    }
    return null
}
