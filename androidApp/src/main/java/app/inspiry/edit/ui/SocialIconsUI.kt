package app.inspiry.edit.ui

import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import app.inspiry.edit.socialIconsSelector.SocialIconsPanel
import app.inspiry.edit.socialIconsSelector.SocialIconsViewModel
import app.inspiry.music.InstrumentViewAndroid

@Composable
fun SocialIconsUI(model: SocialIconsViewModel) {
    val context = LocalContext.current as AppCompatActivity
    val panel: InstrumentViewAndroid = SocialIconsPanel(model, context)
    AndroidView(factory = {
        return@AndroidView panel.createView(context)
    })
}