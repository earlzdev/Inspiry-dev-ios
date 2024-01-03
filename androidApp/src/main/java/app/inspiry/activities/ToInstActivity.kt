package app.inspiry.activities

import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import app.inspiry.R
import app.inspiry.databinding.ActivityToInstBinding
import app.inspiry.core.analytics.AnalyticsManager
import app.inspiry.core.manager.INSTAGRAM_PAGE_LINK
import app.inspiry.core.manager.InstagramSubscribeHolder
import app.inspiry.utils.Constants
import app.inspiry.utils.IntentUtils
import app.inspiry.utils.getOriginalTemplateData
import org.koin.android.ext.android.inject

class ToInstActivity : AppCompatActivity() {
    val analyticManager: AnalyticsManager by inject()
    val subscribeHolder: InstagramSubscribeHolder by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.ToInstActivity)

        val binding: ActivityToInstBinding =
            ActivityToInstBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)

        val source = intent.getStringExtra(Constants.EXTRA_SOURCE)!!
        binding.buttonSubscribe.setOnClickListener {
            analyticManager.subscribeToInstClick(intent.getOriginalTemplateData(), source)
            startActivity(IntentUtils.openLink(INSTAGRAM_PAGE_LINK))
            subscribeHolder.setSubscribed()
            finish()
        }
    }

    companion object {
        const val SOURCE_TEMPLATES = "templates"
    }
}