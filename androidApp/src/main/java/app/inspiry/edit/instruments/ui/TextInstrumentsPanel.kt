package app.inspiry.edit.instruments.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.inspiry.core.media.getAlignIcon
import app.inspiry.edit.instruments.BottomInstrumentColorsLight
import app.inspiry.edit.instruments.BottomInstrumentlColors
import app.inspiry.edit.instruments.defaultPanel.BottomInstrumentsDimens
import app.inspiry.edit.instruments.textPanel.TextInstruments
import app.inspiry.edit.instruments.textPanel.BottomInstrumentsDimensPhone
import app.inspiry.edit.instruments.textPanel.TextInstrumentsPanelViewModel
import app.inspiry.utilities.toCColor
import app.inspiry.utils.getDrawable

private val LocalColors =
    compositionLocalOf<BottomInstrumentlColors> { BottomInstrumentColorsLight() }
private val LocalDimens =
    compositionLocalOf<BottomInstrumentsDimens> { BottomInstrumentsDimensPhone() }

@Composable
fun TextInstrumentsUI(
    viewModel: TextInstrumentsPanelViewModel
) {
    Column(
        modifier = Modifier
            .clickable(false) {} //to prevent taps-blocking bug
            .fillMaxWidth()
            .wrapContentHeight(),
        verticalArrangement = Arrangement.Bottom
    )
    {
        TextInstruments(
            modifier = Modifier
                .height(LocalDimens.current.barHeight.dp)
                .background(LocalColors.current.background.toCColor()), viewModel = viewModel
        )
    }
}

@Composable
private fun TextInstruments(
    modifier: Modifier = Modifier,
    viewModel: TextInstrumentsPanelViewModel
) {

    val currentAlignment = viewModel.alignment.collectAsState()

    LazyRow(
        modifier = modifier
            .wrapContentWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {

        items(items = viewModel.menu.getKeys()) { item ->
            val color =
                if (viewModel.itemHighlight(item)) LocalColors.current.activeTextColor.toCColor()
                else LocalColors.current.inactiveTextColor.toCColor()
            val icon =
                (if (item == TextInstruments.TEXT_ALIGNMENT) currentAlignment.value.getAlignIcon() else viewModel.menu.getIconName(
                    item
                ))
                    .getDrawable(LocalContext.current)

            InstrumentItem(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(LocalDimens.current.instrumentItemWidth.dp),
                color = color,
                text = stringResource(
                    id = viewModel.menu.getText(item).resourceId ?: -1
                ),
                icon_id = icon,
                iconSize = LocalDimens.current.instrumentsIconSize.dp
            ) {
                viewModel.onInstrumentClick(item)
            }
        }
    }
}

@Composable
fun InstrumentItem(
    modifier: Modifier = Modifier,
    color: Color,
    text: String = "",
    icon_id: Int,
    iconSize: Dp,
    onClickAction: () -> Unit
) {
    Column(
        modifier = modifier
            .clickable {
                onClickAction()
            },
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Image(
            painterResource(id = icon_id),
            contentDescription = null,
            modifier = Modifier
                .width(iconSize)
                .height(iconSize)
                .padding(top = 3.dp),
            colorFilter = ColorFilter.lighting(
                multiply = color,
                add = Color(0)
            )
        )
        if (text.isNotEmpty()) {
            Text(
                text = text,
                modifier = Modifier.padding(start = 3.dp, top = 7.dp, end = 3.dp),
                color = color,
                fontSize = LocalDimens.current.labelTextSize.sp,
                maxLines = 1
            )
        }
    }
}