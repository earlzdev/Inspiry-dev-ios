package app.inspiry

import android.app.ActivityManager
import android.app.Application
import android.content.pm.ConfigurationInfo
import android.os.Build
import app.inspiry.bfpromo.BFPromoManager
import app.inspiry.core.ActivityRedirector
import app.inspiry.core.KoinCommonModule
import app.inspiry.core.KoinPlatformModule
import app.inspiry.core.analytics.*
import app.inspiry.core.data.templateCategory.TemplateCategoryProvider
import app.inspiry.core.data.templateCategory.TemplateCategoryProviderImpl
import app.inspiry.core.database.DriverFactory
import app.inspiry.core.database.ExternalResourceDao
import app.inspiry.core.database.InspDatabase
import app.inspiry.core.helper.ABTemplateAvailability
import app.inspiry.core.helper.ABTemplateAvailabilityImpl
import app.inspiry.core.helper.JsonHelper
import app.inspiry.core.log.ErrorHandler
import app.inspiry.core.log.KLogger
import app.inspiry.core.log.LoggerGetter
import app.inspiry.core.manager.*
import app.inspiry.core.media.BaseUnitsConverter
import app.inspiry.core.notification.FreeWeeklyTemplatesNotificationManager
import app.inspiry.core.template.MediaReadWrite
import app.inspiry.core.template.MyStoriesMigration
import app.inspiry.core.template.MyStoriesMigrationAndroid
import app.inspiry.core.template.TemplateReadWrite
import app.inspiry.edit.instruments.format.FormatsProvider
import app.inspiry.edit.instruments.format.FormatsProviderImpl
import app.inspiry.export.ExportCommonViewModel
import app.inspiry.export.PredefineAppProvider
import app.inspiry.font.KoinFontsModule
import app.inspiry.helpers.*
import app.inspiry.helpers.analytics.*
import app.inspiry.helpers.notification.KoinNotificationModule
import app.inspiry.logo.LogoActivity
import app.inspiry.logo.LogoGetFromLibraryImpl
import app.inspiry.logo.LogoRepository
import app.inspiry.logo.LogoViewModel
import app.inspiry.logo.data.LogoDataSource
import app.inspiry.logo.data.LogoDataSourceImpl
import app.inspiry.media.AndroidUnitsConverter
import app.inspiry.music.KoinMusicModule
import app.inspiry.music.android.KoinMusicPlatform
import app.inspiry.palette.KoinPaletteModule
import app.inspiry.removebg.RemoveBgProcessor
import app.inspiry.removebg.RemoveBgProcessorImpl
import app.inspiry.stickers.KoinStickersModule
import app.inspiry.textanim.TextAnimProvider
import app.inspiry.textanim.TextAnimProviderImpl
import app.inspiry.utils.ErrorHandlerAndroid
import app.inspiry.utils.TAG_TEMPLATE
import app.inspiry.utils.isAppInstalled
import app.inspiry.views.vector.InnerVectorViewAndroid
import coil.Coil
import coil.ImageLoader
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.decode.SvgDecoder
import coil.decode.VideoFrameDecoder
import com.adapty.Adapty
import com.adapty.models.AttributionType
import com.adapty.utils.ProfileParameterBuilder
import com.amplitude.api.Amplitude
import com.appsflyer.AppsFlyerConversionListener
import com.appsflyer.AppsFlyerLib
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okio.FileSystem
import org.koin.android.ext.android.get
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

class App : Application() {

    override fun onCreate() {
        K.init(this)
        super.onCreate()
        ap = this

        initFirebase()
        initFlipper()

        initKoin(initCoil())

        DefaultDirs.initialize(this)

        initAttribution()

        sendUserProperties()

        printFirebaseCloudToken()

        InnerVectorViewAndroid.setUpLottie(this)

        printOpenglVersion()

        val appViewModel: AppViewModel = get()
        appViewModel.onCreate(get(), get(), get())

        val myStoriesMigration: MyStoriesMigration = get()
        if (myStoriesMigration.needToMigrate())
            myStoriesMigration.performMigration()
    }


    private fun initCoil(): ImageLoader {
        val loader = ImageLoader.Builder(this)
            .components {
                // GIFs
                if (Build.VERSION.SDK_INT >= 28) {
                    add(ImageDecoderDecoder.Factory())
                } else {
                    add(GifDecoder.Factory())
                }
                // SVGs
                add(SvgDecoder.Factory())
                // Video frames
                add(VideoFrameDecoder.Factory())
            }
            .build()

        Coil.setImageLoader(loader)
        return loader
    }

    private fun initFirebase() {
        FirebaseApp.initializeApp(this)
        FirebaseAnalytics.getInstance(this).setAnalyticsCollectionEnabled(!BuildConfig.DEBUG)
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(!BuildConfig.DEBUG)
    }

    private fun initFlipper() {
        FlipperInitializerImpl().initialize(this)
    }

    private fun printFirebaseCloudToken() {
        if (BuildConfig.DEBUG) {
            FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    K.w("FCM", task.exception) {
                        "Fetching FCM registration token failed"
                    }
                    return@OnCompleteListener
                }
                val token = task.result
                K.d("FCM") {
                    "token = $token"
                }
            })
        }
    }

    private fun initKoin(imageLoader: ImageLoader) {

        startKoin {
            // use Koin logger

            //if (BuildConfig.DEBUG)
            //    androidLogger()
            // declare modules
            androidContext(this@App)
            modules(
                module {
                    single<GoogleAnalyticsManager> { GoogleAnalyticsManagerAndroid(get()) }
                    single<FacebookAnalyticsManager> { FacebookAnalyticsManagerAndroid(get()) }
                    single<AmplitudeAnalyticsManager> { AmplitudeAnalyticsManagerAndroid(get()) }
                    single<AppsflyerAnalyticsManager> { AppsflyerAnalyticsManagerAndroid(get()) }
                    single<AppodealAnalyticsManager> { AppodealAnalyticsManagerAndroid(get())}
                    single { JsonHelper.initJson() }
                    single<ErrorHandler> {
                        ErrorHandlerAndroid(get())
                    }

                    single<InspRemoteConfig> {
                        FirebaseRemoteConfigImpl()
                    }
                    single<LicenseManager> {
                        LicenseManagerImplAndroid(
                            get(),
                            get(),
                            BuildConfig.DEBUG,
                            get()
                        )
                    }
                    single { AppViewModel(get()) }

                    single<ActivityRedirector> { ActivityRedirectorImpl() }

                    single<BaseUnitsConverter> { AndroidUnitsConverter(get()) }
                    factory<FormatsProvider> { FormatsProviderImpl() }
                    factory<TextAnimProvider> { TextAnimProviderImpl(get()) }

                    single<TemplateCategoryProvider> {
                        val freeWeeklyTemplatesNotificationManager: FreeWeeklyTemplatesNotificationManager =
                            get()
                        TemplateCategoryProviderImpl(
                            get(), get()
                        ) { freeWeeklyTemplatesNotificationManager.currentWeekIndex.value }
                    }
                    single {
                        GooglePlayUpdateManager(get(), useFlexibleUpdates = false)
                    }

                    factory {
                        TemplateReadWrite(get(), get(), get(), get())
                    }
                    factory {
                        MediaReadWrite(get(), get())
                    }
                    factory<FileReadWrite> {
                        FileReadWriteAndroid(get())
                    }

                    factory<ABTemplateAvailability> {
                        val remoteConfig: InspRemoteConfig = get()
                        ABTemplateAvailabilityImpl(remoteConfig.templatesAvailabilityTest)
                    }

                    factory { (tag: String) ->
                        KLogger(BuildConfig.DEBUG, tag)
                    }

                    single { LoggerGetter() }

                    factory<RemoveBgProcessor> { RemoveBgProcessorImpl(get(), get(), get()) }

                    single { InspDatabase(DriverFactory(get()).createDriver()) }

                    factory { ExternalResourceDao(get()) }

                    factoryOf(::LogoDataSourceImpl) { bind<LogoDataSource>() }


                    single {
                        imageLoader
                    }

                    single {
                        FileSystem.SYSTEM
                    }

                    factory { BFPromoManager(get(), get()) }

                    single<ProbaSdkWrapper> {
                        ProbaSdkWrapperImpl()
                    }

                    factory<ToastManager> {
                        ToastManagerImpl(get())
                    }
                    factory<CommonClipBoardManager> {
                        CommonClipboardManagerImpl(get())
                    }
                    factory<ExportCommonViewModel> {
                        ExportCommonViewModel(get(), get())
                    }

                    factory<MyStoriesMigration> { MyStoriesMigrationAndroid(get(), get(), get()) }
                },
                KoinNotificationModule.getModule(),
                KoinMusicPlatform.getModulePlatform(),
                KoinMusicModule.getModule(),
                KoinFontsModule.getModule(),
                KoinStickersModule.getModule(),
                KoinPaletteModule.getModule(),
                KoinPlatformModule.getModule(),
                KoinCommonModule.getModule()
            )
        }
    }

    private fun getTestUserIdAdapty(): String {
        val isSamsung = Build.MANUFACTURER == "samsung"
        return if (isSamsung) "test_user_id"
        else "test_user_id_else"
    }


    private fun setUpAdaptyIntegrationAmplitude(userId: String?, deviceId: String?) {
        val profile = ProfileParameterBuilder()

        userId?.let {
            profile.withAmplitudeUserId(it)
        }
        deviceId?.let {
            profile.withAmplitudeDeviceId(it)
        }

        Adapty.updateProfile(profile) {}

        val probaSdkWrapper: ProbaSdkWrapper = get()
        probaSdkWrapper.initProbaSdk(this, userId, AppsFlyerLib.getInstance().getAppsFlyerUID(this))
    }

    private fun initAttribution() {

        Adapty.activate(
            applicationContext,
            "adapty_key",
            if (BuildConfig.DEBUG) getTestUserIdAdapty() else null
        )

        val amplitudeManager: AmplitudeAnalyticsManager = get()
        val amplitudeAndroid = amplitudeManager as AmplitudeAnalyticsManagerAndroid

        K.i("initAttribution") {
            "testUserId ${getTestUserIdAdapty()}, amplitudeUserId ${amplitudeManager.getUserId()}," +
                    " amplDeviceId ${amplitudeManager.getDeviceId()}"
        }
        val amplitudeDeviceId: String? = amplitudeAndroid.getDeviceId()

        if (amplitudeDeviceId != null) {
            setUpAdaptyIntegrationAmplitude(
                amplitudeAndroid.getUserId(),
                amplitudeDeviceId
            )
        } else {
            Amplitude.getInstance().setDeviceIdCallback {
                K.i("initAttribution") {
                    "deviceIdCallback testUserId ${getTestUserIdAdapty()}, amplitudeUserId ${amplitudeManager.getUserId()}," +
                            " amplDeviceId ${amplitudeManager.getDeviceId()}"
                }
                val appViewModel: AppViewModel = get()
                appViewModel.applicationScope.launch(Dispatchers.Main) {
                    setUpAdaptyIntegrationAmplitude(
                        amplitudeAndroid.getUserId(),
                        it
                    )
                }
            }
        }

        AppsFlyerLib.getInstance()
            .init("appsflyer-api-key", object : AppsFlyerConversionListener {

                //install conversion
                override fun onConversionDataSuccess(map: MutableMap<String, Any>) {
                    Adapty.updateAttribution(
                        map,
                        AttributionType.APPSFLYER,
                        AppsFlyerLib.getInstance().getAppsFlyerUID(this@App)
                    ) {

                    }
                }

                override fun onConversionDataFail(p0: String?) {
                }


                //provides retargeting conversion data when an existing app is launched, either manually or through deep linking
                override fun onAppOpenAttribution(p0: MutableMap<String, String>?) {
                }

                override fun onAttributionFailure(p0: String?) {
                }

            }, this)
        AppsFlyerLib.getInstance().start(this)

    }

    fun printOpenglVersion() {
        K.d(TAG_TEMPLATE) {
            val activityManager: ActivityManager =
                getSystemService(ACTIVITY_SERVICE) as ActivityManager
            val configurationInfo: ConfigurationInfo = activityManager.deviceConfigurationInfo
            "Device Supported OpenGL ES Version = " + configurationInfo.glEsVersion
        }
    }

    fun sendUserProperties() {
        val analyticManager: AnalyticsManager = get()
        // can't check if app is installed since android 11
        if (Build.VERSION.SDK_INT < 30) {
            analyticManager.setUserProperty(
                "is_facebook_installed",
                isAppInstalled(PredefineAppProvider.facebookPackage).toString()
            )
            analyticManager.setUserProperty(
                "is_instagram_installed",
                isAppInstalled(PredefineAppProvider.instagramPackage).toString()
            )
            analyticManager.setUserProperty(
                "is_tiktok_installed",
                isAppInstalled("com.ss.android.ugc.trill").toString()
            )
            analyticManager.setUserProperty(
                "is_snapchat_installed",
                isAppInstalled("com.snapchat.android").toString()
            )
            analyticManager.setUserProperty(
                "is_vk_installed",
                isAppInstalled("com.vkontakte.android").toString()
            )
            analyticManager.setUserProperty(
                "is_whatsapp_installed",
                isAppInstalled(PredefineAppProvider.whatsappPackage).toString()
            )
            analyticManager.setUserProperty(
                "is_mojo_installed",
                isAppInstalled("video.mojo").toString()
            )
            analyticManager.setUserProperty(
                "is_instories_installed",
                isAppInstalled("io.instories").toString()
            )
        }
        val settings: com.russhwolf.settings.Settings = get()
        val amplitudeAnalyticsManager: AmplitudeAnalyticsManager = get()

        analyticManager.setDayAndSessionNums(settings, amplitudeAnalyticsManager)
    }
}

@Deprecated("use local context instead")
lateinit var ap: App