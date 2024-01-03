package app.inspiry.edit.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.viewinterop.AndroidView
import app.inspiry.dialog.TimelinePanel
import app.inspiry.edit.instruments.TimeLineInstrumentModel
import app.inspiry.music.InstrumentViewAndroid

@Composable
fun TimeLineUI(model: TimeLineInstrumentModel) {
    val panel: InstrumentViewAndroid = TimelinePanel(model)

    AndroidView(factory = { context ->
        return@AndroidView panel.createView(context)
    })

    DisposableEffect(Unit) {
        onDispose {
            panel.onDestroyView()
        }
    }
}