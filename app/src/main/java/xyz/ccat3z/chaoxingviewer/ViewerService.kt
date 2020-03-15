package xyz.ccat3z.chaoxingviewer

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.accessibility.AccessibilityEvent

class ViewerService : AccessibilityService() {
    override fun onServiceConnected() {
        Log.d(Common.LOG_TAG, "service started")
    }

    override fun onAccessibilityEvent(accessibilityEvent: AccessibilityEvent) {
    }

    override fun onInterrupt() {}
}