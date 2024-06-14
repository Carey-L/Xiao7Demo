package com.example.activity;

import android.os.Bundle;
import android.util.Log;

import com.example.R;
import com.example.xiao7demo.BaseActivity;


/**
 * 测试右上角裁剪父布局
 *
 * @author laiweisheng
 * @date 2024/6/14
 */
public class ClipParentTestActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clip_parent_test);
        findViewById(R.id.parent_rl).setOnClickListener(v -> Log.i("lws", "parent_rl"));
        findViewById(R.id.red_dot_tv).setOnClickListener(v -> Log.i("lws", "red_dot_tv"));
    }

}
