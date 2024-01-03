package app.inspiry.edit.instruments.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.inspiry.R
import app.inspiry.edit.instruments.textPanel.TextSizeInstrumentViewModel
import app.inspiry.edit.size.SizeInstrumentColorsLight
import app.inspiry.edit.size.SizeInstrumentsDimensPhone
import app.inspiry.music.android.ui.InspSlider
import app.inspiry.utilities.toCColor
import dev.icerock.moko.graphics.Color

private val LocalDimens = compositionLocalOf { SizeInstrumentsDimensPhone() }
private val LocalColors = compositionLocalOf { SizeInstrumentColorsLight() }

@Composable
fun EditTextSizePanel(viewModel: TextSizeInstrumentViewModel) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(LocalDimens.current.barHeight.dp)
            .background(LocalColors.current.background.toCColor()),
        verticalArrangement = Arrangement.Center,

    ) {

        val textSize by viewModel.textSizeState.collectAsState()
        val charSpacing by viewModel.letterSpacingState.collectAsState()
        val lineSpacing by viewModel.lineSpacingState.collectAsState()

        CreateLine(
            iconID = R.drawable.sub_instr_text_size,
            progress = textSize,
            onChanged = {
                viewModel.onTextSizeChanged(it)
            })
        CreateLine(
            iconID = R.drawable.sub_instr_char_spacing,
            progress = charSpacing,
            onChanged = {
                viewModel.onLetterSpacingChanged(it)
            })
        CreateLine(
            iconID = R.drawable.sub_instr_line_spacing,
            progress = lineSpacing,
            onChanged = {
                viewModel.onLineSpacingChanged(it)
            })
    }
}

@Composable
private fun CreateLine(iconID: Int, progress: Float, onChanged: (Float) -> Unit) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(LocalDimens.current.lineHeight.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(modifier = Modifier.width(LocalDimens.current.linestartSpacer.dp))
        Image(
            painterResource(id = iconID),
            contentDescription = "action icon",
            modifier = Modifier
                .wrapContentWidth()
                .height(LocalDimens.current.lineIconHeight.dp),
            colorFilter = ColorFilter.lighting(
                multiply = LocalColors.current.textAndIcons.toCColor(),
                add = Color(0).toCColor()
            )
        )
        Spacer(
            modifier = Modifier
                .width(5.dp)
        )

        InspSlider(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .fillMaxHeight(), progress = progress, onChanged = onChanged
        )

        Spacer(
            modifier = Modifier
                .width(5.dp)
        )
        Text(
            text = (progress * 100).toInt().toString(),
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Medium,
            fontStyle = FontStyle.Normal,
            fontSize = LocalDimens.current.textSize.sp,
            color = LocalColors.current.textAndIcons.toCColor()
        )
        Spacer(
            modifier = Modifier
                .width(LocalDimens.current.lineEndSpacer.dp)
        )
    }
}
