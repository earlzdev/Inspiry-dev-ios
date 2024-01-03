package app.inspiry.preview

import android.app.Activity
import android.widget.ImageView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import app.inspiry.core.data.FRAME_IN_MILLIS
import app.inspiry.core.util.doOnce
import app.inspiry.preview.ui.PreviewColors
import app.inspiry.preview.ui.PreviewColorsLight
import app.inspiry.preview.ui.PreviewDimens
import app.inspiry.preview.ui.PreviewDimensPhone
import app.inspiry.preview.viewmodel.PreviewViewModel
import app.inspiry.projectutils.R
import app.inspiry.utilities.toCColor
import app.inspiry.utils.dpToPixels
import app.inspiry.views.template.InspTemplateView
import com.russhwolf.settings.Settings
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.max

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PreviewScreen(
    modifier: Modifier,
    viewModel: PreviewViewModel,
    settings: Settings
) {
    val activity = (LocalContext.current as? Activity)
    val context = LocalContext.current
    val colors = LocalColors.current
    val dimens = LocalDimens.current

    val isWatermarkVisible by viewModel.isWaterMarkVisible.collectAsState()
    val isIGLayoutVisible by viewModel.isIGLayoutVisibleState.collectAsState()
    val templateView = viewModel.templateView
    val snackState = remember { SnackbarHostState() }
    val snackScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        settings.doOnce("snackOnPreview") {
            snackScope.launch {
                snackState.showSnackbar(
                    context.getString(R.string.preview_long_press_hint)
                )
            }
        }
    }

    Box(
        modifier = modifier
            .clickable(false) {}
            .background(Color.Transparent)
            .fillMaxSize(), contentAlignment = Alignment.Center


    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(templateView.template.format.aspectRatio(), false)
        ) {
            if (isWatermarkVisible) InspiryWaterMarkColumn()
        }

        ShadowTop()

        AnimatedVisibility(
            visible = isIGLayoutVisible,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            IGLayout()
        }

        LinearProgress(
            color = Color.White,
            backGroundColor = colors.progressBackground.toCColor(),
            templateView
        )

        Box(
            Modifier
                .fillMaxSize()
                .combinedClickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = {},
                    onLongClick = { viewModel.onTemplateLongClick() }
                )
        )

        Box(
            Modifier
                .align(Alignment.TopEnd)
                .padding(
                    top = dimens.closeIconTopPadding.dp,
                    end = dimens.closeIconEndPadding.dp
                )
                .clip(CircleShape)
                .clickable { activity?.onBackPressed() }
                .padding(dimens.closeIconClickablePadding.dp)
                .wrapContentSize()
        ) {
            Image(
                painter = painterResource(id = app.inspiry.R.drawable.ic_close_preview),
                contentDescription = null
            )
        }
        SnackbarHost(hostState = snackState, Modifier.align(Alignment.BottomCenter))
    }
}

@Composable
fun BoxScope.InspiryWaterMarkColumn() {
    val scrollScope = rememberCoroutineScope()
    val lazyListState = rememberLazyListState()
    val scrollJob = remember { mutableStateOf<Job?>(null) }
    val owner = LocalLifecycleOwner.current
    val dimens = LocalDimens.current
    val colors = LocalColors.current
    var lastTimeScrolledBy = 0L

    DisposableEffect(owner) {
        val observer = LifecycleEventObserver { source, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    scrollJob.value = scrollScope.launch {
                        lazyListState.scrollToItem(Int.MAX_VALUE)
                        while (isActive) {
                            lazyListState.scrollBy(-dimens.waterMarkInspiryScrollStep.dpToPixels())
                            lastTimeScrolledBy = System.currentTimeMillis()
                            delay(
                                max(
                                    FRAME_IN_MILLIS.toLong() - (System.currentTimeMillis() - lastTimeScrolledBy),
                                    10
                                )
                            )
                        }
                    }
                }
                Lifecycle.Event.ON_PAUSE -> {
                    scrollJob.value?.cancel()
                }
                else -> {}
            }
        }
        owner.lifecycle.addObserver(observer)
        onDispose {
            owner.lifecycle.removeObserver(observer)
        }
    }

    LazyColumn(
        modifier = Modifier
            .align(Alignment.CenterStart)
            .padding(start = dimens.waterMarkInspiryStartPadding.dp),
        state = lazyListState
    ) {
        items(Int.MAX_VALUE) {
            InspiryItem(dimens, colors)
        }
    }

}

@Composable
private fun InspiryItem(dimens: PreviewDimens, colors: PreviewColors) {
    val fontFamily = FontFamily(Font(R.font.mont_bold))
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .padding(bottom = dimens.waterMarkInspiryItemBottomPadding.dp)
            .height(100.dp)
            .width(100.dp)
            .rotate(-90f)
            .alpha(dimens.waterMarkInspiryTextOpacity)
    ) {
        Text(
            modifier = Modifier.align(Alignment.TopEnd),
            text = context.getString(app.inspiry.R.string.app_name).uppercase(),
            fontSize = dimens.waterMarkInspiryText.sp,
            style = TextStyle(fontFamily = fontFamily),
            fontWeight = FontWeight.Bold,
            color = colors.textWaterMark.toCColor()
        )
    }
}

@Composable
private fun IGLayout() {
    Box(modifier = Modifier.fillMaxSize()) {
        ShadowBottom()
        IGTop()
        IGBottom()
    }
}

@Composable
private fun BoxScope.IGTop() {
    val colors = LocalColors.current
    val dimens = LocalDimens.current

    Row(
        modifier = Modifier
            .padding(
                start = dimens.IGTopLayoutStartPadding.dp,
                top = dimens.IGTopLayoutTopPadding.dp
            )
            .align(Alignment.TopStart),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(dimens.storiesIconSize.dp)
                .height(dimens.storiesIconSize.dp)
                .background(colors.storiesIconCircle.toCColor(), CircleShape)
                .border(dimens.defaultBorderWidth.dp, Color.White, CircleShape)
        )
        Text(
            modifier = Modifier.padding(start = dimens.storiesProfileStartPadding.dp),
            text = stringResource(id = R.string.preview_template_stories_profile),
            fontSize = dimens.storiesProfileText.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

    }
}

@Composable
private fun BoxScope.IGBottom() {
    val dimens = LocalDimens.current

    Row(
        modifier = Modifier
            .padding(
                start = dimens.IGBottomLayoutStartPadding.dp,
                end = dimens.IGBottomLayoutEndPadding.dp,
                bottom = dimens.IGBottomLayoutBottomPadding.dp
            )
            .align(Alignment.BottomCenter),
        verticalAlignment = Alignment.Bottom
    ) {
        Image(
            modifier = Modifier
                .width(dimens.storiesPhotoSize.dp)
                .height(dimens.storiesPhotoSize.dp),
            painter = painterResource(app.inspiry.R.drawable.ic_photo_stories),
            contentDescription = null
        )
        Box(
            modifier = Modifier
                .padding(
                    start = dimens.storiesCommentsStartPadding.dp,
                    end = dimens.storiesCommentsEndPadding.dp
                )
                .weight(1f)
                .height(dimens.storiesCommentsHeight.dp)
                .border(
                    width = dimens.defaultBorderWidth.dp,
                    color = Color.White,
                    shape = RoundedCornerShape(dimens.storiesCommentsCorners.dp)
                )
        ) {
            Image(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = dimens.storiesContextEndPadding.dp),
                painter = painterResource(app.inspiry.R.drawable.ic_context_stories),
                contentDescription = null
            )

        }
        Image(
            modifier = Modifier.padding(bottom = dimens.storiesDirectBottomPadding.dp),
            painter = painterResource(app.inspiry.R.drawable.ic_direct_stories),
            contentDescription = null
        )
    }
}

@Composable
private fun BoxScope.ShadowTop() {
    val dimens = LocalDimens.current

    AndroidView(
        modifier = Modifier
            .align(Alignment.TopCenter)
            .height(dimens.shadowHeight.dp)
            .fillMaxWidth(),
        factory = { context ->
            val shadow = ImageView(context)
            shadow.setImageResource(app.inspiry.R.drawable.preview_top_shadow)

            shadow
        })
}

@Composable
private fun BoxScope.ShadowBottom() {
    val dimens = LocalDimens.current

    AndroidView(
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .height(dimens.shadowHeight.dp)
            .fillMaxWidth(),
        factory = { context ->
            val shadow = ImageView(context)
            shadow.setImageResource(app.inspiry.R.drawable.preview_top_shadow)
            shadow.rotation = 180f

            shadow
        })
}

@Composable
private fun BoxScope.LinearProgress(
    color: Color,
    backGroundColor: Color,
    templateView: InspTemplateView
) {
    val dimens = LocalDimens.current

    val progressScope = rememberCoroutineScope()
    val progressJob = remember { mutableStateOf<Job?>(null) }
    val progress = remember { mutableStateOf(0f) }
    val owner = LocalLifecycleOwner.current

    DisposableEffect(owner) {
        val observer = LifecycleEventObserver { source, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    progressJob.value = progressScope.launch {
                        val duration: Int =
                            if (templateView.getDuration() <= 0)
                                (8000 / FRAME_IN_MILLIS).toInt()
                            else
                                templateView.getDuration()

                        while (isActive) {
                            progress.value =
                                (templateView.currentFrame.toFloat() / duration)
                            delay(20)
                        }
                    }
                }
                Lifecycle.Event.ON_PAUSE -> {
                    progressJob.value?.cancel()
                }
                else -> {}
            }
        }
        owner.lifecycle.addObserver(observer)
        onDispose {
            owner.lifecycle.removeObserver(observer)
        }
    }

    Canvas(
        modifier = Modifier
            .align(Alignment.TopCenter)
            .padding(
                top = dimens.progressTopPadding.dp,
                start = dimens.progressStartEndPadding.dp,
                end = dimens.progressStartEndPadding.dp
            )
            .height(dimens.progressHeight.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(percent = 100))
    ) {
        val strokeWidth = size.height
        drawLineBackground(backGroundColor, strokeWidth)
        drawLine(progress.value, color, strokeWidth)
    }
}

private fun DrawScope.drawLineBackground(
    color: Color,
    strokeWidth: Float
) = drawLine(1f, color, strokeWidth)

private fun DrawScope.drawLine(
    endFraction: Float,
    color: Color,
    strokeWidth: Float
) {
    val width = size.width
    val height = size.height

    val yOffset = height / 2

    val barEnd = endFraction * width

    drawLine(color, Offset(0f, yOffset), Offset(barEnd, yOffset), strokeWidth)
}

private val LocalColors = compositionLocalOf<PreviewColors> { PreviewColorsLight() }
private val LocalDimens = compositionLocalOf<PreviewDimens> { PreviewDimensPhone() }