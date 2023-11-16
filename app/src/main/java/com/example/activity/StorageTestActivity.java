package com.example.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.R;
import com.example.xiao7demo.BaseActivity;

/**
 * 存储测试界面
 *
 * @author laiweisheng
 * @date 2023/11/16
 */
public class StorageTestActivity extends BaseActivity implements View.OnClickListener {

    private TextView saveFileTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_storage_test);
        saveFileTv = findViewById(R.id.save_file_tv);
        initListener();
    }

    private void initListener() {
        saveFileTv.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v == saveFileTv) {
            Toast.makeText(this, "save", Toast.LENGTH_SHORT).show();
        }
    }
}
