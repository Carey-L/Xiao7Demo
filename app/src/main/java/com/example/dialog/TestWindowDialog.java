package com.example.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.PixelCopy;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.NonNull;

import com.example.R;
import com.example.util.UiUtil;
import com.example.view.DialogFloatWindowView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * 测试透明截图 Dialog
 *
 * @author laiweisheng
 * @date 2024/12/2
 */
public class TestWindowDialog extends Dialog {

    private final Handler mainHandler;

    private View floatView;
    private WindowManager.LayoutParams mFloatViewWindowParams;

    public TestWindowDialog(@NonNull Context context) {
        super(context, R.style.Theme_AppCompat_Translucent);
        mainHandler = new Handler(Looper.getMainLooper());
        initFloatView(context);
        initFloatViewWindowParams();
        initPositionAndSize();
        updateWindowParams(mFloatViewWindowParams);
        setContentView(floatView);
    }

    private void initFloatView(Context context) {
        floatView = new DialogFloatWindowView(context);
        floatView.setOnTouchListener(new View.OnTouchListener() {
            private final int slop = 5;
            private int lastRawX = -1;
            private int lastRawY = -1;
            private boolean moved = false;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                try {
                    if (floatView == null) {
                        return false;
                    }
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            // 是通过拦截触摸事件实现的，所以一定不会走到这里
                            break;
                        case MotionEvent.ACTION_MOVE:
                            final int moveX = (int) event.getRawX();
                            final int moveY = (int) event.getRawY();
                            if (lastRawY == -1 && lastRawX == -1) {
                                lastRawX = moveX;
                                lastRawY = moveY;
                                moved = false;
                            }
                            if (!moved && Math.abs(moveY - lastRawY) < slop && Math.abs(moveX - lastRawX) < slop) {
                                return true;
                            }
                            int dx = moveX - lastRawX;
                            int x = mFloatViewWindowParams.x + dx;
                            int dy = moveY - lastRawY;
                            int y = mFloatViewWindowParams.y + dy;

                            mFloatViewWindowParams.x = x;
                            mFloatViewWindowParams.y = y;
                            updateWindowParams(mFloatViewWindowParams);
                            moved = true;
                            lastRawX = moveX;
                            lastRawY = moveY;
                            return true;
                        case MotionEvent.ACTION_UP:
                            lastRawX = -1;
                            lastRawY = -1;
                            moved = false;
                            break;
                        default:
                            break;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return true;
            }
        });
    }

    private void initFloatViewWindowParams() {
        mFloatViewWindowParams = getWindow().getAttributes();
        mFloatViewWindowParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_PANEL;
        //设置悬浮窗属性
        mFloatViewWindowParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_FULLSCREEN
                | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                | WindowManager.LayoutParams.FLAG_SCALED;
        mFloatViewWindowParams.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN;
        //设置悬浮窗透明
        mFloatViewWindowParams.format = PixelFormat.TRANSLUCENT;
        //设置悬浮窗显示方位
        mFloatViewWindowParams.gravity = Gravity.START | Gravity.TOP;
    }

    private void initPositionAndSize() {
        mFloatViewWindowParams.x = UiUtil.dip2px(10);
        mFloatViewWindowParams.y = UiUtil.dip2px(10);
        mFloatViewWindowParams.width = UiUtil.dip2px(300);
        mFloatViewWindowParams.height = UiUtil.dip2px(150);
    }

    private void updateWindowParams(WindowManager.LayoutParams layoutParams) {
        getWindow().setAttributes(layoutParams);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DisplayMetrics display = UiUtil.getResources().getDisplayMetrics();
        getWindow().getWindowManager().getDefaultDisplay().getRealMetrics(display);
        initFloatViewWindowParams();
        initPositionAndSize();
        updateWindowParams(mFloatViewWindowParams);
        setOnDismissListener(dialog -> {
            if (floatView != null && floatView.isAttachedToWindow()) {
                floatView.setVisibility(View.GONE);
                floatView = null;
            }
        });
    }

    /**
     * 截图窗口
     */
    public void screenshot() {
        // 创建一个透明背景的 Bitmap
        Bitmap bitmap = Bitmap.createBitmap(mFloatViewWindowParams.width, mFloatViewWindowParams.height, Bitmap.Config.ARGB_8888);
        // 使用 PixelCopy 从 Window 截图
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            PixelCopy.request(getWindow(), bitmap, copyResult -> {
                if (copyResult == PixelCopy.SUCCESS) {
                    // 截图成功，保存 Bitmap 或做其他处理
                    saveBitmapToFile(bitmap);
                    // 防止到悬浮窗里
                    floatView.setBackground(new BitmapDrawable(UiUtil.getResources(), bitmap));
                } else {
                    // 截图失败
                    Log.e("PixelCopy", "Screenshot failed with result: " + copyResult);
                }
            }, mainHandler);
        }
    }

    private void saveBitmapToFile(Bitmap bitmap) {
        File file = new File(UiUtil.getContext().getExternalFilesDir(null), "window_screenshot.png");
        try (FileOutputStream out = new FileOutputStream(file)) {
            // 保持透明背景，PNG格式支持透明
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
