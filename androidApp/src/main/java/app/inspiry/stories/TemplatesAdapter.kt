package app.inspiry.stories

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.graphics.Rect
import android.graphics.Typeface
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.setPadding
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.inspiry.BuildConfig
import app.inspiry.R
import app.inspiry.activities.ToInstActivity
import app.inspiry.core.analytics.AnalyticsManager
import app.inspiry.core.analytics.putString
import app.inspiry.core.data.PredefinedTemplatePath
import app.inspiry.core.data.TemplatePath
import app.inspiry.core.data.UserSavedTemplatePath
import app.inspiry.core.data.templateCategory.TemplateCategory
import app.inspiry.core.database.ExternalResourceDao
import app.inspiry.core.log.KLogger
import app.inspiry.core.manager.InspRemoteConfig
import app.inspiry.core.media.BaseUnitsConverter
import app.inspiry.core.media.Template
import app.inspiry.core.media.TemplateAvailability
import app.inspiry.core.media.TemplateFormat
import app.inspiry.core.template.TemplateReadWrite
import app.inspiry.core.template.TemplatesAdapterHelper
import app.inspiry.databinding.ItemTemplateBinding
import app.inspiry.edit.EditActivity
import app.inspiry.helpers.K
import app.inspiry.utils.*
import app.inspiry.views.group.BaseGroupZView
import app.inspiry.views.template.InspTemplateView
import app.inspiry.views.template.InspTemplateViewCreator
import app.inspiry.views.template.TemplateMode
import app.inspiry.views.template.setTemplateRoundedCornersAndShadow
import com.afollestad.materialdialogs.MaterialDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.parameter.parametersOf


class TemplatesAdapter(
    val helper: TemplatesAdapterHelper,
    val activity: FragmentActivity,
    val recyclerView: RecyclerView,
    val fragment: Fragment,
) :
    RecyclerView.Adapter<TViewHolder>(), KoinComponent {
    val viewHolders = mutableListOf<TViewHolder.TemplateViewHolder>()

    val analyticsManager: AnalyticsManager by inject()
    val templateReadWrite: TemplateReadWrite by inject()
    val remoteConfig: InspRemoteConfig by inject()
    val json: Json by inject()
    val externalResourceDao: ExternalResourceDao by inject()
    val logger: KLogger by inject {
        parametersOf("templates-adapter")
    }
    val unitsConverter: BaseUnitsConverter by inject()

    private var selectedPosition = -1
        set(value) {
            val oldField = field
            field = value

            if (oldField != -1) (recyclerView.findViewHolderForLayoutPosition(oldField) as? TViewHolder.TemplateViewHolder)
                ?.innerGroupZView.also {

                    if (it == null) notifyItemChanged(oldField)
                    else it.foreground = null
                }
            if (field != -1) (recyclerView.findViewHolderForLayoutPosition(field) as? TViewHolder.TemplateViewHolder)
                ?.innerGroupZView.also {
                    if (it == null) notifyItemChanged(oldField)
                    else it.foreground =
                        activity.getDrawable(R.drawable.selected_template_foreground)
                }
        }

    init {
        helper.notifyWhenItemChanged (::notifyItemChanged)
        recyclerView.setHasFixedSize(!helper.displayNames())

        if (!helper.myStories) {
            recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {

                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)


                    for (holder in viewHolders) {
                        if (DEBUG_VISIBLE_PART)
                            percentItemVisible(holder)

                        val percent = percentItemVisible(holder)

                        if (holder.templateView.isPlaying.value) {

                            if (percent <= PERCENT_STOP_PLAY_THRESHOLD)
                                holder.templateView.stopPlaying()

                        } else {
                            if (percent > PERCENT_START_PLAY_THRESHOLD) {
                                holder.templateView.startPlaying(false)
                            }
                        }
                    }
                }
            })
        }

        initTemplatesLoading()
    }

    fun reloadTemplates() {
        helper.clear()
        notifyDataSetChanged()
        initTemplatesLoading()
    }

    fun setPremiumChanged(isPremium: Boolean) {
        helper.setPremiumChanged(isPremium = isPremium)
    }

    fun updateTemplates(templates: MutableList<TemplatePath>) {
        helper.templates = templates
    }

    fun updateCategories(categories: List<TemplateCategory>) {
        helper.categories = categories
    }

    fun setInstSubscribeChanged(subscribed: Boolean) {
        helper.setInstSubscribeChanged(subscribed = subscribed)
    }


    private fun initTemplatesLoading() {
        helper.loadAllTemplates(templatesReadWrite = templateReadWrite) { index, value ->
            onTemplateLoaded(value, index)
        }
    }

    private fun onTemplateLoaded(template: Result<Template>, index: Int) {

        if (helper.templatesCount() <= index) {
            logger.error { "TemplatesAdapter.onTemplateLoaded - templates.size <= index" }
            return
        }
        val originalPosition = helper.findOriginalPosition(index)
        K.d("template") {
            "onTemplateLoaded original ${originalPosition} index ${index}, otherPath ${
                helper.getPathByIndex(
                    index = index
                )
            }"
        }

        template.exceptionOrNull()?.printCrashlytics()

        notifyItemChanged(originalPosition)
    }

    private val rowRect = Rect()
    fun percentItemVisible(item: TViewHolder.TemplateViewHolder): Float {

        val lm = recyclerView.layoutManager as LinearLayoutManager
        val pos = item.bindingAdapterPosition
        val firstPos = lm.findFirstVisibleItemPosition()
        val lastPos = lm.findLastVisibleItemPosition()

        val res: Float =
            if ((firstPos > pos || lastPos < pos) && (firstPos != -1 && lastPos != -1)) 0f
            else if (!item.itemView.getLocalVisibleRect(rowRect)) {
                0f

            } else {
                rowRect.height() / item.itemView.height.toFloat()
            }

        if (DEBUG_VISIBLE_PART) {
            K.i("percentItemVisible") {
                "pos $pos, firstPos = ${firstPos}, lastPos = ${lastPos}, res $res"
            }
            if (Build.VERSION.SDK_INT >= 26)
                item.itemView.setBackgroundColor(Color.argb(res, 1f, 0f, 0f))
        }

        return res
    }

    override fun getItemViewType(position: Int): Int {
        val categories = helper.categories

        if (categories == null) {
            return ITEM_TYPE_TEMPLATE
        } else {

            var acc = 0
            for (cat in categories) {

                if (position == acc) {
                    return ITEM_TYPE_CATEGORY
                }

                //plus header
                acc += cat.size + 1

                if (position < acc) {
                    return ITEM_TYPE_TEMPLATE
                }
            }

            throw java.lang.IllegalStateException(
                "itemType is undefined for position ${position}, " +
                        "accumulated value of categories and headers ${acc}, getItemCount() ${itemCount}"
            )
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TViewHolder {

        return when (viewType) {
            ITEM_TYPE_TEMPLATE -> {
                val binding =
                    ItemTemplateBinding.inflate(LayoutInflater.from(parent.context), parent, false)

                val innerGroupZView = BaseGroupZView(
                    parent.context,
                    templateView = null,
                    unitsConverter = unitsConverter
                )
                innerGroupZView.id = R.id.templateView

                binding.rootConstraint.addView(innerGroupZView, 0,
                    ConstraintLayout.LayoutParams(0, 0).also {
                        it.topToTop = ConstraintLayout.LayoutParams.PARENT_ID
                        it.startToStart = ConstraintLayout.LayoutParams.PARENT_ID
                        it.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
                        it.dimensionRatio = "H, 9:16"
                    })

                val templateView: InspTemplateView =
                    InspTemplateViewCreator.createInspTemplateView(
                        innerGroupZView,
                        initialTemplateMode = if (helper.myStories) TemplateMode.PREVIEW else TemplateMode.LIST_DEMO
                    )
                templateView.shouldHaveBackground = true
                templateView.setBackgroundColor(Color.WHITE)

                binding.textTemplateName.updateLayoutParams<ConstraintLayout.LayoutParams> {
                    topToBottom = R.id.templateView
                }

                if (helper.myStories) {
                    binding.imageIndicator.setImageResource(R.drawable.icon_context_template)
                    binding.imageIndicator.setPadding(6.dpToPixels().toInt())
                } else if (!helper.displayNames()) {
                    binding.rootConstraint.removeView(binding.textTemplateName)
                    binding.root.changePaddingToView(padBottom = 16.dpToPixels().toInt())
                }

                innerGroupZView.setTemplateRoundedCornersAndShadow()
                TViewHolder.TemplateViewHolder(binding, templateView, innerGroupZView)
            }

            ITEM_TYPE_CATEGORY -> {
                val textView = TextView(parent.context)
                textView.setTextColor(0xff353535.toInt())
                textView.textSize = 19f
                textView.typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
                textView.setPadding(13.dpToPxInt(), 10.dpToPxInt(), 0, 3.dpToPxInt())

                TViewHolder.HeaderViewHolder(textView)
            }

            else -> throw java.lang.IllegalStateException()
        }
    }

    fun closeContext(): Boolean {

        if (selectedPosition == -1) return false
        else {
            (activity.supportFragmentManager.findFragmentByTag("EditTemplateContextDialog") as? DialogFragment?)?.dismissAllowingStateLoss()
            selectedPosition = -1
            return true
        }
    }

    override fun getItemCount(): Int = helper.templatesCount() + helper.categoriesCount()

    private fun copyTemplate(template: Template, templatePath: UserSavedTemplatePath) {

        fragment.lifecycleScope.launch {

            val (newTemplate, newTemplatePath) = withContext(Dispatchers.IO) {
                lateinit var result: Pair<Template, TemplatePath>
                helper.copyTemplate(
                    templateReadWrite = templateReadWrite,
                    template = template,
                    templatePath = templatePath
                ) { t, p ->
                    result = Pair(t, p)
                }
                result
            }

            externalResourceDao.onTemplateOrMediaCopy(newTemplate.getFilesToClean())

            helper.insertCopiedTemplate(template = newTemplate, templatePath = newTemplatePath)
            notifyItemInserted(0)
            closeContext()
            rebindAllViews()
            recyclerView.scrollToPosition(0)
        }
    }

    private fun rebindAllViews() {

        (0 until recyclerView.childCount).forEach {
            val holder =
                recyclerView.getChildViewHolder(recyclerView.getChildAt(it)) as? TViewHolder.TemplateViewHolder?
            val position = holder?.bindingAdapterPosition ?: -1
            if (position != -1) {
                val templateIndex = helper.findTemplateIndex(position)

                val templatePath = helper.getPathByIndex(templateIndex)
                val template = helper.getTemplateFromCache(templatePath = templatePath)
                if (template != null) {
                    onBindViewHolderDontPlay(
                        holder!!,
                        position, template, templatePath
                    )
                }
            }
        }
    }

    private fun deleteTemplate(
        position: Int,
        template: Result<Template>,
        templatePath: UserSavedTemplatePath
    ) {
        MaterialDialog(activity).show {
            title(text = context.getString(app.inspiry.projectutils.R.string.context_delete_title))
            message(text = context.getString(app.inspiry.projectutils.R.string.context_delete_message))
            positiveButton(
                text = context.getString(app.inspiry.projectutils.R.string.dialog_alert_confirm),
                click = {
                    fragment.lifecycleScope.launch {
                        withContext(Dispatchers.IO) {
                            templateReadWrite.deleteTemplateFiles(
                                template.getOrNull(),
                                templatePath,
                                externalResourceDao
                            )
                        }

                        helper.removeTemplate(path = templatePath) {
                            notifyItemRemoved(position)
                            rebindAllViews()
                        }
                    }
                })
            negativeButton(
                text = context.getString(app.inspiry.projectutils.R.string.cancel),
                click = {})
        }
        closeContext()
    }

    private fun renameTemplate(
        position: Int,
        template: Template,
        templatePath: UserSavedTemplatePath
    ) {
        activity.nameYourStoryDialog(prefill = template.name) {
            template.name = it.toString()
            //just resave it
            fragment.lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    templateReadWrite.saveTemplateToFile(
                        template,
                        templatePath,
                        currentTime = 0L
                    )
                }
                notifyItemChanged(position)
            }
        }
        closeContext()
    }

    private fun displayName(
        holder: TViewHolder.TemplateViewHolder,
        template: Result<Template>,
        templatePath: TemplatePath
    ) {
        if (helper.displayNames() || template.isFailure) {

            val name = helper.getTemplateName(path = templatePath)

            if (name.isNullOrEmpty()) {
                holder.binding.textTemplateName.visibility = View.GONE
                holder.binding.root.changePaddingToView(padBottom = 16.dpToPixels().toInt())
            } else {

                holder.binding.textTemplateName.visibility = View.VISIBLE
                holder.binding.root.changePaddingToView(padBottom = 14.dpToPixels().toInt())
                holder.binding.textTemplateName.text = name
            }
        }
    }

    private fun setOnClickTemplate(
        holder: TViewHolder.TemplateViewHolder,
        templateResult: Result<Template>,
        templatePath: TemplatePath
    ) {
        holder.innerGroupZView.setOnClickListener {

            val template = templateResult.getOrNull()
            if (template != null) {
                if (helper.needToShowInstagram(template = template, remoteConfig = remoteConfig)
                ) {

                    activity.startActivity(
                        Intent(it.context, ToInstActivity::class.java)
                            .putOriginalTemplateData(template.originalData)
                            .putExtra(Constants.EXTRA_SOURCE, ToInstActivity.SOURCE_TEMPLATES)
                    )

                } else {

                    activity.startActivity(
                        Intent(it.context, EditActivity::class.java)
                            .putTemplatePath(templatePath)
                            .putOriginalTemplateData(template.originalData)
                    )
                }

                if (!helper.myStories) {
                    analyticsManager.templateClick(template, holder.templateView.isStatic())
                }
            }
        }

    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun onBindViewHolderDontPlay(
        holder: TViewHolder.TemplateViewHolder,
        position: Int,
        template: Result<Template>,
        templatePath: TemplatePath
    ) {

        holder.innerGroupZView.foreground =
            if (selectedPosition == position) activity.getDrawable(
                R.drawable.selected_template_foreground
            ) else null

        if (!helper.myStories && BuildConfig.DEBUG) {
            holder.innerGroupZView.setOnLongClickListener {

                holder.templateView.printDebugInfo()
                true
            }
        }

        displayName(holder, template, templatePath)

        setOnClickTemplate(holder, template, templatePath)

        if (helper.myStories) {
            holder.binding.imageIndicator.setOnClickListener {
                selectedPosition = position

                val dialog = EditTemplateContextDialog()
                dialog.showOnlyDelete = !template.isSuccess
                dialog.editTemplateListener = {
                    when (it) {
                        EditTemplateContextDialog.ACTION_COPY -> {
                            template.getOrNull()
                                ?.let { copyTemplate(it, templatePath as UserSavedTemplatePath) }
                        }
                        EditTemplateContextDialog.ACTION_DELETE -> deleteTemplate(
                            position,
                            template,
                            templatePath as UserSavedTemplatePath
                        )
                        EditTemplateContextDialog.ACTION_RENAME -> {
                            template.getOrNull()?.let {
                                renameTemplate(
                                    position,
                                    it,
                                    templatePath as UserSavedTemplatePath
                                )
                            }
                        }
                    }

                    analyticsManager.sendEvent("context_item_click", createParams = {
                        putString(
                            "name", when (it) {
                                EditTemplateContextDialog.ACTION_COPY -> "copy"
                                EditTemplateContextDialog.ACTION_DELETE -> "delete"
                                EditTemplateContextDialog.ACTION_RENAME -> "rename"
                                else -> throw IllegalStateException()
                            }
                        )
                    })

                }
                dialog.onDismissListener = {
                    closeContext()
                }
                dialog.show(activity.supportFragmentManager, "EditTemplateContextDialog")
            }

        } else {

            val hasPremiumAccess =
                helper.hasPremium || helper.isInFreeForWeek(templatePath as PredefinedTemplatePath)

            when {
                hasPremiumAccess || !template.isSuccess -> {
                    holder.binding.imageIndicator.visibility = View.GONE
                    holder.binding.imageIndicator.setImageResource(0)
                }
                template.getOrThrow().availability == TemplateAvailability.PREMIUM -> {
                    holder.binding.imageIndicator.visibility = View.VISIBLE
                    holder.binding.imageIndicator.setImageResource(R.drawable.ic_premium_template)
                }
                template.getOrThrow().availability == TemplateAvailability.INSTAGRAM_SUBSCRIBED &&
                        !helper.instagramSubscribed && remoteConfig.getBoolean("show_instagram_subscribed_option") -> {
                    holder.binding.imageIndicator.visibility = View.VISIBLE
                    holder.binding.imageIndicator.setImageResource(R.drawable.ic_instagram_template)

                }
                else -> {
                    holder.binding.imageIndicator.visibility = View.GONE
                    holder.binding.imageIndicator.setImageResource(0)
                }
            }
        }
        holder.innerGroupZView.setRatioBasedOnFormat(
            template.getOrNull()?.format ?: TemplateFormat.story, true
        )
        holder.templateView.restoreRenderingInList()
    }


    private fun onTemplateLoaded(
        holder: TViewHolder.TemplateViewHolder,
        position: Int,
        template: Result<Template>,
        templatePath: TemplatePath
    ) {

        if (template.isSuccess) {
            holder.templateView.loadTemplate(template.getOrThrow())
        } else {
            val exception = template.exceptionOrNull()
            holder.templateView.showErrorView(exception)
        }

        if (helper.myStories) {
            fragment.lifecycleScope.launch {
                holder.templateView.isInitialized.collect {
                    if (it) {
                        holder.templateView.setFrameForEdit()
                    }
                }
            }
        } else {
            holder.innerGroupZView.post {
                if (percentItemVisible(holder) >= PERCENT_START_PLAY_THRESHOLD) {
                    holder.templateView.startPlaying()
                }
            }
        }

        onBindViewHolderDontPlay(holder, position, template, templatePath)
    }


    override fun onBindViewHolder(holder: TViewHolder, position: Int) {

        if (holder is TViewHolder.TemplateViewHolder) {

            val templateIndex = helper.findTemplateIndex(position)
            val templatePath = helper.getPathByIndex(templateIndex)

            val template = helper.getTemplateFromCache(templatePath = templatePath)

            if (template != null) {
                onTemplateLoaded(holder, position, template, templatePath)

            } else {
                holder.templateView.unloadTemplate()
                holder.innerGroupZView.setOnClickListener(null)
                if (helper.myStories) {
                    holder.binding.imageIndicator.setOnClickListener { }
                } else {
                    holder.binding.imageIndicator.visibility = View.GONE
                }
                holder.binding.textTemplateName.visibility = View.GONE
            }

            viewHolders.add(holder)

        } else {
            (holder.itemView as TextView).text =
                holder.itemView.context.getString(helper.getDisplayNameForCategory(position).resourceId)
        }
    }

    override fun onViewRecycled(holder: TViewHolder) {

        if (holder is TViewHolder.TemplateViewHolder) {
            holder.templateView.unbind()
            viewHolders.remove(holder)
        }
    }

    companion object {
        const val PERCENT_START_PLAY_THRESHOLD = 0.5
        const val PERCENT_STOP_PLAY_THRESHOLD = 0.2
        const val DEBUG_ONLY_PRELOAD = false
        const val DEBUG_VISIBLE_PART = false

        const val ITEM_TYPE_TEMPLATE = 0
        const val ITEM_TYPE_CATEGORY = 1
    }
}

sealed class TViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    class TemplateViewHolder(
        val binding: ItemTemplateBinding, val templateView: InspTemplateView,
        val innerGroupZView: BaseGroupZView
    ) : TViewHolder(binding.root)

    class HeaderViewHolder(itemView: TextView) : TViewHolder(itemView)
}