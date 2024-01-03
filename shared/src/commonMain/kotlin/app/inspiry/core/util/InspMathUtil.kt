package app.inspiry.core.util

import app.inspiry.core.data.*
import kotlin.math.*

object InspMathUtil {
    /**
     * Scaling a rectangle and shifting it to center
     */
    fun scaleAndCentering(rect: Rect, factorX: Float, factorY: Float) {
        with(rect) {
            val correctWidth =
                (right * factorX).roundToInt()
            val correctHeight =
                (bottom * factorY).roundToInt()
            val shiftRight = (right - correctWidth) / 2
            val shiftBottom = (bottom - correctHeight) / 2
            top = shiftBottom
            bottom = correctHeight + shiftBottom
            left = shiftRight
            right = correctWidth + shiftRight
        }
    }

    /**
     * @return array points for star
     *
     * @param numSpikes - outer vertices count
     * @param innerRadius - inner vertices radius
     * @param outerRadius - outer vertices radius
     * @param offsetY - default offset Y
     */
    fun starVertices(numSharpCorners: Int, innerRadius: Float, outerRadius: Float, offsetY: Float = 0f): Array<PointF> {

        val numVertices = numSharpCorners * 2
        val angleStep: Float = TWO_PIf / numVertices
        return Array(size = numVertices) { i ->
            val x: Float
            val y: Float
            if (i % 2 == 0) {
                x = cos(angleStep * i - PIf / 2) * outerRadius
                y = sin(angleStep * i - PIf / 2) * outerRadius
            } else {
                x = cos(angleStep * i - PIf / 2) * innerRadius
                y = sin(angleStep * i - PIf / 2) * innerRadius
            }
            PointF(x, y + offsetY)
        }
    }

    /**
     * Round double value to $d decimal places
     * @param value - double value
     * @param d     - number of decimal places
     */
    fun roundDoubleTo(value: Double, d: Int) = (value * 10 * d).roundToInt() / (10.0 * d)

    /**
     * rotation of the rectangle around the point (x,y)
     * @param rect - rectangle
     * @param point around which the rectangle will rotate
     * @param alpha - rotation angle
     */
    fun rotateRectAround(rect: Rect, point: Pair<Float, Float>, angle: Float) {

        val deltaX = rect.left - point.first + rect.width() / 2f
        val deltaY = rect.top - point.second + rect.height() / 2f

        val alpha = angle * PI / 180.0

        val newX = deltaX * cos(alpha) - deltaY * sin(alpha) + point.first - rect.width() / 2f
        val newY = deltaX * sin(alpha) + deltaY * cos(alpha) + point.second - rect.height() / 2f

        val offsetX = (newX - rect.left).roundToInt()
        val offsetY = (newY - rect.top).roundToInt()

        rect.offset(offsetX, offsetY)
    }

    fun getArcAngle(radius: Float, arcLength: Float): Float {
        val alpha = arcLength / radius
        return alpha.toDegree()
    }

    fun convertAspectRatio(newAspectRatio: Float, size: SizeF, makeBigger: Boolean): SizeF {

        val oldAspectRatio = size.width / size.height

        var currentWidth = size.width
        var currentHeight = size.height

        if (oldAspectRatio > newAspectRatio) {
            if (makeBigger)
                currentHeight = currentWidth / newAspectRatio * oldAspectRatio
            else
                currentWidth = currentWidth / oldAspectRatio * newAspectRatio
        } else {
            if (makeBigger)
                currentWidth = currentHeight * newAspectRatio / oldAspectRatio
            else
                currentHeight = currentHeight / newAspectRatio * oldAspectRatio
        }

        return SizeF(currentWidth, currentHeight)
    }

    /**
     * Calculate angle between v1 and v2 in degree [0; 90], [90; 180], [-180; -90], [-90; 0]
     */
    fun calculateAngleDegree(v1: Vector, v2: Vector): Degree {
        val angleRadian = -atan2(v1.x * v2.y - v1.y * v2.x, v1.x * v2.x + v1.y * v2.y)
        return angleRadian.toDegree()
    }

    /**
     * Create a vector directed from the start point to the end point
     */
    fun createVector(start: PointF, end: PointF) =
        Vector(end.x - start.x, start.y - end.y)

    //minimum signed value (15 bit)
    const val SIZE_MIN_VALUE = -16384

    //maximum signed value (15 bit)
    const val SIZE_MAX_VALUE = 16384

    fun floatNumber(float: Float): Number {
        return float as Number
    }
}

fun RectF.toRect() : Rect {
    return Rect(left.roundToInt(), top.roundToInt(), right.toInt(), bottom.toInt())
}

typealias Degree = Float
typealias Radian = Float

fun Degree.toRadian(): Radian = this / 180f * PIf
fun Radian.toDegree(): Degree = this * 180f / PIf

const val PIf = PI.toFloat()
const val TWO_PIf = 2f * PIf