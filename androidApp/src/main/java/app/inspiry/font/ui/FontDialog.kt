package app.inspiry.font.ui

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.inspiry.R
import app.inspiry.core.data.InspResponseData
import app.inspiry.core.data.InspResponseError
import app.inspiry.core.data.InspResponseLoading
import app.inspiry.core.log.GlobalLogger
import app.inspiry.core.util.getExt
import app.inspiry.edit.instruments.font.FontsViewModel
import app.inspiry.font.helpers.PlatformFontObtainerImpl
import app.inspiry.font.model.FontPath
import app.inspiry.font.model.InspFontStyle
import app.inspiry.font.model.UploadedFontPath
import app.inspiry.font.provider.FontsManager
import app.inspiry.font.util.FontUtils
import app.inspiry.font.util.getFontName
import app.inspiry.font.util.getSavedFontsDir
import app.inspiry.subscribe.ui.SubscribeActivity
import app.inspiry.utilities.toCColor
import app.inspiry.utils.Constants
import app.inspiry.utils.autoScroll
import app.inspiry.utils.dpToPixels
import kotlinx.coroutines.launch
import java.io.File
import java.util.*

private val LocalColors = compositionLocalOf { FontDialogColorsDark() }
private val LocalDimens = compositionLocalOf { FontDialogDimensPhone() }

@Composable
fun FontDialogMain(viewModel: FontsViewModel) {
    Column(
        Modifier
            .height(LocalDimens.current.panelHeight.dp)
            .background(LocalColors.current.backgroundColor.toCColor())
            .fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally
    ) {


        val fontPath by viewModel.currentFontPath.collectAsState()
        val fontStyle by viewModel.currentFontStyle.collectAsState()
        val text by viewModel.currentText.collectAsState()

        PanelStyle(fontPath, text, fontStyle, viewModel,
            onSelectedChange = {
                viewModel.onFontStyleChange(it)
            })

        val categoriesData = viewModel.fontsManager.allCategories

        val selectedCategory: Int by viewModel.currentCategoryIndex.collectAsState()

        val selectedFont = fontPath.path

        val fontsListState = rememberLazyListState()
        val composeScope = rememberCoroutineScope()

        viewModel.onInitialFontIndexChange = {
            composeScope.launch {
                fontsListState.scrollToItem(it)
            }
        }

        FontCategories(
            categories = categoriesData,
            selectedCategoryIndex = selectedCategory,
            onSelectedChange = {
                if (selectedCategory != it) {
                    composeScope.launch { fontsListState.scrollToItem(0) }
                    viewModel.currentCategoryIndex.value = it
                }
            })

        val isUpload =
            categoriesData[selectedCategory] == FontsManager.CATEGORY_ID_UPLOAD


        val fonts by viewModel.currentFonts.collectAsState()

        when (fonts) {
            is InspResponseLoading -> {
                Box(
                    Modifier
                        .height(LocalDimens.current.fontsListHeight.dp)
                        .fillMaxWidth(), contentAlignment = Alignment.Center
                ) {
                    // the loading is very fast. we don't need it.
                    CircularProgressIndicator(color = LocalColors.current.fontTextActive.toCColor())
                }
            }
            is InspResponseError -> {
                Box(
                    Modifier
                        .height(LocalDimens.current.fontsListHeight.dp)
                        .fillMaxWidth(), contentAlignment = Alignment.Center
                ) {
                    BasicText(text = "Couldn't load fonts")
                }
            }
            else -> {

                FontsList(
                    fonts = (fonts as InspResponseData).data.fonts, isUpload, viewModel = viewModel,
                    selectedFont = selectedFont, fontsListState
                )
            }
        }
    }
}

@Composable
private fun PanelStyle(
    font: FontPath,
    text: String,
    selectedStyle: InspFontStyle,
    viewModel: FontsViewModel,
    onSelectedChange: (InspFontStyle) -> Unit
) {
    Row(
        Modifier
            .wrapContentWidth()
            .height(LocalDimens.current.stylesSectionHeight.dp)
    ) {
        var buttonsCount = 0
        if (font.supportsBold(viewModel.platformFontPathProvider)) {
            buttonsCount++
            ButtonStyle(
                selectedStyle = selectedStyle,
                currentStyle = InspFontStyle.bold,
                text = "B",
                fontWeight = FontWeight.Bold,
                fontStyle = null,
                onSelectedChange = { onSelectedChange(it) })
        }

        if (font.supportsItalic(viewModel.platformFontPathProvider)) {
            buttonsCount++
            ButtonStyle(
                selectedStyle = selectedStyle,
                currentStyle = InspFontStyle.italic,
                text = "I",
                fontWeight = FontWeight.Normal,
                fontStyle = FontStyle.Italic,
                onSelectedChange = { onSelectedChange(it) })
        }

        if (font.supportsLight(viewModel.platformFontPathProvider)) {
            buttonsCount++
            ButtonStyle(
                selectedStyle = selectedStyle,
                currentStyle = InspFontStyle.light,
                text = "L",
                fontWeight = FontWeight.Light,
                fontStyle = null,
                onSelectedChange = { onSelectedChange(it) })
        }

        Box(
            modifier = Modifier
                .padding(start = if (buttonsCount > 0) LocalDimens.current.stylesBoxPaddingHorizontal.dp * 6f else 0.dp)
                .padding(top = LocalDimens.current.stylesBoxPaddingTop.dp)
                .wrapContentWidth()
                .height(LocalDimens.current.stylesBoxHeight.dp)
                .background(LocalColors.current.styleBg.toCColor(), RoundedCornerShape(4.dp))
                .border(
                    LocalDimens.current.stylesBoxBorderThickness.dp,
                    LocalColors.current.styleBorderActive.toCColor(),
                    RoundedCornerShape(LocalDimens.current.stylesBoxCornerRadius.dp)
                )
                .clickable {
                    viewModel.onClickToggleCapsMode()
                },
            contentAlignment = Alignment.Center
        ) {
            BasicText(
                text = viewModel.textCaseHelper.setCaseBasedOnOther("Aa", text),
                modifier = Modifier
                    .wrapContentSize()
                    .padding(horizontal = LocalDimens.current.stylesBoxTextCasePaddingHorizontal.dp),
                style = TextStyle(
                    fontSize = LocalDimens.current.stylesFontSize.sp,
                    color = LocalColors.current.styleTextActive.toCColor(),
                    textAlign = TextAlign.Center
                ),
                maxLines = 1
            )

        }
    }
}

@Composable
private fun FontCategories(
    categories: List<String>,
    selectedCategoryIndex: Int,
    onSelectedChange: (Int) -> Unit
) {
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    LazyRow(
        Modifier
            .height(LocalDimens.current.categoryHeight.dp)
            .fillMaxWidth(),
        contentPadding = PaddingValues(
            horizontal = LocalDimens.current.categoryContentPadding.dp,
            vertical = 0.dp
        ),
        state = listState
    ) {

        itemsIndexed(
            items = categories,
            key = { index, item -> item + (selectedCategoryIndex == index) }) { index, item ->

            val isSelected = selectedCategoryIndex == index
            Box(
                modifier = Modifier
                    .wrapContentWidth()
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(LocalDimens.current.categoryBgClip.dp))
                    .background(
                        if (isSelected) LocalColors.current.categoryBgActive.toCColor() else
                            Color.Transparent
                    )
                    .clickable {
                        onSelectedChange(index)
                    }
                    .padding(horizontal = LocalDimens.current.categoryItemPaddingHorizontal.dp),
                contentAlignment = Alignment.Center
            ) {

                Text(
                    text = item.capitalize(Locale.ENGLISH),
                    color =
                    if (isSelected) LocalColors.current.categoryTextActive.toCColor() else LocalColors.current.categoryTextInactive.toCColor(),
                    fontSize = LocalDimens.current.categoryTextSize.sp,
                    fontWeight = FontWeight.Light
                )
            }
        }
        listState.autoScroll(scope = scope, scrollToIndex = selectedCategoryIndex)
    }
}

@Composable
private fun FontsList(
    fonts: List<FontPath>,
    isUpload: Boolean,
    viewModel: FontsViewModel,
    selectedFont: String?,
    listState: LazyListState
) {

    val selectedFontAlternative =
        if (selectedFont != null) viewModel.fontsManager.replaceCyrillic(selectedFont) else null

    val context = LocalContext.current
    val platformFontObtainer = PlatformFontObtainerImpl(
        context,
        viewModel.fontsManager,
        viewModel.platformFontPathProvider
    )

    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            if (uri != null) {
                val fontName = uri.getFontName(context) ?: "unknown"

                if (!FontUtils.isFont(fontName)) {
                    Toast.makeText(
                        context,
                        "Selected file is not font - ${fontName.getExt()}",
                        Toast.LENGTH_LONG
                    ).show()

                } else {
                    val outputFile = File(context.getSavedFontsDir(), fontName)
                    outputFile.parentFile?.mkdirs()

                    try {
                        outputFile.outputStream().use {
                            context.contentResolver.openInputStream(uri)?.copyTo(it)
                        }

                        // to verify that we don't have errors
                        val fontPath = UploadedFontPath(outputFile.absolutePath)
                        GlobalLogger.debug("fontInstrument") { "uploaded ${fontPath.displayName}" }
                        platformFontObtainer.getTypefaceFromPath(fontPath, InspFontStyle.regular)
                        viewModel.addUploadedFont(fontPath)

                    } catch (e: Exception) {
                        GlobalLogger.error("fontInstrument") { e.toString() }
                        Toast.makeText(
                            context,
                            "error to load font ${e.message}",
                            Toast.LENGTH_LONG
                        )
                            .show()
                    }
                }
            }
        }
    Box(
        modifier = Modifier
            .height(LocalDimens.current.fontsListHeight.dp)
            .fillMaxWidth(),
        contentAlignment = if (listState.layoutInfo.totalItemsCount == 1 && viewModel.currentCategoryIndex.value == 0) Alignment.Center else Alignment.CenterStart
    ) {

        val scope = rememberCoroutineScope()

        LazyRow(
            Modifier
                .fillMaxHeight()
                .wrapContentWidth(),
            contentPadding = PaddingValues(
                horizontal = LocalDimens.current.fontsListContentPadding.dp,
                vertical = 0.dp
            ),
            state = listState,
        ) {
            if (isUpload) {
                item(key = FontsManager.CATEGORY_ID_UPLOAD) {
                    Row(modifier = Modifier
                        .fillMaxHeight()
                        .wrapContentWidth()
                        .padding(end = 3.dp)
                        .clickable {
                            launcher.launch(arrayOf("*/*"))

                        }
                        .padding(horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Image(
                            modifier = Modifier.padding(end = 9.dp),
                            painter = painterResource(id = R.drawable.ic_fonts_upload),
                            contentDescription = "upload fonts"
                        )

                        BasicText(
                            text = stringResource(id = app.inspiry.projectutils.R.string.upload_font),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            style = TextStyle(
                                color = LocalColors.current.categoryTextInactive.toCColor(),
                                fontSize = LocalDimens.current.uploadLabelTextSize.sp
                            )
                        )

                    }
                }
            }

            itemsIndexed(fonts, key = { index, item -> item.path }) { index, item ->

                val isSelected =
                    FontsManager.isCurrentFont(
                        selectedFontAlternative,
                        item.path
                    ) || FontsManager.isCurrentFont(
                        selectedFont,
                        item.path
                    )

                val hasLicense by viewModel.licenseManager.hasPremiumState.collectAsState()
                val isAvailable = hasLicense || !item.forPremium
                val activity = LocalContext.current as AppCompatActivity
                Box(
                    modifier = Modifier
                        .wrapContentWidth()
                        .fillMaxHeight()
                        .clickable {

                            if (!isAvailable) {
                                viewModel.onPickedNewFont(null)
                            } else {
                                viewModel.onPickedNewFont(item)
                                listState.autoScroll(scope, index)
                            }
                        }
                        .padding(horizontal = LocalDimens.current.fontItemPaddingHorizontal.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val typeface = platformFontObtainer.getFromPathToastOnError(item,
                        viewModel.getStyleForDisplayedPath(item), onError = {
                            Toast.makeText(
                                LocalContext.current,
                                "Cant load typeface ${item.path}",
                                Toast.LENGTH_LONG
                            ).show()
                        })

                    BasicText(
                        text = item.displayName, maxLines = 1,
                        style = TextStyle(
                            color = if (isSelected) LocalColors.current.fontTextActive.toCColor() else LocalColors.current.fontTextInactive.toCColor(),
                            fontSize = LocalDimens.current.fontTextSize.sp,
                            fontFamily = FontFamily(typeface)
                        )
                    )
                }

                if (!isAvailable) {
                    Image(
                        modifier = Modifier
                            .fillMaxSize()
                            .offset(
                                x = -LocalDimens.current.fontLockOffsetY.dp,
                                y = LocalDimens.current.fontLockOffsetY.dp
                            ),
                        painter = painterResource(id = R.drawable.ic_fonts_lock),
                        contentDescription = "For premium only",
                        alignment = Alignment.TopEnd,
                        contentScale = ContentScale.Inside
                    )
                }
            }
        }
    }
}

@Composable
private fun ButtonStyle(
    selectedStyle: InspFontStyle, currentStyle: InspFontStyle, text: String,
    fontWeight: FontWeight, fontStyle: FontStyle?,
    onSelectedChange: (InspFontStyle) -> Unit
) {

    val isSelected = selectedStyle == currentStyle

    Box(
        modifier = Modifier
            .padding(horizontal = LocalDimens.current.stylesBoxPaddingHorizontal.dp)
            .padding(top = LocalDimens.current.stylesBoxPaddingTop.dp)
            .size(LocalDimens.current.stylesBoxWidth.dp, LocalDimens.current.stylesBoxHeight.dp)
            .background(
                LocalColors.current.styleBg.toCColor(),
                RoundedCornerShape(LocalDimens.current.stylesBoxCornerRadius.dp)
            )
            .border(
                LocalDimens.current.stylesBoxBorderThickness.dp,
                if (isSelected) LocalColors.current.styleBorderActive.toCColor() else LocalColors.current.styleBorderInactive.toCColor(),
                RoundedCornerShape(LocalDimens.current.stylesBoxCornerRadius.dp)
            )
            .clickable { onSelectedChange(currentStyle) }, contentAlignment = Alignment.Center
    ) {
        BasicText(
            text = text,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            style = TextStyle(
                color = if (isSelected) LocalColors.current.styleTextActive.toCColor() else LocalColors.current.styleTextInactive.toCColor(),
                textAlign = TextAlign.Center,
                fontStyle = fontStyle,
                fontWeight = fontWeight,
                fontSize = LocalDimens.current.stylesFontSize.sp,
            ),
            maxLines = 1
        )
    }
}
