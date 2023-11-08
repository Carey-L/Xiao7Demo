package com.example.util;

import android.view.View;

import com.example.view.RadiusBorderOutlineProvider;

/**
 * 视图操作工具
 *
 * @author laiweisheng
 * @date 2023/11/8
 */
public class ViewUtil {

    /**
     * 设置圆角
     */
    public static void setOutlineProvider(View view, int radio) {
        view.setOutlineProvider(new RadiusBorderOutlineProvider(radio));
        setOutlineProviderIsEffective(view, true);
    }

    /**
     * 设置是否裁减（四周圆角）
     *
     * @param isEffective 是否裁减
     */
    public static void setOutlineProviderIsEffective(View view, boolean isEffective) {
        view.setClipToOutline(isEffective);
    }
}
