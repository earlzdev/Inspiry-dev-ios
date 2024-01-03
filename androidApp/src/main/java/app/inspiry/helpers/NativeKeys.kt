package app.inspiry.helpers

class NativeKeys {
    external fun stringFromJNI1(): String

    companion object {
        init {
            System.loadLibrary("inspiry")
        }
    }
}