package com.example.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.example.R;
import com.example.util.StringUtil;
import com.example.xiao7demo.BaseActivity;

import java.net.InetAddress;

/**
 * 域名解析测试界面
 *
 * @author laiweisheng
 * @date 2025/1/14
 */
public class DomainResolveTestActivity extends BaseActivity {

    private TextView resolveResultTv;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_domain_resolve_test_layout);
            resolveResultTv = findViewById(R.id.resolve_result_tv);
            EditText urlEt = findViewById(R.id.url_et);
            TextView resolveStartTv = findViewById(R.id.resolve_start_tv);
            resolveStartTv.setOnClickListener(v -> {
                String url = urlEt.getText().toString();
                if (StringUtil.isEmpty(url)) {
                    return;
                }
                resolveDomain(url);
            });
        } catch (Exception e) {
            Log.e("lws", e.toString());
        }
    }

    private void resolveDomain(String url) {
        try {
            ThreadLocal<InetAddress> inetAddressThreadLocal = new ThreadLocal<>();
            Thread thread = new Thread(() -> {
                try {
                    InetAddress inetAddress = InetAddress.getByName(getDomain(url));
                    inetAddressThreadLocal.set(inetAddress);
                    resolveResultTv.post(() -> {
                        resolveResultTv.setText(inetAddress.getHostAddress());
                    });
                } catch (Exception e) {
                    Log.e("lws", e.toString());
                }
            });
            thread.start();
        } catch (Exception e) {
            Log.e("lws", e.toString());
        }
    }

    private static String getDomain(String url) {
        if (TextUtils.isEmpty(url)) {
            return "";
        }
        String domain = url;
        if (url.startsWith("http://")) {
            domain = url.substring(7);
        } else if (url.startsWith("https://")) {
            domain = url.substring(8);
        }
        if (domain.contains("/")) {
            domain = domain.substring(0, domain.indexOf("/"));
        }
        return domain;
    }
}
