package app.inspiry.core.media

import app.inspiry.core.data.Size


enum class TemplateFormat(val analyticsName: String) {
    square("square"), horizontal("horizontal"),
    post("post_4x5"), story("story 9x16");

    fun aspectRatio(): Float = when(this) {
        square -> 1f
        horizontal -> 16/9f
        post -> 4/5f
        story -> 9/16f
    }

    fun matchHeightConstraintsFirst(storyFormatForH: Boolean): Boolean = when(this) {
        story -> {
            storyFormatForH
        }
        else -> true
    }

    fun getRenderingSize(): Size {
        return when (this) {
            square -> Size(1080, 1080)
            horizontal -> Size(1920, 1080)
            post -> Size(1080, 1350)
            story -> Size(1080, 1920)
        }
    }
}

