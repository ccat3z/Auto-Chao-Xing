package xyz.ccat3z.chaoxingviewer.services

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Toast
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import xyz.ccat3z.chaoxingviewer.extensions.findSingleAccessibilityNodeInfoByViewId
import xyz.ccat3z.chaoxingviewer.utils.Logger
import java.lang.Exception
import java.util.concurrent.TimeUnit

class AutoAnswer(
        private val onScreenActivity: OnScreenActivity,
        private val accessibilityService: AccessibilityService,
        private val accessibilityEvent: Observable<AccessibilityEvent>
) {
    private val logger = Logger("AutoAnswer")
    private val rootNode
        get() = accessibilityService.rootInActiveWindow
    private val inPlayerActivity
        get() = onScreenActivity.currentActivityInfo.className == "com.chaoxing.mobile.player.course.CoursePlayerActivity"
    private val stateOfTestView = accessibilityEvent
            .filter { inPlayerActivity }
            .filter { it.eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED }
            .map { it.source?.findSingleAccessibilityNodeInfoByViewId("com.chaoxing.mobile:id/rv_test") != null }
            .distinctUntilChanged()
            .publish()
            .autoConnect()
    private var answerHandler: Disposable? = null

    init {
        stateOfTestView
                .filter { it }
                .subscribe({ onShowTestView() }, { logger.e(it) })
    }

    private fun onShowTestView() {
        answerHandler?.dispose()
        answerHandler = null

        val choices = findTestNode().let {
            val answerNodes = arrayListOf<AccessibilityNodeInfo>()
            for (i in 1 until it.childCount) {
                answerNodes.add(it.getChild(i))
            }
            answerNodes
        }

        logger.d("detect new test")
        Toast.makeText(accessibilityService, "发现测试, 假装读题", Toast.LENGTH_LONG).show()

        answerHandler = accessibilityEvent
                .filter { inPlayerActivity }
                .filter { it.eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED }
                .map { checkAnswerResult() }
                .startWith(Observable.just(checkAnswerResult()))
                .throttleLast(3, TimeUnit.SECONDS, Schedulers.newThread())
                .takeWhile { !it }
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext {
                    val choice = choices.removeAt(0)
                    val choiceText = choice.getChild(0)?.text ?: throw Exception("cannot find answer text")
                    logger.d("guess $choiceText")
                    Toast.makeText(accessibilityService, "猜测 '$choiceText'", Toast.LENGTH_LONG).show()
                    choice.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    findCheckButton().performAction(AccessibilityNodeInfo.ACTION_CLICK)
                }
                .subscribe({}, { logger.e(it) }) {
                    findNextButton().performAction(AccessibilityNodeInfo.ACTION_CLICK)
                }
    }

    private fun findTestNode(): AccessibilityNodeInfo {
        return rootNode?.findSingleAccessibilityNodeInfoByViewId("com.chaoxing.mobile:id/rv_test") ?: throw Exception("cannot find rv_test")
    }

    private fun findCheckButton(): AccessibilityNodeInfo {
        return rootNode?.findSingleAccessibilityNodeInfoByViewId("com.chaoxing.mobile:id/btn_check_answer") ?: throw Exception("cannot find btn_check_answer")
    }

    private fun findNextButton(): AccessibilityNodeInfo {
        return rootNode?.findSingleAccessibilityNodeInfoByViewId("com.chaoxing.mobile:id/btn_next") ?: throw Exception("cannot find btn_next")
    }

    private fun checkAnswerResult(): Boolean {
        val result = rootNode
                ?.findSingleAccessibilityNodeInfoByViewId("com.chaoxing.mobile:id/tv_answer")
                ?.text?.toString() ?: return false
        logger.d(result)
        return result == "回答正确"
    }
}