package cc.c0ldcat.chaoxing.modules;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;
import cc.c0ldcat.chaoxing.Common;
import cc.c0ldcat.chaoxing.utils.CommonUtils;
import cc.c0ldcat.chaoxing.utils.FieldHelper;
import cc.c0ldcat.chaoxing.utils.LogUtils;
import cc.c0ldcat.chaoxing.utils.ViewUtils;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import java.util.*;

public class VideoTest implements IXposedHookLoadPackage, Iterable<VideoTest.Answer> {
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

    private Class<?> videoPlayerActivityClass;
    private Class<?> videoPlayerCheckingThreadClass;
    private Class<?> videoPlayerTestCheckBoxWrapClass;
    private Class<?> videoPlayerTestButtonListenerClass;

    private List<List<Boolean>> possibleAnswers;

    public VideoTest(XC_LoadPackage.LoadPackageParam loadPackageParam) {
        handleLoadPackage(loadPackageParam);
    }

    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) {
        if (loadPackageParam.packageName.equals("com.chaoxing.mobile")) {
            XposedBridge.log("in chaoxing app");

            final VideoTest videoTest = this;

            // find class
            videoPlayerActivityClass = XposedHelpers.findClassIfExists("com.chaoxing.fanya.aphone.ui.video.VideoPlayerActicity", loadPackageParam.classLoader);
            videoPlayerCheckingThreadClass = XposedHelpers.findClassIfExists("com.chaoxing.fanya.aphone.ui.video.n", loadPackageParam.classLoader);
            videoPlayerTestCheckBoxWrapClass = XposedHelpers.findClass("com.chaoxing.fanya.aphone.ui.video.VideoPlayerActicity$j$a", loadPackageParam.classLoader);
            videoPlayerTestButtonListenerClass = XposedHelpers.findClass("com.chaoxing.fanya.aphone.ui.video.VideoPlayerActicity$a", loadPackageParam.classLoader);

            // get view object
            XposedHelpers.findAndHookMethod(videoPlayerActivityClass, "onCreate", Bundle.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    videoTestLayout = (RelativeLayout) FieldHelper.getPrivateObject(videoPlayerActivityClass, viewIdFieldMap.get("rl_video_test"), param.thisObject);
                    testListView = (ListView) FieldHelper.getPrivateObject(videoPlayerActivityClass, viewIdFieldMap.get("lv_test"), param.thisObject);
                    checkRightButton = (Button) FieldHelper.getPrivateObject(videoPlayerActivityClass, viewIdFieldMap.get("check_answer"), param.thisObject);
                    checkAnswerButton = (Button) FieldHelper.getPrivateObject(videoPlayerActivityClass, viewIdFieldMap.get("check_right"), param.thisObject);

                    possibleAnswers = null;
                }
            });

            // hook on check answer button is visible
            XposedHelpers.findAndHookMethod(videoPlayerTestButtonListenerClass, "onClick", View.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    if (checkAnswerButton.getVisibility() == View.VISIBLE) {
                        possibleAnswers = null;
                        checkAnswerButton.performClick();
                    }
                }
            });

            // hook checking thread
            XposedHelpers.findAndHookMethod(videoPlayerCheckingThreadClass, "run", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    Log.i(Common.DEBUG_TAG, "video 1000ms checking thread on run");

                    // if on test
                    if (videoTestLayout != null && videoTestLayout.getVisibility() == View.VISIBLE) {
                        Log.i(Common.DEBUG_TAG, "videoTest is visible " + videoTest.getSize());

                        for (Answer answer: videoTest) {
                            LogUtils.i("NAME: " + answer.getName()
                                    + ", DESC: " + answer.getDescription()
                                    + ", CHECKED: " + answer.isChecked());
                        }

                        LogUtils.i("guess" + videoTest.guessAnswer());
                    }
                }
            });
        }
    }

    public Answer getAnswer(int i) throws Exception {
        return new Answer(i);
    }

    public int getSize() {
        return testListView.getCount() - 1;
    }

    public List<Boolean> guessAnswer() {
        if (checkRightButton.getVisibility() == View.VISIBLE) {
            int size = getSize();

            if (possibleAnswers == null) {
                possibleAnswers = CommonUtils.cartesianProduct(Arrays.asList(true, false), size);
            }

            List<Boolean> possibleAnswer = possibleAnswers.remove(0);
            for (int i = 0; i < size; i++) {
                try {
                    getAnswer(i).setChecked(possibleAnswer.get(i));
                    if (possibleAnswer.get(i) && !isMultiChoice()) break; // WHY??
                } catch (Exception e) {
                    LogUtils.e(CommonUtils.exceptionStacktraceToString(e));
                }
            }
            checkRightButton.performClick();

            return possibleAnswer;
        } else {
            return new ArrayList<>();
        }
    }

    public boolean isMultiChoice() {
        return "多选题".equals(((Map) testListView.getItemAtPosition(1)).get("questionType"));
    }

    @Override
    public Iterator<Answer> iterator() {
        return new AnswerIterator();
    }

    class Answer {
        private int realIndex;
        private Map<String, String> item;
        private View view;
        private CheckBox checkBox;

        Answer(int index) throws Exception {
            realIndex = index + 1; // first one is question's description
            item = (Map) testListView.getItemAtPosition(realIndex);
            view = ViewUtils.getViewByPosition(realIndex, testListView);
            checkBox = (CheckBox) FieldHelper.getPrivateObject(videoPlayerTestCheckBoxWrapClass, otherFieldMap.get(VIDEO_PLAYER_TEST_CHECK_BOX_WRAP_CHECK_BOX), view.getTag());
        }

        public String getName() {
            return item.get("name");
        }

        public String getDescription() {
            return item.get("description");
        }

        public boolean isChecked() {
            return checkBox.isChecked();
        }

        public void setChecked(boolean checked) {
            if (isChecked() != checked) {
                testListView.performItemClick(view, realIndex, 0);
                LogUtils.d("click " + realIndex);
            }
        }
    }

    class AnswerIterator implements Iterator<Answer> {
        private int index = 0;

        @Override
        public boolean hasNext() {
            return index < getSize();
        }

        @Override
        public Answer next() {
            try {
                return getAnswer(index++);
            } catch (Exception e) {
                LogUtils.e(CommonUtils.exceptionStacktraceToString(e));
                return null;
            }
        }
    }
}
