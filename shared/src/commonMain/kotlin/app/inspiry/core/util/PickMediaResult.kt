package app.inspiry.core.util

import app.inspiry.core.data.Size
import app.inspiry.edit.instruments.PickedMediaType
import dev.icerock.moko.parcelize.Parcelable
import dev.icerock.moko.parcelize.Parcelize

@Parcelize
data class PickMediaResult(val uri: String, val type: PickedMediaType, val size: Size) :
    Parcelable
