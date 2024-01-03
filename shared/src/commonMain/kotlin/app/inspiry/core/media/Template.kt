package app.inspiry.core.media

import app.inspiry.core.data.OriginalTemplateData
import app.inspiry.core.data.TemplatePath
import app.inspiry.core.data.templateCategory.TemplateCategoryProvider
import app.inspiry.core.manager.DebugManager
import app.inspiry.core.serialization.MediaSerializer
import app.inspiry.core.serialization.TemplateSerializer
import app.inspiry.core.util.PredefinedColors
import app.inspiry.music.model.TemplateMusic
import app.inspiry.palette.model.TemplatePalette
import app.inspiry.views.template.forEachRecursive
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.jvm.JvmOverloads

@Serializable
@SerialName("template")
class Template(
    var availability: TemplateAvailability = TemplateAvailability.FREE,
    val medias: MutableList<@Serializable(with = MediaSerializer::class) Media> = mutableListOf(),
    var palette: TemplatePalette = TemplatePalette.withBackgroundColor(PredefinedColors.TRANSPARENT),
    var name: String? = null,
    var clipChildren: Boolean = false,
    var preferredDuration: Int = 0,
    var initialDuration: Int? = null,
    var maxDuration: Int? = null,
    var videoDemo: String? = null,
    // This is always not null only in EditActivity. When it is non null?
    // when template is saved. because originally templates don't have this data in assets
    // if templatePath is UserSavedTemplatePath then it is not null
    var originalData: OriginalTemplateData? = null,
    //from 0 to duration
    var timeForEdit: Int? = null,
    var format: TemplateFormat = TemplateFormat.story,
    var music: TemplateMusic? = null,
    //adding images will be sorted by ID if imgOrderById = true
    var imgOrderById: Boolean = false,
    //duration of the animation for listDemo
    var listDemoPreferredDuration: Int? = null,
    var initialPalette: TemplatePalette? = null
) {
    //for inner templates (stickers, animations etc) need for ios
    constructor (medias: MutableList<Media>, preferredDuration: Int): this(medias = medias, timeForEdit = 0, preferredDuration = preferredDuration)

    fun getOriginalPath() = originalData!!.originalPath

    fun availableForUser(
        hasAllInclusive: Boolean,
        templatePath: TemplatePath, categoryProvider: TemplateCategoryProvider
    ): Boolean {

        if (availability == TemplateAvailability.FREE || hasAllInclusive)
            return true

        return categoryProvider.getFreeThisWeek()
            ?.contains(templatePath.getOriginalPathAsset(this)) == true
    }

    fun getFilesToClean(): List<String> {
        val files = mutableListOf<String>()

        medias.forEachRecursive {
            files.addAll(it.getFilesToClean())
        }

        music?.let { files.add(it.url) }

        return files
    }

    override fun toString(): String {
        return "Template(medias=$medias)"
    }

    fun getNameForShare(): String {
        return if (!name.isNullOrEmpty())
            name!!.replace('/', '_')
        else
            "${originalData!!.originalCategory}-${originalData!!.originalIndexInCategory + 1}"
    }

    fun toJsonString(json: Json): String {
        return json.encodeToString(TemplateSerializer, this)
    }


    fun forEachMedias(action: (MutableList<Media>) -> Unit) {
        action(medias)
        medias.forEachRecursive {
            if (it is MediaGroup) action(it.medias)
        }
    }
}