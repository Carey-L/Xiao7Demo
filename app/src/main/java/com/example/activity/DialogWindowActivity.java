package com.example.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.example.R;
import com.example.dialog.TestWindowDialog;
import com.example.xiao7demo.BaseActivity;

/**
 * Dialog 实现悬浮窗，验证截图透明背景
 *
 * @author laiweisheng
 * @date 2024/12/2
 */
public class DialogWindowActivity extends BaseActivity implements View.OnClickListener {

    private TextView openTv;
    private TextView closeTv;
    private TextView screenshotTv;

    private TestWindowDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialog_window);
        openTv = findViewById(R.id.dialog_window_open);
        closeTv = findViewById(R.id.dialog_window_close);
        screenshotTv = findViewById(R.id.dialog_window_screenshot);

        openTv.setOnClickListener(this);
        closeTv.setOnClickListener(this);
        screenshotTv.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v == openTv) {
            // 打开悬浮窗
            openFloatWindow();
        } else if (v == closeTv) {
            // 关闭悬浮窗
            closeFloatWindow();
        } else if (v == screenshotTv) {
            // 截图悬浮窗
            screenshotFloatWindow();
        }
    }

    /**
     * 打开悬浮窗
     */
    private void openFloatWindow() {
        if (dialog == null) {
            dialog = new TestWindowDialog(this);
        }
        if (!dialog.isShowing()) {
            dialog.show();
        }
    }

    /**
     * 关闭悬浮窗
     */
    private void closeFloatWindow() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    /**
     * 截图悬浮窗
     */
    private void screenshotFloatWindow() {
        if (dialog != null && dialog.isShowing()) {
            dialog.screenshot();
        }
    }
}
