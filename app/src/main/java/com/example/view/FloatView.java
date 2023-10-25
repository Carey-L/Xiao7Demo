package com.example.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.widget.FrameLayout;

import com.example.R;

/**
 * 悬浮窗布局
 *
 * @author laiweisheng
 * @date 2023/10/25
 */
public class FloatView extends FrameLayout {

    private int rawX = -1;
    private int rawY = -1;

    public FloatView(Context context) {
        super(context);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.floating_window_layout, this);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                rawX = (int) ev.getRawX();
                rawY = (int) ev.getRawY();
                break;
            case MotionEvent.ACTION_MOVE:
                int moveX = (int) ev.getRawX();
                int moveY = (int) ev.getRawY();
                // 拦截滑动事件
                if (Math.abs(moveX - rawX) > 5 || Math.abs(moveY - rawY) > 5) {
                    return true;
                }
                break;
            case MotionEvent.ACTION_UP:
                rawX = -1;
                rawY = -1;
                break;
            default:
                break;
        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }
}
