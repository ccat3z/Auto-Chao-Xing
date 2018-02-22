package cc.c0ldcat.chaoxing.modules;

import android.webkit.WebView;
import cc.c0ldcat.chaoxing.utils.LogUtils;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import java.util.HashMap;
import java.util.Map;

public class AutoPlay implements IXposedHookLoadPackage {
    private Class<?> cardWebViewClass;
    private Class<?> loadingListenerInterfaceClass;
    private Class<?> cardContentFragmentLoadingListenerClass;

    private Map<Object, WebView> listenerToWebViewClassMap = new HashMap<>();

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) {
        cardWebViewClass = XposedHelpers.findClassIfExists("com.chaoxing.fanya.aphone.view.CardWebView", loadPackageParam.classLoader);
        loadingListenerInterfaceClass = XposedHelpers.findClassIfExists("com.chaoxing.fanya.aphone.view.CardWebView$b", loadPackageParam.classLoader);
        cardContentFragmentLoadingListenerClass = XposedHelpers.findClassIfExists("com.chaoxing.fanya.aphone.ui.chapter.CardContentFragment$1", loadPackageParam.classLoader);

        XposedHelpers.findAndHookMethod(cardWebViewClass, "setLoadingListener", loadingListenerInterfaceClass, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                listenerToWebViewClassMap.put(param.args[0], (WebView) param.thisObject);
            }
        });

        XposedHelpers.findAndHookMethod(cardContentFragmentLoadingListenerClass, "onload", boolean.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if (! (boolean) param.args[0]) {
                    LogUtils.i("try to click video in 5s");

                    // TODO: need a more stable JS
                    listenerToWebViewClassMap.get(param.thisObject).loadUrl("javascript:" + "setTimeout(function (){$('iframe').contents().find('#ext-gen1040').click();}, 5000)");
                }
            }
        });
    }
}
