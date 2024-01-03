package app.inspiry.views.text

import android.content.Context
import android.graphics.*
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.graphics.alpha
import app.inspiry.animator.*
import app.inspiry.core.animator.TextAnimationParams
import app.inspiry.core.animator.text.DrawBackgroundAnimParam
import app.inspiry.core.animator.text.DrawTextAnimParam
import app.inspiry.core.media.*
import app.inspiry.core.util.ArgbColorManager
import app.inspiry.core.util.InspMathUtil
import app.inspiry.palette.model.PaletteLinearGradient
import java.util.*
import kotlin.math.max
import kotlin.math.min


class GenericTextLayoutAndroid(context: Context, override val media: MediaText) :
    AppCompatTextView(context), InnerGenericText<Canvas> {

    override val genericTextHelper: GenericTextHelper<Canvas> = GenericTextHelper(
        media = media,
        layout = this
    )
//    override var needsRecompute = true
//    override val parts: MutableMap<TextAnimationParams, PartInfo> = HashMap()
//
//    override val animationParamIn: TextAnimationParams =
//        media.animationParamIn ?: TextAnimationParams()
//    override val animationParamOut: TextAnimationParams =
//        media.animationParamOut ?: TextAnimationParams()

    private var averageLineHeight = 0f
    private val tempBackgroundAnimParam by lazy { DrawBackgroundAnimParamAndroid() }
    private val tempTextAnimParam by lazy { DrawTextAnimParamAndroid() }
    private val tempShadowAnimParam by lazy { DrawTextAnimParamAndroid() }
    private val tempRect by lazy { Rect() }
    private val tempPath by lazy { Path() }
    private val tempPaint by lazy { Paint() }
    private val shadersCache = mutableMapOf<GradientShaderCache, LinearGradient>()
    private val tempShaderCache by lazy { GradientShaderCache(0f, 0f, 0f, 0f, intArrayOf()) }

    override var needsRecompute: Boolean = true
    override var radius: Float = 0f
//    override var durationIn: Int = 0
//    override var durationOut: Int = 0
    fun setStartTimeSource(source: () -> Int) {
        genericTextHelper.getStartTime = source
    }
    fun setDurationSource(source: () -> Int) {
        genericTextHelper.getDuration = source
    }

    override var circularRadius: Float = 0f
    override var circularGravity: TextAlign = media.innerGravity

    override fun getLineForOffset(offset: Int): Int {
        return layout!!.getLineForOffset(offset)
    }

    override var currentFrame = 0
        set(value) {
            field = value
            invalidate()
        }


    private fun averageLineHeight(): Float {
        val layoutHeight = layout.height.toFloat()
        return layoutHeight / layout.lineCount.toFloat()
    }

    private fun useCachedShader(
        shaderLeft: Float,
        shaderTop: Float,
        shaderRight: Float,
        shaderBottom: Float,
        colors: IntArray
    ): LinearGradient {

        tempShaderCache.x1 = shaderLeft
        tempShaderCache.x2 = shaderRight
        tempShaderCache.y1 = shaderTop
        tempShaderCache.y2 = shaderBottom
        tempShaderCache.colors = colors

        var cachedShader = shadersCache[tempShaderCache]

        if (cachedShader == null) {

            cachedShader = LinearGradient(
                shaderLeft,
                shaderTop,
                shaderRight,
                shaderBottom,
                colors,
                null,
                Shader.TileMode.CLAMP
            )

            shadersCache[GradientShaderCache(
                shaderLeft,
                shaderRight,
                shaderTop,
                shaderBottom,
                colors
            )] =
                cachedShader
        }

        return cachedShader
    }

    fun getFontHeight() = paint.fontMetrics.bottom - paint.fontMetrics.top

    override fun drawTextBackgroundsSingle(
        animParam: TextAnimationParams,
        canvas: Canvas,
        time: Double,
        out: Boolean, shadowMode: Boolean
    ) {
        val parts = genericTextHelper.parts[animParam]

        //draw background parts for lines
        if (!genericTextHelper.lackBackground() && parts != null) {

            val partsCount = parts.subParts?.size ?: 0
            val verticalGravityOffset = calcTopOffsetForVerticalGravity()
            parts.subParts?.forEachIndexed { partIndex, it ->

                val (index, _, partStartTime) = it
                val lineNumber = this.layout.getLineForOffset(index)

                val fontHeight = getFontHeight()
                val baseLine = this.layout.getLineBaseline(lineNumber)

                tempBackgroundAnimParam.nullify()
                tempBackgroundAnimParam.cornersRadius = media.radius
                tempBackgroundAnimParam.left =
                    this.layout.getLineLeft(lineNumber) - media.backgroundMarginLeft * fontHeight + paddingLeft
                tempBackgroundAnimParam.top =
                    baseLine + paint.fontMetrics.ascent - media.backgroundMarginTop * fontHeight +
                            paddingTop + verticalGravityOffset

                val right = ((media.backgroundMarginRight) * fontHeight + layout.getLineRight(
                    lineNumber
                )).toInt() + paddingLeft

                tempBackgroundAnimParam.width = right - tempBackgroundAnimParam.left.toInt()

                val bottom =
                    (media.backgroundMarginBottom * fontHeight + baseLine + paint.fontMetrics.descent).toInt() +
                            paddingTop + verticalGravityOffset

                tempBackgroundAnimParam.height = bottom - tempBackgroundAnimParam.top.toInt()
                tempBackgroundAnimParam.color =
                    media.backgroundGradient?.getFirstColor() ?: media.backgroundColor


                if (time >= partStartTime || out) {
                    genericTextHelper.mayApplyBackAnimOut(
                        animParam,
                        tempBackgroundAnimParam,
                        shadowMode,
                        partIndex,
                        partsCount
                    )

                    //apply background animation
                    genericTextHelper.applyAnimation(animParam,
                        time,
                        partStartTime,
                        tempBackgroundAnimParam, partIndex, partsCount,
                        this, shadowMode, out
                    )

                    //draw background canvas
                    canvas.save()
                    canvas.rotate(
                        tempBackgroundAnimParam.rotate,
                        tempBackgroundAnimParam.pivotX * canvas.width.toFloat(),
                        tempBackgroundAnimParam.pivotY * canvas.height.toFloat()
                    )
                    canvas.scale(
                        tempBackgroundAnimParam.scaleX,
                        tempBackgroundAnimParam.scaleY,
                        tempBackgroundAnimParam.pivotX * canvas.width.toFloat(),
                        tempBackgroundAnimParam.pivotY * canvas.height.toFloat()
                    )
                    val rect = tempBackgroundAnimParam.clipRect


                    fun clipRoundPath(left: Float, top: Float, right: Float, bottom: Float) {
                        tempPath.reset()
                        val radius =
                            tempBackgroundAnimParam.cornersRadius * min(
                                tempBackgroundAnimParam.width,
                                tempBackgroundAnimParam.height
                            ) / 2f
                        tempPath.addRoundRect(
                            left,
                            top,
                            right,
                            bottom,
                            radius,
                            radius,
                            Path.Direction.CW
                        )
                        canvas.clipPath(tempPath)
                    }
                    if (rect != null) {

                        tempRect.offsetRect(
                            rect,
                            tempBackgroundAnimParam.left.toInt(),
                            tempBackgroundAnimParam.top.toInt()
                        )


                        if (tempBackgroundAnimParam.cornersRadius != 0f) {
                            clipRoundPath(
                                tempRect.left.toFloat(),
                                tempRect.top.toFloat(),
                                tempRect.right.toFloat(),
                                tempRect.bottom.toFloat()
                            )
                        } else {
                            canvas.clipRect(tempRect)
                        }
                    } else if (tempBackgroundAnimParam.cornersRadius != 0f) {
                        clipRoundPath(
                            tempBackgroundAnimParam.left, tempBackgroundAnimParam.top,
                            tempBackgroundAnimParam.left + tempBackgroundAnimParam.width,
                            tempBackgroundAnimParam.top + tempBackgroundAnimParam.height
                        )
                    }
//                    K.d("text") {
//                        "getGradientShader ${media.backgroundGradient}"
//                    }

                    if (media.backgroundGradient != null) {
                        paint.shader = getGradientShader(
                            tempBackgroundAnimParam,
                            media.backgroundGradient!!, it, lineNumber
                        )

                    } else {
                        paint.shader = tempBackgroundAnimParam.shader
                    }


                    canvas.translate(
                        tempBackgroundAnimParam.translateX,
                        tempBackgroundAnimParam.translateY
                    )

                    if (tempBackgroundAnimParam.color != 0) {

                        val oldAlphaColor = ArgbColorManager.alpha(tempBackgroundAnimParam.color) / 255f
                        paint.color = ArgbColorManager.applyAlphaToColor(tempBackgroundAnimParam.color,
                            tempBackgroundAnimParam.alpha * oldAlphaColor
                        )

                        if (tempBackgroundAnimParam.cornersRadius != 0f && parent != null) {

                            val absRadius =
                                tempBackgroundAnimParam.cornersRadius * min(height, width) / 2.0f

                            canvas.drawRoundRect(
                                tempBackgroundAnimParam.left,
                                tempBackgroundAnimParam.top,
                                tempBackgroundAnimParam.width + tempBackgroundAnimParam.left,
                                tempBackgroundAnimParam.height + tempBackgroundAnimParam.top,
                                absRadius,
                                absRadius,
                                paint
                            )

                        } else {
                            canvas.drawRect(
                                tempBackgroundAnimParam.left,
                                tempBackgroundAnimParam.top,
                                tempBackgroundAnimParam.width + tempBackgroundAnimParam.left,
                                tempBackgroundAnimParam.height + tempBackgroundAnimParam.top,
                                paint
                            )
                        }

                    }
                    canvas.restore()
                }
            }
        }
    }

    override fun onDraw(canvas: Canvas) {

        if (layout == null) {

            onPreDraw()

            if (layout == null) {
                return
            }
        }

        if (needsRecompute) recompute()

        genericTextHelper.onDrawText(canvas, currentFrame = currentFrame)
    }

    override fun draw(animParam: TextAnimationParams, canvas: Canvas, time: Double, out: Boolean) {

        if (circularRadius != 0f) {
            /**
             * Here sometimes the canvas shifts to the right.
             * I have not found reason why this is happening,
             * so for now I just compensated it:
             */
            canvas.translate(canvas.clipBounds.left + 0f, 0f)

            paint.textSize = circularTextSize
            tempTextAnimParam.apply {
                textFullWidth = paint.getRunAdvance(
                    fullText, 0, fullText.length, 0, fullText.length, isRtl, fullText.length
                )
                circleRadius = circularRadius - getFontHeight()
                circleLength = 2f * Math.PI.toFloat() * circleRadius
                circularLengthFactor = circleLength / textFullWidth

                //the number of spaces is important here
                val spacesCount = fullText.count { it == ' ' } + 1
                val wCount =
                    if (circularGravity == TextAlign.center)  spacesCount else 1
                val remaining = circleLength - textFullWidth
                spaceAngle = if (wCount > 1f) InspMathUtil.getArcAngle(
                    radius = circleRadius,
                    arcLength = remaining / wCount // - spaceWidth
                ) * (1 - media.letterSpacing) else 0f
                val correctAngle = when (wCount) {
                    1 -> -InspMathUtil.getArcAngle(
                        circleRadius,
                        (textFullWidth + remaining * media.letterSpacing) / 2f
                    )
                    2 -> {
                        var result = 0f
                        val spaceIndex = fullText.indexOf(' ')
                        if (spaceIndex > 0) {
                            val wordLength = paint.getRunAdvance(
                                fullText, 0, spaceIndex, 0, spaceIndex, isRtl, spaceIndex
                            )
                            result = -InspMathUtil.getArcAngle(circleRadius, wordLength / 2f)
                        }
                        result
                    }
                    else -> (spaceAngle * (1 - media.letterSpacing)) / 2f
                }
                angleShift = correctAngle - if (circularGravity != TextAlign.right) 0 else 180
            }

        }

        genericTextHelper.drawTextBackgrounds(animParam, canvas, time, out)
        genericTextHelper.drawParts(animParam, canvas, time, out)

    }

    private fun calcTopOffsetForVerticalGravity(): Int {
        return max(height - layout.height - paddingTop - paddingBottom, 0) / 2
    }

    private fun getGradientShader(
        animParam: DrawBackgroundAnimParam, gradient: PaletteLinearGradient,
        partInfo: PartInfo, lineNumber: Int
    ): Shader {

        var right: Float
        val left: Float
        val top = if (animParam is DrawTextAnimParam) animParam.lineTop.toFloat() else animParam.top
        val bottom = animParam.height + top

        if (partInfo.type != TextPartType.line) {

            left = this.layout.getLineLeft(lineNumber) + paddingLeft
            right = this.layout.getLineRight(lineNumber) + paddingLeft

        } else {
            left = animParam.left
            right = animParam.width + left
            if (right == 0f) right = width.toFloat() + left
        }

        val (x0, x1, y0, y1) = gradient.getShaderCoords(left, top, right, bottom)
        return useCachedShader(x0, y0, x1, y1, gradient.colors.toIntArray())
    }
    private fun mayCorrectAngleShift(index: Int) {
        if (circularRadius != 0f)
            tempTextAnimParam.apply {
                if (index == 0) tempTextAnimParam.apply {
                    textPositionStart = left
                }
                else {
                    if (fullText[startTextToRender] == ' ') angleShift += spaceAngle
                    val remaining = circleLength - textFullWidth
                    val freeSpaceForChar = remaining / fullText.count()
                    angleShift += InspMathUtil.getArcAngle(
                        radius = circleRadius,
                        arcLength = freeSpaceForChar * media.letterSpacing
                    )
                }
            }
    }
    override fun drawPart(
        canvas: Canvas,
        time: Double,
        partInfo: PartInfo,
        textAnimationParams: TextAnimationParams,
        animatorOut: Boolean, partIndex: Int, partsCount: Int, lineNumber: Int
    ) {

        if (time >= partInfo.startTime || animatorOut) {

            //set params for text animation
            val index = partInfo.index
            val isRtl = this.layout.isRtlCharAt(index)

            val topOffsetForVerticalGravity = calcTopOffsetForVerticalGravity()
            tempTextAnimParam.initializeAndNullify(
                fullText,
                isRtl,
                index,
                partInfo.length,
                0f,
                0,
                this.compoundPaddingTop,
                this.layout.getLineTop(this.layout.getLineForOffset(index)) + topOffsetForVerticalGravity
            )
            //line top and padding top are for clipping

            tempTextAnimParam.color = media.textColor
            tempTextAnimParam.blurRadius = media.blurRadius

            val primHorizontalPaddingMinusWidth =
                this.layout.getPrimaryHorizontal(index) + this.compoundPaddingStart.toFloat()

            tempTextAnimParam.height = (paint.fontMetrics.bottom - paint.fontMetrics.top).toInt()
            tempTextAnimParam.left = primHorizontalPaddingMinusWidth

            //for drawing text
            tempTextAnimParam.top = this.paddingTop.toFloat() + layout.getLineBaseline(
                layout.getLineForOffset(index)
            ).toFloat() + topOffsetForVerticalGravity

            tempTextAnimParam.width = paint.getRunAdvance(
                fullText,
                index,
                index + partInfo.length,
                0,
                fullText.length,
                isRtl,
                index + partInfo.length
            ).toInt()

            mayCorrectAngleShift(index = index)

            //apply text animation
            genericTextHelper.applyAnimation(textAnimationParams,
                time,
                partInfo.startTime,
                tempTextAnimParam, partIndex, partsCount,
                this, false, animatorOut
            )

            //shadow and strokes
            if (media.paintStyle == PaintStyle.FILL_AND_STROKE) {
                paint.style = Paint.Style.FILL
            }

            val textShadowColor = media.textShadowColor
            if (textShadowColor != null) {
                tempPaint.set(paint)
                val originalAlpha = textShadowColor.alpha
                val shadowColor =
                    ArgbColorManager.colorWithAlpha(textShadowColor, (tempTextAnimParam.alpha * originalAlpha).toInt())

                tempPaint.setShadowLayer(
                    media.textShadowBlurRadius ?: 0f, media.textShadowDx ?: 0f * tempTextAnimParam.height,
                    media.textShadowDy ?: 0f * tempTextAnimParam.height, shadowColor
                )
                if (media.paintStyle == PaintStyle.STROKE) tempPaint.style = Paint.Style.STROKE
                tempTextAnimParam.applyToCanvas(canvas, tempPaint, tempRect)
            }

            if (media.textGradient != null) {

                tempTextAnimParam.shader = getGradientShader(
                    tempTextAnimParam,
                    media.textGradient!!, partInfo, this.layout.getLineForOffset(lineNumber)
                )
            }

            val shadowColors = media.shadowColors
            if (shadowColors != null) {

                val shadowOffsetX = media.shadowOffsetX ?: 0f
                val shadowOffsetY = media.shadowOffsetY ?: 0f

                tempPaint.set(paint)
                if (media.strokeWidth != null) {
                    tempPaint.style = Paint.Style.STROKE
                    tempPaint.strokeWidth = tempTextAnimParam.height * media.strokeWidth!!
                } else {
                    tempPaint.style = Paint.Style.FILL
                }

                val textAnimParamCopy = tempTextAnimParam.copy(tempShadowAnimParam, 0, 0) as DrawTextAnimParamAndroid

                for (i in (shadowColors.size - 1) downTo 0) {
                    val shadowColor = shadowColors[i]

                    val shadowOffsetPixelsY = (shadowOffsetY * averageLineHeight).toInt() * (i + 1)
                    val shadowOffsetPixelsX =
                        (if (isRtl) -1 else 1) * (shadowOffsetX * averageLineHeight).toInt() * (i + 1)

                    textAnimParamCopy.top = tempTextAnimParam.top + shadowOffsetPixelsY
                    textAnimParamCopy.lineBaseline =
                        tempTextAnimParam.lineBaseline + shadowOffsetPixelsY
                    textAnimParamCopy.paddingTop =
                        tempTextAnimParam.paddingTop + shadowOffsetPixelsY
                    textAnimParamCopy.lineTop = tempTextAnimParam.lineTop + shadowOffsetPixelsY
                    textAnimParamCopy.left = tempTextAnimParam.left + shadowOffsetPixelsX

                    textAnimParamCopy.color = shadowColor
                    textAnimParamCopy.applyToCanvas(canvas, tempPaint, tempRect)
                }
            }

            if (media.paintStyle != PaintStyle.STROKE) {
                tempTextAnimParam.applyToCanvas(canvas, paint, tempRect)
            }

            if ((media.paintStyle == PaintStyle.STROKE || media.paintStyle == PaintStyle.FILL_AND_STROKE) && media.strokeWidth != null) {

                val strokeWidth = media.strokeWidth!!

                media.textStrokeColor?.let { tempTextAnimParam.color = it }
                tempTextAnimParam.strokeWidth = strokeWidth

                tempPaint.set(paint)
                if (media.paintStyle == PaintStyle.FILL_AND_STROKE) tempTextAnimParam.shader = null
                tempPaint.style = Paint.Style.STROKE
                tempPaint.strokeWidth = tempTextAnimParam.height * strokeWidth

                tempTextAnimParam.applyToCanvas(canvas, tempPaint, tempRect)
            }
        }
    }

    override fun recompute() {
        shadersCache.clear()

        if (layout == null) {
            needsRecompute = true

        } else {

            genericTextHelper.recompute(fullText)
            needsRecompute = false
            averageLineHeight = averageLineHeight()
        }
    }


    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        needsRecompute = true
    }


    override fun setText(text: CharSequence?, type: BufferType?) {
        super.setText(text, type)
        needsRecompute = true
    }

    override fun refresh() {
        genericTextHelper.parts.clear()
        recompute()

        //to not cut curly fonts
        setShadowLayer(textSize, 0f, 0f, Color.TRANSPARENT)
    }

    class GradientShaderCache(
        var x1: Float,
        var x2: Float,
        var y1: Float,
        var y2: Float,
        var colors: IntArray
    ) {

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as GradientShaderCache

            if (x1 != other.x1) return false
            if (x2 != other.x2) return false
            if (y1 != other.y1) return false
            if (y2 != other.y2) return false
            if (!colors.contentEquals(other.colors)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = x1.hashCode()
            result = 31 * result + x2.hashCode()
            result = 31 * result + y1.hashCode()
            result = 31 * result + y2.hashCode()
            result = 31 * result + colors.contentHashCode()
            return result
        }
    }

    override var fullText: String
        get() = getText().toString()
        set(value) {
            setText(value)
        }

    fun animateGradientParam(
        startColor: Int,
        endColor: Int,
        param: DrawBackgroundAnimParam
    ) {
        param as DrawBackgroundAnimParamAndroid
        param.shader =
            useCachedShader(param.left, 0f, param.width + param.left, 0f, intArrayOf(startColor, endColor))
    }

    override var templateWidth: Int = 0
    override var templateHeight: Int = 0
    override var circularTextSize: Float = 0f
}

fun Rect.offsetRect(rect: Rect, left: Int, top: Int): Rect {
    set(rect.left + left, rect.top + top, rect.right + left, rect.bottom + top)
    return this
}
