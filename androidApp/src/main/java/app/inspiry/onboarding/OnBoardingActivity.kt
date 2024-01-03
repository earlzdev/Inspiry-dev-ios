package app.inspiry.onboarding

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.TextureView
import androidx.activity.OnBackPressedDispatcherOwner
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import app.inspiry.BuildConfig
import app.inspiry.R
import app.inspiry.activities.MainActivity
import app.inspiry.core.analytics.AnalyticsManager
import app.inspiry.core.log.KLogger
import app.inspiry.core.manager.LicenseManager
import app.inspiry.core.media.BaseUnitsConverter
import app.inspiry.core.log.ErrorHandler
import app.inspiry.core.util.PredefinedColors
import app.inspiry.font.helpers.TextCaseHelper
import app.inspiry.font.model.InspFontStyle
import app.inspiry.core.ActivityRedirector
import app.inspiry.utilities.toCColor
import app.inspiry.utils.getContextWithLocale
import app.inspiry.utils.printDebug
import app.inspiry.views.aspectRatioWithBounds
import app.inspiry.views.group.BaseGroupZView
import app.inspiry.views.template.InspTemplateView
import app.inspiry.views.template.InspTemplateViewCreator
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.PagerState
import com.google.accompanist.pager.rememberPagerState
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.AssetDataSource
import com.google.android.exoplayer2.upstream.DataSource
import com.russhwolf.settings.Settings
import dev.icerock.moko.resources.StringResource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.android.get
import org.koin.android.ext.android.inject
import org.koin.core.context.GlobalContext
import org.koin.core.parameter.parametersOf

class OnBoardingActivity : AppCompatActivity() {

    private val settings: Settings by inject()
    private val logger: KLogger by inject {
        parametersOf("OnBoardingActivity")
    }
    private val redirector: ActivityRedirector by inject()
    private val licenseManager: LicenseManager by inject()
    private val analyticsManager: AnalyticsManager by inject()

    private lateinit var viewModel: OnBoardingViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.AppTheme)
        window.statusBarColor = Color.WHITE
        window.setBackgroundDrawable(null)

        viewModel = OnBoardingViewModel.create(
            settings,
            licenseManager,
            analyticsManager,
            get(),
            stringToEnLocale = {
                getContextWithLocale("en").getString(it.resourceId)
            },
            onFinish = {
                startActivity(
                    Intent(this, MainActivity::class.java)
                )
                finish()
            }
        ) {
            redirector.openSubscribeActivity(this, it)
            finish()
        }

        if (savedInstanceState == null) {
            viewModel.onCreateFirst()
        }

        setContent {
            MaterialTheme(
                colors = MaterialTheme.colors.copy(
                    isLight = true,
                    background = PredefinedColors.WHITE.toCColor()
                )
            ) {
                MainUi(viewModel)
            }
        }
    }

    override fun onBackPressed() {
        viewModel.onBackPressed()
    }
}

@OptIn(ExperimentalPagerApi::class)
@Composable
private fun MainUi(viewModel: OnBoardingViewModel) {
    val pagerState = rememberPagerState()
    val scope = rememberCoroutineScope { Dispatchers.Main }

    LaunchedEffect(Unit) {
        viewModel.nextStep = {
            scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
        }
        viewModel.prevStep = {
            scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) }
        }
        viewModel.step = pagerState::currentPage
    }

    Column(modifier = Modifier.fillMaxSize()) {

        HorizontalPager(
            count = viewModel.pagesData.size,
            modifier = Modifier.fillMaxWidth().weight(1f),
            state = pagerState
        ) { page ->

            val data = viewModel.pagesData[page]
            if (data is OnBoardingDataVideo) {
                VideoPromo(data, page, pagerState, viewModel)
            } else if (data is OnBoardingDataQuiz) {
                Quiz(data, page, pagerState, viewModel)
            }
        }
    }
}


@ExperimentalPagerApi
@Composable
private fun VideoPromo(
    data: OnBoardingDataVideo,
    pageIndex: Int,
    pagerState: PagerState,
    viewModel: OnBoardingViewModel
) {
    Column(
        modifier = Modifier.fillMaxSize()
            .background(color = androidx.compose.ui.graphics.Color.White),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        val colors = LocalColors.current
        val context: Context = LocalContext.current
        val dimens = LocalDimens.current
        val backPressedDispatcher =
            LocalOnBackPressedDispatcherOwner.current
        val unitsConverter: BaseUnitsConverter = remember { GlobalContext.get().get() }
        val textCaseHelper: TextCaseHelper = remember { GlobalContext.get().get() }
        val lifecycle = LocalLifecycleOwner.current.lifecycle

        val templateAndroidView = remember {
            BaseGroupZView(context, templateView = null, unitsConverter = unitsConverter)
        }

        val templateView = remember {
            val templateView =
                InspTemplateViewCreator.createInspTemplateView(
                    templateAndroidView,
                    canShowProgress = false
                )
            OnBoardingViewModel.initTemplate(templateView)
            templateView
        }


        val player: ExoPlayer = remember {
            loadVideo(
                context, data = data, templateView = templateView,
                backDispatcher = backPressedDispatcher
            )
        }

        if (pageIndex == pagerState.currentPage) {
            player.play()
        } else {
            player.pause()
        }

        PlayerDisposableEffect(data, player,
            lifecycle, playOnStart = { pageIndex == pagerState.currentPage })

        AndroidView(
            factory = {
                val texture = TextureView(it)

                OnBoardingViewModel.loadTemplate(
                    templateView, it.getString(data.text.resourceId),
                    app.inspiry.core.media.TextAlign.center, InspFontStyle.regular,
                    OnBoardingViewModel.TitleAnimationType.WORDS, colors
                )

                player.setVideoTextureView(texture)
                player.prepare()

                texture

            }, modifier = Modifier.fillMaxWidth()
                .weight(1f)
                .aspectRatioWithBounds(
                    ratio = data.videoWidth / data.videoHeight.toFloat(),
                    matchHeightConstraintsFirst = true
                )

        )

        AndroidView(
            factory = {
                templateAndroidView
            },
            modifier = Modifier.fillMaxWidth(fraction = dimens.animatedTextWidthPercent)
                .heightIn(
                    min = dimens.animatedTextMinHeight.dp,
                    max = (dimens.animatedTextMinHeight * 2).dp
                )
                .padding(
                    bottom = dimens.animatedPaddingBottom.dp,
                    top = dimens.animatedPaddingTop.dp
                )
        )

        Box(
            Modifier
                .padding(horizontal = dimens.buttonContinuePaddingHorizontal.dp)
                .height(dimens.buttonContinueHeight.dp)
                .fillMaxWidth(dimens.buttonContinueWidthPercent)
                .widthIn(min = dimens.buttonContinueMinWidth.dp)
                .clip(RoundedCornerShape(dimens.buttonContinueCorners.dp))
                .background(
                    Brush.horizontalGradient(
                        colors =
                        listOf(
                            colors.videoPromoContinueGradientStart.toCColor(),
                            colors.videoPromoContinueGradientEnd.toCColor()
                        )
                    )
                )
                .clickable {
                    viewModel.onClickContinue()
                },
            contentAlignment = Alignment.Center
        ) {


            val text =
                textCaseHelper.capitalize(LocalContext.current.getString(app.inspiry.projectutils.R.string.subscribe_continue_button))

            Text(
                text = text,
                modifier = Modifier.fillMaxWidth(),
                color = PredefinedColors.WHITE.toCColor(),
                textAlign = TextAlign.Center,
                fontSize = dimens.buttonContinueTextSize.sp,
                fontWeight = FontWeight.Bold,
                maxLines = dimens.buttonContinueMaxLines
            )
        }

        DotsIndicator(
            Modifier.padding(
                bottom = dimens.videoPromoIndicatorPaddingBottom.dp,
                top = dimens.videoPromoIndicatorPaddingTop.dp
            ).fillMaxWidth(),
            max = pagerState.pageCount,
            current = pageIndex
        )
    }
}

@Composable
@Preview
private fun DotsIndicator(
    modifier: Modifier = Modifier,
    max: Int = 4,
    current: Int = 2,
    colors: OnBoardingColors = OnBoardingColorsLight()
) {
    Row(
        modifier = Modifier.then(modifier)
            .height(
                LocalDimens.current.pageIndicatorSize.dp
            ),
        horizontalArrangement = Arrangement.spacedBy(
            LocalDimens.current.pageIndicatorPaddingHorizontal.dp,
            Alignment.CenterHorizontally
        ),
        verticalAlignment = Alignment.CenterVertically
    ) {

        for (i in 0 until max) {

            val scale by animateFloatAsState(if (i == current) 1.2f else 1f)
            val color by animateColorAsState(
                if (i == current) colors.pageIndicatorActive.toCColor()
                else colors.pageIndicatorInactive.toCColor()
            )
            Box(
                modifier = Modifier
                    .size(LocalDimens.current.pageIndicatorSize.dp)
                    .graphicsLayer(scaleX = scale, scaleY = scale)
                    .background(color, shape = CircleShape)
            )
        }
    }
}


@ExperimentalPagerApi
@Composable
private fun Quiz(
    data: OnBoardingDataQuiz,
    pageIndex: Int,
    pagerState: PagerState,
    viewModel: OnBoardingViewModel
) {

    val colors = LocalColors.current
    Column(modifier = Modifier.fillMaxSize().background(androidx.compose.ui.graphics.Color.White)) {

        Box(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
            DotsIndicator(
                Modifier
                    .align(Alignment.Center)
                    .padding(
                        bottom = LocalDimens.current.videoPromoIndicatorPaddingTop.dp,
                        top = LocalDimens.current.videoPromoIndicatorPaddingBottom.dp
                    ),
                max = pagerState.pageCount,
                current = pageIndex,
                colors = colors
            )

            Text(
                text = stringResource(app.inspiry.projectutils.R.string.onboarding_quiz_skip),
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 26.dp)
                    .clickable { viewModel.onClickContinue() }
                    .padding(bottom = 3.dp)
                    .padding(horizontal = 10.dp),
                color = colors.quizTextSkip.toCColor(),
                fontSize = 14f.sp,
                textAlign = TextAlign.End,
                textDecoration = TextDecoration.Underline
            )
        }

        if (data.singleChoice) {
            QuizFirst(data, viewModel, pageIndex, pagerState)
        } else {
            QuizSecond(data, viewModel, pageIndex, pagerState)
        }
    }
}

@ExperimentalPagerApi
@Composable
private fun ColumnScope.QuizFirst(
    data: OnBoardingDataQuiz, viewModel: OnBoardingViewModel,
    pageIndex: Int,
    pagerState: PagerState
) {
    val dimens = LocalDimens.current
    val context = LocalContext.current
    val colors = LocalColors.current

    Box(
        modifier = Modifier.fillMaxWidth().weight(1f),
        contentAlignment = BiasAlignment(0f, -0.3f)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            var selectedIndex: Int? by remember {
                mutableStateOf(null)
            }

            QuizTitle(
                modifier = Modifier.fillMaxWidth(fraction = 0.8f)
                    .padding(bottom = dimens.firstQuizTitlePaddingBottom.dp),
                text = data.title,
                textAlign = app.inspiry.core.media.TextAlign.center,
                pageIndex = pageIndex,
                pagerState = pagerState,
                animType = OnBoardingViewModel.TitleAnimationType.LINES_BOTTOM
            )

            for ((index, optionText) in data.choices.withIndex()) {
                QuizFirstOption(
                    modifier = Modifier.clickable {
                        if (selectedIndex == index)
                            selectedIndex = null
                        else {
                            selectedIndex = index
                            viewModel.onFirstQuizSelected(index)
                        }
                    },
                    text = stringResource(optionText.resourceId),
                    isSelected = index == selectedIndex,
                    colors = colors
                )
            }
        }
    }

    Text(
        text = context.getString(app.inspiry.projectutils.R.string.onboarding_quiz_1_hint),
        modifier = Modifier.padding(bottom = dimens.firstQuizUsefulAnswersPaddingBottom.dp)
            .fillMaxWidth(fraction = dimens.buttonContinueWidthPercent)
            .align(Alignment.CenterHorizontally),
        color = colors.firstQuizUsefulAnswers.toCColor(),
        fontSize = dimens.firstQuizUsefulAnswers.sp,
        textAlign = TextAlign.Center
    )
}

@Composable
@Preview
private fun QuizFirstOption(
    modifier: Modifier = Modifier,
    text: String = "some text",
    isSelected: Boolean = false,
    colors: OnBoardingColors = OnBoardingColorsLight()
) {
    val dimens = LocalDimens.current
    val shape = RoundedCornerShape(dimens.optionCorners.dp)
    Box(
        modifier = Modifier
            .padding(vertical = dimens.firstQuizOptionPaddingVertical.dp)
            .clip(shape)
            .then(modifier)
            .height(dimens.firstQuizOptionHeight.dp)
            .fillMaxWidth(fraction = dimens.buttonContinueWidthPercent)
            .background(
                if (isSelected) androidx.compose.ui.graphics.Color.Transparent
                else colors.firstQuizOptionBg.toCColor(), shape
            )
            .border(
                dimens.optionSelectedBorder.dp,
                if (isSelected) colors.firstQuizOptionBg.toCColor() else androidx.compose.ui.graphics.Color.Transparent,
                shape = shape
            ), contentAlignment = Alignment.Center
    ) {
        Text(
            text,
            modifier = Modifier.fillMaxWidth(),
            color = if (isSelected) colors.firstQuizOptionBg.toCColor() else androidx.compose.ui.graphics.Color.White,
            fontSize = dimens.firstQuizOption.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            textAlign = TextAlign.Center
        )

    }
}

@ExperimentalPagerApi
@Composable
private fun QuizTitle(
    modifier: Modifier = Modifier,
    text: StringResource,
    textAlign: app.inspiry.core.media.TextAlign,
    pageIndex: Int,
    pagerState: PagerState,
    animType: OnBoardingViewModel.TitleAnimationType
) {

    val unitsConverter: BaseUnitsConverter = remember { GlobalContext.get().get() }
    val context: Context = LocalContext.current
    val colors = LocalColors.current


    val templateAndroidView = remember {
        BaseGroupZView(context, templateView = null, unitsConverter = unitsConverter)
    }

    val templateView = remember {
        InspTemplateViewCreator.createInspTemplateView(templateAndroidView, canShowProgress = false)
    }

    fun updateAnim() {
        if (pageIndex == pagerState.currentPage) {
            templateView.startPlaying()
        } else {
            templateView.stopPlaying()
        }
    }
    updateAnim()

    AndroidView(
        factory = {

            OnBoardingViewModel.initTemplate(templateView)
            OnBoardingViewModel.loadTemplate(
                templateView,
                it.getString(text.resourceId),
                textAlign,
                InspFontStyle.bold, animType, colors
            )

            updateAnim()

            templateAndroidView
        },
        modifier = modifier
    )
}


@ExperimentalPagerApi
@Composable
private fun QuizSecond(
    data: OnBoardingDataQuiz,
    viewModel: OnBoardingViewModel,
    pageIndex: Int,
    pagerState: PagerState
) {

    val dimens = LocalDimens.current
    val context = LocalContext.current
    val colors = LocalColors.current
    val bottomBarHeight = 65

    Box(modifier = Modifier.fillMaxSize()) {

        val scrollState = rememberScrollState()
        val selectedChoices: MutableList<Int> = remember {
            mutableStateListOf()
        }

        var textSuggestion by remember { mutableStateOf("") }

        Column(
            modifier = Modifier.fillMaxWidth()
                .fillMaxHeight()
                .verticalScroll(scrollState)
        ) {
            QuizTitle(
                modifier = Modifier.fillMaxWidth(fraction = 0.8f)
                    .padding(start = 29.dp, top = 18.dp),
                text = data.title,
                textAlign = app.inspiry.core.media.TextAlign.start,
                pageIndex = pageIndex,
                pagerState = pagerState,
                animType = OnBoardingViewModel.TitleAnimationType.LINES_RIGHT
            )

            Text(
                text = context.getString(app.inspiry.projectutils.R.string.onboarding_quiz_2_hint),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = dimens.secondQuizOptionPaddingLeft.dp)
                    .padding(top = 28.dp, bottom = 5.dp),
                color = colors.firstQuizUsefulAnswers.toCColor(),
                fontSize = dimens.firstQuizUsefulAnswers.sp
            )
            val allChoices: List<StringResource>
            if (BuildConfig.DEBUG) {
                allChoices = mutableListOf()
                allChoices.addAll(data.choices)
                allChoices.addAll(data.choices)
            } else {
                allChoices = data.choices
            }


            for ((index, item) in allChoices.withIndex()) {
                QuizSecondOption(
                    modifier = Modifier.clickable {
                        if (!selectedChoices.remove(index))
                            selectedChoices.add(index)

                    },
                    isSelected = index in selectedChoices,
                    colors,
                    context.getString(item.resourceId)
                )
            }

            SuggestYourArea(onKeyboardEntered = {

            }, text = textSuggestion, onTextEntered = { textSuggestion = it })

            Spacer(modifier = Modifier.height((bottomBarHeight + 10).dp))
        }

        if (selectedChoices.isNotEmpty() || textSuggestion.isNotBlank()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(bottomBarHeight.dp).align(Alignment.BottomCenter)
                    .background(androidx.compose.ui.graphics.Color.White),
                contentAlignment = Alignment.Center,
            ) {

                val textCaseHelper: TextCaseHelper = GlobalContext.get().get()
                Text(
                    textCaseHelper.toLowerCase(context.getString(app.inspiry.projectutils.R.string.subscribe_continue_button)),
                    color = androidx.compose.ui.graphics.Color.White,
                    fontSize = dimens.buttonContinueTextSize.sp,
                    modifier = Modifier
                        .clip(RoundedCornerShape(dimens.optionCorners.dp))
                        .background(
                            Brush.horizontalGradient(
                                colors =
                                listOf(
                                    colors.secondQuizContinueGradientStart.toCColor(),
                                    colors.secondQuizContinueGradientEnd.toCColor()
                                )
                            )
                        )
                        .clickable {
                            viewModel.onSecondQuizSelected(selectedChoices, textSuggestion)
                        }
                        .padding(horizontal = 37.dp, vertical = 5.dp)
                        .padding(bottom = 1.dp)
                )
            }
        }
    }
}

@Composable
private fun QuizSecondOption(
    modifier: Modifier = Modifier,
    isSelected: Boolean,
    colors: OnBoardingColors,
    text: String
) {
    val dimens = LocalDimens.current
    val shape = RoundedCornerShape(dimens.optionCorners.dp)
    Box(
        modifier = Modifier
            .padding(
                vertical = dimens.firstQuizOptionPaddingVertical.dp,
                horizontal = dimens.secondQuizOptionPaddingLeft.dp
            )
            .clip(shape)
            .then(modifier)
            .height(dimens.firstQuizOptionHeight.dp)
            .fillMaxWidth(fraction = dimens.animatedTextWidthPercent)
            .background(
                if (isSelected) androidx.compose.ui.graphics.Color.Transparent
                else colors.secondQuizOptionBg.toCColor(), shape
            )
            .border(
                dimens.optionSelectedBorder.dp,
                if (isSelected) colors.secondQuizOptionBg.toCColor()
                else androidx.compose.ui.graphics.Color.Transparent,
                shape = shape
            ), contentAlignment = Alignment.Center
    ) {
        Text(
            text,
            modifier = Modifier.fillMaxWidth(),
            color = if (isSelected) colors.firstQuizOptionBg.toCColor()
            else androidx.compose.ui.graphics.Color.White,
            fontSize = dimens.secondQuizOption.sp,
            maxLines = 1,
            textAlign = TextAlign.Center
        )

    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun SuggestYourArea(
    onKeyboardEntered: () -> Unit,
    text: String,
    onTextEntered: (String) -> Unit
) {
    val dimens = LocalDimens.current
    val context = LocalContext.current
    val colors = LocalColors.current

    Text(
        context.getString(app.inspiry.projectutils.R.string.onboarding_quiz_2_option_additional),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = dimens.secondQuizOptionPaddingLeft.dp)
            .padding(top = 28.dp, bottom = 6.dp),
        color = colors.secondQuizSuggestText.toCColor(),
        fontSize = dimens.skipText.sp
    )
    val keyboardController = LocalSoftwareKeyboardController.current

    TextField(
        value = text,
        onValueChange = onTextEntered,
        placeholder = { EmptyTextArea() },
        keyboardActions = KeyboardActions(onSend = {
            keyboardController?.hide()
            onKeyboardEntered()
        }),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
        colors = TextFieldDefaults.textFieldColors(
            textColor = colors.secondQuizSuggestText.toCColor(),
            cursorColor = colors.secondQuizContinueGradientStart.toCColor(),
            backgroundColor = colors.secondQuizSuggestBg.toCColor(),
            focusedIndicatorColor = colors.secondQuizContinueGradientStart.toCColor()
        ),
        textStyle = TextStyle(fontSize = dimens.skipText.sp),

        modifier = Modifier
            .padding(horizontal = dimens.secondQuizOptionPaddingLeft.dp)
            .fillMaxWidth()
    )
}

@Composable
private fun EmptyTextArea() {
    val dimens = LocalDimens.current
    val context = LocalContext.current
    val colors = LocalColors.current

    Text(
        context.getString(app.inspiry.projectutils.R.string.onboarding_quiz_2_option_hint),
        color = colors.secondQuizSuggestTextHint.toCColor(),
        fontSize = dimens.skipText.sp,
        maxLines = 1
    )
}

@Composable
fun PlayerDisposableEffect(
    data: OnBoardingDataVideo,
    player: ExoPlayer,
    lifecycle: Lifecycle,
    playOnStart: () -> Boolean = { true }
) {
    DisposableEffect(key1 = data.video) {

        val lifecycleObserver = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) {
                player.pause()
            } else if (event == Lifecycle.Event.ON_START) {
                if (playOnStart()) {
                    player.play()
                }
            }
        }
        lifecycle.addObserver(lifecycleObserver)
        onDispose {
            lifecycle.removeObserver(lifecycleObserver)
            player.release()
        }
    }
}

fun loadVideo(
    context: Context,
    data: OnBoardingDataVideo,
    templateView: InspTemplateView,
    backDispatcher: OnBackPressedDispatcherOwner?
): ExoPlayer {

    val exoPlayer = ExoPlayer.Builder(context).build()
    exoPlayer.repeatMode = ExoPlayer.REPEAT_MODE_ALL

    val dataSourceFactory = DataSource.Factory { AssetDataSource(context) }

    val videoSource = ProgressiveMediaSource.Factory(dataSourceFactory)
        .createMediaSource(
            MediaItem.Builder()
                .setUri(data.video.path).build()
        )

    exoPlayer.setMediaSource(videoSource)

    exoPlayer.addListener(object : Player.Listener {

        override fun onPlayerError(error: PlaybackException) {
            error.printDebug()
            val errorHandler: ErrorHandler = GlobalContext.get().get()
            errorHandler.toastError(error, onlyMessage = true)

            if (backDispatcher != null) {
                val currentState = backDispatcher.lifecycle.currentState
                if (currentState.isAtLeast(Lifecycle.State.INITIALIZED)) {
                    backDispatcher.onBackPressedDispatcher.onBackPressed()
                }
            }
        }

        override fun onPlaybackStateChanged(state: Int) {
            if (state == ExoPlayer.STATE_READY) {
            }
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            super.onIsPlayingChanged(isPlaying)
            if (isPlaying) {
                templateView.startPlaying(resetFrame = false)
            } else {
                templateView.stopPlaying()
            }
        }

        override fun onPositionDiscontinuity(
            oldPosition: Player.PositionInfo,
            newPosition: Player.PositionInfo,
            reason: Int
        ) {
            super.onPositionDiscontinuity(oldPosition, newPosition, reason)

            if (reason == ExoPlayer.DISCONTINUITY_REASON_AUTO_TRANSITION) {
                OnBoardingViewModel.onVideoLooped(templateView)
            }
        }
    })

    return exoPlayer
}

private val LocalDimens = compositionLocalOf<OnBoardingDimens> { OnBoardingDimensPhone() }
private val LocalColors = compositionLocalOf<OnBoardingColors> { OnBoardingColorsLight() }
