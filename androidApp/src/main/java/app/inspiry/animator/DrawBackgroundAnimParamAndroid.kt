package app.inspiry.animator

import android.graphics.Path
import android.graphics.Shader
import app.inspiry.core.animator.text.DrawBackgroundAnimParam

open class DrawBackgroundAnimParamAndroid: DrawBackgroundAnimParam() {

    var clipPath: Path? = null
    var shader: Shader? = null


    override fun nullify() {
        super.nullify()
        shader = null
    }
}