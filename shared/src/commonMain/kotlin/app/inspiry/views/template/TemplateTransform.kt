package app.inspiry.views.template

import app.inspiry.core.data.Size
import app.inspiry.core.manager.DebugManager
import app.inspiry.core.media.TemplateFormat

data class TemplateTransform(
    val scale: Float = 0f,
    val verticalOffset: Int = 0,
    val staticOffset: Int = 0,
    val containerSize: Size = Size(0,0),
    val aspectRatio: Float = TemplateFormat.story.aspectRatio(),
    val centerGravity: Float = 0f //when 1f - template will be moved to center of the container and fill it
) {
    init {
        if (DebugManager.isDebug && scale > 1f) throw IllegalStateException("template scale can't be > 1")
    }
}