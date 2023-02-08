package com.example.xiao7demo;

public class FirstData {

    private String name;

    private String activity;

    public FirstData() {
    }

    public FirstData(String name) {
        this.name = name;
    }

    public FirstData(String name, String activity) {
        this.name = name;
        this.activity = activity;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getActivity() {
        return activity;
    }

    public void setActivity(String activity) {
        this.activity = activity;
    }
}
