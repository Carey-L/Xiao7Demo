package com.example.utils;

import android.app.Application;

import com.example.util.UiUtil;
import com.wanjian.sak.SAK;

public class SAKApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        SAK.init(this, null);
        UiUtil.setContext(getBaseContext());
    }
}
