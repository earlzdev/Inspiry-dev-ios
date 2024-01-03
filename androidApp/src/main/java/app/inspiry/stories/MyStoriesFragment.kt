package app.inspiry.stories

import android.content.Intent
import android.graphics.Outline
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.*
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.RecyclerView
import app.inspiry.MR
import app.inspiry.R
import app.inspiry.activities.MainActivity
import app.inspiry.core.analytics.AnalyticsManager
import app.inspiry.core.data.TemplatePath
import app.inspiry.core.manager.INSTAGRAM_PAGE_LINK
import app.inspiry.core.manager.INSTAGRAM_PROFILE_NAME
import app.inspiry.core.manager.InspRemoteConfig
import app.inspiry.core.template.MyTemplatesViewModel
import app.inspiry.core.template.TemplateReadWrite
import app.inspiry.core.template.TemplatesAdapterHelper
import app.inspiry.subscribe.ui.SubscribeActivity
import app.inspiry.utils.*
import com.afollestad.materialdialogs.LayoutMode
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.bottomsheets.BottomSheet
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import dev.icerock.moko.resources.desc.desc
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class MyStoriesFragment : AbsStoriesFragment() {

    lateinit var viewModel: MyTemplatesViewModel

    override val myStories: Boolean
        get() = true

    override fun loadData() {
        viewModel.loadMyStories()
    }

    var emptyView: View? = null
    lateinit var iconContact: View
    var topBanner: View? = null

    val remoteConfig: InspRemoteConfig by inject()
    val analyticsManager: AnalyticsManager by inject()

    private var firstOnStartCalled = false

    override fun notifyAdapter(it: MutableList<TemplatePath>) {
        super.notifyAdapter(it)

        if (adapter == null || adapter!!.itemCount == 0) {

            if (emptyView == null) {
                emptyView = createEmptyView()
                binding.root.addView(emptyView)
            }
        } else if (emptyView != null) {
            binding.root.removeView(emptyView)
        }
    }

    override fun createNewAdapter(it: MutableList<TemplatePath>): Pair<TemplatesAdapter, RecyclerView.Adapter<*>> {
        val helper = TemplatesAdapterHelper(
            coroutineContext = lifecycleScope.coroutineContext,
            templates = it,
            myStories = true,
            hasPremium = licenseManager.hasPremiumState.value,
            instagramSubscribed = instagramSubscribeHolder.subscribed.value
        )
        val templatesAdapter = TemplatesAdapter(
            helper, requireActivity(),
            binding.recyclerView, this
        )

        val globalAdapter =
            ConcatAdapter(templatesAdapter, InstagramSubscribeLabelAdapter(analyticsManager))
        return templatesAdapter to globalAdapter
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(
            this,
            MyTemplatesViewModelFactory(templateSaver)
        )[MyTemplatesViewModel::class.java]
    }

    private fun createEmptyView(): View {
        return LayoutInflater.from(requireContext())
            .inflate(R.layout.empty_my_stories, binding.root, false)
            .also {
                it.findViewById<TextView>(R.id.buttonTryAgain).setOnClickListener {
                    (requireActivity() as MainActivity).openTab()
                }
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        lifecycleScope.launch {
            licenseManager.hasPremiumState.collect {
                adapter?.setPremiumChanged(it)

                if (it) {
                    if (topBanner != null) {
                        (requireActivity() as MainActivity).binding.scrollTabs.removeView(topBanner)
                        topBanner = null
                    }
                } else {
                    addTopBanner()
                }
            }
        }

        lifecycleScope.launch {
            viewModel.templates.collect {
                if (it != null) {
                    notifyAdapter(it)
                }
            }
        }

        displayIconContact()

        super.onViewCreated(view, savedInstanceState)
    }

    private fun displayIconContact() {
        val iconContact = ImageView(requireContext())
        iconContact.setImageResource(R.drawable.btn_support)
        iconContact.alpha = 0.85f
        iconContact.scaleType = ImageView.ScaleType.CENTER_INSIDE
        iconContact.setOnClickListener {

            val supportEmail = FirebaseRemoteConfig.getInstance().getString("support_email")
            MaterialDialog(requireContext(), BottomSheet(LayoutMode.WRAP_CONTENT)).show {
                title(text = getString(app.inspiry.projectutils.R.string.feedback_title))
                message(
                    text = getString(
                        app.inspiry.projectutils.R.string.feedback_message,
                        requireContext().appVersion()
                    ) + "\n${supportEmail}"
                )

                negativeButton(text = getString(android.R.string.cancel), click = {

                })
                positiveButton(
                    text = getString(app.inspiry.projectutils.R.string.feedback_positive_button),
                    click = {
                        requireActivity().sendEmail(supportEmail)
                    })
            }
        }
        iconContact.foreground =
            requireContext().getDrawable(requireContext().getIdFromAttr(androidx.appcompat.R.attr.selectableItemBackgroundBorderless))

        iconContact.outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View, outline: Outline) {
                outline.setOval(0, 0, view.width, view.height)
            }
        }
        iconContact.clipToOutline = true

        mainView.addView(iconContact,
            CoordinatorLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT).also {
                it.gravity = Gravity.BOTTOM or Gravity.RIGHT
                it.bottomMargin = 15.dpToPxInt()
                it.leftMargin = 15.dpToPxInt()
                it.rightMargin = 15.dpToPxInt()
            })

        this.iconContact = iconContact
    }

    private fun createTopBannerPremium(): View {
        val topBanner = LinearLayout(requireActivity())
        topBanner.orientation = LinearLayout.HORIZONTAL
        topBanner.gravity = Gravity.CENTER_VERTICAL
        topBanner.setBackgroundResource(R.drawable.banner_stories_bg)
        topBanner.foreground =
            requireContext().getDrawable(requireContext().getIdFromAttr(androidx.appcompat.R.attr.selectableItemBackgroundBorderless))

        topBanner.setOnClickListener {
            startActivity(
                Intent(requireContext(), SubscribeActivity::class.java)
                    .putExtra(Constants.EXTRA_SOURCE, "my_stories_banner")
            )
        }
        topBanner.outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View, outline: Outline) {
                outline.setRoundRect(0, 0, view.width, view.height, 12.dpToPixels())
            }
        }
        topBanner.clipToOutline = true

        val imageContent = ImageView(requireContext())
        imageContent.setImageResource(R.drawable.banner_content)
        topBanner.addView(
            imageContent,
            LinearLayout.LayoutParams(85.dpToPxInt(), 58.dpToPxInt()).also {
                it.leftMargin = (-2).dpToPxInt()
                it.topMargin = (-2).dpToPxInt()
            })

        val leftText = TextView(requireContext())
        leftText.maxLines = 2
        leftText.setTextColor(android.graphics.Color.WHITE)
        leftText.textSize = 16f
        leftText.typeface = MR.fonts.nunito.bold.getTypeface(requireContext())

        leftText.text = getString(app.inspiry.projectutils.R.string.banner_my_stories_title)
        leftText.setPadding(3.dpToPxInt(), 0, 0, 2.dpToPxInt())

        topBanner.addView(leftText, LinearLayout.LayoutParams(0, WRAP_CONTENT, 1f))

        val textBg = GradientDrawable()
        textBg.cornerRadius = 10.dpToPixels()
        textBg.setStroke(2.dpToPxInt(), android.graphics.Color.WHITE)

        val rightText = TextView(requireContext())
        rightText.setSingleLine()
        rightText.setTextColor(android.graphics.Color.WHITE)
        rightText.textSize = 16f
        rightText.typeface = MR.fonts.nunito.regular.getTypeface(requireContext())
        rightText.text = getString(app.inspiry.projectutils.R.string.banner_my_stories_subtitle)
        rightText.background = textBg
        rightText.setPadding(15.dpToPxInt(), 2.dpToPxInt(), 15.dpToPxInt(), 0)

        topBanner.addView(rightText, LinearLayout.LayoutParams(WRAP_CONTENT, 29.dpToPxInt()).also {
            it.rightMargin = 12.dpToPxInt()
            it.leftMargin = 12.dpToPxInt()
        })

        topBanner.layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, 55.dpToPxInt())

        return topBanner
    }

    private fun addTopBanner() {
        val remoteConfigVal = remoteConfig.getString("my_stories_top_banner")

        val topBanner = if (remoteConfigVal == "premium") {
            createTopBannerPremium()
        } else null

        topBanner?.updateLayoutParams<LinearLayout.LayoutParams> {
            topMargin = 12.dpToPxInt()
            leftMargin = 22.dpToPxInt()
            rightMargin = 22.dpToPxInt()
        }
        if (topBanner != null) {
            (requireActivity() as MainActivity).binding.scrollTabs.addView(topBanner)
            this.topBanner = topBanner
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mainView.removeView(iconContact)
        if (topBanner != null) {
            (requireActivity() as MainActivity).binding.scrollTabs.removeView(topBanner)
        }
    }

    override fun onStart() {
        super.onStart()
        if (firstOnStartCalled) {
            lifecycleScope.launch {
                //give time to save the story if we return from editActivity
                delay(500L)
                adapter?.reloadTemplates()
            }
        } else {
            firstOnStartCalled = true
        }

        (requireActivity() as MainActivity).onBackPressListener = {
            if (adapter?.closeContext() != true) {
                (requireActivity() as MainActivity).openTab()
            }
            true
        }
    }

    override fun onStop() {
        super.onStop()
        (requireActivity() as MainActivity).onBackPressListener = null
    }

    class MyTemplatesViewModelFactory(
        private val templateSaver: TemplateReadWrite
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MyTemplatesViewModel(templateSaver) as T
        }
    }

    class InstagramSubscribeLabelAdapter(val analyticsManager: AnalyticsManager) :
        RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): RecyclerView.ViewHolder {

            val root = ComposeView(parent.context)
            root.setContent {
                MaterialTheme(
                    colors = MaterialTheme.colors.copy(
                        background = Color.White,
                        isLight = true
                    )
                ) {

                    Column(modifier = Modifier
                        .padding(horizontal = 20.dp)
                        .fillMaxWidth()
                        .clickable {

                            analyticsManager.subscribeToInstClick(
                                null,
                                "my_stories_label_at_the_end"
                            )
                            parent.context.startActivity(
                                IntentUtils
                                    .openLink(INSTAGRAM_PAGE_LINK)
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            )
                        }
                        .padding(horizontal = 10.dp, vertical = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally) {

                        Text(
                            MR.strings.my_stories_subscribe_to_inst.desc().toString(parent.context),
                            color = Color(0xff5161F6), fontSize = 14.sp,
                            textAlign = TextAlign.Center, lineHeight = 18.sp
                        )
                        Text(
                            "@${INSTAGRAM_PROFILE_NAME}",
                            modifier = Modifier
                                .padding(top = 6.dp)
                                .background(Color(0xff6271F7), RoundedCornerShape(8.dp))
                                .padding(horizontal = 13.dp, vertical = 2.dp)
                                .padding(bottom = 2.dp),
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            root.layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, WRAP_CONTENT)

            return object : RecyclerView.ViewHolder(root) {}
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            // nothing
        }

        override fun getItemViewType(position: Int): Int {
            return TemplatesAdapter.ITEM_TYPE_CATEGORY
        }

        override fun getItemCount(): Int = 1
    }
}