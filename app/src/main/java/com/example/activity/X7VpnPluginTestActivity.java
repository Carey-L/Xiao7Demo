package com.example.activity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.widget.EditText;

import androidx.annotation.Nullable;

import com.example.R;
import com.example.util.StringUtil;
import com.example.xiao7demo.BaseActivity;

import java.nio.charset.StandardCharsets;

/**
 * x7gameBooster vpn 插件测试界面
 *
 * @author laiweisheng
 * @date 2024/12/19
 */
public class X7VpnPluginTestActivity extends BaseActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_x7vpn_plugin_test_layout);
            EditText vpnTypeEt = findViewById(R.id.vpn_type_et);
            vpnTypeEt.setInputType(InputType.TYPE_CLASS_NUMBER);
            findViewById(R.id.start_tv).setOnClickListener(v -> jumpToVpnPlugin(StringUtil.isEmpty(vpnTypeEt.getText().toString()) ? "2" : vpnTypeEt.getText().toString(), true));
            findViewById(R.id.close_tv).setOnClickListener(v -> jumpToVpnPlugin(StringUtil.isEmpty(vpnTypeEt.getText().toString()) ? "2" : vpnTypeEt.getText().toString(), false));
        } catch (Exception e) {
            Log.e("lws", e.toString());
        }
    }

    /**
     * 跳转到 VPN 插件
     *
     * @param vpnType  VPN 类型
     * @param startVpn 是否开启 VPN
     */

    private void jumpToVpnPlugin(String vpnType, boolean startVpn) {
        try {
            PackageManager packageManager = getPackageManager();
            Intent intent = packageManager.getLaunchIntentForPackage("com.smwl.x7vpn.plugin");
            if (intent != null) {
                Bundle bundle = new Bundle();
                bundle.putString("action", startVpn ? "com.smwl.x7vpn.plugin.vpn.ACTION_START_VPN" : "com.smwl.x7vpn.plugin.vpn.ACTION_STOP_VPN");
                // bundle.putString("action", "com.smwl.x7vpn.plugin.vpn.ACTION_UPLOAD_LOG");
                bundle.putString("vpnType", vpnType);
                bundle.putString("mid", "123456");
                bundle.putString("config", base64Encode("[Interface]\n" +
                        "PrivateKey = GJteeRj4Ewit3vhEq1v7lvzAoeznWlGUYlWrYh4h33o=\n" +
                        "Address = 10.8.0.6/24\n" +
                        "DNS = 1.1.1.1\n" +
                        "\n" +
                        "[Peer]\n" +
                        "PublicKey = 8bLSVGKdYEhxPnuWe94NvBpE6W/xQZXZxxd99aVT8xM=\n" +
                        "PresharedKey = c27c2MFmXCCANq3y8ZPviaqrWMrxdvu/nPLV9pnUhZc=\n" +
                        "AllowedIPs = 0.0.0.0/0\n" +
                        "PersistentKeepalive = 0\n" +
                        "Endpoint = 47.99.126.55:51830"));
                intent.putExtras(bundle);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                intent.setComponent(new ComponentName("com.smwl.x7vpn.plugin", "com.smwl.x7vpn.plugin.activity.MainActivity"));
                startActivity(intent);
            }
        } catch (Exception e) {
            Log.e("lws", e.toString());
        }
    }

    /**
     * Base64 编码（兼容低版本和高版本）
     */
    public static String base64Encode(String data) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return java.util.Base64.getEncoder().encodeToString(data.getBytes(StandardCharsets.UTF_8));
        } else {
            return android.util.Base64.encodeToString(data.getBytes(StandardCharsets.UTF_8), android.util.Base64.NO_WRAP);
        }
    }
}
