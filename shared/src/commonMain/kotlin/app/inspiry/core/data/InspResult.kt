package app.inspiry.core.data

data class ResultWrapper<T>(var result: Result<T>) {
    fun getOrNull(): T? = result.getOrNull()
    fun errorOrNull(): Throwable? = result.exceptionOrNull()
    fun getOrThrow() = result.getOrThrow()
    fun exceptionOrNull() = result.exceptionOrNull()
    val isSuccess: Boolean
        get() { return result.isSuccess }
    val isFailure: Boolean
        get() { return result.isFailure}
}