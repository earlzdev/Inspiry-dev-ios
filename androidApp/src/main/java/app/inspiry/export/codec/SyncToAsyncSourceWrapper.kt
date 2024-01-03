package app.inspiry.export.codec

import java.nio.ByteBuffer

class SyncToAsyncSourceWrapper(val source: DataSource): DataSourceAsync {

    override fun canWrite(): Boolean {
        return true
    }

    override fun setCanWriteListener(canWrite: () -> Unit) {

    }

    override fun getMediaFormat() = source.getMediaFormat()

    override fun release() {
        source.release()
    }

    override fun read(inputBuffer: ByteBuffer) = source.read(inputBuffer)

    override fun start() {
        source.start()
    }
}