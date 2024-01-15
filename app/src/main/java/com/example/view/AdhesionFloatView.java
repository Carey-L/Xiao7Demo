package com.example.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.example.R;
import com.example.util.UiUtil;

/**
 * 折叠到边缘时，弹出的贴边按钮
 *
 * @author laiweisheng
 * @date 2024/1/10
 */
public class AdhesionFloatView extends FrameLayout {

    /**
     * 贴边按钮的 宽 高
     */
    public static final int ADHESION_BUTTON_WIDTH = UiUtil.dip2px(25);
    public static final int ADHESION_BUTTON_HEIGHT = UiUtil.dip2px(60);

    private int rawX = -1;
    private int rawY = -1;

    private ImageView adhereToEdgeButtonIv;
    private FrameLayout leftScreenShotFl;
    private ImageView leftScreenShotIv;
    private FrameLayout rightScreenShotFl;
    private ImageView rightScreenShotIv;

    public AdhesionFloatView(@NonNull Context context) {
        super(context);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.adhesion_float_window_layout, this);
        adhereToEdgeButtonIv = findViewById(R.id.float_view_fold_iv);
        leftScreenShotFl = findViewById(R.id.left_screen_shot_fl);
        leftScreenShotIv = findViewById(R.id.left_screen_shot_iv);
        rightScreenShotFl = findViewById(R.id.right_screen_shot_fl);
        rightScreenShotIv = findViewById(R.id.right_screen_shot_iv);
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

    public void setLocation(boolean isLeft) {
        adhereToEdgeButtonIv.setImageDrawable(ContextCompat.getDrawable(getContext(), isLeft ? R.drawable.float_view_left_adhesion_icon : R.drawable.float_view_right_adhesion_icon));
    }

    public ImageView getAdhereToEdgeButtonIv() {
        return adhereToEdgeButtonIv;
    }

    public FrameLayout getLeftScreenShotFl() {
        return leftScreenShotFl;
    }

    public FrameLayout getRightScreenShotFl() {
        return rightScreenShotFl;
    }

    public ImageView getLeftScreenShotIv() {
        return leftScreenShotIv;
    }

    public ImageView getRightScreenShotIv() {
        return rightScreenShotIv;
    }
}
