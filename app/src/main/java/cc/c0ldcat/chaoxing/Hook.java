package cc.c0ldcat.chaoxing;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import java.util.HashMap;
import java.util.Map;

public class Hook implements IXposedHookLoadPackage {
    final static private Map<String, String> viewIdFieldMap = new HashMap<String, String>() {{
        put("rl_video_test", "aA");
        put("lv_test", "aB");
        put("check_answer", "aE");
        put("check_right", "aF");
        put("tv_right_answer", "aG");
    }};

    final static private Map<String, String> otherFieldMap = new HashMap<String, String>() {{
        put(VIDEO_PLAYER_TEST_CHECK_BOX_WRAP_CHECK_BOX, "a");
    }};

    final static private String VIDEO_PLAYER_TEST_CHECK_BOX_WRAP_CHECK_BOX = "VIDEO_PLAYER_TEST_CHECK_BOX_WRAP_CHECK_BOX";

    private RelativeLayout videoTestLayout;
    private ListView testListView;
    private Button checkRightButton;
    private Button checkAnswerButton;
    private TextView rightAnswerTextView;

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) {
        if (loadPackageParam.packageName.equals("com.chaoxing.mobile")) {
            XposedBridge.log("in chaoxing app");

            // fing class
            final Class<?> videoPlayerActivityClass = XposedHelpers.findClassIfExists("com.chaoxing.fanya.aphone.ui.video.VideoPlayerActicity", loadPackageParam.classLoader);
            final Class<?> videoPlayerCheckingThreadClass = XposedHelpers.findClassIfExists("com.chaoxing.fanya.aphone.ui.video.n", loadPackageParam.classLoader);
            final Class<?> videoPlayerTestCheckBoxWrapClass = XposedHelpers.findClass("com.chaoxing.fanya.aphone.ui.video.VideoPlayerActicity$j$a", loadPackageParam.classLoader);

            // get view object
            XposedHelpers.findAndHookMethod(videoPlayerActivityClass, "onCreate", Bundle.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    videoTestLayout = (RelativeLayout) FieldHelper.getPrivateObject(videoPlayerActivityClass, viewIdFieldMap.get("rl_video_test"), param.thisObject);
                    testListView = (ListView) FieldHelper.getPrivateObject(videoPlayerActivityClass, viewIdFieldMap.get("lv_test"), param.thisObject);
                    checkRightButton = (Button) FieldHelper.getPrivateObject(videoPlayerActivityClass, viewIdFieldMap.get("check_answer"), param.thisObject);
                    checkAnswerButton = (Button) FieldHelper.getPrivateObject(videoPlayerActivityClass, viewIdFieldMap.get("check_right"), param.thisObject);
                    rightAnswerTextView = (TextView) FieldHelper.getPrivateObject(videoPlayerActivityClass, viewIdFieldMap.get("tv_right_answer"), param.thisObject);
                }
            });

            // hook checking thread
            XposedHelpers.findAndHookMethod(videoPlayerCheckingThreadClass, "run", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    Log.i(Common.DEBUG_TAG, "video 1000ms checking thread on run");

                    // if on test
                    if (videoTestLayout != null && videoTestLayout.getVisibility() == View.VISIBLE) {
                        Log.i(Common.DEBUG_TAG, "videoTest is visible " + testListView.getCount());

                        for (int i = 0; i < testListView.getCount(); i++) {
                            try {
                                Map<String, String> item = (Map) testListView.getItemAtPosition(i);

                                Object itemViewCheckBoxTag = Utils.getViewByPosition(i, testListView).getTag();
                                CheckBox itemViewCheckBox = (CheckBox) FieldHelper.getPrivateObject(videoPlayerTestCheckBoxWrapClass, otherFieldMap.get(VIDEO_PLAYER_TEST_CHECK_BOX_WRAP_CHECK_BOX), itemViewCheckBoxTag);

                                Log.i(Common.DEBUG_TAG, item.get("questionType") + item.get("name") + item.get("description") + itemViewCheckBox.isChecked() + isRight());
                            } catch (Exception e) {
                                Log.d(Common.DEBUG_TAG, e.getMessage());
                            }
                        }
                    }
                }
            });
        }
    }

    private boolean isRight() {
        return rightAnswerTextView.getVisibility() == View.VISIBLE;
    }
}
