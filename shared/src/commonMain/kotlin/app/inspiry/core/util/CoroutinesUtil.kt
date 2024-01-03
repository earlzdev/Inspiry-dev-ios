package app.inspiry.core.util

import app.inspiry.core.data.InspResponse
import app.inspiry.core.data.InspResponseData
import app.inspiry.core.data.InspResponseError
import app.inspiry.core.log.ErrorHandler
import app.inspiry.core.media.Template
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.buffer
import kotlin.coroutines.CoroutineContext

inline fun <T> CoroutineScope.collectUntil(
    stateFlow: StateFlow<T>,
    crossinline condition: (T) -> Boolean,
    crossinline action: suspend () -> Unit
) {
    launch {
        stateFlow.collect {
            if (condition(it)) {
                this.cancel()
                action()
            }
        }
    }
}

fun createDefaultScope() = CoroutineScope(SupervisorJob() + Dispatchers.Main)
fun createScope() = CoroutineScope(SupervisorJob() + Dispatchers.Default)
//can be used in swift code
val uiDispatcherCommon: CoroutineContext
    get() = Dispatchers.Main
val ioDispatcherCommon: CoroutineContext
    get() = Dispatchers.Default

val scopeMain = CoroutineScope(uiDispatcherCommon)
val scopeIO = CoroutineScope(ioDispatcherCommon)

inline fun <T> CoroutineScope.collectUntil(
    stateFlow: StateFlow<T>,
    crossinline condition: (T) -> Boolean,
    crossinline action: suspend (T) -> Unit
) {
    launch {
        stateFlow.buffer().collect {
            if (condition(it)) {
                cancel()
                action(it)
            }
        }
    }
}

fun <T>createStateFlow(value: T) = MutableStateFlow(value)

suspend fun StateFlow<InspResponse<Template>?>.collectInActivity(
    errorHandler: ErrorHandler, finish: () -> Unit,
    onTemplateLoaded: (Template) -> Unit
) {
    collect {
        if (it != null) {
            when (it) {
                is InspResponseData -> {
                    onTemplateLoaded(it.data)
                }
                is InspResponseError -> {
                    errorHandler.toastError(it.throwable)
                    finish()
                }
                else -> {
                    throw IllegalStateException("unknown type of InspResponse")
                }
            }
        }

    }
}