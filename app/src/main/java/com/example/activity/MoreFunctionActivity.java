package com.example.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_more_function);
        pipTv = findViewById(R.id.picture_in_picture_tv);
        storageTestTv = findViewById(R.id.storage_test_tv);
        initListener();
    }

    private void initListener() {
        pipTv.setOnClickListener(this);
        storageTestTv.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
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
