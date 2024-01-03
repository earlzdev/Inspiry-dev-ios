package app.inspiry.edit.instruments.ui.media

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.inspiry.R
import app.inspiry.edit.instruments.media.VideoEditViewModel
import app.inspiry.music.android.ui.SliderThumb2Layers
import app.inspiry.music.util.TrackUtils
import app.inspiry.slide.ui.*
import app.inspiry.utilities.toCColor
import app.inspiry.utils.BitmapUtils
import app.inspiry.utils.printDebug
import app.inspiry.views.media.InspMediaView
import app.inspiry.views.template.TemplateMode
import com.github.krottv.compose.sliders.DefaultTrack
import com.github.krottv.compose.sliders.SliderValueHorizontal
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlin.math.ceil
import kotlin.math.roundToInt

private val LocalColors = compositionLocalOf<SlidesPanelColors> { SlidesPanelDarkColors() }
private val LocalDimens = compositionLocalOf<SlidePanelDimens> { SlidePanelDimensPhone() }

@Composable
fun VideoChangeVolume(model: VideoEditViewModel) {

    val mediaView by model.currentView.collectAsState()

    DisposableEffect(key1 = mediaView) {

        onDispose {
            model.stopPlaying()
        }
    }

    Box(
        Modifier
            .fillMaxWidth()
            .height(LocalDimens.current.contentPanelHeight.dp)
            .background(LocalColors.current.topPanelBackground.toCColor()),
        contentAlignment = Alignment.CenterStart
    ) {
        val isPlaying by mediaView.innerMediaView!!.isVideoPlayingState().collectAsState()

        PlayPauseButton(
            modifier = Modifier
                .align(Alignment.TopCenter),
            offsetX = 0,
            offsetY = -40,
            isPlaying,
        ) {
            mediaView.onClickPlayPauseFromInstruments(it)
        }

        ChangeVolumeContent(mediaView)
    }
}

@Composable
private fun PlayPauseButton(
    modifier: Modifier = Modifier,
    offsetX: Int = 0,
    offsetY: Int = 0,
    isPlaying: Boolean = false,
    onPlayingStateChange: (isPlaying: Boolean) -> Unit = {}
) {
    Image(
        painterResource(if (isPlaying) app.inspiry.R.drawable.ic_stop_trim_page else app.inspiry.R.drawable.ic_play_trim_page),
        contentDescription = "play-pause", contentScale = ContentScale.Inside,
        modifier = modifier
            .offset(y = offsetY.dp, x = offsetX.dp)
            .size(LocalDimens.current.trimBgPlayPauseSize.dp)
            .clip(
                RoundedCornerShape(
                    LocalDimens.current.trimImageSequenceCornerRadius.dp
                )
            )
            .alpha(0.8f)
            .clickable {
                onPlayingStateChange(!isPlaying)
            }
            .background(
                LocalColors.current.littleElementBg.toCColor()
            )
    )
}

@Composable
private fun ChangeVolumeContent(mediaView: InspMediaView) {

    val volumeState: MutableStateFlow<Float> = mediaView.getVideoVolumeConsiderDuplicate() ?: MutableStateFlow(0f)
    val volume by volumeState.collectAsState()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = LocalDimens.current.volumeSliderPaddingSide.dp,
                end = LocalDimens.current.volumeSliderPaddingSide.dp
            )

            .height(LocalDimens.current.volumeSliderHeight.dp)
    ) {

        val noSoundColor = if (volume == 0f) LocalColors.current.selectedPageItem.toCColor()
        else LocalColors.current.unselectedPageItem.toCColor()

        Image(
            painterResource(app.inspiry.music.android.R.drawable.ic_sound_off_wave_from_dialog),
            contentDescription = "disable sound",
            modifier = Modifier
                .padding(end = 7.dp)
                .width(34.dp)
                .fillMaxHeight()
                .background(
                    LocalColors.current.littleElementBg.toCColor(),
                    RoundedCornerShape(LocalDimens.current.volumeSliderBgRounding.dp)
                )
                .clickable {
                    volumeState.value = 0f
                },

            contentScale = ContentScale.Inside,
            colorFilter = ColorFilter.tint(noSoundColor)
        )

        Row(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth()
                .background(
                    LocalColors.current.littleElementBg.toCColor(),
                    RoundedCornerShape(LocalDimens.current.volumeSliderBgRounding.dp)
                )
        ) {

            val stateSlider by volumeState.collectAsState()

            SliderValueHorizontal(
                stateSlider, { volumeState.value = it },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                thumbHeightMax = true,
                track = { p1, p2, p3, p4, p5 ->
                    DefaultTrack(
                        p1, p2, p3, p4, p5,
                        height = 4.dp,
                        colorTrack = Color(0xff828282),
                        colorProgress = Color.White
                    )
                },
                thumb = { modifier, p2, interactionSource, p4, p5 ->

                    SliderThumb2Layers(
                        modifier,
                        DpSize(11.dp, 11.dp),
                        Color(0xff828282).copy(alpha = 0.6f),
                        Color.White,
                        interactionSource
                    )
                },
                thumbSizeInDp = DpSize(19.dp, 19.dp)
            )

            val soundColor =
                if (volume != 0f) LocalColors.current.selectedPageItem.toCColor()
                else LocalColors.current.unselectedPageItem.toCColor()

            Image(painterResource(app.inspiry.music.android.R.drawable.ic_sound_on_wave_from_dialog),
                contentDescription = "sound on", contentScale = ContentScale.Inside,
                colorFilter = ColorFilter.tint(soundColor),
                modifier = Modifier
                    .fillMaxHeight()
                    .clickable {
                        volumeState.value = 1f
                    }
                    .padding(end = 10.dp)
            )
        }

    }
}
@Composable
fun TrimVideoPage(model: VideoEditViewModel) {
    CompositionLocalProvider(LocalGetSeriesOfImagesFunc provides getMediaRetrieverSeriesOfImagesFunc()) {
        PageTrimVideo(model = model)
    }
}

@Composable
private fun PageTrimVideo(model: VideoEditViewModel) {

    val mediaView by model.currentView.collectAsState()

    val isPlaying by mediaView.innerMediaView!!.isVideoPlayingState().collectAsState()

    DisposableEffect(key1 = mediaView) {
        mediaView.innerMediaView?.setVideoPositionIgnoreViewTiming()

        onDispose {
            if (mediaView.templateParent.templateMode == TemplateMode.EDIT) { //fix when user press preview when playing video
                mediaView.innerMediaView?.pauseVideoIfExists()
                mediaView.innerMediaView?.drawVideoFrameAsync(mediaView.currentFrame, false)
            }
        }
    }
    if (isPlaying) {
        LaunchedEffect(key1 = isPlaying) {
            while (isActive) {
                mediaView.innerMediaView?.updateVideoCurrentTimeNoViewTimingMode()
                delay(10L)
            }
        }
    }

    val currentTimeMs by mediaView.innerMediaView!!.videoCurrentTimeMs().collectAsState()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(LocalDimens.current.contentPanelHeight.dp)
            .background(LocalColors.current.topPanelBackground.toCColor())
            .clickable(false) {}
    ) {

        PlayPauseButton(
            modifier = Modifier
                .align(Alignment.TopCenter),
            offsetX = 0,
            offsetY = -40,
            isPlaying,
        ) {
            mediaView.onClickPlayPauseFromInstruments(it)
        }

        Text(
            text = TrackUtils.convertTimeToString(currentTimeMs),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 6.dp, end = 20.dp)
                .background(
                    LocalColors.current.littleElementBg.toCColor(),
                    RoundedCornerShape(LocalDimens.current.trimTextIndicatorBgCornerRadius.dp)
                )
                .padding(horizontal = 5.dp, vertical = 2.dp),
            fontSize = LocalDimens.current.labelTextSize.sp,
            maxLines = 1,
            color = LocalColors.current.trimTextCurrentTime.toCColor()
        )

        TrimmingPanel(Modifier.align(Alignment.BottomCenter), mediaView, currentTimeMs)
    }
}

@Composable
private fun TrimmingPanel(
    modifier: Modifier,
    mediaView: InspMediaView,
    currentTimeMs: Long
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(LocalDimens.current.trimDragBoxSize.dp)
            .padding(
                start = LocalDimens.current.trimContentPaddingSize.dp,
                end = LocalDimens.current.trimContentPaddingSize.dp,
                bottom = LocalDimens.current.BottomTrimDragBoxSize.dp
            )
    ) {

        val videoUri = mediaView.media.originalSource ?: mediaView.media.demoSource
        ?: throw IllegalStateException()

        val viewDuration = mediaView.getDurationForTrimmingMillis()
        val videoDuration = mediaView.getVideoDurationMs()
        val videoStartTime by mediaView.media.videoStartTimeMs?.collectAsState()
            ?: remember { mutableStateOf(0) }

        TrimmingSequenceOfImages(videoUri)

        TrimmingTopElements(
            videoDuration,
            viewDuration,
            videoStartTime,
            currentTimeMs
        ) {
            mediaView.media.videoStartTimeMs?.value = it
        }
    }
}

@Composable
fun dpToPx(value: Int) = LocalDensity.current.run { value.dp.toPx() }

private const val PREVIEW_BG = 0xff202020
@Preview(backgroundColor = PREVIEW_BG)
@Composable
private fun TrimmingTopElements(
    videoDurationMs: Long = 6000L,
    viewDurationMs: Int = 2000,
    videoStartOffsetMs: Int = 1000,
    currentTimeMs: Long = 1000L,
    videoStartOffsetChanged: (Int) -> Unit = {}
) {

    val videoDurationFloat = videoDurationMs.toFloat()
    val currentTimeLinePositionPercent =
        kotlin.math.max(currentTimeMs - videoStartOffsetMs, 0) / viewDurationMs.toFloat()
    val viewDurationPercent = kotlin.math.max(0.05f, viewDurationMs / videoDurationFloat)

    fun Float.clipVideoStartOffsetPercent(): Float {
        return kotlin.math.max(0f, kotlin.math.min(this, 1f - viewDurationPercent))
    }

    val videoStartOffsetPercent =
        (videoStartOffsetMs / videoDurationFloat).clipVideoStartOffsetPercent()

    val lineColor = LocalColors.current.trimCurrentTimeLineColor
    val lineWidth =
        LocalDensity.current.run { LocalDimens.current.trimCurrentTimeLineWidth.dp.toPx() }

    val viewDurationCornerRadius = dpToPx(LocalDimens.current.trimTextIndicatorBgCornerRadius)

    val whiteBorderVerticalThickness = dpToPx(LocalDimens.current.trimWhiteBorderThicknessVertical)
    val whiteBorderHorizontalThickness =
        dpToPx(LocalDimens.current.trimWhiteBorderThicknessHorizontal)
    val whiteBorderCornerRadius = dpToPx(LocalDimens.current.trimImageSequenceCornerRadius)

    val trY = LocalDensity.current.run { -6.dp.toPx() }
    val context = LocalContext.current

    val arrowBitmap = remember {
        BitmapFactory.decodeResource(context.resources, R.drawable.ic_arrow_trim).asImageBitmap()
    }

    val leftArrowOffsetX = dpToPx(1)
    val leftArrowOffsetY = dpToPx(-1)
    val rightArrowOffsetY = dpToPx(4)


    var boxSize by remember {
        mutableStateOf<IntSize?>(null)
    }

    Box(modifier = Modifier
        .fillMaxSize()
        .onGloballyPositioned {
            boxSize = it.size
        }) {

        val dragState = rememberDraggableState { delta ->
            boxSize?.let {
                val newVideoOffsetPercent =
                    (videoStartOffsetPercent + delta / it.width).clipVideoStartOffsetPercent()
                videoStartOffsetChanged((newVideoOffsetPercent * videoDurationMs).roundToInt())
            }
        }
        val additionalLineOffsetPx = dpToPx(4)

        Canvas(
            Modifier
                .fillMaxHeight()
                .fillMaxWidth(fraction = viewDurationPercent)
                .graphicsLayer {
                    translationY = trY
                    translationX = videoStartOffsetPercent * (boxSize?.width ?: 0)

                    //stupid hack to enable masks
                    alpha = 0.99f
                }
                .draggable(dragState, orientation = Orientation.Horizontal)

        ) {

            val sequenceWidth = size.width

            val videoStartX = 0f//videoStartOffsetPercent * sequenceWidth
            val viewDurationPx = sequenceWidth//viewDurationPercent * sequenceWidth

            drawRoundRect(
                color = Color.White, topLeft = Offset(videoStartX, 0f),
                size = Size(viewDurationPx, size.height),
                cornerRadius = CornerRadius(whiteBorderCornerRadius, whiteBorderCornerRadius)
            )

            drawRoundRect(
                color = Color.Black,
                topLeft = Offset(
                    videoStartX + whiteBorderHorizontalThickness,
                    whiteBorderVerticalThickness
                ),
                size = Size(
                    viewDurationPx - whiteBorderHorizontalThickness * 2,
                    size.height - whiteBorderVerticalThickness * 2
                ),
                cornerRadius = CornerRadius(viewDurationCornerRadius, viewDurationCornerRadius)
            )

            drawRoundRect(
                color = Color.White,
                topLeft = Offset(
                    videoStartX + whiteBorderHorizontalThickness + lineWidth,
                    whiteBorderVerticalThickness + lineWidth
                ),
                size = Size(
                    viewDurationPx - whiteBorderHorizontalThickness * 2 - lineWidth * 2,
                    size.height - whiteBorderVerticalThickness * 2 - lineWidth * 2
                ),
                cornerRadius = CornerRadius(viewDurationCornerRadius, viewDurationCornerRadius),
                blendMode = BlendMode.Xor
            )

            drawImage(
                arrowBitmap, topLeft = Offset(
                    videoStartX + leftArrowOffsetX,
                    size.height / 2f - arrowBitmap.height / 2f + leftArrowOffsetY
                )
            )

            val endArrowOffset = Offset(
                videoStartX + viewDurationPx - leftArrowOffsetX,
                size.height / 2f + rightArrowOffsetY
            )

            rotate(180f, endArrowOffset) {
                drawImage(arrowBitmap, topLeft = endArrowOffset)
            }
        }

        // it can be in one canvas easily.
        Canvas(
            Modifier
                .height(57.dp)
                .fillMaxWidth(fraction = viewDurationPercent)
                .graphicsLayer {
                    translationY = trY
                    translationX = videoStartOffsetPercent * (boxSize?.width ?: 0)
                }

        ) {

            val realSequenceWidth = size.width - whiteBorderHorizontalThickness * 2 - lineWidth * 2
            val lineX =
                (realSequenceWidth * currentTimeLinePositionPercent) + whiteBorderHorizontalThickness + lineWidth / 2

            drawRoundRect(
                lineColor.toCColor(), Offset(lineX, -additionalLineOffsetPx),
                size = Size(lineWidth, size.height + additionalLineOffsetPx * 2),
                cornerRadius = CornerRadius(lineWidth, lineWidth)
            )
        }
    }
}

data class SequenceOfImagesState(val url: String, val needBitmaps: Int)

private fun getMediaRetrieverSeriesOfImagesFunc() =
    { context: Context, videoUri: String, numThumbs: Int, thumbSize: Int ->
        BitmapUtils.getSeriesOfBitmaps(context, Uri.parse(videoUri), numThumbs, thumbSize)
            .map { it?.asImageBitmap() }
    }

private fun getPreviewSeriesOfImagesFunc() =
    { context: Context, videoUri: String, numThumbs: Int, thumbSize: Int ->
        flow<ImageBitmap?> {
            for (i in 0 until numThumbs) {
                emit(ImageBitmap(width = thumbSize, height = thumbSize))
            }
        }
    }

private val LocalGetSeriesOfImagesFunc = compositionLocalOf {
    getPreviewSeriesOfImagesFunc()
}

@Composable
private fun TrimmingSequenceOfImages(videoUrl: String) {

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(LocalDimens.current.trimImageSequenceSize.dp)
            .clip(RoundedCornerShape(LocalDimens.current.trimImageSequenceCornerRadius.dp))
            .background(LocalColors.current.littleElementBg.toCColor())
    ) {

        val bitmaps = remember {
            mutableStateListOf<ImageBitmap?>()
        }

        var innerState by remember {
            mutableStateOf(SequenceOfImagesState(videoUrl, 0))
        }

        val scope = rememberCoroutineScope {
            Dispatchers.Main
        }

        var launchedSeriesOfBitmaps by remember {
            mutableStateOf<Job?>(null)
        }

        val context = LocalContext.current
        val getBitmapsFunc = LocalGetSeriesOfImagesFunc.current

        //bitmaps are square sized.
        Canvas(modifier = Modifier
            .fillMaxSize()
            .onGloballyPositioned {

                val width = it.size.width
                val height = it.size.height

                val needBitmapsLocal = ceil(width / height.toFloat()).toInt()

                val newState = SequenceOfImagesState(videoUrl, needBitmapsLocal)

                if (innerState != newState) {
                    innerState = newState
                    bitmaps.clear()
                    launchedSeriesOfBitmaps?.cancel()
                    launchedSeriesOfBitmaps = scope.launch {

                        getBitmapsFunc
                            .invoke(context, videoUrl, needBitmapsLocal, height)
                            .flowOn(Dispatchers.IO)
                            .catch { e -> e.printDebug() }
                            .collect {
                                bitmaps.add(it)
                            }
                    }
                }

            }) {

            var currentOffsetX = 0f
            for (b in bitmaps) {

                if (b != null) {
                    drawImage(image = b, Offset(currentOffsetX, 0f))
                }

                currentOffsetX += size.height
            }
        }
    }
}