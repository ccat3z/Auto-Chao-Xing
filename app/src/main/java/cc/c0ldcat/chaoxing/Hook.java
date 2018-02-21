package cc.c0ldcat.chaoxing;

import cc.c0ldcat.chaoxing.modules.VideoTest;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class Hook implements IXposedHookLoadPackage {
    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) {
        new VideoTest(loadPackageParam);
    }
}
