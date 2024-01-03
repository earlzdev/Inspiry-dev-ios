package app.inspiry.helpers.notification

import app.inspiry.BuildConfig
import app.inspiry.core.manager.AppViewModel
import app.inspiry.core.notification.*
import app.inspiry.core.util.SharedConstants
import org.koin.dsl.module


object KoinNotificationModule {
    fun getModule() = module {
        val isDebug = BuildConfig.DEBUG
        factory<NotificationScheduler> { NotificationSchedulerAndroid(get()) }

        single {
            val appViewModel: AppViewModel = get()
            FreeWeeklyTemplatesNotificationManager(
                get(), get(), get(), appViewModel.applicationScope, get(),
                get(), freeTemplatesPeriodDays = SharedConstants.FreeTemplatesPeriodDays
            )
        }
        single {
            val appViewModel: AppViewModel = get()
            RemoveBgNotificationManager(get(), get(), get(), appViewModel.applicationScope)
        }
        single {
            val appViewModel: AppViewModel = get()
            DiscountNotificationManager(get(), get(), appViewModel.applicationScope, get(), get())

        }
        single {
            StoryUnfinishedNotificationManager(
                get(),
                get(),
                get(),
                get(),
                get(),
                isDebug
            )
        }

        single {

            val freeWeeklyTemplatesNotificationManager: FreeWeeklyTemplatesNotificationManager =
                get()
            val discountNotificationManager: DiscountNotificationManager = get()
            val unfinishedNotificationManager: StoryUnfinishedNotificationManager = get()
            val removeBgNotificationManager: RemoveBgNotificationManager = get()
            NotificationManagersContainer(listOf(
                freeWeeklyTemplatesNotificationManager,
                discountNotificationManager,
                unfinishedNotificationManager,
                removeBgNotificationManager
            ))
        }

        factory {

            NotificationSenderAndroid(
                get(),
                get(),
                get(),
                get()
            )
        }
        single { NotificationProvider() }

    }
}
