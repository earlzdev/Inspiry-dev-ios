package app.inspiry.music

import android.content.Context
import android.view.View

interface InstrumentViewAndroid {
    fun createView(context: Context): View
    fun onDestroyView() {}
}