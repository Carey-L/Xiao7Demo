package com.example.xiao7demo;

import android.content.Context;
import android.util.TypedValue;

import com.example.R;

import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.CommonNavigatorAdapter;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerIndicator;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerTitleView;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.indicators.LinePagerIndicator;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.titles.ColorTransitionPagerTitleView;

public class MagicAdapter extends CommonNavigatorAdapter {

    private final String[] titles;

    private OnIndicatorTapClickListener listener;

    public MagicAdapter(Context context) {
        this.titles = context.getResources().getStringArray(R.array.indicator_title);
    }

    @Override
    public int getCount() {
        if (titles != null) {
            return titles.length;
        }
        return 0;
    }

    @Override
    public IPagerTitleView getTitleView(Context context, int index) {
        ColorTransitionPagerTitleView colorTransitionPagerTitleView = new ColorTransitionPagerTitleView(context);
        colorTransitionPagerTitleView.setText(titles[index]);
        colorTransitionPagerTitleView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
        colorTransitionPagerTitleView.setSelectedColor(context.getResources().getColor(R.color.teal_200));
        colorTransitionPagerTitleView.setNormalColor(context.getResources().getColor(R.color.black));
        // 为此标题设置点击事件
        colorTransitionPagerTitleView.setOnClickListener(view -> {
            // 为该标题设置监听事件
            listener.onTabClick(index);
        });
        return colorTransitionPagerTitleView;
    }

    @Override
    public IPagerIndicator getIndicator(Context context) {
        LinePagerIndicator linePagerIndicator = new LinePagerIndicator(context);
        // 下滑线的宽度
        linePagerIndicator.setMode(LinePagerIndicator.MODE_EXACTLY);
        // 设置下横线的颜色
        linePagerIndicator.setColors(R.color.teal_200);
        return linePagerIndicator;
    }

    public void setOnIndicatorTapClickListener(OnIndicatorTapClickListener listener) {
        this.listener = listener;
    }

    public interface OnIndicatorTapClickListener {
        void onTabClick(int index);
    }
}