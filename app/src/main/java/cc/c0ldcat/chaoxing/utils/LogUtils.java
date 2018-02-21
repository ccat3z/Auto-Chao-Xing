package cc.c0ldcat.chaoxing.utils;

import android.util.Log;
import cc.c0ldcat.chaoxing.Common;

public class LogUtils {
    public static void i(String msg) {
        Log.i(Common.DEBUG_TAG, msg);
    }

    public static void d(String msg) {
        Log.d(Common.DEBUG_TAG, msg);
    }

    public static void e(String msg) {
        Log.e(Common.DEBUG_TAG, msg);
    }
}
