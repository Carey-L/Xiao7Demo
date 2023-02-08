package com.example.xiao7demo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewOutlineProvider;

import androidx.appcompat.widget.AppCompatButton;

import com.liulishuo.filedownloader.FileDownloader;

public class FirstItemButton extends AppCompatButton {

    private final Paint paint;

    public static final int STATE_DOWNLOAD = 0; // 下载 - 初始状态

    public final static int STATE_DOWNLOADING = 1; // 下载中

    public final static int STATE_COMPLETE = 2; // 打开

    private int curState; // 当前状态

    public FirstItemButton(Context context) {
        this(context, null);
    }

    public FirstItemButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FirstItemButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        paint = new Paint();
        // 初始状态
        curState = STATE_DOWNLOAD;
        // 消除重叠
        paint.setAntiAlias(true);
        paint.setTextSize(getTextSize());
        paint.setColor(Color.WHITE);
        // 设置点击事件
        setOnClickListener(v -> {
            if (curState == STATE_DOWNLOAD) {
                setState(STATE_DOWNLOADING);
                // FileDownloader.getImpl().pause(FileDownloaderUtil.singleTaskId);
            } else if (curState == STATE_COMPLETE) {
                // 下载完成，打开第三应用，模拟打开浏览器
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.baidu.com"));
                context.startActivity(intent);
            }
        });
    }

    // 设置当前状态
    public void setState(int state) {
        this.curState = state;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // 视图轮廓提供者
        ViewOutlineProvider viewOutlineProvider = new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                int width = getWidth();
                int height = getHeight();
                // 设置有弧角的轮廓
                outline.setRoundRect(0, 0, width, height, 50);
            }
        };
        // 传入轮廓构建者
        setOutlineProvider(viewOutlineProvider);
        // 修改轮廓
        setClipToOutline(true);
        // 重新构建轮廓
        invalidateOutline();

        String tip = "";
        if (curState == STATE_DOWNLOAD) {
            tip = "下载";
        } else if (curState == STATE_DOWNLOADING) {
            tip = "下载中";
        } else if (curState == STATE_COMPLETE) {
            tip = "打开";
        }

        // 绘制提示文本
        @SuppressLint("DrawAllocation") Rect textBound = new Rect();
        paint.getTextBounds(tip, 0, tip.length(), textBound);
        canvas.drawText(tip, (float) (getMeasuredWidth() - textBound.width()) / 2, (float) (getMeasuredHeight() + textBound.height()) / 2, paint);
    }
}
