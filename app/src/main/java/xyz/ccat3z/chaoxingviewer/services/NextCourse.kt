package xyz.ccat3z.chaoxingviewer.services

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import xyz.ccat3z.chaoxingviewer.extensions.findSingleAccessibilityNodeInfoByViewId
import xyz.ccat3z.chaoxingviewer.utils.Logger
import java.lang.Exception
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

class NextCourse(
        private val onScreenActivity: OnScreenActivity,
        private val accessibilityEvents: Observable<AccessibilityEvent>,
        private val accessibilityService: AccessibilityService
) {
    private val logger = Logger("NextCourse")
    private var inCourseActivity: Boolean = false

    private val currentRoot
        get() = accessibilityService.rootInActiveWindow ?: throw Exception("cannot get root node")

    var justFinished: String? = null

    init {
        onScreenActivity.currentActivity
                .distinctUntilChanged()
                .map { it.className == "com.chaoxing.fanya.aphone.ui.course.StudentCourseActivity" }
                .doOnNext { inCourseActivity = it }
                .filter { it }
                .doOnNext { findAndClickNextCourse() }
                .subscribe({}, { logger.e(it) })
    }

    private fun findAndClickNextCourse() {
        val lastCourse = justFinished ?: return

        accessibilityEvents
                .filter { inCourseActivity }
                .filter { it.eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED }
                .throttleWithTimeout(1, TimeUnit.SECONDS, Schedulers.newThread()) // no change in 1s
                .observeOn(AndroidSchedulers.mainThread())
                .map {
                    val course = findNextCourse(lastCourse)
                    if (course == null) {
                        logger.d("find $lastCourse in next page")
                        findKnowledgeRecycleView().performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)
                        false
                    } else {
                        logger.d("click next course: ${course.name}")
                        course.node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                        true
                    }
                }
                .takeUntil { it } // stop detect after course was find
                .timeout(3, TimeUnit.SECONDS) // no response in 3s (no more course)
                .onErrorComplete {
                    if (it !is TimeoutException) return@onErrorComplete false

                    logger.e("cannot find course next to $lastCourse")
                    true
                }
                .doOnError { logger.e(it) }
                .onErrorComplete()
                .doOnComplete { justFinished = null }
                .subscribe()
    }

    data class CourseNode(val name: String, val node: AccessibilityNodeInfo) {
        constructor(node: AccessibilityNodeInfo) : this(
                {
                    val index = node.getChild(0)?.text?.toString()?.trim() ?: "0"
                    val title = node.getChild(2)?.text?.toString()?.trim() ?: "unknown"
                    "$index $title"
                }(),
                node.parent
        )
    }
    private fun findNextCourse(nameOfLastCourse: String): CourseNode? {
        val nodes = findCourseNodes().map { CourseNode(it) }
        nodes.forEachIndexed {
            i, course ->
            logger.d("compare $nameOfLastCourse with ${course.name}")
            if (course.name == nameOfLastCourse && i != nodes.size - 1) {
                return nodes[i + 1]
            }
        }
        return null
    }

    private fun findCourseNodes(): List<AccessibilityNodeInfo> {
        val coursePage = currentRoot.findSingleAccessibilityNodeInfoByViewId("com.chaoxing.mobile:id/vp_course") ?: throw Exception("cannot find vp_course")
        return coursePage.findAccessibilityNodeInfosByViewId("com.chaoxing.mobile:id/sub_node")
    }

    private fun findKnowledgeRecycleView(): AccessibilityNodeInfo {
        return currentRoot.findSingleAccessibilityNodeInfoByViewId("com.chaoxing.mobile:id/rv_knowledge") ?: throw Exception("cannot find rv_knowledge")
    }
}
