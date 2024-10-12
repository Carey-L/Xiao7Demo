package com.example.util;

import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import java.io.StringReader;

/**
 * Json 解析工具
 *
 * @author laiweisheng
 * @date 2024/10/11
 */
public class JsonParseUtil {

    private static final Gson gson = new Gson();

    /**
     * 解析数据，有返回值
     *
     * @param json      需要解析的 json 字符串
     * @param beanClass 目标 bean
     */
    public static <T> T parse(String json, Class<T> beanClass) {
        try {
            json = json.trim();
            JsonReader reader = new JsonReader(new StringReader(json));
            reader.setLenient(true);
            return gson.fromJson(reader, beanClass);
        } catch (Exception e) {
            e.printStackTrace();
            if ("main".equalsIgnoreCase(Thread.currentThread().getName())) {
                Toast.makeText(UiUtil.getContext(), "解析工具类异常", Toast.LENGTH_SHORT).show();
            }
        }
        return null;
    }
}
