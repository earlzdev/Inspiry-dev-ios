package app.inspiry.core.data

const val FPS = 30
const val FRAME_IN_MILLIS = 1000.0 / FPS

fun Int.frameToTimeUs() = (this * FRAME_IN_MILLIS * 1000L).toLong()
fun Int.frameToTimeMillis() = (this * FRAME_IN_MILLIS).toLong()
fun Int.frameToTimeNano() = (this * FRAME_IN_MILLIS * 1000_000L).toLong()