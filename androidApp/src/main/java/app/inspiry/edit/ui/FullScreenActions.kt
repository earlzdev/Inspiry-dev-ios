package app.inspiry.edit.ui

import android.content.Intent
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.lifecycleScope
import app.inspiry.core.analytics.AnalyticsManager
import app.inspiry.core.data.UserSavedTemplatePath
import app.inspiry.core.manager.DebugManager
import app.inspiry.core.serialization.TemplateSerializerBase
import app.inspiry.core.util.PickMediaResult
import app.inspiry.edit.EditViewModel
import app.inspiry.edit.ExportActionForPremium
import app.inspiry.edit.ExportActionSave
import app.inspiry.edit.instruments.FullScreenTools
import app.inspiry.edit.instruments.InstrumentAdditional
import app.inspiry.edit.instruments.PickedMediaType
import app.inspiry.export.mainui.ExportActivity
import app.inspiry.export.viewmodel.KEY_IMAGE_ELSE_VIDEO
import app.inspiry.featurepromo.RemoveBgPromoActivity
import app.inspiry.helpers.*
import app.inspiry.removebg.RemovingBgViewModel
import app.inspiry.stickers.helpers.StickersActivityResult
import app.inspiry.subscribe.ui.SubscribeActivity
import app.inspiry.utils.Constants
import app.inspiry.utils.ImageUtils
import app.inspiry.utils.putOriginalTemplateData
import app.inspiry.utils.putTemplatePath
import app.inspiry.views.media.InspMediaView
import app.inspiry.views.text.InspTextView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import org.koin.java.KoinJavaComponent.getKoin

@Composable
fun FullScreenActionsMain(model: EditViewModel) {

    val fullScreenTools by model.instrumentsManager.fullScreenTools.collectAsState()
    val scope = rememberCoroutineScope()

    val stickersLauncher = stickersLauncher(viewModel = model)
    val textAnimLauncher = textAnimLauncher(viewModel = model)
    val musicLauncher = musicLauncher(viewModel = model)
    val matisseLauncher = mediaPickerLauncher(viewModel = model)
    val removeBGLauncher = removeBgLauncher(viewModel = model)
    val jsonLauncher = editJsonLauncher(viewModel = model)
    val logoLauncher = logoLauncher(viewModel = model)
    val pickFrameLauncher = pickMediaLauncher(viewModel = model)
    val activity = LocalContext.current as AppCompatActivity


    when (fullScreenTools) {
        FullScreenTools.ADD_LOGO -> {
            model.setSingleMediaConfig()
            model.instrumentsManager.resetFullScreenTool()
            logoLauncher.launch(Unit)
        }
        FullScreenTools.PICK_SINGLE_MEDIA -> {
            model.instrumentsManager.resetFullScreenTool()
            LaunchedEffect(Unit) {
                if (ImageUtils.isMediaChooserPrepared(activity)) {
                    pickFrameLauncher.launch(Unit)
                }
            }
        }
        FullScreenTools.STICKERS -> {
            model.instrumentsManager.resetFullScreenTool()
            stickersLauncher.launch(Unit)
        }
        FullScreenTools.TEXT_ANIM -> {
            model.instrumentsManager.resetFullScreenTool()
            textAnimLauncher.launch(null)
        }
        FullScreenTools.MUSIC -> {
            model.instrumentsManager.resetFullScreenTool()
            musicLauncher.launch(null)
        }
        FullScreenTools.REMOVE_BG_PROMO -> {
            val context = LocalContext.current
            context.startActivity(Intent(context, RemoveBgPromoActivity::class.java))
        }
        FullScreenTools.REMOVE_BG -> {
            model.instrumentsManager.resetFullScreenTool()
            val mediasToRemoveBg = model.getMediaViewsToRemoveBgAfterPickedFromGallery()
            if (mediasToRemoveBg.isNotEmpty()) {
                if (model.canRemoveBgOrOpenPromo()) {
                    val list = arrayListOf<String>()
                    mediasToRemoveBg.mapTo(list) { it.media.originalSource!! }
                    removeBGLauncher.launch(
                        RemoveBgActivityData(
                            RemovingBgViewModel.SOURCE_TEMPLATE,
                            list
                        )
                    )
                } else {
                    model.instrumentsManager.removeBGPromo()
                }
            } else {
                if (model.canRemoveBgOrOpenPromo()) {
                    removeBGLauncher.launch(
                        RemoveBgActivityData(
                            RemovingBgViewModel.SOURCE_INSTRUMENT,
                            arrayListOf((model.templateView.selectedView as InspMediaView).media.originalSource!!)
                        )
                    )
                } else {
                    model.instrumentsManager.removeBGPromo()
                }
            }
        }
        FullScreenTools.PICK_IMAGE -> {
            model.instrumentsManager.resetFullScreenTool()
            LaunchedEffect(Unit) {
                if (ImageUtils.isMediaChooserPrepared(activity, model.getPickImageConfig())) {
                    matisseLauncher.launch(Unit)
                }
            }
        }
        FullScreenTools.REPLACE -> {
            model.setSingleMediaConfig()

                model.instrumentsManager.resetFullScreenTool()

                LaunchedEffect(Unit) {
                    if (ImageUtils.isMediaChooserPrepared(activity, model.getPickImageConfig())) {
                        if (model.templateView.selectedView?.isLogo != true) matisseLauncher.launch(Unit)
                        else logoLauncher.launch(Unit)
                    }
                }

        }
        FullScreenTools.SAVING -> {
            model.instrumentsManager.resetFullScreenTool()
            LaunchedEffect(Unit) {
                scope.launch {
                    val data =
                        model.exportButtonClick(
                            startRenderWithoutPurchase = DebugManager.isDebug,
                            ImageUtils.getPermissionController(activity)
                        )
                    when (data) {
                        is ExportActionSave -> {
                            val intent = Intent(activity, ExportActivity::class.java)
                                .putTemplatePath(data.templatePath)
                                .putOriginalTemplateData(data.originalTemplateData)

                            if (data.isStatic) intent.putExtra(KEY_IMAGE_ELSE_VIDEO, true)
                            activity.startActivity(intent)

                        }
                        is ExportActionForPremium -> {
                            activity.startActivity(
                                Intent(activity, SubscribeActivity::class.java)
                                    .putExtra(Constants.EXTRA_SOURCE, data.source)
                            )
                        }
                        else -> {
                            //nothing if the user has not granted write access
                            //show message?
                        }
                    }
                }
            }
        }
        FullScreenTools.DEBUG_EDIT_SAVED -> {
            model.instrumentsManager.resetFullScreenTool()
            jsonLauncher.launch(EditJsonParams(model.templatePath as UserSavedTemplatePath, null))
        }
        FullScreenTools.DEBUG_EDIT_JSON -> {

            val json: Json = getKoin().get()

            LaunchedEffect(Unit) {
                scope.launch {
                    val templateJson = withContext(Dispatchers.Default) {
                        json.encodeToString(
                            TemplateSerializerBase(skipTemporaryMedia = false),
                            model.templateView.template
                        )
                    }

                    model.instrumentsManager.resetFullScreenTool()
                    jsonLauncher.launch(
                        EditJsonParams(
                            model.templatePath as UserSavedTemplatePath,
                            templateJson
                        )
                    )
                }
            }
        }
        FullScreenTools.SUBSCRIBE -> {
            model.instrumentsManager.resetFullScreenTool()
            val source = when (
                val innerState =
                    model.instrumentsManager.instrumentsState.value.currentAdditionalInstrument
            ) {
                InstrumentAdditional.FORMAT -> "formats"
                InstrumentAdditional.FONT -> "font_click"
                else -> {
                    if (!DebugManager.isDebug)
                        innerState?.name ?: "unknown"
                    else throw IllegalStateException("not implemented state for subscribe screen, source: ${innerState?.name}")
                }
            }
            activity.startActivity(
                Intent(activity, SubscribeActivity::class.java)
                    .putExtra(Constants.EXTRA_SOURCE, source)
            )
        }
        else -> {}
    }
}

@Composable
private fun stickersLauncher(viewModel: EditViewModel) =
    rememberLauncherForActivityResult(contract = StickersActivityResult()) { result ->
        viewModel.onStickerResult(result)
    }

@Composable
private fun mediaPickerLauncher(viewModel: EditViewModel) =
    rememberLauncherForActivityResult(contract = MatisseActivityResult()) { matisseResult ->
        viewModel.whenTemplateInitializedCancelable {
            viewModel.onImagePicked(matisseResult)
        }
    }


@Composable
private fun textAnimLauncher(viewModel: EditViewModel): ManagedActivityResultLauncher<String?, String?> {
    val context = LocalContext.current
    return rememberLauncherForActivityResult(contract = PickTextAnimationActivityResult()) { result ->
        (context as AppCompatActivity).lifecycleScope.launch {
            val currentText = viewModel.templateView.selectedView as? InspTextView
            viewModel.onTextPicked(currentText, textAnimationResult = result)
        }

    }
}

@Composable
private fun editJsonLauncher(viewModel: EditViewModel): ManagedActivityResultLauncher<EditJsonParams, Boolean> {
    return rememberLauncherForActivityResult(contract = EditJsonActivityResult()) { result ->
        if (result) {
            viewModel.loadTemplatePath()
        }

    }
}

@Composable
private fun removeBgLauncher(viewModel: EditViewModel): ManagedActivityResultLauncher<RemoveBgActivityData, List<PickMediaResult>?> {
    return rememberLauncherForActivityResult(contract = RemovingBgActivityResult()) { result ->
        if (result != null) {
            viewModel.whenTemplateInitializedCancelable {

                if (viewModel.templateView.selectedView != null && result.size == 1) {
                    viewModel.insertRemovedBgView(resultItem = result.first())
                } else {
                    viewModel.insertRemovedBgBatch(result)
                }
            }
        }

    }
}

@Composable
private fun musicLauncher(viewModel: EditViewModel) =
    rememberLauncherForActivityResult(contract = PickMusicActivityResult()) { result ->
        viewModel.setMusic(result.music)
    }

@Composable
private fun pickMediaLauncher(viewModel: EditViewModel) =
    rememberLauncherForActivityResult(contract = MatisseActivityResult()) { matisseResult ->
        matisseResult.let {
            val analyticsManager: AnalyticsManager = getKoin().get()
            analyticsManager.onMediaAdded(
                isLogo = true,
                isVideo = it.first().type == PickedMediaType.VIDEO
            )
            viewModel.whenTemplateInitializedCancelable {
                viewModel.onMediaPicked(it)
            }
        }
    }

@Composable
private fun logoLauncher(viewModel: EditViewModel) =
    rememberLauncherForActivityResult(contract = LogoActivityResult()) { result ->
        viewModel.whenTemplateInitializedCancelable {
            result?.let {
                val analyticsManager: AnalyticsManager = getKoin().get()
                val config = viewModel.getPickImageConfig()
                if (config.resultViewIndex < 0) {
                    analyticsManager.onMediaAdded(isLogo = true, isVideo = false)
                    viewModel.onMediaPicked(
                        listOf(
                            result
                        ), isLogo = true
                    )
                } else {
                    viewModel.onImagePicked(mutableListOf(it))
                }
            }
        }
    }