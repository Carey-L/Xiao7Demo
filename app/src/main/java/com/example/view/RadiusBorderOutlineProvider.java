package com.example.view;

import android.graphics.Outline;
import android.graphics.Rect;
import android.view.View;
import android.view.ViewOutlineProvider;

/**
 * 边框圆角裁剪
 *
 * @author laiweisheng
 * @date 2023/11/8
 */
public class RadiusBorderOutlineProvider extends ViewOutlineProvider {
    private float mRadius;
    private int width;
    private int height;

    public RadiusBorderOutlineProvider(float radius) {
        this.mRadius = radius;
    }

    @Override
    public void getOutline(View view, Outline outline) {
        Rect rect = new Rect();
        view.getGlobalVisibleRect(rect);
        int leftMargin = 0;
        int topMargin = 0;
        if (view.getWidth() > width) {
            width = view.getWidth();
        }
        if (view.getHeight() > height) {
            height = view.getHeight();
        }
        Rect selfRect = new Rect(leftMargin, topMargin,
                view.getWidth(), view.getHeight());
        outline.setRoundRect(selfRect, mRadius);
    }
}