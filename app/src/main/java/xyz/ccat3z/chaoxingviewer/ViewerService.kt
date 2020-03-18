package xyz.ccat3z.chaoxingviewer

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import io.reactivex.rxjava3.subjects.PublishSubject

class ViewerService : AccessibilityService() {
    val accessibilityEvents = PublishSubject.create<AccessibilityEvent>()!!

    override fun onServiceConnected() {
        Log.d("Viewer", "service started")
        ServicesContainer(this).apply {
            autoPlay
            autoAnswer
            nextCourse
        }
    }

    override fun onAccessibilityEvent(accessibilityEvent: AccessibilityEvent) {
        val packageName = accessibilityEvent.packageName
        if (packageName == null || packageName != "com.chaoxing.mobile") return
        Log.d("Viewer", "received event: $accessibilityEvent")
        accessibilityEvents.onNext(accessibilityEvent)
    }

    override fun onDestroy() {
        super.onDestroy()
        accessibilityEvents.onComplete()

        Log.d("Viewer", "service stopped")
    }

    override fun onInterrupt() {}
}