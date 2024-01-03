package app.inspiry.logo

import app.inspiry.MR
import app.inspiry.core.database.data.LogoItem
import app.inspiry.core.manager.LicenseManager
import app.inspiry.core.util.createDefaultScope
import com.soywiz.klock.DateTime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlin.jvm.JvmOverloads

class LogoViewModel
@JvmOverloads constructor(
    val logoRepository: LogoRepository,
    licenseManager: LicenseManager,
    private val scope: CoroutineScope = createDefaultScope(),
    val subscribeAction: (() -> Unit),
    val pickLogoAction: ((LogoItem) -> Unit)
) {

    private val errorChannel = Channel<ErrorsLogos>()
    val error = errorChannel.receiveAsFlow()

    val categories = listOf(MR.strings.your_logo)

    val displayList = logoRepository.getLogosListFlow()

    val license = licenseManager.hasPremiumState


    private suspend fun addNewLogo(
        path: String,
        dateAdded: String,
        height: Long,
        width: Long
    ) {
        logoRepository.addLogo(path, dateAdded, height, width)
    }

    init {
        scope.launch {
            logoRepository.getNewMediasFlow().collect { list ->
                val logo = list.firstOrNull() ?: return@collect
                try {
                    addNewLogo(
                        logo.uri,
                        DateTime.now().toStringDefault(),
                        logo.size.height.toLong(),
                        logo.size.width.toLong()
                    )
                } catch (e: Exception) { // We need SQLiteConstraintException but it is android class
                    if (e.message?.startsWith("UNIQUE constraint failed") == true) {
                        errorChannel.send(ErrorsLogos.SAME_PATH_ERROR)
                    } else e.printStackTrace()
                }
            }
        }
    }

    private fun onInsertLogoFromLibraryClick() {
        scope.launch {
            logoRepository.getLogoFromLibrary()
        }
    }

    fun onRemoveLogoClick(id: Long) {
        scope.launch {
            logoRepository.removeLogo(id)
        }
    }

    fun onLogoSelected(logoItem: LogoItem) {
        scope.launch {
            pickLogoAction(logoItem)
        }
    }

    fun addLogoAction(logosCount: Int) {

        if (!license.value && logosCount > 0) subscribeAction()
        else onInsertLogoFromLibraryClick()

    }

}

enum class ErrorsLogos {
    SAME_PATH_ERROR
}