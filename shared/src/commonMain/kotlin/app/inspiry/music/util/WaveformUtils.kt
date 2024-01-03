package app.inspiry.music.util

import kotlin.math.pow

object WaveformUtils {

    const val MIN_WAVEFORM_LEVEL = 0.05f

    fun processFrames(frameGains: IntArray, numFrames: Int): FloatArray {

        val smoothedGains = DoubleArray(numFrames)
        if (numFrames == 1) {
            smoothedGains[0] = frameGains[0].toDouble()
        } else if (numFrames == 2) {
            smoothedGains[0] = frameGains[0].toDouble()
            smoothedGains[1] = frameGains[1].toDouble()
        } else if (numFrames > 2) {
            smoothedGains[0] = (frameGains[0] / 2.0 + frameGains[1] / 2.0)
            for (i in 1 until numFrames - 1) {
                smoothedGains[i] = (frameGains[i - 1] / 3.0 +
                        frameGains[i] / 3.0 +
                        frameGains[i + 1] / 3.0)
            }
            smoothedGains[numFrames - 1] =
                (frameGains[numFrames - 2] / 2.0 + frameGains[numFrames - 1] / 2.0)
        }

        // Make sure the range is no more than 0 - 255
        var maxGain = 1.0
        for (i in 0 until numFrames) {
            if (smoothedGains[i] > maxGain) {
                maxGain = smoothedGains[i]
            }
        }
        var scaleFactor = 1.0
        if (maxGain > 255.0) {
            scaleFactor = 255 / maxGain
        }

        // Build histogram of 256 bins and figure out the new scaled max
        maxGain = 0.0
        val gainHist = IntArray(256)
        for (i in 0 until numFrames) {
            var smoothedGain = (smoothedGains[i] * scaleFactor).toInt()
            if (smoothedGain < 0)
                smoothedGain = 0
            if (smoothedGain > 255)
                smoothedGain = 255

            if (smoothedGain > maxGain)
                maxGain = smoothedGain.toDouble()

            gainHist[smoothedGain]++
        }

        // Re-calibrate the min to be 5%
        var minGain = 0.0
        var sum = 0
        while (minGain < 255 && sum < numFrames / 20) {
            sum += gainHist[minGain.toInt()]
            minGain++
        }

        // Re-calibrate the max to be 99%
        sum = 0
        while (maxGain > 2 && sum < numFrames / 100) {
            sum += gainHist[maxGain.toInt()]
            maxGain--
        }

        // Compute the heights
        val heights = FloatArray(numFrames)
        val range = maxGain - minGain

        for (i in 0 until numFrames) {
            var value = (smoothedGains[i] * scaleFactor - minGain) / range

            if (value < MIN_WAVEFORM_LEVEL)
                value = 0.05
            if (value > 1.0)
                value = 1.0

            var scaledValue = (value * value).toFloat()
            if (scaledValue < MIN_WAVEFORM_LEVEL)
                scaledValue = MIN_WAVEFORM_LEVEL

            heights[i] = scaledValue
        }
        return heights
    }

    fun postProcessFrames(frames: FloatArray) {

        val max = frames.maxOrNull()
        if (max != null && max < 1) {
            for (i in frames.indices) {
                frames[i] = frames[i] / max
            }
        }
    }

    fun processFrames(
        frameGains: ShortArray, numFrames: Int = frameGains.size,
        amplify: Float = 1f, pow: Double = 2.0
    ): FloatArray {

        val smoothedGains = DoubleArray(numFrames)
        if (numFrames == 1) {
            smoothedGains[0] = frameGains[0].toDouble()
        } else if (numFrames == 2) {
            smoothedGains[0] = frameGains[0].toDouble()
            smoothedGains[1] = frameGains[1].toDouble()
        } else if (numFrames > 2) {
            smoothedGains[0] = (frameGains[0] / 2.0 + frameGains[1] / 2.0)
            for (i in 1 until numFrames - 1) {
                smoothedGains[i] = (frameGains[i - 1] / 3.0 +
                        frameGains[i] / 3.0 +
                        frameGains[i + 1] / 3.0)
            }
            smoothedGains[numFrames - 1] =
                (frameGains[numFrames - 2] / 2.0 + frameGains[numFrames - 1] / 2.0)
        }

        // Make sure the range is no more than 0 - 255
        var maxGain = 1.0
        for (i in 0 until numFrames) {
            if (smoothedGains[i] > maxGain) {
                maxGain = smoothedGains[i]
            }
        }
        var scaleFactor = 1.0
        if (maxGain > 255.0) {
            scaleFactor = 255 / maxGain
        }

        // Build histogram of 256 bins and figure out the new scaled max
        maxGain = 0.0
        val gainHist = IntArray(256)
        for (i in 0 until numFrames) {
            var smoothedGain = (smoothedGains[i] * scaleFactor).toInt()
            if (smoothedGain < 0)
                smoothedGain = 0
            if (smoothedGain > 255)
                smoothedGain = 255

            if (smoothedGain > maxGain)
                maxGain = smoothedGain.toDouble()

            gainHist[smoothedGain]++
        }

        // Re-calibrate the min to be 5%
        var minGain = 0.0
        var sum = 0
        while (minGain < 255 && sum < numFrames / 20) {
            sum += gainHist[minGain.toInt()]
            minGain++
        }

        // Re-calibrate the max to be 99%
        sum = 0
        while (maxGain > 2 && sum < numFrames / 100) {
            sum += gainHist[maxGain.toInt()]
            maxGain--
        }

        // Compute the heights
        val heights = FloatArray(numFrames)
        val range = maxGain - minGain

        for (i in 0 until numFrames) {
            var value = (smoothedGains[i] * scaleFactor - minGain) / range

            value *= amplify

            if (value < MIN_WAVEFORM_LEVEL)
                value = 0.05
            else if (value > 1.0)
                value = 1.0

            var scaledValue = value.pow(pow).toFloat()

            if (scaledValue < MIN_WAVEFORM_LEVEL)
                scaledValue = MIN_WAVEFORM_LEVEL
            else if (scaledValue > 1.0)
                scaledValue = 1.0f

            heights[i] = scaledValue
        }
        return heights
    }
}