package app.inspiry.edit.instruments.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.inspiry.R
import app.inspiry.core.manager.DebugManager
import app.inspiry.core.media.TemplateFormat
import app.inspiry.edit.instruments.format.*
import app.inspiry.edit.instruments.format.DisplayTemplateFormat
import app.inspiry.edit.instruments.format.FormatsProviderImpl
import app.inspiry.subscribe.ui.SubscribeActivity
import app.inspiry.utilities.toCColor
import app.inspiry.utils.Constants


private val LocalColors =
    compositionLocalOf<FormatInstrumentColors> { FormatInstrumentColorsLight() }
private val LocalDimens =
    compositionLocalOf<FormatInstrumentDimens> { FormatInstrumentDimensPhone() }

@Composable
fun TemplateFormatsPanel(viewModel: FormatSelectorViewModel? = null) {

    val formats = FormatsProviderImpl().getFormats()
    val selected by viewModel?.currentFormat?.collectAsState()
        ?: remember { mutableStateOf(TemplateFormat.story) } //for preview only

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(LocalDimens.current.barHeight.dp)
            .background(LocalColors.current.background.toCColor()),
        horizontalArrangement = Arrangement.Center
    ) {
        val context = LocalContext.current
        LazyRow(modifier = Modifier.padding(bottom = LocalDimens.current.topPadding.dp)) {
            items(items = formats) { item ->
                val color =
                    if (selected == item.templateFormat) LocalColors.current.activeTextColor.toCColor()
                    else LocalColors.current.inactiveTextColor.toCColor()
                FormatItem(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(LocalDimens.current.instrumentItemWidth.dp),
                    format = item,
                    viewModel = viewModel,
                    color = color
                )
                { toSubscribe ->
                    if (toSubscribe) {
                        viewModel?.onFormatChanged(null)
                    } else {
                        viewModel?.onFormatChanged(item.templateFormat)
                    }
                }
            }
        }
    }

}

@Composable
private fun FormatIcon(format: DisplayTemplateFormat, color: Color, hasPremium: Boolean) {
    Box(
        modifier = Modifier
            .padding(top = LocalDimens.current.iconTopPadding.dp)
            .fillMaxWidth()
            .height(LocalDimens.current.iconHeight.dp), contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .width(format.iconWidthDp.dp)
                .height(format.iconHeightDp.dp)
                .border(
                    width = 1.6.dp,
                    shape = RoundedCornerShape(2.2.dp),
                    color = color
                )
        )
        if (format.premium && !hasPremium) {
            Image(
                modifier = Modifier
                    .width(LocalDimens.current.proWidth.dp)
                    .height(LocalDimens.current.proHeight.dp)
                    .offset(
                        x = LocalDimens.current.proHorizontalOffset.dp,
                        y = -(format.iconHeightDp / 2f).dp
                    ),
                painter = painterResource(id = R.drawable.ic_premium_template),
                contentDescription = null
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FormatItem(
    modifier: Modifier,
    format: DisplayTemplateFormat,
    viewModel: FormatSelectorViewModel?,
    color: Color,
    onClickAction: (toSubscribe: Boolean) -> Unit
) {
    val hasPremium by viewModel?.licenseManager?.hasPremiumState?.collectAsState()
        ?: remember { mutableStateOf(false) } //for preview only
    Column(
        modifier = modifier
            .combinedClickable(enabled = true,
                onClick = {
                    val toSubscribe =
                        format.premium && !hasPremium
                    onClickAction(toSubscribe)
                },
                onLongClick = { if (DebugManager.isDebug) onClickAction(false) }),
        verticalArrangement = Arrangement.SpaceAround,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        FormatIcon(format = format, color = color, hasPremium = hasPremium)

        Text(
            text = stringResource(id = format.text.resourceId),
            color = color,
            fontSize = LocalDimens.current.labelTextSize.sp,
            maxLines = 1
        )
    }
}

@Preview(name = "Formats preview pt", locale = "pt")
@Composable
private fun FormatsPreviewPt() {
    TemplateFormatsPanel()
}

@Preview(name = "Formats preview es", locale = "es")
@Composable
private fun FormatsPreviewEs() {
    TemplateFormatsPanel()
}

@Preview(name = "Formats preview ru", locale = "ru")
@Composable
private fun FormatsPreviewRu() {
    TemplateFormatsPanel()
}

@Preview(name = "Formats preview en", locale = "en")
@Composable
private fun FormatsPreviewEn() {
    TemplateFormatsPanel()
}

