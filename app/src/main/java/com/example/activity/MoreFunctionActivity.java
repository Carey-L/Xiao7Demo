package com.example.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.R;
import com.example.xiao7demo.BaseActivity;

/**
 * 首页编辑跳转界面，防止更多功能界面
 *
 * @author laiweisheng
 * @date 2023/11/16
 */
public class MoreFunctionActivity extends BaseActivity implements View.OnClickListener {

    private TextView pipTv;
    private TextView storageTestTv;
    private TextView chatAiTv;
    private TextView jump64PluginTv;
    private TextView jump32PluginTv;
    private TextView clipParentTestTv;
    private TextView constraintLayoutTestTv;
    private TextView translucentActivityTestTv;
    private TextView getNetworkAddressIpTv;
    private TextView dialogWindowScreenshotTv;
    private TextView selectAnchorScalePicTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_more_function);
        pipTv = findViewById(R.id.picture_in_picture_tv);
        storageTestTv = findViewById(R.id.storage_test_tv);
        chatAiTv = findViewById(R.id.chat_ai_tv);
        jump64PluginTv = findViewById(R.id.jump_64_plugin_tv);
        jump32PluginTv = findViewById(R.id.jump_32_plugin_tv);
        clipParentTestTv = findViewById(R.id.clip_parent_test_tv);
        constraintLayoutTestTv = findViewById(R.id.constraint_layout_test_tv);
        translucentActivityTestTv = findViewById(R.id.translucent_activity_test_tv);
        getNetworkAddressIpTv = findViewById(R.id.get_network_address_ip_tv);
        dialogWindowScreenshotTv = findViewById(R.id.dialog_window_screenshot_tv);
        selectAnchorScalePicTv = findViewById(R.id.select_anchor_scale_pic_tv);
        initListener();
    }

    private void initListener() {
        pipTv.setOnClickListener(this);
        storageTestTv.setOnClickListener(this);
        chatAiTv.setOnClickListener(this);
        jump64PluginTv.setOnClickListener(this);
        jump32PluginTv.setOnClickListener(this);
        clipParentTestTv.setOnClickListener(this);
        constraintLayoutTestTv.setOnClickListener(this);
        translucentActivityTestTv.setOnClickListener(this);
        getNetworkAddressIpTv.setOnClickListener(this);
        dialogWindowScreenshotTv.setOnClickListener(this);
        selectAnchorScalePicTv.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        try {
            if (v == pipTv) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    // 如果权限尚未授权，请求权限
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                } else {
                    // 权限已授权，执行相应操作
                    startActivity(new Intent(this, VideoActivity.class));
                }
            } else if (v == storageTestTv) {
                startActivity(new Intent(this, StorageTestActivity.class));
            } else if (v == chatAiTv) {
                startActivity(new Intent(this, ChatAiActivity.class));
            } else if (v == jump64PluginTv) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("smwl://com.smwl.auxiliary64bit.wakeup?packageName=com.jsqq.alspzf.x7sy"));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            } else if (v == jump32PluginTv) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("smwl://com.smwl.auxiliary32bit.wakeup?packageName=com.jsqq.alspzf.x7sy"));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            } else if (v == clipParentTestTv) {
                startActivity(new Intent(this, ClipParentTestActivity.class));
            } else if (v == constraintLayoutTestTv) {
                startActivity(new Intent(this, ConstraintLayoutTestActivity.class));
            } else if (v == translucentActivityTestTv) {
                startActivity(new Intent(this, TestTranslucentActivity.class));
            } else if (v == getNetworkAddressIpTv) {
                startActivity(new Intent(this, NetworkAddressIpTestActivity.class));
            } else if (v == dialogWindowScreenshotTv) {
                startActivity(new Intent(this, DialogWindowActivity.class));
            } else if (v == selectAnchorScalePicTv) {
                startActivity(new Intent(this, SelectAnchorScalePicActivity.class));
            }
        } catch (Exception e) {
            Log.e("lws", e.toString());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 用户授权了权限，可以开始访问存储
                startActivity(new Intent(this, VideoActivity.class));
            } else {
                // 用户拒绝了权限请求，你需要提供适当的反馈
                Toast.makeText(this, "画中画需要读取本地视频文件，请在设置开启应用的存储权限", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
