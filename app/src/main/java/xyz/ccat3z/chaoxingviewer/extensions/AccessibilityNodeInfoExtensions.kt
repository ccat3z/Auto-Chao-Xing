package xyz.ccat3z.chaoxingviewer.extensions

import android.view.accessibility.AccessibilityNodeInfo

fun AccessibilityNodeInfo.findSingleAccessibilityNodeInfoByViewId(viewId: String) =
        this.findAccessibilityNodeInfosByViewId(viewId)?.getOrNull(0)
