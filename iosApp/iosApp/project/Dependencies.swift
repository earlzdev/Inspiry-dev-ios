//
//  Dependencies.swift
//  MusicFeatureIos
//
//  Created by vlad on 21/4/21.
//

import Foundation
import shared
import Swinject
import SwinjectAutoregistration

class Dependencies {
    static let diContainer = Container()
    
    static func registerAppDependencies() {
        diContainer.registerCommonDependencies()
        diContainer.registerMusicDependencies()
        diContainer.registerFontDependencies()
    }
    
    static func resolveAuto<T>(diContainer: Container = Dependencies.diContainer) -> T {
        return diContainer.resolveAuto()
    }
    
    static func isDebug() -> Bool {
        var result = false
        #if DEBUG
        result = true
        #endif
        
        return result
    }
}

extension Container {
    
    func registerFontDependencies() {
        register(UploadedFontsProvider.self, factory: { _ in
            UploadedFontsProviderImpl()
        })
        register(TextCaseHelper.self, factory: { _ in
            TextCaseHelperImpl()
        })
        register(PlatformFontPathProvider.self, factory: { _ in
            PlatformFontPathProviderImpl()
        }).inObjectScope(.container)
        register(FontsManager.self, factory: { container in
            FontsManager(isDebug: Dependencies.isDebug(), platformFontPathProvider: container.resolve(
                         PlatformFontPathProvider.self)!)
        })
        
        autoregister(PlatformFontObtainerImpl.self, initializer: PlatformFontObtainerImpl.init)
    }
    
    func registerMusicDependencies() {
        register(LocalMusicLibraryProvider.self, factory: {
            _ in
            LocalMusicLibraryProviderImpl()
        })
        autoregister(ITunesMusicLibraryProvider.self, initializer: ITunesMusicLibraryProvider.init)
        autoregister(RemoteLibraryMusicProvider.self, initializer: RemoteLibraryMusicProvider.init)
        
        register(HttpClient.self, factory: {
            container in
            HttpClientFactory().createHttpClient(json: container.resolve(Json.self)!, loggerGetter: container.resolve(LoggerGetter.self)!)
        }).inObjectScope(.container)
    }
    
    func resolveAuto<T>() -> T {
        return resolve(T.self)!
    }
    
    func registerCommonDependencies() {
        
        register(Json.self) { _ in
            JsonHelper().doInitJson()
        }.inObjectScope(.container)
        
        register(Settings.self, factory: { _ in
            SettingUtil().getAppleSettings()
        }).inObjectScope(.container)
        
        autoregister(InspRemoteConfig.self, initializer: RemoteConfigIos.init).inObjectScope(.container)
        
        register(KLogger.self, factory: { _, tag in
            KLogger(isLogEnabled: Dependencies.isDebug(), tag: tag)
        })
        register(LoggerGetter.self, factory: { _ in
            LoggerGetter()
        })
        
        register(JsonCacheClient.self) { _ in
            JsonCacheClientImpl()
        }
        register(FacebookAnalyticsManager.self) { _ in
            FacebookAnalyticsManagerImpl()
        }
        register(AmplitudeAnalyticsManager.self) { _ in
            AmplitudeAnalyticsManagerImpl()
        }
        register(GoogleAnalyticsManager.self) { _ in
            GoogleAnalyticsManagerImpl()
        }
        register(AppsflyerAnalyticsManager.self) { _ in
            AppsflyerAnalyticsManagerImpl()
        }
        register(AppodealAnalyticsManager.self) { _ in
            AppodealAnalyticsManagerImpl()
        }
        autoregister(AnalyticsManager.self,
                     initializer: CommonAnalyticsManager.init).inObjectScope(.container)
        
        autoregister(StickersProvider.self, initializer: StickersProviderImpl.init)
        register(FileReadWrite.self, factory: { resolver in
            return FileReadWriteImpl()
        }).inObjectScope(.container)
        autoregister(MediaReadWrite.self, initializer: MediaReadWrite.init)
        autoregister(TextAnimProvider.self, initializer: TextAnimProviderImpl.init)
        autoregister(BaseUnitsConverter.self, initializer: UnitConverterApple.init).inObjectScope(.container)
        //autoregister(ABTemplateAvailability.self, initializer: ABTemplateAvailabilityEmpty.init)
        register(ABTemplateAvailability.self, factory: { resolver in
            return ABTemplateAvailabilityEmpty()
            // let removeConfig = resolver.resolve(InspRemoteConfig.self)!
            // return ABTemplateAvailability(abTestMode: removeConfig.templatesAvailabilityTest)
        }).inObjectScope(.container)
        
        autoregister(TemplateReadWrite.self, initializer: TemplateReadWrite.init).inObjectScope(.container)
        
        register(TemplateCategoryProvider.self, factory: { resolver in
            
            return TemplateCategoryProviderImpl(settings: resolver.resolve(Settings.self)!, remoteConfig: resolver.resolve(InspRemoteConfig.self)!, getFreeThisWeekIndex: {
                    nil
            })
        }).inObjectScope(.container)
        
        autoregister(ViewProvider.self, initializer: InspViewProvider.init)
        autoregister(ViewFromMediaFactory.self, initializer: EmptyViewFromMediaFactory.init)
        
        register(InspDatabase.self, factory: { resolver in
            InspDatabaseCompanion.init().invoke(driver: DriverFactory.init().createDriver())
        }).inObjectScope(.container)
        
        autoregister(ExternalResourceDao.self, initializer: ExternalResourceDao.init)
        autoregister(MusicFileCreator.self, initializer: MusicFileCreator.init)
        
        register(OkioFileSystem.self, factory: { resolver in
            OkioFileSystem.Companion.init().SYSTEM
        }).inObjectScope(.container)
        
        autoregister(MusicDownloadingViewModel.self, initializer: MusicDownloadingViewModel.init)
        
        register(LicenseManager.self, factory: { resolver in
            
            LicenseManagerApple(prefPurchases: resolver.resolve(Settings.self)!, remoteConfig: resolver.resolve(InspRemoteConfig.self)!, alwaysFreeVersion: false, analyticsManager: resolver.resolve(AnalyticsManager.self)!)
            
        }).inObjectScope(.container)
        
        autoregister(ToastManager.self, initializer: ToastManagerImpl.init)
        autoregister(CommonClipBoardManager.self, initializer: CommonClipboardManagerImpl.init)
        autoregister(ExportCommonViewModel.self, initializer: ExportCommonViewModel.init)
        autoregister(AppViewModel.self, initializer: AppViewModel.init)
        
        autoregister(NotificationScheduler.self, initializer: NotificationSchedulerApple.init)
            
        
    }
}

