package app.inspiry.featurepromo

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.TextureView
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import app.inspiry.R
import app.inspiry.activities.MainActivity
import app.inspiry.core.manager.LicenseManager
import app.inspiry.core.media.BaseUnitsConverter
import app.inspiry.onboarding.*
import app.inspiry.core.ActivityRedirector
import app.inspiry.utilities.toCColor
import app.inspiry.utils.Constants
import app.inspiry.views.aspectRatioWithBounds
import app.inspiry.views.group.BaseGroupZView
import app.inspiry.views.template.InspTemplateViewCreator
import com.google.android.exoplayer2.ExoPlayer
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.core.context.GlobalContext

class RemoveBgPromoActivity : AppCompatActivity() {

    val licenseManager: LicenseManager by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.AppTheme)

        lifecycleScope.launch {
            licenseManager.hasPremiumState.collect {
                if (it)
                    finish()
            }
        }

        setContent {
            MaterialTheme(
                colors = MaterialTheme.colors
                    .copy(
                        background = LocalColors.current.background.toCColor(),
                        isLight = LocalColors.current.isLight
                    )
            ) {
                MainUI(FeaturePromoData.removeBgPromoData)
            }
        }
    }

    override fun onBackPressed() {
        if (intent.action == Constants.ACTION_FROM_NOTIFICATION) {
            startActivity(Intent(this, MainActivity::class.java))
        }
        super.onBackPressed()
    }
}

@Composable
private fun MainUI(data: FeaturePromoData) {
    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        val dimens = LocalDimens.current
        val colors = LocalColors.current
        val context = LocalContext.current

        Box(modifier = Modifier.fillMaxWidth().height(dimens.topPanelHeight.dp)) {

            val backPress = LocalOnBackPressedDispatcherOwner.current
            Image(
                painter = painterResource(R.drawable.ic_subscribe_close),
                contentDescription = "close",
                modifier = Modifier.align(Alignment.CenterStart)
                    .padding(start = dimens.closeMarginEnd.dp)
                    .clip(CircleShape)
                    .clickable {
                        if (backPress != null && backPress.lifecycle.currentState.isAtLeast(
                                Lifecycle.State.INITIALIZED
                            )
                        ) {
                            backPress.onBackPressedDispatcher.onBackPressed()
                        }
                    }
                    .padding(
                        horizontal = dimens.closeAdditionalPadding.dp,
                        vertical = dimens.closeAdditionalPadding.dp
                    ),
                contentScale = ContentScale.Inside,
                alpha = 0.6f,
                colorFilter = ColorFilter.tint(colors.closeTint.toCColor())
            )
        }

        InnerVideoView(data)

        Box(
            modifier = Modifier.padding(vertical = dimens.buttonMarginVertical.dp)
                .height(dimens.buttonHeight.dp)
                .fillMaxWidth(dimens.buttonWidthPercent)
                .clip(RoundedCornerShape(dimens.buttonCornersRadius.dp))
                .background(
                    brush = Brush.linearGradient(
                        listOf(
                            colors.buttonGradientStart.toCColor(),
                            colors.buttonGradientEnd.toCColor()
                        )
                    )
                )
                .clickable {
                    val activityRedirector: ActivityRedirector = GlobalContext.get().get()
                    activityRedirector.openSubscribeActivity(context as Activity, data.id)
                },
            contentAlignment = Alignment.Center
        ) {

            Text(
                context.getString(app.inspiry.projectutils.R.string.feature_promo_button),
                modifier = Modifier.fillMaxWidth(), color = Color.White,
                fontSize = dimens.buttonTextSize.sp, fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center, maxLines = 1
            )
        }
    }
}

@Composable
private fun ColumnScope.InnerVideoView(data: FeaturePromoData) {

    val dimens = LocalDimens.current
    val colors = LocalColors.current
    val context = LocalContext.current
    val backPressedDispatcher = LocalOnBackPressedDispatcherOwner.current
    val unitsConverter: BaseUnitsConverter = remember { GlobalContext.get().get() }
    val lifecycle = LocalLifecycleOwner.current.lifecycle

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .weight(1f)
            .padding(horizontal = dimens.videoPaddingHorizontal.dp)
            .clip(RoundedCornerShape(dimens.videoCornersRadius))
            .aspectRatioWithBounds(
                data.videoWidth / data.videoHeight.toFloat(),
                matchHeightConstraintsFirst = true
            )
    ) {


        val templateAndroidView = remember {
            BaseGroupZView(context, templateView = null, unitsConverter = unitsConverter)
        }

        val templateView = remember {
            val templateView = InspTemplateViewCreator.createInspTemplateView(templateAndroidView, canShowProgress = false)
            OnBoardingViewModel.initTemplate(templateView)
            templateView
        }

        val player: ExoPlayer = remember {
            loadVideo(
                context, data = data,
                templateView = templateView,
                backDispatcher = backPressedDispatcher,
            )
        }

        PlayerDisposableEffect(data, player, lifecycle)


        AndroidView(
            factory = { context ->
                val surface = TextureView(context)

                templateView.loadTemplate(
                    FeaturePromoData.removeBgPromoTemplate(
                        colors,
                        textToString = { context.getString(it.resourceId) })
                )

                player.setVideoTextureView(surface)
                player.prepare()
                player.play()

                surface
            },
            modifier = Modifier.fillMaxSize()
        )

        AndroidView(
            factory = {
                templateAndroidView

            }, modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .align(Alignment.BottomCenter)
        )

    }
}


private val LocalColors = compositionLocalOf<FeaturePromoColors> { FeaturePromoColorsLight() }
private val LocalDimens = compositionLocalOf<FeaturePromoDimens> { FeaturePromoDimensPhone() }