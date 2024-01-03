package app.inspiry.edit.instruments

enum class PickedMediaType {
    MEDIA, VECTOR, VIDEO
}

data class PickImageConfig(
    var maxSelectable: Int = 1,
    var imageOnly: Boolean = false,
    var pickedMediaType: PickedMediaType = PickedMediaType.MEDIA,
    var resultViewIndex: Int = -1,
    var replaceMedia: Boolean = false
)
