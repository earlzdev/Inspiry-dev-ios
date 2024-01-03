package app.inspiry.edit.ui

import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import app.inspiry.MR
import app.inspiry.R
import app.inspiry.core.database.ExternalResourceDao
import app.inspiry.edit.EditTopbarAction
import app.inspiry.edit.EditViewModel
import app.inspiry.edit.EditWrapperHelper
import app.inspiry.font.helpers.PlatformFontObtainerImpl
import app.inspiry.utilities.toCColor
import app.inspiry.utils.dpToPixels
import app.inspiry.utils.toCommonSize
import app.inspiry.views.androidhelper.SelectedItemBorderAndroid
import app.inspiry.views.template.InspTemplateView
import app.inspiry.views.text.InspTextView
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import org.koin.androidx.compose.get
import org.koin.java.KoinJavaComponent
import kotlin.math.roundToInt


@Composable
fun EditScreenUI(modifier: Modifier, viewModel: EditViewModel, onBackAction: () -> Unit) {
    val editTextAction by viewModel.isKeyboardShown.collectAsState()

    FullScreenActionsMain(model = viewModel)

    Column(
        modifier = modifier
            .fillMaxHeight(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Column {

                EditTopBar(viewModel = viewModel) {
                    onBackAction()
                }

                Spacer(modifier = Modifier
                    .fillMaxSize()
                    .weight(0.5f)
                    .onGloballyPositioned { coordinates ->
                        viewModel.templateView.refreshTemplateTransform(
                            coordinates.size.toCommonSize(),
                            coordinates.positionInParent().y.roundToInt()
                        )

                    }
                )

                if (!editTextAction) BottomInstrumentsPanelUI(model = viewModel)
            }
        }
    }

    if (!editTextAction)
        EditWrapper(
            modifier = Modifier
                .fillMaxSize(),
            templateView = viewModel.templateView
        )

    AnimatedVisibility(
        visible = editTextAction,
        enter = fadeIn(animationSpec = tween(durationMillis = 200)),
        exit = fadeOut(animationSpec = tween(durationMillis = 200))
    ) {

        val statusBarColor by animateColorAsState(
            if (editTextAction) LocalColors.current.keyboardDoneBg.toCColor() else LocalColors.current.keyboardDoneBg.toCColor()
                .copy(alpha = 0f),
            animationSpec = tween(durationMillis = 200)
        )
        val systemUiController = rememberSystemUiController()
        systemUiController.setStatusBarColor(
            color = statusBarColor
        )

        EditTextView(
            inspTextView = viewModel.templateView.selectedView as? InspTextView,
            model = viewModel
        )
    }
}

@Composable
private fun EditWrapper(
    modifier: Modifier,
    templateView: InspTemplateView,
    externalResourceDao: ExternalResourceDao = get()
) {

    val composeScope = rememberCoroutineScope()

    val helper: EditWrapperHelper = remember {
        EditWrapperHelper(composeScope, templateView, 1.dpToPixels(), externalResourceDao)
    }

    AndroidSelectedItemBorder(modifier, helper)
}

@Composable
private fun AndroidSelectedItemBorder(modifier: Modifier, helper: EditWrapperHelper) {
    AndroidView(
        modifier = modifier,
        factory = {
            SelectedItemBorderAndroid(it, helper)
        })
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun EditTopBar(viewModel: EditViewModel, onClickBack: () -> Unit) {
    val rightButtonState by viewModel.getTopBarActionDisplayFlow()
        .collectAsState(initial = EditTopbarAction.EXPORT)
    val colors = app.inspiry.export.mainui.LocalColors.current
    MaterialTheme(colors = MaterialTheme.colors.copy(isLight = colors.isLight)) {
        Box(
            modifier = Modifier
                .padding(horizontal = LocalDimens.current.editTopBarHorizontalPadding.dp)
                .fillMaxWidth()
                .height(LocalDimens.current.topBarHeight.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                BackButton(modifier = Modifier
                    .clip(shape = RoundedCornerShape(LocalDimens.current.editTopBarTextRounding.dp))
                    .clickable {
                        onClickBack()
                    }
                    .padding(LocalDimens.current.editTopBarTextPadding.dp))
                val activity = LocalContext.current as AppCompatActivity
                when (rightButtonState) {
                    EditTopbarAction.EXPORT -> {
                        Box(
                            modifier = Modifier
                                .clip(shape = RoundedCornerShape(10.dp))
                                .clickable { viewModel.instrumentsManager.saveButtonAction() },
                            contentAlignment = Alignment.BottomEnd
                        ) {
                            Text(
                                text = stringResource(id = MR.strings.share.resourceId),
                                color = LocalColors.current.topBarText.toCColor(),
                                fontSize = LocalDimens.current.topBarTextSize.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .clip(shape = RoundedCornerShape(LocalDimens.current.editTopBarTextRounding.dp))
                                    .padding(LocalDimens.current.editTopBarTextPadding.dp)
                            )
                        }
                    }
                    EditTopbarAction.DONE -> {
                        Text(
                            text = stringResource(id = MR.strings.done.resourceId),
                            color = LocalColors.current.sharePremiumText.toCColor(),
                            fontSize = LocalDimens.current.topBarTextSize.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clickable {
                                    viewModel.removeBottomPanel()
                                }
                                .padding(vertical = 10.dp),

                            )
                    }
                    EditTopbarAction.SUBSCRIBE -> {
                        Box(
                            contentAlignment = Alignment.BottomEnd
                        ) {
                            Text(
                                text = stringResource(id = MR.strings.share.resourceId),
                                color = LocalColors.current.sharePremiumText.toCColor(),
                                fontSize = LocalDimens.current.topBarTextSize.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .clip(shape = RoundedCornerShape(LocalDimens.current.editTopBarTextRounding.dp))
                                    .background(LocalColors.current.sharePremiumBg.toCColor())
                                    .combinedClickable(
                                        enabled = true,
                                        onClick = {
                                            viewModel.instrumentsManager.saveButtonAction()
                                        }
                                    )
                                    .padding(horizontal = 10.dp, vertical = 3.dp)
                            )
                            Image(
                                painterResource(id = R.drawable.ic_pro_share),
                                contentDescription = null,
                                modifier = Modifier.offset(x = 5.dp, y = 5.dp)
                            )
                        }
                    }
                }
            }
            Image(
                painterResource(id = R.drawable.ic_preview_template), contentDescription = null,
                modifier = Modifier
                    .clickable {
                        viewModel.toPreviewMode()

                    }
                    .padding(all = 10.dp)
            )
        }
    }
}

@Composable
private fun BackButton(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(9.dp)
    ) {

        Image(
            painterResource(R.drawable.ic_arrow_back_edit),
            contentDescription = null, contentScale = ContentScale.Inside
        )

        Text(
            stringResource(
                app.inspiry.projectutils.R.string.back
            ),
            color = app.inspiry.export.mainui.LocalColors.current.topBarText.toCColor(),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun EditTextView(
    inspTextView: InspTextView?,
    model: EditViewModel
) {
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    var valueState by remember {
        mutableStateOf(
            TextFieldValue(
                inspTextView?.media?.text ?: "",
                selection = TextRange(inspTextView?.media?.text?.length ?: 0)
            )
        )
    }
    val lifecycleOwner = LocalLifecycleOwner.current

    val fontObtainerImpl = PlatformFontObtainerImpl(
        context = LocalContext.current,
        fontsManager = KoinJavaComponent.getKoin().get(),
        platformFontPathProvider = KoinJavaComponent.getKoin().get()
    )
    val typeface = fontObtainerImpl.getTypefaceFromFontData(
        inspTextView?.media?.font
    )

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }) { }
            .background(
                LocalColors.current.editTextBack
                    .toCColor()
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        KeyboardHeaderDone(onBack = { model.isKeyboardShown.value = false }) {
            inspTextView?.let { model.editTextDone(it, valueState.text) }
        }
        Box(modifier = Modifier.weight(0.5f), contentAlignment = Alignment.Center) {
            BasicTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 15.dp)
                    .focusRequester(focusRequester),
                value = valueState,
                textStyle = TextStyle(
                    color = Color.Black,
                    fontFamily = FontFamily(typeface),
                    textAlign = TextAlign.Center,
                    fontSize = 20.sp
                ),
                onValueChange = {
                    valueState = it
                },
            )
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE) {
                model.isKeyboardShown.value = false
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            keyboardController?.hide()
        }
    }

}

@Composable
private fun KeyboardHeaderDone(onBack: () -> Unit, onDone: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(36.dp)
            .background(LocalColors.current.keyboardDoneBg.toCColor()),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        BackButton(modifier = Modifier
            .padding(start = 24.dp)
            .clip(shape = RoundedCornerShape(LocalDimens.current.editTopBarTextRounding.dp))
            .clickable { onBack() }
            .padding(LocalDimens.current.editTopBarTextPadding.dp))

        Text(
            modifier = Modifier
                .padding(end = 24.dp)
                .clip(shape = RoundedCornerShape(LocalDimens.current.editTopBarTextRounding.dp))
                .clickable { onDone() }
                .padding(LocalDimens.current.editTopBarTextPadding.dp),
            text = stringResource(id = MR.strings.done.resourceId),
            fontSize = 16.sp,
            color = LocalColors.current.keyboardDoneColor.toCColor(),
            fontWeight = FontWeight.Bold

        )
    }
}