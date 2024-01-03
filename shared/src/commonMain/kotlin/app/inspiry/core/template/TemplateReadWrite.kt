package app.inspiry.core.template

import app.inspiry.core.data.OriginalTemplateData
import app.inspiry.core.data.PredefinedTemplatePath
import app.inspiry.core.data.TemplatePath
import app.inspiry.core.data.UserSavedTemplatePath
import app.inspiry.core.data.templateCategory.TemplateCategory
import app.inspiry.core.database.ExternalResourceDao
import app.inspiry.core.helper.ABTemplateAvailability
import app.inspiry.core.manager.DefaultDirs
import app.inspiry.core.manager.FileReadWrite
import app.inspiry.core.media.Template
import app.inspiry.core.util.WorkerThread
import app.inspiry.core.util.getExt
import app.inspiry.core.util.removeScheme

import com.soywiz.klock.DateTime
import kotlinx.coroutines.flow.*
import kotlinx.serialization.json.Json
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath

class TemplateReadWrite(
    val json: Json,
    private val fileReadWrite: FileReadWrite,
    private val abTemplateAvailability: ABTemplateAvailability,
    private val fileSystem: FileSystem
) {

    internal fun myStoriesFolder(): Path {
        return myStoriesFolderBase(DefaultDirs.contentsDirectory!!)
    }

    internal fun myStoriesFolderBase(baseDir: String): Path {
        val path = baseDir.toPath().resolve("my-stories")
        fileSystem.createDirectories(path, mustCreate = false)
        return path
    }

    fun loadTemplateFromPath(path: TemplatePath): Template {

        val jsonString = when (path) {
            is PredefinedTemplatePath -> {
                fileReadWrite.readContentFromAssets(path.res)
            }
            is UserSavedTemplatePath -> {
                fileReadWrite.readContentFromFiles(path.path)

            }
            else -> throw IllegalStateException()
        }

        return TemplateUtils.parseTemplate(jsonString, path, json, abTemplateAvailability)
    }

    @WorkerThread
    fun deleteTemplateFiles(
        template: Template?,
        path: TemplatePath,
        externalResourceDao: ExternalResourceDao
    ) {
        if (path is UserSavedTemplatePath)
            fileSystem.delete(path.path.removeScheme().toPath(), mustExist = false)

        template?.getFilesToClean()?.forEach {
            val withoutScheme = it.removeScheme()
            val shouldDelete = externalResourceDao.onRemoveResource(withoutScheme)
            if (shouldDelete)
                fileSystem.delete(withoutScheme.toPath(), mustExist = false)
        }
    }

    private fun loadPredefinedTemplate(
        path: PredefinedTemplatePath,
        category: String,
        indexInCategory: Int
    ): Template {
        val jsonStr = fileReadWrite.readContentFromAssets(path.res)
        val template =
            TemplateUtils.parseTemplate(jsonStr, path, json, abTemplateAvailability)
        template.originalData = OriginalTemplateData(category, indexInCategory, path.path)
        return template
    }

    private fun loadMyStoriesTemplate(path: String): Template {
        return TemplateUtils.parseTemplate(
            fileReadWrite.readContentFromFiles(path),
            UserSavedTemplatePath(path), json, abTemplateAvailability
        )
    }

    fun myStoriesPaths(): List<String> {

        val list = fileSystem.list(myStoriesFolder())

        return list.sortedByDescending { fileSystem.metadata(it).lastModifiedAtMillis }.map {
            val str = it.toString()
            if (str.getExt() != "json") throw IllegalStateException("unsupported extension is found $str")
            str
        }
    }

    fun copyTemplate(
        template: Template,
        templatePath: UserSavedTemplatePath,
        templates: Collection<Template>
    ): Pair<Template, TemplatePath> {
        val newTemplate = loadMyStoriesTemplate(templatePath.path)

        val templateName = template.name ?: template.originalData!!.originalName

        val newName = generateNewTemplateName(templateName, templates)
        newTemplate.name = newName
        val newTemplatePath = saveTemplateToFile(newTemplate, existingPath = null)

        return newTemplate to newTemplatePath
    }

    /*
     return path to which template was saved
     */
    fun saveTemplateToFile(
        template: Template,
        existingPath: TemplatePath?,
        currentTime: Long = DateTime.nowUnixLong()
    ): UserSavedTemplatePath {

        val resultPath: String

        if (existingPath !is UserSavedTemplatePath || !fileSystem.exists(existingPath.path.toPath())) {
            val nm = template.originalData!!.originalName
            val newName = "$nm-${currentTime}.json"

            resultPath = myStoriesFolder().resolve(newName).toString()
        } else {
            resultPath = existingPath.path
        }

        val str = template.toJsonString(json)
        fileReadWrite.writeContentToFile(str, resultPath)

        return UserSavedTemplatePath(resultPath)
    }

    private fun generateNewTemplateName(
        templateName: String,
        templates: Collection<Template>
    ): String {
        var index = 0
        val containsRegex by lazy {
            Regex("$templateName #\\d+")
        }
        templates.forEach { template ->
            val name = template.name
            if (name?.contains(regex = containsRegex) == true) {
                val id = name.split('#').last().toInt()
                if (id > index) index = id
            }
        }
        index++
        return "$templateName #$index"
    }

    private fun findTemplateIndexAndCategory(
        categories: List<TemplateCategory>,
        templatePosition: Int
    ): Pair<Int, String> {

        var previousTemplatesSize = 0

        for (cat in categories) {

            //plus header
            previousTemplatesSize += cat.size

            if (previousTemplatesSize > templatePosition) {
                val indexInCategory = templatePosition - (previousTemplatesSize - cat.size)

                return Pair(indexInCategory, cat.id)
            }
        }

        return Pair(0, categories[0].id)
    }

    fun loadAllTemplates(helper: TemplatesAdapterHelper) =
        loadAllTemplates(
            templates = helper.templates,
            categories = helper.categories,
            templatesCache = helper.mapTemplates,
            loadMyStories = helper.myStories
        )


    private fun loadAllTemplates(
        templates: List<TemplatePath>,
        categories: List<TemplateCategory>?,
        templatesCache: Map<TemplatePath, Result<Template>>?,
        loadMyStories: Boolean
    ): Flow<Result<Template>> {

        return flow {
            templates.forEachIndexed { index, templatePath ->
                //delay for testing
                //delay(500L)
                val result: Result<Template> = templatesCache?.get(templatePath)
                    ?: if (loadMyStories) {
                        val path = (templatePath as UserSavedTemplatePath).path
                        try {
                            Result.success(loadMyStoriesTemplate(path))
                        } catch (e: Exception) {
                            e.printStackTrace()
                            Result.failure(e)
                        }

                    } else {
                        val (indexInCategory, category) = findTemplateIndexAndCategory(
                            categories!!,
                            index
                        )

                        try {
                            Result.success(
                                loadPredefinedTemplate(
                                    templatePath as PredefinedTemplatePath,
                                    category,
                                    indexInCategory
                                )
                            )
                        } catch (e: Exception) {
                            e.printStackTrace()
                            Result.failure(e)
                        }

                    }

                emit(result)
            }
        }
    }

    companion object {

        fun predefinedTemplatePaths(categories: List<TemplateCategory>): MutableList<TemplatePath> {
            val list = mutableListOf<TemplatePath>()

            for (cat in categories) {
                for (p in cat.templatePaths) {
                    list.add(PredefinedTemplatePath(p))
                }
            }

            return list
        }
    }
}