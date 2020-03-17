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
            .doOnNext { Log.d(Common.LOG_TAG, "in activity: $it") }
            .replay(1)
            .refCount()

}

data class ActivityInfo(val className: String)
