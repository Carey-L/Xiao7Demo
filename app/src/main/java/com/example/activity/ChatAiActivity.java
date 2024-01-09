package com.example.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.example.R;

/**
 * chatAI WebView 界面
 *
 * @author laiweisheng
 * @date 2024/1/5
 */
public class ChatAiActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_ai);
        WebView chatWebView = findViewById(R.id.chat_webview);
        chatWebView.getSettings().setJavaScriptEnabled(true);
        if (!isWeChatInstalled(this)) {
            // 没有微信则修改设备标识
            chatWebView.getSettings().setUserAgentString("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/88.0.4324.182 Safari/537.36");
        }
        chatWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.startsWith("weixin://")) {
                    try {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        startActivity(intent);
                    } catch (Exception e) {
                        e.printStackTrace();
                        ChatAiActivity.this.runOnUiThread(() -> Toast.makeText(ChatAiActivity.this, "请安装微信", Toast.LENGTH_SHORT).show());
                    }
                    return true;
                }
                return super.shouldOverrideUrlLoading(view, url);
            }
        });
        chatWebView.loadUrl("https://chat.14x.cn");
    }

    /**
     * 判断是否安装了微信
     */
    private boolean isWeChatInstalled(Context context) {
        PackageManager pm = context.getPackageManager();
        try {
            pm.getPackageInfo("com.tencent.mm", PackageManager.GET_ACTIVITIES);
            Log.i("lws", "设备安装了微信");
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            Log.i("lws", "设备未安装微信");
            return false;
        }
    }
}