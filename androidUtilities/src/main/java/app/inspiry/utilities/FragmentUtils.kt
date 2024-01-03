package app.inspiry.utilities

import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner


fun <T : Fragment> T.putArgs(action: Bundle.() -> Unit): T {
    val b = Bundle()
    action(b)
    arguments = b
    return this
}


fun LifecycleOwner.doOnDestroy(action: () -> Unit) {
    lifecycle.addObserver(object : LifecycleEventObserver {
        override fun onStateChanged(owner: LifecycleOwner, event: Lifecycle.Event) {
            if (event == Lifecycle.Event.ON_DESTROY) {
                action()
                lifecycle.removeObserver(this)
            }
        }
    })
}

fun LifecycleOwner.doOnStop(action: () -> Unit) {
    lifecycle.addObserver(object : LifecycleEventObserver {
        override fun onStateChanged(owner: LifecycleOwner, event: Lifecycle.Event) {
            if (event == Lifecycle.Event.ON_STOP) {
                action()
                lifecycle.removeObserver(this)
            }
        }
    })
}

fun LifecycleOwner.doOnStart(action: () -> Unit) {
    if (lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
        action()
    } else {
        lifecycle.addObserver(object : LifecycleEventObserver {
            override fun onStateChanged(owner: LifecycleOwner, event: Lifecycle.Event) {
                if (event == Lifecycle.Event.ON_START) {
                    action()
                    lifecycle.removeObserver(this)
                }
            }
        })
    }
}

fun DialogFragment.bindToActivityLifecycle() {
    requireActivity().doOnStop {
        dismissAllowingStateLoss()
    }
}