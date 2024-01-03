package app.inspiry.edit.instruments.ui

import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.inspiry.edit.instruments.BottomInstrumentColorsLight
import app.inspiry.edit.instruments.BottomInstrumentlColors
import app.inspiry.edit.instruments.defaultPanel.BottomInstrumentsDimens
import app.inspiry.edit.instruments.defaultPanel.DefaultInstrumentsDimensPhone
import app.inspiry.edit.instruments.defaultPanel.DefaultInstrumentsPanelViewModel
import app.inspiry.utilities.toCColor
import app.inspiry.utils.getDrawable

private val LocalColors =
    compositionLocalOf<BottomInstrumentlColors> { BottomInstrumentColorsLight() }
private val LocalDimens =
    compositionLocalOf<BottomInstrumentsDimens> { DefaultInstrumentsDimensPhone() }

@Composable
fun DefaultInstrumentsUI(
    viewModel: DefaultInstrumentsPanelViewModel
) {
    val selected by viewModel.activeInstrument.collectAsState(initial = null)
    Column(
        modifier = Modifier
            .clickable(false) {}
            .fillMaxWidth()
            .wrapContentHeight(),
        verticalArrangement = Arrangement.Bottom
    )
    {
        val animVisibleState = remember { MutableTransitionState(false) }
            .apply { targetState = true }
        animVisibleState.targetState = selected != null

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(LocalDimens.current.barHeight.dp)
                .background(LocalColors.current.background.toCColor()),
            horizontalArrangement = Arrangement.Center
        ) {

            LazyRow {
                items(items = viewModel.menu.getKeys()) { item ->
                    val color =
                        if (viewModel.itemHighlight(item)) LocalColors.current.activeTextColor.toCColor()
                        else LocalColors.current.inactiveTextColor.toCColor()
                    InstrumentItem(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(LocalDimens.current.instrumentItemWidth.dp),
                        color = color,
                        text = stringResource(
                            id = viewModel.menu.getMenuItem(item).text.resourceId
                        ),
                        icon_id = viewModel.menu.getMenuItem(item).icon!!.getDrawable(LocalContext.current),
                        iconSize = LocalDimens.current.instrumentsIconSize.dp
                    ) {
                        viewModel.selectInstrument(item)
                    }
                }
            }
        }
    }
}