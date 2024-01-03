package app.inspiry.views.text

import app.inspiry.core.animator.TextAnimationParams
import app.inspiry.core.animator.applyAnimationText
import app.inspiry.core.animator.text.DrawBackgroundAnimParam
import app.inspiry.core.data.FRAME_IN_MILLIS
import app.inspiry.core.media.MediaText
import app.inspiry.core.media.PartInfo
import app.inspiry.core.media.TextPartType
import kotlin.math.min
import kotlin.random.Random

class GenericTextHelper<Canvas>(
    val media: MediaText,
    val layout: InnerGenericText<Canvas>
) {

    var getStartTime: () -> Int = { 0 }
    var getDuration: () -> Int = { 0 }

    val animationParamIn: TextAnimationParams = media.animationParamIn ?: TextAnimationParams()
    val animationParamOut: TextAnimationParams = media.animationParamOut ?: TextAnimationParams()

    /**
     * Those don't include startTime of media, but include startTime of animator
     */
    var durationIn: Int = 0
    var durationOut: Int = 0

    val parts: MutableMap<TextAnimationParams, PartInfo> = HashMap()

    fun getPartInfo(animParam: TextAnimationParams): PartInfo? {
        return parts[animParam]
    }

    /**
     * Parse text to parts. This is simple.
     * No matter which animations we have we parse text.
     */
    fun computeParts(text: String, textAnimationParams: TextAnimationParams): PartInfo {
        var partsCharacter = ArrayList<PartInfo>()
        var partsWords = ArrayList<PartInfo>()
        val partLines = ArrayList<PartInfo>()
        val allWords = ArrayList<PartInfo>()

        val allChars = ArrayList<PartInfo>(text.length)

        val textLength = text.length
        var startTime = 0.0
        var counter = 0
        var timeStartWords = 0.0
        var timeStartLines = 0.0
        var prevChar = 0.toChar()
        var lineIndex = prevChar.code
        var nextCounter: Int
        var wordIndex = 0

        while (counter < textLength) {
            val curChar = text[counter]
            partsCharacter.add(PartInfo(counter, 1, startTime, TextPartType.character, null))

            nextCounter = counter + 1

            val lineForOffsetCounter = layout.getLineForOffset(counter)
            val lineForOffset = layout.getLineForOffset(min(nextCounter, text.length - 1))


            if (textAnimationParams.charDelayBetweenWords || curChar != '\n' && curChar != ' ')
                startTime += textAnimationParams.charDelayMillis

            if (curChar != '\n' && lineForOffsetCounter == lineForOffset) {
                if (curChar == ' ' && prevChar != ' ' && prevChar != '\n') {
                    partsWords.add(
                        PartInfo(
                            wordIndex,
                            counter - wordIndex + 1,
                            timeStartWords,
                            TextPartType.word, partsCharacter
                        )
                    )
                    allChars.addAll(partsCharacter)
                    partsCharacter = ArrayList()

                    startTime += textAnimationParams.wordDelayMillis
                    timeStartWords = startTime
                    wordIndex = nextCounter
                }
            } else {
                partsWords.add(
                    PartInfo(
                        wordIndex,
                        counter - wordIndex + 1,
                        timeStartWords,
                        TextPartType.word, partsCharacter
                    )
                )

                allChars.addAll(partsCharacter)
                partsCharacter = ArrayList()

                partLines.add(
                    PartInfo(
                        lineIndex,
                        counter - lineIndex + 1,
                        timeStartLines,
                        TextPartType.line,
                        partsWords
                    )
                )

                allWords.addAll(partsWords)
                partsWords = ArrayList()

                startTime += textAnimationParams.lineDelayMillis
                timeStartWords = startTime
                timeStartLines = startTime
                wordIndex = nextCounter
                lineIndex = nextCounter
            }
            prevChar = curChar
            counter = nextCounter
        }

        allChars.addAll(partsCharacter)
        partsWords.add(
            PartInfo(
                wordIndex,
                text.length - wordIndex,
                timeStartWords,
                TextPartType.word, partsCharacter
            )
        )

        allWords.addAll(partsWords)
        partLines.add(
            PartInfo(
                lineIndex,
                text.length - lineIndex,
                timeStartLines,
                TextPartType.line,
                partsWords
            )
        )

        val result = PartInfo(0, text.length, 0.0, TextPartType.wholeText, partLines)
        textAnimationParams.applyInterpolators(allChars, allWords, partLines)
        changeOrderIfNeeded(textAnimationParams, allChars, allWords, partLines)

        return result
    }

    fun mayApplyBackAnimOut(animParam: TextAnimationParams, backgroundAnimParam: DrawBackgroundAnimParam, shadowMode: Boolean, partIndex: Int, partsCount: Int) {
        if (animParam == animationParamOut) {
            for (group in animationParamOut.backgroundAnimatorGroups) {
                if (if (shadowMode) group.showApplyShadowAnimation(
                        partIndex,
                        partsCount
                    ) else group.shouldApplyAnimation(
                        partIndex, partsCount
                    )
                ) {

                    for (animator in group.animators) {
                        animator.applyAnimationText(backgroundAnimParam, 1f, layout)
                    }
                }
            }
        }
    }

    private fun changeOrderIfNeeded(
        textAnimationParams: TextAnimationParams,
        allChars: ArrayList<PartInfo>, allWords: ArrayList<PartInfo>, allLines: ArrayList<PartInfo>
    ) {
        if (textAnimationParams.reverse) {

            fun reverseStartTimes(list: ArrayList<PartInfo>) {

                val listStartTimes = list.map { it.startTime }
                val reversed = listStartTimes.toMutableList()
                reversed.reverse()

                list.forEachIndexed { index, partInfo ->
                    partInfo.startTime = reversed[index]
                }
            }

            when (textAnimationParams.textPartType) {
                TextPartType.character -> reverseStartTimes(allChars)
                TextPartType.word -> reverseStartTimes(allWords)
                else -> reverseStartTimes(allLines)
            }

        } else if (textAnimationParams.shuffle) {
            val random = Random(42)

            fun shuffleStartTimes(list: ArrayList<PartInfo>) {

                val listStartTimes = list.map { it.startTime }
                val shuffled = listStartTimes.toMutableList()
                shuffled.shuffle(random)

                list.forEachIndexed { index, partInfo ->
                    partInfo.startTime = shuffled[index]
                }
            }

            when (textAnimationParams.textPartType) {
                TextPartType.character -> shuffleStartTimes(allChars)
                TextPartType.word -> shuffleStartTimes(allWords)
                else -> shuffleStartTimes(allLines)
            }
        }
    }

    fun recompute(text: String) {
        parts[animationParamIn] = computeParts(text, animationParamIn)
        parts[animationParamOut] = computeParts(text, animationParamOut)
        calcDurations()
    }

    fun calcDurations() {
        val durations = calcDurations(true)
        durationIn = durations.first
        durationOut = durations.second
    }

    fun calcPartCounts(p: PartInfo?): IntArray {

        //val p = parts[animParam]

        val charsCount = p?.subParts?.sumOf { it.subParts?.sumOf { it.subParts?.size ?: 0 } ?: 0 }
            ?: 1
        val linesCount = p?.subParts?.size ?: 1
        val wordsCount = p?.subParts?.sumOf { it.subParts?.size ?: 0 } ?: 1

        return intArrayOf(charsCount, wordsCount, linesCount)
    }

    fun applyAnimation(textAnimationParams: TextAnimationParams,
                       time: Double,
                       partStartTime: Double,
                       backgroundAnimParam: DrawBackgroundAnimParam,
                       partIndex: Int, partsCount: Int,
                       view: InnerGenericText<*>, shadowMode: Boolean, out: Boolean) {
        textAnimationParams.textAnimatorGroups.applyAnimationText(
            time,
            partStartTime,
            backgroundAnimParam, partIndex, partsCount,
            view, shadowMode, out)

    }
    fun onDrawText(
        canvas: Canvas,
        currentFrame: Int
    ) {
        val startTime = getStartTime()
        //TODO: improve out algorithm. We need to preserve in animation until the end.
        // But we can overlap it with out animation if necessary.

        if (currentFrame >= startTime) {

            val drawIn =
                currentFrame < durationIn + startTime + media.delayBeforeEnd || durationOut == 0

            if (drawIn) {

                layout.draw(animationParamIn, canvas, (currentFrame - startTime) * FRAME_IN_MILLIS, false)
            } else {

                val duration = getDuration()
                layout.draw(
                    animationParamOut, canvas,
                    (currentFrame - startTime - duration +
                            durationOut + media.delayBeforeEnd) * FRAME_IN_MILLIS,
                    true
                )
            }
        }
    }

    fun lackBackground() = media.lackBackgroundLineColor() || !media.hasBackground()

    fun drawTextBackgrounds(
        animParam: TextAnimationParams,
        canvas: Canvas,
        time: Double,
        out: Boolean
    ) {
        if (animParam.backgroundAnimatorGroups.any { it.group.startsWith("shadow") }) {
            layout.drawTextBackgroundsSingle(animParam, canvas, time, out, true)
        }
        layout.drawTextBackgroundsSingle(animParam, canvas, time, out, false)
    }


    /**
     * Call drawPart method for every part
     */
    fun drawParts(
        animParam: TextAnimationParams,
        canvas: Canvas,
        time: Double,
        out: Boolean
    ) {
        val parts = parts[animParam]!!

        if (parts.subParts != null) {
            for ((lineNumber, subPart) in parts.subParts.withIndex()) {

                if (animParam.textPartType == TextPartType.line) {
                    layout.drawPart(
                        canvas,
                        time,
                        subPart,
                        animParam,
                        out,
                        lineNumber,
                        parts.subParts.size,
                        lineNumber
                    )

                } else if (subPart.subParts != null) {

                    for ((index, subsubParts) in subPart.subParts.withIndex()) {
                        if (animParam.textPartType == TextPartType.word) {
                            layout.drawPart(
                                canvas,
                                time,
                                subsubParts,
                                animParam,
                                out,
                                index,
                                subPart.subParts.size, lineNumber
                            )

                        } else if (subsubParts.subParts != null) {
                            for ((index, partInfo) in subsubParts.subParts.withIndex()) {
                                layout.drawPart(
                                    canvas,
                                    time,
                                    partInfo,
                                    animParam,
                                    out,
                                    index,
                                    subsubParts.subParts.size, lineNumber
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    @Deprecated("old method")
    private fun calcPartCounts(text: String): IntArray {
        if (text.isBlank()) return intArrayOf(0, 0, 0)

        var increment = 0
        var prevChar = 0.toChar()
        var charsCount = 0
        var linesCount = 1
        var wordsCount = 1

        while (increment < text.length) {

            val curChar = text[increment]
            val thisLine = layout.getLineForOffset(increment)
            ++increment
            val nextLine = layout.getLineForOffset(min(increment, text.length - 1))

            if (curChar != '\n' && thisLine == nextLine) {

                if (curChar == ' ' && prevChar != ' ' && prevChar != '\n') {
                    wordsCount++
                }
            } else {
                wordsCount++
                linesCount++
            }
            charsCount++
            prevChar = curChar
        }
        return intArrayOf(charsCount, wordsCount, linesCount)
    }


    fun calcDurations(includeStartTimeToOut: Boolean): Pair<Int, Int> {

        val countsIn = calcPartCounts(parts[animationParamIn])
        val countsOut = calcPartCounts(parts[animationParamOut])

        val inD = animationParamIn.calcDuration(countsIn[0], countsIn[1], countsIn[2], true)
        val outD = animationParamOut.calcDuration(
            countsOut[0],
            countsOut[1],
            countsOut[2],
            includeStartTimeToOut
        )

        return Pair(inD, outD)
    }
}