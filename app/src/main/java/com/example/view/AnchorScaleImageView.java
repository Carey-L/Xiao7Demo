package com.example.view;

import android.content.Context;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;

public class AnchorScaleImageView extends AppCompatImageView {

    /**
     * 宽高比 9:16
     */
    private static final float WIDTH_HEIGHT_RATIO = 9.0f / 16.0f;
    private static final float HEIGHT_WIDTH_RATIO = 16.0f / 9.0f;

    private Matrix matrix;
    private float startX, startY;

    /**
     * 判断是否触摸左下角区域
     */
    private boolean isLeftBottomTouch = false;

    /**
     * 判断是否触摸右下角区域
     */
    private boolean isRightBottomTouch = false;

    public AnchorScaleImageView(@NonNull Context context) {
        this(context, null);
    }

    public AnchorScaleImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AnchorScaleImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        matrix = new Matrix();
        setScaleType(ScaleType.MATRIX);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float endX = event.getX();
        float endY = event.getY();

        int leftBottomTouchArea = dpToPx(50);
        int rightBottomTouchArea = getWidth() - dpToPx(50);

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // 判断触摸点是否在左下角或右下角区域
                if (endX >= 0 && endX <= leftBottomTouchArea && endY >= getHeight() - leftBottomTouchArea && endY <= getHeight()) {
                    isLeftBottomTouch = true;
                } else if (endX >= rightBottomTouchArea && endX <= getWidth() && endY >= getHeight() - leftBottomTouchArea && endY <= getHeight()) {
                    isRightBottomTouch = true;
                }
                break;

            case MotionEvent.ACTION_MOVE:
                if (isLeftBottomTouch || isRightBottomTouch) {
                    // 根据触摸点的变化来计算缩放比例
                    float deltaX = endX - startX;
                    Log.e("lws", "deltaX: " + deltaX);


                    // 根据 deltaX 计算 deltaY，保证宽高比
                    float deltaY = deltaX * HEIGHT_WIDTH_RATIO;

                    // 根据触摸点来选择缩放的锚点
                    if (isLeftBottomTouch) {
                        // 左下角触摸，以右上角为锚点
                        matrix.postScale(1 - deltaX / getWidth(), 1 - deltaY / getHeight(), getWidth(), 0);
                    } else if (isRightBottomTouch) {
                        // 右下角触摸，以左上角为锚点
                        matrix.postScale(1 + deltaX / getWidth(), 1 + deltaY / getHeight(), 0, 0);
                    }

                    // 更新图片的矩阵
                    setImageMatrix(matrix);
                }
                break;

            case MotionEvent.ACTION_UP:
                // 手指离开后清除触摸状态
                isLeftBottomTouch = false;
                isRightBottomTouch = false;
                break;
        }

        // 记录当前触摸点
        startX = endX;
        startY = endY;

        return true;
    }

    // dp 转 px
    private int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }
}
