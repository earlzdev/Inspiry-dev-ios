package app.inspiry.views.guideline

import app.inspiry.core.media.Alignment
import app.inspiry.core.util.toDegree
import app.inspiry.core.util.toRadian
import app.inspiry.views.InspView
import app.inspiry.views.template.InspTemplateView
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * @param targetEdge - it is the edge of view which we move, which will stick to the guideline
 * @param align - It is to where guideline will align
 */
abstract class GuideLine(
    val root: InspTemplateView,
    val targetEdge: Alignment,
    val align: Alignment,
    val orientation: Orientation,
    val offset: Int = 0
) {

    var isDisplayed: Boolean = false

    fun coordsDiff(inspView: InspView<*>): Int {
        val targetCoord = getTargetCoord(inspView)
        val guidelineCoord = getGuidelineCoord()

        return guidelineCoord - targetCoord
    }


    fun getGuidelineCoord(): Int {
        val coord: Int

        when (orientation) {
            Orientation.VERTICAL -> {

                coord = when (align) {
                    Alignment.center_start -> {
                        offset
                    }
                    Alignment.center -> {
                        root.viewWidth / 2 + offset
                    }
                    Alignment.center_end -> {
                        root.viewWidth - offset
                    }
                    else -> {
                        throw IllegalStateException("wrong alignment ${align}")
                    }
                }

            }
            Orientation.HORIZONTAL -> {

                coord = when (align) {
                    Alignment.top_center -> {
                        offset
                    }
                    Alignment.center -> {
                        root.viewHeight / 2 + offset
                    }
                    Alignment.bottom_center -> {
                        root.viewHeight - offset
                    }
                    else -> {
                        throw IllegalStateException("wrong alignment ${align}")
                    }
                }

            }
        }

        return coord
    }

    private fun getTargetCoord(inspView: InspView<*>): Int {

        val position = getPositionInParent(root, inspView)
        val height = inspView.viewHeight
        val width = inspView.viewWidth
        val rotation = inspView.getRealRotation().toRadian()
        return when (orientation) {
            Orientation.VERTICAL -> {

                when (targetEdge) {
                    Alignment.center_start -> {
                        position[0]
                    }
                    Alignment.center -> {
                        position[0] + ((width * cos(rotation) / 2f)).roundToInt() - ((height * sin(rotation) / 2f)).roundToInt()

                    }
                    Alignment.center_end -> {
                        position[0] + inspView.viewWidth
                    }
                    else -> {
                        throw IllegalStateException()
                    }
                }
            }
            Orientation.HORIZONTAL -> {

                when (targetEdge) {
                    Alignment.top_center -> {
                        position[1]
                    }
                    Alignment.center -> {
                        position[1] + ((width * sin(rotation) / 2f)).roundToInt() + ((height * cos(rotation) / 2f)).roundToInt()

                    }
                    Alignment.bottom_center -> {
                        position[1] + inspView.viewHeight
                    }
                    else -> {
                        throw IllegalStateException()
                    }
                }
            }
        }
    }


    protected abstract fun getPositionInParent(parent: InspTemplateView, child: InspView<*>): IntArray

    enum class Orientation {
        VERTICAL,
        HORIZONTAL
    }
}