package app.inspiry.edit

import app.inspiry.core.data.PointF
import app.inspiry.core.data.Rect
import app.inspiry.core.data.TouchAction
import app.inspiry.core.data.Vector
import app.inspiry.core.database.ExternalResourceDao
import app.inspiry.core.log.GlobalLogger
import app.inspiry.core.util.InspMathUtil
import app.inspiry.core.util.createDefaultScope
import app.inspiry.core.util.toRadian
import app.inspiry.views.InspView
import app.inspiry.views.group.InspGroupView
import app.inspiry.views.media.InspMediaView
import app.inspiry.views.template.InspTemplateView
import app.inspiry.views.template.TemplateTransform
import app.inspiry.views.text.InspTextView
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.jvm.JvmOverloads
import kotlin.math.*


class EditWrapperHelper
@JvmOverloads constructor(
    val scope: CoroutineScope = createDefaultScope(),
    val templateView: InspTemplateView,
    dpFactor: Float,
    val externalResourceDao: ExternalResourceDao
) {

    var selectedView: InspView<*>? = null

    private val _isWrapperVisible: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val isWrapperVisible: StateFlow<Boolean> = _isWrapperVisible


    private val _editBounds = MutableStateFlow(Rect())
    val editBounds: StateFlow<Rect> = _editBounds

    private val _boundsRotation = MutableStateFlow(0f)
    val boundsRotation: StateFlow<Float> = _boundsRotation

    private val _availableActions = MutableStateFlow(emptyList<TouchAction>())
    val availableActions: StateFlow<List<TouchAction>> = _availableActions

    private val _newViewSize = MutableStateFlow(Pair(0, 0))
    val newViewSize: StateFlow<Pair<Int, Int>> = _newViewSize

    private var selectedViewChangedAction: ((new: InspView<*>?, old: InspView<*>?) -> Unit)? = null

    private val minPossibleSize = dpFactor * MIN_POSSIBLE_SIZE

    var borderPadding: Int = 0

    var borderAnimationEnabled = true

    init {
        scope.launch {
            templateView.selectedViewState.collect {
                val oldSelected = selectedView
                onSelectedChangeMaySkip(it)

                if (oldSelected?.isInSlides() == true && it?.isInSlides() == true) {
                    val oldParent = (oldSelected as? InspMediaView)?.getSlidesParent()
                    val newParent = (it as? InspMediaView)?.getSlidesParent()
                    borderAnimationEnabled = oldParent?.group?.media?.id != newParent?.group?.media?.id
                }
                selectedViewChangedAction?.invoke(it, oldSelected)
            }
        }
        scope.launch {
            templateView.templateTransform.collect {
                updateBounds()
            }
        }

        scope.launch {
            templateView.isInitialized.collect {
                updateBounds()
            }
        }
        templateView.onSelectedViewMovedListener = ::updateBounds
    }

    fun onSelectedViewChanged(action: (new: InspView<*>?, old: InspView<*>?) -> Unit) {
        selectedViewChangedAction = action
    }

    fun animationWasIgnored() {
        borderAnimationEnabled = true
    }

    private var shapeObserver: Job? = null
    private fun onSelectedChangeMaySkip(view: InspView<*>?) {
        if (selectedView == view)
            return
        _availableActions.value = view?.media?.touchActions ?: emptyList()
        val mediaView =  view?.asInspMediaView()
        val isEmptyMedia = mediaView != null && !mediaView.media.hasBackground() && !mediaView.media.hasUserSource()
        _isWrapperVisible.value = view != null && !isEmptyMedia
        shapeObserver?.cancel()

        selectedView = view
        if (view is InspMediaView) {
            shapeObserver = scope.launch {
                view.shapeState.collect {
                    updateBounds()
                }
            }
        }
    }

    fun updateBounds() {
        val view = selectedView ?: return

        if (view is InspMediaView && !view.isSocialIcon()) {
            borderPadding = if (view.media.isMovable == true) 8 else 2
        } else {
            borderPadding = 12
        }

        val newBounds =
            view.findParentIfDependsOnItNotLinked()?.getViewBounds() ?: view.getViewBounds()

        val templateTransform = templateView.templateTransform.value
        newBounds.transformWithTemplate(templateTransform)
        val independentGroup = selectedView?.parentInsp
        val dependentGroup = selectedView?.findParentIfDependsOnIt()

        if (dependentGroup == null && independentGroup != null && independentGroup is InspGroupView) {

            val parentBounds = independentGroup.getViewBounds(recursive = true)
            parentBounds.transformWithTemplate(templateTransform)
            val parentCenter = Pair(parentBounds.centerX() + 0f, parentBounds.centerY() + 0f)
            InspMathUtil.rotateRectAround(
                rect = newBounds,
                point = parentCenter,
                angle = independentGroup.getAbsoluteRotation()
            )

        }
        _boundsRotation.value = view.getAbsoluteRotation()
        _editBounds.value = newBounds
    }

    fun removeAction() {
        selectedView?.let {
            it.removeFromActionButton(externalResourceDao)
            onSelectedChangeMaySkip(null)
        }
    }

    fun copyAction() {
        scope.launch {
            val view = selectedView ?: return@launch
            withContext(Dispatchers.Default) {
                externalResourceDao.onTemplateOrMediaCopy(view.media.getFilesToClean())
            }
            templateView.copyInspView(view)
        }
    }

    private var rotationStartAngle = 0F
    private lateinit var rotationCenterPoint: PointF
    private lateinit var rotationFirstVector: Vector

    /**
     * determining the center of rotation
     * @param touchPoint - touch point in the Template
     */
    fun startRotateAction(touchPoint: PointF) {
        val centerX = editBounds.value.centerX()
        val centerY = editBounds.value.centerY()
        rotationStartAngle = boundsRotation.value
        rotationCenterPoint = PointF(centerX + 0f, centerY + 0f)
        rotationFirstVector = InspMathUtil.createVector(rotationCenterPoint, touchPoint)
    }

    /**
     * InspView rotation
     * @param touchPoint - touch point in the Template
     */
    fun rotateAction(touchPoint: PointF) {
        val rotationSecondVector = InspMathUtil.createVector(rotationCenterPoint, touchPoint)
        val deltaRotationAngle =
            InspMathUtil.calculateAngleDegree(rotationFirstVector, rotationSecondVector)
        rotateSelectedView(rotationStartAngle, deltaRotationAngle)
    }

    fun rotateSelectedView(startAngle: Float, deltaAngle: Float) {
        val rotationAngle = (startAngle + deltaAngle)
            .transformRotationAngleDegree()
            .clipAngleDegree()

        selectedView?.setupRotation(rotationAngle)
        updateBounds()
    }

    private var lastTouchSizeX = 0f
    private var lastTouchSizeY = 0f

    fun startScaleAction(touchPoint: PointF) {
        lastTouchSizeX = touchPoint.x
        lastTouchSizeY = touchPoint.y
    }

    fun scaleAction(touchPoint: PointF) {
        val view = selectedView ?: return
        val media = view.media
        var newHeight: Int? = null
        var newWidth: Int? = null
        view.maySwitchToAutosizeMode()


        val aspect = view.initialWidth / view.initialHeight.toFloat()

        val alpha = _boundsRotation.value.toRadian()

        var deltaX = touchPoint.x - lastTouchSizeX
        var deltaY = touchPoint.y - lastTouchSizeY

        val newX = deltaX * cos(alpha) + deltaY * sin(alpha)
        val newY = -deltaX * sin(alpha) + deltaY * cos(alpha)

        deltaX = newX
        deltaY = newY

        if (media.keepAspect && abs(deltaX) > abs(deltaY)) deltaY = deltaX / aspect
        if (media.keepAspect && abs(deltaX) < abs(deltaY)) deltaX = deltaY * aspect

        if (media.canMoveX()) {
            lastTouchSizeX = touchPoint.x

            val scaleDelta = deltaX / view.viewWidth * view.userScaleX
            var scaleX = view.userScaleX + scaleDelta
            var minPossibleWidth = minPossibleSize
            if (media.keepAspect && aspect > 1) {
                minPossibleWidth = minPossibleSize * aspect
            }
            if (scaleX * view.initialWidth < minPossibleWidth) scaleX =
                minPossibleWidth / view.initialWidth

            view.userScaleX = scaleX
            newWidth = view.applyScaleX()
        }


        if (media.canMoveY()) {
            lastTouchSizeY = touchPoint.y
            val scaleDelta =
                deltaY / view.viewHeight * view.userScaleY
            var scaleY = view.userScaleY + scaleDelta

            var minPossibleHeight = minPossibleSize
            if (media.keepAspect && aspect < 1) {
                minPossibleHeight = minPossibleSize / aspect
            }
            if (scaleY * view.initialHeight < minPossibleHeight) scaleY =
                minPossibleHeight / view.initialHeight
            view.userScaleY = scaleY

            newHeight = view.applyScaleY()

        }
        _newViewSize.value = Pair(newWidth ?: view.viewWidth, newHeight ?: view.viewHeight)
        view.scalePadding()
        view.scaleMargin()

        if (view is InspTextView) {
            view.doForDuplicates {
                it.maySwitchToAutosizeMode()
                it.userScaleY = view.userScaleY
                it.userScaleX = view.userScaleX
                it.applyScaleY()
                it.applyScaleX()
                it.scalePadding()
                it.scaleMargin()
            }
        }

        updateBounds()
    }

    fun finishScaleAction() {
        val view = selectedView ?: return
        if (view.isSocialIcon() && view.parentInsp is InspGroupView) {
            val socialText =
                (view.parentInsp as? InspGroupView)?.children?.find { it is InspTextView }
            socialText?.let {
                view.syncScaleWithMedia(it)
            }
        }
        templateView.isChanged.value = true

    }


    companion object {
        private val ROTATION_ANCHOR_ANGLES_DEGREE = floatArrayOf(0F, 90F, 180F)
        private const val ROTATION_ANCHOR_SENSITIVITY_DEGREE = 3F
        private const val ROTATION_SPEED_FACTOR = 1F
        private const val MIN_POSSIBLE_SIZE = 20 //dp
    }

    /**
     * Setting up rotation speed, binding to the nearest values
     */
    private fun Float.transformRotationAngleDegree(): Float {

        fun Float.isNearAnchor(anchorAngle: Float) =
            abs(this - anchorAngle) <= ROTATION_ANCHOR_SENSITIVITY_DEGREE

        var rotationAngle = (this * ROTATION_SPEED_FACTOR).clipAngleDegree()
        for (anchorAngle in ROTATION_ANCHOR_ANGLES_DEGREE) {
            if (rotationAngle.isNearAnchor(anchorAngle) || rotationAngle.isNearAnchor(-anchorAngle)) {
                rotationAngle = anchorAngle * sign(rotationAngle)
                break
            }
        }
        return rotationAngle
    }

    private fun Float.clipAngleDegree(): Float {
        var angle = this % 360
        when {
            angle > 180 -> angle -= 360
            angle < -180 -> angle += 360
        }
        return angle
    }

    private fun Rect.transformWithTemplate(templateTransform: TemplateTransform) {
        val scale = templateTransform.scale
        val cwidth = templateTransform.containerSize.width
        val tempWidth = width()
        val tempHeight = height()
        left = (left * scale + (cwidth - cwidth * scale) / 2f).roundToInt()
        top =
            (top * scale + templateTransform.verticalOffset + templateTransform.staticOffset).roundToInt()
        right = (left + tempWidth * scale).roundToInt()
        bottom = (top + tempHeight * scale).roundToInt()
    }
}
