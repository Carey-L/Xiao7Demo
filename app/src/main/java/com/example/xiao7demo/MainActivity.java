package com.example.xiao7demo;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.example.R;
import com.example.activity.CalendarTestActivity;
import com.example.service.FloatingWindowService;
import com.youth.banner.Banner;
import com.youth.banner.adapter.BannerImageAdapter;
import com.youth.banner.holder.BannerImageHolder;
import com.youth.banner.indicator.CircleIndicator;
import com.youth.banner.listener.OnBannerListener;

import net.lucode.hackware.magicindicator.MagicIndicator;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.CommonNavigator;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends BaseActivity implements View.OnClickListener {

    private final List<BannerData> bannerData = new ArrayList<>();

    private final List<FirstData> firstData = new ArrayList<>();

    private final String[] firstMsgInit = new String[]{"小号回收", "小号管理", "联系客服", "绑定有礼", "邀请有奖"};

    private final String[] firstMsgMore = new String[]{"关于小7", "投诉反馈", "小7视频", "扫描", "设置中心"};

    private final String[] bannerMsg = new String[]{"apple", "banana", "grape", "watermelon"};

    private final int[] bannerResource = new int[]{R.drawable.lws_apple, R.drawable.lws_banana, R.drawable.lws_grape, R.drawable.lws_watermelon};

    private FirstRecyclerViewAdapter firstAdapter;

    private Button expand, close, message, download;

    private ObjectAnimator animator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lws_activity_main);

        // tool bar
        // Toolbar toolbar = findViewById(R.id.toolbar);
        // setSupportActionBar(toolbar);

        // 读取内存储
        boolean expandFlag = getSharedPreferences("data", MODE_PRIVATE).getBoolean("expand_flag", false);

        // banner
        Banner<BannerData, BannerImageAdapter> banner = findViewById(R.id.banner);
        banner.setAdapter(new BannerImageAdapter<BannerData>(getBannerData()) {
                    @Override
                    public void onBindView(BannerImageHolder holder, BannerData data, int position, int size) {
                        Glide.with(MainActivity.this)
                                .load(data.getResId())
                                //.apply(RequestOptions.bitmapTransform(new RoundedCorners(150)))
                                .into(holder.imageView);
                    }
                })
                .setIndicator(new CircleIndicator(this))
                .addBannerLifecycleObserver(this)
                .setOnBannerListener((OnBannerListener<BannerData>) (data, position) -> Toast.makeText(MainActivity.this, data.getMsg() + ":" + position, Toast.LENGTH_SHORT).show());

        // recycler view 1
        RecyclerView firstRecyclerView = findViewById(R.id.recycler_view_1);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 5);
        firstAdapter = new FirstRecyclerViewAdapter(MainActivity.this, getFirstData());
        gridLayoutManager.setOrientation(RecyclerView.VERTICAL);
        firstRecyclerView.setLayoutManager(gridLayoutManager);
        firstRecyclerView.setAdapter(firstAdapter);

        // expand button
        expand = findViewById(R.id.refresh_first_view);
        expand.setOnClickListener(this);
        float translationY = expand.getTranslationY();
        // 如果还没点击过展开按钮，则播放动画
        if (!expandFlag) {
            animator = ObjectAnimator.ofFloat(expand, "translationY", translationY, translationY, 10.0f, translationY, 10.0f, translationY, translationY);
            animator.setDuration(2000);
            animator.setRepeatCount(-1);
            animator.start();
        }
        // close button
        close = findViewById(R.id.close_first_view);
        close.setOnClickListener(this);

        // view  pager
        ViewPager2 viewPager = findViewById(R.id.view_pager);
        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(this);
        viewPager.setAdapter(viewPagerAdapter);

        // magic indicator
        MagicIndicator indicator = findViewById(R.id.magic_indicator);
        MagicAdapter adapter = new MagicAdapter(this);
        adapter.setOnIndicatorTapClickListener(viewPager::setCurrentItem);
        CommonNavigator navigator = new CommonNavigator(this);
        navigator.setAdjustMode(true);
        navigator.setAdapter(adapter);
        indicator.setNavigator(navigator);

        bindViewPager2(indicator, viewPager);

        message = findViewById(R.id.message_title);
        message.setOnClickListener(this);

        download = findViewById(R.id.download_title);
        download.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v == expand) {
            firstAdapter.refresh(moreFirstData());
            expand.setVisibility(View.GONE);
            close.setVisibility(View.VISIBLE);
            if (animator != null && animator.isRunning()) {
                animator.end();
                // 修改内存储初次加载状态
                getSharedPreferences("data", MODE_PRIVATE).edit().putBoolean("expand_flag", true).apply();
            }
        } else if (v == close) {
            firstAdapter.refresh(getFirstData());
            close.setVisibility(View.GONE);
            expand.setVisibility(View.VISIBLE);
        } else if (v == message) {
            startActivity(new Intent(this, CalendarTestActivity.class));
        } else if (v == download) {
            Intent intent = new Intent(this, FloatingWindowService.class);
            startService(intent);
        }
    }

    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar, menu);
        return true;
    }*/

    public List<FirstData> moreFirstData() {
        for (int i = 0; i < 5; i++) {
            firstData.add(new FirstData(firstMsgMore[i], "activity:" + (i + 5)));
        }
        return firstData;
    }

    public List<FirstData> getFirstData() {
        firstData.clear();
        for (int i = 0; i < 5; i++) {
            firstData.add(new FirstData(firstMsgInit[i], "activity:" + i));
        }
        return firstData;
    }

    public List<BannerData> getBannerData() {
        bannerData.clear();
        for (int i = 0; i < 4; i++) {
            bannerData.add(new BannerData(bannerResource[i], bannerMsg[i]));
        }
        return bannerData;
    }

    private void bindViewPager2(MagicIndicator magicIndicator, ViewPager2 viewPager) {
        // viewpager2 注册一个回调方法当界面改变时
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            // 旧界面被滚动
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels);
                magicIndicator.onPageScrolled(position, positionOffset, positionOffsetPixels);
            }

            // 新界面被选择
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                magicIndicator.onPageSelected(position);
            }

            // state：0 --- ViewPager2 处于空闲、已设置状态
            // state：1 --- ViewPager2 当前正在被拖动
            // state：2 --- ViewPager2 正在调整到最终位置
            @Override
            public void onPageScrollStateChanged(int state) {
                super.onPageScrollStateChanged(state);
                magicIndicator.onPageScrollStateChanged(state);
            }
        });
    }

}