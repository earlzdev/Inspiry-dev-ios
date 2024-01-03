package app.inspiry.font.helpers

class TypefaceObtainingException(val originalError: Throwable): Exception(originalError.message)