package app.inspiry.core.data

sealed class InspResponse<T>

data class InspResponseLoading<T>(val progress: Float? = null) : InspResponse<T>()
data class InspResponseData<T>(val data: T) : InspResponse<T>()
data class InspResponseError<T>(val throwable: Throwable) : InspResponse<T>()


class InspResponseNothing<T>: InspResponse<T>() {
    override fun toString(): String {
        return "InspResponseNothing()"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is InspResponseNothing<*>) return false
        return true
    }

    override fun hashCode(): Int {
        return this::class.hashCode()
    }

}