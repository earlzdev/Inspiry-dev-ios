package app.inspiry.core.manager

actual object DebugManager {
    actual val isDebug: Boolean
        get() = Platform.isDebugBinary
}