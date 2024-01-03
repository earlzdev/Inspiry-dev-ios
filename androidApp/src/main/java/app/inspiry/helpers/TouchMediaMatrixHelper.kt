package app.inspiry.helpers

import android.animation.Animator
import android.animation.PropertyValuesHolder
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Matrix
import android.graphics.PointF
import android.graphics.RectF
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.animation.DecelerateInterpolator
import androidx.core.graphics.transform
import androidx.core.view.GestureDetectorCompat
import app.inspiry.core.data.SizeF
import app.inspiry.utils.dpToPixels
import app.inspiry.core.data.TransformMediaData
import app.inspiry.views.gestures.RotateGestureDetector
import kotlin.math.*

class TouchMediaMatrixHelper(
    context: Context,
    displaySize: SizeF,
    mediaSize: SizeF,
    private var onMatrixChangedListener: OnMatrixChangedListener?,
    defaultTransformMediaData: TransformMediaData,
    private var videoRotation: Float?
) : ScaleGestureDetector.OnScaleGestureListener,
    GestureDetector.OnGestureListener, RotateGestureDetector.OnRotateGestureListener {

    private val scaleDetector = ScaleGestureDetector(context, this)
        .apply { isQuickScaleEnabled = false }
    private val gestureDetector = GestureDetectorCompat(context, this)
    private val rotationDetector = RotateGestureDetector(context, this)
    private val transformMatrix = Matrix()
    private var lastFocusX = 0F
    private var lastFocusY = 0F
    private var translateAnimator: Animator? = null
    private var isScaleInProgress = false
    private var isRotateInProgress = false
    private var minWidth = 0F
    private var minHeight = 0F
    private var maxWidth = 0F
    private var maxHeight = 0F

    private var displayWidth = displaySize.width
    private var displayHeight = displaySize.height
    private val mediaWidth = mediaSize.width
    private val mediaHeight = mediaSize.height

    private val scaleFactorWidth: Float
        get() = displayWidth / mediaWidth

    private val scaleFactorHeight: Float
        get() = displayHeight / mediaHeight

    private var baseScaleFactor = 0F
    private var baseTranslateX = 0F
    private var baseTranslateY = 0F
    private val mrotate by lazy {
        Matrix()
    }

    init {
        gestureDetector.setIsLongpressEnabled(false)
        reset(defaultTransformMediaData)
    }

    /**
     * Setup up the initial state
     */
    fun reset(defaultTransformMediaData: TransformMediaData) {
        setupScaleBoundsParams()
        setupMatrix(defaultTransformMediaData)
    }

    fun updateDisplaySize(displaySize: PointF, currentTransformMedia: TransformMediaData) {
        if (displaySize.x > 0 && displaySize.y > 0 && (displayWidth != displaySize.x ||
                    displayHeight != displaySize.y)
        ) {
            displayWidth = displaySize.x
            displayHeight = displaySize.y
            reset(currentTransformMedia)
        }
    }

    fun setupMatrix(transformMediaData: TransformMediaData) {
        setupCenterCropMatrix()
        if (videoRotation == null) rotate(transformMediaData.rotate, 0f, 0f)
        else rDegress = transformMediaData.rotate
        scale(transformMediaData.scale, baseTranslateX, baseTranslateY)
        translate(
            transformMediaData.translateX * displayWidth,
            transformMediaData.translateY * displayHeight
        )

        onMatrixChanged(false)
    }

    private fun setupCenterCropMatrix() {
        baseScaleFactor = max(scaleFactorWidth, scaleFactorHeight)
        if (scaleFactorHeight > scaleFactorWidth) {
            baseTranslateX = (displayWidth - mediaWidth * baseScaleFactor) / 2F
        } else baseTranslateY = (displayHeight - mediaHeight * baseScaleFactor) / 2F
        transformMatrix.reset()
        scale(baseScaleFactor, 0F, 0F)
        translate(baseTranslateX, baseTranslateY)
    }

    private fun onMatrixChanged(fromUser: Boolean) {
        val values = FloatArray(9)
            .apply { transformMatrix.getValues(this) }
        val translateX = values[Matrix.MTRANS_X]
        val translateY = values[Matrix.MTRANS_Y]
        val scaleX = values[Matrix.MSCALE_X]
        val skewY = values[Matrix.MSKEW_Y]
        val scaleFactor = sqrt(scaleX * scaleX + skewY * skewY)
        val rotation = if (videoRotation != null) rDegress else
            -round(
                atan2(
                    values[Matrix.MSKEW_X],
                    values[Matrix.MSCALE_X]
                ) * (180 / Math.PI)
            ).toFloat()

        val transformImageData = TransformMediaData(
            scale = scaleFactor / baseScaleFactor,
            translateX = (translateX - baseTranslateX) / displayWidth,
            translateY = (translateY - baseTranslateY) / displayHeight,
            rotate = rotation
        )
        onMatrixChangedListener?.onMatrixChanged(transformMatrix, transformImageData, fromUser)
    }

    private fun setupScaleBoundsParams() {
        if (scaleFactorHeight > scaleFactorWidth) {
            minWidth = displayWidth * MIN_SCALE_FACTOR
            minHeight = 0F
            maxWidth = 0F
            maxHeight = max(displayHeight, mediaHeight) * MAX_SCALE_FACTOR
        } else {
            minWidth = 0F
            minHeight = displayHeight * MIN_SCALE_FACTOR
            maxWidth = max(displayWidth, mediaWidth) * MAX_SCALE_FACTOR
            maxHeight = 0f
        }
    }

    fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                stopTranslateAnimation()
                isScaleInProgress = false
                isRotateInProgress = false
            }
        }

        rotationDetector.onTouchEvent(event)
        scaleDetector.onTouchEvent(event)

        if (!isScaleInProgress || !isRotateInProgress) {
            if (scaleDetector.isInProgress) isScaleInProgress = true
            if (rotationDetector.isInProgress) isRotateInProgress = true

            if (!isScaleInProgress && !isRotateInProgress)
                gestureDetector.onTouchEvent(event)
        }
        return true
    }

    override fun onScroll(
        e1: MotionEvent?,
        e2: MotionEvent?,
        distanceX: Float,
        distanceY: Float
    ): Boolean {
        translate(-distanceX, -distanceY)
        onMatrixChanged(true)
        return true
    }

    private fun translate(distanceX: Float, distanceY: Float) {
        val bounds = getImageBounds()
        val distance = if (videoRotation != null) {
            val videoBounds = RectF(bounds)
            mrotate.reset()
            mrotate.setRotate(
                videoRotation!! + rDegress,
                (bounds.right + bounds.left) / 2f,
                (bounds.bottom + bounds.top) / 2f
            )
            videoBounds.transform(mrotate)
            PointF(distanceX, distanceY)
                .translateBinding(videoBounds)
                .translateBounds(videoBounds)
        } else PointF(distanceX, distanceY)
            .translateBinding(bounds)
            .translateBounds(bounds)
        if (distance.x != 0F || distance.y != 0F) {
            transformMatrix.postTranslate(distance.x, distance.y)
        }
    }

    private fun getImageBounds(): RectF {
        val res = RectF(0F, 0F, mediaWidth, mediaHeight)

        res.apply { transformMatrix.mapRect(this) }
        return res
    }

    private fun PointF.translateBounds(bounds: RectF): PointF {
        var distanceX = x
        var distanceY = y
        if (bounds.right + distanceX < 0) {
            distanceX = -bounds.right
        }
        if (bounds.left + distanceX > displayWidth) {
            distanceX = displayWidth - bounds.left
        }
        if (bounds.bottom + distanceY < 0) {
            distanceY = -bounds.bottom
        }
        if (bounds.top + distanceY > displayHeight) {
            distanceY = displayHeight - bounds.top
        }
        return PointF(distanceX, distanceY)
    }

    private fun PointF.translateBinding(bounds: RectF): PointF {
        var distanceX = x
        var distanceY = y
        if (abs(bounds.left + distanceX) <= TRANSLATE_ANCHOR_SENSITIVITY) {
            distanceX = -bounds.left
        }
        if (abs(bounds.top + distanceY) <= TRANSLATE_ANCHOR_SENSITIVITY) {
            distanceY = -bounds.top
        }
        if (abs(bounds.right + distanceX - displayWidth) <= TRANSLATE_ANCHOR_SENSITIVITY) {
            distanceX = displayWidth - bounds.right
        }
        if (abs(bounds.bottom + distanceY - displayHeight) <= TRANSLATE_ANCHOR_SENSITIVITY) {
            distanceY = displayHeight - bounds.bottom
        }
        return PointF(distanceX, distanceY)
    }

    override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
        lastFocusX = detector.focusX
        lastFocusY = detector.focusY
        return true
    }

    override fun onScale(detector: ScaleGestureDetector): Boolean {
        val focusX = detector.focusX
        val focusY = detector.focusY
        scale(detector.scaleFactor, focusX, focusY)
        translate(focusX - lastFocusX, focusY - lastFocusY)
        onMatrixChanged(true)
        lastFocusX = focusX
        lastFocusY = focusY
        return true
    }

    @Suppress("NAME_SHADOWING")
    private fun scale(scaleFactor: Float, focusX: Float, focusY: Float) {
        val bounds = getImageBounds()
        val scaleFactor = scaleFactor
            .scaleBinding(bounds)
            .scaleBounds(bounds)
        transformMatrix.postScale(scaleFactor, scaleFactor, focusX, focusY)
    }

    private fun Float.scaleBounds(bounds: RectF): Float {
        val imageWidth = bounds.width()
        val imageHeight = bounds.height()
        var scaleFactor = this
        if (minWidth != 0F && imageWidth * this < minWidth) {
            scaleFactor = max(scaleFactor, minWidth / imageWidth)
        }
        if (minHeight != 0F && imageHeight * this < minHeight) {
            scaleFactor = max(scaleFactor, minHeight / imageHeight)
        }
        if (maxWidth != 0F && imageWidth * this > maxWidth) {
            scaleFactor = min(scaleFactor, maxWidth / imageWidth)
        }
        if (maxHeight != 0F && imageHeight * this > maxHeight) {
            scaleFactor = min(scaleFactor, maxHeight / imageHeight)
        }
        return scaleFactor
    }

    private fun Float.scaleBinding(bounds: RectF): Float {
        val imageWidth = bounds.width()
        val imageHeight = bounds.height()
        return when {
            abs(imageWidth * this - displayWidth) <= SCALE_ANCHOR_SENSITIVITY -> displayWidth / imageWidth
            abs(imageHeight * this - displayHeight) <= SCALE_ANCHOR_SENSITIVITY -> displayHeight / imageHeight
            else -> this
        }
    }

    override fun onFling(
        e1: MotionEvent?,
        e2: MotionEvent,
        velocityX: Float,
        velocityY: Float
    ): Boolean {
        if (isFlingAvailable(velocityX, velocityY)) {
            val fromX = e2.x
            val fromY = e2.y
            startTranslateAnimation(
                fromX, fromY, fromX + velocityX * TRANSLATE_ANIMATION_DISTANCE_FACTOR,
                fromY + velocityY * TRANSLATE_ANIMATION_DISTANCE_FACTOR
            )
            return true
        }
        return false
    }

    private fun isFlingAvailable(velocityX: Float, velocityY: Float) =
        velocityX * velocityX + velocityY * velocityY > MINIMUM_FLING_VELOCITY * MINIMUM_FLING_VELOCITY

    private fun startTranslateAnimation(fromX: Float, fromY: Float, toX: Float, toY: Float) {
        stopTranslateAnimation()
        val valuesX = PropertyValuesHolder.ofFloat("x", fromX, toX)
        val valuesY = PropertyValuesHolder.ofFloat("y", fromY, toY)
        var lastX = fromX
        var lastY = fromY
        translateAnimator = ValueAnimator.ofPropertyValuesHolder(valuesX, valuesY)
            .apply {
                interpolator = DecelerateInterpolator()
                duration = TRANSLATE_ANIMATION_DURATION
                addUpdateListener {
                    val x = it.getAnimatedValue("x") as Float
                    val y = it.getAnimatedValue("y") as Float
                    translate(x - lastX, y - lastY)
                    onMatrixChanged(true)
                    lastX = x
                    lastY = y
                }
                start()
            }
    }

    private fun stopTranslateAnimation() {
        translateAnimator?.run {
            translateAnimator = null
            cancel()
        }
    }

    override fun onDown(e: MotionEvent?) = true
    override fun onScaleEnd(detector: ScaleGestureDetector) = Unit
    override fun onShowPress(e: MotionEvent?) = Unit
    override fun onSingleTapUp(e: MotionEvent?) = false
    override fun onLongPress(e: MotionEvent?) = Unit

    fun unregisterListener() {
        onMatrixChangedListener = null
    }

    fun rotate(degrees: Float, additionalPx: Float, additionalPy: Float) {
        //or getImageBounds.centerX with postRotate
        if (videoRotation == null) transformMatrix.preRotate(degrees, additionalPx, additionalPy)
    }

    private var rDegress = 0f

    private fun Float.bindRotation(): Float {
        var deltaRotation = this
        val values = FloatArray(9)
            .apply { transformMatrix.getValues(this) }
        //85
        val prevRotation: Float =
            -round(
                atan2(
                    values[Matrix.MSKEW_X],
                    values[Matrix.MSCALE_X]
                ) * (180 / Math.PI)
            ).toFloat()
        //delta = 3
        val currentRotation = prevRotation + deltaRotation.toDouble()
        val rotationResidue = abs(currentRotation) % 90
        if (rotationResidue <= ROTATE_ANCHOR_SENSITIVITY) {
            deltaRotation = -prevRotation % 90

            if (deltaRotation > 45)
                deltaRotation = 90 - deltaRotation
            else if (deltaRotation < -45) {
                deltaRotation += 90
            }
        }
        return deltaRotation
    }

    private fun Float.bindVideoRotation(): Float {
        var deltaRotation = this
        val prevRotation = rDegress
        val currentRotation = rDegress + deltaRotation.toDouble()
        //maybe need to add a delta here so that there is less logic
        //then rotationResidue will be ~+0
        var rotationResidue = abs(currentRotation) % 90
        if (rotationResidue > 45) rotationResidue = 90 - rotationResidue
        if (rotationResidue <= ROTATE_ANCHOR_SENSITIVITY) {
            deltaRotation = -prevRotation % 90
            if (deltaRotation > 45)
                deltaRotation = 90 - deltaRotation
            else if (deltaRotation < -45) {
                deltaRotation += 90
            }
        }
        return deltaRotation
    }

    override fun onRotate(detector: RotateGestureDetector): Boolean {

        if (videoRotation != null) {
            rDegress = (rDegress + (-detector.rotationDegreesDelta * 3).bindVideoRotation()) % 360
            if (rDegress < 0) rDegress += 360

        } else {
            val deltaRotation = -detector.rotationDegreesDelta * 3
            rotate(deltaRotation.bindRotation(), mediaWidth / 2f, mediaHeight / 2)
        }
        onMatrixChanged(true)
        return true
    }

    override fun onRotateBegin(detector: RotateGestureDetector): Boolean {
        //return abs(detector.rotationDegreesDelta) > 2
        return true
    }

    override fun onRotateEnd(detector: RotateGestureDetector) = Unit

    interface OnMatrixChangedListener {
        fun onMatrixChanged(
            matrix: Matrix,
            transformMediaData: TransformMediaData,
            fromUser: Boolean
        )
    }

    companion object {
        private val MINIMUM_FLING_VELOCITY = 200F.dpToPixels()
        private const val TRANSLATE_ANIMATION_DISTANCE_FACTOR = 0.15F
        private const val TRANSLATE_ANIMATION_DURATION = 400L
        private val TRANSLATE_ANCHOR_SENSITIVITY = 2F.dpToPixels()
        const val ROTATE_ANCHOR_SENSITIVITY = 2f
        private val SCALE_ANCHOR_SENSITIVITY = 5F.dpToPixels()
        private const val MIN_SCALE_FACTOR = 0.5F
        private const val MAX_SCALE_FACTOR = 4F
    }

}