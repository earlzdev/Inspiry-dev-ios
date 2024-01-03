package app.inspiry.views.path

import app.inspiry.core.media.MediaPath

interface InnerViewPath<PATH: CommonPath> {

    val media: MediaPath

    fun invalidateColorOrGradient()

    var drawPath: () -> PATH?
}