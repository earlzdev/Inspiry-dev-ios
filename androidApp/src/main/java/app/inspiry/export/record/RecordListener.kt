package app.inspiry.export.record

interface RecordListener {
    fun onError(e: Throwable, isCritical: Boolean)
    fun onFinish(timeTook: Long)
    fun onUpdate(progress: Float, encoderType: EncoderType)
}
