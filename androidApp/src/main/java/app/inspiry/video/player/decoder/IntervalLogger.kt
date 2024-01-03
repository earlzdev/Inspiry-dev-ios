package app.inspiry.video.player.decoder

import android.util.Log
import app.inspiry.BuildConfig
import kotlin.math.max
import kotlin.math.min

class IntervalLogger(
        private val tag: String,
        private val dropIntervalMs: Long = Long.MAX_VALUE
) {

    private var lastTimeMs = 0L
    private var totalResetMs = 0L
    private var totalMs = 0L
    private var count = 0
    private var minMs = Long.MAX_VALUE
    private var maxMs = Long.MIN_VALUE
    private var lastIntervalMs = 0L

    fun log() {
        if (BuildConfig.DEBUG) {
            if (lastTimeMs != 0L) {
                if (totalResetMs >= RESET_INTERVAL_MS) {
                    reset()
                }
                calculate()
                Log.d(LOG_TAG, "$tag: $lastIntervalMs [$minMs, $maxMs] ${totalMs / count}")
            }
            lastTimeMs = System.currentTimeMillis()
        }
    }

    fun reset() {
        count = 0
        minMs = Long.MAX_VALUE
        maxMs = Long.MIN_VALUE
        totalMs = 0
        totalResetMs = 0
        Log.d(LOG_TAG, "$tag: ______________________ reset ________________________")
    }

    private fun calculate() {
        lastIntervalMs = System.currentTimeMillis() - lastTimeMs
        if (lastIntervalMs < dropIntervalMs) {
            count++
            minMs = min(minMs, lastIntervalMs)
            maxMs = max(maxMs, lastIntervalMs)
            totalMs += lastIntervalMs
            totalResetMs += lastIntervalMs
        }
    }

    companion object {
        private const val LOG_TAG = "interval_logger"
        private const val RESET_INTERVAL_MS = 5000
    }
}