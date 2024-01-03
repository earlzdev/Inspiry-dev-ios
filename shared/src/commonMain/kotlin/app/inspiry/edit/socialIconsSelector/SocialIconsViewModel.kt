package app.inspiry.edit.socialIconsSelector

import app.inspiry.core.util.FileUtils
import app.inspiry.edit.instruments.BottomInstrumentsViewModel
import app.inspiry.views.InspView
import app.inspiry.views.media.InspMediaView
import app.inspiry.views.vector.InspVectorView
import kotlinx.coroutines.flow.MutableStateFlow

class SocialIconsViewModel(inspView: InspView<*>) :
    BottomInstrumentsViewModel {

    val currentStickerPath =
        MutableStateFlow<String?>(null)

    var currentView: InspView<*> = inspView

    fun getIconsPath(): MutableList<String> {
        val iconList = mutableListOf<String>()
        val defaultSource =
            (currentView as? InspMediaView)?.media?.defaultSource
                ?: (currentView as? InspVectorView)?.media?.defaultSource
        defaultSource?.let {
            if (!it.contains(Regex("sticker-resources\\/social\\/Social_\\d+\\.json")) && it.endsWith(
                    ".json"
                )
            )
                iconList.add(it)
        }
        STICKERS_DEFAULT_LIST.forEach {
            iconList.add("${FileUtils.ASSETS_SCHEME}://sticker-resources/social/Social_$it.json")
        }
        return iconList
    }

    var onSelectedChanged: ((InspView<*>) -> Unit)? = null

    override fun onSelectedViewChanged(newSelected: InspView<*>?) {
        newSelected?.let {
            currentView = it
            onSelectedChanged?.invoke(it)
        }

    }

    companion object {
        //icon numbers in the list to choosing social icons
        val STICKERS_DEFAULT_LIST = listOf(1, 2, 3, 4, 5, 6, 8, 9, 15, 16)
    }
}