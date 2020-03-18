package xyz.ccat3z.chaoxingviewer.services

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Toast
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import xyz.ccat3z.chaoxingviewer.extensions.OnNextException
import xyz.ccat3z.chaoxingviewer.extensions.findFirst
import xyz.ccat3z.chaoxingviewer.extensions.findSingleAccessibilityNodeInfoByViewId
import xyz.ccat3z.chaoxingviewer.extensions.idle
import xyz.ccat3z.chaoxingviewer.utils.Logger
import java.util.concurrent.TimeUnit

class AutoPlay(
        private val onScreenActivity: OnScreenActivity,
        private val accessibilityService: AccessibilityService,
        accessibilityEvents: Observable<AccessibilityEvent>,
        private val nextCourse: NextCourse
) {
    private val inChapterDetailActivity: Boolean
        get() = onScreenActivity.currentActivityInfo.className == "com.chaoxing.fanya.aphone.ui.chapter.detail.ui.ChapterDetailActivity"
    private val inStudentCourseActivity: Boolean
        get() = onScreenActivity.currentActivityInfo.className == "com.chaoxing.fanya.aphone.ui.course.StudentCourseActivity"
    private val rootNode
        get() = accessibilityService.rootInActiveWindow!!
    private val chapterEvents = accessibilityEvents.filter { inChapterDetailActivity }
    private var currentChapterHandler: Disposable? = null
    private val nonChapterName = "*"
    private val logger = Logger("AutoPlay")

    init {
        accessibilityEvents
                .filter { inChapterDetailActivity || inStudentCourseActivity }
                .map { rootNode.findSingleAccessibilityNodeInfoByViewId("com.chaoxing.mobile:id/tv_chapter_name")?.text?.toString() ?: nonChapterName }
                .filter {
                    it != nonChapterName || // chapter name is invalid
                    inStudentCourseActivity
                }
                .distinctUntilChanged()
                .doOnNext(this::onNewChapter)
                .doOnError { logger.e(it) }
                .retry()
                .subscribe()
    }

    private fun onNewChapter(chapter: String) {
        currentChapterHandler?.dispose()

        if (chapter == nonChapterName) {
            logger.d("unselected chapter")
            return
        }

        logger.d("detected new chapter: $chapter")
        currentChapterHandler = chapterEvents
                .filter { it.eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED }
                .throttleWithTimeout(3, TimeUnit.SECONDS) // screen stay over 3s
                .filter { inChapterDetailActivity } // still in chapter detail
                .map { isChapterOnScreenFinished() }
                .distinctUntilChanged()
                .takeUntil { it } // completed on finished
                .filter { !it } // only unfinished
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ playVideo() }, { logger.e(it) }, {
                    onChapterFinished()
                    nextCourse.justFinished = chapter
                })
    }

    private fun isChapterOnScreenFinished(): Boolean {
        val missionFlag = rootNode.findFirst { it.text == "任务点" }
        val finishedFlag = rootNode.findFirst { it.text == "任务点已完成" }

        return when {
            finishedFlag != null -> true
            missionFlag != null -> false
            else -> true
        }
    }

    private fun playVideo() {
        val playNode = rootNode.findFirst { it.text == "play" }

        if (playNode == null || !playNode.isClickable) {
            Toast.makeText(accessibilityService, "找不到播放按键!", Toast.LENGTH_LONG).show()
            logger.e("cannot find play button on chapter activity")
            exitChapterIfNoAction()
            return
        }

        Toast.makeText(accessibilityService, "3秒后自动播放", Toast.LENGTH_LONG).show()
        logger.d("play in 3s")
        chapterEvents
                .idle(3, TimeUnit.SECONDS)
                .doOnComplete { playNode.performAction(AccessibilityNodeInfo.ACTION_CLICK) }
                .doOnError {
                    if (it !is OnNextException) return@doOnError
                    Toast.makeText(accessibilityService, "已取消自动播放", Toast.LENGTH_LONG).show()
                    logger.d("cancel auto play")
                }
                .onErrorComplete { it is OnNextException }
                .subscribe({}, { logger.e(it) })
    }

    private fun onChapterFinished() {
        Toast.makeText(accessibilityService, "任务已完成", Toast.LENGTH_LONG).show()
        exitChapterIfNoAction()
    }

    private fun exitChapterIfNoAction() {
        val backNode = rootNode.findSingleAccessibilityNodeInfoByViewId("com.chaoxing.mobile:id/iv_chapter_back")
        if (backNode == null || !backNode.isClickable) {
            Toast.makeText(accessibilityService, "找不到返回按键!", Toast.LENGTH_LONG).show()
            logger.e("cannot find back button on chapter activity")
            return
        }

        Toast.makeText(accessibilityService, "3秒后退出", Toast.LENGTH_LONG).show()
        chapterEvents
                .idle(3, TimeUnit.SECONDS)
                .doOnComplete {
                    logger.d("exit chapter")
                    backNode.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                }
                .doOnError {
                    if (it !is OnNextException) return@doOnError
                    Toast.makeText(accessibilityService, "已取消退出", Toast.LENGTH_LONG).show()
                    logger.d("user canceled")
                }
                .onErrorComplete { it is OnNextException }
                .subscribe({}, { logger.e(it) })
    }
}