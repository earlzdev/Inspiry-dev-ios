package app.inspiry.core.animator.text

open class DrawTextAnimParam : DrawBackgroundAnimParam() {

    var charSequence: CharSequence = ""
    var isRtl: Boolean = false
    var startTextToRender: Int = 0
    var lengthTextToRender: Int = 0
    var strokeWidth: Float = 0f
    var lineBaseline: Int = 0
    var paddingTop: Int = 0
    var lineTop: Int = 0

    var blurRadius = 0f

    override fun nullify() {
        super.nullify()
        this.blurRadius = 0f
    }

    fun initializeAndNullify(
        charSequence: CharSequence,
        isRtl: Boolean,
        startTextToRender: Int,
        lengthTextToRender: Int,
        strokeWidth: Float,
        lineBaseline: Int,
        paddingTop: Int,
        lineTop: Int
    ) {

        nullify()
        this.charSequence = charSequence
        this.isRtl = isRtl
        this.startTextToRender = startTextToRender
        this.lengthTextToRender = lengthTextToRender
        this.strokeWidth = strokeWidth
        this.lineBaseline = lineBaseline
        this.paddingTop = paddingTop
        this.lineTop = lineTop
    }

    open fun copy(
        copied: DrawTextAnimParam,
        shadowLeftOffset: Int,
        shadowTopOffset: Int
    ): DrawTextAnimParam {

        copied.initializeAndNullify(
            charSequence,
            isRtl,
            startTextToRender,
            lengthTextToRender,
            strokeWidth,
            lineBaseline + shadowTopOffset,
            paddingTop + shadowTopOffset,
            lineTop + shadowTopOffset
        )
        copied.left = left + shadowLeftOffset.toFloat()
        copied.top = top + shadowTopOffset.toFloat()
        copied.clipRect = clipRect
        copied.width = width
        copied.height = height
        copied.alpha = alpha
        copied.color = color
        copied.cornersRadius = cornersRadius
        copied.rotate = rotate
        copied.scaleX = scaleX
        copied.scaleY = scaleY
        copied.translateX = translateX
        copied.translateY = translateY
        copied.blurRadius = blurRadius
        copied.textFullWidth = textFullWidth
        copied.textPositionStart = textPositionStart
        copied.circleRadius = circleRadius
        copied.circleLength = circleLength
        copied.circularLengthFactor = circularLengthFactor
        return copied
    }

}