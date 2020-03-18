package xyz.ccat3z.chaoxingviewer.services

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.subjects.BehaviorSubject
import xyz.ccat3z.chaoxingviewer.Common
import xyz.ccat3z.chaoxingviewer.extensions.findSingleAccessibilityNodeInfoByViewId
import java.lang.Exception
import java.util.concurrent.TimeUnit

class ChaoXingCourse(
        onScreenActivity: OnScreenActivity,
        accessibilityEvents: Observable<AccessibilityEvent>,
        private val accessibilityService: AccessibilityService
) {
    val latestCourse = BehaviorSubject.createDefault(CourseInfo())!!
    private var inCourseActivity = false

    private val contextChangeEvents: Observable<AccessibilityEvent> = accessibilityEvents
            .filter { inCourseActivity }
            .filter { it.eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED }
            .publish()
            .autoConnect()
            .doOnComplete { Log.d(Common.LOG_TAG, "done") }
    private val currentActivity = onScreenActivity.currentActivity
    private val currentRoot
        get() = accessibilityService.rootInActiveWindow ?: throw Exception("cannot get root node")

    private val coursePage
        get() = currentRoot.findSingleAccessibilityNodeInfoByViewId("com.chaoxing.mobile:id/vp_course")
    private val courseNodes
        get() = coursePage?.findAccessibilityNodeInfosByViewId("com.chaoxing.mobile:id/sub_node")
    private val knowledgeRecycleView
        get() = currentRoot.findSingleAccessibilityNodeInfoByViewId("com.chaoxing.mobile:id/rv_knowledge")

    init {
        currentActivity.subscribe { inCourseActivity = it.className == "com.chaoxing.fanya.aphone.ui.course.StudentCourseActivity" }

        contextChangeEvents
                .map {
                    accessibilityService.rootInActiveWindow
                            ?.findSingleAccessibilityNodeInfoByViewId("com.chaoxing.mobile:id/toolbar_tv_title")
                            ?.text?.toString() ?: "unknown"
                }
                .filter { it != "unknown" }
                .distinctUntilChanged()
                .subscribe(this::onNewCourse)

        contextChangeEvents
                .throttleWithTimeout(1, TimeUnit.SECONDS, Schedulers.newThread())
                .filter { courseNodes != null && knowledgeRecycleView != null }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { onContentChange() }

        contextChangeEvents
                .filter {
                    it.className == "android.support.v7.widget.RecyclerView" &&
                            it.source?.findAccessibilityNodeInfosByViewId("com.chaoxing.mobile:id/rv_knowledge") != null
                }
                .subscribe {
//                    Log.d(Common.LOG_TAG, it.toString())
                }
    }

    private fun onNewCourse(name: String) {
        latestCourse.onNext(CourseInfo(name))
    }

    private fun onContentChange() {
        courseNodes!!.map {
            node ->
            val index = node.getChild(0)?.text?.toString() ?: "0"
            val title = node.getChild(2)?.text?.toString() ?: "unknown"
            KnowledgeInfo(index, title)
        }.apply {
            latestCourse.onNext(latestCourse.value.copy(knownledges = this))
        }
        knowledgeRecycleView!!.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)
    }
}

data class CourseInfo(
        var name: String = "unknown",
        var card: String = "unknown",
        var knownledges: List<KnowledgeInfo> = listOf()
)

data class KnowledgeInfo(
        var id: String,
        var name: String
)
