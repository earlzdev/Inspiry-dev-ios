package app.inspiry.edit.instruments.ui

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.consumeAllChanges
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import app.inspiry.MR
import app.inspiry.R
import app.inspiry.edit.instruments.PickImageConfig
import app.inspiry.helpers.MatisseActivityResult
import app.inspiry.slide.model.SlideInstrumentViewModel
import app.inspiry.slide.ui.SlideInstrumentDimensPhone
import app.inspiry.slide.ui.SlidesInstrumentColors
import app.inspiry.slide.ui.SlidesInstrumentColorsLight
import app.inspiry.slide.ui.SlidesInstrumentDimens
import app.inspiry.utilities.toCColor
import app.inspiry.utils.ImageUtils
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import kotlinx.coroutines.launch
import org.koin.android.ext.android.get

private val LocalDimens =
    compositionLocalOf<SlidesInstrumentDimens> { SlideInstrumentDimensPhone() }
private val LocalColors =
    compositionLocalOf<SlidesInstrumentColors> { SlidesInstrumentColorsLight() }

@Composable
fun SlidesInstrument(model: SlideInstrumentViewModel) {
    Column(
        modifier = Modifier
            .clickable(false) {} //to prevent taps-blocking bug
            .fillMaxWidth()
            .wrapContentHeight()
            .background(Color(0xff292929)),
        verticalArrangement = Arrangement.Bottom
    )
    {
        SlidesList(
            modifier = Modifier
                .height(LocalDimens.current.barHeight.dp),
            model = model
        )
    }
}

@Composable
private fun SlidesList(
    modifier: Modifier = Modifier,
    model: SlideInstrumentViewModel
) {
    val idList by model.slideList.collectAsState()
    val view by model.currentView.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val selected by model.selectedSlideID.collectAsState()

    val matisseLauncher = matisseLauncher(viewModel = model)

    val listState: LazyListState = rememberLazyListState()

    val draggableListState = remember {
        DraggableListState(lazyListState = listState, onMove = { old, new ->
            if (new > idList.lastIndex) return@DraggableListState
            val newList = idList.toMutableList()
            val id = newList.removeAt(old)
            newList.add(new, id)
            model.slideList.value = newList
            model.replaceSlides(id, new)
        })
    }

    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectDragGesturesAfterLongPress(
                    onDrag = { change, offset ->
                        change.consumeAllChanges()
                        draggableListState.onDrag(offset = offset)
                    },
                    onDragStart = { offset ->
                        draggableListState.onDragStart(offset, idList.indexOf(selected))
                    },
                    onDragEnd = {
                        draggableListState.onDragFinished()
                    },
                    onDragCancel = {
                        draggableListState.onDragCanceled()
                    }
                )
            },
        verticalAlignment = Alignment.CenterVertically,
        state = listState
    ) {

        items(items = idList, key = { it }) { itemID ->
            val url by remember(idList) {
                mutableStateOf(model.getUrlByID(itemID))
            }
            Spacer(modifier = Modifier.width(LocalDimens.current.itemPadding.dp))
            val currentIndex = idList.indexOf(itemID)
            Box(
                modifier = Modifier
                    .composed {
                        val offset =
                            draggableListState.draggedOffset.takeIf { currentIndex == draggableListState.draggedItemIndex }
                                ?: 0f

                        val elementOffset by animateFloatAsState(
                            targetValue = draggableListState.elementOffsetByIndex(
                                currentIndex
                            ), animationSpec = tween(
                                durationMillis = if (draggableListState.currentElement == null) 0 else 240
                            )
                        )
                        val zIndex =
                            if (draggableListState.draggedItemIndex == currentIndex) 10f else 0f
                        val scale by animateFloatAsState(targetValue = if (draggableListState.currentElement?.index == currentIndex) 1.5f else 1f)
                        Modifier
                            .graphicsLayer {
                                translationX = offset + elementOffset
                                translationY = 0f
                                scaleX = scale
                                scaleY = scale
                                clip = false
                            }
                            .zIndex(zIndex)
                    }
                    .height(LocalDimens.current.itemSize.dp)
                    .width(LocalDimens.current.itemSize.dp)
                    .clickable { model.setSelected(itemID) },
                contentAlignment = Alignment.Center
            ) {
                if (selected == itemID)
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .scale(1.15f)
                            .clip(RoundedCornerShape(7.dp))
                            .background(
                                LocalColors.current.selectedSlide.toCColor()
                            )
                    )
                SubcomposeAsyncImage(
                    modifier = modifier
                        .height(LocalDimens.current.itemSize.dp)
                        .width(LocalDimens.current.itemSize.dp)
                        .clip(RoundedCornerShape(7.dp)),
                    model = ImageRequest.Builder(context)
                        .data(url)
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
            Spacer(modifier = Modifier.width(LocalDimens.current.itemPadding.dp))
        }
        if (idList.size < (view.getSlidesParent()?.maxSlides ?: 5))
            item {
                Spacer(modifier = Modifier.width(LocalDimens.current.itemPadding.dp))
                Row(
                    modifier = Modifier
                        .fillMaxHeight()
                        .background(Color(0xff292929))
                        .clickable {
                            scope.launch {
                                if (ImageUtils.isMediaChooserPrepared(
                                        context as AppCompatActivity,
                                        config = PickImageConfig(maxSelectable = model.emptySlidesCount())
                                    )
                                ) {
                                    matisseLauncher.launch(Unit)
                                }
                            }
                        }, verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_add_slide),
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(LocalDimens.current.itemPadding.dp))
                    BasicText(
                        text = stringResource(id = MR.strings.instrument_add.resourceId),
                        style = TextStyle(
                            color = LocalColors.current.newItemColor.toCColor(),
                            fontSize = LocalDimens.current.addTextSize.sp,
                            textAlign = TextAlign.Center
                        )
                    )
                    Spacer(modifier = Modifier.width(LocalDimens.current.itemPadding.dp))
                }
            }
    }
}

@Composable
private fun matisseLauncher(viewModel: SlideInstrumentViewModel?) =
    rememberLauncherForActivityResult(contract = MatisseActivityResult()) { matisseResult ->
        if (matisseResult.isNotEmpty())
            viewModel?.onNewSlideAppend(matisseResult)
    }

fun Modifier.notClip() = graphicsLayer(clip = false);


