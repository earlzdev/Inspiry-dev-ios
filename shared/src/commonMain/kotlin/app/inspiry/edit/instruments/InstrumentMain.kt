package app.inspiry.edit.instruments

enum class InstrumentMain {
    DEFAULT, TEXT, MOVABLE, MEDIA, SOCIAL_ICONS, TIMELINE, DEBUG, ADD_VIEWS, SLIDES;

    fun instrumentsDependOnView(): Boolean {
        return this != DEFAULT
    }
    fun isCloseable() = this != DEFAULT && this != TEXT && this != SLIDES && this != MEDIA

    fun hasDifferentModel(): Boolean = this == MOVABLE // instrument has different models for different media types. (eg colorViewModel for Image and Vector is different)
}
