package app.inspiry.helpers

import android.app.Activity
import android.content.Context
import android.content.IntentSender
import app.inspiry.utils.printDebug
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.ktx.updatePriority
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import java.lang.IllegalStateException
import java.lang.ref.WeakReference
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.suspendCoroutine

/*
 Flexible update is not supported fully by now.
 Because we need to display UI
 Example of how to display UI can be found here
 https://github.com/SanojPunchihewa/InAppUpdater/blob/master/updatemanager/src/main/java/com/sanojpunchihewa/updatemanager/UpdateManager.java
 */
class GooglePlayUpdateManager(val context: Context, private val useFlexibleUpdates: Boolean) {

    private val appUpdateManager = AppUpdateManagerFactory.create(context)

    //might be nonnull after update has been finished. Should add no trouble.
    private var currentRequest: RequestUpdate? = null

    fun checkUpdates(stalled: Boolean, scope: CoroutineScope, activityRef: WeakReference<Activity>) {
        scope.launch {

            try {
                val request = if (stalled) checkStalledImmediateUpdates() else checkUpdates()
                val act = activityRef.get()
                if (request != null && act != null) {
                    requestUpdate(act, request)
                }
            } catch (e: Exception) {
                e.printDebug()
            }
        }
    }

    //should be called onResume in every app entry point.
    // also this check is useless if we already called checkUpdates
    suspend fun checkStalledImmediateUpdates(): RequestUpdate? {

        val updateType = currentRequest?.type ?: return null

        return if (updateType == AppUpdateType.IMMEDIATE) {
            suspendCoroutine { continuation ->

                appUpdateManager
                    .appUpdateInfo
                    .addOnSuccessListener { appUpdateInfo ->

                        if (appUpdateInfo.updateAvailability()
                            == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS
                        ) {
                            continuation.resumeWith(Result.success(RequestUpdate(AppUpdateType.IMMEDIATE, appUpdateInfo)))
                        }
                    }
            }
        } else null
    }

    //return true if there are flexible updates and we need to show a button to finish it
    suspend fun checkStalledFlexibleUpdates(): Boolean {

        val updateType = currentRequest?.type ?: return false

        return if (updateType == AppUpdateType.FLEXIBLE) {
            suspendCoroutine { continuation ->

                appUpdateManager
                    .appUpdateInfo
                    .addOnSuccessListener { appUpdateInfo ->

                        if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                            continuation.resumeWith(Result.success(true))
                        } else {
                            continuation.resumeWith(Result.success(false))
                        }
                    }.addOnFailureListener {
                        continuation.resumeWith(Result.failure(it))
                    }
            }
        } else false
    }

    fun finishFlexibleUpdate() {
        appUpdateManager.completeUpdate()
    }

    fun requestUpdate(activity: Activity, request: RequestUpdate) {

        this.currentRequest = request

        if (request.type == AppUpdateType.FLEXIBLE) {
            createListenerIfFlexibleUpdate()
        }

        try {
            appUpdateManager.startUpdateFlowForResult(
                // Pass the intent that is returned by 'getAppUpdateInfo()'.
                request.appUpdateInfo,
                // The current activity making the update request.
                activity,
                // Or pass 'AppUpdateType.FLEXIBLE' to newBuilder() for
                // flexible updates.
                AppUpdateOptions.newBuilder(request.type)
                    .setAllowAssetPackDeletion(true)
                    .build(),
                // Include a request code to later monitor this update request.
                ACTIVITY_REQUEST_CODE
            )


        } catch (e: IntentSender.SendIntentException) {
            e.printDebug()
        }
    }

    //we should track update status.
    fun createListenerIfFlexibleUpdate(): Flow<Double> {

        return callbackFlow {

            val listener = InstallStateUpdatedListener { state ->

                when (state.installStatus()) {
                    InstallStatus.DOWNLOADING -> {
                        val bytesDownloaded = state.bytesDownloaded()
                        val totalBytesToDownload = state.totalBytesToDownload()

                        val progress = bytesDownloaded.toDouble() / totalBytesToDownload

                        channel.trySend(progress)
                    }
                    InstallStatus.DOWNLOADED -> {
                        channel.close()
                    }
                    InstallStatus.FAILED -> {
                        channel.close(IllegalStateException("update failed errorCode ${state.installErrorCode()}"))
                    }
                    InstallStatus.CANCELED -> {
                        channel.close(IllegalStateException("update cancelled"))
                    }
                }
            }

            // Before starting an update, register a listener for updates.
            appUpdateManager.registerListener(listener)

        }
    }

    suspend fun checkUpdates(): RequestUpdate? {

        val appUpdateInfoTask = appUpdateManager.appUpdateInfo


        return suspendCoroutine { continuation ->

            appUpdateInfoTask.addOnFailureListener {
                continuation.resumeWith(Result.failure(it))
            }

            appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->

                val updateAvailability = appUpdateInfo.updateAvailability()

                when (updateAvailability) {
                    UpdateAvailability.UPDATE_AVAILABLE -> {

                        //Staleness - for how much days update was available.
                        val stalenessDays = appUpdateInfo.clientVersionStalenessDays() ?: -1
                        val priority = appUpdateInfo.updatePriority

                        if (appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE) &&
                            (stalenessDays >= STALENESS_IMMEDIATE_UPDATE || priority >= REQUEST_IMMEDIATE_IF_EXCEEDS_PRIORITY)
                        ) {

                            continuation.resumeWith(
                                Result.success(
                                    RequestUpdate(
                                        AppUpdateType.IMMEDIATE,
                                        appUpdateInfo
                                    )
                                )
                            )

                        } else if (useFlexibleUpdates && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE) &&
                            (stalenessDays >= STALENESS_FLEXIBLE_UPDATE || priority >= REQUEST_FLEXIBLE_IF_EXCEEDS_PRIORITY)
                        ) {

                            continuation.resumeWith(
                                Result.success(
                                    RequestUpdate(
                                        AppUpdateType.FLEXIBLE,
                                        appUpdateInfo
                                    )
                                )
                            )
                        } else {
                            continuation.resumeWith(Result.success(null))
                        }
                    }

                    UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS -> {
                        continuation.resumeWith(Result.success(RequestUpdate(AppUpdateType.IMMEDIATE, appUpdateInfo)))
                    }

                    else -> continuation.resumeWith(Result.success(null))
                }
            }
        }
    }

    class RequestUpdate(@AppUpdateType val type: Int, val appUpdateInfo: AppUpdateInfo)

    companion object {
        val STALENESS_FLEXIBLE_UPDATE = 7
        val STALENESS_IMMEDIATE_UPDATE = 21

        //priority we can specify in Google Play console. range [1;5]
        val REQUEST_FLEXIBLE_IF_EXCEEDS_PRIORITY = 3
        val REQUEST_IMMEDIATE_IF_EXCEEDS_PRIORITY = 4

        val ACTIVITY_REQUEST_CODE = 193
    }
}