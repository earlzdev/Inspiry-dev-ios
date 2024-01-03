package app.inspiry.logo

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import app.inspiry.core.util.PickMediaResult
import app.inspiry.edit.instruments.PickImageConfig
import app.inspiry.helpers.MatisseActivityResult
import app.inspiry.logo.data.LogoGetFromLibrary
import app.inspiry.utils.ImageUtils
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class LogoGetFromLibraryImpl(
    private val activity: AppCompatActivity
) : LogoGetFromLibrary {

    private val resultChannel = Channel<List<PickMediaResult>>()
    override val newMediasFlow = resultChannel.receiveAsFlow()

    override suspend fun getLogoFromLibrary() {
            if (ImageUtils.isMediaChooserPrepared(activity, config = PickImageConfig(imageOnly = true))) {
                getContent.launch(Unit)
        }
    }

    private val getContent = activity.registerForActivityResult(MatisseActivityResult())
    { matisseResult ->
       activity.lifecycleScope.launch {
            resultChannel.send(matisseResult)
       }
    }

}