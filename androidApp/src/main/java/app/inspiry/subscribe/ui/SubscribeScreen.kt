package app.inspiry.subscribe.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.TextureView
import android.view.ViewGroup
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.inspiry.BuildConfig
import app.inspiry.MR
import app.inspiry.R
import app.inspiry.ap
import app.inspiry.core.data.FRAME_IN_MILLIS
import app.inspiry.core.data.InspResponseData
import app.inspiry.core.data.InspResponseError
import app.inspiry.databinding.ItemSubscribeFeatureBinding
import app.inspiry.music.android.ui.ContentError
import app.inspiry.subscribe.*
import app.inspiry.subscribe.model.DisplayProduct
import app.inspiry.subscribe.model.DisplayProductPeriod
import app.inspiry.subscribe.viewmodel.SubscribeUiState
import app.inspiry.subscribe.viewmodel.SubscribeViewModelAndroid
import app.inspiry.subscribe.viewmodel.createOnClickSubscribeHandler
import app.inspiry.utilities.toCColor
import app.inspiry.utils.capitalized
import app.inspiry.utils.dpToPixels
import app.inspiry.utils.pixelsToDp
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.android.exoplayer2.C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.AssetDataSource
import com.google.android.exoplayer2.upstream.DataSource
import dev.icerock.moko.resources.AssetResource
import dev.icerock.moko.resources.desc.PluralFormatted
import dev.icerock.moko.resources.desc.StringDesc
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min

@Composable
fun SubscribeScreen(
    viewModel: SubscribeViewModelAndroid,
    onCloseClickListener: () -> Unit,
    activity: SubscribeActivity
) {
    val uiVersion by viewModel.uiVersionFlow.collectAsState()
    val systemUiController = rememberSystemUiController()

    when (uiVersion) {
        null -> SubscribeScreenProgress(Modifier.fillMaxSize())
        "1" -> SubscribeScreenA(viewModel, onCloseClickListener, activity)
        "2" -> SubscribeScreenB(viewModel, onCloseClickListener, activity)
        "3" -> SubscribeScreenC(viewModel, onCloseClickListener, activity)
        else -> SubscribeScreenD(viewModel, onCloseClickListener, activity)
    }

    SideEffect {
        systemUiController.isStatusBarVisible = false
    }
}

@Composable
private fun SubscribeScreenCommon(
    viewModel: SubscribeViewModelAndroid,
    Header: @Composable () -> Unit,
    SubscribeContent: @Composable ColumnScope.(SubscribeUiState) -> Unit
) {
    val state by viewModel.stateFlow.collectAsState()

    Column(modifier = Modifier
        .fillMaxSize()
        .clickable(enabled = false) {}) {

        Header()
        FeatureList()

        when (state) {
            is InspResponseData -> {
                SubscribeContent((state as InspResponseData<SubscribeUiState>).data)
            }
            is InspResponseError -> {
                ErrorScreen((state as InspResponseError<SubscribeUiState>).throwable, viewModel)
            }
            else -> {
                LoadingDataScreen()
            }
        }

        SubscribeConditions()
    }
}


@Composable
fun SubscribeScreenA(
    viewModel: SubscribeViewModelAndroid,
    onCloseClickListener: () -> Unit,
    activity: SubscribeActivity
) {
    SubscribeScreenCommon(
        viewModel = viewModel,
        Header = {
            HeaderA(onCloseClickListener)
        },
        SubscribeContent = {
            OptionsA(it, viewModel)
            TrialInfoText(it)
            SubscribeButtonA(it.selectedOptionTrialDays, viewModel, activity)
        }
    )
}

@Composable
fun SubscribeScreenB(
    viewModel: SubscribeViewModelAndroid,
    onCloseClickListener: () -> Unit,
    activity: SubscribeActivity
) {
    SubscribeScreenCommon(
        viewModel = viewModel,
        Header = {
            HeaderB(onCloseClickListener)
        },
        SubscribeContent = {
            OptionsB(it, viewModel)
            TrialInfoText(it)
            SubscribeButtonB(viewModel, activity)
        }
    )
}


@Composable
fun SubscribeScreenC(
    viewModel: SubscribeViewModelAndroid,
    onCloseClickListener: () -> Unit,
    activity: SubscribeActivity
) {
    SubscribeScreenCommon(
        viewModel = viewModel,
        Header = {
            HeaderB(onCloseClickListener)
        },
        SubscribeContent = {
            OptionsB(it, viewModel)
            TrialInfoText(it)
            SubscribeButtonB(viewModel, activity)
        }
    )
}


@Composable
fun SubscribeScreenD(
    viewModel: SubscribeViewModelAndroid,
    onCloseClickListener: () -> Unit,
    activity: SubscribeActivity
) {
    SubscribeScreenCommon(
        viewModel = viewModel,
        Header = {
            HeaderB(onCloseClickListener)
        },
        SubscribeContent = {
            OptionsB(it, viewModel)
            SubscribeButtonD(it.selectedOptionTrialDays, viewModel, activity)
        }
    )
}

@Composable
private fun ColumnScope.LoadingDataScreen() {
    SubscribeScreenProgress(
        modifier = Modifier
            .fillMaxWidth()
            .weight(1f)
    )
}

@Composable
private fun ColumnScope.ErrorScreen(error: Throwable, viewModel: SubscribeViewModelAndroid) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .weight(1f), contentAlignment = Alignment.Center
    ) {

        val colors = LocalColors.current
        ContentError(
            modifier = Modifier.fillMaxWidth(0.7f),
            error = error,
            buttonTextColor = Color.White,
            buttonBgColor = colors.gradient1Start.toCColor(),
            textInfoColor = Color.Black.copy(alpha = 0.9f),
            alwaysShowError = false,
            onClick = viewModel::loadDefaultProductsIfAbsent
        )
    }
}

@Composable
private fun SubscribeScreenProgress(modifier: Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = LocalColors.current.gradient1Start.toCColor())
    }
}

@Composable
private fun HeaderA(onCloseClickListener: () -> Unit) {
    val context: Context = LocalContext.current
    val dimens = LocalDimens.current
    val colors = LocalColors.current

    Box(
        modifier = Modifier
            .height(calculateHeaderHeight(context, dimens, isAVersion = true))
            .fillMaxWidth()
            .clip(
                RoundedCornerShape(
                    bottomStart = dimens.headerCorners.dp,
                    bottomEnd = dimens.headerCorners.dp
                )
            )
    ) {
        Video(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f), videoId = MR.assets.videos.subscribe
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .height(30.dp)
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            colors.headerGradientTop.toCColor(),
                            colors.headerGradientBottom.toCColor()
                        )
                    )
                )
        )
        Image(
            modifier = Modifier
                .padding(10.dp)
                .clip(CircleShape)
                .clickable { onCloseClickListener() }
                .padding(10.dp)
                .align(Alignment.TopStart),
            painter = painterResource(id = R.drawable.ic_subscribe_close),
            contentDescription = stringResource(id = app.inspiry.projectutils.R.string.back),
        )
        Column(
            modifier = Modifier.align(Alignment.BottomStart)
        ) {
            Text(
                modifier = Modifier.padding(
                    start = dimens.headerTextStartEndPadding.dp,
                    end = dimens.headerTextStartEndPadding.dp
                ),
                text = stringResource(id = app.inspiry.projectutils.R.string.banner_my_stories_title),
                fontSize = dimens.headerTextTitle.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                modifier = Modifier.padding(
                    bottom = dimens.headerTextBottomPadding.dp,
                    start = dimens.headerTextStartEndPadding.dp,
                    end = dimens.headerTextStartEndPadding.dp
                ),
                text = stringResource(id = app.inspiry.projectutils.R.string.subscribe_header_subtitle),
                fontSize = dimens.headerTextSubTitle.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
private fun HeaderB(onCloseClickListener: () -> Unit) {
    val context: Context = LocalContext.current
    val dimens = LocalDimens.current
    val colors = LocalColors.current
    val textStyle = TextStyle(
        shadow = Shadow(
            color = LocalColors.current.headerTextShadowColor.toCColor(),
            offset = Offset(4f, 4f),
            blurRadius = 8f
        )
    )

    Box(
        modifier = Modifier
            .height(calculateHeaderHeight(context, dimens, isAVersion = false))
            .fillMaxWidth()
            .clip(
                RoundedCornerShape(
                    bottomStart = dimens.headerCorners.dp,
                    bottomEnd = dimens.headerCorners.dp
                )
            )
    ) {
        Video(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f), videoId = MR.assets.videos.subscribe
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .height(30.dp)
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            colors.headerGradientTop.toCColor(),
                            colors.headerGradientBottom.toCColor()
                        )
                    )
                )
        )
        Image(
            modifier = Modifier
                .padding(10.dp)
                .clip(CircleShape)
                .clickable { onCloseClickListener() }
                .padding(10.dp)
                .align(Alignment.TopStart),
            painter = painterResource(id = R.drawable.ic_subscribe_close),
            contentDescription = stringResource(id = app.inspiry.projectutils.R.string.back),
        )
        Column(
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = dimens.headerTextStartEndPadding.dp,
                        end = dimens.headerTextStartEndPadding.dp
                    ),
                textAlign = TextAlign.Center,
                style = textStyle,
                text = stringResource(id = app.inspiry.projectutils.R.string.banner_my_stories_title).uppercase(),
                fontSize = dimens.headerTextTitle.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        bottom = dimens.headerTextBottomPadding.dp,
                        start = dimens.headerTextStartEndPadding.dp,
                        end = dimens.headerTextStartEndPadding.dp
                    ),
                textAlign = TextAlign.Center,
                style = textStyle,
                text = stringResource(id = app.inspiry.projectutils.R.string.subscribe_header_subtitle),
                fontSize = dimens.headerTextSubTitle.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
fun Video(modifier: Modifier = Modifier, videoId: AssetResource) {
    val context: Context = LocalContext.current
    val player: ExoPlayer = remember {
        exoPlayer(context, videoId)
    }

    AndroidView(
        factory = {
            val texture = TextureView(it)

            player.setVideoTextureView(texture)
            player.prepare()

            texture
        },
        modifier = modifier
    )
}

@Composable
private fun FeatureList() {
    val scrollFeaturesScope = rememberCoroutineScope()
    val dimens = LocalDimens.current

    AndroidView(
        factory = {
            val items = getFeatures()
            val rv = RecyclerView(it)
            var lastDx = 0
            val recyclerScrollStep = floor(0.7f.dpToPixels()).toInt()
            var scrollWithSign = recyclerScrollStep
            rv.layoutManager = LinearLayoutManager(it, LinearLayoutManager.HORIZONTAL, false)
            rv.adapter = FeatureAdapter(items, dimens)
            rv.scrollToPosition(items.size * (Integer.MAX_VALUE / 20))

            rv.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    lastDx = dx
                }

                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                        scrollWithSign = if (lastDx >= 0) {
                            recyclerScrollStep
                        } else {
                            -recyclerScrollStep
                        }
                    }
                }
            })

            scrollFeaturesScope.launch {
                var lastTimeScrolledBy = 0L
                while (isActive) {
                    if (rv.scrollState == RecyclerView.SCROLL_STATE_IDLE) {
                        rv.scrollBy(scrollWithSign, 0)
                        lastTimeScrolledBy = System.currentTimeMillis()
                    }
                    delay(
                        max(
                            FRAME_IN_MILLIS.toLong() - (System.currentTimeMillis() - lastTimeScrolledBy),
                            10
                        )
                    )
                }
            }

            rv
        },
        modifier = Modifier.padding(
            top = dimens.featuresListTopPadding.dp,
            bottom = dimens.featuresListBottomPadding.dp
        )
    )
}

@Composable
private fun ColumnScope.OptionsA(state: SubscribeUiState, viewModel: SubscribeViewModelAndroid) {
    Column(
        modifier = Modifier.weight(1f),
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        state.options.forEachIndexed { index, product ->
            OptionButtonA(
                state.selectedOptionPos == index,
                index,
                product,
                state,
                viewModel::onOptionSelected
            )
        }
    }
}

@Composable
private fun OptionButtonA(
    selected: Boolean,
    pos: Int,
    product: DisplayProduct,
    state: SubscribeUiState,
    onClick: (Int) -> Unit
) {
    val colors = LocalColors.current
    val dimens = LocalDimens.current

    val borderModifier = if (selected)
        Modifier.border(
            width = dimens.defaultBorderWidth.dp,
            brush = Brush.horizontalGradient(
                listOf(
                    colors.gradient1Start.toCColor(),
                    colors.gradient1End.toCColor()
                )
            ),
            RoundedCornerShape(dimens.optionACorners.dp)
        )
    else
        Modifier.border(
            width = 3.dp,
            color = colors.optionBorderColorA.toCColor(),
            RoundedCornerShape(dimens.optionACorners.dp)
        )

    val optionBgModifier = if (selected) Modifier.background(colors.optionBgActiveA.toCColor())
    else Modifier.background(colors.optionBgInactiveA.toCColor())

    val textColor = if (selected)
        colors.textOptionActiveA.toCColor()
    else
        colors.textOptionInactiveA.toCColor()


    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(dimens.optionAHeight.dp)
            .padding(
                start = dimens.optionStartEndPadding.dp,
                end = dimens.optionStartEndPadding.dp
            )
            .clip(RoundedCornerShape(dimens.optionACorners.dp))
            .then(borderModifier)
            .padding(3.dp)
            .border(
                width = dimens.defaultBorderWidth.dp,
                colors.optionBorderWhiteA.toCColor(),
                RoundedCornerShape(dimens.optionACorners.dp)
            )
            .then(optionBgModifier)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }) { onClick(pos) }
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (product.period == DisplayProductPeriod.YEAR && state.yearSaveAmount != null) {
                Box(
                    modifier = Modifier
                        .padding(start = 14.dp)
                        .weight(1f)
                ) {
                    Text(
                        modifier = Modifier.align(
                            Alignment.Center
                        ),
                        text = decodeOptionText(LocalContext.current, product),
                        fontSize = dimens.optionText.sp,
                        color = textColor
                    )
                }
                OptionLabelA(state.yearSaveAmount!!)
            } else {
                Text(
                    text = decodeOptionText(LocalContext.current, product),
                    fontSize = dimens.optionText.sp,
                    color = textColor
                )
            }
        }

    }
}

@Composable
private fun OptionLabelA(saveAmount: String) {
    val colors = LocalColors.current
    val dimens = LocalDimens.current

    Box(
        modifier = Modifier
            .height(dimens.optionALabelHeight.dp)
            .padding(end = 16.dp)
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(dimens.optionALabelCorners.dp),
                clip = true
            )
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        colors.gradient1Start.toCColor(),
                        colors.gradient1End.toCColor()
                    )
                )
            )
    ) {
        Text(
            text = stringResource(
                id = app.inspiry.projectutils.R.string.subscription_save,
                "$saveAmount%"
            ),
            modifier = Modifier
                .align(Alignment.Center)
                .padding(start = 16.dp, end = 16.dp, top = 2.dp, bottom = 2.dp),
            fontWeight = FontWeight.Medium,
            fontSize = dimens.optionALabelText.sp,
            color = Color.White
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SubscribeButtonCommon(
    text: String,
    viewModel: SubscribeViewModelAndroid,
    activity: SubscribeActivity
) {

    val colors = LocalColors.current
    val dimens = LocalDimens.current

    val infiniteTransition = rememberInfiniteTransition()
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        )
    )

    val onClickHandler = remember {
        createOnClickSubscribeHandler(activity)
    }

    val clickableModifier = if (BuildConfig.DEBUG) {
        Modifier.combinedClickable(
            onClick = { viewModel.onSubscribeClick(onClickHandler) },
            onLongClick = { viewModel.onDebugSubscribeLongClick() }
        )
    } else
        Modifier.clickable { viewModel.onSubscribeClick(onClickHandler) }


    Box(modifier = Modifier.graphicsLayer {
        scaleX = scale
        scaleY = scale
    }) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    top = dimens.subscribeButtonATopPadding.dp,
                    start = dimens.subscribeButtonAStartEndPadding.dp,
                    end = dimens.subscribeButtonAStartEndPadding.dp
                )
                .height(dimens.subscribeButtonHeight.dp)
                .clip(RoundedCornerShape(dimens.subscribeButtonCorners.dp))
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            colors.gradient1Start.toCColor(),
                            colors.gradient1End.toCColor()
                        )
                    )
                )
                .then(clickableModifier)
        ) {
            Text(
                text = text,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(bottom = 3.dp),
                fontSize = dimens.subscribeButtonText.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SubscribeButtonA(
    trialDays: Int,
    viewModel: SubscribeViewModelAndroid,
    activity: SubscribeActivity
) {

    val text =
        if (trialDays > 0) stringResource(id = app.inspiry.projectutils.R.string.subscribe_try_days_button)
        else stringResource(id = app.inspiry.projectutils.R.string.subscribe_continue_button)
    SubscribeButtonCommon(text = text, viewModel = viewModel, activity = activity)
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SubscribeButtonB(
    viewModel: SubscribeViewModelAndroid,
    activity: SubscribeActivity
) {
    SubscribeButtonCommon(
        text = stringResource(id = app.inspiry.projectutils.R.string.subscribe_continue_button)
            .lowercase().capitalized(), viewModel = viewModel, activity = activity
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SubscribeButtonD(
    trialDays: Int,
    viewModel: SubscribeViewModelAndroid,
    activity: SubscribeActivity
) {

    val text =
        if (trialDays > 0) stringResource(
            app.inspiry.projectutils.R.string.subscribe_try_days_b,
            getPluralTryDays(trialDays, LocalContext.current)
        )
        else stringResource(id = app.inspiry.projectutils.R.string.subscribe_continue_button)
    SubscribeButtonCommon(text = text, viewModel = viewModel, activity = activity)
}

@Composable
private fun ColumnScope.OptionsB(state: SubscribeUiState, viewModel: SubscribeViewModelAndroid) {
    Column(
        modifier = Modifier.weight(1f),
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        state.options.forEachIndexed { index, product ->
            OptionButtonB(
                state.selectedOptionPos == index,
                index,
                product,
                state,
                viewModel::onOptionSelected
            )
        }
    }
}

@Composable
private fun OptionButtonB(
    selected: Boolean,
    pos: Int,
    product: DisplayProduct,
    state: SubscribeUiState,
    onClick: (Int) -> Unit
) {
    val colors = LocalColors.current
    val dimens = LocalDimens.current

    val optionModifier = if (selected) Modifier
        .border(
            width = 1.dp,
            color = colors.optionBorderColorB.toCColor(),
            shape = RoundedCornerShape(dimens.optionBCorners.dp)
        )
        .background(
            color = colors.optionBgColorB.toCColor(),
            RoundedCornerShape(dimens.optionBCorners.dp)
        )
    else
        Modifier

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = dimens.optionStartEndPadding.dp,
                end = dimens.optionStartEndPadding.dp
            )
            .clip(RoundedCornerShape(dimens.optionBCorners.dp))
            .then(optionModifier)
            .clickable { onClick(pos) },
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {

        BrushRadioButton(
            dimens = dimens,
            modifier = Modifier.padding(start = 20.dp, end = 10.dp),
            selected = selected,
            dotBrush = Brush.horizontalGradient(
                listOf(
                    colors.gradient1Start.toCColor(),
                    colors.gradient1End.toCColor()
                )
            ),
            borderActiveColor = Color.White,
            borderInactiveColor = colors.radioButtonBorderInactiveColor.toCColor(),
            onClick = { onClick(pos) })


        Column(
            modifier = Modifier
                .height(dimens.optionBHeight.dp)
                .weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            if (product.period == DisplayProductPeriod.YEAR && state.yearPerMonthPrice != null) {
                Row {
                    Text(
                        text = stringResource(app.inspiry.projectutils.R.string.subscription_12_months),
                        fontSize = dimens.optionText.sp,
                        color = colors.textOptionDarkColorB.toCColor(),
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = " ${product.localizedPrice}",
                        fontSize = dimens.optionBPriceText.sp,
                        color = colors.textOptionColorB.toCColor()
                    )
                }
                Text(
                    modifier = Modifier.padding(top = 4.dp),
                    text = stringResource(
                        app.inspiry.projectutils.R.string.subscribe_price_per_month,
                        state.yearPerMonthPrice!!
                    ),
                    fontSize = dimens.optionBPriceText.sp,
                    color = colors.textOptionColorB.toCColor()
                )
            } else {
                Text(
                    text = decodeOptionText(LocalContext.current, product),
                    fontSize = dimens.optionText.sp,
                    color = colors.textOptionColorB.toCColor()
                )
            }
        }

        if (product.period == DisplayProductPeriod.YEAR && state.yearSaveAmount != null)
            OptionLabelB(selected, saveAmount = state.yearSaveAmount!!)
    }
}

@Composable
private fun OptionLabelB(selected: Boolean, saveAmount: String) {
    val colors = LocalColors.current
    val dimens = LocalDimens.current

    Column(
        modifier = Modifier
            .height(dimens.optionBLabelHeight.dp)
            .aspectRatio(1f)
            .padding(6.dp)
    ) {
        AnimatedVisibility(
            visible = selected,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                colors.gradient1Start.toCColor(),
                                colors.gradient1End.toCColor()
                            )
                        ),
                        RoundedCornerShape(dimens.optionBLabelCorners.dp)
                    ),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(
                        id = app.inspiry.projectutils.R.string.subscription_save,
                        ""
                    ).uppercase(),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = dimens.optionBLabelText.sp,
                    color = Color.White
                )
                Text(
                    text = "$saveAmount%",
                    fontSize = dimens.optionBLabelText.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun TrialInfoText(state: SubscribeUiState) {
    val colors = LocalColors.current
    val dimens = LocalDimens.current

    val selectedPeriodIsYear =
        state.options.getOrNull(state.selectedOptionPos)?.period == DisplayProductPeriod.YEAR &&
                state.selectedOptionTrialDays > 0

    val transition = updateTransition(
        selectedPeriodIsYear,
        "alphaTransition"
    )

    val alpha by transition.animateFloat(label = "AlphaPriceTransition8") {
        if (it) 1f else 0f
    }

    Row(
        modifier = Modifier
            .alpha(alpha)
            .fillMaxWidth()
            .padding(top = dimens.trialInfoTopPadding.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val priceForYear = state.options.firstOrNull { it.period == DisplayProductPeriod.YEAR }
        if (priceForYear != null) {
            Text(
                text = stringResource(
                    app.inspiry.projectutils.R.string.subscribe_try_days_b,
                    getPluralTryDays(state.selectedOptionTrialDays, LocalContext.current)
                ),
                color = colors.textOptionDarkColorB.toCColor(),
                fontWeight = FontWeight.Bold,
                fontSize = dimens.trialInfoText.sp
            )
            Text(
                text = stringResource(
                    app.inspiry.projectutils.R.string.subscribe_after_trial_end,
                    priceForYear.localizedPrice
                ),
                color = colors.textOptionColorB.toCColor(),
                fontSize = dimens.trialInfoText.sp
            )
        }
    }
}

@Composable
private fun SubscribeConditions() {
    val dimens = LocalDimens.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                top = dimens.subscribeConditionsTopPadding.dp,
                bottom = dimens.subscribeConditionsBottomPadding.dp
            )
    ) {
        Text(
            modifier = Modifier.align(Alignment.Center),
            text = stringResource(id = app.inspiry.projectutils.R.string.subscribe_conditions),
            fontSize = dimens.subscribeConditionsText.sp, color = Color.Black
        )
    }
}

private fun exoPlayer(context: Context, videoId: AssetResource): ExoPlayer {
    val player = ExoPlayer.Builder(context).build()
    player.repeatMode = ExoPlayer.REPEAT_MODE_ALL
    player.videoScalingMode = VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING
    val dataSourceFactory = DataSource.Factory { AssetDataSource(context) }

    val videoSource = ProgressiveMediaSource.Factory(dataSourceFactory)
        .createMediaSource(MediaItem.Builder().setUri(videoId.path).build())

    player.setMediaSource(videoSource)
    player.playWhenReady = true
    return player
}

private fun calculateHeaderHeight(
    context: Context,
    dimens: SubscribeDimens,
    isAVersion: Boolean
): Dp {
    val contentHeightPx =
        if (isAVersion) calculateContentAHeight(dimens) else calculateContentBHeight(dimens)
    val maxHeaderHeightPx = (context.resources.displayMetrics.widthPixels * 0.8f).toInt()
    val possibleHeaderHeightPx = (context.resources.displayMetrics.heightPixels - contentHeightPx)

    val headerHeightPx = min(
        possibleHeaderHeightPx,
        maxHeaderHeightPx
    )

    return headerHeightPx.pixelsToDp().dp
}

private fun calculateContentAHeight(dimens: SubscribeDimens): Int = with(dimens) {
    featuresListTopPadding + featuresListItemHeight + featuresListBottomPadding +
            3 * (optionATopPadding + optionAHeight) +
            subscribeButtonATopPadding + subscribeButtonHeight +
            subscribeConditionsTopPadding + subscribeConditionsHeight + subscribeConditionsBottomPadding
}.dpToPixels().toInt()

private fun calculateContentBHeight(dimens: SubscribeDimens): Int = with(dimens) {
    featuresListTopPadding + featuresListItemHeight + featuresListBottomPadding +
            2 * (optionBHeight + optionBTopPadding) +
            optionBWithLabelHeight + optionBTopPadding +
            trialInfoTopPadding + trialInfoHeight +
            subscribeButtonBTopPadding + subscribeButtonHeight +
            subscribeConditionsTopPadding + subscribeConditionsHeight + subscribeConditionsBottomPadding
}.dpToPixels().toInt()

private fun decodeOptionText(context: Context, productModel: DisplayProduct): String {
    val buttonText = when (productModel.period) {
        DisplayProductPeriod.MONTH -> app.inspiry.projectutils.R.string.subscription_1_month
        DisplayProductPeriod.YEAR -> app.inspiry.projectutils.R.string.subscription_1_year
        else -> app.inspiry.projectutils.R.string.subscribe_option_forever
    }

    return context.getString(buttonText, productModel.localizedPrice)
}

private class SubscribeFeature(val icon: Int, val text: Int)

private fun getFeatures(): List<SubscribeFeature> {
    return listOf(
        SubscribeFeature(
            R.drawable.feature_all_templates,
            app.inspiry.projectutils.R.string.subscribe_feature_all
        ),
        SubscribeFeature(
            R.drawable.feature_fonts,
            app.inspiry.projectutils.R.string.subscribe_feature_fonts
        ),
        SubscribeFeature(
            R.drawable.feature_watermark,
            app.inspiry.projectutils.R.string.subscribe_feature_watermark
        ),
        SubscribeFeature(
            R.drawable.feature_text_animations,
            app.inspiry.projectutils.R.string.subscribe_feature_text_animations
        ),
        SubscribeFeature(
            R.drawable.feature_formats,
            app.inspiry.projectutils.R.string.subscribe_feature_format
        ),
        SubscribeFeature(
            R.drawable.feature_remove_bg,
            app.inspiry.projectutils.R.string.remove_bg_promo_title
        )
    )
}

private class FeatureViewHolder(val item: ItemSubscribeFeatureBinding) :
    RecyclerView.ViewHolder(item.root)

private class FeatureAdapter(val items: List<SubscribeFeature>, val dimens: SubscribeDimens) :
    RecyclerView.Adapter<FeatureViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeatureViewHolder {
        val holder = FeatureViewHolder(
            ItemSubscribeFeatureBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

        val dimensHeight = dimens.featuresListItemHeight.dpToPixels().toInt()
        val botPadding = dimens.featuresListItemBottomPadding.dpToPixels().toInt()
        val startEndPadding = dimens.featuresListItemStartEndPadding.dpToPixels().toInt()

        holder.item.root.updateLayoutParams {
            height = dimensHeight
            width = dimensHeight
        }
        holder.item.textView.textSize = dimens.featuresListItemText
        holder.item.textView.updateLayoutParams<ViewGroup.MarginLayoutParams> {
            setMargins(startEndPadding, 0, startEndPadding, botPadding)
        }

        return holder
    }

    override fun onBindViewHolder(holder: FeatureViewHolder, position: Int) {
        val realPos = position % items.size

        val item = items[realPos]
        holder.item.imageIcon.setImageResource(item.icon)
        holder.item.textView.setText(item.text)
    }

    override fun getItemCount() = Integer.MAX_VALUE
}

@Composable
private fun BrushRadioButton(
    dimens: SubscribeDimens,
    modifier: Modifier = Modifier,
    selected: Boolean,
    dotBrush: Brush,
    borderActiveColor: Color,
    borderInactiveColor: Color,
    onClick: (() -> Unit)?
) {
    val dotRadius by animateDpAsState(
        targetValue = if (selected) dimens.radioButtonDotSize.dp / 2 else 0.dp,
        animationSpec = tween(durationMillis = 100)
    )
    val borderColor = if (selected) borderActiveColor else borderInactiveColor

    val selectableModifier =
        if (onClick != null) {
            Modifier.selectable(
                selected = selected,
                onClick = onClick,
                enabled = true,
                role = Role.RadioButton,
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            )
        } else {
            Modifier
        }

    Canvas(
        modifier
            .then(selectableModifier)
            .wrapContentSize(Alignment.Center)
            .padding(dimens.radioButtonPadding.dp)
            .requiredSize(dimens.radioButtonSize.dp)
    ) {
        drawRadio(dimens, dotBrush, borderColor, dotRadius)
    }

}

private fun DrawScope.drawRadio(
    dimens: SubscribeDimens,
    dotBrush: Brush,
    borderColor: Color,
    dotRadius: Dp
) {
    val strokeWidth = dimens.radioStrokeWidth.dp.toPx()
    drawCircle(
        borderColor,
        dimens.radioRadius.dp.toPx() - strokeWidth / 2,
        style = Stroke(strokeWidth)
    )
    if (dotRadius > 0.dp) {
        drawCircle(dotBrush, dotRadius.toPx() - strokeWidth / 2, style = Fill)
    }
}

private fun getPluralTryDays(quantity: Int, context: Context): String {
    return StringDesc.PluralFormatted(MR.plurals.subscribe_try_days_plural, quantity, quantity)
        .toString(context)
}

private val LocalDimens = compositionLocalOf { pickCorrectDimens() }
private val LocalColors = compositionLocalOf<SubscribeColors> { SubscribeColorsLight() }

private fun pickCorrectDimens(): SubscribeDimens {
    val heightDp = ap.resources.displayMetrics.let { (it.heightPixels / it.density).toInt() }

    return if (heightDp >= 700)
        SubscribeDimensPhoneH700()
    else if (heightDp >= 600)
        SubscribeDimensPhoneH600()
    else
        SubscribeDimensPhoneH500()
}