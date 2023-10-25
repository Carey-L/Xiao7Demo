package com.example.xiao7demo;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.util.UiUtil;

public class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        UiUtil.setCurrentActivity(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
