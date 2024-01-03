package app.inspiry.music.ui

import app.inspiry.core.ui.UIColors
import dev.icerock.moko.graphics.Color

interface MusicColors: UIColors {
    val errorButtonBg: Color
    val background: Color
    val tabTextActive: Color
    val tabTextInactive: Color
    val albumBorderActive: Color
    val albumBorderInactive: Color
    val albumTextActive: Color
    val albumTextInactive: Color
    val searchTextInactive: Color
    val searchTextActive: Color
    val searchBg: Color
    val trackTextTitle: Color
    val trackTextSubtitle: Color
    val trackPlayPauseBgInactive: Color
    val trackPlayPauseBgActive: Color
    val trackPlayPauseBgActivePlaceholder: Color
    val trackAddBg: Color
    val trackSelectedBg: Color
    val headerPreviewText: Color
    val searchEditCursor: Color
    val musicThumbColorInside: Color
    val musicThumbColorOutside: Color
    val musicTrackBg: Color
    val musicTrackProgress: Color
    val trackSelectedImageBorder: Color
    val downloadDialogBg: Color
    val headerMusicCountText: Color
    val headerProgress: Color
    val headerProgressTrack: Color
}