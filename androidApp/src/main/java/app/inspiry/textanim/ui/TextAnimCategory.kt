package app.inspiry.textanim.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.inspiry.utilities.toCColor
import app.inspiry.utils.autoScroll

private val LocalColors =
    compositionLocalOf<AnimationCategoriesColors> { AnimationCategoriesColorsLight() }
private val LocalDimens =
    compositionLocalOf<AnimationCategoriesDimens> { AnimationCategoriesDimensPhone() }

@Composable
fun TextAnimCategory(
    items: List<String>, selectedIndex: Int,
    onItemClick: (templateCategoryIndex: Int) -> Unit
) {
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    LazyRow(
        state = listState, modifier = Modifier
            .fillMaxHeight()
    ) {
        itemsIndexed(items) { index, item ->
            ItemCategory(itemText = item, selected = index == selectedIndex) {
                onItemClick(index)
            }

        }

        listState.autoScroll(scope = scope, selectedIndex)
    }
}


@Composable
private fun ItemCategory(itemText: String, selected: Boolean, onItemClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxHeight()
            .wrapContentWidth()
            .padding(LocalDimens.current.itemContainerPadding.dp),
        contentAlignment = Alignment.Center,
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
                    .defaultMinSize(74.dp)
                    .padding(horizontal = LocalDimens.current.textPadding.dp),
                text = itemText,
                fontSize = LocalDimens.current.fontSize.sp,
                color = textColor,
                fontWeight = FontWeight.Medium,
                maxLines = 1
            )
        }
    }
}

