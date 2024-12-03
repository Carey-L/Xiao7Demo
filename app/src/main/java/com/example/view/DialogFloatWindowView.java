package com.example.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;

import com.example.R;

/**
 * 测试透明截图 Dialog 自定义 View
 *
 * @author laiweisheng
 * @date 2024/12/2
 */
public class DialogFloatWindowView extends FrameLayout {

    private int rawX = -1;
    private int rawY = -1;
    private long downEventTime = -1;

    public DialogFloatWindowView(@NonNull Context context) {
        super(context);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.dialog_float_window_layout, this);
        // 开启硬件加速
        setLayerType(View.LAYER_TYPE_HARDWARE, null);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                rawX = (int) ev.getRawX();
                rawY = (int) ev.getRawY();
                downEventTime = ev.getEventTime();
                break;
            case MotionEvent.ACTION_MOVE:
                int moveX = (int) ev.getRawX();
                int moveY = (int) ev.getRawY();
                //拦截滑动事件
                if (Math.abs(moveX - rawX) > 5 || Math.abs(moveY - rawY) > 5) {
                    return true;
                }
                break;
            case MotionEvent.ACTION_UP:
                final long downTime = downEventTime;
                downEventTime = -1;
                rawX = -1;
                rawY = -1;
                if (downTime != -1 && ev.getEventTime() - downTime > 200) {
                    return true;
                }
                break;
            default:
                break;
        }
        return super.onInterceptTouchEvent(ev);
    }
}
