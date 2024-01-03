package app.inspiry.core.animator.clipmask

import app.inspiry.core.animator.clipmask.logic.ClipMaskSettings
import app.inspiry.core.animator.clipmask.logic.ClipMaskType
import app.inspiry.core.animator.clipmask.logic.MaskBrush
import app.inspiry.core.animator.clipmask.logic.MaskCircle
import app.inspiry.core.animator.clipmask.shape.ShapeTransform
import app.inspiry.core.animator.clipmask.shape.ShapeType
import app.inspiry.core.animator.clipmask.shape.isSquared
import app.inspiry.core.data.PointF
import app.inspiry.core.data.Rect
import app.inspiry.core.util.InspMathUtil
import app.inspiry.core.util.toRadian
import app.inspiry.views.path.CommonPath
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.native.concurrent.ThreadLocal


class MaskProvider(val maskPath: CommonPath, var width: Int, var height: Int) {

    private var maskBrush: MaskBrush? = null
    private var maskCircle: MaskCircle? = null

    private val shapeMath = ShapeMathHelper()

    var lastShapeBounds: Rect = Rect()

    private fun smoothVerticesToShape(transform: ShapeTransform, vertices: Array<PointF>) {

        val startPoint = shapeMath.transformPoint(vertices[0], transform)


        maskPath.moveTo(startPoint.first, startPoint.second)

        var index = 1

        while (index < vertices.lastIndex) {

            val point1 = shapeMath.transformPoint(vertices[index], transform)
            index++
            val point2 = shapeMath.transformPoint(vertices[index], transform)
            index++

            maskPath.quadTo(point1.first, point1.second, point2.first, point2.second)
        }
    }

    private fun verticesToShape(transform: ShapeTransform, vertices: Array<PointF>) {

        vertices.forEachIndexed { index, p ->

            val xy = shapeMath.transformPoint(p, transform)

            if (index == 0) maskPath.moveTo(xy.first, xy.second)
            else maskPath.lineTo(xy.first, xy.second)

        }
    }

    private fun circularShape(transform: ShapeTransform, radius: Float = 1f) {
        val center = shapeMath.transformPoint(PointF(0f,0f), transform)
        maskPath.addCircle(center.first, center.second, min(width / 2f, height / 2f) * radius)
    }

    private fun roundedShape(transform: ShapeTransform, roundingCorners: Float = 1f) {
        val anchor = shapeMath.transformPoint(PointF(-0.5f,-0.5f), transform)
        val radius = min(width, height) * roundingCorners
        maskPath.addRoundRect(anchor.first, anchor.second,width + 0f, height + 0f, rx = radius, ry = radius)
    }

    private fun ovalShape(transform: ShapeTransform) {
        val anchor = shapeMath.transformPoint(PointF(-0.5f,-0.47f), transform)
        maskPath.addOval(anchor.first, width + 0f, anchor.second, height * 0.97f)

    }

    private fun applyShape(settings: ClipMaskSettings) {
        shapeMath.isSquared = settings.shape?.isSquared() ?: false
        when (settings.shape) {

            ShapeType.STAR_FIVE -> {
                verticesToShape(settings.shapeTransform, FIVE_STAR)
            }
            ShapeType.SQUARE -> {
                verticesToShape(settings.shapeTransform, SIMPLE_RECT)
            }
            ShapeType.ROUNDED_RECT -> {
                roundedShape(settings.shapeTransform, 0.05f)
            }
            ShapeType.MORE_ROUNDED_RECT -> {
                roundedShape(settings.shapeTransform, 0.35f)
            }
            ShapeType.CIRCLE -> {
                circularShape(settings.shapeTransform)
            }
            ShapeType.TRIANGLE -> {
                smoothVerticesToShape(settings.shapeTransform, TRIANGLE_SHAPE)
            }
            ShapeType.WINDOW -> {
                smoothVerticesToShape(settings.shapeTransform, WINDOW_SHAPE)
            }
            ShapeType.HEXAGON -> {
                verticesToShape(settings.shapeTransform, HEXAGON_SHAPE)
            }
            ShapeType.OVAL -> {
                ovalShape(settings.shapeTransform)
            }
            ShapeType.STAR_MULTI -> {
                verticesToShape(settings.shapeTransform, MULTI_STAR)
            }
            ShapeType.HEART -> {
                smoothVerticesToShape(settings.shapeTransform, HEART_SHAPE)
            }
            ShapeType.FLY -> {
                smoothVerticesToShape(settings.shapeTransform, FLY_SHAPE)
            }
            ShapeType.NOTHING -> {
                verticesToShape(settings.shapeTransform, SIMPLE_RECT)
            }
            else -> {
                throw IllegalStateException("unsupported shape ${settings.shape?.name}")
            }
        }


        maskPath.close()

        val shapeWidth = shapeMath.wFactor.roundToInt()
        val shapeHeight = shapeMath.hFactor.roundToInt()
        val shapeX = shapeMath.wShift.roundToInt()
        val shapeY = shapeMath.hShift.roundToInt()
        lastShapeBounds.apply {
            left = shapeX
            top = shapeY
            right = shapeWidth + shapeX
            bottom = shapeY + shapeHeight
        }
    }

    fun updateMask(maskSettings: ClipMaskSettings) {
        maskPath.reset()
        when (maskSettings.maskType) {
            ClipMaskType.NONE -> {
                if (maskSettings.shape == null) throw (IllegalStateException(
                    "Mask provider should not be initialized if ClipMask = NONE and without shape"
                ))
            }
            ClipMaskType.BRUSH -> {
                setBrushMask(maskSettings = maskSettings)
            }
            ClipMaskType.CIRCULAR -> {
                setCircularClip(maskSettings = maskSettings)
            }
        }
        // can use inversion for any mask
        maskPath.updateFillType(maskSettings.inverse)

        if (maskSettings.shape != null) {
            applyShape(maskSettings)
        }
    }

    private fun setBrushMask(maskSettings: ClipMaskSettings) {
        mayInitMaskBrush()
        maskSettings.apply {
            viewWidth = width + 0f
            viewHeight = height + 0f
        }

        maskBrush!!.getMaskArray(maskSettings = maskSettings).forEach {
            maskPath.addRoundRect(
                it.left,
                it.top,
                it.right,
                it.bottom,
                it.roundBorderRadius,
                it.roundBorderRadius
            )
        }
    }

    private fun setCircularClip(maskSettings: ClipMaskSettings) {
        mayInitMaskCircle()
        val circle = maskCircle!!.getMaskArray(maskSettings = maskSettings)
        maskPath.addCircle(
            circle[0].centerX,
            circle[0].centerY,
            circle[0].radius
        )
    }

    private fun mayInitMaskBrush() {
        if (maskBrush == null) {
            maskBrush = MaskBrush()
        }
    }

    private fun mayInitMaskCircle() {
        if (maskCircle == null) {
            maskCircle = MaskCircle()
        }
    }

    fun updateSize(width: Int, height: Int) {
        this.width = width
        this.height = height
    }

    inner class ShapeMathHelper {
        private var angle = 0f
            set(value) {
                field = value
                cachedSinAngle = sin(value.toRadian())
                cachedCosAngle = cos(value.toRadian())
            }

        private var cachedSinAngle = 0f
        private var cachedCosAngle = 1f

        var hShift: Float = 0f
        private set
        var wShift: Float = 0f
        private set

        var wFactor: Float = 0f
            private set(value) {
                field = value
                wShift = (width - wFactor) / 2f
            }
        var hFactor: Float = 0f
            private set(value) {
                field = value
                hShift = (height - hFactor) / 2f
            }

        var isSquared: Boolean = true
            set(value) {
                field = value
                updateSize()
            }

        private fun updateSize() {
            if (!isSquared) {
                wFactor = width + 0f
                hFactor = height + 0f
            } else {
                wFactor = min(width, height) + 0f
                hFactor = wFactor + 0f
            }
        }

        private fun sinAngle(a: Float): Float {
            if (a != angle) angle = a
            return cachedSinAngle
        }

        private fun cosAngle(a: Float): Float {
            if (a != angle) angle = a
            return cachedCosAngle
        }

        fun transformPoint(p: PointF, shapeTransform: ShapeTransform): Pair<Float, Float> {
            val tx =
                p.x * cosAngle(shapeTransform.rotation) - p.y * sinAngle(shapeTransform.rotation)
            val ty =
                p.x * sinAngle(shapeTransform.rotation) + p.y * cosAngle(shapeTransform.rotation)

            val px = wFactor * (tx + 0.5f + shapeTransform.xOffset) + wShift
            val py = hFactor * (ty + 0.5f + shapeTransform.yOffset) + hShift

            return Pair(px, py)
        }
    }

    companion object {

        //todo shape improvements: task https://app.asana.com/0/1200151811933045/1202282157187703/f
        //line style
        val SIMPLE_RECT = arrayOf(
            PointF(-0.5f, -0.5f),
            PointF(0.5f, -0.5f),
            PointF(0.5f, 0.5f),
            PointF(-0.5f, 0.5f),
        )
        val HEXAGON_SHAPE = arrayOf(
            PointF( -0.5f, -0.18f),
            PointF( 0f, -0.54f),
            PointF( 0.5f, -0.18f),
            PointF( 0.5f, 0.18f),
            PointF( 0f, 0.54f),
            PointF( -0.5f, 0.18f),
        )

        val FIVE_STAR = InspMathUtil.starVertices(5, 0.3f, 0.53f, offsetY = 0.035f)
        val SQUARE_STAR = InspMathUtil.starVertices(4, 0.3f, 0.5f)
        val MULTI_STAR = InspMathUtil.starVertices(12, 0.4f, 0.5f)

        //smooth style
        //[point0: curvePoint1, endPoint1, curvePoint2, endPoint2...]
        val SMOOTH_SHAPE = arrayOf(
            PointF(-0.4f, -0.5f), //start point

            PointF(0.4f, -0.5f), // curve control point line1
            PointF(0.4f, -0.5f), // end point line1

            PointF(0.5f, -0.5f), // curve control point line1
            PointF(0.5f, -0.4f), // end point line1

            PointF(0.5f, -0.4f), //curve control point line2
            PointF(0.5f, 0.4f), //end point line2

            PointF(0.5f, 0.5f), // curve control point line1
            PointF(0.4f, 0.5f), // end point line1

            PointF(-0.4f, 0.5f), // curve control point line1
            PointF(-0.4f, 0.5f), // end point line1

            PointF(-0.5f, 0.5f),
            PointF(-0.5f, 0.4f),

            PointF(-0.5f, -0.4f),
            PointF(-0.5f, -0.4f),

            PointF(-0.5f, -0.5f),
            PointF(-0.4f, -0.5f),

            )
        val FLY_SHAPE = arrayOf(
            PointF(-0.5f, -0.4f),

            PointF(-0.2f, -0.6f),
            PointF(0.475f, -0.4f),

            PointF(0.52f, -0.3f),
            PointF(0.46f, 0f),

            PointF(0.38f, 0.3f),
            PointF(0.46f, 0.5f),

            PointF(0.05f, 0.3f),
            PointF(-0.4f, 0.35f),

            PointF(-0.3f, -0.1f),
            PointF(-0.5f, -0.4f),
        )
        val TRIANGLE_SHAPE = arrayOf(
            PointF( -0.1f, -0.43f),

            PointF( 0.0f, -0.57f),
            PointF( 0.1f, -0.43f),

            PointF( 0.48f, 0.3f),
            PointF( 0.48f, 0.3f),

            PointF( 0.535f, 0.45f),
            PointF( 0.365f, 0.5f),

            PointF( -0.365f, 0.5f),
            PointF( -0.365f, 0.5f),

            PointF( -0.535f, 0.45f),
            PointF( -0.48f, 0.3f),

            PointF( -0.1f, -0.43f),
            PointF( -0.1f, -0.43f),
        )

        val STAR_FIVE_SHAPE = arrayOf(
            PointF( -0.05f, -0.05f),

            PointF( 0f, -0.55f),
            PointF( 0.05f, 0.05f),

            PointF( 0.187f, 0.25f),
            PointF( 0.187f, 0.25f),

            PointF( 0.9f, -0.24f),
            PointF( 0.2f, -0.23f),

            PointF( 0.48f, -0.143f),
            PointF( 0.48f, -0.143f),

            PointF( 0.52f, -0.35f),
            PointF( 0.48f, -0.11f),

            PointF( 0.32f, -0.15f),
            PointF( 0.32f, -0.15f),

            PointF( 0.31f, -0.14f),
            PointF( 0.3f, -0.13f),

            PointF( 0.05f, 0.35f),
            PointF( 0.05f, 0.35f),
        )

        val WINDOW_SHAPE = arrayOf(
            PointF(0f, -0.4f),

            PointF(0.49f, -0.4f),
            PointF(0.49f, -0.1f),

            PointF(0.49f, 0.4f),
            PointF(0.49f, 0.4f),

            PointF(-0.49f, 0.4f),
            PointF(-0.49f, 0.4f),

            PointF(-0.49f, -0.1f),
            PointF(-0.49f, -0.1f),

            PointF(-0.49f, -0.4f),
            PointF(0f, -0.4f),
        )

        val HEART_SHAPE = arrayOf(
            PointF(0f, -0.4f),

            PointF(0.1f, -0.5f),
            PointF(0.25f, -0.48f),

            PointF(0.5f, -0.45f),
            PointF(0.49f, -0.2f),

            PointF(0.5f, 0.15f),
            PointF(0f, 0.45f),

            PointF(-0.5f, 0.15f),
            PointF(-0.49f, -0.2f),

            PointF(-0.5f, -0.45f),
            PointF(-0.25f, -0.48f),

            PointF(-0.1f, -0.5f),
            PointF(0f, -0.4f),
        )

    }

}

