package com.example.sotest;

import android.content.Context;

public class TextMetrics {

    static {
        System.loadLibrary("TextMetrics");
    }

    public static native void getTextWidth(String text, float textSize);

    public static native void getTextWidthWithFont(Context context, String text, float textSize, String path);
}
