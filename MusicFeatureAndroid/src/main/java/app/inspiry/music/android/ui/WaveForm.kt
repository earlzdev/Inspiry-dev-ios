package app.inspiry.music.android.ui

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.content.res.TypedArray
import android.graphics.*
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.View
import android.view.ViewConfiguration
import android.view.animation.DecelerateInterpolator
import android.widget.EdgeEffect
import androidx.core.content.ContextCompat
import app.inspiry.music.android.BuildConfig
import app.inspiry.music.android.R
import app.inspiry.music.util.WaveformUtils.MIN_WAVEFORM_LEVEL
import app.inspiry.core.log.KLogger
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.parameter.parametersOf
import java.lang.Math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sign

class WaveForm(
    context: Context, attrs: AttributeSet
) : View(context, attrs), KoinComponent {

    private var directionOnTravel = 0f
    private var displacement = 0f
    private var flingAnimation = ValueAnimator()
    private val mVelocityTracker: VelocityTracker = VelocityTracker.obtain()
    private var mMaximumFlingVelocity: Float = 0F
    private var mLeftEdge: EdgeEffect? = null
    private var mRightEdge: EdgeEffect? = null
    private var singleWaveSpace = 0f
    private var singleWaveWidth = 0f
    private var rectWhiteWave = RectF()
    private var rectPurpleWave = RectF()
    private val rectF: RectF = RectF()
    private var centerShift = 0
    private var widthStrokeSign = 0f
    private var widthView = 0
    private var heightView = 0
    private var interpolatedArray: FloatArray = INITIAL_DATA
    private val wavePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val backgroundWave = Paint(Paint.ANTI_ALIAS_FLAG)
    private val whitePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val purplePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var centerLeftLine = 0f
    private var centerRightLine = 0f
    private var pictureBlackWave = Picture()
    private var pictureWhiteWave = Picture()
    private var picturePurpleWave = Picture()
    private var firstTouchX = 0f
    private var shiftWaveScrolling = 0f
    private var startValue = 0f
    private var touchSlop: Float
    private var durationSong: Long = 0
    private var lengthTemplate = 0.0
    private var rightBorderPurple = 0f

    private var playPositionReset = false

    var playPosition = 0L
        private set
    var leftLinePositionToMillis = 0L
        private set
    var rightLinePositionToMillis = 0L
        private set

    // true when array gain was set
    var isInitialed = false
        private set

    var arrayGain: FloatArray = INITIAL_DATA
        set(value) {
            field = value
            requestLayout()
        }

    var startPositionListener: StartPositionListener? = null
    var waveScrollListener: WaveScrollListener? = null

    var isInScroll = false


    val logger: KLogger by inject {
        parametersOf("music:WaveformView")
    }

    init {

        val configuration = ViewConfiguration.get(context)
        mMaximumFlingVelocity = configuration.scaledMaximumFlingVelocity.toFloat()
        touchSlop = configuration.scaledTouchSlop.toFloat()

        val attrArray: TypedArray =
            context.obtainStyledAttributes(attrs, R.styleable.InspiryWave)
        try {
            singleWaveSpace = attrArray.getDimension(
                R.styleable.InspiryWave_spacing,
                resources.getDimension(R.dimen.spacing_wave)
            )
            singleWaveWidth =
                attrArray.getDimension(
                    R.styleable.InspiryWave_waveWidth,
                    resources.getDimension(R.dimen.wave_width)
                )
            widthStrokeSign =
                attrArray.getDimension(
                    R.styleable.InspiryWave_widthStrokeSign,
                    resources.getDimension(R.dimen.width_stroke_sign)
                )
            centerLeftLine =
                attrArray.getDimension(
                    R.styleable.InspiryWave_centerLeftMark,
                    resources.getDimension(R.dimen.center_line_left)
                )
            wavePaint.strokeWidth = widthStrokeSign
            purplePaint.strokeWidth = widthStrokeSign
            whitePaint.strokeWidth = widthStrokeSign
            wavePaint.color =
                ContextCompat.getColor(context, R.color.unselected_area_wave_form_color)

            whitePaint.color = Color.WHITE
            purplePaint.color =
                ContextCompat.getColor(context, R.color.play_wave_form_color)

            backgroundWave.color = attrArray.getColor(
                R.styleable.InspiryWave_backgroundCroppedWave,
                Color.TRANSPARENT
            )
            mLeftEdge = EdgeEffect(context)
            mRightEdge = EdgeEffect(context)

        } finally {
            attrArray.recycle()
        }
        setupUpFling()
    }

    fun setDurations(durationSong: Long, lengthTemplate: Double) {
        this.durationSong = durationSong
        this.lengthTemplate = lengthTemplate
        requestLayout()
    }

    fun setInitializedAsIs() {
        isInitialed = true
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        heightView = MeasureSpec.getSize(heightMeasureSpec)
        widthView = MeasureSpec.getSize(widthMeasureSpec)
        rectWhiteWave.bottom = heightView.toFloat()
        rectPurpleWave.bottom = heightView.toFloat()
        centerRightLine = widthView - centerLeftLine

        val wholeWaveWidth: Float

        if (isWaveShort) {
            wholeWaveWidth = widthView.toFloat() - centerLeftLine * 2
        } else {
            wholeWaveWidth =
                (durationSong * (widthView - centerLeftLine * 2) / lengthTemplate).toFloat()
        }

        val numOfLevelsRequired = (wholeWaveWidth / (singleWaveSpace + singleWaveWidth)).toInt()

        interpolatedArray = interpolateData(arrayGain, numOfLevelsRequired)

        logger.debug {
            "gain size ${arrayGain.size}, " +
                    "interpolatedSize ${interpolatedArray.size}," +
                    ", numOfLevelsRequired ${numOfLevelsRequired}" +
                    ", durationSong ${durationSong}"
        }


        pictureBlackWave = getPicture(wavePaint, backgroundWave, wholeWaveWidth)
        pictureWhiteWave = getPicture(whitePaint, backgroundWave, wholeWaveWidth)
        picturePurpleWave = getPicture(purplePaint, backgroundWave, wholeWaveWidth)


        val shiftWaveScrolling =
            ((playPosition * pictureBlackWave.width) / durationSong.toFloat())

        setShiftWaveScrolling(shiftWaveScrolling)
        getPositionLeftRightLineToMillis(false)
        playPositionTo(playPosition, false)
        notifyStartPositionListener(false)

        mLeftEdge?.setSize(heightView, widthView)
        mRightEdge?.setSize(heightView, widthView)
        invalidate()

        if (!arrayGain.contentEquals(INITIAL_DATA))
            isInitialed = true
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        canvas.save()
        canvas.translate(-shiftWaveScrolling, 0f)
        canvas.drawPicture(pictureBlackWave)
        canvas.restore()

        canvas.save()
        canvas.translate(-shiftWaveScrolling, 0f)

        rectWhiteWave.left = centerLeftLine + shiftWaveScrolling
        rectWhiteWave.right = centerRightLine + shiftWaveScrolling

        canvas.clipRect(rectWhiteWave)
        canvas.drawPicture(pictureWhiteWave)
        canvas.restore()


        canvas.save()
        canvas.translate(-shiftWaveScrolling, 0f)

        rectPurpleWave.left = centerLeftLine + shiftWaveScrolling

        rectPurpleWave.right = if (CLIP_PROGRESS_LINE)
            max(rectPurpleWave.left, min(rightBorderPurple, centerRightLine + shiftWaveScrolling))
        else rightBorderPurple

        canvas.clipRect(rectPurpleWave)
        canvas.drawPicture(picturePurpleWave)
        canvas.restore()


        canvas.save()
        canvas.drawLine(
            centerLeftLine,
            heightView.toFloat(),
            centerLeftLine,
            0f,
            purplePaint
        )
        canvas.drawLine(
            centerRightLine,
            heightView.toFloat(),
            centerRightLine,
            0f,
            purplePaint
        )
        canvas.restore()
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)

        val overScrollMode = overScrollMode
        if (overScrollMode == OVER_SCROLL_ALWAYS || (overScrollMode == OVER_SCROLL_IF_CONTENT_SCROLLS)) {

            if (!mLeftEdge?.isFinished!!) {
                canvas.save()
                canvas.translate(0f, heightView.toFloat())
                canvas.rotate(270f)
                mLeftEdge?.draw(canvas)
                canvas.restore()
                invalidate()
            }
            if (!mRightEdge?.isFinished!!) {
                canvas.save()
                canvas.translate(widthView.toFloat(), 0f)
                canvas.rotate(90f)
                mRightEdge?.draw(canvas)
                canvas.restore()
                invalidate()
            }
        } else {
            mLeftEdge?.finish()
            mRightEdge?.finish()
        }
    }

    fun setShiftWaveScrolling(value: Float) {
        shiftWaveScrolling = 0f.coerceAtLeast(value)
            .coerceAtMost((pictureBlackWave.width - widthView.toFloat()))
    }

    private fun isShiftWaveScrollingReachedEdge() = isShiftWaveScrollingReachedLeftEdge() ||
            isShiftWaveScrollingReachedRightEdge()

    private fun isShiftWaveScrollingReachedLeftEdge() = shiftWaveScrolling <= 0
    private fun isShiftWaveScrollingReachedRightEdge() = shiftWaveScrolling >=
            (pictureBlackWave.width - widthView)

    private fun setupUpFling() {

        flingAnimation.addUpdateListener {
            setShiftWaveScrolling((it.animatedValue as Int).toFloat())

            if (isShiftWaveScrollingReachedEdge()) {
                flingAnimation.cancel()
            }

            if (pictureBlackWave.width > widthView) {
                if (isShiftWaveScrollingReachedLeftEdge()) pullEdgeEffect(
                    widthView.toFloat(),
                    1 - displacement,
                    mLeftEdge!!
                )
                if (isShiftWaveScrollingReachedRightEdge())
                    pullEdgeEffect(widthView.toFloat(), displacement, mRightEdge!!)
            }

            getPositionLeftRightLineToMillis(true)
            notifyStartPositionListener(true)
            invalidate()
        }

        flingAnimation.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator?) {

            }

            override fun onAnimationEnd(animation: Animator?) {
                post {
                    onScrollStateChange(false)
                }
            }

            override fun onAnimationCancel(animation: Animator?) {

            }

            override fun onAnimationRepeat(animation: Animator?) {

            }

        })
    }

    private fun onScrollStateChange(isInScroll: Boolean) {
        logger.debug { "onScrollStateChange ${isInScroll}" }
        this.isInScroll = isInScroll
        if (!isInScroll) playPositionReset = false
        waveScrollListener?.onWaveScrollChanged(isInScroll)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val touchPointX = event.x
        val touchPointY = event.y
        mVelocityTracker.addMovement(event)

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                if (flingAnimation.isRunning) flingAnimation.cancel()
                firstTouchX = touchPointX
                startValue = shiftWaveScrolling
                invalidate()
                return true
            }

            MotionEvent.ACTION_UP -> {
                if (isInScroll) {
                    setShiftWaveScrolling(firstTouchX - touchPointX + startValue)
                    startAnimationFling(event)

                    if (!flingAnimation.isRunning) {
                        onScrollStateChange(false)
                    }

                }
                invalidate()
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                directionOnTravel = firstTouchX - touchPointX
                displacement = touchPointY / widthView
                mLeftEdge?.onRelease()
                mLeftEdge?.finish()
                mRightEdge?.onRelease()
                mRightEdge?.finish()

                if (isWaveShort) {
                    onMoveEdgeEffect()
                } else {
                    if (!isInScroll) {
                        onScrollStateChange(true)
                    }

                    setShiftWaveScrolling(firstTouchX - touchPointX + startValue)
                    onMoveEdgeEffect()
                    getPositionLeftRightLineToMillis(true)
                    notifyStartPositionListener(true)
                }
                invalidate()

                return true
            }
        }
        return true
    }

    private val isWaveShort: Boolean
        get() {
            return lengthTemplate >= durationSong
        }

    private fun onMoveEdgeEffect() {
        if (isShiftWaveScrollingReachedLeftEdge() && (directionOnTravel) < 0) {
            pullEdgeEffect(
                abs(directionOnTravel) / widthView,
                1 - displacement,
                mLeftEdge!!
            )
        }
        if (isShiftWaveScrollingReachedRightEdge() && (directionOnTravel) > 0) {
            pullEdgeEffect(
                abs(directionOnTravel) / widthView,
                displacement,
                mRightEdge!!
            )
        }
    }

    private fun pullEdgeEffect(deltaDistance: Float, displacement: Float, edgeEffect: EdgeEffect) {
        edgeEffect.onPull(
            deltaDistance,
            displacement
        )
    }

    private fun startAnimationFling(event: MotionEvent) {
        val pointerId = event.getPointerId(0)
        mVelocityTracker.computeCurrentVelocity(1000, mMaximumFlingVelocity)
        val velocityX = mVelocityTracker.getXVelocity(pointerId)
        mVelocityTracker.clear()

        var ratio: Float
        var duration: Long
        when (abs(pxToMm(velocityX))) {
            in 0.0..10.0 -> {
                return
            }
            in 10.0..25.0 -> {
                ratio = 0.003f
                duration = 500
            }
            in 25.0..100.0 -> {
                ratio = 0.01f
                duration = 900
            }
            in 100.0..200.0 -> {
                ratio = 0.05f
                duration = 900
            }
            in 200.0..300.0 -> {
                ratio = 0.1f
                duration = 1000
            }
            in 300.0..400.0 -> {
                ratio = 0.2f
                duration = 1100
            }
            in 400.0..500.0 -> {
                ratio = 0.3f
                duration = 1100
            }
            in 500.0..600.0 -> {
                ratio = 0.4f
                duration = 1100
            }
            in 600.0..700.0 -> {
                ratio = 0.5f
                duration = 1100
            }
            in 700.0..800.0 -> {
                ratio = 0.8f
                duration = 1100
            }
            in 800.0..900.0 -> {
                ratio = 0.9f
                duration = 1100
            }
            else -> {
                ratio = 1f
                duration = 1300
            }
        }
        ratio *= sign(pxToMm(velocityX) / 5)

        val flingShift = (ratio * pictureBlackWave.width).toInt()

        flingAnimation.interpolator = DecelerateInterpolator(1.5f)
        flingAnimation.duration = duration
        flingAnimation.setIntValues(
            shiftWaveScrolling.toInt(),
            (shiftWaveScrolling - flingShift).toInt()
        )
        flingAnimation.start()
    }

    private fun getPicture(paint: Paint, background: Paint, widthPicture: Float): Picture {
        val picture = Picture()
        val canvas: Canvas
        val spaceForDrawing = singleWaveSpace
        val singleWaveWidth = singleWaveWidth


        var waveLineDrawingCoordinate = 0f
        var widthPicture = widthPicture

        waveLineDrawingCoordinate += centerLeftLine
        widthPicture += centerLeftLine * 2

        canvas = picture.beginRecording(
            widthPicture.toInt(),
            heightView
        )

        rectF.set(
            0f,
            0f,
            widthPicture,
            heightView.toFloat()
        )
        canvas.drawRect(rectF, background)

        var numberLineWave = 0

        if (interpolatedArray.isNotEmpty()) {
            while (numberLineWave < interpolatedArray.size) {
                if (interpolatedArray[numberLineWave] < MIN_WAVEFORM_LEVEL) {
                    interpolatedArray[numberLineWave] = MIN_WAVEFORM_LEVEL
                }
                rectF.set(
                    waveLineDrawingCoordinate,
                    ((heightView / 2f - (heightView / 2 * interpolatedArray[numberLineWave] * 0.8f) + centerShift)),
                    waveLineDrawingCoordinate + singleWaveWidth,
                    ((heightView / 2f + (heightView / 2 * interpolatedArray[numberLineWave] * 0.8f) + centerShift))
                )
                canvas.drawRoundRect(rectF, singleWaveWidth / 2f, singleWaveWidth / 2f, paint)
                numberLineWave++
                waveLineDrawingCoordinate += (singleWaveWidth + spaceForDrawing)
            }
        }

        picture.endRecording()
        return picture
    }

    private fun pxToMm(px: Float): Float {
        val dm = context.resources.displayMetrics
        return px / TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_MM, 1f, dm)
    }

    private fun interpolateData(data: FloatArray, numOfLevelsRequired: Int): FloatArray {
        val dataSize = data.size
        val interpolationFactorWhole: Int
        val interpolationFactorFloating: Float
        val interpolationFactor: Float
        if (numOfLevelsRequired != dataSize) {

            interpolationFactor = dataSize / numOfLevelsRequired.toFloat()
            interpolationFactorWhole = interpolationFactor.toInt()
            interpolationFactorFloating = interpolationFactor - interpolationFactorWhole
        } else {
            return data
        }

        if (interpolationFactor > 1f) {
            //[0; numOfLevelsRequiredShortSong)
            fun getData(i: Int): Float {

                val initialIndex = i * interpolationFactor
                val initialIndexWhole = initialIndex.toInt()

                var count = interpolationFactorWhole
                if (initialIndex - initialIndexWhole + interpolationFactorFloating > 1f) {
                    count++
                }
                if (count + initialIndexWhole >= dataSize) {
                    count = dataSize - initialIndexWhole - 1
                }

                var sum = 0f
                for (dataIndex in initialIndexWhole until (count + initialIndexWhole)) {
                    sum += data[dataIndex]
                }
                return sum / count
            }

            return FloatArray(numOfLevelsRequired) { getData(it) }
        } else {

            //1. making new array where we move elements
            //2. interpolating empty values

            //кусочно линейная интерполяция
            val newArray = FloatArray(numOfLevelsRequired) { -1f }

            data.forEachIndexed { index, fl ->

                val newIndex = index / interpolationFactor
                newArray[newIndex.toInt()] = fl
            }

            var prevValue = 0f
            var prevIndex = 0

            fun onArrayIteration(index: Int, fl: Float) {
                if (prevIndex + 1 < index) {

                    val needToFillCount = index - prevIndex - 1
                    val difference = fl - prevValue
                    val step = difference / (needToFillCount + 1)
                    var accamulated = prevValue

                    for (i in prevIndex + 1 until index) {
                        accamulated += step
                        newArray[i] = accamulated
                    }
                }
            }
            newArray.forEachIndexed { index, fl ->
                if (fl != -1f) {

                    onArrayIteration(index, fl)
                    prevIndex = index
                    prevValue = fl
                }
            }

            //if last element is -1
            onArrayIteration(newArray.size, 0f)
            return newArray
        }
    }

    private fun notifyStartPositionListener(fromUser: Boolean) {
        startPositionListener?.onStartPositionChanged(leftLinePositionToMillis, fromUser)
    }

    interface StartPositionListener {
        fun onStartPositionChanged(position: Long, fromUser: Boolean)
    }

    interface WaveScrollListener {
        fun onWaveScrollChanged(isScroll: Boolean)
    }

    fun playPositionTo(positionMillis: Long, invalidate: Boolean = true) {
        playPosition = positionMillis
        rightBorderPurple =
            (pictureBlackWave.width * positionMillis / durationSong.toFloat()) + centerLeftLine
        if (invalidate)
            invalidate()

        logger.debug {
            "playPositionTo $positionMillis, rightBorderPurple ${rightBorderPurple}"
        }
    }

    private fun getPositionLeftRightLineToMillis(resetPlay: Boolean) {

        leftLinePositionToMillis =
            ((shiftWaveScrolling.toDouble()) / (pictureBlackWave.width.toDouble()) * durationSong).toLong()
        rightLinePositionToMillis =
            (((centerRightLine - centerLeftLine + shiftWaveScrolling.toDouble()) / pictureBlackWave.width) * durationSong).toLong()

        /*logger.debug {
            "getPositionLeftRightLine left ${leftLinePositionToMillis}," +
                    " right ${rightLinePositionToMillis}, centerLeftLine ${centerLeftLine}, shiftWaveScrolling ${shiftWaveScrolling}"
        }*/

        if (resetPlay) {
            if (DONT_RESET_POSITION_ON_SCROLL) {


            } else {
                playPositionTo(leftLinePositionToMillis, false)
            }
        }
    }

    fun setInitialPosition(trimStartTime: Long) {
        playPosition = trimStartTime
    }

    companion object {
        val INITIAL_DATA = FloatArray(20) { MIN_WAVEFORM_LEVEL }
        val CLIP_PROGRESS_LINE = !BuildConfig.DEBUG
        const val DONT_RESET_POSITION_ON_SCROLL = true
    }
}
