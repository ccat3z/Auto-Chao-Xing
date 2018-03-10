package cc.c0ldcat.chaoxing.modules;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import cc.c0ldcat.chaoxing.utils.FieldHelper;
import cc.c0ldcat.chaoxing.utils.LogUtils;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AutoNext implements IXposedHookLoadPackage {
    private Class<?> knowledgePagerActivityClass;
    private Class<?> videoPlayerActivityClass;
    private Class<?> courseKnowledgeListNewKnowledgeAdapterClass;
    private Class<?> courseKnowledgeListNewKnowledgeAdapterViewTagClass;
    private Class<?> knowledgeClass;
    private Class<?> newKnowledgeClass;
    private Class<?> knowledgeOnCliskListenerClass;
    private Class<Enum> knowledgeShowStatusEnumClass;

    private Activity currentKnowledgePagerActivity;
    private Object currentCourseKnowledgeListNewKnowledgeAdapter;

    private int wantKnowledgeIndex = -1;
    private boolean allowNextKnowledge = false;

    final static private Map<String, String> viewIdFieldMapInCourseKnowledgeListNewKnowledgeAdapterViewTag = new HashMap<String, String>() {{
        put("tv_index", "b");
        put("v_cur_item", "i");
        put("item_container", "t");
    }};

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) {
        knowledgePagerActivityClass = XposedHelpers.findClassIfExists("com.chaoxing.fanya.aphone.ui.chapter.KnowledgePagerActivity", loadPackageParam.classLoader);
        videoPlayerActivityClass = XposedHelpers.findClassIfExists("com.chaoxing.fanya.aphone.ui.video.VideoPlayerActicity", loadPackageParam.classLoader);
        courseKnowledgeListNewKnowledgeAdapterClass = XposedHelpers.findClassIfExists("com.chaoxing.fanya.aphone.ui.course.ag", loadPackageParam.classLoader);
        courseKnowledgeListNewKnowledgeAdapterViewTagClass = XposedHelpers.findClassIfExists("com.chaoxing.fanya.aphone.ui.course.ag$e", loadPackageParam.classLoader);
        knowledgeClass = XposedHelpers.findClassIfExists("com.chaoxing.fanya.common.model.Knowledge", loadPackageParam.classLoader);
        newKnowledgeClass = XposedHelpers.findClassIfExists("com.chaoxing.fanya.common.model.NewKnowledge", loadPackageParam.classLoader);
        knowledgeOnCliskListenerClass = XposedHelpers.findClassIfExists("com.chaoxing.fanya.aphone.ui.course.ai", loadPackageParam.classLoader);
        knowledgeShowStatusEnumClass = (Class<Enum>) XposedHelpers.findClassIfExists("com.chaoxing.fanya.common.model.KnowledgeShowStatus", loadPackageParam.classLoader);

        // get current knowledge pager activity
        XposedHelpers.findAndHookMethod(knowledgePagerActivityClass, "onCreate", Bundle.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                LogUtils.i("get knowledge pager activity");

                currentKnowledgePagerActivity = (Activity) param.thisObject;
            }
        });

        // finish current knowledge pager activity when video player is finished
        XposedHelpers.findAndHookMethod(videoPlayerActivityClass, "finish", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                LogUtils.i("video finished, finish knowledge pager activity now");

                currentKnowledgePagerActivity.finish();
                currentKnowledgePagerActivity = null;
                allowNextKnowledge = true;
            }
        });

        // get current course knowledge list new knowledge adapter
        XposedHelpers.findAndHookConstructor(courseKnowledgeListNewKnowledgeAdapterClass, Context.class, List.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                currentCourseKnowledgeListNewKnowledgeAdapter = param.thisObject;
            }
        });

        // start knowledge when knowledge view item init
        XposedHelpers.findAndHookMethod(courseKnowledgeListNewKnowledgeAdapterClass,
                "a", courseKnowledgeListNewKnowledgeAdapterViewTagClass, knowledgeClass, int.class, View.class, new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        Object tag = param.args[0];
                        Object knowledge = param.args[1];

                        int index = (int) param.args[2];
                        Object testKnowledge = getKnowledge(index);
                        View vCurItem = (View) FieldHelper.getPrivateObject(courseKnowledgeListNewKnowledgeAdapterViewTagClass, viewIdFieldMapInCourseKnowledgeListNewKnowledgeAdapterViewTag.get("v_cur_item"), tag);
                        TextView tvIndex = (TextView) FieldHelper.getPrivateObject(courseKnowledgeListNewKnowledgeAdapterViewTagClass, viewIdFieldMapInCourseKnowledgeListNewKnowledgeAdapterViewTag.get("tv_index"), tag);
                        LogUtils.d("setup knowledge item: " + (vCurItem.getVisibility() == View.VISIBLE) + ":" + tvIndex.getText() + ":" + index + ":" + knowledge.hashCode() + ":" + testKnowledge.hashCode());

                        if (wantKnowledgeIndex != -1 && allowNextKnowledge) {
                            allowNextKnowledge = false;
                            int targetIndex = wantKnowledgeIndex;
                            wantKnowledgeIndex = -1;
                            startKnowledge(targetIndex);
                        }
                    }
        });

        // find knowledge index and request new
        XposedHelpers.findAndHookMethod(knowledgeOnCliskListenerClass, "onClick", View.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                int currentKnowledgeIndex = (int) FieldHelper.getPrivateObject(knowledgeOnCliskListenerClass, "b", param.thisObject);
                wantKnowledgeIndex = currentKnowledgeIndex + 1;

                // skip invalid knowledge
                if (knowledgeClass.getMethod("getShowStatus").invoke(getKnowledge(wantKnowledgeIndex)) == Enum.valueOf(knowledgeShowStatusEnumClass, "LOCK")) {
                    wantKnowledgeIndex += 1;
                }

                LogUtils.i("current knowledge id: " + currentKnowledgeIndex);
            }
        });
    }

    private List<Object> getNewKnowledgeList() throws Exception {
        return  (List<Object>) FieldHelper.getPrivateObject(courseKnowledgeListNewKnowledgeAdapterClass, "d", currentCourseKnowledgeListNewKnowledgeAdapter);
    }

    private Object getNewKnowledge(int i) throws Exception {
        return getNewKnowledgeList().get(i);
    }

    private Object getKnowledge(int i) throws Exception {
        return getKnowledge(getNewKnowledge(i));
    }

    private Object getKnowledge(Object newKnowledge) throws Exception {
        return FieldHelper.getPrivateObject(newKnowledgeClass, "group", newKnowledge);
    }

    private void startKnowledge(int i) throws Exception {
        Constructor<?> constructor = knowledgeOnCliskListenerClass.getDeclaredConstructor(courseKnowledgeListNewKnowledgeAdapterClass, knowledgeClass, int.class);
        constructor.setAccessible(true);

        Object knowledge = getKnowledge(i);
        View.OnClickListener listener = (View.OnClickListener) constructor.newInstance(currentCourseKnowledgeListNewKnowledgeAdapter, knowledge, i);

        LogUtils.i("try to start knowledge: " + i + ":" + knowledge.hashCode());
        listener.onClick(null);
    }

}
