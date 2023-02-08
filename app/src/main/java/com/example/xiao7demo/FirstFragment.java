package com.example.xiao7demo;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.R;

import java.util.ArrayList;
import java.util.List;

public class FirstFragment extends BaseFragment {

    private RecyclerView recyclerView;

    private FirstFragmentAdapter adapter;

    private final List<FirstViewData> listData = new ArrayList<>();

    private int refreshTime = 0;

    private SwipeRefreshLayout swipeRefreshLayout;

    private final String[] gameNames = new String[]{"霸者大陆", "西游女儿国", "乱斗西游", "我是死神", "部落冲突"};

    private final String[] gameUrl = new String[]{"123", "abc", "bcd", "avc", "add"};

    private String firstUrl = "http://dl.x7sy.com/market/android/tg/x7market_tg_1.apk";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.lws_first_fragment;
    }

    @Override
    protected void initView(View rootView) {
        recyclerView = rootView.findViewById(R.id.first_recycler_view);
        swipeRefreshLayout = rootView.findViewById(R.id.refresh_layout);
        // 关闭下拉刷新
        swipeRefreshLayout.setEnabled(false);
        initAdapter();
    }

    private void initAdapter() {
        LinearLayoutManager manager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(manager);
        adapter = new FirstFragmentAdapter(getContext());
        // 初始化数据
        loadFirstRecyclerView();
        adapter.refresh(listData);
        recyclerView.setItemAnimator(null);
        // 自定义 ItemDecoration
        LinearItemDecoration decoration = new LinearItemDecoration();
        recyclerView.addItemDecoration(decoration);
        recyclerView.setAdapter(adapter);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (manager.findLastCompletelyVisibleItemPosition() == listData.size() - 1) {
                    // recyclerview 滑动到底部，更新数据
                    // 加载更多数据，如果总 item 到一定值停止加载
                    if (refreshTime >= 10) return;
                    swipeRefreshLayout.setRefreshing(true);
                    new Handler().postDelayed(() -> {
                        moreFirstRecyclerView();
                        adapter.refresh(listData);
                        adapter.notifyDataSetChanged();
                        swipeRefreshLayout.setRefreshing(false);
                    }, 1000);
                }
            }
        });
    }

    private void loadFirstRecyclerView() {
        refreshTime = 0;
        listData.clear();
        listData.add(new FirstViewData("小7手游", firstUrl, FirstItemButton.STATE_DOWNLOAD));
        for (int i = 0; i < 5; i++) {
            listData.add(new FirstViewData(gameNames[i], gameUrl[i], FirstItemButton.STATE_COMPLETE));
        }
    }

    private void moreFirstRecyclerView() {
        for (int i = 0; i < 5; i++) {
            refreshTime++;
            listData.add(new FirstViewData(gameNames[i], gameUrl[i], FirstItemButton.STATE_COMPLETE));
        }
    }
}
