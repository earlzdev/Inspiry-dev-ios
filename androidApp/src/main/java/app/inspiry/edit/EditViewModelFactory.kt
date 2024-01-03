package app.inspiry.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import app.inspiry.core.analytics.AnalyticsManager
import app.inspiry.core.data.OriginalTemplateData
import app.inspiry.core.data.TemplatePath
import app.inspiry.core.data.templateCategory.TemplateCategoryProvider
import app.inspiry.core.database.ExternalResourceDao
import app.inspiry.core.manager.AppViewModel
import app.inspiry.core.manager.InspRemoteConfig
import app.inspiry.core.manager.LicenseManager
import app.inspiry.core.notification.FreeWeeklyTemplatesNotificationManager
import app.inspiry.core.notification.StoryUnfinishedNotificationManager
import app.inspiry.core.template.MediaReadWrite
import app.inspiry.core.template.TemplateReadWrite
import app.inspiry.core.template.TemplateViewModel
import app.inspiry.font.helpers.TextCaseHelper
import app.inspiry.font.provider.PlatformFontPathProvider
import app.inspiry.font.provider.UploadedFontsProvider
import app.inspiry.views.template.InspTemplateView
import com.russhwolf.settings.Settings
import kotlinx.coroutines.CoroutineScope

class EditViewModelFactory(
    private val licenseManger: LicenseManager,
    private val templateCategoryProvider: TemplateCategoryProvider,
    private val templateViewModel: TemplateViewModel,
    private val freeWeeklyTemplatesNotificationManager: FreeWeeklyTemplatesNotificationManager,
    private val scope: CoroutineScope,
    private val templateView: InspTemplateView,
    private val appViewModel: AppViewModel,
    private val storyUnfinishedNotificationManager: StoryUnfinishedNotificationManager,
    private val templateSaver: TemplateReadWrite,
    private val mediaReadWrite: MediaReadWrite,
    private var templatePath: TemplatePath,
    private val initialOriginalTemplateData: OriginalTemplateData?,
    private val externalResourceDao: ExternalResourceDao,
    private val settings: Settings,
    private val remoteConfig: InspRemoteConfig,
    private val analyticsManager: AnalyticsManager,
    private val platformFontPathProvider: PlatformFontPathProvider,
    private val uploadedFontsProvider: UploadedFontsProvider,
    private val textCaseHelper: TextCaseHelper
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return EditViewModel(
            licenseManger,
            templateCategoryProvider,
            templateViewModel,
            freeWeeklyTemplatesNotificationManager,
            scope,
            templateView,
            appViewModel,
            storyUnfinishedNotificationManager,
            templateSaver,
            mediaReadWrite,
            templatePath,
            initialOriginalTemplateData,
            externalResourceDao,
            settings,
            remoteConfig,
            analyticsManager,
            platformFontPathProvider,
            uploadedFontsProvider,
            textCaseHelper
        ) as T
    }
}