package app.inspiry.main.ui

import app.inspiry.core.ui.UIColors
import dev.icerock.moko.graphics.Color

abstract class MainScreenColors: UIColors {
    abstract val backgroundColor: Color
    abstract val instagramLinkTextColor: Color
    abstract val instagramButtonBack: Color
    abstract val instagramButtonText: Color
    abstract val newStoryButtonBack: Color
    abstract val emptyStoriesText: Color
}