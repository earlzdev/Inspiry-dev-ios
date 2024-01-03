package app.inspiry.music.android.ui

import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.github.krottv.compose.sliders.DefaultTrack
import com.github.krottv.compose.sliders.ListenOnPressed
import com.github.krottv.compose.sliders.SliderValueHorizontal


@Composable
fun InspSlider(modifier: Modifier = Modifier, progress: Float, onChanged: (Float) -> Unit) {
    var stateSlider2 by remember(progress) { mutableStateOf(progress) }
    SliderValueHorizontal(
        stateSlider2, {
            stateSlider2 = it
            onChanged(it)
        },
        modifier = modifier,
        steps = 0,
        thumbHeightMax = true,
        track = { p1, p2, p3, p4, p5 ->
            DefaultTrack(
                p1, p2, p3, p4, p5,
                height = 2.dp,
                colorTrack = Color(0xff828282),
                colorProgress = Color(0xFFFFFFFF)
            )
        },
        thumb = { thumb_modifier, _, interactionSource, _, _ ->
            SliderThumb2Layers(
                thumb_modifier,
                DpSize(10.dp, 10.dp),
                Color(0x99828282),
                Color(0xFFFFFFFF),
                interactionSource,
                1.5f
            )
        }
    )
}

@Composable
fun SliderThumb2Layers(
    modifier: Modifier, sizeInner: DpSize, colorOuter: Color,
    colorInner: Color, interactionSource: MutableInteractionSource, scaleOnPress: Float = 1.3f
) {
    var isPressed by remember { mutableStateOf(false) }
    interactionSource.ListenOnPressed { isPressed = it }

    val scale: Float by animateFloatAsState(
        if (isPressed) scaleOnPress else 1f,
        animationSpec = SpringSpec(0.3f)
    )

    Box(
        modifier
            .graphicsLayer(scaleX = scale, scaleY = scale)
            .background(colorOuter, CircleShape),
        contentAlignment = Alignment.Center
    ) {

        Spacer(
            modifier = Modifier
                .size(sizeInner)
                .background(colorInner, CircleShape)
        )
    }
}