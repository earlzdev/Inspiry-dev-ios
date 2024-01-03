package app.inspiry.edit

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.WindowCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import app.inspiry.R
import app.inspiry.core.media.BaseUnitsConverter
import app.inspiry.core.notification.NotificationType
import app.inspiry.core.template.TemplateReadWrite
import app.inspiry.core.template.TemplateViewModel
import app.inspiry.edit.ui.CombinedEditScreenUI
import app.inspiry.helpers.TemplateViewModelFactory
import app.inspiry.helpers.notification.NotificationSenderAndroid
import app.inspiry.utils.*
import app.inspiry.views.group.BaseGroupZView
import app.inspiry.views.template.InspTemplateViewAndroid
import app.inspiry.views.template.InspTemplateViewCreator
import app.inspiry.views.template.TemplateMode
import app.inspiry.views.template.setTemplateRoundedCornersAndShadow
import com.google.accompanist.insets.ProvideWindowInsets
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent
import org.koin.android.ext.android.get

class EditActivity : AppCompatActivity() {

    private lateinit var editViewModel: EditViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val templateViewModel = ViewModelProvider(
            this,
            TemplateViewModelFactory(get())
        )[TemplateViewModel::class.java]

        val templateReadWrite: TemplateReadWrite = get()
        val unitsConverter: BaseUnitsConverter = get()

        val innerGroupZView =
            BaseGroupZView(this, templateView = null, unitsConverter = unitsConverter)
        val templateView = InspTemplateViewCreator.createInspTemplateView(
            innerGroupZView,
            initialTemplateMode = TemplateMode.EDIT
        )

        val innerView = (templateView as InspTemplateViewAndroid).innerView
        innerView.setTemplateRoundedCornersAndShadow()

        editViewModel = ViewModelProvider(
            this, EditViewModelFactory(
                licenseManger = get(),
                templateCategoryProvider = get(),
                templateViewModel = templateViewModel,
                freeWeeklyTemplatesNotificationManager = get(),
                scope = this.lifecycleScope,
                templateView = templateView,
                appViewModel = get(),
                storyUnfinishedNotificationManager = get(),
                templateSaver = templateReadWrite,
                mediaReadWrite = get(),
                templatePath = intent.getTemplatePath(),
                initialOriginalTemplateData = intent.getOriginalTemplateData(),
                externalResourceDao = get(),
                settings = get(),
                remoteConfig = get(),
                analyticsManager = get(),
                platformFontPathProvider = get(),
                uploadedFontsProvider = get(),
                textCaseHelper = get()
            )
        )[EditViewModel::class.java]

        editViewModel.loadTemplatePath()
        setTheme(R.style.EditThemeActivity)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        val rootConstraint = ConstraintLayout(this)
        rootConstraint.id = View.generateViewId()

        rootConstraint.layoutParams = ViewGroup.LayoutParams(
            MATCH_PARENT,
            MATCH_PARENT
        )

        setContent {
            ProvideWindowInsets(windowInsetsAnimationsEnabled = true) {
                CombinedEditScreenUI(viewModel = editViewModel)
            }
        }
        checkNotificationIntent()

        KeyboardVisibilityEvent.registerEventListener(this) { visible ->
            if (!visible) editViewModel.isKeyboardShown.value = false
        }

        innerView.id = View.generateViewId()
    }

    override fun onStop() {
        super.onStop()

        if (editViewModel.saveOnStop()) {
            editViewModel.saveTemplateToFile()
        }
    }

    private fun checkNotificationIntent() {
        if (NotificationSenderAndroid.checkNotificationOpened(intent, analyticsManager = get())) {
            intent.action = ""
            val type =
                NotificationType.valueOf(intent.getStringExtra(Constants.EXTRA_NOTIFICATION_TYPE)!!)
            if (type == NotificationType.UNFINISHED_STORY) {
                intent.putExtra(EXTRA_RETURN_TO_MAIN_ACT_ON_CLOSE, true)
            }
        }
    }

    companion object {
        const val EXTRA_RETURN_TO_MAIN_ACT_ON_CLOSE = "return_to_main_act_on_close"
        const val EDIT_PREVIEW_ANIMATION_TIME = 240 //edit -> preview and preview -> edit animation time (ms)
    }
}
