package app.inspiry.views.text

import app.inspiry.core.animator.TextAnimationParams
import app.inspiry.core.media.MediaText
import app.inspiry.core.media.PartInfo
import app.inspiry.core.media.TextAlign

//TODO: drawing process could be abstracted more
interface InnerGenericText<Canvas> {
    val media: MediaText
    var fullText: String //it cannot be called "text" because there will be a conflict in the project for ios
    val genericTextHelper: GenericTextHelper<Canvas>?
    var needsRecompute: Boolean

    var radius: Float
    var currentFrame: Int

    var circularTextSize: Float

    var templateWidth: Int
    var templateHeight: Int

    /**
     * @return line number for char index
     */
    fun getLineForOffset(offset: Int): Int
    fun refresh()


    fun drawTextBackgroundsSingle(
        animParam: TextAnimationParams,
        canvas: Canvas,
        time: Double,
        out: Boolean, shadowMode: Boolean
    )
    fun drawPart(
        canvas: Canvas,
        time: Double,
        partInfo: PartInfo,
        textAnimationParams: TextAnimationParams,
        animatorOut: Boolean, partIndex: Int, partsCount: Int, lineNumber: Int
    )

    fun recompute()

    fun draw(animParam: TextAnimationParams, canvas: Canvas, time: Double, out: Boolean)

    var circularRadius: Float
    var circularGravity: TextAlign
}