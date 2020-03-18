package xyz.ccat3z.chaoxingviewer.services

import android.accessibilityservice.AccessibilityService
import android.util.Log
import io.reactivex.rxjava3.core.Observable
import xyz.ccat3z.chaoxingviewer.Common

class AutoPlay(onScreenActivity: OnScreenActivity, accessibilityService: AccessibilityService) {
    val activities: Observable<ActivityInfo>

    init {
        activities = onScreenActivity.currentActivity

        activities.subscribe {
            Log.d(Common.LOG_TAG, accessibilityService.windows.toString())
        }
    }
}