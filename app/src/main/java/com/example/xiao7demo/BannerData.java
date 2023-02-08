package com.example.xiao7demo;

public class BannerData {

    private int resId;

    private String msg;

    public BannerData() {}

    public BannerData(int resId, String msg) {
        this.resId = resId;
        this.msg = msg;
    }

    public int getResId() {
        return resId;
    }

    public void setResId(int resId) {
        this.resId = resId;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

}
