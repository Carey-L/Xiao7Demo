package com.example.activity;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;

import com.example.R;
import com.example.xiao7demo.BaseActivity;

/**
 * ConstraintLayout 实现右上角裁剪父布局
 *
 * @author laiweisheng
 * @date 2024/6/14
 */
public class ConstraintLayoutTestActivity extends BaseActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_constraint_layout_test);
        findViewById(R.id.parent_cl).setOnClickListener(v -> Log.i("lws", "parent_cl"));
        findViewById(R.id.red_dot_tv).setOnClickListener(v -> Log.i("lws", "red_dot_tv"));
    }
}
