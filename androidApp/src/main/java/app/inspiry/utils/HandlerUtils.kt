package app.inspiry.utils

import android.os.Handler
import android.os.Message


fun Handler.getPostMessageCompat(r: () -> Unit, token: Any): Message {
    val m = Message.obtain(this, r)
    m.obj = token
    return m
}