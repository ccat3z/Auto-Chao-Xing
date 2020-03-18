package xyz.ccat3z.chaoxingviewer.extensions

import android.view.accessibility.AccessibilityNodeInfo

fun AccessibilityNodeInfo.findSingleAccessibilityNodeInfoByViewId(viewId: String) =
        this.findAccessibilityNodeInfosByViewId(viewId)?.getOrNull(0)

fun AccessibilityNodeInfo.findFirst(condition: (node: AccessibilityNodeInfo) -> Boolean): AccessibilityNodeInfo? {
    if (condition(this)) return this

    for (i in 0 until this.childCount) {
        val res = this.getChild(i)?.findFirst(condition)
        if (res != null) return res
    }

    return null
}
