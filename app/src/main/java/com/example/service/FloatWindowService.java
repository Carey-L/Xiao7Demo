package com.example.service;

import android.Manifest;
import android.animation.Animator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;

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

    /**
     * 是否在回滚状态
     */
    private boolean isSnapBacking = false;

    private boolean canZoom = false;

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
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                    PixelFormat.TRANSLUCENT
            );
            params.gravity = Gravity.TOP | Gravity.START;
            params.x = UiUtil.dip2px(5);
            params.y = UiUtil.dip2px(5);
            params.width = defaultWidth;
            params.height = defaultHeight;

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
                    int currentWidth = params.width;
                    int targetWidth = Math.max(UiUtil.dip2px(160), Math.min(UiUtil.getDeviceDisplayMetrics().widthPixels - UiUtil.dip2px(10), (int) (currentWidth * scale)));
//                    int targetWidth = Math.min(UiUtil.dip2px(270), Math.max(UiUtil.dip2px(90), (int) (currentWidth * scale)));
                    if (targetWidth % 2 != 0) {
                        targetWidth -= 1;
                    }
//                    int targetWidth = Math.max(UiUtil.dip2px(160), Math.min(UiUtil.getDeviceDisplayMetrics().widthPixels - UiUtil.dip2px(10), (int) (beginWidth * scaleFactor * scale)));
                    if (currentWidth == targetWidth) {
                        return;
                    }
                    int currentHeight = params.height;
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
                    int x = params.x - (targetWidth - currentWidth) / 2;
                    int y = params.y - (targetHeight - currentHeight) / 2;
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
                    if (isSnapBacking) {
                        return true;
                    }
                    if (event.getPointerCount() >= 2) {
                        lastRawX = -1;
                        lastRawY = -1;
                        initOuterFloatView();
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
                            int x = params.x + deltaX;
                            int y = params.y + deltaY;
                            // 边界处理
//                            x = Math.max(UiUtil.dip2px(5), Math.min(x, UiUtil.getDeviceDisplayMetrics().widthPixels - UiUtil.dip2px(5) - v.getWidth()));
//                            y = Math.max(UiUtil.dip2px(5), Math.min(y, UiUtil.getDeviceDisplayMetrics().heightPixels - UiUtil.getStatusBarHeight() - UiUtil.dip2px(5) - v.getHeight()));
                            // 更新悬浮窗位置大小
                            params.x = x;
                            params.y = y;
                            windowManager.updateViewLayout(v, params);
                            moved = true;
                            lastRawX = moveX;
                            lastRawY = moveY;
                            return true;
                        case MotionEvent.ACTION_UP:
                            if (outerView != null) {
                                UiUtil.getMainHandler().postDelayed(() -> removeOuterFloatView(), 200);
                            } else {
                                isSnapBacking = true;
                                snapBackFloatView();
                            }
                            canZoom = false;
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
            floatView.getRootView().setBackground(bitmapToDrawable(getRoundedCornerBitmap(generateColoredBitmap(ContextCompat.getDrawable(this, R.drawable.float_view_background), getResources().getColor(R.color.black)) , UiUtil.dip2px(36))));
            floatView.getBackgroundIv().setImageDrawable(bitmapToDrawable(getRoundedCornerBitmap(((BitmapDrawable) ContextCompat.getDrawable(this, R.drawable.float_view_background)).getBitmap(), UiUtil.dip2px(32))));
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

    @SuppressLint("ClickableViewAccessibility")
    private void initOuterFloatView() {
        if (outerView != null) {
            return;
        }
        if (windowManager == null) {
            return;
        }
        beginWidth = params.width;
        beginHeight = params.height;
        if (outerLayoutParams == null) {
            outerLayoutParams = new WindowManager.LayoutParams(
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ?
                            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY :
                            WindowManager.LayoutParams.TYPE_PHONE,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                    PixelFormat.TRANSLUCENT
            );
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
        FrameLayout.LayoutParams borderLayoutParams = new FrameLayout.LayoutParams(params.width, params.height);
        borderLayoutParams.leftMargin = params.x;
        borderLayoutParams.topMargin = params.y + UiUtil.getStatusBarHeight();
        outerBorderBackgroundTv.setLayoutParams(borderLayoutParams);
        outerBorderBackgroundTv.setImageDrawable(bitmapToDrawable(getRoundedCornerBitmap(generateColoredBitmap(floatView.getRootView().getBackground(), getResources().getColor(R.color.black)) , UiUtil.dip2px(36))));
        outerBorderBackgroundTv.setScaleType(ImageView.ScaleType.FIT_XY);
        outerBorderBackgroundTv.setVisibility(View.VISIBLE);
        outerView.addView(outerBorderBackgroundTv);

        outerBackgroundIv = new ImageView(this);
        FrameLayout.LayoutParams childLayoutParams = new FrameLayout.LayoutParams(params.width - UiUtil.dip2px(8), params.height - UiUtil.dip2px(8));
        childLayoutParams.leftMargin = params.x + UiUtil.dip2px(4);
        childLayoutParams.topMargin = params.y + UiUtil.dip2px(4) + UiUtil.getStatusBarHeight();
        outerBackgroundIv.setLayoutParams(childLayoutParams);
        outerBackgroundIv.setImageDrawable(bitmapToDrawable(getRoundedCornerBitmap(((BitmapDrawable) floatView.getBackgroundIv().getDrawable()).getBitmap(), UiUtil.dip2px(0))));
        outerBackgroundIv.setScaleType(ImageView.ScaleType.FIT_XY);
        outerBackgroundIv.setVisibility(View.VISIBLE);
        outerView.addView(outerBackgroundIv);

        windowManager.addView(outerView, outerLayoutParams);
        UiUtil.getMainHandler().postDelayed(() -> {
            if (floatView != null) {
                floatView.setAlpha(0);
            }
            canZoom = true;
        }, 20);
    }

    /**
     * 获取跟指定 drawable 同样宽高的 指定颜色的 Bitmap
     */
    private Bitmap generateColoredBitmap(Drawable originalDrawable, int color) {
        Bitmap targetBitmap = ((BitmapDrawable) originalDrawable).getBitmap();
        int targetWidth = targetBitmap.getWidth();
        int targetHeight = targetBitmap.getHeight();
        Bitmap resultBitmap = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(resultBitmap);
        ColorDrawable colorDrawable = new ColorDrawable(color);
        colorDrawable.setBounds(0, 0, targetWidth, targetHeight);
        colorDrawable.draw(canvas);
        return resultBitmap;
    }

    /**
     * 把 drawable 改成圆角
     */
    private Bitmap getRoundedCornerBitmap(Bitmap originalBitmap, float radius) {
        Bitmap roundedBitmap = Bitmap.createBitmap(originalBitmap.getWidth(), originalBitmap.getHeight(), Bitmap.Config.ARGB_8888);
        // 画布
        Canvas canvas = new Canvas(roundedBitmap);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        // 绘制圆角
        canvas.drawRoundRect(new RectF(0, 0, originalBitmap.getWidth(), originalBitmap.getHeight()), radius, radius, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(originalBitmap, 0, 0, paint);
        return roundedBitmap;
    }

    /**
     * 把 bitmap 转换成 drawable
     */
    private Drawable bitmapToDrawable(Bitmap bitmap) {
        return new BitmapDrawable(getResources(), bitmap);
    }

    private void removeOuterFloatView() {
        if (outerView != null) {
            if (floatView != null) {
                floatView.setAlpha(1.0f);
            }
            UiUtil.getMainHandler().postDelayed(() -> {
                if (outerView != null) {
                    outerView.setAlpha(0);
                    outerView.setVisibility(View.GONE);
                    windowManager.removeView(outerView);
                    outerView = null;
                    outerBackgroundIv = null;
                    isSnapBacking = true;
                    snapBackFloatView();
                }
            }, 20);
        }
    }

    /**
     * 悬浮窗需要回弹，则开始回弹动画
     */
    private void snapBackFloatView() {
        final int x = params.x;
        final int y = params.y;
        if (x < UiUtil.dip2px(5)
                || y < UiUtil.dip2px(5)
                || x > UiUtil.getDeviceDisplayMetrics().widthPixels - UiUtil.dip2px(5) - floatView.getWidth()
                || y > UiUtil.getDeviceDisplayMetrics().heightPixels - UiUtil.getStatusBarHeight() - UiUtil.dip2px(5) - floatView.getHeight()) {
            // 需要进行回弹
            int snapBackDistanceX = Math.max(UiUtil.dip2px(5), Math.min(x, UiUtil.getDeviceDisplayMetrics().widthPixels - UiUtil.dip2px(5) - floatView.getWidth())) - x;
            int snapBackDistanceY = Math.max(UiUtil.dip2px(5), Math.min(y, UiUtil.getDeviceDisplayMetrics().heightPixels - UiUtil.getStatusBarHeight() - UiUtil.dip2px(5) - floatView.getHeight())) - y;
            ValueAnimator translationAnimator = ValueAnimator.ofFloat(0, 1.0f);
            translationAnimator.setInterpolator(new DecelerateInterpolator());
            translationAnimator.setDuration(250);
            translationAnimator.addUpdateListener(animation -> {
                float animatedValue = (float) animation.getAnimatedValue();
                params.x = x + (int) (snapBackDistanceX * animatedValue);
                params.y = y + (int) (snapBackDistanceY * animatedValue);
                windowManager.updateViewLayout(floatView, params);
            });
            translationAnimator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    isSnapBacking = false;
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
            translationAnimator.start();
        } else {
            isSnapBacking = false;
        }
    }

}
