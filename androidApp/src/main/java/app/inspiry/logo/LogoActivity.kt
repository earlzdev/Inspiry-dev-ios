package app.inspiry.logo

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import app.inspiry.MR
import app.inspiry.core.ActivityRedirector
import app.inspiry.core.data.Size
import app.inspiry.core.database.data.LogoItem
import app.inspiry.logo.data.LogoDataSource
import app.inspiry.logo.data.LogoDataSourceImpl
import app.inspiry.logo.ui.*
import app.inspiry.utilities.toCColor
import kotlinx.coroutines.launch
import org.koin.android.ext.android.get

class LogoActivity : AppCompatActivity() {

    private lateinit var viewModel: LogoViewModel
    private val colors = LogoColors()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val activityRedirector: ActivityRedirector = get()
        val logoDataSourceImpl: LogoDataSourceImpl = get()
        val logoRepository = LogoRepository(logoDataSourceImpl, LogoGetFromLibraryImpl(this))

        viewModel = LogoViewModel(
            logoRepository = logoRepository,
            licenseManager = get(),
            scope = this.lifecycleScope,
            subscribeAction = { activityRedirector.openSubscribeActivity(this, "logo") },
            pickLogoAction = ::pickLogo,
        )

        setContent {
            MaterialTheme(
                colors = MaterialTheme.colors.copy(
                    background = colors.background.toCColor(),
                    isLight = false
                )
            ) {
                SystemUi(windows = window)
                Column(
                    Modifier
                        .fillMaxSize()
                        .clickable(enabled = false) {}
                        .background(colors.background.toCColor())
                ) {
                    LogosMain(viewModel = viewModel, colors = colors)
                }
            }
        }
        lifecycleScope.launch {
            viewModel.error.collect { error ->
                when (error) {
                    ErrorsLogos.SAME_PATH_ERROR -> Toast.makeText(
                        this@LogoActivity,
                        resources.getString(MR.strings.toast_db_logo.resourceId),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }


    private fun pickLogo(logo: LogoItem) {
        val size = Size(logo.width.toInt(), logo.height.toInt())
        val result = Intent()
        result.putExtra(URI, logo.path)
        result.putExtra(SIZE, size)
        setResult(RESULT_OK, result)
        finish()
    }

    companion object {
        const val URI = "uri"
        const val SIZE = "size"
    }
}
