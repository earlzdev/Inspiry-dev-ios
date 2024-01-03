package app.inspiry.views.timeline

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.view.*
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.*
import androidx.core.graphics.withTranslation
import androidx.core.view.updateLayoutParams
import app.inspiry.BuildConfig
import app.inspiry.R
import app.inspiry.core.data.FRAME_IN_MILLIS
import app.inspiry.helpers.K
import app.inspiry.core.media.Media
import app.inspiry.utils.dpToPixels
import app.inspiry.utils.dpToPxInt
import app.inspiry.utils.scrollToDescendantCompat
import app.inspiry.views.InspView
import app.inspiry.views.group.InspGroupView
import app.inspiry.views.template.InspTemplateView
import app.inspiry.views.template.swap
import app.inspiry.views.text.InspTextView
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

@SuppressLint("ViewConstructor")
class TimelineView(context: Context, val templateView: InspTemplateView) : LinearLayout(context) {

    private var secondsInFullWidth = 0f
    val duration: Double
        get() = templateView.getDuration() * FRAME_IN_MILLIS

    var currentTime = 0.0
    val insideViews = mutableListOf<TimelineInsideView>()

    private val timeIndicatorUnpressedColor = 0xff625FFF.toInt()
    private val timeIndicatorPressedColor = timeIndicatorUnpressedColor

    private val activeAreaPaint = Paint().also {
        it.color = 0xff333333.toInt()
        it.style = Paint.Style.FILL
    }

    private val viewsMargin = 2f.dpToPixels().toInt()

    private val timeIndicatorPaint = Paint().also {
        it.color = timeIndicatorUnpressedColor
        it.strokeCap = Paint.Cap.ROUND
        it.strokeWidth = 2.dpToPixels()
        it.style = Paint.Style.STROKE
    }

    private val edgeEffectLeft = EdgeEffect(context)
    private val edgeEffectRight = EdgeEffect(context)

    private var mScroller: OverScroller
    private var mVelocityTracker: VelocityTracker? = null
    private val mTouchSlop: Int
    private val mMinimumVelocity: Int
    private val mMaximumVelocity: Int
    private val mOverflingDistance: Int
    private var mOverscrollDistance: Int
    private var scrollViewTexts: ScrollView? = null
    private var defaultSecondsInFullWidth = 0f
    private val timesView: TimelineTimesView

    //TODO: make it later. Scale detector doesnt work properly.
    // To make it work good we need to implement it ourselves.
    private val USE_SCALE_GESTURE = false
    private val textsContainer: LinearLayout

    var selectedView: TimelineInsideView? = null
        private set

    fun setSelectedView(value: TimelineInsideView?) {
        if (selectedView == value) return

        val old = selectedView
        selectedView = value

        old?.view?.isSelected = false
        value?.view?.isSelected = true

        /*K.i("timelineView") {
            "setSelectedView $old, new $value, notifyTemplate $notifyTemplate"
        }*/

        templateView.changeSelectedView(
            if (value is TimelineTextView) value.mediaText.view else null
        )
    }

    fun addInsideText(it: InspTextView) {
        val insideView =
            TimelineTextView(context, it.media)

        textsContainer.addView(
            insideView,
            MarginLayoutParams(MATCH_PARENT, INNER_VIEW_HEIGHT_DP.dpToPxInt()).also {
                it.bottomMargin = viewsMargin
                it.topMargin = viewsMargin
            })
        insideViews.add(insideView)
    }

    val canChangeViewsOrder: Boolean

    init {
        orientation = VERTICAL
        setBackgroundColor(0xff424242.toInt())
        isFocusable = true
        mScroller = OverScroller(context)
        clipChildren = false

        val configuration = ViewConfiguration.get(context)
        mTouchSlop = configuration.scaledTouchSlop
        mMinimumVelocity = configuration.scaledMinimumFlingVelocity * 3
        mMaximumVelocity = configuration.scaledMaximumFlingVelocity / 2
        mOverflingDistance = configuration.scaledOverflingDistance
        mOverscrollDistance = configuration.scaledOverscrollDistance

        setOnClickListener {
            setSelectedView(null)
        }

        addView(Space(context),
            MATCH_PARENT,
            context.resources.getDimensionPixelSize(R.dimen.timeline_control_panel_height))

        timesView = TimelineTimesView(getContext())
        addView(timesView,
            MATCH_PARENT,
            HEIGHT_TIMES_DP.dpToPxInt())

        insideViews.add(timesView)

        val textViews = templateView.allViews.filterIsInstance(InspTextView::class.java).filter { !it.isDuplicate() }
        textsContainer = LinearLayout(context)

        textsContainer.orientation = VERTICAL
        textsContainer.clipChildren = false

        val scroll = ScrollView(context)
        scroll.isVerticalScrollBarEnabled = false
        scroll.addView(textsContainer, MATCH_PARENT, WRAP_CONTENT)
        scroll.clipChildren = false

        textsContainer.setOnClickListener {
            setSelectedView(null)
        }

        scrollViewTexts = scroll

        addView(scroll, MATCH_PARENT, getTextsScrollViewHeight(textViews.size))

        templateView.allViews.forEach {
            if (it is InspTextView && !it.isDuplicate()) {
                addInsideText(it)
            }
        }

        val timelineTemplateView =
            TimelineTemplateView(getContext(), templateView)
        timelineTemplateView.minDuration =
            templateView.getTimeMs(templateView.getMinPossibleDuration())

        addView(
            timelineTemplateView,
            MarginLayoutParams(MATCH_PARENT, INNER_VIEW_HEIGHT_DP.dpToPxInt()).also {
                it.bottomMargin = viewsMargin * 2
                it.topMargin = viewsMargin
            })
        insideViews.add(timelineTemplateView)

        guessDefaultScale()

        scrollViewTexts?.addOnLayoutChangeListener { v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
            if (scrollViewTexts != null) {
                scrollViewTexts!!.translationX = offsetForContent
                scrollViewTexts!!.clipBounds =
                    Rect(
                        -ARROW_WIDTH,
                        0,
                        scrollViewTexts!!.width + ARROW_WIDTH,
                        scrollViewTexts!!.height
                    )
            }
        }
        templateView.removeViewListener = { inspView ->

            val selectedText = inspView.media.selectTextView()
            if (selectedText != null) {
                val toRemove =
                    insideViews.find { (it as? TimelineTextView)?.mediaText === selectedText }

                if (toRemove != null) {
                    insideViews.remove(toRemove)
                    textsContainer.removeView(toRemove.view)
                    updateTextsScrollViewHeight()
                }
            }
        }

        canChangeViewsOrder = textViews.all {
            val dependsOnParent = it.parentDependsOnThisView()
            (dependsOnParent && (it.parentInsp is InspGroupView && (it.parentInsp as InspGroupView).parentInsp is InspTemplateView))
                    || (!dependsOnParent && it.parentInsp is InspTemplateView)
        }

        post {

            val selected = templateView.selectedView
            if (selected is InspTextView) {
                setSelectedView(insideViews.find { (it as? TimelineTextView)?.mediaText === selected.media.selectTextView()!! })
            } else
                setSelectedView(timelineTemplateView)
        }
    }

    fun onSelectedViewChange(inspView: InspView<*>?) {
        val selectedView = if (inspView == null) null
        else {
            insideViews.find {
                it is TimelineTextView && it.mediaText === inspView.media
            }
        }
        if (selectedView != null)
            mayScrollToView(selectedView)
        setSelectedView(selectedView)
    }

    fun updateTextsScrollViewHeight() {
        scrollViewTexts?.updateLayoutParams<LayoutParams> {
            height =
                getTextsScrollViewHeight(insideViews.filterIsInstance(TimelineTextView::class.java).size)
        }
        scrollViewTexts?.requestLayout()
    }

    private fun getTextsScrollViewHeight(textViewsCount: Int) =
        (INNER_VIEW_HEIGHT_DP.dpToPxInt() + viewsMargin * 2) *
                (if (textViewsCount > MAX_TEXT_CHILDREN_SCROLL) MAX_TEXT_CHILDREN_SCROLL else textViewsCount)

    private fun mayScrollToView(selectedView: TimelineInsideView) {
        if (scrollViewTexts != null) {
            scrollViewTexts?.scrollToDescendantCompat(selectedView.view, true)
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        timesView.translationX = offsetForContent
    }

    private fun guessDefaultScale() {
        defaultSecondsInFullWidth =
            (templateView.getDuration() * FRAME_IN_MILLIS / 1000.0).toFloat() * if (BuildConfig.DEBUG) 1.3f else 1.3f
        setSecondsInWidth(defaultSecondsInFullWidth, false)
    }

    private fun setSecondsInWidth(seconds: Float, update: Boolean) {

        secondsInFullWidth =
            max(min(seconds, MAX_SECONDS_IN_FULL_WITH), MIN_SECONDS_IN_FULL_WITH)

        K.i("onScale") {
            "secondsInFullWidth ${seconds} ${secondsInFullWidth}"
        }

        if (update)
            updatePosition(templateView.getCurrentTime(), false)
    }


    val mScaleDetector = ScaleGestureDetector(context,
        object : ScaleGestureDetector.SimpleOnScaleGestureListener() {

            override fun onScale(detector: ScaleGestureDetector): Boolean {
                K.i("onScale") {
                    "scaleFactor ${detector.scaleFactor}"
                }
                setSecondsInWidth(defaultSecondsInFullWidth * detector.scaleFactor, true)
                return true
            }

        }).apply {
        isQuickScaleEnabled = false
    }

    private fun initOrResetVelocityTracker() {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain()
        } else {
            mVelocityTracker?.clear()
        }
    }

    private fun initVelocityTrackerIfNotExists() {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain()
        }
    }

    private fun recycleVelocityTracker() {
        if (mVelocityTracker != null) {
            mVelocityTracker?.recycle()
            mVelocityTracker = null
        }
    }

    override fun computeScroll() {

        val res = mScroller.computeScrollOffset()

        if (res) {

            val oldX: Int = scrollX
            val x = mScroller.currX
            val y = mScroller.currY

            if (oldX != x) {
                val range: Int = calcWidthOfViews().toInt()

                seekPositionOn((x - oldX).toFloat(), y.toFloat(), false, true)

                val overscrollMode = overScrollMode
                val canOverscroll =
                    overscrollMode == View.OVER_SCROLL_ALWAYS ||
                            overscrollMode == View.OVER_SCROLL_IF_CONTENT_SCROLLS && range > 0
                if (canOverscroll) {
                    if (x < 0 && oldX >= 0) {
                        edgeEffectLeft.onAbsorb(mScroller.currVelocity.toInt())
                    } else if (range in oldX until x) {
                        edgeEffectRight.onAbsorb(mScroller.currVelocity.toInt())
                    }
                }

            }
            postInvalidateOnAnimation()
        }
    }

    override fun onOverScrolled(
        scrollX: Int, scrollY: Int,
        clampedX: Boolean, clampedY: Boolean
    ) {
        // Treat animating scrolls differently; see #computeScroll() for why.
        if (!mScroller.isFinished) {

            this.scrollX = scrollX
            if (clampedX) {
                mScroller.springBack(scrollX, scrollY, 0, calcWidthOfViews().toInt(), 0, 0)
            }
        } else {
            super.scrollTo(scrollX, scrollY)
        }
    }

    private fun fling(velocityX: Int) {
        if (childCount > 0) {
            val w: Int = width - paddingLeft - paddingRight
            mScroller.forceFinished(true)
            mScroller.fling(
                scrollX, scrollY, velocityX, 0, 0,
                calcWidthOfViews().toInt(), 0, 0, w / 2, 0
            )

            postInvalidateOnAnimation()
        }
    }

    private fun calcViewOffset(currentTime: Double, fromUser: Boolean) {
        val percentPassed = currentTime / duration
        val containsWidths = calcWidthOfViews()

        if (!fromUser)
            scrollX = (containsWidths * percentPassed).toInt()

        scrollViewTexts?.updateLayoutParams {
            width = containsWidths.toInt()
        }
    }

    fun calcWidthOfViews() = duration * width / (secondsInFullWidth * 1000.0)


    fun updatePosition(currentTime: Double, fromUser: Boolean) {
        this.currentTime = currentTime
        calcViewOffset(currentTime, fromUser)

        insideViews.forEach {
            it.setCurrentTime(currentTime)
            it.view.invalidate()
        }
    }

    val offsetForContent: Float
        get() = width / 2f

    val offsetForContentText: Float
        get() = if (scrollViewTexts == null) width / 2f else 0f


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        canvas.drawRect(
            offsetForContent, 0f, offsetForContent + calcWidthOfViews().toFloat(),
            height.toFloat(), activeAreaPaint
        )
    }

    private var lastMoveX: Float = 0f
    private var lastMoveY: Float = 0f
    private val THRESHOLD_MOVE_X = 4.dpToPixels()
    private val MAX_MOVE_Y = 3.dpToPixels()
    private var movedX = 0f
    private var movedY = 0f
    private var inMoveMode = false

    //necessary to prevent losing parts of numbers
    private var startFrame = 0
    private var totalFramesSeek: Double = 0.0

    /**
     * ID of the active pointer. This is used to retain consistency during
     * drags/flings if multiple pointers are used.
     */

    private val INVALID_POINTER: Int = -1
    private var mActivePointerId: Int = INVALID_POINTER
    var handledScaleOnThisTouch = false


    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (USE_SCALE_GESTURE) {
            val originalY: Float = event.y
            val originalX: Float = event.x
            event.setLocation(event.rawX, event.rawY)
            mScaleDetector.onTouchEvent(event)
            event.setLocation(originalX, originalY)

            if (mScaleDetector.isInProgress) {
                handledScaleOnThisTouch = true
            }
            if (handledScaleOnThisTouch) {
                if (event.action == MotionEvent.ACTION_CANCEL || event.action == MotionEvent.ACTION_UP) {
                    handledScaleOnThisTouch = false
                }
                return true
            }
        }

        return super.dispatchTouchEvent(event)
    }

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {

        if (handledScaleOnThisTouch) return true

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                lastMoveX = event.x
                lastMoveY = event.y
                movedX = 0f
                movedY = 0f

                mActivePointerId = event.getPointerId(0)


                initOrResetVelocityTracker()
                mVelocityTracker!!.addMovement(event)
            }
            MotionEvent.ACTION_MOVE -> {
                val deltaX = lastMoveX - event.x
                movedX += deltaX
                lastMoveX = event.x

                movedY += (lastMoveY - event.y)
                lastMoveY = event.y

                if (abs(movedY) >= MAX_MOVE_Y)
                    return false

                if (!inMoveMode && abs(movedX) > THRESHOLD_MOVE_X) {
                    inMoveMode = true

                    initVelocityTrackerIfNotExists()
                    mVelocityTracker!!.addMovement(event)

                    enterMoveMode()
                }

                if (inMoveMode)
                    return true
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                mActivePointerId = INVALID_POINTER
                recycleVelocityTracker()
                handledScaleOnThisTouch = false
            }
            MotionEvent.ACTION_POINTER_DOWN -> {
                val index: Int = event.actionIndex
                lastMoveX = event.getX(index)
                lastMoveY = event.getY(index)
                mActivePointerId = event.getPointerId(index)
            }
            MotionEvent.ACTION_POINTER_UP -> {
                onSecondaryPointerUp(event)
            }
        }
        return false
    }


    private fun onSecondaryPointerUp(ev: MotionEvent) {
        val pointerIndex = ev.action and MotionEvent.ACTION_POINTER_INDEX_MASK shr
                MotionEvent.ACTION_POINTER_INDEX_SHIFT
        val pointerId = ev.getPointerId(pointerIndex)
        if (pointerId == mActivePointerId) {
            // This was our active pointer going up. Choose a new
            // active pointer and adjust accordingly.

            val newPointerIndex = if (pointerIndex == 0) 1 else 0
            lastMoveX = ev.getX(newPointerIndex)
            lastMoveY = ev.getY(newPointerIndex)
            mActivePointerId = ev.getPointerId(newPointerIndex)
            if (mVelocityTracker != null) {
                mVelocityTracker!!.clear()
            }
        }
    }


    /**
     * usual touch delegate is useless here.
     * This is necessary because scrollView clips touch area of TimelineTextView
     */
    var redirectToDelegate = false
    private fun checkDelegateView(event: MotionEvent): Boolean {

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                if (scrollViewTexts != null && selectedView is TimelineTextView) {
                    val mBounds = Rect()
                    selectedView!!.view.getHitRect(mBounds)
                    mBounds.offset(
                        offsetForContent.toInt() - scrollX,
                        -scrollViewTexts!!.scrollY + timesView.height + getChildAt(0).height
                    )
                    redirectToDelegate = mBounds.contains(event.x.toInt(), event.y.toInt())

                } else {
                    redirectToDelegate = false
                }
            }
        }

        if (selectedView == null) redirectToDelegate = false

        if (redirectToDelegate) {

            //the same offset as for rect with the opposite sign
            event.offsetLocation(
                scrollX - offsetForContent + ARROW_WIDTH,
                (scrollViewTexts!!.scrollY - timesView.height - getChildAt(0).height).toFloat()
            )
            selectedView!!.view.dispatchTouchEvent(event)
        }

        val handled = redirectToDelegate
        when (event.action) {
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                redirectToDelegate = false
            }
        }

        return handled
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {

        if (checkDelegateView(event)) return true

        if (handledScaleOnThisTouch) return true

        initVelocityTrackerIfNotExists()
        mVelocityTracker!!.addMovement(event)

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {

                /*
                 * If being flinged and user touches, stop the fling. isFinished
                 * will be false if being flinged.
                 */
                if (!mScroller.isFinished) {
                    mScroller.abortAnimation()
                }
                mActivePointerId = event.getPointerId(0)
            }
            MotionEvent.ACTION_MOVE -> {
                val activePointerIndex: Int = event.findPointerIndex(mActivePointerId)
                if (activePointerIndex == -1) {
                    return false
                }

                val x = event.getX(activePointerIndex)

                val delta = lastMoveX - x
                movedX += delta
                lastMoveX = x

                if (!inMoveMode) {
                    enterMoveMode()
                }
                if (inMoveMode) {
                    seekPositionOn(delta, event.y, true, true)
                }
            }
            MotionEvent.ACTION_UP -> {
                if (inMoveMode) {

                    val velocityTracker = mVelocityTracker!!
                    velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity.toFloat())
                    val initialVelocity = velocityTracker.getXVelocity(mActivePointerId).toInt()

                    if (abs(initialVelocity) > mMinimumVelocity) {
                        fling(-initialVelocity)
                    } else {
                        if (mScroller.springBack(
                                scrollX, scrollY, 0,
                                calcWidthOfViews().toInt(), 0, 0
                            )
                        ) {
                            postInvalidateOnAnimation()
                        }
                    }
                    endMoveMode()
                } else if (!handledScaleOnThisTouch) {
                    performClick()
                }

                mActivePointerId = INVALID_POINTER
                recycleVelocityTracker()
                handledScaleOnThisTouch = false
            }
            MotionEvent.ACTION_CANCEL -> {
                if (inMoveMode) {
                    endMoveMode()
                }

                mActivePointerId = INVALID_POINTER
                handledScaleOnThisTouch = false
                recycleVelocityTracker()
            }
            MotionEvent.ACTION_POINTER_UP -> {
                onSecondaryPointerUp(event)
            }
        }
        return true
    }


    fun seekPositionOn(
        deltaX: Float,
        eventY: Float,
        fromTouchOverScroll: Boolean,
        fromTouchListener: Boolean
    ) {

        // Calling overScrollBy will call onOverScrolled, which
        // calls onScrollChanged if applicable.

        val widthOfViews = calcWidthOfViews()
        val percentSeek = deltaX / widthOfViews
        val framesSeek = templateView.maxFrames * percentSeek


        if (fromTouchListener) {
            if (overScrollBy(
                    deltaX.toInt(), 0, scrollX, 0, widthOfViews.toInt(), 0,
                    mOverscrollDistance, 0, fromTouchOverScroll
                )
            ) {
                // Break our velocity if we hit a scroll barrier.
                mVelocityTracker?.clear()
            }
        }

        totalFramesSeek += framesSeek

        var newFrame = (startFrame + totalFramesSeek).toInt()

        val pullOffset = widthOfViews * (currentTime / duration) + deltaX

        //display over scroll
        if (newFrame > templateView.maxFrames) {
            newFrame = templateView.maxFrames

            if (fromTouchOverScroll) {
                edgeEffectRight.onPull(
                    (pullOffset - widthOfViews).toFloat() / width,
                    1 - (eventY / height)
                )
                if (!edgeEffectLeft.isFinished) {
                    edgeEffectLeft.onRelease()
                }
            }

        } else if (newFrame < 0) {
            newFrame = 0

            if (fromTouchOverScroll) {
                edgeEffectLeft.onPull(
                    pullOffset.toFloat() / width,
                    eventY / height
                )
                if (!edgeEffectRight.isFinished) {
                    edgeEffectRight.onRelease()
                }
            }
        }

        if (fromTouchOverScroll && (!edgeEffectLeft.isFinished || !edgeEffectRight.isFinished)) {
            postInvalidateOnAnimation()
        }

        templateView.setFrameSync(newFrame)
        templateView.setVideoFrameAsync(newFrame, sequential = false)
        templateView.updateFramesListener?.invoke(fromTouchListener)
    }

    private fun enterMoveMode() {
        inMoveMode = true
        resetStartFrame()
    }

    private fun endMoveMode() {
        inMoveMode = false

        edgeEffectLeft.onRelease()
        edgeEffectRight.onRelease()
        invalidate()
    }

    override fun onDrawForeground(canvas: Canvas) {
        super.onDrawForeground(canvas)

        timeIndicatorPaint.setColor(if (inMoveMode || !mScroller.isFinished) timeIndicatorPressedColor else timeIndicatorUnpressedColor)

        canvas.withTranslation(scrollX.toFloat(), scrollY.toFloat()) {
            val lineX = width / 2f
            canvas.drawLine(
                lineX,
                -timeIndicatorPaint.strokeWidth,
                lineX,
                timeIndicatorPaint.strokeWidth,
                timeIndicatorPaint
            )
            canvas.drawLine(
                lineX,
                SECOND_CURRENT_LINE_Y_START,
                lineX,
                height.toFloat(),
                timeIndicatorPaint
            )
        }


        if (!edgeEffectLeft.isFinished) {
            val restoreCount = canvas.save()
            val width: Int
            val height: Int
            val translateX: Float
            val translateY: Float
            if (clipToPadding) {
                width = getWidth() - paddingLeft - paddingRight
                height = getHeight() - paddingTop - paddingBottom
                translateX = paddingLeft.toFloat()
                translateY = paddingTop.toFloat()
            } else {
                width = getWidth()
                height = getHeight()
                translateX = 0f
                translateY = 0f
            }
            canvas.translate(translateX + offsetForContent, translateY + height)
            canvas.rotate(-90f)

            edgeEffectLeft.setSize(height, width)
            if (edgeEffectLeft.draw(canvas)) {
                postInvalidateOnAnimation()
            }
            canvas.restoreToCount(restoreCount)
        }
        if (!edgeEffectRight.isFinished) {

            val restoreCount = canvas.save()
            val width: Int
            val height: Int
            val translateX: Float
            val translateY: Float
            if (clipToPadding) {
                width = getWidth() - paddingLeft - paddingRight
                height = getHeight() - paddingTop - paddingBottom
                translateX = paddingLeft.toFloat()
                translateY = paddingTop.toFloat()
            } else {
                width = getWidth()
                height = getHeight()
                translateX = 0f
                translateY = 0f
            }
            canvas.translate(
                translateX + offsetForContent + calcWidthOfViews().toFloat(),
                translateY
            )
            canvas.rotate(90f)
            edgeEffectRight.setSize(height, width)
            if (edgeEffectRight.draw(canvas)) {
                postInvalidateOnAnimation()
            }
            canvas.restoreToCount(restoreCount)
        }
    }

    fun templateParamChanged() {
        templateView.setFrameSync(templateView.currentFrame)
        templateView.isChanged.value = true
    }

    fun changeOrderViews(timelineTextView: TimelineTextView, up: Boolean) {

        var translationY = viewsMargin * 2 + timelineTextView.height

        val nextView: TimelineTextView
        val index = insideViews.indexOf(timelineTextView)

        if (up) {
            nextView = (insideViews.getOrNull(index - 1) as? TimelineTextView?) ?: return
            translationY = -translationY

        } else {
            nextView = (insideViews.getOrNull(index + 1) as? TimelineTextView?) ?: return
        }

        timelineTextView.translationY += translationY
        nextView.translationY -= translationY

        insideViews.swap(timelineTextView, nextView)
        templateView.changeOrderOfViews(
            timelineTextView.mediaText.getParentGroupMedia(),
            nextView.mediaText.getParentGroupMedia()
        )

        setSelectedView(timelineTextView)

        scrollViewTexts?.scrollToDescendantCompat(timelineTextView, true)
    }

    fun checkTextViewsDurations() {
        insideViews.filterIsInstance(TimelineTextView::class.java).forEach {

            var diffDuration = duration - (it.startTime + it.duration)

            if (diffDuration < 0) {

                val oldDuration = it.duration

                if (it.mediaText.minDuration != Media.MIN_DURATION_AS_TEMPLATE) {
                    val newTextDuration = max(it.minDuration, it.duration + diffDuration)
                    it.setNewTextDuration(newTextDuration)
                    diffDuration += oldDuration - it.duration
                } else {
                    diffDuration += oldDuration - (duration - it.startTime)
                }


                if (diffDuration < 0) {

                    if (abs(diffDuration) > it.startTime)
                        throw IllegalStateException("text duration cannot be bigger than template duration")

                    it.startTime += diffDuration
                }
            }
        }
    }

    fun resetStartFrame() {
        startFrame = templateView.currentFrame
        totalFramesSeek = 0.0
        templateView.stopPlaying()
    }


    companion object {
        const val INNER_VIEW_HEIGHT_DP = 32
        const val MAX_TEXT_CHILDREN_SCROLL = 4
        const val HEIGHT_TIMES_DP = 16

        const val MIN_SECONDS_IN_FULL_WITH = 4f
        const val MAX_SECONDS_IN_FULL_WITH = 30f

        val ARROW_WIDTH = 20.dpToPxInt()
        val SECOND_CURRENT_LINE_Y_START = 15.dpToPixels()
    }
}