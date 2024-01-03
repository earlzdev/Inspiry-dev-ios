package app.inspiry.stickers.helpers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import app.inspiry.stickers.StickersViewModel
import app.inspiry.stickers.providers.StickersProvider

class StickersViewModelFactory(val provider: StickersProvider,
                               val initialCategory: String?,
                               val initialStickerIndex: Int?) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return StickersViewModel(provider, initialCategory, initialStickerIndex) as T
    }
}