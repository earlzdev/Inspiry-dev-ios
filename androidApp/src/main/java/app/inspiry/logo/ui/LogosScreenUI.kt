package app.inspiry.logo.ui

import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.inspiry.MR
import app.inspiry.R
import app.inspiry.core.database.data.LogoItem
import app.inspiry.logo.LogoColors
import app.inspiry.logo.LogoViewModel
import app.inspiry.utilities.toCColor
import coil.compose.SubcomposeAsyncImage
import coil.imageLoader
import coil.request.ImageRequest
import dev.icerock.moko.resources.FontResource
import dev.icerock.moko.resources.StringResource


@Composable
fun LogosMain(
    viewModel: LogoViewModel,
    colors: LogoColors
) {
    Column(
        Modifier
            .fillMaxSize()
            .clickable(enabled = false) {}
            .background(colors.background.toCColor())
    ) {
        TopBar(colors)
        Tabs(category = viewModel.categories, colors)
        Logos(
            viewModel = viewModel,
            colors = colors
        )
    }
}

@Composable
private fun Tabs(category: List<StringResource>, colors: LogoColors) {
    category.forEach {
        Box(
            modifier = Modifier
                .wrapContentSize()
                .padding(start = 26.dp, top = 15.dp, bottom = 26.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(colors.tabBgActive.toCColor())
                .padding(horizontal = 16.dp, vertical = 5.dp),
            contentAlignment = Alignment.Center
        ) {
            BasicText(
                text = stringResource(id = it.resourceId),
                overflow = TextOverflow.Ellipsis, maxLines = 1,
                style = TextStyle(
                    color = colors.tabTextActive.toCColor(),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal
                )
            )
        }
    }
}

@Composable
private fun TopBar(colors: LogoColors) {
    Row(
        Modifier
            .fillMaxWidth()
            .height(40.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Box(
            modifier = Modifier
                .fillMaxHeight()
                .weight(1f)
        ) {
            val dispatcher = LocalOnBackPressedDispatcherOwner.current!!.onBackPressedDispatcher
            Row(
                Modifier
                    .fillMaxHeight()
                    .wrapContentWidth()
                    .clickable { dispatcher.onBackPressed() }
                    .padding(start = 28.dp, end = 10.dp),

                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_arrow_back_edit),
                    contentDescription = stringResource(id = MR.strings.back.resourceId),
                    colorFilter = ColorFilter.tint(
                        colors.topBarText.toCColor(),
                        BlendMode.SrcAtop
                    ),
                    modifier = Modifier.padding(end = 9.dp)
                )
                BasicText(
                    text = stringResource(id = app.inspiry.projectutils.R.string.back),
                    overflow = TextOverflow.Ellipsis, maxLines = 1,
                    style = TextStyle(
                        color = colors.topBarText.toCColor(),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun Logos(
    viewModel: LogoViewModel,
    colors: LogoColors
) {

    val logos = viewModel.displayList.collectAsState(initial = listOf()).value.toMutableList()
    val hasPremium = viewModel.license.collectAsState(initial = false).value
    LazyVerticalGrid(
        modifier = Modifier.padding(end = 26.dp),
        cells = GridCells.Fixed(2)
    ) {
        items(
            items = logos,
            itemContent = {
                LogoItem(
                    logo = it,
                    logoCount = logos.size,
                    hasPremium = hasPremium,
                    colors = colors,
                    viewModel = viewModel
                ) { //on item selected
                    viewModel.onLogoSelected(logoItem = it)
                }
            }
        )
        item(
            content = {
                LogoItem(
                    logo = null,
                    logoCount = logos.size,
                    hasPremium = hasPremium,
                    colors = colors,
                    viewModel = viewModel
                ) { //on item selected
                    viewModel.addLogoAction(logosCount = logos.size)
                }
            }
        )
    }
}

@Composable
fun LogoItem(
    logo: LogoItem?,
    logoCount: Int,
    hasPremium: Boolean,
    colors: LogoColors,
    viewModel: LogoViewModel,
    onClick: () -> Unit
) {

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .padding(start = 26.dp, bottom = 26.dp)
            .size(150.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(colors.tabBgActive.toCColor())
            .clickable {
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        if (logo == null) {
            AddLogoItem()
            if (!hasPremium && logoCount > 0) LogoProBadge(colors)
        } else {
            SubcomposeAsyncImage(
                modifier = Modifier
                    .fillMaxSize()
                    .align(Alignment.Center),
                model = ImageRequest.Builder(LocalContext.current)
                    .data(logo.path)
                    .crossfade(true)
                    .build(),
                imageLoader = LocalContext.current.imageLoader,
                contentDescription = null,
                loading = {
                    CircularProgressIndicator()
                },
                contentScale = ContentScale.Fit
            )
            IconButton(modifier = Modifier
                .align(Alignment.TopEnd),
                onClick = {
                    viewModel.onRemoveLogoClick(logo.id)

                })
            {
                Image(
                    modifier = Modifier.size(25.dp),
                    painter = painterResource(id = R.drawable.ic_trash),
                    contentDescription = null
                )
            }
        }
    }
}

@Composable
private fun BoxScope.LogoProBadge(colors: LogoColors) {
    BasicText(
        text = "PRO", maxLines = 1,
        style = TextStyle(
            color = colors.proText.toCColor(),
            fontSize = 10.sp,
            fontFamily = FontFamily(Font(app.inspiry.projectutils.R.font.sf_pro_display_regular))
        ),
        modifier = Modifier
            .padding(10.dp)
            .border(
                1.dp,
                color = colors.proStroke.toCColor(),
                RoundedCornerShape(4.dp)
            )
            .padding(start = 5.dp, end = 5.dp, bottom = 1.dp)
            .align(Alignment.TopEnd)
    )
}

@Composable
private fun AddLogoItem() {
    Column {
        Image(
            modifier = Modifier
                .size(21.dp)
                .align(Alignment.CenterHorizontally),
            painter = painterResource(id = R.drawable.icon_add),
            contentDescription = null,
            colorFilter = ColorFilter.tint(Color(0xFFC4C4C4)),
        )
        BasicText(
            modifier = Modifier.padding(top = 11.dp),
            text = stringResource(id = MR.strings.add_logo.resourceId),
            maxLines = 1,
            style = TextStyle(
                color = Color(0xFFC4C4C4),
                fontSize = 12.sp,
            )
        )
    }
}

@Preview(name = "logo tab en", locale = "en")
@Composable
private fun LogoTabsPreview_en() {
    Tabs(category = listOf(MR.strings.your_logo), colors = LogoColors())
}

@Preview(name = "logo tab ru", locale = "ru")
@Composable
private fun LogoTabsPreview_ru() {
    Tabs(category = listOf(MR.strings.your_logo), colors = LogoColors())
}

@Preview(name = "addlogo item")
@Composable
private fun AddLogoPreview() {
    val colors = LogoColors()
    Box(
        modifier = Modifier
            .size(140.dp)
            .background(colors.tabBgActive.toCColor())
            .clip(RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        AddLogoItem()
        LogoProBadge(colors = colors)
    }
}