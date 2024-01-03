package app.inspiry.export.codec

interface DataSourceAsync: DataSource {

    // false if currently no buffers are available, but they might become available in the future
    fun canWrite(): Boolean

    // the listener is invoked when canWrite becomes true. It is necessary for asynchronous mediaCodec mode
    fun setCanWriteListener(canWrite: () -> Unit)
}