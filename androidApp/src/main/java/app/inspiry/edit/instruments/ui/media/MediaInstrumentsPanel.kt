package app.inspiry.edit.instruments.ui.media

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.inspiry.R
import app.inspiry.edit.instruments.BottomInstrumentColorsLight
import app.inspiry.edit.instruments.BottomInstrumentlColors
import app.inspiry.edit.instruments.defaultPanel.BottomInstrumentsDimens
import app.inspiry.edit.instruments.media.MediaInstrumentsPanelViewModel
import app.inspiry.edit.instruments.textPanel.BottomInstrumentsDimensPhone
import app.inspiry.edit.instruments.ui.InstrumentItem
import app.inspiry.utilities.toCColor
import app.inspiry.utils.getDrawable

private val LocalColors =
    compositionLocalOf<BottomInstrumentlColors> { BottomInstrumentColorsLight() }
private val LocalDimens =
    compositionLocalOf<BottomInstrumentsDimens> { BottomInstrumentsDimensPhone() }

@Composable
fun MediaInstrumentsUI(
    viewModel: MediaInstrumentsPanelViewModel
) {
    Column(
        modifier = Modifier
            .clickable(false) {} //to prevent taps-blocking bug
            .fillMaxWidth()
            .wrapContentHeight()
            .background(LocalColors.current.background.toCColor()),
        verticalArrangement = Arrangement.Bottom,
        horizontalAlignment = Alignment.CenterHorizontally
    )
    {
        MediaInstruments(
            modifier = Modifier
                .height(LocalDimens.current.barHeight.dp),
            viewModel = viewModel
        )
    }
}

@Composable
private fun MediaInstruments(
    modifier: Modifier = Modifier,
    viewModel: MediaInstrumentsPanelViewModel
) {
    val menu by viewModel.menu.collectAsState()
    val selected by viewModel.activeMediaInstrument.collectAsState(initial = null)
    val hasPremium by viewModel.licenseManager.hasPremiumState.collectAsState()

    LazyRow(
        modifier = modifier
            .wrapContentWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {

        items(items = menu.getKeys()) { item ->
            val color =
                if (item == selected || selected == null) LocalColors.current.activeTextColor.toCColor()
                else LocalColors.current.inactiveTextColor.toCColor()
            val icon = menu.getIconName(
                item
            ).getDrawable(LocalContext.current)
            Box {
                InstrumentItem(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(LocalDimens.current.instrumentItemWidth.dp),
                    color = color,
                    text = stringResource(
                        id = menu.getText(item).resourceId
                    ),
                    icon_id = icon,
                    iconSize = LocalDimens.current.instrumentsIconSize.dp
                ) {
                    viewModel.onInstrumentClick(item)
                }
                if (viewModel.showPremiumBadge(hasPremium, item)) {
                    Image(
                        painterResource(id = R.drawable.ic_premium_template),
                        contentDescription = null,
                        contentScale = ContentScale.Inside,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = 12.dp, y = -2.dp)
                    )
                }
            }
        }
    }
}