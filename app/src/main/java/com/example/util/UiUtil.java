package com.example.util;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;

public class UiUtil {

    private static Context mContext;
    private static Activity currentActivity;
    private static Handler mainHandler;

    public static int dip2px(int dip) {
        return dip2px((float) dip);
    }

    public static int dip2px(float dip) {
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int heightPixels = metrics.heightPixels;
        int widthPixels = metrics.widthPixels;
        float density = metrics.density;
        float heightDp = heightPixels / density;
        float widthDp = widthPixels / density;
        float smallestWidthDp = Math.min(widthDp, heightDp);
        return (int) (dip * smallestWidthDp / 360 * density + 0.5f);
    }

    public static void setContext(Context context) {
        UiUtil.mContext = context;
        mainHandler = new Handler(Looper.getMainLooper());
    }

    public static Context getContext() {
        return mContext;
    }

    public static Handler getMainHandler() {
        return mainHandler;
    }

    public static Activity getCurrentActivity() {
        return currentActivity;
    }

    public static void setCurrentActivity(Activity currentActivity) {
        UiUtil.currentActivity = currentActivity;
    }

    public static Resources getResources() {
        return mContext.getResources();
    }

    /**
     * 获取设备屏幕参数
     */
    public static DisplayMetrics getDeviceDisplayMetrics() {
        DisplayMetrics outMetrics = new DisplayMetrics();
        currentActivity.getWindowManager().getDefaultDisplay().getRealMetrics(outMetrics);
        return outMetrics;
    }

    /**
     * 获取设备状态栏高度
     */
    public static int getStatusBarHeight() {
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        return resourceId > 0 ? getResources().getDimensionPixelSize(resourceId) : 0;
    }

    /**
     * 获取设备导航栏高度
     */
    public static int getNavigationBarHeight() {
        int resourceId = getResources().getIdentifier("navigation_bar_height", "dimen", "android");
        return resourceId > 0 ? getResources().getDimensionPixelSize(resourceId) : 0;
    }
}
