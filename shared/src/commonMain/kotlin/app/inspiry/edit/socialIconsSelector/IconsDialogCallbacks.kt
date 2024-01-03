package app.inspiry.edit.socialIconsSelector

import app.inspiry.views.InspView

interface IconsDialogCallbacks {
    fun applySocialIcon(newIconPath: String): InspView<*>?
    fun pickNewImage()
    fun disableSocialIcon()
}