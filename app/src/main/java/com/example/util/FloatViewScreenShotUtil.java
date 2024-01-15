package com.example.util;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;

/**
 * 悬浮窗截图工具
 *
 * @author laiweisheng
 * @date 2024/1/15
 */
public class FloatViewScreenShotUtil {

    /**
     * 修剪截图的圆角
     */
    public static Drawable getScreenShotRadiusDrawable(Drawable screenShotDrawable, int radius) {
        return bitmapToDrawable(getRoundedCornerBitmap(((BitmapDrawable) screenShotDrawable).getBitmap(), radius));
    }

    /**
     * 获取截图的边框 drawable
     *
     * @param screenShotDrawable 截图
     * @param color              边框颜色
     * @param radius             边框图的圆角
     */
    public static Drawable getScreenShotBorderDrawable(Drawable screenShotDrawable, int color, int radius) {
        return bitmapToDrawable(getRoundedCornerBitmap(generateColoredBitmap(screenShotDrawable, color), radius));
    }

    /**
     * 获取跟指定 drawable 同样宽高的 指定颜色的 Bitmap
     */
    public static Bitmap generateColoredBitmap(Drawable originalDrawable, int color) {
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
    public static Bitmap getRoundedCornerBitmap(Bitmap originalBitmap, float radius) {
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
    public static Drawable bitmapToDrawable(Bitmap bitmap) {
        return new BitmapDrawable(UiUtil.getResources(), bitmap);
    }
}
