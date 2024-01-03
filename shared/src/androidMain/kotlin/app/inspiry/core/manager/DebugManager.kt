package app.inspiry.core.manager

import app.inspiry.projectutils.BuildConfig

actual object DebugManager {
    actual val isDebug: Boolean
        get() = BuildConfig.DEBUG

}