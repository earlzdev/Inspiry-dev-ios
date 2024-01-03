package app.inspiry.dialog.rating

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.DialogFragment
import app.inspiry.MR
import app.inspiry.R
import app.inspiry.core.media.BaseUnitsConverter
import app.inspiry.core.media.Media
import app.inspiry.core.media.MediaText
import app.inspiry.core.media.Template
import app.inspiry.core.template.MediaReadWrite
import app.inspiry.utilities.putArgs
import app.inspiry.utilities.toCColor
import app.inspiry.views.group.BaseGroupZView
import app.inspiry.views.template.InspTemplateViewCreator
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieComposition
import org.koin.android.ext.android.inject
import org.koin.core.context.GlobalContext.get


class RateUsDialog : DialogFragment() {

    private val mediaReadWrite: MediaReadWrite by inject()

    private val dialogHelper = RatingDialogHelper(dialogID = DIALOG_ID)

    init {
        setStyle(STYLE_NO_TITLE, R.style.RatingDialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val corners = Shapes(
            small = RoundedCornerShape(5.dp),
            medium = RoundedCornerShape(20.dp),
            large = RoundedCornerShape(20.dp)
        )

        dialogHelper.sendDialogOpenEvent()

        return ComposeView(requireContext()).apply {
            setContent {
                MaterialTheme(shapes = corners) {
                    RateUsContent(textMedia(), ::dismiss) { r, f ->
                        dialogHelper.rating(stars = r, feedback = f, context = requireContext())
                        dismiss()
                    }
                }
            }
        }
    }

    private fun textMedia(): Media {
        val tMedia =
            mediaReadWrite.decodeMediaFromAssets(MR.assets.texts.RatingDialogText) as MediaText
        tMedia.text = requireArguments().getString(POPUP_ARG) ?: throw IllegalStateException("Null popup arg passed")
        tMedia.animationParamOut = null
        tMedia.animatorsOut = emptyList()
        return tMedia
    }

    companion object {

        const val DIALOG_ID = "second"

        /**
         * Creating RateUsDialog
         * @param popupString - dialog main string (like "We'd love to know your opinion")
         * @param sharing - creating dialog after share stories
         * @param force - dialog will be created anyway (for testing)
         */
        fun create(
            popupString: String,
        ): RateUsDialog {

            val dialog = RateUsDialog()

            dialog.putArgs {
                putString(POPUP_ARG, popupString)
            }

            return dialog
        }
    }
}
private const val POPUP_ARG = "popup_string"

private val LocalColors = compositionLocalOf<RatingDialogColors> { RatingDialogLightColors() }

@Composable
private fun RateUsContent(
    textMedia: Media?,
    onBackPress: () -> Unit,
    onRating: (Int, String?) -> Unit
) {

    Box(
        Modifier
            .background(
                color = LocalColors.current.background.toCColor(),
                shape = RoundedCornerShape(30.dp)
            )
            .size(280.dp, 367.dp),
        Alignment.TopCenter
    ) {
        var rating by remember { mutableStateOf(0) }
        if (rating in 1..4) Feedback { onRating(rating, it) }
        else PopupMain(textMedia, onBackPress) {
            if (it == 5) onRating(5, null)
            else rating = it
        }
    }
}

@Composable
private fun Lottie(resId: Int, iterationCount: Int) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(resId))
    LottieAnimation(
        composition = composition,
        modifier = Modifier.fillMaxSize(),
        iterations = iterationCount
    )
}

@Composable
private fun BoxScope.PopupMain(
    textMedia: Media?,
    onBackPress: () -> Unit, onFeedback: (Int) -> Unit
) {
    Box(
        Modifier
            .size(188.dp, 150.dp)
            .padding(top = 38.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Lottie(resId = R.raw.line_star, Integer.MAX_VALUE)

    }
    Column(
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Box(
            Modifier
                .size(130.dp, 150.dp)
                .padding(top = 38.dp)
        ) {
            Lottie(resId = R.raw.smile_popup, 1)

        }
        Box(
            Modifier
                .height(88.dp)
                .fillMaxWidth()
                .padding(top = 5.dp, start = 5.dp, end = 5.dp),
            Alignment.Center
        ) {
            TextAnimation(textMedia)
        }
        Box(
            Modifier
                .height(130.dp)
                .background(
                    color = LocalColors.current.starsBackground.toCColor(),
                    shape = RoundedCornerShape(32.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                Modifier
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Text(
                    text = LocalContext.current.getString(app.inspiry.projectutils.R.string.rating_dialog_rateus_text),
                    color = LocalColors.current.starsUpText.toCColor(),
                    fontSize = 18.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                Stars() { onFeedback(it) }
            }

        }
    }

    Image(
        painterResource(R.drawable.ic_bottom_banner_close),
        stringResource(app.inspiry.projectutils.R.string.back),
        modifier = Modifier.align(Alignment.TopEnd)
            .graphicsLayer(scaleX = 0.9f, scaleY = 0.9f)
            .clip(CircleShape)
            .clickable(onClick = onBackPress)
            .padding(vertical = 16.dp, horizontal = 16.dp),
        contentScale = ContentScale.Inside,
        colorFilter = ColorFilter.tint(Color.Gray.copy(alpha = 0.8f))
    )
}

@Composable()
private fun Stars(onStarClick: (Int) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        for (i in 1..5) {
            Image(
                painterResource(id = R.drawable.ic_rating_star),
                null,
                modifier = Modifier
                    .width(36.dp)
                    .padding(start = 2.dp)
                    .clickable { onStarClick(i) }
            )
        }
    }
}

@Composable
private fun TextAnimation(textMedia: Media?) {
    AndroidView(
        factory = {
            val unitsConverter: BaseUnitsConverter = get().get()
            val innerGroupZView = BaseGroupZView(it, null, unitsConverter)
            val templateView = InspTemplateViewCreator.createInspTemplateView(
                innerGroupZView,
                canShowProgress = false
            )
            val template =
                if (textMedia == null) Template() else Template(medias = mutableListOf(textMedia))
            templateView.shouldHaveBackground = false
            templateView.loadTemplate(template)
            templateView.loopAnimation = false
            templateView.startPlaying(mayPlayMusic = false)
            innerGroupZView
        },
        Modifier
            .fillMaxSize()
    ) {
    }
}

//Feedback dialog
@Composable
private fun Feedback(onFeedback: (String?) -> Unit) {
    var feedbackText by remember { mutableStateOf("") }
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            LocalContext.current.getString(app.inspiry.projectutils.R.string.rating_dialog_feedback_title),
            style = MaterialTheme.typography.h5,
            modifier = Modifier
                .padding(top = 15.dp)
        )
        Box(
            modifier = Modifier
                .padding(start = 10.dp, top = 10.dp, end = 10.dp)
                .fillMaxWidth()
                .height(250.dp),
            contentAlignment = Alignment.Center


        ) {
            OutlinedTextField(
                value = feedbackText,
                onValueChange = {
                    feedbackText = it
                },
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = LocalColors.current.focusedFeedbackBorder.toCColor(),
                    unfocusedBorderColor = LocalColors.current.unfocusedFeedbackBorder.toCColor()
                ),
                modifier = Modifier
                    .padding(10.dp)
                    .fillMaxWidth()
                    .height(250.dp)
            )
            Text(
                text = if (feedbackText == "") LocalContext.current.getString(app.inspiry.projectutils.R.string.rating_dialog_suggestions) else "",
                color = LocalColors.current.feedbackHintText.toCColor(),
                modifier = Modifier
                    .padding(15.dp)
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            TextButton(onClick = {
                onFeedback(null)
            }, modifier = Modifier.padding(start = 14.dp)) {
                Text(
                    LocalContext.current.getString(app.inspiry.projectutils.R.string.rating_dialog_cancel),
                    color = LocalColors.current.cancelButtonText.toCColor()
                )
            }
            TextButton(onClick = {
                onFeedback(feedbackText)
            }, modifier = Modifier.padding(end = 14.dp)) {
                Text(
                    LocalContext.current.getString(app.inspiry.projectutils.R.string.rating_dialog_submit),
                    color = LocalColors.current.submitButtonText.toCColor()
                )
            }
        }
    }
}
