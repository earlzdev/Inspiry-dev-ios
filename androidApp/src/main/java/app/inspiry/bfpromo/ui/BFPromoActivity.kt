package app.inspiry.bfpromo.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.TextureView
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.ViewModelProvider
import app.inspiry.MR
import app.inspiry.R
import app.inspiry.activities.MainActivity
import app.inspiry.bfpromo.BFPromoData
import app.inspiry.bfpromo.BFPromoManager
import app.inspiry.bfpromo.viewmodel.*
import app.inspiry.core.analytics.AnalyticsManager
import app.inspiry.core.data.InspResponse
import app.inspiry.core.data.InspResponseData
import app.inspiry.core.data.InspResponseError
import app.inspiry.core.manager.LicenseManager
import app.inspiry.font.helpers.getCurrentLocale
import app.inspiry.subscribe.ui.Video
import app.inspiry.utilities.toCColor
import app.inspiry.utils.Constants
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieComposition
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.android.exoplayer2.C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.AssetDataSource
import com.google.android.exoplayer2.upstream.DataSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.koin.android.ext.android.inject
import kotlin.math.roundToInt

class BFPromoActivity : ComponentActivity() {
    val licenseManager: LicenseManager by inject()
    val analyticsManager: AnalyticsManager by inject()
    lateinit var source: String
    lateinit var viewModel: BFPromoViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(android.R.style.Theme_Material_NoActionBar_Fullscreen)

        source = intent.getStringExtra(Constants.EXTRA_SOURCE)!!

        viewModel =
            ViewModelProvider(
                this,
                BFPromoViewModelImplFactory(source, analyticsManager, licenseManager)
            )[BFPromoViewModelImpl::class.java]

        setContent {
            MaterialTheme(colors = MaterialTheme.colors.copy(isLight = false)) {
                CompositionLocalProvider(LocalViewModel provides viewModel) {
                    MainUi()
                }
            }
        }
    }

    override fun onBackPressed() {
        if (source == Constants.ACTION_FROM_NOTIFICATION || source == SOURCE_ON_STARTUP) {
            startActivity(Intent(this, MainActivity::class.java))
        }
        super.onBackPressed()
    }
}

private const val SOURCE_ON_STARTUP = "on_startup"

fun BFPromoManager.mayOpenBFPromoOnStart(activity: Activity): Boolean {

    val needToOpen = shouldOpenBFPromoOnStart()
    if (needToOpen) {
        activity.startActivity(
            Intent(
                activity,
                BFPromoActivity::class.java
            ).putExtra(Constants.EXTRA_SOURCE, SOURCE_ON_STARTUP)
                .addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
        )
    }
    return needToOpen
}

@Composable
@Preview
private fun MainUi() {
    // Remember a SystemUiController
    val systemUiController = rememberSystemUiController()

    LaunchedEffect(Unit) {
        systemUiController.setNavigationBarColor(Color.Black)
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {

        val boxScope = this
        val dimens = LocalDimens.current
        val colors = LocalColors.current

        Video(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = boxScope.maxHeight - dimens.panelHeight.dp + dimens.panelTopCorners.dp)
                .aspectRatio(0.5625f)
                .align(Alignment.TopCenter),
            videoId = MR.assets.videos.promo.bfpromo
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            val response by LocalViewModel.current.state.collectAsState()

            val specialOfferString =
                stringResource(app.inspiry.projectutils.R.string.bf_special_offer)
            Text(
                (response as? InspResponseData?)?.data?.discountPercent?.let { "$specialOfferString -$it%" }
                    ?: specialOfferString,
                modifier = Modifier.fillMaxWidth(0.9f).padding(bottom = 11.dp),
                color = colors.specialOffer.toCColor(),
                fontSize = dimens.textSpecialOffer.sp,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
            )

            Box(
                modifier = Modifier.fillMaxWidth()
                    .height(dimens.panelHeight.dp).background(
                        colors.panelBg.toCColor(),
                        RoundedCornerShape(
                            topStart = dimens.panelTopCorners.dp,
                            topEnd = dimens.panelTopCorners.dp
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {


                if (response == null) {
                    CircularProgressIndicator(color = colors.newPrice.toCColor())
                } else if (response is InspResponseError) {
                    PanelError((response as InspResponseError<BFPromoData>).throwable)
                } else if (response is InspResponseData) {
                    PanelContent((response as InspResponseData<BFPromoData>).data)
                } else {
                    throw IllegalStateException()
                }
            }
        }

        val lottieComposition by
        rememberLottieComposition(LottieCompositionSpec.Asset(MR.assets.json.bfpromo_circles.path))
        LottieAnimation(
            lottieComposition,
            modifier = Modifier.fillMaxWidth(0.916f)
                .align(Alignment.BottomCenter)
                .offset(y = maxHeight / 7),
            contentScale = ContentScale.Fit,
            iterations = Integer.MAX_VALUE
        )

        val backPressedDispatcherOwner = LocalOnBackPressedDispatcherOwner.current
        Image(
            painter = painterResource(R.drawable.ic_close_bfpromo),
            stringResource(app.inspiry.projectutils.R.string.back),
            modifier = Modifier
                .clickable {
                    backPressedDispatcherOwner?.onBackPressedDispatcher?.onBackPressed()
                }
                .padding(start = 10.dp, top = 10.dp)
                .padding(10.dp)
        )
    }
}

@Composable
@Preview
private fun PanelContentPreview() {
    val dimens = LocalDimens.current
    val colors = LocalColors.current
    Box(
        modifier = Modifier.fillMaxWidth()
            .height(dimens.panelHeight.dp).background(
                colors.panelBg.toCColor(),
                RoundedCornerShape(
                    topStart = dimens.panelTopCorners.dp,
                    topEnd = dimens.panelTopCorners.dp
                )
            )
    ) {
        PanelContent(getDefaultData())
    }
}

@Composable
@Preview
private fun DeleteText(text: String = "28.5$") {

    val dimens = LocalDimens.current
    val colors = LocalColors.current

    Box(modifier = Modifier.width(IntrinsicSize.Min).height(IntrinsicSize.Min)) {

        val strokeWidth = LocalDensity.current.run { 2.dp.toPx() }
        Canvas(modifier = Modifier.fillMaxSize().padding(bottom = 4.dp, top = 6.dp)) {

            drawLine(
                color = colors.oldPriceDelete.toCColor(),
                strokeWidth = strokeWidth,
                start = Offset(0f, size.height),
                end = Offset(size.width, 0f)
            )
        }

        Text(
            text,
            modifier = Modifier.padding(horizontal = 15.dp),
            color = colors.oldPrice.toCColor(),
            fontSize = dimens.textOldPrice.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
@Preview
private fun PanelError(e: Throwable = IllegalStateException("no message")) {
    LaunchedEffect(e) {
        e.printStackTrace()
    }
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {

        Text(
            stringResource(app.inspiry.projectutils.R.string.error_to_load_template_subtitle),
            modifier = Modifier.padding(top = 10.dp, bottom = 10.dp).padding(horizontal = 20.dp),
            color = Color.White,
            fontSize = 16.sp,
            textAlign = TextAlign.Center
        )
        val viewModel = LocalViewModel.current

        ButtonContinue(stringResource(app.inspiry.projectutils.R.string.error_to_load_template_button)) {
            viewModel.reload()
        }
    }
}

@Composable
private fun ButtonContinue(text: String, onClick: () -> Unit) {

    val dimens = LocalDimens.current
    val colors = LocalColors.current

    Box(
        modifier = Modifier
            .height(dimens.buttonContinueHeight.dp)
            .widthIn(min = 216.dp)
            .clickable(onClick = onClick)
            .background(
                Brush.horizontalGradient(
                    listOf(
                        colors.buttonGradientStart.toCColor(),
                        colors.buttonGradientEnd.toCColor()
                    )
                ),
                shape = RoundedCornerShape(dimens.buttonRoundedCorners.dp)
            ), contentAlignment = Alignment.Center
    ) {


        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp),
            color = colors.buttonText.toCColor(),
            textAlign = TextAlign.Center,
            fontSize = dimens.textContinue.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun PanelContent(data: BFPromoData) {

    val dimens = LocalDimens.current
    val colors = LocalColors.current

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            stringResource(app.inspiry.projectutils.R.string.bf_12_months),
            modifier = Modifier.padding(top = 17.dp, bottom = 5.dp),
            color = colors.specialOffer.toCColor(),
            fontSize = dimens.text12Months.sp,
            textAlign = TextAlign.Center
        )

        DeleteText(data.oldPrice)

        Text(
            stringResource(app.inspiry.projectutils.R.string.bf_only_price) + " " + data.newPrice,
            modifier = Modifier.padding(top = 10.dp, bottom = 16.dp),
            color = colors.newPrice.toCColor(),
            fontSize = dimens.textOldPrice.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        val context = LocalContext.current
        val viewModel = LocalViewModel.current


        val continueUppercase =
            stringResource(app.inspiry.projectutils.R.string.subscribe_continue_button)
        // make string capitalized
        ButtonContinue(
            continueUppercase[0] + continueUppercase.substring(1)
                .lowercase(context.getCurrentLocale())
        ) {

            // can we do such conversion ?
            viewModel.onClickSubscribe(context as Activity)
        }
    }
}

private fun getDefaultData() = BFPromoData(50, "23.56$", "11.99$")

private val LocalDimens = compositionLocalOf<BFPromoDimens> { BFPromoDimensPhone() }
private val LocalColors = compositionLocalOf<BFPromoColors> { BFPromoColorsDark() }

private val LocalViewModel = compositionLocalOf<BFPromoViewModel> {
    object : BFPromoViewModel {
        override val state: StateFlow<InspResponse<BFPromoData>?> =
            MutableStateFlow(InspResponseData(getDefaultData()))

        override fun onClickSubscribe(activity: Activity) {}
        override fun reload() {}
    }
}