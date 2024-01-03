package app.inspiry.music.android.waveform

import app.inspiry.music.util.WaveformUtils
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Copyright 2018 siy1121
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 *Data object contains raw sample data of 16bit short array and some params
 *
 * You have to use [WaveFormData.Factory] to build
 *
 * @property sampleRate Sample rate of data
 * @property channel Count of channel
 * @property duration Duration of data in milliseconds
 * @property samples Raw sample data
 */
class WaveFormData constructor(
    val sampleRate: Int, val channel: Int, val duration: Long,
    var samples: FloatArray = FloatArray(0), val numSamples: Int = samples.size
) {

    constructor(
        sampleRate: Int,
        channel: Int,
        duration: Long,
        stream: ByteArrayOutputStream
    ) : this(sampleRate, channel, duration) {
        samples = WaveformUtils.processFrames(stream.toShortArray(), amplify = 5f, pow = 2.0)
    }

    private fun ByteArrayOutputStream.toShortArray(): ShortArray {
        val array =
            ByteBuffer.wrap(this.toByteArray()).order(ByteOrder.nativeOrder()).asShortBuffer()
        val results = ShortArray(array.remaining())
        array.get(results)
        return results
    }
}