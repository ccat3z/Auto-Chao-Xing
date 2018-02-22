package cc.c0ldcat.chaoxing.modules;

import android.content.Context;
import android.util.AttributeSet;
import android.webkit.WebView;
import cc.c0ldcat.chaoxing.utils.LogUtils;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class WebViewDebug implements IXposedHookLoadPackage {
    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) {
        XC_MethodHook hook = new EnableWebViewDebugHook();

        XposedHelpers.findAndHookConstructor(WebView.class, Context.class, hook);
        XposedHelpers.findAndHookConstructor(WebView.class, Context.class, AttributeSet.class, hook);
        XposedHelpers.findAndHookConstructor(WebView.class, Context.class, AttributeSet.class, int.class, hook);
        XposedHelpers.findAndHookConstructor(WebView.class, Context.class, AttributeSet.class, int.class, int.class, hook);
        XposedHelpers.findAndHookConstructor(WebView.class, Context.class, AttributeSet.class, int.class, boolean.class, hook);
    }

    private class EnableWebViewDebugHook extends XC_MethodHook {
        @Override
        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
            LogUtils.i("WebView " + param.thisObject.getClass().getName() + " is constructed");
            WebView.setWebContentsDebuggingEnabled(true);
        }
    }
}
