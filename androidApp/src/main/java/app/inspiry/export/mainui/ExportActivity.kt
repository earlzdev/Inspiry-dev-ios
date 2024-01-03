package app.inspiry.export.mainui

import android.animation.ValueAnimator
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.animation.Animation
import android.widget.ImageView
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.doOnLayout
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import app.inspiry.R
import app.inspiry.core.data.InspResponseData
import app.inspiry.core.log.ErrorHandler
import app.inspiry.core.manager.InspRemoteConfig
import app.inspiry.core.media.BaseUnitsConverter
import app.inspiry.core.media.Template
import app.inspiry.core.media.TemplateFormat
import app.inspiry.core.template.TemplateReadWrite
import app.inspiry.core.template.TemplateViewModel
import app.inspiry.core.util.collectInActivity
import app.inspiry.databinding.ActivitySavingBinding
import app.inspiry.dialog.rating.RatingRequest
import app.inspiry.export.ExportCommonViewModel
import app.inspiry.export.ExportState
import app.inspiry.export.dialog.ExportDialogViewModel
import app.inspiry.export.dialog.ExportDialogViewModelFactory
import app.inspiry.export.viewmodel.*
import app.inspiry.helpers.K
import app.inspiry.helpers.TemplateViewModelFactory
import app.inspiry.utils.*
import app.inspiry.views.group.BaseGroupZView
import app.inspiry.views.template.*
import coil.ImageLoader
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import org.koin.android.ext.android.get
import org.koin.android.ext.android.inject


class ExportActivity : AppCompatActivity() {

    lateinit var binding: ActivitySavingBinding
    val unitsConverter: BaseUnitsConverter by inject()
    private val templateSaver: TemplateReadWrite by inject()
    private val imageLoader: ImageLoader by inject()
    private val errorHandler: ErrorHandler by inject()
    private val remoteConfig: InspRemoteConfig by inject()

    private lateinit var templateView: InspTemplateView
    private lateinit var innerGroupZView: BaseGroupZView

    private lateinit var recordViewModel: RecordViewModel
    private lateinit var exportViewModel: ExportViewModel
    private lateinit var dialogViewModel: ExportDialogViewModel
    private lateinit var templateViewModel: TemplateViewModel

    private val commonViewModel: ExportCommonViewModel by inject()

    private var imageTemplate: ImageView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.EditThemeActivity)

        window.setBackgroundDrawable(ColorDrawable(getColorCompat(R.color.template_activity_bg)))
        binding = ActivitySavingBinding.inflate(LayoutInflater.from(this))

        insertTemplateView()

        binding.textureTemplate.isOpaque = false
        binding.textureTemplate.translationX = resources.displayMetrics.widthPixels + 500f
        innerGroupZView.setTemplateRoundedCornersAndShadow()

        dialogViewModel = ViewModelProvider(
            this, ExportDialogViewModelFactory(
                get(),
                applicationContext.packageManager
            )
        )[ExportDialogViewModel::class.java]


        templateViewModel = ViewModelProvider(
            this,
            TemplateViewModelFactory(templateSaver)
        )[TemplateViewModel::class.java]

        recordViewModel = RecordViewModelImpl(savedInstanceState,
            if (intent.hasExtra(KEY_IMAGE_ELSE_VIDEO)) intent.getBooleanExtra(
                KEY_IMAGE_ELSE_VIDEO, false
            ) else null,
            templateView,
            innerGroupZView,
            this,
            binding.textureTemplate,
            get(), get(), get(), get(),
            templateViewModel,
            intent.getOriginalTemplateData(),
            displayImageBitmap = {
                imageTemplate?.setImageBitmap(it)
            })

        exportViewModel = ExportViewModelImpl(this, recordViewModel)

        // set temporary margins in order to display it correctly.

        innerGroupZView.setMarginsByFormat(TemplateFormat.story)

        setContentCompose()

        loadTemplate()

    }

    private fun initialUiState(template: Template) {

        binding.textureTemplate.setRatioBasedOnFormat(
            template.format,
            false,
            requestLayoutOnChange = false
        )
        binding.textureTemplate.setMarginsByFormat(template.format)
        binding.textureTemplate.requestLayout()

        innerGroupZView.elevation = 2.dpToPixels()
        innerGroupZView.setRatioBasedOnFormat(
            template.format,
            false,
            requestLayoutOnChange = false
        )
        innerGroupZView.setMarginsByFormat(template.format)
        innerGroupZView.requestLayout()

        innerGroupZView.doOnLayout {
            val valueAnimator = ValueAnimator()
            valueAnimator.setFloatValues(0f, 1f)
            valueAnimator.duration = 200L
            valueAnimator.addUpdateListener {
                innerGroupZView.alpha = it.animatedFraction
            }
            valueAnimator.start()
        }
    }

    private fun collectState(templateViewModel: TemplateViewModel) {

        lifecycleScope.launch {

            templateViewModel.template.combine(recordViewModel.state) { templateWrapper, state ->
                if (templateWrapper is InspResponseData) {

                    templateWrapper.data to state
                } else {
                    null
                }
            }.collect {

                K.i("export") {
                    "collectState ${it?.second}"
                }
                val (template, state) = it ?: return@collect

                K.i("export") {
                    "collectState ${state}"
                }

                when (state) {
                    is ExportState.UserPicked -> {

                        K.i("export") {
                            "collectState userPicked ${template.format.getRenderingSize()}"
                        }
                        hideTemplate(template)
                        if (state.imageElseVideo) {
                            showImage(template)
                        } else {
                            showTexture()
                        }

                    }
                    is ExportState.RenderingInProcess -> {

                    }
                    is ExportState.Rendered -> {

                        RatingRequest(this@ExportActivity, remoteConfig)
                            .showRatingDialog(true)

                        if (!state.imageElseVideo)
                            onFinishVideoRender(template)
                    }
                    else -> {}
                }
            }
        }
    }

    private fun setContentCompose() {
        setContent {
            ExportMainUI(
                exportViewModel,
                recordViewModel,
                dialogViewModel,
                commonViewModel,
                imageLoader,
                binding.root
            )
        }
    }

    private fun loadTemplate() {
        templateViewModel.loadTemplate(intent.getTemplatePath(), skipIfLoaded = true)

        collectState(templateViewModel)

        lifecycleScope.launch {
            templateViewModel.template.collectInActivity(
                errorHandler,
                ::finish,
                ::initialUiState
            )
        }
    }

    private fun insertTemplateView() {
        innerGroupZView = BaseGroupZView(this, templateView = null, unitsConverter = unitsConverter)
        templateView = InspTemplateViewCreator.createInspTemplateView(innerGroupZView)

        innerGroupZView.id = R.id.templateView

        binding.root.addView(innerGroupZView, 0, ConstraintLayout.LayoutParams(0, 0).also {
            it.startToStart = ConstraintLayout.LayoutParams.PARENT_ID
            it.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
            it.topToTop = ConstraintLayout.LayoutParams.PARENT_ID
            it.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
            it.dimensionRatio = "W, 9:16"
        })

        innerGroupZView.alpha = 0f
    }


    private fun hideTemplate(template: Template) {
        innerGroupZView.elevation = 0f
        innerGroupZView.outlineProvider = null
        innerGroupZView.translationZ = 0f

        // translationX used instead of invisible, because textureView inside this view should be visible
        innerGroupZView.translationX = resources.displayMetrics.widthPixels + 500f

        val renderingSize = template.format.getRenderingSize()

        innerGroupZView.layoutParams =
            ConstraintLayout.LayoutParams(renderingSize.width, renderingSize.height).also {
                it.leftToLeft = ConstraintSet.PARENT_ID
                it.topToTop = ConstraintSet.PARENT_ID
            }
        innerGroupZView.requestLayout()
    }

    private fun showImage(template: Template): View {

        val format = template.format
        val renderingSize = template.format.getRenderingSize()

        binding.textureTemplate.layoutParams =
            ConstraintLayout.LayoutParams(renderingSize.width, renderingSize.height).also {
                it.leftToLeft = ConstraintSet.PARENT_ID
                it.topToTop = ConstraintSet.PARENT_ID
                it.bottomToBottom = 0
                it.rightToRight = 0
            }
        binding.textureTemplate.requestLayout()

        val imageTemplate = ImageView(this)

        imageTemplate.layoutParams = ConstraintLayout.LayoutParams(0, 0).also {
            it.leftToLeft = ConstraintSet.PARENT_ID
            it.topToTop = ConstraintSet.PARENT_ID
            it.bottomToBottom = 0
            it.rightToRight = 0
        }

        imageTemplate.setRatioBasedOnFormat(
            format,
            false,
            requestLayoutOnChange = false
        )

        imageTemplate.setMarginsByFormat(format)
        imageTemplate.setBackgroundColor(Color.WHITE)
        imageTemplate.setTemplateRoundedCornersAndShadow()

        binding.root.addView(imageTemplate)

        this.imageTemplate = imageTemplate
        return innerGroupZView
    }

    private fun showTexture() {
        binding.textureTemplate.translationX = 0f
        binding.textureTemplate.setTemplateRoundedCornersAndShadow()
    }

    override fun onDestroy() {
        super.onDestroy()
        recordViewModel.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        recordViewModel.onSaveInstanceState(outState)
    }

    override fun onBackPressed() {
        recordViewModel.stopRecordThread()
        super.onBackPressed()
    }

    private fun onFinishVideoRender(template: Template) {

        innerGroupZView.setTemplateRoundedCornersAndShadow()
        binding.textureTemplate.visibility = View.GONE

        showTemplateOnTop(template)

        templateView.post {
            templateView.startPlaying()
        }
    }

    private fun View.setMarginsByFormat(format: TemplateFormat) {
        updateLayoutParams<ConstraintLayout.LayoutParams> {
            val marginSide = when (format) {
                TemplateFormat.square, TemplateFormat.post -> 20.dpToPxInt()
                TemplateFormat.horizontal -> 10.dpToPxInt()
                TemplateFormat.story -> 0
            }
            leftMargin = marginSide
            rightMargin = marginSide
            marginStart = marginSide
            marginEnd = marginSide

            if (format == TemplateFormat.story) {
                topMargin = 26.dpToPxInt()
                bottomMargin = 30.dpToPxInt()

            } else if (format == TemplateFormat.post) {
                topMargin = 18.dpToPxInt()
                bottomMargin = 20.dpToPxInt()
            }
        }
    }

    private fun showTemplateOnTop(template: Template) {
        innerGroupZView.translationX = 0F
        binding.textureTemplate.visibility = View.INVISIBLE
        innerGroupZView.elevation = 2.dpToPixels()
        innerGroupZView.setMarginsByFormat(template.format)

        val set = ConstraintSet()
        set.clone(binding.root)

        set.constrainHeight(R.id.templateView, 0)
        set.constrainWidth(R.id.templateView, 0)

        val dimensionRatio = when (template.format) {
            TemplateFormat.post -> "H, 4:5"
            TemplateFormat.square -> "H, 1:1"
            TemplateFormat.horizontal -> "H, 16:9"
            TemplateFormat.story -> "W, 9:16"
        }
        set.setDimensionRatio(R.id.templateView, dimensionRatio)

        set.connect(
            R.id.templateView,
            ConstraintSet.START,
            ConstraintSet.PARENT_ID,
            ConstraintSet.START
        )
        set.connect(
            R.id.templateView,
            ConstraintSet.END,
            ConstraintSet.PARENT_ID,
            ConstraintSet.END
        )

        set.connect(
            R.id.templateView,
            ConstraintSet.TOP,
            ConstraintLayout.LayoutParams.PARENT_ID,
            ConstraintSet.TOP
        )
        set.connect(
            R.id.templateView,
            ConstraintSet.BOTTOM,
            ConstraintLayout.LayoutParams.PARENT_ID,
            ConstraintSet.BOTTOM
        )
        set.applyTo(binding.root)
        innerGroupZView.visibility = View.VISIBLE
    }
}

