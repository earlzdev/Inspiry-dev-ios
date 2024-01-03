package app.inspiry.edit.instruments.ui

import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset

/**
 * This is for horizontal only.
 *  needs some logic improvement for vertical or both
 */
class DraggableListState(
    val lazyListState: LazyListState,
    private val onMove: (Int, Int) -> Unit
) {
    var draggedDistance by mutableStateOf(0f) //use Offset for x and y
    var draggedElementInfo by mutableStateOf<LazyListItemInfo?>(null)
    var draggedItemIndex by mutableStateOf<Int?>(null)

    var hoveredItemIndex by mutableStateOf<Int?>(null)

    val draggedOffset: Float?
        get() = draggedItemIndex
            ?.let {
                lazyListState.layoutInfo.visibleItemsInfo.getOrNull(it - lazyListState.layoutInfo.visibleItemsInfo.first().index)
            }
            ?.let { item ->
                (draggedElementInfo?.offset ?: 0f).toFloat() + draggedDistance - item.offset
            }

    val currentElement: LazyListItemInfo?
        get() = draggedItemIndex?.let {
            lazyListState.layoutInfo.visibleItemsInfo.getOrNull(it - lazyListState.layoutInfo.visibleItemsInfo.first().index)
        }

    fun onDragStart(offset: Offset, selectedIndex: Int) {
        lazyListState.layoutInfo.visibleItemsInfo
            .firstOrNull { item ->
                selectedIndex == item.index && (offset.x.toInt() in item.offset..(item.offset + item.size))
            }?.also {
                draggedItemIndex = it.index
                draggedElementInfo = it
            }
    }

    fun onDragFinished() {
        draggedItemIndex?.let { dragged ->
            hoveredItemIndex?.let { hovered ->
                onDragCanceled()
                onMove(dragged, hovered)
            }
        }
    }

    fun onDragCanceled() {

        draggedDistance = 0f
        draggedItemIndex = null
        draggedElementInfo = null
    }

    fun elementOffsetByIndex(currentIndex: Int): Float {
        return if (hoveredItemIndex != null) {
            if (currentIndex >= hoveredItemIndex!! && (currentIndex < (draggedItemIndex ?: 0))
            ) lazyListState.layoutInfo.visibleItemsInfo[draggedItemIndex ?: 0].size + 0f
            else
                if (currentIndex <= hoveredItemIndex!! && currentIndex > (draggedItemIndex ?: 999)
                ) 0f - lazyListState.layoutInfo.visibleItemsInfo[draggedItemIndex ?: 0].size
                //todo Using draggedItemIndex is a bad idea. ^ But in our case, this is necessary,
                // since we have a pinned element that differs in size and should not move.
                // And the sizes of all elements are the same now
                else 0f
        } else 0f
    }

    fun onDrag(offset: Offset) {
        draggedDistance += offset.x
        val fullOffsetX = (currentElement
            ?.offset ?: 0) + draggedDistance
        lazyListState.layoutInfo.visibleItemsInfo
            .firstOrNull { item ->
                fullOffsetX.toInt() in item.offset..(item.size + item.offset)
            }?.also {
                hoveredItemIndex = it.index
            }
    }
}