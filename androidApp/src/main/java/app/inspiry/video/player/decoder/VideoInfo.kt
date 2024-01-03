package app.inspiry.video.player.decoder

interface VideoInfo {
    val videoSize: TextureSize
    val videoDurationUs: Long
}

data class TextureSize(val width: Int, val height: Int, val rotation: Float = 0f) {
    override fun toString(): String {
        return "TextureSize(width=$width, height=$height, rotation=$rotation)"
    }
}