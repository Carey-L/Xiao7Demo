package com.example.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

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

    private View rootView;
    private ImageView backgroundIv;

    private TouchEventListener listener;

    public FloatView(Context context) {
        super(context);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.float_window_layout, this);
        rootView = findViewById(R.id.root_view);
        backgroundIv = findViewById(R.id.background_iv);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                rawX = (int) ev.getRawX();
                rawY = (int) ev.getRawY();
                if (listener != null) {
                    listener.onDown();
                }
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

    public ImageView getBackgroundIv() {
        return backgroundIv;
    }

    @Override
    public View getRootView() {
        return rootView;
    }

    public void setListener(TouchEventListener listener) {
        this.listener = listener;
    }

    public interface TouchEventListener {
        void onDown();
    }
}
