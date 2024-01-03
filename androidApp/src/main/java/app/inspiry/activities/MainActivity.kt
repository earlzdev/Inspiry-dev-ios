package app.inspiry.activities

import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.lifecycle.lifecycleScope
import app.inspiry.MR
import app.inspiry.R
import app.inspiry.core.analytics.AnalyticsManager
import app.inspiry.core.manager.InspRemoteConfig
import app.inspiry.core.log.KLogger
import app.inspiry.core.manager.LicenseManager
import app.inspiry.core.util.doOnce
import app.inspiry.databinding.ActivityMainBinding
import app.inspiry.databinding.ItemTabBinding
import app.inspiry.dialog.rating.RatingRequest
import app.inspiry.stories.AbsStoriesFragment
import app.inspiry.stories.CategorizedTemplatesFragment
import app.inspiry.stories.MyStoriesFragment
import app.inspiry.helpers.GooglePlayUpdateManager
import app.inspiry.helpers.notification.NotificationSenderAndroid
import app.inspiry.export.viewmodel.getExportFilesFolder
import app.inspiry.subscribe.ui.SubscribeActivity
import app.inspiry.utils.Constants
import app.inspiry.utils.getColorCompat
import app.inspiry.utils.getIdFromAttr
import com.afollestad.materialdialogs.MaterialDialog
import com.google.firebase.analytics.FirebaseAnalytics
import com.russhwolf.settings.Settings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf
import java.lang.ref.WeakReference


class MainActivity : BaseThemeActivity() {

    var onBackPressListener: (() -> Boolean)? = null
    val tabs = mutableListOf<ItemTabBinding>()
    var displayBottomBanner = true
    lateinit var binding: ActivityMainBinding

    val settings: Settings by inject()
    val licenseManger: LicenseManager by inject()
    val remoteConfig: InspRemoteConfig by inject()
    val analyticsManager: AnalyticsManager by inject()
    val googlePlayUpdateManager: GooglePlayUpdateManager by inject()

    val logger: KLogger by inject {
        parametersOf("MainActivity")
    }

    fun setUpTab(
        tab: ItemTabBinding,
        createFragment: (() -> AbsStoriesFragment)?,
        onClick: (View) -> Unit = {}
    ) {
        tabs.add(tab)
        tab.root.setOnClickListener {

            if (createFragment != null) {

                tabs.forEach {
                    it.root.isActivated = false
                }
                tab.root.isActivated = true

                val fragment: AbsStoriesFragment = createFragment()
                val tag = fragment::class.java.simpleName

                if (supportFragmentManager.findFragmentByTag(tag) != null)
                    return@setOnClickListener

                supportFragmentManager.beginTransaction()
                    .replace(R.id.container, fragment, tag).commit()

                val bundle = Bundle()
                bundle.putString(
                    FirebaseAnalytics.Param.SCREEN_NAME,
                    fragment::class.java.simpleName
                )
                bundle.putString(FirebaseAnalytics.Param.SCREEN_CLASS, tag)
                FirebaseAnalytics.getInstance(this)
                    .logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle)
            }

            onClick(it)
        }
    }

    fun openTab(index: Int = 0) {
        tabs[index].root.performClick()
    }

    private val inflater by lazy { LayoutInflater.from(this) }
    private fun createTab(text: Int, position: Int, ic: Int): ItemTabBinding {

        val rootTabs = if (position == 1) binding.viewTabs else FrameLayout(this)

        val tab = ItemTabBinding.inflate(inflater, rootTabs, false)
        tab.textTab.setText(text)
        tab.textTab.typeface = MR.fonts.made.bold.getTypeface(this)

        tab.iconTab.setImageResource(ic)
        tab.root.setBackgroundResource(getIdFromAttr(androidx.appcompat.R.attr.selectableItemBackground))

        binding.viewTabs.addView(tab.root)
        return tab
    }

    override fun onStart() {
        super.onStart()
        licenseManger.restorePurchases()

        clearExportCache()
    }

    private fun clearExportCache() {
        val cacheDir = getExportFilesFolder(false)
        if (cacheDir.exists()) {
            lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    cacheDir.deleteRecursively()
                    cacheDir.delete()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)
        window.setBackgroundDrawable(ColorDrawable(getColorCompat(R.color.template_activity_bg)))

        createTabs()

        val newTabToOpen = checkInitialPage(intent, defaultPage = -1)
        if (newTabToOpen != -1) {
            openTab(newTabToOpen)
        } else if (savedInstanceState == null) {
            openTab(0)
        } else {
            activateCorrectFragment()
        }


        checkGooglePlayUpdates()
        askRating()

        mayShowFreeDialog()
        registerLicenseStateObserver()
    }

    // return new tab
    private fun checkInitialPage(intent: Intent, defaultPage: Int): Int {
        setIntent(Intent())

        val initialPageIndex = intent.getIntExtra(EXTRA_INITIAL_PAGE_INDEX, defaultPage)

        NotificationSenderAndroid.checkNotificationOpened(intent, analyticsManager)

        return initialPageIndex
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        val newTabToOpen = checkInitialPage(intent, defaultPage = -1)
        if (newTabToOpen != -1)
            openTab(newTabToOpen)
    }

    private fun registerLicenseStateObserver() {
        lifecycleScope.launchWhenCreated {
            licenseManger.hasPremiumState.drop(1).collectLatest {
                if (it)
                    onGotPremium()
            }
        }
    }

    private fun mayShowFreeDialog() {
        if (licenseManger.mayShowFreeDialogCausePaymentsFailed()) {

            settings.doOnce("free_cause_payments_failed") {

                MaterialDialog(this).show {
                    title(text = getString(app.inspiry.projectutils.R.string.dialog_free_title))
                    message(text = getString(app.inspiry.projectutils.R.string.dialog_free_message))
                    positiveButton(text = context.getString(android.R.string.ok), click = {
                        dismiss()
                    })
                }

            }
        }
    }

    override fun onResume() {
        super.onResume()

        googlePlayUpdateManager.checkUpdates(
            stalled = true,
            scope = lifecycleScope,
            activityRef = WeakReference(this)
        )
    }

    private fun checkGooglePlayUpdates() {
        googlePlayUpdateManager.checkUpdates(
            stalled = false,
            scope = lifecycleScope,
            activityRef = WeakReference(this)
        )
    }

    private fun askRating() {
        RatingRequest(this, remoteConfig)
            .showRatingDialog()
    }

    private fun onGotPremium() {
        tabs.clear()
        binding.viewTabs.removeAllViews()
        createTabs()

        activateCorrectFragment()
    }

    private fun activateCorrectFragment() {
        val myStoriesFragment =
            supportFragmentManager.findFragmentByTag(MyStoriesFragment::class.java.simpleName)

        if (myStoriesFragment != null) tabs[1].root.isActivated = true
        else tabs[0].root.isActivated = true
    }

    private fun createTabs() {

        val hasAllInclusive = licenseManger.hasPremiumState.value
        setUpTab(
            createTab(app.inspiry.projectutils.R.string.tab_templates, 0, R.drawable.ic_tab_templates),
            { CategorizedTemplatesFragment() })

        setUpTab(createTab(
            app.inspiry.projectutils.R.string.tab_my_stories,
            if (hasAllInclusive) 2 else 1,
            R.drawable.ic_tab_mystories
        ), { MyStoriesFragment() })

        if (!hasAllInclusive) {
            setUpTab(createTab(app.inspiry.projectutils.R.string.tab_pro, 2, R.drawable.ic_premium_off), null, {

                startActivity(
                    Intent(this, SubscribeActivity::class.java)
                        .putExtra(Constants.EXTRA_SOURCE, "tab")
                )
            })
        }
    }

    override fun onBackPressed() {
        if (onBackPressListener?.invoke() != true)
            super.onBackPressed()
    }

    companion object {
        const val EXTRA_INITIAL_PAGE_INDEX = "initial_page_index"
    }
}