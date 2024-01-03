package app.inspiry.views.template

enum class RecordMode {
    // user has not selected recordMode yet
    NONE,
    // it is used in android before the user has picked actual record mode, but when has entered ExportScreen
    UNSPECIFIED,
    // Rendering a single image
    IMAGE,
    // Rendering a video
    VIDEO;

    val started: Boolean
        get() {
            return this != NONE && this != UNSPECIFIED
        }

    companion object {
        fun getForRecord(imageElseVideo: Boolean) = if (imageElseVideo) IMAGE else VIDEO
    }
}