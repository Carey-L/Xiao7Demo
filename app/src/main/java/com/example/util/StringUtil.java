package com.example.util;

/**
 * 字符串操作工具类
 *
 * @author laiweisheng
 * @date 2024/10/11
 */
public class StringUtil {

    public static boolean isEmpty(String value) {
        return value == null || "".equalsIgnoreCase(value.trim()) || "null".equalsIgnoreCase(value.trim());
    }

    public static boolean allIsNotEmpty(String... strs) {
        if (strs == null || strs.length <= 0) {
            return false;
        }
        for (String str : strs) {
            if (isEmpty(str)) {
                return false;
            }
        }
        return true;
    }
}
