package cc.c0ldcat.chaoxing.modules;

import android.webkit.WebView;
import cc.c0ldcat.chaoxing.utils.LogUtils;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class AutoPlay implements IXposedHookLoadPackage {
    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) {
        Class<?> webViewClientClass = XposedHelpers.findClass("android.webkit.WebViewClient", loadPackageParam.classLoader);
        Class<?> webViewClass =XposedHelpers.findClass("android.webkit.WebView", loadPackageParam.classLoader);

        XposedHelpers.findAndHookMethod(webViewClientClass, "onPageFinished", webViewClass, String.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                LogUtils.i("try to click /ananas/css/play.png in 5s");

                ((WebView) param.args[0]).evaluateJavascript("javascript:" + "setTimeout(function (){$('iframe').contents().find('[src=\"/ananas/css/play.png\"]').click();}, 5000)", null);
            }
        });
    }
}
