package app.inspiry.core.template

import app.inspiry.core.data.PredefinedTemplatePath
import app.inspiry.core.data.TemplatePath
import app.inspiry.core.data.UserSavedTemplatePath
import app.inspiry.core.data.templateCategory.TemplateCategory
import app.inspiry.core.data.templateCategory.TemplateCategoryProvider
import app.inspiry.core.log.GlobalLogger
import app.inspiry.core.manager.DebugManager
import app.inspiry.core.manager.InspRemoteConfig
import app.inspiry.core.media.Template
import app.inspiry.core.media.TemplateAvailability
import app.inspiry.core.util.getFileName
import app.inspiry.core.util.ioDispatcherCommon
import app.inspiry.core.util.removeExt
import dev.icerock.moko.resources.StringResource
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectIndexed
import kotlinx.coroutines.flow.flowOn
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.cancellation.CancellationException

class TemplatesAdapterHelper(
    coroutineContext: CoroutineContext,
    var templates: MutableList<TemplatePath>,
    var myStories: Boolean,
    var hasPremium: Boolean,
    var instagramSubscribed: Boolean,
    var categories: List<TemplateCategory>? = null
) : CoroutineScope {

    /**
     * swift constructor with EmptyCoroutineContext
     */
    constructor(
        templates: MutableList<TemplatePath>,
        myStories: Boolean,
        hasPremium: Boolean,
        instagramSubscribed: Boolean,
        categories: List<TemplateCategory>? = null
    ) : this(
        coroutineContext = EmptyCoroutineContext,
        templates = templates,
        myStories = myStories,
        hasPremium = hasPremium,
        instagramSubscribed = instagramSubscribed,
        categories = categories
    )

    var onItemChanged: ((Int) -> Unit)? = null

    val job = SupervisorJob()

    override val coroutineContext: CoroutineContext = job + coroutineContext

    val mapTemplates = mutableMapOf<TemplatePath, Result<Template>>()

    fun notifyWhenItemChanged(action: (Int) -> Unit) {
        onItemChanged = action
    }

    fun displayNames() = myStories || DebugManager.isDebug

    fun clear() {
        stopLoading()
        mapTemplates.clear()
    }

    fun stopLoading() {
        job.cancelChildren()
    }

    fun setPremiumChanged(isPremium: Boolean) {
        if (hasPremium != isPremium) {
            hasPremium = isPremium

            for ((index, it) in mapTemplates.values.withIndex()) {
                if (it.isSuccess && it.getOrThrow().availability == TemplateAvailability.PREMIUM) {
                    onItemChanged?.invoke(findOriginalPosition(index))
                }
            }
        }
    }

    fun setLoadableTemplates(templatePaths: List<TemplatePath>) {
        this.templates = templatePaths.toMutableList()
    }

    fun getPathByIndex(index: Int) = templates[index]

    fun getTemplateFromCache(templatePath: TemplatePath) = mapTemplates[templatePath]

    fun getTemplateFromCacheOrNull(templatePath: TemplatePath) =
        mapTemplates[templatePath]?.getOrNull()

    fun updateTemplatesCache(template: Template, templatePath: TemplatePath) {
        mapTemplates[templatePath] = Result.success(template)
    }

    fun updateTemplatesCache(index: Int, templateResult: Result<Template>) {
        mapTemplates[templates[index]] = templateResult
    }

    fun getTemplates(): Collection<Template> {
        return mapTemplates.values.mapNotNull { it.getOrNull() }
    }
    fun getTemplateName(path: TemplatePath): String? {

        val template = getTemplateFromCache(templatePath = path)
            ?: return path.path.getFileName().removeExt()

        val userDefinedName = template.getOrNull()?.name

        return if ((DebugManager.isDebug && !myStories) || template.isFailure) userDefinedName
            ?: path.path.getFileName().removeExt() else userDefinedName
    }
    fun insertCopiedTemplate(template: Template, templatePath: TemplatePath) {
        templates.add(0, templatePath)
        mapTemplates[templatePath] = Result.success(template)
    }

    fun needToShowInstagram(template: Template, remoteConfig: InspRemoteConfig): Boolean {
        return !myStories && template.availability == TemplateAvailability.INSTAGRAM_SUBSCRIBED &&
                !instagramSubscribed &&
                !hasPremium && remoteConfig.getBoolean("show_instagram_subscribed_option")
    }

    fun setInstSubscribeChanged(subscribed: Boolean) {
        if (instagramSubscribed != subscribed) {
            instagramSubscribed = subscribed

            for ((index, it) in mapTemplates.values.withIndex()) {
                if (it.isSuccess && it.getOrThrow().availability == TemplateAvailability.INSTAGRAM_SUBSCRIBED) {
                    onItemChanged?.invoke(findOriginalPosition(index))
                }
            }
        }
    }

    fun removeTemplate(path: TemplatePath, onRemoved: () -> Unit) {
        if (templates.contains(element = path)) {
            templates.remove(element = path)
            onRemoved()
        }
    }

    fun copyTemplate(
        templateReadWrite: TemplateReadWrite,
        template: Template,
        templatePath: TemplatePath,
    onCopied: (Template, TemplatePath) -> Unit) {
        if (templatePath is UserSavedTemplatePath) {
            val (t, p) = templateReadWrite.copyTemplate(
                template,
                templatePath,
                getTemplates()
            )
            onCopied.invoke(t, p)
        }
    }

    fun templatesCount() = templates.size

    fun categoriesCount() = categories?.size ?: 0

    private fun findCategoryIndex(position: Int): Int {
        if (position == 0) return 0

        var acc = 0

        for ((index, cat) in categories!!.withIndex()) {

            //plus header
            acc += cat.size + 1

            if (acc > position) {
                return index
            }
        }

        return -1
    }
    //todo need shared logic
    fun loadTemplatesApple(
        templatesReadWrite: TemplateReadWrite,
        onLoad: (Int, Template, TemplatePath) -> Unit
    ) {
        val helper = this
        launch {
            templatesReadWrite.loadAllTemplates(helper)
                .flowOn(ioDispatcherCommon)
                .cancellable()
                .catch {
                    GlobalLogger.debug("TemplateReadWrite") {
                        it.message ?: "loadAllTemplates unknown error"
                    }
                }
                .collectIndexed { index, value ->
                    if (value.isSuccess && index < templates.size) {
                        helper.updateTemplatesCache(index = index, templateResult = value)
                        onLoad(index, value.getOrNull()!!, getPathByIndex(index) )
                    }
                }
        }
    }
    fun loadAllTemplates(
        templatesReadWrite: TemplateReadWrite,
        onLoad: (Int, Result<Template>) -> Unit
    ) {
        val helper = this
        launch {
            templatesReadWrite.loadAllTemplates(helper)
                .flowOn(Dispatchers.Default)
                .cancellable()
                .catch {
                    GlobalLogger.error("TemplateReadWrite", t = it)
                }
                .collectIndexed { index, value ->
                    helper.updateTemplatesCache(index = index, templateResult = value)
                    onLoad(index, value)
                }
        }
    }

    fun getPathListForCategory(categoryIndex: Int): List<TemplatePath> {
        if (categories == null) return templates
        val start = getTemplateIndex(0, categoryIndex)
        val size = categories!![categoryIndex].size
        if (start >= templates.size || start + size >= templates.size) return emptyList()
        return templates.subList(start, start + size)
    }

    fun isInFreeForWeek(path: PredefinedTemplatePath) =
        categories?.find { it.id == TemplateCategoryProvider.CATEGORY_ID_FREE_FOR_WEEK }
            ?.templatePaths?.contains(path.res) ?: false

    fun getDisplayNameForCategory(categoryIndex: Int): StringResource =
        categories!![findCategoryIndex(categoryIndex)].displayName

    fun findOriginalPosition(templatePosition: Int): Int {
        val categories = categories ?: return templatePosition

        var acc = 0

        for ((index, cat) in categories.withIndex()) {
            //plus header
            acc += cat.size + 1

            val templatePosAcc = templatePosition + index + 1

            if (templatePosAcc < acc) {
                return templatePosAcc
            }
        }

        return -1
    }

    fun getTemplateIndex(position: Int, categoryIndex: Int): Int {
        val categories = categories ?: return position
        var size = 0
        for ((index, cat) in categories.withIndex()) {
            if (index == categoryIndex) {
                return position + size
            }
            size += cat.size
        }
        return -1
    }

    fun findTemplateIndex(position: Int): Int {
        val categories = categories ?: return position

        var acc = 0

        for ((index, cat) in categories.withIndex()) {

            if (position == acc) {
                return -1
            }

            //plus header
            acc += cat.size + 1

            if (position < acc) {
                return position - (index + 1)
            }
        }

        return -1
    }
}
