package app.inspiry.subscribe.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material.MaterialTheme
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import app.inspiry.activities.MainActivity
import app.inspiry.core.log.KLogger
import app.inspiry.core.manager.DEFAULT_ADAPTY_ACCESS
import app.inspiry.core.manager.LicenseManager
import app.inspiry.helpers.logError
import app.inspiry.onboarding.OnBoardingViewModel.Companion.SOURCE_ONBOARDING
import app.inspiry.subscribe.viewmodel.SubscribeViewModelAndroid
import app.inspiry.subscribe.viewmodel.SubscribeViewModelAndroidFactory
import com.adapty.Adapty
import com.adapty.errors.AdaptyError
import com.adapty.models.GoogleValidationResult
import com.adapty.models.ProductModel
import com.adapty.models.PurchaserInfoModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.android.ext.android.get
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf

class SubscribeActivity : AppCompatActivity() {

    val licenseManager: LicenseManager by inject()
    lateinit var viewModel: SubscribeViewModelAndroid

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        viewModel.handleIntent(intent)
    }

    override fun onBackPressed() {
        if (viewModel.source.startsWith(SOURCE_ONBOARDING)) {
            startActivity(Intent(this, MainActivity::class.java))
        }
        super.onBackPressed()
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel =
            ViewModelProvider(
                this,
                SubscribeViewModelAndroidFactory(get(), get(), get(), licenseManager)
            )[SubscribeViewModelAndroid::class.java]

        viewModel.onCreate(savedInstanceState, intent)

        setContent {
            MaterialTheme(colors = MaterialTheme.colors.copy(isLight = true)) {
                SubscribeScreen(viewModel, ::onBackPressed, this)
            }
        }

        lifecycleScope.launch {
            licenseManager.hasPremiumState.collectLatest {
                if (it)
                    onBackPressed()
            }
        }
    }
}