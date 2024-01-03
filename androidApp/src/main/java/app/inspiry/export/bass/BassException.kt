package app.inspiry.export.bass

class BassException(
    val errorCode: Int,
    message: String? = null
) : Exception(message)