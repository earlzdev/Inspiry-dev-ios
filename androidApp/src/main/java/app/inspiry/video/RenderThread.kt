package app.inspiry.video

import android.os.Handler
import android.os.HandlerThread

class RenderThread {

    private val thread = HandlerThread(this::class.java.name)
    internal val handler: Handler

    init {
        thread.isDaemon = true
        thread.start()
        handler = Handler(thread.looper)
    }

    fun run(block: () -> Unit) {
        if (Thread.currentThread().id == thread.id) block()
        else handler.post(block)
    }
}

private val renderThread by lazy { RenderThread() }

val renderThreadHandler: Handler
    get() = renderThread.handler

fun renderThread(block: () -> Unit) {
    renderThread.run(block)
}