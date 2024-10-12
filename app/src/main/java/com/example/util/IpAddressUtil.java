package com.example.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.bean.ip.BaiDuIpDataBean;
import com.example.bean.ip.CommonIpDataBean;
import com.example.bean.ip.IPIP_IpDataBean;
import com.example.bean.ip.IPStackIpDataBean;
import com.example.bean.ip.KloudendIpDataBean;
import com.example.bean.ip.TencentIpDataBean;
import com.example.interfce.IpDataObtain;
import com.example.interfce.OkHttpCallback;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * IP 获取工具
 *
 * @author laiweisheng
 * @date 2024/10/11
 */
public class IpAddressUtil {

    private static final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .build();
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();

    /**
     * 免费查询 IP 信息的接口 map
     */
    private static final Map<String, Class<?>> IP_CHECK_FREE_URL_MAP = new LinkedHashMap<>();

    /**
     * 初始化 IP 查询接口数据（优先适用国际的）
     */
    public static void initIpCheckUrlData() {
        IP_CHECK_FREE_URL_MAP.clear();
        // IPIP
        IP_CHECK_FREE_URL_MAP.put("https://myip.ipip.net/json", IPIP_IpDataBean.class);
        // Kloudend
        IP_CHECK_FREE_URL_MAP.put("https://ipapi.co/json", KloudendIpDataBean.class);
        // 腾讯
        IP_CHECK_FREE_URL_MAP.put("https://r.inews.qq.com/api/ip2city", TencentIpDataBean.class);
        // 百度
        IP_CHECK_FREE_URL_MAP.put("https://qifu-api.baidubce.com/ip/local/geo/v1/district", BaiDuIpDataBean.class);
        // IPStack
        IP_CHECK_FREE_URL_MAP.put("https://iplark.com/ipstack", IPStackIpDataBean.class);
    }

    /**
     * 检查是否连接到网络（Wi-Fi 或移动网络）
     */
    public static boolean isNetworkConnected(Context context) {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            return networkInfo != null && networkInfo.isConnected();
        }
        return false;
    }

    /**
     * 检查是否通过 Wi-Fi 连接
     */
    public static boolean isWifiConnected(Context context) {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo wifiInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            return wifiInfo != null && wifiInfo.isConnected();
        }
        return false;
    }

    /**
     * 检查是否通过移动网络连接
     */
    public static boolean isMobileConnected(Context context) {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo mobileInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            return mobileInfo != null && mobileInfo.isConnected();
        }
        return false;
    }

    /**
     * 获取本机 IPv4 地址
     */
    public static String getIpAddress(Context context) {
        if (null == context) {
            return getIpAddress();
        }
        if (isNetworkConnected(context)) {
            if (isWifiConnected(context)) {
                WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                if (wifiManager.isWifiEnabled()) {
                    // 已经开启了 WiFi
                    WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                    int ipAddress = wifiInfo.getIpAddress();
                    return intToIp(ipAddress);
                } else {
                    // 未开启 WiFi
                    return getIpAddress();
                }
            } else {
                return getIpAddress();
            }
        } else {
            Toast.makeText(context, "Please check if the current network is available", Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    private static String intToIp(int ipAddress) {
        return (ipAddress & 0xFF) + "." +
                ((ipAddress >> 8) & 0xFF) + "." +
                ((ipAddress >> 16) & 0xFF) + "." +
                (ipAddress >> 24 & 0xFF);
    }

    /**
     * 获取本机 IPv4 地址
     */
    private static String getIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()
                            && !inetAddress.isAnyLocalAddress()
                            && inetAddress instanceof Inet4Address) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取设备公网 IP
     */
    public static void getPublicIpAddress(Context context, @NonNull OkHttpCallback callback) {
        if (IP_CHECK_FREE_URL_MAP.isEmpty()) {
            initIpCheckUrlData();
        }
        if (!IpAddressUtil.isNetworkConnected(context)) {
            Toast.makeText(context, "Please check if the current network is available", Toast.LENGTH_SHORT).show();
            return;
        }
        executor.execute(() -> {
            String ip = "";
            for (String ipCheckUrl : IP_CHECK_FREE_URL_MAP.keySet()) {
                try {
                    Log.i("lai", "本次查询 IP 的接口：" + ipCheckUrl);
                    Request request = new Request.Builder()
                            .url(ipCheckUrl)
                            .get()
                            .build();
                    try (Response response = client.newCall(request).execute()) {
                        Log.i("lai", "本次查询 IP 的响应码：" + response.code());
                        if (response.isSuccessful() && response.body() != null) {
                            String responseBody = response.body().string();
                            Log.i("lai", "本次查询 IP 接口返回参数：" + responseBody);
                            IpDataObtain ipData = (IpDataObtain) JsonParseUtil.parse(responseBody, IP_CHECK_FREE_URL_MAP.get(ipCheckUrl));
                            if (ipData == null) {
                                continue;
                            }
                            ip = ipData.getIp();
                            String country = ipData.getCountry();
                            if (StringUtil.allIsNotEmpty(ip, country)) {
                                callback.onSuccess(ipData.getCommonIpDataJson());
                                return;
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (StringUtil.isEmpty(ip)) {
                callback.onFail("check current devices ip address fail");
            } else {
                callback.onSuccess(new CommonIpDataBean(ip).toString());
            }
        });
    }
}
