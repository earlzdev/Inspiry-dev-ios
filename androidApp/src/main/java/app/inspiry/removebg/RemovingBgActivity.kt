package app.inspiry.removebg

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import app.inspiry.MR
import app.inspiry.R
import app.inspiry.core.media.BaseUnitsConverter
import app.inspiry.core.media.Template
import app.inspiry.core.log.ErrorHandler
import app.inspiry.helpers.K
import app.inspiry.onboarding.OnBoardingViewModel
import app.inspiry.utilities.toCColor
import app.inspiry.utils.Constants
import app.inspiry.utils.IntentUtils
import app.inspiry.utils.printDebug
import app.inspiry.views.group.BaseGroupZView
import app.inspiry.views.template.InspTemplateViewCreator
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieComposition
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.koin.android.ext.android.get
import org.koin.android.ext.android.inject
import org.koin.core.context.GlobalContext

class RemovingBgActivity : ComponentActivity() {

    private lateinit var viewModel: RemovingBgViewModel
    private val errorHandler: ErrorHandler by inject()
    lateinit var source: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val inputString = intent.getStringArrayListExtra(EXTRA_IMAGE_PATHS)
        source = intent.getStringExtra(Constants.EXTRA_SOURCE)!!

        if (inputString != null)
            load(inputString)


        setContent {
            MaterialTheme(
                colors = MaterialTheme.colors
                    .copy(
                        background = Color.Transparent,
                        isLight = LocalColors.current.isLight
                    )
            ) {
                MainUI()
            }
        }
    }

    companion object {
        const val EXTRA_IMAGE_PATHS = "image_path"
        const val EXTRA_RESULT = "results"
    }

    private fun load(inputString: List<String>) {
        viewModel =
            ViewModelProvider(
                this,
                RemovingBgViewModelFactory(inputString, get(), get(), get(), source, get(), get())
            )[RemovingBgViewModel::class.java]

        lifecycleScope.launch {
            viewModel.processedImage.collect {

                if (it != null) {
                    if (it.isSuccess) {
                        val result = it.getOrThrow()
                        K.i("RemovingBgActivity") {
                            "result image path $result"
                        }
                        setResult(
                            RESULT_OK,
                            Intent().putExtra(EXTRA_RESULT, ArrayList(result))
                        )
                        finish()
                    } else if (it.isFailure) {
                        val e = it.exceptionOrNull()
                        if (e != null) {
                            errorHandler.toastError(e)
                            e.printDebug()
                            finish()
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MainUI() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {

        val colors = LocalColors.current
        val dimens = LocalDimens.current
        val context = LocalContext.current

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val composition by rememberLottieComposition(LottieCompositionSpec.Asset(MR.assets.app_resources.remove_background_progress.path))
            LottieAnimation(
                composition = composition,
                modifier = Modifier.size(dimens.animationSize.dp).padding(10.dp),
                iterations = Integer.MAX_VALUE
            )

            val template = remember {
                OnBoardingViewModel.getTemplateRemovingBg(
                    context.getString(
                        app.inspiry.projectutils.R.string.remove_bg_loading
                    ), colors, dimens
                )
            }
            TemplateView(
                modifier = Modifier.fillMaxWidth(), template
            )
        }

        Image(
            painterResource(R.drawable.photoroom_attribution), contentScale = ContentScale.Fit,
            modifier = Modifier.align(Alignment.BottomCenter)
                .padding(bottom = dimens.attributionPaddingBottom.dp)
                .width(dimens.attributionWidth.dp)
                .height(dimens.attributionHeight.dp)
                .clickable {

                    context.startActivity(
                        IntentUtils.openLink("https://play.google.com/store/apps/details?id=com.photoroom.app&hl=en&gl=US")
                            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    )
                },
            alignment = Alignment.Center, contentDescription = "attribution"
        )

        val backPress = LocalOnBackPressedDispatcherOwner.current
        Row(
            modifier = Modifier
                .padding(20.dp)
                .align(Alignment.BottomCenter)
                .clickable {
                    if (backPress != null && backPress.lifecycle.currentState.isAtLeast(
                            Lifecycle.State.INITIALIZED
                        )
                    ) {
                        backPress.onBackPressedDispatcher.onBackPressed()
                    }
                }
                .padding(horizontal = 15.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = context.getString(app.inspiry.projectutils.R.string.cancel).lowercase(),
                color = colors.closeTint.toCColor(),
                maxLines = 1,
                fontSize = dimens.cancelTextSize.sp
            )

            Image(
                painter = painterResource(R.drawable.ic_subscribe_close),
                contentDescription = "close",
                modifier = Modifier
                    .padding(start = 10.dp)
                    .background(colors.closeBg.toCColor(), CircleShape)
                    .scale(0.65f)
                    .padding(all = 6.dp),
                contentScale = ContentScale.Inside,
                colorFilter = ColorFilter.tint(colors.closeTint.toCColor())
            )
        }
    }
}

@Composable
fun TemplateView(modifier: Modifier = Modifier, template: Template) {
    AndroidView(
        factory = { context ->
            val unitsConverter: BaseUnitsConverter = GlobalContext.get().get()
            val templateAndroidView =
                BaseGroupZView(context, templateView = null, unitsConverter = unitsConverter)

            val templateView =
                InspTemplateViewCreator.createInspTemplateView(
                    templateAndroidView,
                    canShowProgress = false
                )

            templateView.shouldHaveBackground = false
            templateView.loopAnimation = true

            templateView.loadTemplate(template)
            templateView.startPlaying()

            templateAndroidView

        }, modifier = modifier
    )
}


private val LocalColors = compositionLocalOf<RemovingBgColors> { RemovingBgColorsBlack() }
private val LocalDimens = compositionLocalOf<RemovingBgDimens> { RemovingBgDimensPhone() }