package app.inspiry.helpers

import ai.proba.probasdk.ProbaSdk
import android.content.Context
import app.inspiry.BuildConfig
import com.amplitude.api.Amplitude
import com.appsflyer.AppsFlyerLib
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout
import org.json.JSONObject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.jvm.Throws

/**
 * proba is an A/B test platform.
 * documentation - https://platform.appbooster.com/ab/docs
 */
class ProbaSdkWrapperImpl: ProbaSdkWrapper {

    private var probaSdk: ProbaSdk? = null
    private val jobInitializeProba: Job = Job()

    private val defaults: Map<String, String> by lazy {
        mapOf(
            "subscribe_screen_ui_version" to "1"
        )
    }

    override fun initProbaSdk(context: Context, amplitudeUserId: String?, appsflyerId: String?) {
        return
        val builder =
            ProbaSdk.Builder(context) // you can initiate sdk using either application or activity context
                .appId("0000")
                .sdkToken("proba-sdk-key")
                .usingShake(false) // true by default for debug mode, turn it off if you are already using shake motion in your app for other purposes
                .defaults(defaults)
        builder.showLogs(BuildConfig.DEBUG)

        if (appsflyerId != null) {
            builder.appsFlyerId(appsflyerId) // optional, use AppsFlyerLib.getInstance().getAppsFlyerUID(context) if AppsFlyer integration is needed
        }

        if (amplitudeUserId != null)
            builder.amplitudeUserId(amplitudeUserId) // optional, use Amplitude.getInstance().getUserId()

        val sdk = builder.build()
        val experiments = sdk.getExperiments()
        AppsFlyerLib.getInstance().setAdditionalData(experiments)

        val userProperties = JSONObject(experiments)
        Amplitude.getInstance().setUserProperties(userProperties)

        probaSdk = sdk
        jobInitializeProba.cancel()
    }

    override suspend fun getValueAfterInit(key: String, waitFetch: Boolean): String {

        // don't wait more than 15s
        try {
            return withTimeout(15000L) {

                if (probaSdk == null) {
                    jobInitializeProba.join()
                }

                if (waitFetch) {
                    getValueAfterFetchDefaultOnThrow(probaSdk!!, key)
                } else {
                    getValueInstantly(key)
                }
            }
        } catch (e: TimeoutCancellationException) {
            return getValueInstantly(key)
        }
    }

    override fun getValueInstantly(key: String) = probaSdk?.get(key) ?: returnDefault(key)

    @Throws(IllegalStateException::class)
    private suspend fun getValueAfterFetch(proba: ProbaSdk, key: String): String {
        return suspendCoroutine {
            proba.fetch(onSuccessListener = object : ProbaSdk.OnSuccessListener {

                override fun onSuccess() {
                    val result = proba[key]
                    if (result == null)
                        it.resumeWith(Result.failure(IllegalStateException("no value for key ${key}")))
                    else
                        it.resume(result)
                }

            }, onErrorListener = object : ProbaSdk.OnErrorListener {
                override fun onError(th: Throwable) {
                    it.resumeWith(Result.failure(th))
                }
            })
        }
    }

    private suspend fun getValueAfterFetchDefaultOnThrow(proba: ProbaSdk, key: String): String {
        return try {
            getValueAfterFetch(proba, key)
        } catch (e: Exception) {
            returnDefault(key)
        }
    }

    private fun returnDefault(key: String): String {
        return defaults[key]
            ?: throw IllegalStateException("there's no default value for key=${key}")
    }
}