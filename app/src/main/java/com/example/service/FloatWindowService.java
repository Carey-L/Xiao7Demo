package com.example.service;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.example.R;
import com.example.util.UiUtil;
import com.example.view.FloatView;

import java.lang.reflect.Field;

/**
 * 悬浮窗服务
 *
 * @author laiweisheng
 * @date 2023/10/25
 */
public class FloatWindowService extends Service {

    private WindowManager windowManager;
    private FloatView floatView;
    private WindowManager.LayoutParams params;
    private ScaleGestureDetector scaleGestureDetector;

    /**
     * 悬浮窗初始宽高
     */
    private final int defaultWidth = UiUtil.dip2px(160);
    private final int defaultHeight = UiUtil.dip2px(90);

    /**
     * 悬浮窗比例（width/height）
     */
    private final float aspectRatio = (float) 16 / 9;

    @Override
    public void onCreate() {
        super.onCreate();
        // 创建并显示悬浮窗，具体布局和交互逻辑可以在这里定义
        createFloatingWindow();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        removeFloatingWindow();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createFloatingWindow() {
        if (canDrawOverlays()) {
            // 创建并显示悬浮窗
            windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
            floatView = new FloatView(this);
            params = new WindowManager.LayoutParams(
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ?
                            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY :
                            WindowManager.LayoutParams.TYPE_PHONE,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT
            );
            params.gravity = Gravity.BOTTOM | Gravity.END;
            params.x = UiUtil.dip2px(5);
            params.y = UiUtil.dip2px(5);
            params.width = defaultWidth;
            params.height = defaultHeight;

            // 设置双指缩放手势
            scaleGestureDetector = new ScaleGestureDetector(this, new ScaleGestureDetector.SimpleOnScaleGestureListener() {

                private static final float SMOOTHING_FACTOR = 0.05f;
                private float smoothedScaleFactor = 1.0f;

                @Override
                public boolean onScale(ScaleGestureDetector detector) {
                    if (Math.abs(detector.getScaleFactor() - 1) < 0.01) {
                        return false;
                    }
                    initOuterFloatView();
                    scaleByZoom(detector.getScaleFactor());
                    return true;
                }

                @Override
                public boolean onScaleBegin(ScaleGestureDetector detector) {
                    return true;
                }

                @Override
                public void onScaleEnd(ScaleGestureDetector detector) {
                    removeOuterFloatView();
                }

                public void scaleByZoom(float scale) {
                    smoothedScaleFactor = smoothedScaleFactor + SMOOTHING_FACTOR * (scale - smoothedScaleFactor);
                    int currentWidth = params.width;
                    int targetWidth = Math.min(UiUtil.getDeviceDisplayMetrics().widthPixels - UiUtil.dip2px(10), (int) (currentWidth * scale));
                    if (currentWidth == targetWidth) {
                        return;
                    }
                    int currentHeight = params.height;
                    int targetHeight = (int) (targetWidth / aspectRatio);
                    // 确保悬浮窗往四周缩放
                    int x = params.x - (targetWidth - currentWidth) / 2;
                    int y = params.y - (targetHeight - currentHeight) / 2;
                    // 边界处理
                    x = Math.max(UiUtil.dip2px(5), Math.min(x, UiUtil.getDeviceDisplayMetrics().widthPixels - UiUtil.dip2px(5) - targetWidth));
                    y = Math.max(UiUtil.dip2px(5), Math.min(y, UiUtil.getDeviceDisplayMetrics().heightPixels - UiUtil.getStatusBarHeight() - UiUtil.dip2px(5) - targetHeight));
                    // 更新悬浮窗位置大小
                    params.x = x;
                    params.y = y;
                    params.width = targetWidth;
                    params.height = targetHeight;
                    windowManager.updateViewLayout(floatView, params);
                }
            });

            /*scaleGestureDetector = new ScaleGestureDetector(this, new ScaleGestureDetector.SimpleOnScaleGestureListener() {

                private float scaleFactor = 1.0f;

                @Override
                public boolean onScale(ScaleGestureDetector detector) {
                    scaleFactor *= detector.getScaleFactor();
                    scaleFactor = Math.max(0.1f, Math.min(scaleFactor, 5.0f));
                    floatView.setScaleX(scaleFactor);
                    floatView.setScaleY(scaleFactor);
                    windowManager.updateViewLayout(floatView, params);
                    return true;
                }
            });*/

            // 去掉缩放的误触限制
            try {
                Field minSpanField = ScaleGestureDetector.class.getDeclaredField("mMinSpan");
                Field spanSlopField = ScaleGestureDetector.class.getDeclaredField("mSpanSlop");
                minSpanField.setAccessible(true);
                spanSlopField.setAccessible(true);
                minSpanField.setInt(scaleGestureDetector, 0);
                minSpanField.setInt(scaleGestureDetector, 0);
            } catch (Exception e) {
                e.printStackTrace();
            }

            // 设置悬浮窗的触摸监听
            floatView.setOnTouchListener(new View.OnTouchListener() {

                private int lastRawX = -1;
                private int lastRawY = -1;
                private boolean moved = false;

                @SuppressLint("ClickableViewAccessibility")
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getPointerCount() == 2) {
                        lastRawX = -1;
                        lastRawY = -1;
                        return scaleGestureDetector.onTouchEvent(event);
                    }
                    if (event.getPointerCount() > 2) {
                        return true;
                    }
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_MOVE:
                            final int moveX = (int) event.getRawX();
                            final int moveY = (int) event.getRawY();
                            if (lastRawY == -1 && lastRawX == -1) {
                                lastRawX = moveX;
                                lastRawY = moveY;
                                moved = false;
                            }
                            final int deltaX = moveX - lastRawX;
                            final int deltaY = moveY - lastRawY;
                            if (!moved && Math.abs(deltaX) < 5 && Math.abs(deltaY) < 5) {
                                return true;
                            }
                            int x = params.x - deltaX;
                            int y = params.y - deltaY;
                            // 边界处理
                            x = Math.max(UiUtil.dip2px(5), Math.min(x, UiUtil.getDeviceDisplayMetrics().widthPixels - UiUtil.dip2px(5) - v.getWidth()));
                            y = Math.max(UiUtil.dip2px(5), Math.min(y, UiUtil.getDeviceDisplayMetrics().heightPixels - UiUtil.getStatusBarHeight() - UiUtil.dip2px(5) - v.getHeight()));
                            // 更新悬浮窗位置大小
                            params.x = x;
                            params.y = y;
                            windowManager.updateViewLayout(v, params);
                            moved = true;
                            lastRawX = moveX;
                            lastRawY = moveY;
                            return true;
                        case MotionEvent.ACTION_UP:
                            removeOuterFloatView();
                            lastRawX = -1;
                            lastRawY = -1;
                            moved = false;
                            break;
                        default:
                            break;
                    }
                    return true;
                }
            });

            windowManager.addView(floatView, params);
        } else {
            // 如果没有权限，跳转到系统设置界面以请求权限
            gotoOverlaySetting();
        }
    }

    /**
     * 跳转到悬浮窗权限设置页面
     */
    public void gotoOverlaySetting() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivity(intent);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void removeFloatingWindow() {
        if (windowManager != null && floatView != null) {
            windowManager.removeView(floatView);
        }
    }

    private boolean canDrawOverlays() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return Settings.canDrawOverlays(this);
        } else {
            return (ContextCompat.checkSelfPermission(this, Manifest.permission.SYSTEM_ALERT_WINDOW) == PackageManager.PERMISSION_GRANTED);
        }
    }

    WindowManager outerWindowManager;
    WindowManager.LayoutParams outerLayoutParams;
    View outerView;

    private void initOuterFloatView() {
        if (outerView != null) {
            return;
        }
        Log.e("lws", "123");
        if (outerWindowManager == null) {
            // 创建并显示悬浮窗
            outerWindowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        }
        if (outerLayoutParams == null) {
            outerLayoutParams = new WindowManager.LayoutParams(
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ?
                            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY :
                            WindowManager.LayoutParams.TYPE_PHONE,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT
            );
            outerLayoutParams.gravity = Gravity.BOTTOM | Gravity.END;
            outerLayoutParams.x = 0;
            outerLayoutParams.y = 0;
            outerLayoutParams.width = UiUtil.getDeviceDisplayMetrics().widthPixels;
            outerLayoutParams.height = UiUtil.getDeviceDisplayMetrics().heightPixels;
        }
        outerView = new View(this);
        outerView.setBackgroundColor(getResources().getColor(R.color.transparent));
        outerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return false;
            }
        });

        outerWindowManager.addView(outerView, outerLayoutParams);
    }

    private void removeOuterFloatView() {
        Log.e("lws", "456");
        if (outerWindowManager != null && outerView != null) {
            outerWindowManager.removeViewImmediate(outerView);
            outerView.setVisibility(View.GONE);
            outerView = null;
        }
    }

}
