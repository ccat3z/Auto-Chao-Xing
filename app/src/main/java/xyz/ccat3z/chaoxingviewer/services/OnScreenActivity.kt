package xyz.ccat3z.chaoxingviewer.services

import android.util.Log
import android.view.accessibility.AccessibilityEvent
import io.reactivex.rxjava3.core.Observable
import xyz.ccat3z.chaoxingviewer.Common

class OnScreenActivity(accessibilityEvents: Observable<AccessibilityEvent>) {
    val currentActivity: Observable<ActivityInfo> = accessibilityEvents
            .filter {
                it.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
                        && it.source != null
            }
            .map { ActivityInfo(it.className.toString()) }
            .distinctUntilChanged()
            .doOnNext {
                Log.d("OnScreenActivity", "in activity: $it")
                currentActivityInfo = it
            }
            .replay(1)
            .refCount()

    var currentActivityInfo: ActivityInfo = ActivityInfo("unknown")
        private set

    init {
        currentActivity.subscribe()
    }
}

data class ActivityInfo(val className: String)
