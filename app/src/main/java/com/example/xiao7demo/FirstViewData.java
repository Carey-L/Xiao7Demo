package com.example.xiao7demo;

public class FirstViewData {

    private String name;

    private String url;

    private Integer status;

    public FirstViewData() {
    }

    public FirstViewData(String name, String url, Integer status) {
        this.name = name;
        this.url = url;
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status= status;
    }
}
