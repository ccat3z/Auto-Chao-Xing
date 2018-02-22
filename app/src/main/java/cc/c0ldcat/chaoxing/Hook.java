package cc.c0ldcat.chaoxing;

import cc.c0ldcat.chaoxing.modules.VideoTest;
import cc.c0ldcat.chaoxing.utils.LogUtils;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class Hook implements IXposedHookLoadPackage {
    private static Class<?>[] moduleClasses = new Class<?>[] {VideoTest.class};

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        if (loadPackageParam.packageName.equals("com.chaoxing.mobile")) {
            LogUtils.i("in chaoxing app");

            for (Class<?> c : moduleClasses) {
                if (IXposedHookLoadPackage.class.isAssignableFrom(c)) {
                    try {
                        LogUtils.i("load module " + c.getName());

                        IXposedHookLoadPackage m = (IXposedHookLoadPackage) c.newInstance();
                        m.handleLoadPackage(loadPackageParam);
                    } catch (Throwable e) {
                        LogUtils.e(e);
                    }
                }
            }
        }
    }
}
