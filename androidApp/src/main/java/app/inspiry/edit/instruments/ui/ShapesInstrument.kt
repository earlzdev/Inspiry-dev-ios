package app.inspiry.edit.instruments.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import app.inspiry.core.animator.clipmask.shape.ShapeType
import app.inspiry.core.animator.clipmask.shape.icon
import app.inspiry.edit.instruments.defaultPanel.BottomInstrumentsDimens
import app.inspiry.edit.instruments.shapes.ShapesInstrumentViewModel
import app.inspiry.edit.instruments.textPanel.BottomInstrumentsDimensPhone
import app.inspiry.utils.getDrawable

private val LocalDimens =
    compositionLocalOf<BottomInstrumentsDimens> { BottomInstrumentsDimensPhone() }

@Composable
fun ShapesInstrument(model: ShapesInstrumentViewModel) {
    Column(
        modifier = Modifier
            .clickable(false) {} //to prevent taps-blocking bug
            .fillMaxWidth()
            .wrapContentHeight()
            .background(Color(0xff292929)),
        verticalArrangement = Arrangement.Bottom,
        horizontalAlignment = Alignment.CenterHorizontally
    )
    {
        ShapesList(
            modifier = Modifier
                .height(LocalDimens.current.barHeight.dp),
            model = model
        )
    }
}

@Composable
private fun ShapesList(
    modifier: Modifier = Modifier,
    model: ShapesInstrumentViewModel
) {
    val shapesList = ShapesInstrumentViewModel.shapesList

    val view by model.currentView.collectAsState()
    val selected by view.shapeState.collectAsState()
    val context = LocalContext.current
    LazyRow(
        modifier = modifier
            .wrapContentWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {

        items(items = shapesList) { item ->
            val color =
                if (item == (selected ?: ShapeType.NOTHING)) Color(0xffababab)
                else Color(0xff363636)
            Box(modifier = Modifier
                .fillMaxHeight()
                .width(LocalDimens.current.instrumentItemWidth.dp)
                .clickable { model.selectShape(item) }
                .padding(all = 10.dp)
                .background(color, RoundedCornerShape(corner = CornerSize(5.dp))),
            contentAlignment = Alignment.Center) {
                Image(
                    painterResource(id = item.icon().getDrawable(context)),
                    contentDescription = null,
                    modifier = Modifier
                        .width(30.dp)
                        .height(30.dp),
                )
            }
        }
    }
}