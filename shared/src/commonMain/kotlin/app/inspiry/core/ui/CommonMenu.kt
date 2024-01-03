package app.inspiry.core.ui

import dev.icerock.moko.resources.StringResource
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Generic CommonMenu
 * TODO here we can implement logic for any menu
 * (selected, highlight, items rotation on click)
 * this will simplify the UI and logic of instruments
 *
 */
class CommonMenu<T : Enum<T>>(private var iconSize: Int = 20): Iterable<T> {

    private val menuItems = mutableMapOf<T, CommonMenuItem<T>>()

    // todo var selected: MutableStateFlow<T?> = MutableStateFlow(null)

    fun setMenuItem(item: T, text: StringResource, icon: String) {
        menuItems[item] =
            CommonMenuItem(
                size = iconSize,
                text = text,
                icon = icon
            )
    }

    fun setMenuItem(item: T, text: StringResource, icon: String, mayBeSelected: Boolean) {
        menuItems[item] =
            CommonMenuItem(
                size = iconSize,
                text = text,
                icon = icon,
                mayBeSelected = mayBeSelected
            )
    }

    fun setMenuItem(item: T, text: StringResource, icon: String, inactiveIcon: String) {
        menuItems[item] =
            CommonMenuItem(
                size = iconSize,
                text = text,
                icon = icon
            )
    }

    fun removeMenuItem(item: T) {
        menuItems.remove(item)
    }

    fun setIcon(item: T, icon: String) {
        getMenuItem(item).icon = icon
    }

    fun setTextMenuItem(item: T, text: StringResource) {
        menuItems[item] = CommonMenuItem(
            size = iconSize,
            text = text
        )
    }

    fun setMenuItem(item: T, menuItem: CommonMenuItem<T>) {
        menuItems[item] = menuItem
    }

    fun getMenuItem(item: T): CommonMenuItem<T> {
        return menuItems[item] ?: throw IllegalStateException("unknown menu item: ${item.name}")
    }

    fun updateIconSize(size: Int) {
        iconSize = size
        menuItems.values.forEach { it.size = size }
    }

    fun updateIconSize(item: T, size: Int) {
        menuItems[item]?.let { it.size = size }
    }

    fun getIconName(item: T, active: Boolean = true): String =
        getMenuItem(item = item).run {
            (if (active) this.icon else this.inactiveIcon) ?: throw IllegalStateException("requested icon is null for ${item.name}")
        }

    fun getText(item: T) = getMenuItem(item = item).text

    fun getKeys() = menuItems.keys.toList()

    fun contains(item: T) = menuItems.keys.contains(item)

    override fun iterator(): MutableIterator<T> {
        return menuItems.keys.iterator()
    }

}

data class CommonMenuItem<T : Enum<T>>(
    var size: Int,
    var text: StringResource,
    var icon: String? = null,
    var inactiveIcon: String? = null,
    var mayBeSelected: Boolean = false,
    var action: ((T) -> Unit)? = null
)