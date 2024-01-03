package app.inspiry.export

import kotlin.math.min

sealed class ExportState {
    data class Initial(override val imageElseVideo: Boolean) : ExportState() {
        override val whereToExport: WhereToExport?
            get() = null
        override val fromDialog: Boolean
            get() = throw UnsupportedOperationException()
    }

    data class UserPicked(
        override val imageElseVideo: Boolean,
        override val whereToExport: WhereToExport,
        override val fromDialog: Boolean
    ) : ExportState()

    data class RenderingInProcess(
        override val imageElseVideo: Boolean,
        override val whereToExport: WhereToExport,
        override val fromDialog: Boolean,
        val progress: Float?
    ) : ExportState()

    data class Rendered(
        override val imageElseVideo: Boolean,
        override val whereToExport: WhereToExport?, override val fromDialog: Boolean,
        val file: String,
        val localId: String? = null //for ios
    ) : ExportState()

    abstract val imageElseVideo: Boolean
    abstract val whereToExport: WhereToExport?
    abstract val fromDialog: Boolean
}

fun progressFloatToString(progress: Float): String {
    val intProgress = (progress * 100).toInt()
    return min(intProgress, 100).toString() + "%"
}
