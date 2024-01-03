package app.inspiry.export.dialog

import android.content.pm.ResolveInfo
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.inspiry.projectutils.R
import app.inspiry.utilities.toCColor
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter

@Composable
internal fun ExportDialogMainUI(
    modifier: Modifier = Modifier,
    imageElseVideo: Boolean,
    viewModel: ExportDialogViewModel,
    imageLoader: ImageLoader,
    onPickOption: (ResolveInfo) -> Unit
) {
    val colors = LocalColors.current
    Column(modifier = Modifier
        .fillMaxWidth()
        .then(modifier)) {

        Text(
            stringResource(R.string.share_dialog_title),
            modifier = Modifier.padding(
                start = 30.dp, bottom = 20.dp,
                top = 20.dp, end = 20.dp
            ),
            color = colors.title.toCColor(),
            fontSize = 22.sp
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    colors.bg.toCColor(),
                    RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
                )
                .padding(horizontal = 20.dp),
            contentAlignment = Alignment.Center
        ) {

            val infos by viewModel.getPackageInfoState(imageElseVideo).collectAsState()
            if (infos == null) {
                CircularProgressIndicator(color = colors.progressIndicator.toCColor())
            } else {
                ExportList(viewModel, imageLoader, infos!!, onPickOption)
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ExportList(
    viewModel: ExportDialogViewModel,
    imageLoader: ImageLoader,
    infos: List<ResolveInfo>,
    onPickOption: (ResolveInfo) -> Unit
) {

    val colors = LocalColors.current
    val state = rememberLazyListState()
    LazyVerticalGrid(
        cells = GridCells.Fixed(4),
        modifier = Modifier.fillMaxWidth(),
        state,
        contentPadding = PaddingValues(top = 10.dp, bottom = 20.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {


        itemsIndexed(infos) { index, item ->
            Column(
                modifier = Modifier
                    .size(90.dp, 110.dp)
                    .clickable { onPickOption(item) },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                AsyncImage(
                    model = item.imageUri,
                    contentDescription = null,
                    imageLoader = imageLoader,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .padding(top = 15.dp)
                        .size(50.dp),
                    error = painterResource(id = app.inspiry.R.drawable.ic_launcher_default)
                )

                val activityName = item.activityInfo.name
                var appName by remember(activityName) {
                    mutableStateOf<String?>(null)
                }
                LaunchedEffect(activityName) {
                    viewModel.getTextForItem(activityName) {
                        appName = it
                    }
                }

                Text(
                    appName ?: "",
                    modifier = Modifier
                        .padding(top = 5.dp, start = 5.dp, end = 5.dp)
                        .fillMaxWidth(),
                    color = colors.itemText.toCColor(),
                    fontSize = 12.sp, maxLines = 2, textAlign = TextAlign.Center
                )
            }
        }
    }
}

internal val LocalColors = compositionLocalOf<ExportDialogColors> {
    ExportDialogColorsLight()
}