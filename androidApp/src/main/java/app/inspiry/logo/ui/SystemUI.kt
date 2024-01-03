package app.inspiry.logo.ui

import android.os.Build
import android.view.View
import android.view.Window
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb


@Composable
fun SystemUi(windows: Window) {

    windows.statusBarColor = MaterialTheme.colors.background.toArgb()
    windows.navigationBarColor = MaterialTheme.colors.background.toArgb()

    if (Build.VERSION.SDK_INT >= 26) {
        @Suppress("DEPRECATION")
        if (MaterialTheme.colors.background.luminance() > 0.5f) {
            windows.decorView.systemUiVisibility = windows.decorView.systemUiVisibility or
                    View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }

        @Suppress("DEPRECATION")
        if (MaterialTheme.colors.background.luminance() > 0.5f) {
            windows.decorView.systemUiVisibility = windows.decorView.systemUiVisibility or
                    View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        }
    }
}