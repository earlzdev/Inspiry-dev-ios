package app.inspiry.edit.instruments.ui

import android.app.Activity
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.toUpperCase
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import app.inspiry.MR
import app.inspiry.R
import app.inspiry.core.ui.CommonMenuItem
import app.inspiry.core.util.ImageUtil
import app.inspiry.core.util.SharedConstants
import app.inspiry.edit.instruments.PickedMediaType
import app.inspiry.edit.instruments.color.*
import app.inspiry.edit.instruments.color.ColorDialogPage.*
import app.inspiry.edit.instruments.color.PaletteItems.Companion.COLOR_PICKER_ITEM
import app.inspiry.edit.instruments.color.PaletteItems.Companion.REMOVE_ITEM
import app.inspiry.helpers.MatisseActivityResult
import app.inspiry.music.android.ui.InspSlider
import app.inspiry.palette.model.PaletteLinearGradient
import app.inspiry.utilities.toCColor
import app.inspiry.utils.ImageUtils
import app.inspiry.utils.autoScroll
import app.inspiry.utils.dpToPixels
import app.inspiry.utils.getDrawable
import app.inspiry.video.parseAssetsPathForAndroid
import app.inspiry.views.aspectRatioWithBounds
import app.inspiry.views.media.ColorFilterMode
import app.inspiry.views.media.stringResource
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.jaredrummler.android.colorpicker.ColorPickerDialog
import com.jaredrummler.android.colorpicker.ColorPickerDialogListener
import dev.icerock.moko.resources.StringResource
import kotlinx.coroutines.launch
import org.koin.android.ext.android.get

@Composable
fun ColorPanel(model: ColorDialogViewModel) {
    with(model) {

        val page by selectedPage.collectAsState()
        val currentContext = LocalContext.current
        if (colorPickerShow == null) colorPickerShow = { layer ->
            val dialog = ColorPickerDialog.newBuilder()
                .setShowAlphaSlider(false)
                .create()
            dialog.colorPickerDialogListener =
                object : ColorPickerDialogListener {

                    override fun onColorChanged(dialogId: Int, color: Int) {
                        onPickColor(layer, color)
                    }

                    override fun onColorSelected(dialogId: Int, color: Int) {
                        onPickColor(layer, color)
                    }

                    override fun onDialogDismissed(dialogId: Int) {
                        onPageSelected(COLOR)
                    }
                }

            dialog.show(
                (currentContext as FragmentActivity).supportFragmentManager,
                "color_picker_dialog"
            )
        }

        ColorChangeDialogCompose(
            viewModel = model,
            selected = page,
            modifier = Modifier.animateContentSize(
                animationSpec = tween(
                    350,
                    easing = LinearEasing
                )
            )
        )
    }
}

private val LocalColors =
    compositionLocalOf<TextPaletteDialogColors> { TextColorDialogLightColors() }
private val LocalDimens =
    compositionLocalOf<TextPaletteDialogDimens> { TextColorDialogDimensPhone() }

@Composable
private fun ColorChangeDialogCompose(
    modifier: Modifier = Modifier,
    viewModel: ColorDialogViewModel? = null,
    selected: ColorDialogPage,
) {

    val page by viewModel?.selectedPage?.collectAsState() ?: remember { mutableStateOf(selected) }

    Column(
        modifier
            .clickable(false) {}
            .background(LocalColors.current.background.toCColor())
            .fillMaxWidth()
            .padding(
                top = LocalDimens.current.panelTopPadding.dp
            ),
        verticalArrangement = Arrangement.Center
    ) {
        TopSelector(viewModel = viewModel, selected = page) {
            viewModel?.onPageSelected(it)
        }

        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.Top) {
            Spacer(Modifier.height(LocalDimens.current.itemsSpacer.dp * 0.7f))
            when (page) {
                COLOR -> ColorItemsGroup(viewModel = viewModel)
                GRADIENT -> GradientItemsGroup(viewModel = viewModel)
                PALETTE -> PaletteItemsGroup(viewModel = viewModel)
                OPACITY -> OpacitySlidersGroup(viewModel = viewModel)
                IMAGE -> ImageSelector(viewModel = viewModel)
                ROUNDNESS -> TopSliders(viewModel = viewModel)
                else -> {
                }
            }
        }
    }
}

@Composable
private fun matisseLauncher(viewModel: ColorDialogViewModel?, action: (Uri) -> Unit) =
    rememberLauncherForActivityResult(contract = MatisseActivityResult()) { matisseResult ->
        if (matisseResult.isNotEmpty())
            matisseResult[0].let {
                val isVideo = it.type == PickedMediaType.VIDEO
                viewModel?.onSingleMediaSelected(
                    it.uri,
                    isVideo
                )
                action(Uri.parse(it.uri))
            }
    }

@Composable
private fun ImageSelector(viewModel: ColorDialogViewModel?) {
    var result by remember {
        mutableStateOf(
            viewModel?.getCurrentImageBackground()
                ?.let { Uri.parse(it.parseAssetsPathForAndroid()) })
    }

    val launcher = matisseLauncher(viewModel = viewModel) { result = it }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    Row(
        modifier = Modifier
            .height(130.dp)
            .fillMaxWidth()
            .padding(horizontal = 15.dp, vertical = 10.dp)
    ) {
        Column(
            modifier = Modifier
                .clip(shape = RoundedCornerShape(10.dp))
                .background(color = LocalColors.current.placeholderBackground.toCColor())
                .clickable {
                    scope.launch {
                        if (ImageUtils.isMediaChooserPrepared(context as AppCompatActivity)) {
                            launcher.launch(Unit)
                        }
                    }
                }
                .aspectRatioWithBounds(
                    SharedConstants.ASPECT_10by16,
                    matchHeightConstraintsFirst = true
                )
                .fillMaxHeight(),
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            if (result == null) {

                Image(
                    painter = painterResource(id = R.drawable.ic_add_icon),
                    contentDescription = "add",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .width(LocalDimens.current.itemSize.dp * 0.7f)
                        .height(LocalDimens.current.itemSize.dp * 0.7f)

                )
                Text(
                    text = stringResource(id = MR.strings.palette_add_image.resourceId),
                    textAlign = TextAlign.Center,
                    color = LocalColors.current.addFromGallery.toCColor(),
                    fontSize = 12.sp
                )
            } else {
                SubcomposeAsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(result)
                        .crossfade(true)
                        .build(),
                    imageLoader = (context as Activity).get(),
                    contentDescription = null,
                    loading = {
                        CircularProgressIndicator()
                    },
                    contentScale = ContentScale.Crop
                )

            }
        }

    }
}


@Composable
private fun TopSliders(viewModel: ColorDialogViewModel?) {
    Spacer(Modifier.height(9.dp))
    Row(
        modifier = Modifier
            .fillMaxWidth(), horizontalArrangement = Arrangement.Center
    ) {
        InspSlider(
            modifier = Modifier
                .fillMaxWidth(0.75f)
                .height(LocalDimens.current.paletteListItemSize.dp),
            progress = viewModel?.getRoundness() ?: 0.5f,
        ) { viewModel?.onRoundnessChange(it) }

    }
    Spacer(Modifier.height(8.dp))
}

@Composable
private fun OpacitySlidersGroup(viewModel: ColorDialogViewModel?) {
    val alphaOneLayer by viewModel?.alphaOneLayer?.collectAsState() ?: remember {
        mutableStateOf(
            false
        )
    }
    val colorLayersCount by viewModel?.colorLayerCount?.collectAsState()
        ?: remember { mutableStateOf(2) }
    val layersCount = if (alphaOneLayer) 1 else colorLayersCount
    Spacer(Modifier.height(9.dp))
    for (layer in 0 until layersCount) {
        Row(
            modifier = Modifier
                .fillMaxWidth(), horizontalArrangement = Arrangement.Center
        ) {
            InspSlider(
                modifier = Modifier
                    .fillMaxWidth(0.75f)
                    .height(LocalDimens.current.paletteListItemSize.dp),
                progress = viewModel?.getCurrentAlphaForLayer(layer) ?: 0.5f
            ) { viewModel?.onOpacityChanged(layer, it) }

        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun ColorFilterMenu(viewModel: ColorDialogViewModel?) {
    val selected by viewModel?.currentColorFilter?.collectAsState()
        ?: remember { mutableStateOf(ColorFilterMode.SCREEN) }
    LazyRow(Modifier.padding(horizontal = 5.dp)) {
        items(
            viewModel?.colorFilterList ?: mutableListOf(
                ColorFilterMode.DEFAULT,
                ColorFilterMode.SCREEN,
                ColorFilterMode.MULTIPLY,
                ColorFilterMode.DARKEN,
                ColorFilterMode.OVERLAY,
                ColorFilterMode.LIGHTEN,
                ColorFilterMode.ADD
            )
        ) { item ->
            Box(
                modifier = Modifier
                    .padding(horizontal = 15.dp, vertical = 5.dp)
                    .clickable { viewModel?.onColorFilterChanged(item) }) {
                val color =
                    if (selected == item) LocalColors.current.activeColorFilterMode.toCColor() else LocalColors.current.incactiveColorFilterMode.toCColor()
                BasicText(
                    text = stringResource(item.stringResource().resourceId),
                    style = TextStyle(
                        color = color,
                        fontSize = 12.sp
                    )
                )
            }
        }
    }
}

@Composable
private fun ColorItemsGroup(viewModel: ColorDialogViewModel?) {
    if (viewModel?.colorFilterIsAvailable() == true) ColorFilterMenu(viewModel)
    Spacer(Modifier.height(LocalDimens.current.itemsSpacer.dp))
    val layersCount by viewModel?.colorLayerCount?.collectAsState()
        ?: remember { mutableStateOf(2) }
    for (layer in 0 until layersCount) {
        ItemPicker(
            type = COLOR,
            selectedItem = viewModel?.getCurrentColorIndexForLayer(layer) ?: 5,
            paletteItems = viewModel?.paletteItems ?: PaletteItems(2),
            layer = layer,
            viewModel
        ) { item ->
            viewModel?.onColorSelected(layer, item)
        }
        Spacer(Modifier.height(LocalDimens.current.itemsSpacer.dp))
    }
}

@Composable
fun GradientItemsGroup(viewModel: ColorDialogViewModel?) {

    Spacer(Modifier.height(LocalDimens.current.itemsSpacer.dp))

    val layersCount by viewModel?.gradientLayerCount?.collectAsState() ?: remember {
        mutableStateOf(
            1
        )
    }
    for (layer in 0 until layersCount) {
        ItemPicker(
            type = GRADIENT,
            selectedItem = viewModel?.getCurrentGradientIndexForLayer(layer) ?: 1,
            paletteItems = viewModel?.paletteItems ?: PaletteItems(1),
            layer = layer,
            viewModel
        ) { item ->
            viewModel?.onGradientSelected(layer, item)
        }
        Spacer(Modifier.height(LocalDimens.current.itemsSpacer.dp))
    }
    //show colors in other layers
    val colorLayersCount by viewModel?.colorLayerCount?.collectAsState()
        ?: remember { mutableStateOf(2) }
    for (layer in layersCount until colorLayersCount) {
        ItemPicker(
            type = COLOR,
            selectedItem = viewModel?.getCurrentColorIndexForLayer(layer) ?: 5,
            paletteItems = viewModel?.paletteItems ?: PaletteItems(2),
            layer = layer,
            viewModel
        ) { item ->
            viewModel?.onColorSelected(layer, item)
        }
        Spacer(Modifier.height(LocalDimens.current.itemsSpacer.dp))
    }
}

@Composable
fun PaletteItemsGroup(viewModel: ColorDialogViewModel?) {

    Spacer(Modifier.height(LocalDimens.current.itemsSpacer.dp))

    val layersCount by viewModel?.paletteLayerCount?.collectAsState()
        ?: remember { mutableStateOf(1) }

    for (layer in 0 until layersCount) {
        ItemPicker(
            type = PALETTE,
            selectedItem = viewModel?.getCurrentPaletteIndexForLayer(layer) ?: 1,
            paletteItems = viewModel?.paletteItems ?: PaletteItems(3),
            layer = layer,
            viewModel
        ) { item ->
            viewModel?.onPaletteSelected(layer, item)
        }
        Spacer(Modifier.height(LocalDimens.current.itemsSpacer.dp))
    }
}

@Composable
private fun TopSelector(
    viewModel: ColorDialogViewModel?,
    selected: ColorDialogPage,
    onSelect: (ColorDialogPage) -> Unit
) {
    val currentPage by viewModel?.selectedPage?.collectAsState() ?: remember {
        mutableStateOf(
            selected
        )
    }
    Row(
        Modifier
            .wrapContentHeight()
            .wrapContentWidth()
            .horizontalScroll(rememberScrollState())
            .padding(
                end = LocalDimens.current.panelEndPadding.dp * 0.7f,
                start = LocalDimens.current.panelStartPadding.dp * 0.7f
            ),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {

        val pages = ColorDialogViewModel.pages
        TopSelectorText(
            modifier = Modifier.padding(end = 3.dp),
            menuItem = pages.getMenuItem(COLOR),
            isSelected = currentPage == COLOR,
            isAvailable = viewModel?.colorIsAvailable() ?: true
        ) {
            onSelect(
                COLOR
            )
        }
        TopSelectorText(
            modifier = Modifier.padding(end = 3.dp),
            menuItem = pages.getMenuItem(GRADIENT),
            isSelected = currentPage == GRADIENT,
            isAvailable = viewModel?.gradientIsAvailable() ?: true
        ) {
            onSelect(
                GRADIENT
            )
        }
        TopSelectorText(
            menuItem = pages.getMenuItem(PALETTE),
            isSelected = currentPage == PALETTE,
            isAvailable = viewModel?.paletteIsAvailable() ?: true
        ) {
            onSelect(
                PALETTE
            )
        }

        TopSelectorText(
            menuItem = pages.getMenuItem(IMAGE),
            isSelected = currentPage == IMAGE,
            isAvailable = viewModel?.customImageChoiceIsAvailable() ?: true
        ) {
            onSelect(
                IMAGE
            )
        }

        TopSelectorText(
            menuItem = pages.getMenuItem(OPACITY),
            isSelected = currentPage == OPACITY,
            isAvailable = viewModel?.colorIsAvailable() ?: true
        ) {
            onSelect(
                OPACITY
            )
        }
        if (viewModel?.hasAdditionalSliders() != false) TopSelectorText(
            menuItem = pages.getMenuItem(ROUNDNESS),
            isSelected = currentPage == ROUNDNESS,
            isAvailable = viewModel?.colorIsAvailable() ?: true
        ) {
            onSelect(
                ROUNDNESS
            )
        }
    }
}

@Composable
private fun ItemPicker(
    type: ColorDialogPage,
    selectedItem: Int,
    paletteItems: PaletteItems,
    layer: Int,
    model: ColorDialogViewModel?,
    onItemSelected: (Int) -> Unit
) {
    val inspview by model?.selectedView?.collectAsState() ?: remember { mutableStateOf(null) }
    var selectedId by remember(
        inspview,
        selectedItem
    ) { mutableStateOf(model?.getCurrentIndexForLayer(type = type, layer = layer) ?: selectedItem) }
    var hasAdditionalItem by remember(inspview) { mutableStateOf(false) }
    val composableScope = rememberCoroutineScope()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(LocalDimens.current.itemListHeight.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        val elements = paletteItems.getElements(
            type = type,
            layer = layer
        ) {
            hasAdditionalItem = it
        }

        val ids = paletteItems.getIndices(type = type, size = elements.size)

        val listState = rememberLazyListState()

        LazyRow(
            verticalAlignment = Alignment.CenterVertically,
            state = listState,
            contentPadding = PaddingValues(horizontal = LocalDimens.current.pageTextHorizontalPadding.dp),
        ) {
            items(items = ids) { item ->
                if (item != 0 || hasAdditionalItem) {
                    val isSelected = (selectedId == item && item >= 0)
                    Box(
                        modifier = Modifier
                            .width(with(LocalDimens.current) { paletteListItemSize.dp })
                            .height(LocalDimens.current.paletteListItemSize.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (isSelected) {
                            SelectedItemWithBorder(addRoundedCorners = type == PALETTE) {
                                when (type) {
                                    COLOR -> ColorItem(
                                        color = Color(
                                            if (item == 0) paletteItems.getAdditionalColor(
                                                layer
                                            ) else elements[item] as Int
                                        )
                                    )
                                    GRADIENT -> GradientItem(
                                        gradient = if (item == 0) paletteItems.getAdditionalGradient(
                                            layer
                                        ) else elements[item] as PaletteLinearGradient,
                                        LocalDimens.current.itemSize.dpToPixels() * 0.84f
                                    )
                                    PALETTE -> PaletteItem(paletteItem = elements[item] as IntArray)
                                    else -> {
                                    }
                                }
                            }
                        } else

                            when (item) {
                                COLOR_PICKER_ITEM -> ColorPickerIcon {
                                    selectedId = item
                                    hasAdditionalItem = false
                                    onItemSelected(item)

                                }
                                REMOVE_ITEM -> ResetIcon {
                                    selectedId = item
                                    onItemSelected(item)

                                }
                                else -> {
                                    when (type) {
                                        COLOR -> ColorItem(
                                            color = Color(
                                                if (item == 0) paletteItems.getAdditionalColor(
                                                    layer
                                                ) else elements[item] as Int
                                            )
                                        ) {
                                            selectedId = item
                                            onItemSelected(item)
                                        }
                                        GRADIENT -> GradientItem(
                                            gradient = if (item == 0) paletteItems.getAdditionalGradient(
                                                layer
                                            ) else elements[item] as PaletteLinearGradient,
                                            LocalDimens.current.itemSize.dpToPixels()
                                        ) {
                                            selectedId = item
                                            onItemSelected(item)
                                        }
                                        PALETTE -> PaletteItem(paletteItem = elements[item] as IntArray) {
                                            selectedId = item
                                            onItemSelected(item)
                                        }
                                        else -> {
                                        }
                                    }
                                }
                            }
                    }
                }

            }

            val scrollToIndex = ids.indexOf(if (selectedId > 0) selectedId else 0)
            listState.autoScroll(scope = composableScope, scrollToIndex = scrollToIndex)
        }
    }

}

@Composable
private fun SliderWithTopLabel(
    modifier: Modifier = Modifier,
    label: String,
    sliderValue: Float,
    onValueChanged: (Float) -> Unit
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Spacer(modifier = Modifier.fillMaxWidth(0.14f))
        Text(
            text = label,
            fontSize = LocalDimens.current.tabTextSize.sp,
            modifier = Modifier.padding(
                top = LocalDimens.current.pageTextVerticalPadding.dp,
                bottom = LocalDimens.current.pageTextVerticalPadding.dp,
                end = LocalDimens.current.panelEndPadding.dp
            ),
            color = LocalColors.current.sliderLabelColor.toCColor()
        )
    }
    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        InspSlider(
            modifier = Modifier
                .height(35.dp)
                .fillMaxWidth(0.75f),
            progress = sliderValue,
            onChanged = onValueChanged
        )
    }


}

@Composable
private fun SelectedItemWithBorder(
    addRoundedCorners: Boolean = false,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .width(LocalDimens.current.outerBorderActiveItem.dp)
            .height(LocalDimens.current.outerBorderActiveItem.dp)
            .background(
                color = LocalColors.current.selectedItemOuterBorder.toCColor(),
                shape = if (!addRoundedCorners) CircleShape else RoundedCornerShape(LocalDimens.current.paletteItemCornerRadius.dp * 1.1f)
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .height(LocalDimens.current.innerBorderActiveItem.dp)
                .width(LocalDimens.current.innerBorderActiveItem.dp)
                .border(
                    width = LocalDimens.current.activeItemBorderWidth.dp * 0.85f,
                    shape = if (!addRoundedCorners) CircleShape else RoundedCornerShape(LocalDimens.current.paletteItemCornerRadius.dp),
                    color = LocalColors.current.selectedItemInnerBorder.toCColor()
                ),
            contentAlignment = Alignment.Center
        ) {
            content()
        }
    }
}

@Composable
private fun ResetIcon(onClick: () -> Unit) {
    Image(
        painterResource(id = R.drawable.ic_remove_color),
        contentDescription = "remove color",
        contentScale = ContentScale.Fit,
        modifier = Modifier
            .width(LocalDimens.current.itemSize.dp)
            .height(LocalDimens.current.itemSize.dp)
            .clickable { onClick() }
    )
}

@Composable
private fun ColorPickerIcon(onClick: () -> Unit) {
    val colors = ImageUtil.sweepGradientIcon().map { Color(it) }
    Box(
        modifier = Modifier
            .background(
                brush = Brush.sweepGradient(colors = colors),
                shape = CircleShape
            )
            .width(LocalDimens.current.itemSize.dp)
            .height(LocalDimens.current.itemSize.dp)
            .clickable { onClick() }
    )
}

@Composable
private fun TopSelectorText(
    modifier: Modifier = Modifier,
    menuItem: CommonMenuItem<ColorDialogPage>,
    isSelected: Boolean = false,
    isAvailable: Boolean = true,
    onSelect: () -> Unit
) {
    if (!isAvailable) return
    Row(
        modifier = modifier
            .background(
                color = if (isSelected) LocalColors.current.selectedPageTextBackground.toCColor() else Color.Transparent,
                shape = RoundedCornerShape(LocalDimens.current.selectedPageBorderRadius.dp)
            )
            .clickable {
                onSelect()
            }
            .padding(
                horizontal = LocalDimens.current.pageTextHorizontalPadding.dp,
                vertical = LocalDimens.current.pageTextVerticalPadding.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val elementColor =
            if (isSelected) LocalColors.current.selectedPageText.toCColor() else LocalColors.current.pageText.toCColor()
        if (menuItem.icon != null) {
            val icon = menuItem.icon!!.getDrawable(context = LocalContext.current)
            Image(
                painterResource(id = icon),
                contentDescription = stringResource(menuItem.text.resourceId).toUpperCase(
                    Locale.current
                ),
                modifier = Modifier
                    .height(11.dp)
                    .wrapContentWidth()
                    .padding(end = 3.dp),
                contentScale = ContentScale.FillHeight,
                colorFilter = ColorFilter.lighting(
                    multiply = elementColor,
                    add = Color(0)
                )
            )
        }
        Text(
            text = stringResource(menuItem.text.resourceId).toUpperCase(Locale.current),
            fontSize = LocalDimens.current.tabTextSize.sp,
            color = elementColor
        )
    }
}

@Composable
private fun ColorItem(color: Color, onClick: () -> Unit = {}) {

    Box(
        modifier = Modifier
            .width(LocalDimens.current.itemSize.dp)
            .height(LocalDimens.current.itemSize.dp)
            .clip(shape = CircleShape)
            .background(color = color)
            .clickable { onClick() }
    )
}

@Composable
private fun PaletteItem(paletteItem: IntArray, onClick: () -> Unit = {}) {
    Box(
        modifier = Modifier
            .width(LocalDimens.current.itemSize.dp)
            .height(LocalDimens.current.itemSize.dp)
            .border(
                color = LocalColors.current.paletteBorderColor.toCColor(),
                shape = RoundedCornerShape(LocalDimens.current.paletteItemCornerRadius.dp),
                width = LocalDimens.current.paletteBorderWidth.dp
            )
            .clip(shape = RoundedCornerShape(LocalDimens.current.paletteItemCornerRadius.dp))
            .clickable { onClick() }

    ) {
        Column {
            for (color in paletteItem) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(LocalDimens.current.itemSize.dp / paletteItem.size)
                        .background(color = Color(color))
                )

            }
        }

    }
}

@Composable
private fun GradientItem(
    gradient: PaletteLinearGradient,
    itemSize: Float,
    onClick: () -> Unit = {}
) {
    val offsetCoordList = gradient.getShaderCoords(0f, 0f, itemSize, itemSize)
    Box(
        modifier = Modifier
            .width(LocalDimens.current.itemSize.dp)
            .height(LocalDimens.current.itemSize.dp)
            .clip(shape = CircleShape)
            .clickable { onClick() }
            .background(
                brush = Brush.linearGradient(
                    colors = gradient.colors.map { Color(it) },
                    start = Offset(offsetCoordList[0], offsetCoordList[1]),
                    end = Offset(offsetCoordList[2], offsetCoordList[3])
                )
            )
    )
}

@Preview(name = "Palette selected")
@Composable
private fun PreviewDialog2() {
    ColorChangeDialogCompose(selected = PALETTE)
}

@Preview(name = "Opacity selected")
@Composable
private fun PreviewDialog3() {
    ColorChangeDialogCompose(selected = OPACITY)
}

@Preview(name = "Round selected")
@Composable
private fun PreviewDialog4() {
    ColorChangeDialogCompose(selected = ROUNDNESS)
}

@Preview(name = "Color selected en", locale = "en")
@Composable
private fun PreviewDialogEn() {
    ColorChangeDialogCompose(selected = COLOR)
}

@Preview(name = "Color selected ru", locale = "ru")
@Composable
private fun PreviewDialogRu() {
    ColorChangeDialogCompose(selected = COLOR)
}

@Preview(name = "Color selected es", locale = "es")
@Composable
private fun PreviewDialogEs() {
    ColorChangeDialogCompose(selected = COLOR)
}

@Preview(name = "Color selected pt", locale = "pt")
@Composable
private fun PreviewDialogPt() {
    ColorChangeDialogCompose(selected = COLOR)
}

@Preview(name = "Image selected en", locale = "en")
@Composable
private fun PreviewDialogImageEn() {
    ColorChangeDialogCompose(selected = IMAGE)
}

@Preview(name = "Image selected ru", locale = "ru")
@Composable
private fun PreviewDialogImageRu() {
    ColorChangeDialogCompose(selected = IMAGE)
}

@Preview(name = "Image selected es", locale = "es")
@Composable
private fun PreviewDialogImageEs() {
    ColorChangeDialogCompose(selected = IMAGE)
}

@Preview(name = "Image selected pt", locale = "pt")
@Composable
private fun PreviewDialogImagePt() {
    ColorChangeDialogCompose(selected = IMAGE)
}

@Preview(name = "Image selected uk", locale = "uk")
@Composable
private fun PreviewDialogImageUk() {
    ColorChangeDialogCompose(selected = IMAGE)
}