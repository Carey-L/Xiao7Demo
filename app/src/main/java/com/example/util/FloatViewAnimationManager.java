package com.example.util;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;

import androidx.core.content.ContextCompat;

import com.example.R;
import com.example.view.AdhesionFloatView;
import com.example.view.FloatView;

/**
 * 悬浮窗动画管理类
 *
 * @author laiweisheng
 * @date 2024/1/15
 */
public class FloatViewAnimationManager {

    /**
     * 贴边、回弹动画相关时间（单位：ms）
     * （点击/拖拽）_（目的：全展示/半展示）_（贴边按钮/游戏画面）_（目的：出现/隐藏）_（持续/延时）（_回弹）
     */
    private static final int CLICK_FULL_ADHERE_BUTTON_HIDE_DURATION = 200;
    private static final int CLICK_FULL_GAME_SURFACE_APPEAR_DURATION = 300;

    private static final int DRAG_FULL_ADHERE_BUTTON_HIDE_DURATION = 200;
    private static final int DRAG_FULL_ADHERE_BUTTON_HIDE_DELAY = 100;
    private static final int DRAG_FULL_ADHERE_BUTTON_HIDE_DURATION_REVERSE = 200;
    private static final int DRAG_FULL_GAME_SURFACE_APPEAR_DURATION = 300;
    private static final int DRAG_FULL_GAME_SURFACE_APPEAR_DELAY = 100;
    private static final int DRAG_FULL_GAME_SURFACE_APPEAR_DURATION_REVERSE = 200;

    private static final int DRAG_HALF_GAME_SURFACE_HIDE_DURATION = 200;
    private static final int DRAG_HALF_GAME_SURFACE_HIDE_DELAY = 100;
    private static final int DRAG_HALF_ADHERE_BUTTON_APPEAR_DURATION = 200;
    private static final int DRAG_HALF_ADHERE_BUTTON_APPEAR_DELAY = DRAG_HALF_GAME_SURFACE_HIDE_DELAY + DRAG_HALF_GAME_SURFACE_HIDE_DURATION;
    private static final int DRAG_HALF_GAME_SURFACE_HIDE_DURATION_REVERSE = 300;

    /**
     * 当前悬浮窗贴边状态【-1：贴左屏幕边缘；0：不贴边；1：贴右屏幕边缘】
     */
    public static final String ADHESION_STATE_LEFT = "-1";
    public static final String ADHESION_STATE_NONE = "0";
    public static final String ADHESION_STATE_RIGHT = "1";

    /**
     * 窗口管理器
     */
    private WindowManager windowManager;

    /**
     * 主悬浮窗
     */
    private FloatView gameSurfaceFloatView;
    private WindowManager.LayoutParams gameSurfaceParams;

    /**
     * 贴边悬浮窗
     */
    private AdhesionFloatView adhesionFloatView;
    private WindowManager.LayoutParams adhesionParams;

    /**
     * 现在是否为贴边状态（默认不贴边）
     */
    private String adhesionState = ADHESION_STATE_NONE;

    /**
     * 是否在做动画
     */
    public static boolean isAnimating = false;

    /**
     * 贴边悬浮窗的拖动样式是否初始化
     */
    private boolean initAdhesionFloatViewDragStyle = false;

    private FloatViewAnimationManager() {

    }

    private static class FloatViewAnimationManagerHolder {
        private static final FloatViewAnimationManager INSTANCE = new FloatViewAnimationManager();
    }

    public static FloatViewAnimationManager getInstance() {
        return FloatViewAnimationManagerHolder.INSTANCE;
    }

    public void setWindowManager(WindowManager windowManager) {
        this.windowManager = windowManager;
    }

    public void setFloatViewData(FloatView gameSurfaceFloatView, WindowManager.LayoutParams gameSurfaceParams) {
        this.gameSurfaceFloatView = gameSurfaceFloatView;
        this.gameSurfaceParams = gameSurfaceParams;
    }

    /**
     * 初始化贴边悬浮窗基本数据
     */
    @SuppressLint("ClickableViewAccessibility")
    public void initAdhereFloatView(Context context) {
        if (windowManager == null) {
            return;
        }
        // 标志位初始化
        adhesionState = ADHESION_STATE_NONE;
        isAnimating = false;
        initAdhesionFloatViewDragStyle = false;
        // 创建贴边悬浮窗
        adhesionFloatView = new AdhesionFloatView(context);
        adhesionParams = new WindowManager.LayoutParams(
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ?
                        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY :
                        WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT
        );
        adhesionParams.gravity = Gravity.TOP | Gravity.START;
        adhesionParams.x = 0;
        adhesionParams.y = 0;
        adhesionParams.width = 0;
        adhesionParams.height = 0;


        adhesionFloatView.setVisibility(View.GONE);
        adhesionFloatView.getAdhereToEdgeButtonIv().setOnTouchListener(new View.OnTouchListener() {

            private boolean moved = false;
            private int lastRawX = -1;
            private int lastRawY = -1;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (isAnimating) {
                    return true;
                }
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        lastRawX = (int) event.getRawX();
                        lastRawY = (int) event.getRawY();
                        moved = false;
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        final int moveX = (int) event.getRawX();
                        final int moveY = (int) event.getRawY();
                        final int deltaX = moveX - lastRawX;
                        final int deltaY = moveY - lastRawY;
                        if (!moved && Math.abs(deltaX) < 5 && Math.abs(deltaY) < 5) {
                            return true;
                        }
                        if (!initAdhesionFloatViewDragStyle) {
                            initAdhesionFloatViewDragStyle = true;
                            initAdhereFloatViewStyle();
                        }
                        // 更新贴边悬浮窗的位置
                        adhesionParams.x += deltaX;
                        adhesionParams.y += deltaY;
                        // 更新主悬浮窗位置
                        gameSurfaceParams.x += deltaX;
                        gameSurfaceParams.y += deltaY;
                        windowManager.updateViewLayout(gameSurfaceFloatView, gameSurfaceParams);
                        windowManager.updateViewLayout(adhesionFloatView, adhesionParams);
                        moved = true;
                        lastRawX = moveX;
                        lastRawY = moveY;
                        return true;
                    case MotionEvent.ACTION_UP:
                        if (moved) {
                            controlFloatViewBehavior(false);
                        } else {
                            executeClickBackToScreenAnimation();
                        }
                        initAdhesionFloatViewDragStyle = false;
                        moved = false;
                        break;
                    default:
                        break;
                }
                return true;
            }
        });
        windowManager.addView(adhesionFloatView, adhesionParams);
    }

    /**
     * 设置贴边悬浮窗的样式
     */
    public void initAdhereFloatViewStyle() {
        if (!isAdheredToEdge()) {
            return;
        }
        adhesionFloatView.getLeftScreenShotFl().setVisibility(isAdheredToEdgeLeft() ? View.VISIBLE : View.GONE);
        adhesionFloatView.getRightScreenShotFl().setVisibility(isAdheredToEdgeLeft() ? View.GONE : View.VISIBLE);
        adhesionFloatView.getAdhereToEdgeButtonIv().setTranslationX(0f);
        adhesionFloatView.getAdhereToEdgeButtonIv().setAlpha(1.0f);
        adhesionFloatView.setAlpha(1.0f);
        gameSurfaceFloatView.setAlpha(0f);
    }

    /**
     * 根据当前的悬浮窗位置，判断执行 贴边 or 回弹 动画
     *
     * @param snapBack 是否直接回弹
     */
    public void controlFloatViewBehavior(boolean snapBack) {
        try {
            if (snapBack) {
                executeSnapBackAnimation();
            } else {
                checkAdhereToEdge();
            }
        } catch (Exception e) {
            e.printStackTrace();
            executeSnapBackAnimation();
        }
    }

    /**
     * 悬浮窗检测是否需要执行贴边动画
     */
    private void checkAdhereToEdge() {
        final int widthGameSurface = gameSurfaceParams.width;
        if (isAdheredToEdge()) {
            // 现在是贴边状态，则出现的尺寸为 1/3 悬浮窗大小（减去贴边按钮的宽度）
            final int appearWidth = widthGameSurface / 3;
            if (dragToSnapBackToScreenRangeWithAdhesion(appearWidth)) {
                // 需要回到屏幕内
                executeBackToScreenAnimation();
            } else {
                // 回弹到贴边状态
                executeSnapBackAdhesionStateAnimation();
            }
        } else {
            // 现在不是贴边状态，则隐藏的尺寸为 1/2  悬浮窗大小
            final int hideWidth = widthGameSurface / 2;
            if (dragToAdhereToEdgeRange(hideWidth)) {
                // 需要贴边
                executeAdhereToEdgeAnimation();
            } else {
                // 不需要贴边，回弹到屏幕内
                executeSnapBackAnimation();
            }
        }
    }

    /**
     * 执行回弹动画（没有贴边悬浮窗的时候使用：缩放后回弹 or 移动未达到贴边范围回弹）
     */
    private void executeSnapBackAnimation() {
        final int x = gameSurfaceParams.x;
        final int y = gameSurfaceParams.y;
        if (isFloatViewXAndYInScreen(x, y)) {
            // 需要进行回弹
            int snapBackDistanceX = Math.max(UiUtil.dip2px(5), Math.min(x, UiUtil.getDeviceDisplayMetrics().widthPixels - UiUtil.dip2px(5) - gameSurfaceFloatView.getWidth())) - x;
            int snapBackDistanceY = Math.max(UiUtil.dip2px(5), Math.min(y, UiUtil.getDeviceDisplayMetrics().heightPixels - UiUtil.getStatusBarHeight() - UiUtil.dip2px(5) - gameSurfaceFloatView.getHeight())) - y;
            ValueAnimator translationAnimator = ValueAnimator.ofFloat(0, 1.0f);
            translationAnimator.setInterpolator(new DecelerateInterpolator());
            translationAnimator.setDuration(DRAG_HALF_GAME_SURFACE_HIDE_DURATION_REVERSE);
            translationAnimator.addUpdateListener(animation -> {
                try {
                    float animatedValue = (float) animation.getAnimatedValue();
                    gameSurfaceParams.x = x + (int) (snapBackDistanceX * animatedValue);
                    gameSurfaceParams.y = y + (int) (snapBackDistanceY * animatedValue);
                    windowManager.updateViewLayout(gameSurfaceFloatView, gameSurfaceParams);
                } catch (Exception e) {
                    // gameSurfaceFloatView 已经被移除，会报异常，直接捕获就行
                    e.printStackTrace();
                }
            });
            translationAnimator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    isAnimating = false;
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    isAnimating = false;
                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
            translationAnimator.start();
        } else {
            isAnimating = false;
        }
    }

    /**
     * 执行回弹到屏幕边缘的动画（已经有贴边悬浮窗的时候使用，回弹到屏幕边缘）
     */
    private void executeSnapBackAdhesionStateAnimation() {
        final int xGameSurface = gameSurfaceParams.x;
        final int yGameSurface = gameSurfaceParams.y;
        final int xAdhereButton = adhesionParams.x;
        final int yAdhereButton = adhesionParams.y;
        final int widthGameSurface = gameSurfaceParams.width;
        final int heightGameSurface = gameSurfaceParams.height;
        final int appearWidth = widthGameSurface / 3;
        if (!dragToSnapBackToScreenRangeWithAdhesion(appearWidth)) {
            // 是否回弹到左屏幕边缘
            final boolean adhereToLeftEdge = xGameSurface < -appearWidth;
            // 是否超出屏幕上边缘
            final boolean overTopEdge = yGameSurface < UiUtil.dip2px(5);
            // 是否超出屏幕下边缘
            final boolean overBottomEdge = yGameSurface > UiUtil.getDeviceDisplayMetrics().heightPixels - UiUtil.getStatusBarHeight() - UiUtil.dip2px(5) - heightGameSurface;
            // 包含方向的回弹距离
            int snapBackDistanceX = adhereToLeftEdge ? -(xGameSurface + widthGameSurface) : UiUtil.getDeviceDisplayMetrics().widthPixels - xGameSurface;
            int snapBackDistanceY = overTopEdge ?
                    UiUtil.dip2px(5) - yGameSurface :
                    overBottomEdge ? -(yGameSurface - (UiUtil.getDeviceDisplayMetrics().heightPixels - UiUtil.getStatusBarHeight() - UiUtil.dip2px(5) - heightGameSurface)) : 0;
            ValueAnimator translationAnimator = ValueAnimator.ofFloat(0, 1.0f);
            translationAnimator.setInterpolator(new DecelerateInterpolator());
            translationAnimator.setDuration(DRAG_FULL_GAME_SURFACE_APPEAR_DURATION_REVERSE);
            translationAnimator.addUpdateListener(animation -> {
                try {
                    float animatedValue = (float) animation.getAnimatedValue();
                    adhesionParams.x = xAdhereButton + (int) (snapBackDistanceX * animatedValue);
                    adhesionParams.y = yAdhereButton + (int) (snapBackDistanceY * animatedValue);
                    windowManager.updateViewLayout(adhesionFloatView, adhesionParams);
                    gameSurfaceParams.x = xGameSurface + (int) (snapBackDistanceX * animatedValue);
                    gameSurfaceParams.y = yGameSurface + (int) (snapBackDistanceY * animatedValue);
                    windowManager.updateViewLayout(gameSurfaceFloatView, gameSurfaceParams);
                } catch (Exception e) {
                    // gameSurfaceFloatView or adhesionFloatView 已经被移除，会报异常，直接捕获就行
                    e.printStackTrace();
                }
            });
            translationAnimator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    isAnimating = false;
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    isAnimating = false;
                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
            translationAnimator.start();
        } else {
            isAnimating = false;
        }
    }

    /**
     * 执行贴边动画（主悬浮窗贴到屏幕边缘，贴边悬浮窗出现）
     */
    private void executeAdhereToEdgeAnimation() {
        final int xGameSurface = gameSurfaceParams.x;
        final int yGameSurface = gameSurfaceParams.y;
        final int widthGameSurface = gameSurfaceParams.width;
        final int heightGameSurface = gameSurfaceParams.height;
        final int hideWidth = widthGameSurface / 2;
        if (dragToAdhereToEdgeRange(hideWidth)) {
            // 是否回弹到左屏幕边缘
            final boolean adhereToLeftEdge = xGameSurface < -hideWidth;
            // 是否超出屏幕上边缘
            final boolean overTopEdge = yGameSurface < UiUtil.dip2px(5);
            // 是否超出屏幕下边缘
            final boolean overBottomEdge = yGameSurface > UiUtil.getDeviceDisplayMetrics().heightPixels - UiUtil.getStatusBarHeight() - UiUtil.dip2px(5) - heightGameSurface;
            // 包含方向的回弹距离
            int snapBackDistanceX = adhereToLeftEdge ? -(xGameSurface + widthGameSurface) : UiUtil.getDeviceDisplayMetrics().widthPixels - xGameSurface;
            int snapBackDistanceY = overTopEdge ?
                    UiUtil.dip2px(5) - yGameSurface :
                    overBottomEdge ? -(yGameSurface - (UiUtil.getDeviceDisplayMetrics().heightPixels - UiUtil.getStatusBarHeight() - UiUtil.dip2px(5) - heightGameSurface)) : 0;
            // 动画集
            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.setInterpolator(new DecelerateInterpolator());
            // 主悬浮窗平移到屏幕边缘动画
            ValueAnimator gameSurfaceTranslationAnimator = ValueAnimator.ofFloat(0, 1.0f);
            gameSurfaceTranslationAnimator.setStartDelay(DRAG_HALF_GAME_SURFACE_HIDE_DELAY);
            gameSurfaceTranslationAnimator.setDuration(DRAG_HALF_GAME_SURFACE_HIDE_DURATION);
            gameSurfaceTranslationAnimator.addUpdateListener(animation -> {
                try {
                    float animatedValue = (float) animation.getAnimatedValue();
                    gameSurfaceParams.x = xGameSurface + (int) (snapBackDistanceX * animatedValue);
                    gameSurfaceParams.y = yGameSurface + (int) (snapBackDistanceY * animatedValue);
                    windowManager.updateViewLayout(gameSurfaceFloatView, gameSurfaceParams);
                } catch (Exception e) {
                    // gameSurfaceFloatView 已经被移除，会报异常，直接捕获就行
                    e.printStackTrace();
                }
            });
            // 贴边悬浮窗初始数据
            final int widthAdhereView = AdhesionFloatView.ADHESION_BUTTON_WIDTH + widthGameSurface;
            final int heightAdhereView = heightGameSurface;
            final int xAdhereView = adhereToLeftEdge ? -widthAdhereView : UiUtil.getDeviceDisplayMetrics().widthPixels;
            final int yAdhereView = yGameSurface + snapBackDistanceY;
            // 贴边悬浮窗平移出现动画
            ValueAnimator adhereButtonTranslationAnimator = adhereToLeftEdge
                    ? ValueAnimator.ofInt(xAdhereView, xAdhereView + AdhesionFloatView.ADHESION_BUTTON_WIDTH)
                    : ValueAnimator.ofInt(xAdhereView, xAdhereView - AdhesionFloatView.ADHESION_BUTTON_WIDTH);
            adhereButtonTranslationAnimator.setStartDelay(DRAG_HALF_ADHERE_BUTTON_APPEAR_DELAY);
            adhereButtonTranslationAnimator.setDuration(DRAG_HALF_ADHERE_BUTTON_APPEAR_DURATION);
            adhereButtonTranslationAnimator.addUpdateListener(animation -> {
                try {
                    adhesionParams.x = (int) animation.getAnimatedValue();
                    windowManager.updateViewLayout(adhesionFloatView, adhesionParams);
                } catch (Exception e) {
                    // adhesionFloatView 已经被移除，会报异常，直接捕获就行
                    e.printStackTrace();
                }
            });
            adhereButtonTranslationAnimator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                    if (adhesionFloatView == null) {
                        return;
                    }
                    // 初始化贴边悬浮窗
                    adhesionFloatView.setLocation(adhereToLeftEdge);
                    // 设置截图
                    adhesionFloatView.getLeftScreenShotFl().setVisibility(adhereToLeftEdge ? View.VISIBLE : View.GONE);
                    adhesionFloatView.getLeftScreenShotFl().setBackground(FloatViewScreenShotUtil.getScreenShotBorderDrawable(ContextCompat.getDrawable(UiUtil.getContext(), R.drawable.float_view_background), UiUtil.getResources().getColor(R.color.black_2f3131), UiUtil.dip2px(36)));
                    adhesionFloatView.getLeftScreenShotIv().setImageDrawable(FloatViewScreenShotUtil.getScreenShotRadiusDrawable(ContextCompat.getDrawable(UiUtil.getContext(), R.drawable.float_view_background), UiUtil.dip2px(32)));
                    adhesionFloatView.getRightScreenShotFl().setVisibility(adhereToLeftEdge ? View.GONE : View.VISIBLE);
                    adhesionFloatView.getRightScreenShotFl().setBackground(FloatViewScreenShotUtil.getScreenShotBorderDrawable(ContextCompat.getDrawable(UiUtil.getContext(), R.drawable.float_view_background), UiUtil.getResources().getColor(R.color.black_2f3131), UiUtil.dip2px(36)));
                    adhesionFloatView.getRightScreenShotIv().setImageDrawable(FloatViewScreenShotUtil.getScreenShotRadiusDrawable(ContextCompat.getDrawable(UiUtil.getContext(), R.drawable.float_view_background), UiUtil.dip2px(32)));
                    adhesionFloatView.getAdhereToEdgeButtonIv().setTranslationX(0f);
                    adhesionFloatView.getAdhereToEdgeButtonIv().setAlpha(1.0f);
                    adhesionFloatView.setAlpha(1.0f);
                    adhesionFloatView.setVisibility(View.VISIBLE);
                    adhesionParams.width = widthAdhereView;
                    adhesionParams.height = heightAdhereView;
                    adhesionParams.x = xAdhereView;
                    adhesionParams.y = yAdhereView;
                    windowManager.updateViewLayout(adhesionFloatView, adhesionParams);
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    adhesionState = adhereToLeftEdge ? ADHESION_STATE_LEFT : ADHESION_STATE_RIGHT;
                    gameSurfaceFloatView.setAlpha(0f);
                    isAnimating = false;
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    isAnimating = false;
                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
            animatorSet.playTogether(gameSurfaceTranslationAnimator, adhereButtonTranslationAnimator);
            animatorSet.start();
        } else {
            isAnimating = false;
        }
    }

    /**
     * 执行贴边悬浮窗回到屏幕的动画（主悬浮窗回到屏幕内，贴边悬浮窗隐藏）
     */
    private void executeBackToScreenAnimation() {
        final int xGameSurface = gameSurfaceParams.x;
        final int yGameSurface = gameSurfaceParams.y;
        final int xAdhereView = adhesionParams.x;
        final int yAdhereView = adhesionParams.y;
        final int widthGameSurface = gameSurfaceParams.width;
        final int heightGameSurface = gameSurfaceParams.height;
        final int appearWidth = widthGameSurface / 3;
        if (dragToSnapBackToScreenRangeWithAdhesion(appearWidth)) {
            // 是否超出屏幕上边缘
            final boolean overTopEdge = yGameSurface < UiUtil.dip2px(5);
            // 是否超出屏幕下边缘
            final boolean overBottomEdge = yGameSurface > UiUtil.getDeviceDisplayMetrics().heightPixels - UiUtil.getStatusBarHeight() - UiUtil.dip2px(5) - heightGameSurface;
            final boolean leftScreenEdge = isAdheredToEdgeLeft();
            // 包含方向的回弹距离
            int gameSurfaceDistanceX = isFloatViewXInScreen() ? 0 : isAdheredToEdgeLeft() ? UiUtil.dip2px(5) - xGameSurface : UiUtil.getDeviceDisplayMetrics().widthPixels - widthGameSurface - UiUtil.dip2px(5) - xGameSurface;
            int gameSurfaceDistanceY = overTopEdge ?
                    UiUtil.dip2px(5) - yGameSurface :
                    overBottomEdge ? -(yGameSurface - (UiUtil.getDeviceDisplayMetrics().heightPixels - UiUtil.getStatusBarHeight() - UiUtil.dip2px(5) - heightGameSurface)) : 0;
            // 动画集
            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.setInterpolator(new DecelerateInterpolator());
            // 主悬浮窗平移到屏幕边缘动画
            ValueAnimator gameSurfaceTranslationAnimator = ValueAnimator.ofFloat(0, 1.0f);
            gameSurfaceTranslationAnimator.setStartDelay(DRAG_FULL_GAME_SURFACE_APPEAR_DELAY);
            gameSurfaceTranslationAnimator.setDuration(DRAG_FULL_GAME_SURFACE_APPEAR_DURATION);
            gameSurfaceTranslationAnimator.addUpdateListener(animation -> {
                try {
                    float animatedValue = (float) animation.getAnimatedValue();
                    gameSurfaceParams.x = xGameSurface + (int) (gameSurfaceDistanceX * animatedValue);
                    gameSurfaceParams.y = yGameSurface + (int) (gameSurfaceDistanceY * animatedValue);
                    windowManager.updateViewLayout(gameSurfaceFloatView, gameSurfaceParams);
                } catch (Exception e) {
                    // gameSurfaceFloatView 已经被移除，会报异常，直接捕获就行
                    e.printStackTrace();
                }
            });
            // 贴边按钮平移隐藏动画
            ValueAnimator adhereViewTranslationAnimator = ValueAnimator.ofFloat(0f, 1.0f);
            adhereViewTranslationAnimator.setStartDelay(DRAG_FULL_GAME_SURFACE_APPEAR_DELAY);
            adhereViewTranslationAnimator.setDuration(DRAG_FULL_GAME_SURFACE_APPEAR_DURATION);
            adhereViewTranslationAnimator.addUpdateListener(animation -> {
                try {
                    float animatedValue = (float) animation.getAnimatedValue();
                    adhesionParams.x = xAdhereView + (int) (gameSurfaceDistanceX * animatedValue);
                    adhesionParams.y = yAdhereView + (int) (gameSurfaceDistanceY * animatedValue);
                    windowManager.updateViewLayout(adhesionFloatView, adhesionParams);
                } catch (Exception e) {
                    // adhesionFloatView 已经被移除，会报异常，直接捕获就行
                    e.printStackTrace();
                }
            });
            // 贴边按钮平移隐藏动画
            ValueAnimator adhereButtonTranslationAnimator = ValueAnimator.ofFloat(0f, 1.0f);
            adhereButtonTranslationAnimator.setStartDelay(DRAG_FULL_ADHERE_BUTTON_HIDE_DELAY);
            adhereButtonTranslationAnimator.setDuration(DRAG_FULL_ADHERE_BUTTON_HIDE_DURATION);
            adhereButtonTranslationAnimator.addUpdateListener(animation -> {
                try {
                    float animatedValue = (float) animation.getAnimatedValue();
                    adhesionFloatView.getAdhereToEdgeButtonIv().setTranslationX(animatedValue * (leftScreenEdge ? -AdhesionFloatView.ADHESION_BUTTON_WIDTH : AdhesionFloatView.ADHESION_BUTTON_WIDTH));
                    adhesionFloatView.getAdhereToEdgeButtonIv().setAlpha(1.0f - animatedValue);
                } catch (Exception e) {
                    // adhesionFloatView 已经被移除，会报异常，直接捕获就行
                    e.printStackTrace();
                }
            });
            animatorSet.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                    try {
                        adhesionFloatView.getLeftScreenShotFl().setVisibility(isAdheredToEdgeLeft() ? View.VISIBLE : View.GONE);
                        adhesionFloatView.getRightScreenShotFl().setVisibility(isAdheredToEdgeLeft() ? View.GONE : View.VISIBLE);
                        adhesionFloatView.getAdhereToEdgeButtonIv().setTranslationX(0f);
                        adhesionFloatView.getAdhereToEdgeButtonIv().setAlpha(1.0f);
                        adhesionFloatView.setAlpha(1.0f);
                        gameSurfaceFloatView.setAlpha(0f);
                    } catch (Exception e) {
                        // gameSurfaceFloatView or adhesionFloatView 已经被移除，会报异常，直接捕获就行
                        e.printStackTrace();
                    }
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    adhesionState = ADHESION_STATE_NONE;
                    isAnimating = false;
                    try {
                        // 悬浮穿参数重置
                        gameSurfaceFloatView.setAlpha(1.0f);
                        adhesionFloatView.getLeftScreenShotFl().setVisibility(View.GONE);
                        adhesionFloatView.getRightScreenShotFl().setVisibility(View.GONE);
                        adhesionFloatView.getAdhereToEdgeButtonIv().setTranslationX(0f);
                        adhesionFloatView.getAdhereToEdgeButtonIv().setAlpha(0f);
                        adhesionFloatView.setAlpha(0f);
                        // 隐藏贴边悬浮窗
                        adhesionFloatView.setVisibility(View.GONE);
                        adhesionParams.width = 0;
                        adhesionParams.height = 0;
                        adhesionParams.x = 0;
                        adhesionParams.y = 0;
                        windowManager.updateViewLayout(adhesionFloatView, adhesionParams);
                    } catch (Exception e) {
                        // gameSurfaceFloatView or adhesionFloatView 已经被移除，会报异常，直接捕获就行
                        e.printStackTrace();
                    }
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    isAnimating = false;
                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
            animatorSet.playTogether(gameSurfaceTranslationAnimator, adhereViewTranslationAnimator, adhereButtonTranslationAnimator);
            animatorSet.start();
        } else {
            isAnimating = false;
        }
    }

    /**
     * 执行点击贴边按钮悬浮窗回弹到屏幕内动画（点击贴边按钮时执行）
     */
    private void executeClickBackToScreenAnimation() {
        final int xGameSurface = gameSurfaceParams.x;
        final int yGameSurface = gameSurfaceParams.y;
        final int xAdhereView = adhesionParams.x;
        final int yAdhereView = adhesionParams.y;
        final int widthGameSurface = gameSurfaceParams.width;
        final int heightGameSurface = gameSurfaceParams.height;
        final int widthAdhereView = adhesionParams.width;
        if (isAdheredToEdge()) {
            // 是否超出屏幕上边缘
            final boolean overTopEdge = yGameSurface < UiUtil.dip2px(5);
            // 是否超出屏幕下边缘
            final boolean overBottomEdge = yGameSurface > UiUtil.getDeviceDisplayMetrics().heightPixels - UiUtil.getStatusBarHeight() - UiUtil.dip2px(5) - heightGameSurface;
            // 包含方向的回弹距离
            int gameSurfaceDistanceX = isAdheredToEdgeLeft() ? UiUtil.dip2px(5) - xGameSurface : UiUtil.getDeviceDisplayMetrics().widthPixels - widthGameSurface - UiUtil.dip2px(5) - xGameSurface;
            int gameSurfaceDistanceY = overTopEdge ?
                    UiUtil.dip2px(5) - yGameSurface :
                    overBottomEdge ? -(yGameSurface - (UiUtil.getDeviceDisplayMetrics().heightPixels - UiUtil.getStatusBarHeight() - UiUtil.dip2px(5) - heightGameSurface)) : 0;
            // 动画集
            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.setInterpolator(new DecelerateInterpolator());
            // 主悬浮窗平移到屏幕边缘动画
            ValueAnimator gameSurfaceTranslationAnimator = ValueAnimator.ofFloat(0, 1.0f);
            gameSurfaceTranslationAnimator.setDuration(CLICK_FULL_GAME_SURFACE_APPEAR_DURATION);
            gameSurfaceTranslationAnimator.addUpdateListener(animation -> {
                try {
                    float animatedValue = (float) animation.getAnimatedValue();
                    gameSurfaceParams.x = xGameSurface + (int) (gameSurfaceDistanceX * animatedValue);
                    gameSurfaceParams.y = yGameSurface + (int) (gameSurfaceDistanceY * animatedValue);
                    windowManager.updateViewLayout(gameSurfaceFloatView, gameSurfaceParams);
                } catch (Exception e) {
                    // gameSurfaceFloatView 已经被移除，会报异常，直接捕获就行
                    e.printStackTrace();
                }
            });
            // 贴边悬浮窗的平移参数包含方向
            final int adhereButtonDistanceX = isAdheredToEdgeLeft() ? -widthAdhereView : widthAdhereView;
            final int adhereButtonDistanceY = gameSurfaceDistanceY;
            // 贴边按钮平移隐藏动画
            ValueAnimator adhereButtonTranslationAnimator = ValueAnimator.ofFloat(0f, 1.0f);
            adhereButtonTranslationAnimator.setDuration(CLICK_FULL_ADHERE_BUTTON_HIDE_DURATION);
            adhereButtonTranslationAnimator.addUpdateListener(animation -> {
                try {
                    float animatedValue = (float) animation.getAnimatedValue();
                    adhesionParams.x = xAdhereView + (int) (adhereButtonDistanceX * animatedValue);
                    adhesionParams.y = yAdhereView + (int) (adhereButtonDistanceY * animatedValue);
                    adhesionFloatView.setAlpha(1.0f - animatedValue);
                    windowManager.updateViewLayout(adhesionFloatView, adhesionParams);
                } catch (Exception e) {
                    // adhesionFloatView 已经被移除，会报异常，直接捕获就行
                    e.printStackTrace();
                }
            });
            animatorSet.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                    try {
                        gameSurfaceFloatView.setAlpha(1.0f);
                    } catch (Exception e) {
                        // gameSurfaceFloatView 已经被移除，会报异常，直接捕获就行
                        e.printStackTrace();
                    }
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    adhesionState = ADHESION_STATE_NONE;
                    isAnimating = false;
                    try {
                        // 隐藏贴边悬浮窗
                        adhesionFloatView.setVisibility(View.GONE);
                        adhesionParams.width = 0;
                        adhesionParams.height = 0;
                        adhesionParams.x = 0;
                        adhesionParams.y = 0;
                        windowManager.updateViewLayout(adhesionFloatView, adhesionParams);
                    } catch (Exception e) {
                        // adhesionFloatView 已经被移除，会报异常，直接捕获就行
                        e.printStackTrace();
                    }
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    isAnimating = false;
                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
            animatorSet.playTogether(gameSurfaceTranslationAnimator, adhereButtonTranslationAnimator);
            animatorSet.start();
        } else {
            isAnimating = false;
        }
    }

    /**
     * 移除贴边悬浮窗
     */
    public void removeAdhereFloatView() {
        if (windowManager != null) {
            if (adhesionFloatView != null) {
                windowManager.removeViewImmediate(adhesionFloatView);
            }
        }
    }

    /**
     * 悬浮窗是否在限制的边界内
     */
    private boolean isFloatViewXAndYInScreen(int x, int y) {
        return x < UiUtil.dip2px(5)
                || y < UiUtil.dip2px(5)
                || x > UiUtil.getDeviceDisplayMetrics().widthPixels - UiUtil.dip2px(5) - gameSurfaceFloatView.getWidth()
                || y > UiUtil.getDeviceDisplayMetrics().heightPixels - UiUtil.getStatusBarHeight() - UiUtil.dip2px(5) - gameSurfaceFloatView.getHeight();
    }

    /**
     * 悬浮窗横向是否在屏幕内
     */
    private boolean isFloatViewXInScreen() {
        return gameSurfaceParams.x >= UiUtil.dip2px(5) && gameSurfaceParams.x <= UiUtil.getDeviceDisplayMetrics().widthPixels - gameSurfaceParams.width - UiUtil.dip2px(5);
    }

    /**
     * 贴边状态拖拽时，是否在回弹到屏幕内的范围内
     *
     * @return true 需要回弹到屏幕内
     */
    private boolean dragToSnapBackToScreenRangeWithAdhesion(int appearWidth) {
        int xGameSurface = gameSurfaceParams.x;
        int widthGameSurface = gameSurfaceParams.width;
        return isAdheredToEdgeLeft() && xGameSurface >= -(widthGameSurface - appearWidth)
                || isAdheredToEdgeRight() && xGameSurface <= UiUtil.getDeviceDisplayMetrics().widthPixels - appearWidth;
    }

    /**
     * 普通状态拖拽时，是否在贴边的范围内
     *
     * @return true 需要回弹到屏幕内
     */
    private boolean dragToAdhereToEdgeRange(int hideWidth) {
        int xGameSurface = gameSurfaceParams.x;
        return xGameSurface < -hideWidth || xGameSurface > UiUtil.getDeviceDisplayMetrics().widthPixels - hideWidth;
    }

    /**
     * 当前是否为贴边状态
     *
     * @return true 贴边状态；false 不贴边状态
     */
    private boolean isAdheredToEdge() {
        return !ADHESION_STATE_NONE.equals(adhesionState);
    }

    /**
     * 是否贴屏幕左边
     */
    private boolean isAdheredToEdgeLeft() {
        return ADHESION_STATE_LEFT.equals(adhesionState);
    }

    /**
     * 是否贴屏幕右边
     */
    private boolean isAdheredToEdgeRight() {
        return ADHESION_STATE_RIGHT.equals(adhesionState);
    }
}
