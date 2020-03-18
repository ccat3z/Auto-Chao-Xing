package xyz.ccat3z.chaoxingviewer

import xyz.ccat3z.chaoxingviewer.services.AutoAnswer
import xyz.ccat3z.chaoxingviewer.services.AutoPlay
import xyz.ccat3z.chaoxingviewer.services.ChaoXingCourse
import xyz.ccat3z.chaoxingviewer.services.OnScreenActivity

/**
 * a simple DI solution
 */
class ServicesContainer(val viewerService: ViewerService) {
    val onScreenActivity by lazy { OnScreenActivity(viewerService.accessibilityEvents) }
    val course by lazy { ChaoXingCourse(onScreenActivity, viewerService.accessibilityEvents, viewerService) }
    val autoPlay by lazy { AutoPlay(onScreenActivity, viewerService, viewerService.accessibilityEvents) }
    val autoAnswer by lazy { AutoAnswer(onScreenActivity, viewerService, viewerService.accessibilityEvents) }
}