package app.inspiry.textanim

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.os.Bundle
import android.os.PersistableBundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.core.view.children
import androidx.core.view.setMargins
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.inspiry.BuildConfig
import app.inspiry.R
import app.inspiry.core.analytics.AnalyticsManager
import app.inspiry.core.manager.LicenseManager
import app.inspiry.core.log.LoggerGetter
import app.inspiry.core.media.BaseUnitsConverter
import app.inspiry.databinding.ActivityTextAnimationsBinding
import app.inspiry.font.helpers.TextCaseHelper
import app.inspiry.core.ActivityRedirector
import app.inspiry.core.log.ErrorHandler
import app.inspiry.textanim.ui.TextAnimCategory
import app.inspiry.utils.dpToPxInt
import app.inspiry.views.group.BaseGroupZView
import app.inspiry.views.infoview.InfoViewColorsLight
import app.inspiry.views.template.InspTemplateView
import app.inspiry.views.template.InspTemplateViewCreator
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.koin.android.ext.android.get
import org.koin.android.ext.android.inject


class TextAnimationsActivity : AppCompatActivity() {

    private lateinit var adapterAnimations: AnimationsAdapter
    private lateinit var binding: ActivityTextAnimationsBinding
    private val licenseManager: LicenseManager by inject()
    private lateinit var viewModel: TextAnimViewModel
    private val activityRedirector: ActivityRedirector by inject()
    private val textCaseHelper: TextCaseHelper by inject()
    private val unitsConverter: BaseUnitsConverter by inject()

    private lateinit var templatePreviewAnimation: InspTemplateView
    private lateinit var templatePreviewAnimationAndroid: BaseGroupZView

    private fun setResultAndFinish() {

        viewModel.onClickSaveTemplate()
        setResult(
            Activity.RESULT_OK,
            Intent()
                .putExtra("animation_path", viewModel.selectedAnimationPath?.path)
        )
        finish()
    }

    private fun initPreviewTemplateView() {
        templatePreviewAnimationAndroid =
            BaseGroupZView(this, templateView = null, unitsConverter = unitsConverter)
        templatePreviewAnimation = InspTemplateViewCreator.createInspTemplateView(
            templatePreviewAnimationAndroid,
            colors = InfoViewColorsLight()
        )

        templatePreviewAnimationAndroid.id = R.id.templateView
        binding.previewTextContainer.addView(
            templatePreviewAnimationAndroid, FrameLayout.LayoutParams(
                MATCH_PARENT, WRAP_CONTENT
            )
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTextAnimationsBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)
        initPreviewTemplateView()

        val currentText = intent.getStringExtra("preview_text")
        val initialAnimationPath = savedInstanceState?.getString("selected_animation")
        val initialTabNum = savedInstanceState?.getInt("current_tab_num") ?: 0
        viewModel =
            ViewModelProvider(
                this,
                TextAnimViewModelFactory(
                    currentText, textCaseHelper,
                    get(), get(), get(), get(), get(), get(), initialAnimationPath, initialTabNum
                )
            )[TextAnimViewModel::class.java]

        binding.buttonBack.setOnClickListener {
            finish()
        }

        if (BuildConfig.DEBUG) {
            binding.buttonSave.setOnLongClickListener {
                if (viewModel.selectedAnimationPath != null) {
                    setResultAndFinish()
                }
                true
            }
        }

        if (BuildConfig.DEBUG) {
            binding.buttonSave.setOnLongClickListener {
                setResultAndFinish()
                true
            }

        }

        binding.buttonSave.setOnClickListener {
            if (viewModel.selectedAnimationPath != null) {

                if (viewModel.shouldOpenSubscribeOnClickSave(
                        licenseManager.hasPremiumState.value,
                        templatePreviewAnimation
                    )
                ) {
                    activityRedirector.openSubscribeActivity(this, "text_animation")

                } else {
                    setResultAndFinish()
                }
            }
        }


        val backDrawable = getDrawable(R.drawable.ic_arrow_back_edit)?.mutate()
        backDrawable?.colorFilter = PorterDuffColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP)
        binding.buttonBack.setCompoundDrawablesWithIntrinsicBounds(backDrawable, null, null, null)

        adapterAnimations = AnimationsAdapter(hasPremium = licenseManager.hasPremiumState.value)
        binding.recyclerAnimations.setHasFixedSize(true)
        binding.recyclerAnimations.layoutManager = GridLayoutManager(this, 3)
        binding.recyclerAnimations.adapter = adapterAnimations

        createTabs()

        viewModel.initPreviewTemplateView(templatePreviewAnimation)
        templatePreviewAnimationAndroid.setPadding(
            16.dpToPxInt(),
            8.dpToPxInt(),
            16.dpToPxInt(),
            8.dpToPxInt()
        )

        lifecycleScope.launch {
            licenseManager.hasPremiumState.collectLatest {
                adapterAnimations.changeHasPremium(it)
            }
        }
        lifecycleScope.launch {
            viewModel.currentAnimations.collect {
                if (it != null) {
                    adapterAnimations.changeAnimations(it)
                }
            }
        }
        lifecycleScope.launch {
            viewModel.currentPreviewAnimation.collect {
                if (it != null) {
                    viewModel.previewAnimation(it.media, templatePreviewAnimation)
                }
            }
        }
    }


    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
        super.onSaveInstanceState(outState, outPersistentState)
        outState.putInt("current_tab_num", viewModel.currentTabNum.value)
        outState.putString("selected_animation", viewModel.selectedAnimationPath?.originalPath)
    }

    private fun createTabs() {
        binding.categoryTabs.removeAllViews()
        binding.categoryTabs.addView(getCategoryTabs(viewModel.getTextAnimationTabs()))
    }

    private fun getCategoryTabs(tabs: List<String>): View {
        return ComposeView(this).apply {
            setContent {
                MaterialTheme(
                    colors = MaterialTheme.colors.copy(isLight = true)
                ) {
                    val currentCategory by viewModel.currentTabNum.collectAsState()
                    TextAnimCategory(items = tabs, currentCategory) { categoryIndex ->
                        viewModel.showAnimationsFromTab(categoryIndex, true)
                    }
                }
            }
        }
    }

    inner class AnimationsAdapter(
        var datas: List<MediaWithRes> = emptyList(),
        private var hasPremium: Boolean
    ) :
        RecyclerView.Adapter<TemplateViewHolder>() {

        fun changeAnimations(datas: List<MediaWithRes>) {
            this.datas = datas
            notifyDataSetChanged()
        }

        fun changeHasPremium(isPremium: Boolean) {
            if (this.hasPremium != isPremium) {
                hasPremium = isPremium
                notifyDataSetChanged()
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TemplateViewHolder {

            val frameLayout = FrameLayout(parent.context)
            frameLayout.layoutParams = ViewGroup.MarginLayoutParams(MATCH_PARENT, 86.dpToPxInt())
                .apply { setMargins(5.dpToPxInt(), 5.dpToPxInt(), 5.dpToPxInt(), 5.dpToPxInt()) }

            val templateAndroidView = BaseGroupZView(
                parent.context,
                templateView = null,
                unitsConverter = unitsConverter
            )
            val templateView = InspTemplateViewCreator.createInspTemplateView(
                templateAndroidView,
                displayInfoText = false
            )
            viewModel.initPreviewTemplateView(templateView)
            templateAndroidView.setBackgroundResource(R.drawable.grid_text_animations)
            templateAndroidView.isDuplicateParentStateEnabled = true

            frameLayout.addView(templateAndroidView, MATCH_PARENT, MATCH_PARENT)

            val textPro = TextView(parent.context)
            textPro.setBackgroundResource(R.drawable.text_pro_background)
            textPro.text = "PRO"
            textPro.textSize = 9f
            textPro.gravity = Gravity.CENTER
            textPro.translationZ = 100f
            textPro.setTextColor(0xffbdbdbd.toInt())

            val textLp = FrameLayout.LayoutParams(26.dpToPxInt(), 14.dpToPxInt(), Gravity.END)
            textLp.setMargins(6.dpToPxInt())

            frameLayout.addView(textPro, textLp)

            return TemplateViewHolder(frameLayout, textPro, templateView, templateAndroidView)
        }

        override fun onViewRecycled(holder: TemplateViewHolder) {
            super.onViewRecycled(holder)
            holder.templateView.unbind(false)
        }

        override fun getItemCount() = datas.size

        override fun onBindViewHolder(holder: TemplateViewHolder, position: Int) {

            val data = datas[position]
            val media = data.media
            val path = data.res
            holder.root.isActivated = path == viewModel.selectedAnimationPath

            viewModel.previewAnimationInList(media, position, holder.templateView)

            if (media.forPremium && !hasPremium) {
                holder.textPro.visibility = View.VISIBLE

            } else holder.textPro.visibility = View.GONE

            holder.templateAndroidView.setOnClickListener {

                binding.recyclerAnimations.children.forEach { it.isActivated = false }
                holder.root.isActivated = true

                viewModel.onClickTemplateInList(data)
            }
        }
    }
}

class TextAnimViewModelFactory(
    private val currentText: String?,
    private val textCaseHelper: TextCaseHelper,
    private val json: Json,
    private val unitsConverter: BaseUnitsConverter,
    private val provider: TextAnimProvider,
    private val analyticsManager: AnalyticsManager,
    private val errorHandler: ErrorHandler,
    private val loggerGetter: LoggerGetter,
    private val initialAnimationPath: String?,
    private val initialTabNum: Int
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return TextAnimViewModel(
            currentText,
            textCaseHelper,
            json,
            unitsConverter,
            provider,
            analyticsManager,
            errorHandler,
            loggerGetter,
            initialTabNum,
            initialAnimationPath
        ) as T
    }
}


class TemplateViewHolder(
    val root: ViewGroup,
    val textPro: TextView,
    val templateView: InspTemplateView,
    val templateAndroidView: ViewGroup
) : RecyclerView.ViewHolder(root)
