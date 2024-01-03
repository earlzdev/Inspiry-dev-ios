/*
 * Copyright (C) 2008 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package app.inspiry.music.android.waveform

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.core.net.toFile
import app.inspiry.music.util.WaveformUtils
import app.inspiry.core.data.InspResponse
import app.inspiry.core.data.InspResponseData
import app.inspiry.core.util.getExt
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.internal.closeQuietly
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream
import java.util.*

/**
 * CheapSoundFile is the parent class of several subclasses that each
 * do a "cheap" scan of various sound file formats, parsing as little
 * as possible in order to understand the high-level frame structure
 * and get a rough estimate of the volume level of each frame.  Each
 * subclass is able to:
 * - open a sound file
 * - return the sample rate and number of frames
 * - return an approximation of the volume level of each frame
 *
 * A frame should represent no less than 1 ms and no more than 100 ms of
 * audio.  This is compatible with the native frame sizes of most audio
 * file formats already, but if not, this class should expose virtual
 * frames in that size range.
 *
 * Modified by Anna Stępień <anna.stepien></anna.stepien>@semantive.com>
 */
abstract class CheapSoundFile protected constructor() {
    interface ProgressListener {
        /**
         * Will be called by the CheapSoundFile subclass periodically
         * with values between 0.0 and 1.0.  Return true to continue
         * loading the file, and false to cancel.
         */
        fun reportProgress(fractionComplete: Double): Boolean
    }

    interface Factory {
        fun create(): CheapSoundFile
        val supportedExtensions: Array<String>
    }

    companion object {
        var sSubclassFactories = arrayOf(
            CheapAAC.getFactory(),
            CheapAMR.getFactory(),
            CheapMP3.getFactory(),
            CheapWAV.getFactory()
        )
        var sSupportedExtensions = ArrayList<String>()
        var sExtensionMap = HashMap<String, Factory>()

        /**
         * Static method to create the appropriate CheapSoundFile subclass
         * given a filename.
         *
         * TODO: make this more modular rather than hardcoding the logic
         */
        @Throws(Exception::class)
        fun create(uri: Uri, context: Context): Flow<InspResponse<WaveFormData>> {

            return flow {

                val extension: String
                var inputStream: InputStream? = null
                val fileSize: Long

                try {
                    if (uri.scheme == "file") {
                        val f = uri.toFile()
                        if (!f.exists()) {
                            throw FileNotFoundException(uri.toString())
                        }
                        extension =
                            f.name.getExt()
                                ?: throw IllegalStateException("extension is unknown ${f.name}")
                        inputStream = f.inputStream()
                        fileSize = f.length()

                    } else if (uri.scheme == "content") {

                        val resolver = context.contentResolver
                        val mime: MimeTypeMap = MimeTypeMap.getSingleton()
                        extension = mime.getExtensionFromMimeType(resolver.getType(uri))
                            ?: throw IllegalStateException("extension is unknown ${uri}")
                        inputStream = resolver.openInputStream(uri)
                            ?: throw IllegalStateException("can't open inputStream ${uri}")

                        fileSize = resolver.openFileDescriptor(uri, "r")?.use {
                            it.statSize
                        } ?: throw IllegalStateException("can't get file size")

                    } else
                        throw IllegalStateException("unknown scheme ${uri.scheme}")


                    val factory = sExtensionMap[extension]
                        ?: throw IllegalStateException("didn't find ")

                    val soundFile = factory.create()

                    /*soundFile.setProgressListener(object : ProgressListener {
                        override fun reportProgress(fractionComplete: Double): Boolean {
                            emit(ResponseLoading(fractionComplete.toFloat()))
                            return true
                        }
                    })*/
                    soundFile.readStream(inputStream, fileSize)

                    emit(
                        InspResponseData(
                            WaveFormData(
                                soundFile.sampleRate, soundFile.channels, 0,
                                samples = WaveformUtils.processFrames(
                                    soundFile.frameGains ?: IntArray(0),
                                    soundFile.numFrames
                                ), numSamples = soundFile.numFrames
                            )
                        )
                    )

                } finally {
                    inputStream?.closeQuietly()
                }
            }
        }


        init {
            for (f in sSubclassFactories) {
                for (extension in f.supportedExtensions) {
                    sSupportedExtensions.add(extension)
                    sExtensionMap[extension] = f
                }
            }
        }
    }

    @JvmField
    protected var mProgressListener: ProgressListener? = null


    @Throws(FileNotFoundException::class, IOException::class)
    open fun readStream(inputStream: InputStream, fileSize: Long) {
        fileSizeBytes = fileSize
    }

    fun setProgressListener(progressListener: ProgressListener?) {
        mProgressListener = progressListener
    }


    var fileSizeBytes: Long = 0L

    open val numFrames: Int
        get() = 0
    open val samplesPerFrame: Int
        get() = 0
    open val frameGains: IntArray?
        get() = null

    open val avgBitrateKbps: Int
        get() = 0
    open val sampleRate: Int
        get() = 0
    open val channels: Int
        get() = 0
    open val filetype: String?
        get() = "Unknown"

    /**
     * If and only if this particular file format supports seeking
     * directly into the middle of the file without reading the rest of
     * the header, this returns the byte offset of the given frame,
     * otherwise returns -1.
     */
    fun getSeekableFrameOffset(frame: Int): Int {
        return -1
    }
}