package com.example.xiao7demo;

import android.app.Activity;
import android.app.Application;
import android.app.ListActivity;

import com.wanjian.sak.SAK;

import java.util.ArrayList;
import java.util.List;

public class MyApplication extends Application {

    private final List<Activity> activities = new ArrayList<>();

    public static class SingleApplication {
        public static final MyApplication INSTANCE = new MyApplication();
    }

    public static MyApplication getInstance() {
        return SingleApplication.INSTANCE;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    // 添加 Activity 到容器中
    public void add(Activity activity) {
        activities.add(activity);
    }

    // Activity 销毁时移除
    public void remove(Activity activity) {
        activities.remove(activity);
    }

    // 遍历所有 Activity 并 finish
    public void exit() {
        for (Activity activity : activities) {
            activity.finish();
        }
        activities.clear();
    }

}
