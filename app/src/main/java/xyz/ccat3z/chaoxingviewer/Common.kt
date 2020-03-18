package xyz.ccat3z.chaoxingviewer

import android.util.Log

object Common {
    const val LOG_TAG = "xyz.ccat3z.cxviewer"

    fun recordError(e: Throwable?) {
        Log.e(LOG_TAG, e.toString());
    }
}