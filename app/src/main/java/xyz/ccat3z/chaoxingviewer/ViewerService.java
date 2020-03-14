package xyz.ccat3z.chaoxingviewer;

import android.accessibilityservice.AccessibilityService;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

public class ViewerService extends AccessibilityService {
    @Override
    protected void onServiceConnected() {
        Log.d(Common.LOG_TAG, "service started");
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
    }

    @Override
    public void onInterrupt() {

    }
}
