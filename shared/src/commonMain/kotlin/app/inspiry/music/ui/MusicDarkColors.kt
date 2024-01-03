package app.inspiry.music.ui

import dev.icerock.moko.graphics.Color

object MusicDarkColors : MusicColors {
    override val background = Color(0x19, 0x19, 0x19, 0xff)
    override val tabTextActive = Color(0xE0, 0xE0, 0xE0, 0xff)
    override val tabTextInactive = Color(0x76, 0x76, 0x76, 0xff)
    override val albumBorderActive = Color(0xff, 0xff, 0xff, 0xff)
    override val albumBorderInactive = Color(0x26, 0x26, 0x26, 0xff)
    override val albumTextActive = Color(0xF2, 0xF2, 0xF2, 0xff)
    override val albumTextInactive = Color(0xBD, 0xBD, 0xBD, 0xff)
    override val searchTextInactive = Color(0x82, 0x82, 0x82, 0xff)
    override val searchTextActive = Color(0xCC, 0xCC, 0xCC, 0xff)
    override val searchBg = Color(0x33, 0x33, 0x33, 0xff)
    override val trackTextTitle = albumTextInactive
    override val trackTextSubtitle = albumTextActive
    override val trackPlayPauseBgInactive = Color(0x3C, 0x3C, 0x3C, 0xff)
    override val trackPlayPauseBgActive = Color(0x00, 0x00, 0x00, 0xaa)
    override val trackPlayPauseBgActivePlaceholder = Color(0xB6, 0xB6, 0xB6, 0xff)
    override val trackAddBg = Color(0x4F, 0x4F, 0x4F, 0xff)
    override val trackSelectedBg = Color(0xC4, 0xC4, 0xC4, 0x19)
    override val headerPreviewText = Color(0x68, 0x68, 0x68, 0xff)
    override val headerMusicCountText = Color(0xa5, 0xa5, 0xa5, 0xff)
    override val headerProgress = Color(0x60, 0x60, 0x60, 0xff)
    override val headerProgressTrack = Color(0x39, 0x39, 0x39, 0xff)
    override val searchEditCursor = Color(0x4C, 0x8B, 0xFD, 0xff)
    override val errorButtonBg = Color(0x3B, 0x3B, 0x3B, 0xff)
    override val musicThumbColorInside = Color(0xC4, 0xC4, 0xC4, 0xff)
    override val musicThumbColorOutside = Color(0xD5, 0x52, 0xEB, 0x99)
    override val musicTrackBg = musicThumbColorInside
    override val musicTrackProgress = Color(0xD5, 0x52, 0xEB, 0xff)
    override val trackSelectedImageBorder = Color(0xE5, 0xE5, 0xE5, 0x55)
    override val downloadDialogBg: Color
        get() = albumBorderInactive

    override val isLight: Boolean
        get() = false
}