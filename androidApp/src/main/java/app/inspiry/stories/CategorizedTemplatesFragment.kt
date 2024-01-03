package app.inspiry.stories

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.inspiry.MR
import app.inspiry.R
import app.inspiry.activities.MainActivity
import app.inspiry.bfpromo.BFPromoManager
import app.inspiry.bfpromo.ui.BFPromoBannerUI
import app.inspiry.core.ActivityRedirector
import app.inspiry.core.data.TemplatePath
import app.inspiry.core.data.templateCategory.TemplateCategory
import app.inspiry.core.data.templateCategory.TemplateCategoryProvider
import app.inspiry.core.notification.FreeWeeklyTemplatesNotificationManager
import app.inspiry.core.template.TemplateReadWrite
import app.inspiry.core.template.TemplatesAdapterHelper
import app.inspiry.stories.ui.TemplateCategoryTabs
import app.inspiry.utils.dpToPixels
import app.inspiry.utils.dpToPxInt
import app.inspiry.utils.getIdFromAttr
import com.google.android.material.appbar.AppBarLayout
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class CategorizedTemplatesFragment : AbsStoriesFragment() {

    private lateinit var bottomTabs: View
    private var bottomBanner: View? = null
    private var currentCategoryIndex = MutableStateFlow(0)
    private val templateCategoryProvider: TemplateCategoryProvider by inject()
    private val weeklyTemplatesNotificationManager: FreeWeeklyTemplatesNotificationManager by inject()
    private val activityRedirector: ActivityRedirector by inject()

    override val myStories: Boolean
        get() = false

    override fun loadData() {
        notifyAdapter(TemplateReadWrite.predefinedTemplatePaths(templateCategories))
    }

    private val bfPromoManager: BFPromoManager by inject()

    private lateinit var templateCategories: List<TemplateCategory>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        lifecycleScope.launch {
            licenseManager.hasPremiumState.collect {

                adapter?.setPremiumChanged(it)
                if (it) {
                    if (bottomBanner != null) {
                        refreshCategories(true)
                        mainView.removeView(bottomBanner)
                        bottomBanner = null
                    }

                } else {
                    if ((activity as MainActivity).displayBottomBanner && bottomBanner == null) {
                        addBottomBanner()
                        refreshCategories(false)
                    }
                }
            }
        }
        lifecycleScope.launch {
            weeklyTemplatesNotificationManager.currentWeekIndex.drop(1).collect {
                refreshCategories(licenseManager.hasPremiumState.value)
            }
        }

        super.onViewCreated(view, savedInstanceState)
    }

    private fun onClickRemoveBanner() {
        (activity as MainActivity).displayBottomBanner = false
        mainView.removeView(this.bottomBanner)
        this.bottomBanner = null
    }

    private fun createBottomBannerPremium(): View {
        val bottomBanner = LinearLayout(requireActivity())
        bottomBanner.orientation = LinearLayout.HORIZONTAL

        val shape = GradientDrawable()
        shape.cornerRadius = 10.dpToPixels()
        shape.colors = intArrayOf(0xe55C59FC.toInt(), 0xe536CFFF.toInt())
        shape.gradientType = GradientDrawable.LINEAR_GRADIENT
        shape.orientation = GradientDrawable.Orientation.RIGHT_LEFT

        bottomBanner.background = shape
        bottomBanner.foreground =
            requireContext().getDrawable(requireContext().getIdFromAttr(androidx.appcompat.R.attr.selectableItemBackgroundBorderless))

        bottomBanner.setOnClickListener {
            activityRedirector.openSubscribeActivity(requireActivity(), SOURCE_FOR_PURCHASE)
        }
        bottomBanner.clipToOutline = true

        val iconClose = ImageView(requireContext())
        iconClose.scaleType = ImageView.ScaleType.CENTER_INSIDE
        iconClose.setImageResource(R.drawable.ic_bottom_banner_close)
        iconClose.setBackgroundResource(requireContext().getIdFromAttr(androidx.appcompat.R.attr.selectableItemBackground))
        iconClose.setOnClickListener {
            onClickRemoveBanner()
        }

        bottomBanner.gravity = Gravity.CENTER_VERTICAL
        bottomBanner.addView(iconClose, 50.dpToPxInt(), MATCH_PARENT)

        val textsLayout = LinearLayout(requireContext())
        textsLayout.orientation = LinearLayout.VERTICAL

        val topText = TextView(requireContext())
        topText.setSingleLine()
        topText.setTextColor(Color.WHITE)
        topText.textSize = 15f

        topText.typeface = MR.fonts.nunito.bold.getTypeface(requireContext())

        topText.gravity = Gravity.CENTER_HORIZONTAL

        val bottomText = TextView(requireContext())
        bottomText.setSingleLine()
        bottomText.setTextColor(Color.WHITE)
        bottomText.textSize = 12f
        bottomText.typeface = MR.fonts.nunito.regular.getTypeface(requireContext())

        bottomText.text = getString(app.inspiry.projectutils.R.string.banner_trial_subtitle)
        bottomText.gravity = Gravity.CENTER_HORIZONTAL

        textsLayout.addView(topText, MATCH_PARENT, WRAP_CONTENT)
        textsLayout.addView(bottomText, LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT).also {
            it.topMargin =
                (-2).dpToPxInt()
        })
        topText.text =
            resources.getString(app.inspiry.projectutils.R.string.subscribe_try_days_button)

        bottomBanner.addView(textsLayout, LinearLayout.LayoutParams(0, WRAP_CONTENT, 1f).also {
            it.rightMargin = 50.dpToPxInt()
        })

        return bottomBanner
    }

    private fun createBottomBannerPromo(worksUntilUnixDate: Long): View {
        return ComposeView(requireActivity()).also {
            it.setContent {
                BFPromoBannerUI(
                    worksUntilUnixDate,
                    onClickRemoveBanner = ::onClickRemoveBanner,
                    onClickBanner = {
                        activityRedirector.openBFPromoActivity(
                            requireActivity(),
                            SOURCE_FOR_PURCHASE
                        )
                    })
            }
        }
    }

    private fun addBottomBanner() {

        val bannerBfPromoTime = bfPromoManager.getBannerDisplayDate()

        val bottomBanner = if (bannerBfPromoTime == null) {
            createBottomBannerPremium()
        } else {
            createBottomBannerPromo(bannerBfPromoTime)
        }

        mainView.addView(bottomBanner,
            CoordinatorLayout.LayoutParams(MATCH_PARENT, 43.dpToPxInt()).also {
                it.gravity = Gravity.BOTTOM
                it.anchorGravity = Gravity.BOTTOM
                it.bottomMargin = 68.dpToPxInt()
                it.leftMargin = 30.dpToPxInt()
                it.rightMargin = 30.dpToPxInt()
                it.behavior = AppBarLayout.ScrollingViewBehavior()
            })

        this.bottomBanner = bottomBanner
    }

    private fun refreshCategories(isPremium: Boolean) {
        templateCategories = templateCategoryProvider.getTemplateCategories(isPremium)
        mainView.removeView(bottomTabs)
        createTabs()
        loadData()
    }

    private fun createTabs() {
        val tabs = createComposeTabs()

        mainView.addView(tabs,
            CoordinatorLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT).also {
                it.gravity = Gravity.BOTTOM
                it.anchorGravity = Gravity.BOTTOM
            })
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val root = super.onCreateView(inflater, container, savedInstanceState) as ViewGroup

        binding.recyclerView.apply {
            setPadding(
                paddingLeft,
                8.dpToPxInt(),
                paddingRight,
                48.dpToPxInt()
            )
        }

        templateCategories =
            templateCategoryProvider.getTemplateCategories(licenseManager.hasPremiumState.value)

        createTabs()

        val layoutManager = binding.recyclerView.layoutManager as GridLayoutManager

        var lastVisibleItemPos = 0
        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {

                var firstItemPosition = 0
                val lastCompletelyVisibleItemPos =
                    layoutManager.findLastCompletelyVisibleItemPosition()
                if (lastCompletelyVisibleItemPos != -1) lastVisibleItemPos =
                    lastCompletelyVisibleItemPos

                for ((index, templateCategory) in templateCategories.withIndex()) {
                    firstItemPosition += templateCategory.size + 1

                    if (firstItemPosition > lastVisibleItemPos - 1) {
                        currentCategoryIndex.value = index
                        break
                    }
                }
            }
        })

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mainView.removeView(bottomTabs)
        bottomBanner?.let { mainView.removeView(it) }
    }

    override fun createNewAdapter(it: MutableList<TemplatePath>): Pair<TemplatesAdapter, RecyclerView.Adapter<*>> {
        val helper = TemplatesAdapterHelper(
            coroutineContext = lifecycleScope.coroutineContext,
            templates = it,
            myStories = false,
            hasPremium = licenseManager.hasPremiumState.value,
            instagramSubscribed = instagramSubscribeHolder.subscribed.value,
            categories = templateCategories
        )
        val adapter = TemplatesAdapter(
            helper, requireActivity(),
            binding.recyclerView, this
        )

        return adapter to adapter
    }

    override fun notifyAdapter(it: MutableList<TemplatePath>) {
        if (adapter != null) adapter?.updateCategories(categories = templateCategories)
        super.notifyAdapter(it)
    }

    private fun scrollToCategory(category: TemplateCategory) {
        var firstItemPosition = 0

        for (templateCategory in templateCategories) {
            if (category == templateCategory) break

            firstItemPosition += templateCategory.size + 1
        }

        /*K.i("scrollToCategory") {
            "firstItemPosition $firstItemPosition, even = ${firstItemPosition % 2}"
        }*/

        (binding.recyclerView.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(
            firstItemPosition,
            1.dpToPxInt()
        )
    }

    private fun createComposeTabs(): View {
        bottomTabs = ComposeView(requireContext()).apply {
            setContent {
                MaterialTheme(
                    colors = MaterialTheme.colors.copy(isLight = true)
                ) {
                    val currentIndex by currentCategoryIndex.collectAsState()
                    TemplateCategoryTabs(
                        templateCategories = templateCategories,
                        currentIndex
                    ) { categoryIndex ->
                        scrollToCategory(templateCategories[categoryIndex])
                    }
                }
            }
        }
        return bottomTabs
    }
}

private const val SOURCE_FOR_PURCHASE = "templates_banner"
