package com.example.xiao7demo;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.util.UiUtil;

import java.util.Locale;

public class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(newBase);
        // 从SharedPreferences中获取用户选择的语言
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String selectedLanguage = preferences.getString("language", "zh_CN");
        // 设置应用程序的语言
        setAppLanguage(selectedLanguage);
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

    private void setAppLanguage(String language) {
        try {
            Resources res = getResources();
            Configuration config = res.getConfiguration();
            Locale locale = new Locale(language);
            config.setLocale(locale);
            res.updateConfiguration(config, res.getDisplayMetrics());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
