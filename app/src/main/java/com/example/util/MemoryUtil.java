package com.example.util;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Debug;

public class MemoryUtil {

    public static void logMemoryClass(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        int memoryClass = activityManager.getMemoryClass();
        int largeMemoryClass = activityManager.getLargeMemoryClass();

        LogUtil.e("Memory Class: " + memoryClass + " MB");
        LogUtil.e("Large Memory Class: " + largeMemoryClass + " MB");

        int pid = android.os.Process.myPid();
        Debug.MemoryInfo[] memoryInfoArray = activityManager.getProcessMemoryInfo(new int[]{pid});
        Debug.MemoryInfo memoryInfo = memoryInfoArray[0];
        int totalPss = memoryInfo.getTotalPss();
        int totalPrivateDirty = memoryInfo.getTotalPrivateDirty();
        int totalSharedDirty = memoryInfo.getTotalSharedDirty();

        LogUtil.e("Total PSS: " + totalPss + " KB");
        LogUtil.e("Total Private Dirty: " + totalPrivateDirty + " KB");
        LogUtil.e("Total Shared Dirty: " + totalSharedDirty + " KB");
    }
}
