package app.inspiry.edit.ui

import android.content.Intent
import android.content.pm.ActivityInfo
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import app.inspiry.core.notification.FreeWeeklyTemplatesNotificationManager
import app.inspiry.edit.EditViewModel
import app.inspiry.edit.instruments.FullScreenTools
import app.inspiry.featurepromo.RemoveBgPromoActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.java.KoinJavaComponent.getKoin

@Composable
fun DebugPanelUI(viewModel: EditViewModel) {
    val items = listOf(
        "Edit json from template",
        "Edit saved to file json",
        "Display demo sources",
        "Stickers",
        "Orientation Toggle",
        "Next free for week",
        "RemoveBgPromo"
    )
    val activity = LocalContext.current as AppCompatActivity

    Column() {
        items.forEachIndexed { index, text ->
            Box(modifier = Modifier.fillMaxWidth().height(50.dp).clickable {
                when (index) {
                    0 -> {
                        activity.lifecycleScope.launch {
                            viewModel.saveTemplateToFile().join()
                            viewModel.instrumentsManager.selectFullScreenTool(FullScreenTools.DEBUG_EDIT_JSON)
                        }
                        viewModel.removeBottomPanel()
                    }
                    1 -> {
                        activity.lifecycleScope.launch {
                            viewModel.saveTemplateToFile().join()
                            viewModel.instrumentsManager.selectFullScreenTool(FullScreenTools.DEBUG_EDIT_SAVED)
                        }
                        viewModel.removeBottomPanel()
                    }
                    2 -> {
                        viewModel.setDemoToAllImages()
                        viewModel.removeBottomPanel()
                    }
                    3 -> {
                        viewModel.instrumentsManager.selectFullScreenTool(FullScreenTools.STICKERS)
                        viewModel.removeBottomPanel()
                    }
                    4 -> {
                        activity.lifecycleScope.launch {
                            val current = activity.requestedOrientation
                            delay(1000)

                            if (current != ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
                                activity.requestedOrientation =
                                    ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                            else
                                activity.requestedOrientation =
                                    ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                        }
                        viewModel.removeBottomPanel()
                    }
                    5 -> {
                        val freeWeeklyTemplatesNotificationManager: FreeWeeklyTemplatesNotificationManager = getKoin().get()
                        freeWeeklyTemplatesNotificationManager.debugMoveToNextPeriod(false)
                        viewModel.removeBottomPanel()
                    }
                    6 -> {
                        activity.startActivity(Intent(activity, RemoveBgPromoActivity::class.java))
                        viewModel.removeBottomPanel()
                    }
                }
            }, contentAlignment = Alignment.Center) {
                Text(text, color = Color.White, fontSize = 15.sp)
            }
        }
    }
}