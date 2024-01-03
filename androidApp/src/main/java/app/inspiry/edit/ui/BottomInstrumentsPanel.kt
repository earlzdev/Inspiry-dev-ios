package app.inspiry.edit.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.keyframes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import app.inspiry.edit.EditViewModel
import app.inspiry.edit.instruments.InstrumentAdditional
import app.inspiry.edit.instruments.InstrumentsManager
import app.inspiry.edit.instruments.InstrumentMain.*
import app.inspiry.edit.instruments.ui.*
import app.inspiry.edit.instruments.ui.media.MediaInstrumentsUI
import app.inspiry.edit.instruments.ui.media.TrimVideoPage
import app.inspiry.edit.instruments.ui.media.VideoChangeVolume
import app.inspiry.font.ui.FontDialogMain
import app.inspiry.utilities.toCColor
import app.inspiry.utils.pixelsToDp
import kotlin.math.abs
import kotlin.math.roundToInt

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun BottomInstrumentsPanelUI(model: EditViewModel) {
    val state by model.instrumentsManager.instrumentsState.collectAsState()
    Column(
        Modifier
            .background(LocalColors.current.instrumentsBar.toCColor()),
    ) {


        InnerPanel(
            instrumentsManager = model.instrumentsManager,
            instrumentAdditionalState = state.currentAdditionalInstrument
        )
        AnimatedContent(
            targetState = state.currentMainInstrument,
            transitionSpec = {
                (
                        slideInVertically { height -> height } + fadeIn() with
                                slideOutVertically { height -> height } + fadeOut()
                        ).using(
                        SizeTransform(clip = false, sizeAnimationSpec = { initialSize, targetSize ->
                            keyframes {
                                val dur =
                                    ((abs((targetSize.height - initialSize.height)).pixelsToDp()) * 3.5f).roundToInt()
                                durationMillis = if (dur > 400) 400 else dur
                            }
                        })
                    )
            }
        ) { newState ->
            when (newState) {
                ADD_VIEWS -> {
                    model.instrumentsManager.getAddViewsModel()?.let {
                        MenuAddViewPanelUI(viewModel = it)
                    }
                }
                DEFAULT -> {
                    model.instrumentsManager.getDefaultModel()?.let {
                        DefaultInstrumentsUI(it)
                    }

                }
                TEXT -> {
                    model.instrumentsManager.getTextModel()?.let {
                        TextInstrumentsUI(it)
                    }
                }
                MOVABLE -> {
                    model.instrumentsManager.getColorModel()?.let {
                        ColorPanel(it)
                    }
                }
                SOCIAL_ICONS -> {
                    model.instrumentsManager.getSocialModel()?.let {
                        SocialIconsUI(it)
                    }

                }
                TIMELINE -> {
                    model.instrumentsManager.getTimeLineModel()?.let {
                        TimeLineUI(it)
                    }
                }
                MEDIA -> {
                    model.instrumentsManager.getMediaModel()?.let {
                        MediaInstrumentsUI(it)
                    }
                }
                DEBUG -> {
                    DebugPanelUI(model)
                }
                SLIDES -> {
                    Box(
                        modifier = Modifier
                            .background(LocalColors.current.instrumentsBar.toCColor())
                            .fillMaxWidth()
                            .height(64.dp)
                    ) {
                        Text(modifier = Modifier.align(Alignment.Center), text = "slides edit instrument", color = Color.White)
                    }
                }
                else -> {
                    EmptyPanel()
                }
            }
        }


    }
}

@Composable
fun EmptyPanel() {
    Box(
        modifier = Modifier
            .background(LocalColors.current.instrumentsBar.toCColor())
            .fillMaxWidth()
            .height(64.dp)
    )
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun InnerPanel(
    instrumentsManager: InstrumentsManager,
    instrumentAdditionalState: InstrumentAdditional?
) {
    val targetState by remember(instrumentAdditionalState) {
        mutableStateOf(
            instrumentAdditionalState
        )
    }
    AnimatedContent(
        targetState = targetState,
        transitionSpec = {
            (
                    slideInVertically { height -> height } + fadeIn() with
                            slideOutVertically { height -> height } + fadeOut()
                    ).using(
                    SizeTransform(clip = false, sizeAnimationSpec = { initialSize, targetSize ->
                        keyframes {
                            val dur =
                                ((abs((targetSize.height - initialSize.height)).pixelsToDp()) * 3.5f).roundToInt()
                            durationMillis = if (dur > 400) 400 else dur
                        }
                    })
                )
        }
    ) { panel ->
        panel?.let { secondPanel ->
            when (secondPanel) {
                InstrumentAdditional.COLOR, InstrumentAdditional.BACK -> {
                    instrumentsManager.getColorModel()?.let {
                        ColorPanel(model = it)
                    }
                }
                InstrumentAdditional.FORMAT -> {
                    instrumentsManager.getFormatModel()?.let {
                        TemplateFormatsPanel(viewModel = it)
                    }
                }
                InstrumentAdditional.SIZE -> {
                    instrumentsManager.getSizeModel()?.let {
                        EditTextSizePanel(viewModel = it)
                    }
                }
                InstrumentAdditional.FONT -> {
                    instrumentsManager.getFontModel()?.let {
                        FontDialogMain(viewModel = it)
                    }
                }
                InstrumentAdditional.EDIT_MUSIC -> {
                    MusicPanelUI(instrumentsManager = instrumentsManager)
                }
                InstrumentAdditional.VOLUME -> {
                    instrumentsManager.getVideoEditModel()?.let {
                        VideoChangeVolume(model = it)
                    }
                }
                InstrumentAdditional.TRIM -> {
                    instrumentsManager.getVideoEditModel()?.let {
                        TrimVideoPage(model = it)
                    }
                }
                InstrumentAdditional.SHAPE -> {
                    instrumentsManager.getShapesModel()?.let {
                        ShapesInstrument(model = it)
                    }
                }
                InstrumentAdditional.SLIDE -> {
                    instrumentsManager.getSlidesModel()?.let { 
                        SlidesInstrument(model = it)
                    }
                }
                else -> {
                }
            }
        }
    }
}