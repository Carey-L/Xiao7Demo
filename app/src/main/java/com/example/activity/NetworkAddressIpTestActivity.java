package com.example.activity;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkRequest;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.example.R;
import com.example.interfce.OkHttpCallback;
import com.example.util.IpAddressUtil;
import com.example.util.StringUtil;
import com.example.xiao7demo.BaseActivity;

/**
 * 测试获取设备 IP
 *
 * @author laiweisheng
 * @date 2024/10/11
 */
public class NetworkAddressIpTestActivity extends BaseActivity {

    private TextView currentIpTv;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_netword_address_ip_layout);
        currentIpTv = findViewById(R.id.current_ip_tv);
        initIpData();
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        connectivityManager.registerNetworkCallback(new NetworkRequest.Builder().build(), new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(Network network) {
                // 网络可用
                if (StringUtil.isEmpty(currentIpTv.getText().toString())) {
                    initIpData();
                }
            }

            @Override
            public void onLost(Network network) {
                // 网络断开
                Toast.makeText(NetworkAddressIpTestActivity.this, "The current network is unavailable", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initIpData() {
        IpAddressUtil.getPublicIpAddress(this, new OkHttpCallback() {
            @Override
            public void onSuccess(String result) {
                runOnUiThread(() -> {
                    if (StringUtil.isEmpty(currentIpTv.getText().toString())) {
                        currentIpTv.setText(result);
                    }
                });
            }

            @Override
            public void onFail(String result) {
                final String ipAddress = IpAddressUtil.getIpAddress(NetworkAddressIpTestActivity.this);
                runOnUiThread(() -> {
                    if (StringUtil.isEmpty(ipAddress)) {
                        Toast.makeText(NetworkAddressIpTestActivity.this, "Please check if the current network is available", Toast.LENGTH_SHORT).show();
                    } else {
                        if (StringUtil.isEmpty(currentIpTv.getText().toString())) {
                            currentIpTv.setText(result);
                        }
                    }
                });
            }
        });
    }
}
