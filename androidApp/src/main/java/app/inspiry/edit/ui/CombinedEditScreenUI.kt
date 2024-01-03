package app.inspiry.edit.ui

import android.app.Activity
import android.content.Intent
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.AlertDialog
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Alignment.Companion.TopCenter
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import app.inspiry.MR
import app.inspiry.activities.MainActivity
import app.inspiry.core.data.InspResponseData
import app.inspiry.core.data.InspResponseError
import app.inspiry.core.data.InspResponseLoading
import app.inspiry.core.media.Template
import app.inspiry.core.media.TemplateFormat
import app.inspiry.edit.EditActivity.Companion.EDIT_PREVIEW_ANIMATION_TIME
import app.inspiry.edit.EditActivity.Companion.EXTRA_RETURN_TO_MAIN_ACT_ON_CLOSE
import app.inspiry.edit.EditScreenState
import app.inspiry.edit.EditViewModel
import app.inspiry.preview.PreviewScreen
import app.inspiry.utilities.toCColor
import app.inspiry.views.group.BaseGroupZView
import app.inspiry.views.template.InspTemplateViewAndroid
import com.google.accompanist.insets.navigationBarsWithImePadding
import com.google.accompanist.insets.statusBarsPadding
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import org.koin.java.KoinJavaComponent.getKoin

val LocalColors = compositionLocalOf { EditColorsLight() }
val LocalDimens = compositionLocalOf { EditDimensPhone() }

@Composable
fun CombinedEditScreenUI(viewModel: EditViewModel) {
    val activity = (LocalContext.current as? Activity)
    var showSaveConfirmationDialog by remember { mutableStateOf(false) }
    val screenMode by viewModel.editScreenState.collectAsState()
    val transition by animateFloatAsState(
        if (screenMode == EditScreenState.EDIT) 1f else 0f,
        animationSpec = tween(durationMillis = EDIT_PREVIEW_ANIMATION_TIME, easing = LinearEasing)
    )

    viewModel.setTemplateCenterGravity(1f - transition)

    val back by animateColorAsState(
        if (screenMode == EditScreenState.PREVIEW) Color.Black else Color.Transparent,
        animationSpec = tween(durationMillis = EDIT_PREVIEW_ANIMATION_TIME, easing = LinearEasing)
    )
    val systemUiController = rememberSystemUiController()

    systemUiController.setStatusBarColor(
        color = back,
        darkIcons = back.alpha < 0.5
    )

    fun backAction(withoutSaving: Boolean = false) {
        if (!viewModel.backAction()) return
        if (!withoutSaving && viewModel.showConfirmationIsNeed(showSaveConfirmationDialog)) {
            showSaveConfirmationDialog = true
            return
        }

        if (activity?.intent?.getBooleanExtra(EXTRA_RETURN_TO_MAIN_ACT_ON_CLOSE, false) == true) {
            activity.startActivity(Intent(activity, MainActivity::class.java))
            activity.finish()
        } else
            activity?.finish()
    }

    BackHandler {
        backAction()
    }
    if (showSaveConfirmationDialog) {
        AlertDialog(
            onDismissRequest = {
                showSaveConfirmationDialog = false
            },
            title = {
                Text(
                    modifier = Modifier.padding(bottom = 20.dp),
                    text = stringResource(id = MR.strings.save_project_title.resourceId),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.onExitWithoutSave()
                        backAction(withoutSaving = true)

                    }) {
                    Text(
                        text = stringResource(id = MR.strings.save_project_negative.resourceId).uppercase(),
                        fontSize = 14.sp,
                        color = LocalColors.current.saveConfirmationNegativeButton.toCColor()
                    )
                }
            },
            dismissButton = {
                TextButton(

                    onClick = {
                        backAction()
                    }) {
                    Text(
                        text = stringResource(id = MR.strings.save_project_positive.resourceId).uppercase(),
                        fontSize = 14.sp
                    )
                }
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsWithImePadding()
            .background(back),
        contentAlignment = TopCenter
    ) {

        ComposeTemplateView(
            modifier = Modifier,
            viewModel = viewModel
        )
        if (screenMode == EditScreenState.PREVIEW || transition != 1f) {
            PreviewScreen(
                modifier = Modifier.alpha(1f - transition),
                viewModel = viewModel.previewViewModel!!,
                settings = getKoin().get()
            )
        }

        if (screenMode == EditScreenState.RENDER && transition != 1f) {
            TODO("not implemented")
        }

        if (screenMode == EditScreenState.EDIT || transition != 0f) {

            EditScreenUI(modifier = Modifier.alpha(transition), viewModel = viewModel) {
                backAction()
            }
        }

    }
}

@Composable
fun ComposeTemplateView(
    modifier: Modifier = Modifier,
    viewModel: EditViewModel?
) {
    val template = viewModel!!.template.collectAsState()
    val aspectRatio by viewModel.templateFormatState.collectAsState()
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
                if (viewModel.isInEditMode) {
                    viewModel.templateView.changeSelectedView(null)
                }
            }
            .fillMaxSize(), contentAlignment = TopCenter
    ) {

        with(template.value) {
            when (this) {
                is InspResponseData<Template> -> {
                    AndroidTemplate(
                        modifier = Modifier,
                        (viewModel.templateView as InspTemplateViewAndroid).innerView.apply {
                            aspectRatio?.let { applyFormat(it) }
                            viewModel.templateView.refreshTemplateTransform()

                        }, this.data.format
                    )
                }
                is InspResponseError<Template> -> {
                    val message = this.throwable.stackTraceToString()
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(vertical = 64.dp, horizontal = 10.dp),
                        contentAlignment = Center
                    )
                    {
                        Text(message)
                    }
                }
                is InspResponseLoading<Template> -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Center)
                    {
                        CircularProgressIndicator()
                    }
                }
                else -> {
                }
            }
        }
    }
}

@Composable
fun AndroidTemplate(
    modifier: Modifier,
    innerGroupZView: BaseGroupZView,
    templateFormat: TemplateFormat
) {

    AndroidView(
        modifier = modifier,
        factory = {
            (innerGroupZView.parent as? ConstraintLayout)?.removeAllViews()
            val rootConstraint = ConstraintLayout(it)
            rootConstraint.id = View.generateViewId()

            rootConstraint.layoutParams = ViewGroup.LayoutParams(
                MATCH_PARENT,
                MATCH_PARENT
            )

            rootConstraint.addView(innerGroupZView, ConstraintLayout.LayoutParams(
                MATCH_PARENT, 0
            ).apply {
                val ratio = when (templateFormat) {
                    TemplateFormat.post -> "H, 4:5"
                    TemplateFormat.square -> "H, 1:1"
                    TemplateFormat.horizontal -> "H, 16:9"
                    TemplateFormat.story -> "H, 9:16"
                }
                dimensionRatio = ratio
            }
            )
            val set = ConstraintSet()
            set.clone(rootConstraint)
            set.connect(innerGroupZView.id, ConstraintSet.TOP, rootConstraint.id, ConstraintSet.TOP)
            set.applyTo(rootConstraint)
            return@AndroidView rootConstraint
        }
    )
}

