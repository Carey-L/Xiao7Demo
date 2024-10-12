package com.example.interfce;

/**
 * 网络请求回调
 *
 * @author laiweisheng
 * @date 2024/10/11
 */
public interface OkHttpCallback {

    /**
     * 请求成功
     */
    void onSuccess(String result);

    /**
     * 请求失败
     */
    void onFail(String result);
}
