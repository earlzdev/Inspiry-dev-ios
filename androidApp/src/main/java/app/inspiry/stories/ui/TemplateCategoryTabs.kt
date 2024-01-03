package app.inspiry.stories.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.inspiry.MR
import app.inspiry.R
import app.inspiry.core.data.templateCategory.TemplateCategory
import app.inspiry.core.data.templateCategory.TemplateCategoryIcon
import app.inspiry.main.ui.TemplateCategoriesColors
import app.inspiry.main.ui.TemplateCategoriesColorsLight
import app.inspiry.main.ui.TemplateCategoriesDimens
import app.inspiry.main.ui.TemplateCategoriesDimensPhone
import app.inspiry.utilities.toCColor
import app.inspiry.utils.autoScroll
import app.inspiry.utils.coloredShadow

private val LocalColors =
    compositionLocalOf<TemplateCategoriesColors> { TemplateCategoriesColorsLight() }
private val LocalDimens =
    compositionLocalOf<TemplateCategoriesDimens> { TemplateCategoriesDimensPhone() }

@Composable
fun TemplateCategoryTabs(
    templateCategories: List<TemplateCategory>, selectedIndex: Int,
    onItemClick: (templateCategoryIndex: Int) -> Unit
) {
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    Box(
        Modifier
            .height(LocalDimens.current.fullHeight.dp)
            .fillMaxWidth()
            .clickable(false) {},
        contentAlignment = Alignment.BottomCenter
    ) {
        LazyRow(
            state = listState,
            modifier = Modifier
                .height(LocalDimens.current.listHeight.dp)
                .coloredShadow(alpha = 0.2f, offsetY = 5.dp, shadowRadius = 15.dp)
                .background(LocalColors.current.background.toCColor()),
            verticalAlignment = Alignment.CenterVertically,
            contentPadding = PaddingValues(horizontal = LocalDimens.current.contentPadding.dp)
        ) {
            itemsIndexed(templateCategories) { index, item ->
                ItemCategory(menuItem = item, selected = index == selectedIndex) {
                    onItemClick(index)
                }

            }

            listState.autoScroll(scope = scope, selectedIndex)
        }
        Divider(color = LocalColors.current.bottomDivider.toCColor(), thickness = 1.dp)
    }
}

private fun getTabIcon(type: TemplateCategoryIcon): Int {
    return when (type) {
        TemplateCategoryIcon.NONE -> 0
        TemplateCategoryIcon.FIRE -> R.drawable.ic_fire
    }
}

@Composable
private fun ItemCategory(menuItem: TemplateCategory, selected: Boolean, onItemClick: () -> Unit) {
    Box(
        modifier = Modifier
            .wrapContentHeight()
            .wrapContentWidth()
            .padding(LocalDimens.current.itemContainerPadding.dp),
        contentAlignment = Alignment.TopEnd
    ) {
        val bg =
            if (selected) LocalColors.current.activeBackground.toCColor() else LocalColors.current.inactiveBackground.toCColor()
        Box(
            modifier = Modifier
                .height(LocalDimens.current.itemHeight.dp)
                .wrapContentWidth()
                .clip(RoundedCornerShape(LocalDimens.current.itemRounding.dp))
                .background(color = bg)
                .clickable(onClick = onItemClick),
            contentAlignment = Alignment.Center
        ) {
            val textColor =
                if (selected) LocalColors.current.activeText.toCColor() else LocalColors.current.inactiveText.toCColor()
            Text(
                modifier = Modifier
                    .padding(horizontal = LocalDimens.current.textPadding.dp),
                text = stringResource(menuItem.displayName.resourceId),
                fontSize = LocalDimens.current.fontSize.sp,
                color = textColor,
                fontWeight = FontWeight.Medium,
                maxLines = 1
            )
        }
        if (menuItem.icon != TemplateCategoryIcon.NONE) Image(
            painter = painterResource(id = getTabIcon(menuItem.icon)),
            contentDescription = null,
            modifier = Modifier
                .height(LocalDimens.current.iconSize.dp)
                .width(LocalDimens.current.iconSize.dp)
                .offset(
                    x = LocalDimens.current.iconOffset.dp,
                    y = -(LocalDimens.current.iconOffset.dp)
                ),
            contentScale = ContentScale.Inside
        )
    }
}

@Preview
@Composable
private fun CategoryItem() {
    ItemCategory(
        menuItem = TemplateCategory(
            "123",
            MR.strings.category_business,
            listOf(),
            TemplateCategoryIcon.FIRE
        ), selected = true
    ) {}
}

