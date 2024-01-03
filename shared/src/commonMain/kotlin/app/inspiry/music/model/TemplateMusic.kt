package app.inspiry.music.model

import dev.icerock.moko.parcelize.Parcelable
import dev.icerock.moko.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Serializable
@Parcelize
data class TemplateMusic(
    var url: String,
    var title: String,
    var artist: String,
    var album: String,
    // in millis
    var duration: Long,
    // in millis
    var trimStartTime: Long = 0,
    //from 0 to 100
    var volume: Int = 100,

    val tab: MusicTab,

    val albumId: Long,

    ) : Parcelable {

    override fun toString(): String {
        return "TemplateMusic(url='$url', title='$title', artist='$artist', album='$album', duration=$duration, trimStartTime=$trimStartTime, volume=$volume)"
    }
}