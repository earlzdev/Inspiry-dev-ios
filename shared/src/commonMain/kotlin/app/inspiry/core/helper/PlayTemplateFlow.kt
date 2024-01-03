package app.inspiry.core.helper

import app.inspiry.core.data.FRAME_IN_MILLIS
import com.soywiz.klock.DateTime
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min

object PlayTemplateFlow {

    fun create(
        startFromFrame: Int,
        maxFrames: Int, loopAnimation: Boolean
    ): Flow<Int> {

        return flow {
            var plusFrames = 0

            var frames = startFromFrame
            var timeStarted =
                DateTime.nowUnixLong() - max(0, (frames * FRAME_IN_MILLIS).toLong())
            var skippedTimeMillis = 0L

            fun reset() {
                frames = 0
                skippedTimeMillis = 0L
                timeStarted = DateTime.nowUnixLong()
            }


            while (currentCoroutineContext().isActive) {

                val currentTimeMillis = DateTime.nowUnixLong()

                if (frames >= (maxFrames - 1) && loopAnimation) {
                    plusFrames = 0
                    reset()
                } else {
                    frames += plusFrames
                }

                emit(frames)

                if (frames >= (maxFrames - 1) && !loopAnimation) {
                    emit(Int.MAX_VALUE)
                    break
                } else {

                    val curTimeAfterDraw = DateTime.nowUnixLong()

                    val timeDeltaToDraw = curTimeAfterDraw - currentTimeMillis
                    skippedTimeMillis += timeDeltaToDraw

                    plusFrames = 1
                    if (skippedTimeMillis > FRAME_IN_MILLIS) {

                        //if we have 50 maxFrames, 48 is currentFrame, 3 is plusFrames
                        //then plusFrames need to become 1
                        //-2 because we already have plusFrame = 1
                        //this is needed to trigger the last frame of animation

                        val maxAdditionalCanTake = maxFrames - frames - 2
                        val skippedFrames =
                            min(
                                floor(timeDeltaToDraw / FRAME_IN_MILLIS).toInt(),
                                maxAdditionalCanTake
                            )

                        if (skippedFrames > 0) {
                            skippedTimeMillis -= (skippedFrames * FRAME_IN_MILLIS).toLong()
                            plusFrames += skippedFrames
                        }
                    }

                    if (ADJUST_TIME_ERROR) {
                        val timePassedInViews = frames * FRAME_IN_MILLIS
                        val timePassedSinceStart = curTimeAfterDraw - timeStarted
                        val timeError = timePassedSinceStart - timePassedInViews

                        //sleep for duration of one frame or less if we skipped some
                        if ((timeDeltaToDraw + timeError) < FRAME_IN_MILLIS) {
                            val sleepTime = (FRAME_IN_MILLIS - timeDeltaToDraw - timeError).toLong()
                            delay(sleepTime)
                        }

                    } else {
                        if (timeDeltaToDraw < FRAME_IN_MILLIS) {
                            val sleepTime = FRAME_IN_MILLIS - timeDeltaToDraw
                            delay(sleepTime.toLong())
                        }
                    }
                }
            }
        }
    }


    private const val ADJUST_TIME_ERROR = true
}