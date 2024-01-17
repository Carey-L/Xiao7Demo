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
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.example.R;
import com.example.util.FloatViewAnimationManager;
import com.example.util.FloatViewScreenShotUtil;
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
    private FloatView gameSurfaceFloatView;
    private WindowManager.LayoutParams gameSurfaceParams;
    private ScaleGestureDetector scaleGestureDetector;

    /**
     * 悬浮窗初始宽高
     */
    private final int defaultWidth = UiUtil.dip2px(320);
    private final int defaultHeight = UiUtil.dip2px(180);

    /**
     * 悬浮窗比例（width/height）
     */
    private final float aspectRatio = (float) 16 / 9;

    /**
     * 外层悬浮窗相关，丝滑缩放效果关键
     */
    private WindowManager.LayoutParams outerLayoutParams;
    private FrameLayout outerView;
    private ImageView outerBackgroundIv;
    private ImageView outerBorderBackgroundTv;

    /**
     * 开始缩放时，根据这个原始尺寸来计算外层悬浮窗的缩放比例
     */
    private int beginWidth;
    private int beginHeight;

    private boolean canZoom = false;

    @Override
    public void onCreate() {
        super.onCreate();
        // 创建并显示悬浮窗，具体布局和交互逻辑可以在这里定义
        createFloatWindow();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        removeFloatView();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createFloatWindow() {
        if (canDrawOverlays()) {
            if (windowManager == null) {
                windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
            }
            FloatViewAnimationManager.getInstance().setWindowManager(windowManager);
            FloatViewAnimationManager.getInstance().initAdhereFloatView(this);
            initGameSurfaceFloatView();
            FloatViewAnimationManager.getInstance().setFloatViewData(gameSurfaceFloatView, gameSurfaceParams);
        } else {
            // 如果没有权限，跳转到系统设置界面以请求权限
            gotoOverlaySetting();
        }
    }

    /**
     * 初始化主悬浮窗，后添加，保证层级（主悬浮窗在贴边悬浮窗下面）
     */
    private void initGameSurfaceFloatView() {
        // 创建并显示悬浮窗
        gameSurfaceFloatView = new FloatView(this);
        gameSurfaceParams = new WindowManager.LayoutParams(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY : WindowManager.LayoutParams.TYPE_PHONE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, PixelFormat.TRANSLUCENT);
        gameSurfaceParams.gravity = Gravity.TOP | Gravity.START;
        gameSurfaceParams.x = UiUtil.dip2px(5);
        gameSurfaceParams.y = UiUtil.dip2px(5);
        gameSurfaceParams.width = defaultWidth;
        gameSurfaceParams.height = defaultHeight;

        // 设置双指缩放手势
        scaleGestureDetector = new ScaleGestureDetector(this, new ScaleGestureDetector.SimpleOnScaleGestureListener() {

            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                if (!canZoom) {
                    return false;
                }
                scaleByZoom(detector.getScaleFactor());
                return true;
            }

            @Override
            public boolean onScaleBegin(ScaleGestureDetector detector) {
                return true;
            }

            @Override
            public void onScaleEnd(ScaleGestureDetector detector) {
            }

            private void scaleOuter(float scaleX, float scaleY) {
                if (outerBackgroundIv != null) {
                    outerBackgroundIv.setScaleX(scaleX);
                    outerBackgroundIv.setScaleY(scaleY);
                }
            }

            private void scaleBorder(float scaleX, float scaleY) {
                if (outerBorderBackgroundTv != null) {
                    outerBorderBackgroundTv.setScaleX(scaleX);
                    outerBorderBackgroundTv.setScaleY(scaleY);
                }
            }

            private void scaleByZoom(float scale) {
                int currentWidth = gameSurfaceParams.width;
                int targetWidth = Math.max(UiUtil.dip2px(160), Math.min(UiUtil.getDeviceDisplayMetrics().widthPixels - UiUtil.dip2px(10), (int) (currentWidth * scale)));
//                    int targetWidth = Math.min(UiUtil.dip2px(270), Math.max(UiUtil.dip2px(90), (int) (currentWidth * scale)));
                if (targetWidth % 2 != 0) {
                    targetWidth -= 1;
                }
//                    int targetWidth = Math.max(UiUtil.dip2px(160), Math.min(UiUtil.getDeviceDisplayMetrics().widthPixels - UiUtil.dip2px(10), (int) (beginWidth * scaleFactor * scale)));
                if (currentWidth == targetWidth) {
                    return;
                }
                int currentHeight = gameSurfaceParams.height;
                int targetHeight = (int) (targetWidth / aspectRatio);
                if (targetHeight % 2 != 0) {
                    targetHeight -= 1;
                }
                try {
                    scaleBorder((float) targetWidth / beginWidth, (float) targetHeight / beginHeight);
                    scaleOuter((float) (targetWidth - UiUtil.dip2px(8)) / (beginWidth - UiUtil.dip2px(8)), (float) (targetHeight - UiUtil.dip2px(8)) / (beginHeight - UiUtil.dip2px(8)));
                } catch (IllegalArgumentException e) {
                    return;
                }
                // 确保悬浮窗往四周缩放
                int x = gameSurfaceParams.x - (targetWidth - currentWidth) / 2;
                int y = gameSurfaceParams.y - (targetHeight - currentHeight) / 2;
//                    Log.e("lws", "-----------------------");
//                    Log.e("lws", "width:height=" + targetWidth + ":" + targetHeight);
//                    Log.e("lws", "originW:originH=" + outerBackgroundIv.getWidth() + ":" + outerBackgroundIv.getHeight());
//                    Log.e("lws", "scaleW:scaleH=" + outerBackgroundIv.getWidth() * outerBackgroundIv.getScaleX() + ":" + outerBackgroundIv.getHeight() * outerBackgroundIv.getScaleY());
//                    Log.e("lws", "scaleX:scaleY=" + outerBackgroundIv.getScaleX() + ":" + outerBackgroundIv.getScaleY());
//                    Log.e("lws", "x:y=" + x + ":" + y);
                // 边界处理
//                    x = Math.max(UiUtil.dip2px(5), Math.min(x, UiUtil.getDeviceDisplayMetrics().widthPixels - UiUtil.dip2px(5) - targetWidth));
//                    y = Math.max(UiUtil.dip2px(5), Math.min(y, UiUtil.getDeviceDisplayMetrics().heightPixels - UiUtil.getStatusBarHeight() - UiUtil.dip2px(5) - targetHeight));
                // 更新悬浮窗位置大小
                gameSurfaceParams.x = x;
                gameSurfaceParams.y = y;
                gameSurfaceParams.width = targetWidth;
                gameSurfaceParams.height = targetHeight;

                windowManager.updateViewLayout(gameSurfaceFloatView, gameSurfaceParams);
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

        gameSurfaceFloatView.setListener(new FloatView.TouchEventListener() {
            @Override
            public void onDown() {

            }

            @Override
            public void onClose() {
                removeFloatView();
            }
        });
        // 设置悬浮窗的触摸监听
        gameSurfaceFloatView.setOnTouchListener(new View.OnTouchListener() {

            private int lastRawX = -1;
            private int lastRawY = -1;
            private boolean moved = false;
            private boolean scaled = false;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (FloatViewAnimationManager.isAnimating) {
                    return true;
                }
                if (event.getPointerCount() >= 2) {
                    lastRawX = -1;
                    lastRawY = -1;
                    initOuterFloatView();
                    scaled = true;
                    return scaleGestureDetector.onTouchEvent(event);
                }
                if (event.getAction() != MotionEvent.ACTION_UP && outerBackgroundIv != null) {
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
                        int x = gameSurfaceParams.x + deltaX;
                        int y = gameSurfaceParams.y + deltaY;
                        // 边界处理
//                            x = Math.max(UiUtil.dip2px(5), Math.min(x, UiUtil.getDeviceDisplayMetrics().widthPixels - UiUtil.dip2px(5) - v.getWidth()));
//                            y = Math.max(UiUtil.dip2px(5), Math.min(y, UiUtil.getDeviceDisplayMetrics().heightPixels - UiUtil.getStatusBarHeight() - UiUtil.dip2px(5) - v.getHeight()));
                        // 更新悬浮窗位置大小
                        gameSurfaceParams.x = x;
                        gameSurfaceParams.y = y;
                        windowManager.updateViewLayout(v, gameSurfaceParams);
                        moved = true;
                        lastRawX = moveX;
                        lastRawY = moveY;
                        return true;
                    case MotionEvent.ACTION_UP:
                        if (outerView != null) {
                            UiUtil.getMainHandler().postDelayed(() -> removeOuterFloatView(), 200);
                        } else {
                            FloatViewAnimationManager.isAnimating = true;
                            FloatViewAnimationManager.getInstance().controlFloatViewBehavior(scaled);
                        }
                        canZoom = false;
                        lastRawX = -1;
                        lastRawY = -1;
                        if (!moved && !scaled) {
                            v.performClick();
                        }
                        moved = false;
                        scaled = false;
                        break;
                    default:
                        break;
                }
                return true;
            }
        });
        gameSurfaceFloatView.getRootView().setBackground(FloatViewScreenShotUtil.getScreenShotBorderDrawable(ContextCompat.getDrawable(this, R.drawable.float_view_background), getResources().getColor(R.color.black_2f3131), UiUtil.dip2px(36)));
        gameSurfaceFloatView.getBackgroundIv().setImageDrawable(FloatViewScreenShotUtil.getScreenShotRadiusDrawable(ContextCompat.getDrawable(this, R.drawable.lws_download), UiUtil.dip2px(32)));
        windowManager.addView(gameSurfaceFloatView, gameSurfaceParams);
    }

    private void removeFloatView() {
        if (windowManager != null) {
            if (gameSurfaceFloatView != null) {
                windowManager.removeViewImmediate(gameSurfaceFloatView);
                gameSurfaceFloatView = null;
            }
            FloatViewAnimationManager.getInstance().removeAdhereFloatView();
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

    private boolean canDrawOverlays() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return Settings.canDrawOverlays(this);
        } else {
            return (ContextCompat.checkSelfPermission(this, Manifest.permission.SYSTEM_ALERT_WINDOW) == PackageManager.PERMISSION_GRANTED);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initOuterFloatView() {
        if (outerView != null) {
            return;
        }
        if (windowManager == null) {
            return;
        }
        beginWidth = gameSurfaceParams.width;
        beginHeight = gameSurfaceParams.height;
        if (outerLayoutParams == null) {
            outerLayoutParams = new WindowManager.LayoutParams(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY : WindowManager.LayoutParams.TYPE_PHONE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, PixelFormat.TRANSLUCENT);
            outerLayoutParams.gravity = Gravity.TOP | Gravity.START;
            outerLayoutParams.x = 0;
            outerLayoutParams.y = -UiUtil.getStatusBarHeight();
            outerLayoutParams.width = UiUtil.getDeviceDisplayMetrics().widthPixels;
            outerLayoutParams.height = UiUtil.getDeviceDisplayMetrics().heightPixels + UiUtil.getStatusBarHeight();
        }
        outerView = new FrameLayout(this);
        outerView.setOnTouchListener((v, event) -> {
            removeOuterFloatView();
            return false;
        });

        outerBorderBackgroundTv = new ImageView(this);
        FrameLayout.LayoutParams borderLayoutParams = new FrameLayout.LayoutParams(gameSurfaceParams.width, gameSurfaceParams.height);
        borderLayoutParams.leftMargin = gameSurfaceParams.x;
        borderLayoutParams.topMargin = gameSurfaceParams.y + UiUtil.getStatusBarHeight();
        outerBorderBackgroundTv.setLayoutParams(borderLayoutParams);
        outerBorderBackgroundTv.setImageDrawable(FloatViewScreenShotUtil.getScreenShotBorderDrawable(gameSurfaceFloatView.getRootView().getBackground(), getResources().getColor(R.color.black_2f3131), UiUtil.dip2px(36)));
        outerBorderBackgroundTv.setScaleType(ImageView.ScaleType.FIT_XY);
        outerBorderBackgroundTv.setVisibility(View.VISIBLE);
        outerView.addView(outerBorderBackgroundTv);

        outerBackgroundIv = new ImageView(this);
        FrameLayout.LayoutParams childLayoutParams = new FrameLayout.LayoutParams(gameSurfaceParams.width - UiUtil.dip2px(8), gameSurfaceParams.height - UiUtil.dip2px(8));
        childLayoutParams.leftMargin = gameSurfaceParams.x + UiUtil.dip2px(4);
        childLayoutParams.topMargin = gameSurfaceParams.y + UiUtil.dip2px(4) + UiUtil.getStatusBarHeight();
        outerBackgroundIv.setLayoutParams(childLayoutParams);
        outerBackgroundIv.setImageDrawable(FloatViewScreenShotUtil.getScreenShotRadiusDrawable(gameSurfaceFloatView.getBackgroundIv().getDrawable(), UiUtil.dip2px(0)));
        outerBackgroundIv.setScaleType(ImageView.ScaleType.FIT_XY);
        outerBackgroundIv.setVisibility(View.VISIBLE);
        outerView.addView(outerBackgroundIv);

        windowManager.addView(outerView, outerLayoutParams);
        UiUtil.getMainHandler().postDelayed(() -> {
            if (gameSurfaceFloatView != null) {
                gameSurfaceFloatView.setAlpha(0);
            }
            canZoom = true;
        }, 50);
    }


    private void removeOuterFloatView() {
        if (outerView != null) {
            if (gameSurfaceFloatView != null) {
                gameSurfaceFloatView.setAlpha(1.0f);
            }
            UiUtil.getMainHandler().postDelayed(() -> {
                if (outerView != null) {
                    outerView.setAlpha(0);
                    outerView.setVisibility(View.GONE);
                    windowManager.removeView(outerView);
                    outerView = null;
                    outerBackgroundIv = null;
                    FloatViewAnimationManager.isAnimating = true;
                    FloatViewAnimationManager.getInstance().controlFloatViewBehavior(true);
                }
            }, 50);
        }
    }
}
