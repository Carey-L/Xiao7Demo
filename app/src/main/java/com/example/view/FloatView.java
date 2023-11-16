package com.example.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.example.R;

import java.lang.ref.WeakReference;

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
    private ImageView closeIv;

    private boolean closeIvVisible = false;

    private TouchEventListener listener;

    private Runnable dismissCloseIvRunnable;

    public FloatView(Context context) {
        super(context);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.float_window_layout, this);
        rootView = findViewById(R.id.root_view);
        backgroundIv = findViewById(R.id.background_iv);
        closeIv = findViewById(R.id.close_iv);
        
        closeIv.setOnClickListener(v -> {
            if (listener != null) {
                listener.onClose();
            }
        });
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
        if (dismissCloseIvRunnable == null) {
            dismissCloseIvRunnable = new DismissCloseIvRunnable(this);
        } else {
            removeCallbacks(dismissCloseIvRunnable);
        }
        closeIvVisible = !closeIvVisible;
        closeIv.setVisibility(closeIvVisible ? VISIBLE : GONE);
        if (closeIvVisible) {
            postDelayed(dismissCloseIvRunnable, 2000);
        }
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

        void onClose();
    }

    private static class DismissCloseIvRunnable implements Runnable {

        private final WeakReference<FloatView> container;

        public DismissCloseIvRunnable(FloatView floatView) {
            container = new WeakReference<>(floatView);
        }

        @Override
        public void run() {
            try {
                FloatView floatView = container.get();
                if (floatView != null) {
                    floatView.closeIvVisible = !floatView.closeIvVisible;
                    floatView.closeIv.setVisibility(GONE);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
