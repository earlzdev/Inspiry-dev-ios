package app.inspiry.export

import app.inspiry.MR
import app.inspiry.core.manager.CommonClipBoardManager
import app.inspiry.core.manager.INSTAGRAM_DISPLAY_NAME
import app.inspiry.core.manager.ToastLength
import app.inspiry.core.manager.ToastManager
import dev.icerock.moko.resources.desc.ResourceFormattedStringDesc
import dev.icerock.moko.resources.format

class ExportCommonViewModel(
    private val toastManager: ToastManager,
    private val commonClipBoardManager: CommonClipBoardManager
) {

    fun onClickInstInspiry(toString: (ResourceFormattedStringDesc) -> String) {

        commonClipBoardManager.copyToClipboard(INSTAGRAM_DISPLAY_NAME)

        toastManager.displayToast(
            toString(
                MR.strings.saving_copied_clipboard.format(
                    INSTAGRAM_DISPLAY_NAME
                )
            ), ToastLength.LONG
        )
    }
}