package app.inspiry.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import app.inspiry.bfpromo.BFPromoManager
import app.inspiry.bfpromo.ui.mayOpenBFPromoOnStart
import app.inspiry.onboarding.OnBoardingActivity
import app.inspiry.onboarding.OnBoardingViewModel.Companion.KEY_ONBOARDING_FINISHED
import app.inspiry.core.analytics.AmplitudeAnalyticsManager
import app.inspiry.core.manager.InspRemoteConfig
import app.inspiry.core.log.KLogger
import app.inspiry.utils.IntentUtils
import com.russhwolf.settings.Settings
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf

class StartActivity : Activity() {

    private val settings: Settings by inject()
    private val remoteConfig: InspRemoteConfig by inject()
    private val logger: KLogger by inject {
        parametersOf("start-activity")
    }
    private val amplitudeAnalyticsManager: AmplitudeAnalyticsManager by inject()
    private val bfPromoManager: BFPromoManager by inject()

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        handleIntent(intent)
        setIntent(intent)
    }

    private fun parseFirebaseMessagingIntent(intent: Intent?): String? {

        val extras = intent?.extras
        if (extras?.containsKey("google.sent_time") == true) {

            return extras["link"] as? String?

        }
        return null
    }

    private fun handleIntent(intent: Intent?): Boolean {
        val parse = parseFirebaseMessagingIntent(intent)

        if (parse != null) {

            startActivity(IntentUtils.openLink(parse).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            setIntent(Intent())

            if (!parse.startsWith("inspiry")) {
                finish()
                return true
            }
        }
        return false
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            if (savedInstanceState == null)
                amplitudeAnalyticsManager.sendEvent("app_open")

            if (handleIntent(intent)) return

            if (settings.getBoolean(KEY_ONBOARDING_FINISHED, false)) {

                if (bfPromoManager.mayOpenBFPromoOnStart(this)) {

                } else {
                    startActivity(
                        Intent(this, MainActivity::class.java)
                            .addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                    )
                }

            } else {
                startActivity(
                    Intent(this, OnBoardingActivity::class.java)
                        .addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                )
            }

        } finally {
            finish()
        }
    }
}