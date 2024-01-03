package app.inspiry.export.mainui

import android.os.Build
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import app.inspiry.MR
import app.inspiry.R
import app.inspiry.core.manager.INSTAGRAM_DISPLAY_NAME
import app.inspiry.edit.ui.EditColors
import app.inspiry.edit.ui.EditColorsLight
import app.inspiry.edit.ui.EditDimens
import app.inspiry.edit.ui.EditDimensPhone
import app.inspiry.export.*
import app.inspiry.export.dialog.ExportDialogMainUI
import app.inspiry.export.dialog.ExportDialogViewModel
import app.inspiry.export.dialog.imageUri
import app.inspiry.export.viewmodel.ExportViewModel
import app.inspiry.export.viewmodel.RecordViewModel
import app.inspiry.export.viewmodel.whereToExport
import app.inspiry.utilities.toCColor
import app.inspiry.utils.findResIdByName
import app.inspiry.utils.isAppInstalled
import coil.ImageLoader
import coil.compose.AsyncImage
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieComposition
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlin.math.min


@OptIn(ExperimentalMaterialApi::class)
@Composable
internal fun ExportMainUI(
    exportViewModel: ExportViewModel,
    recordViewModel: RecordViewModel,
    dialogViewModel: ExportDialogViewModel,
    commonViewModel: ExportCommonViewModel,
    imageLoader: ImageLoader,
    androidView: View
) {
    MaterialTheme(colors = MaterialTheme.colors.copy(isLight = true)) {

        val systemUiController = rememberSystemUiController()

        systemUiController.setNavigationBarColor(LocalColors.current.exportBottomPanelBg.toCColor())

        val sheetState = rememberModalBottomSheetState(ModalBottomSheetValue.Hidden)

        val coroutineScope = rememberCoroutineScope()

        val onBackPressDispatcher =
            LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher

        val callback = remember(coroutineScope) {
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    coroutineScope.launch {
                        sheetState.hide()
                    }
                }
            }
        }
        callback.isEnabled = sheetState.isVisible

        LaunchedEffect(onBackPressDispatcher) {
            onBackPressDispatcher?.addCallback(callback)
        }

        val state by recordViewModel.state.collectAsState()

        val transition = updateTransition(
            sheetState.targetValue == ModalBottomSheetValue.Hidden,
            "exportDialogTransition"
        )

        val dimColor = Color.Black.copy(
            alpha = if (Build.VERSION.SDK_INT >= 31) 0.5f else 0.7f
        )

        val statusBarOverlay by transition.animateColor(label = "ExportDialogDimStatusBar") {
            if (it) Color.Transparent else dimColor
        }

        val blurBehind by transition.animateDp(label = "ExportDialogBlurBehind") {
            if (it) 0.dp else 4.dp
        }

        systemUiController.setStatusBarColor(statusBarOverlay.compositeOver(Color.White))

        ModalBottomSheetLayout(
            modifier = Modifier.fillMaxSize(),
            sheetElevation = 0.dp,
            sheetState = sheetState,
            sheetContent = {
                ExportDialogMainUI(
                    modifier = Modifier.padding(top = 50.dp),
                    state.imageElseVideo,
                    dialogViewModel,
                    imageLoader
                ) {
                    exportViewModel.onClickExportInDialog(it)
                    coroutineScope.launch {
                        sheetState.hide()
                    }
                }
            },
            scrimColor = dimColor,
            sheetContentColor = Color.Transparent,
            sheetBackgroundColor = Color.Transparent
        ) {

            Column(
                modifier = Modifier
                    .blur(blurBehind)
                    .fillMaxSize()
                    .background(Color.White)

            ) {

                ExportTopBar(state) {
                    if (state is ExportState.Rendered) {
                        exportViewModel.backToStartActivity()
                    } else {
                        onBackPressDispatcher?.onBackPressed()
                    }
                }

                AndroidView(factory = {
                    androidView
                }, modifier = Modifier.fillMaxWidth().weight(1f))

                ExportActivityBottomPanelMainUI(
                    state,
                    recordViewModel,
                    exportViewModel,
                    dialogViewModel,
                    commonViewModel,
                    imageLoader,
                    onClickExportMore = {
                        coroutineScope.launch {
                            sheetState.show()
                        }
                    })
            }
        }
    }
}

@Composable
internal fun ExportTopBar(state: ExportState, onClickBack: () -> Unit) {

    val colors = LocalColors.current
    MaterialTheme(colors = MaterialTheme.colors.copy(isLight = colors.isLight)) {
        Row(
            modifier = Modifier
                .padding(horizontal = 18.dp)
                .padding(top = 10.dp)
                .height(40.dp)
                .clickable(onClick = onClickBack)
                .padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(9.dp)
        ) {

            Image(
                painterResource(R.drawable.ic_arrow_back_edit),
                contentDescription = null, contentScale = ContentScale.Inside
            )

            Text(
                stringResource(
                    if (state is ExportState.Rendered) app.inspiry.projectutils.R.string.create_new_story
                    else app.inspiry.projectutils.R.string.back
                ),
                color = LocalColors.current.topBarText.toCColor(),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun ExportActivityBottomPanelMainUI(
    state: ExportState,
    viewModel: RecordViewModel,
    viewModelActivity: ExportViewModel,
    dialogViewModel: ExportDialogViewModel,
    commonViewModel: ExportCommonViewModel,
    imageLoader: ImageLoader,
    onClickExportMore: () -> Unit
) {
    val colors = LocalColors.current
    MaterialTheme(colors = MaterialTheme.colors.copy(isLight = colors.isLight)) {
        Box(
            modifier = Modifier.fillMaxWidth()
                .animateContentSize(),
            contentAlignment = Alignment.Center
        ) {

            when (state) {
                is ExportState.Initial -> {
                    Initial(
                        viewModel,
                        viewModelActivity,
                        dialogViewModel,
                        imageLoader,
                        state,
                        onClickExportMore
                    )
                }
                is ExportState.UserPicked -> {
                    UndeterminedProgress()
                }
                is ExportState.RenderingInProcess -> {
                    val progress = state.progress
                    if (progress == null) {
                        UndeterminedProgress()
                    } else {
                        ExportProgress(progress)
                    }
                }
                is ExportState.Rendered -> {
                    Rendered(
                        viewModelActivity,
                        dialogViewModel,
                        commonViewModel,
                        imageLoader,
                        state,
                        onClickExportMore
                    )
                }
            }
        }
    }
}

@Composable
fun UndeterminedProgress() {
    Box(
        modifier = Modifier.fillMaxWidth()
            .height(LocalDimens.current.exportBottomPanelHeightProgress.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = LocalColors.current.exportProgressText.toCColor())
    }
}

@Composable
private fun ExportToChoices(
    state: ExportState,
    displayToGallery: Boolean,
    imageLoader: ImageLoader,
    viewModelActivity: ExportViewModel,
    dialogViewModel: ExportDialogViewModel,
    onClickExportMore: () -> Unit
) {

    Row(
        modifier = Modifier.height(LocalDimens.current.exportChoiceSingleHeight.dp)
            .fillMaxWidth(0.82f),
        horizontalArrangement = Arrangement.Center
    ) {

        var preloadedItemsMaxSize = if (displayToGallery) 2 else 3

        // 1. predefined apps like instagram or facebook
        val context = LocalContext.current
        val predefinedExportApps =
            remember { getDefaultPredefinedExportApps().filter { context.isAppInstalled(it.whereToExport.whereApp) } }

        for (index in 0 until (min(predefinedExportApps.size, preloadedItemsMaxSize))) {
            val app = predefinedExportApps[index]
            IconExportTo(app.name, context.findResIdByName(app.iconRes), imageLoader) {
                viewModelActivity.onClickExportToApp(app.whereToExport, state, fromDialog = false)
            }
        }
        preloadedItemsMaxSize -= predefinedExportApps.size

        // 2. last apps that user has
        if (preloadedItemsMaxSize > 0) {

            val flow = remember(state.imageElseVideo) {
                dialogViewModel.getPackageInfoState(state.imageElseVideo)
                    .map { it?.filter { item -> !predefinedExportApps.any { item.activityInfo.packageName == it.whereToExport.whereApp } } }
            }
            val loadedItems by flow.collectAsState(null)

            if (loadedItems != null) {
                for (index in 0 until (min(loadedItems!!.size, preloadedItemsMaxSize))) {
                    val app = loadedItems!![index]
                    val imageUri = app.imageUri
                    if (imageUri != null) {

                        val activityName = app.activityInfo.name
                        var appName by remember(activityName) {
                            mutableStateOf<String?>(null)
                        }
                        LaunchedEffect(activityName) {
                            dialogViewModel.getTextForItem(activityName) {
                                appName = it
                            }
                        }

                        IconExportTo(appName ?: "", imageUri, imageLoader) {
                            viewModelActivity.onClickExportToApp(
                                app.whereToExport, state, fromDialog = false
                            )
                        }
                    }
                }
            }
        }

        if (displayToGallery) {
            IconExportTo(
                stringResource(app.inspiry.projectutils.R.string.export_option_gallery),
                R.drawable.ic_export_gallery,
                imageLoader
            ) {
                viewModelActivity.onClickExportToApp(
                    whereToExportGallery,
                    state,
                    fromDialog = false
                )
            }
        }

        IconExportTo(
            stringResource(app.inspiry.projectutils.R.string.export_option_more),
            R.drawable.ic_export_more,
            imageLoader,
            onClick = onClickExportMore
        )
    }
}

@Composable
private fun SaveToGalleryLabel(onClick: () -> Unit) {

    val colors = LocalColors.current

    Row(
        modifier = Modifier.clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {

        val composition by rememberLottieComposition(LottieCompositionSpec.Asset(MR.assets.json.export_save_to_gallery.path))
        var visibleText by remember { mutableStateOf(false) }

        LottieAnimation(
            composition,
            isPlaying = visibleText,
            modifier = Modifier.size(28.dp),
            contentScale = ContentScale.Fit
        )

        AnimatedVisibility(
            visibleText,
            enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 })
        ) {

            Text(
                stringResource(app.inspiry.projectutils.R.string.share_save_to_gallery),
                color = colors.exportSaveToGalleryText.toCColor(),
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }

        LaunchedEffect(Unit) {
            delay(300L)
            visibleText = true
        }
    }
}

private const val COMPOSE_PREVIEW_BG = 0xffE6EAEB

@Composable
@Preview(backgroundColor = COMPOSE_PREVIEW_BG, showBackground = true)
private fun TagUs(onClick: () -> Unit = {}) {

    val colors = LocalColors.current
    val dimens = LocalDimens.current

    Column(
        modifier = Modifier.clickable(onClick = onClick).padding(horizontal = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                stringResource(app.inspiry.projectutils.R.string.saving_tag_us_inspiry1),
                color = colors.exportToAppText.toCColor(),
                fontSize = dimens.exportChoiceTextSize.sp,
                maxLines = 1, softWrap = false,
                modifier = Modifier.padding(end = dimens.exportTagUsBetweenPadding.dp)
            )

            Box(
                modifier = Modifier.background(
                    Brush.horizontalGradient(
                        listOf(
                            colors.exportProgressStart.toCColor(),
                            colors.exportProgressEnd.toCColor()
                        )
                    ),
                    RoundedCornerShape(dimens.exportTagUsInspiryBgCornerRadius.dp)

                ).padding(horizontal = 8.dp)
                    .padding(bottom = 2.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    INSTAGRAM_DISPLAY_NAME,
                    fontSize = dimens.exportTagUsInspiryText.sp,
                    color = colors.exportImageElseVideoSelectedText.toCColor(),
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Text(
            stringResource(app.inspiry.projectutils.R.string.saving_tag_us_inspiry2),
            color = colors.exportToAppText.toCColor(),
            fontSize = dimens.exportChoiceTextSize.sp,
            maxLines = 1, softWrap = false,
            modifier = Modifier
                .padding(horizontal = 4.dp)
                .padding(top = 3.dp, bottom = 3.dp)
        )
    }
}

@Composable
private fun IconExportTo(
    title: String,
    imageUri: Any,
    imageLoader: ImageLoader,
    onClick: () -> Unit
) {

    val dimens = LocalDimens.current

    Column(
        modifier = Modifier.height(dimens.exportChoiceSingleHeight.dp)
            .widthIn(min = 75.dp)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        AsyncImage(
            imageUri, contentDescription = null,
            modifier = Modifier
                .padding(
                    bottom = dimens.exportChoiceImagePaddingBottom.dp,
                    top = dimens.exportChoiceImagePaddingTop.dp
                )
                .padding(horizontal = dimens.exportChoiceImagePaddingHorizontal.dp)
                .size(dimens.exportChoiceImageSize.dp, dimens.exportChoiceImageSize.dp),
            contentScale = ContentScale.Fit,
            imageLoader = imageLoader
        )

        Text(
            title,
            maxLines = 2,
            color = LocalColors.current.exportToAppText.toCColor(),
            fontSize = dimens.exportChoiceTextSize.sp,
            fontWeight = FontWeight.Bold,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = dimens.exportChoiceTextPaddingHorizontal.dp)
                .widthIn(max = dimens.exportChoiceTextMaxWidth.dp)
        )
    }
}

@Composable
@Preview
private fun PickImageOrVideoPreview() {
    var imageElseVideo by remember { mutableStateOf(false) }
    PickImageOrVideo(imageElseVideo) { imageElseVideo = it }
}

@Composable
private fun PickImageOrVideo(imageElseVideo: Boolean, onChange: (Boolean) -> Unit) {
    val colors = LocalColors.current
    val dimens = LocalDimens.current

    val buttonWidth = dimens.exportImageElseVideoButtonWidth.dp

    val transition = updateTransition(imageElseVideo, "imageElseVideoTransition")
    val animDuration = 200

    val offsetBg by transition.animateDp(
        { TweenSpec(animDuration, easing = FastOutLinearInEasing) },
        label = "OffsetTransition"
    ) {
        if (it) buttonWidth else 0.dp
    }

    val leftTextColor by transition.animateColor(
        { TweenSpec(animDuration, easing = FastOutLinearInEasing) },
        label = "LeftTextColor"
    ) {
        if (it) colors.exportImageElseVideoSelectedBg.toCColor()
        else colors.exportImageElseVideoSelectedText.toCColor()
    }

    val rightTextColor by transition.animateColor(
        { TweenSpec(animDuration, easing = FastOutLinearInEasing) },
        label = "RightTextColor"
    ) {
        if (!it) colors.exportImageElseVideoSelectedBg.toCColor()
        else colors.exportImageElseVideoSelectedText.toCColor()
    }

    Box(
        modifier = Modifier.height(dimens.exportImageElseVideoHeight.dp)
            .width(dimens.exportImageElseVideoWidth.dp)
            .background(
                colors.exportImageElseVideoBg.toCColor(),
                RoundedCornerShape(dimens.exportImageElseVideoCornerRadius.dp)
            )
            .padding(
                horizontal = dimens.exportImageElseVideoOuterPaddingHorizontal.dp,
                vertical = dimens.exportImageElseVideoPaddingVertical.dp
            ),
        Alignment.CenterStart
    ) {

        val shape = RoundedCornerShape(dimens.exportImageElseVideoButtonCornerRadius.dp)

        Box(
            modifier = Modifier
                .graphicsLayer(translationX = LocalDensity.current.run { offsetBg.toPx() })
                .fillMaxHeight()
                .width(buttonWidth)
                .background(
                    colors.exportImageElseVideoSelectedBg.toCColor(),
                    shape
                )
        )

        ButtonExportAs(
            leftTextColor,
            stringResource(app.inspiry.projectutils.R.string.animation_enabled),
            shape,
            buttonWidth,
            Alignment.CenterStart
        ) {
            onChange(false)
        }
        ButtonExportAs(
            rightTextColor,
            stringResource(app.inspiry.projectutils.R.string.animation_disabled),
            shape,
            buttonWidth,
            Alignment.CenterEnd
        ) {
            onChange(true)
        }
    }
}

@Composable
private fun BoxScope.ButtonExportAs(
    color: Color,
    text: String,
    shape: Shape,
    width: Dp,
    alignment: Alignment,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    Box(
        Modifier
            .align(alignment)
            .fillMaxHeight()
            .width(width)
            .clip(shape)
            .clickable(
                onClick = onClick,
                interactionSource = interactionSource,
                indication = null
            ), contentAlignment = Alignment.Center
    ) {
        Text(
            text,
            color = color,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            softWrap = false
        )
    }
}

@Composable
private fun Rendered(
    viewModelActivity: ExportViewModel,
    dialogViewModel: ExportDialogViewModel,
    commonViewModel: ExportCommonViewModel,
    imageLoader: ImageLoader,
    state: ExportState.Rendered,
    onClickExportMore: () -> Unit
) {
    val colors = LocalColors.current
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(15.dp)
    ) {
        SaveToGalleryLabel { viewModelActivity.onClickExported(state.imageElseVideo) }

        Column(
            modifier = Modifier.fillMaxWidth()
                .background(colors.exportBottomPanelBg.toCColor(), getBgPanelShape())
                .padding(bottom = 15.dp, top = LocalDimens.current.exportChoiceInnerPaddingTop.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            ExportToChoices(
                state,
                displayToGallery = false,
                imageLoader,
                viewModelActivity,
                dialogViewModel,
                onClickExportMore
            )
            val context = LocalContext.current
            TagUs {
                commonViewModel.onClickInstInspiry { it.toString(context) }
            }
        }
    }
}

@Composable
private fun getBgPanelShape(): Shape {
    val round = LocalDimens.current.exportCornerRadius
    return RoundedCornerShape(topStart = round.dp, topEnd = round.dp)
}

@Composable
private fun Initial(
    viewModel: RecordViewModel,
    viewModelActivity: ExportViewModel,
    dialogViewModel: ExportDialogViewModel,
    imageLoader: ImageLoader,
    state: ExportState.Initial,
    onClickExportMore: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        PickImageOrVideo(state.imageElseVideo, viewModel::onChangeImageElseVideo)

        val dimens = LocalDimens.current
        Box(
            modifier = Modifier.fillMaxWidth()
                .padding(top = dimens.exportChoicePaddingTop.dp)
                .background(
                    LocalColors.current.exportBottomPanelBg.toCColor(),
                    getBgPanelShape()
                )
                .padding(
                    top = dimens.exportChoiceInnerPaddingTop.dp,
                    bottom = dimens.exportChoiceInnerPaddingBottom.dp
                ),
            contentAlignment = Alignment.Center
        ) {
            ExportToChoices(
                state,
                displayToGallery = true,
                imageLoader,
                viewModelActivity,
                dialogViewModel,
                onClickExportMore
            )
        }
    }
}

@Composable
@Preview
private fun ExportProgress(progress: Float = 0.66f) {
    val colors = LocalColors.current
    val dimens = LocalDimens.current
    Column(
        modifier = Modifier.fillMaxWidth()
            .height(dimens.exportBottomPanelHeightProgress.dp)
            .background(colors.exportBottomPanelBg.toCColor(), getBgPanelShape())
            .padding(horizontal = dimens.exportBottomPanelPaddingHorizontal.dp)
    ) {

        Row(
            modifier = Modifier.fillMaxWidth().padding(
                top = dimens.exportBottomPanelProgressPaddingTop.dp,
                bottom = dimens.exportBottomPanelProgressPaddingBetweenTextProgress.dp
            ),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Text(
                stringResource(app.inspiry.projectutils.R.string.saving_activity_progress_title),
                color = colors.exportProgressText.toCColor(),
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )

            Text(
                progressFloatToString(progress),
                color = colors.exportProgressText.toCColor(),
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Box(
            modifier = Modifier.fillMaxWidth().height(6.dp)
                .background(Color.White, shape = RoundedCornerShape(100)),
            contentAlignment = Alignment.CenterStart
        ) {
            Spacer(
                Modifier.padding(horizontal = 1.dp)
                    .fillMaxWidth(progress)
                    .height(4.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                colors.exportProgressStart.toCColor(),
                                colors.exportProgressEnd.toCColor()
                            )
                        ),
                        RoundedCornerShape(100)
                    )
            )
        }
    }
}

internal val LocalColors = compositionLocalOf<EditColors> { EditColorsLight() }
private val LocalDimens = compositionLocalOf<EditDimens> { EditDimensPhone() }

