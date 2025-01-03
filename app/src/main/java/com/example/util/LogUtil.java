package com.example.util;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * 日志输出工具类
 *
 * @author laiweisheng
 * @date 2024/12/16
 */
public class LogUtil {

    private static final String TAG = "x7demo";

    private LogUtil() {

    }

    public static void d(String msg) {
        android.util.Log.d(TAG, msg);
    }

    public static void e(String msg) {
        android.util.Log.e(TAG, msg);
    }

    public static void i(String msg) {
        android.util.Log.i(TAG, msg);
    }

    public static void w(String msg) {
        android.util.Log.w(TAG, msg);
    }

    public static void v(String msg) {
        android.util.Log.v(TAG, msg);
    }

    public static String getErrorTrace(Throwable t) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        t.printStackTrace(writer);
        StringBuffer buffer = stringWriter.getBuffer();
        return buffer.toString();
    }
}
