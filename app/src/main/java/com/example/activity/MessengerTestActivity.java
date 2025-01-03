package com.example.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.widget.Toast;

import com.example.util.LogUtil;
import com.example.util.UiUtil;
import com.example.xiao7demo.BaseActivity;

/**
 * 测试 Messenger 通信
 *
 * @author laiweisheng
 * @date 2024/12/18
 */
public class MessengerTestActivity extends BaseActivity {

    /**
     *  客户端自己的 Messenger，用于接收服务端回复
     */
    private final Messenger clientMessenger = new Messenger(new IncomingHandler());

    /**
     * 服务端的 Messenger
     */
    private Messenger mServiceMessenger = null;


    boolean isBound = false;

    /**
     * ServiceConnection：管理服务的连接状态
     */
    private final ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LogUtil.i("Service connected");
            // 获取服务端 Messenger
            mServiceMessenger = new Messenger(service);
            isBound = true;

            // 发送消息给服务端
            sendMessageToService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            LogUtil.i("Service disconnected");
            mServiceMessenger = null;
            isBound = false;
        }
    };

    private static class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 2) {
                // 服务端的回复
                Bundle data = msg.getData();
                String vpnType = data.getString("vpn_type");
                boolean vpnConnecting = data.getBoolean("vpn_connecting");
                LogUtil.i("Reply from service: " + vpnConnecting);
                Toast.makeText(UiUtil.getContext(), "VPN 连接中：" + vpnType + "-" + vpnConnecting, Toast.LENGTH_SHORT).show();
            } else {
                super.handleMessage(msg);
            }
        }
    }

    /**
     * 发送消息给服务端
     */
    private void sendMessageToService() {
        if (!isBound) {
            return;
        }

        // 1 表示消息类型
        Message msg = Message.obtain(null, 1);
        Bundle data = new Bundle();
        data.putString("action", "get_vpn_status");
        msg.setData(data);

        // 设置 replyTo，用于接收服务端的回复
        msg.replyTo = clientMessenger;

        // 发送消息
        try {
            mServiceMessenger.send(msg);
        } catch (Exception e) {
            LogUtil.e(LogUtil.getErrorTrace(e));
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 使用显式 Intent 绑定服务端的 Service
        Intent intent = new Intent();
        intent.setComponent(new ComponentName(
                // 服务端应用的包名
                "com.smwl.x7vpn.plugin",
                // 服务端 Service 的完整类名
                "com.smwl.x7vpn.plugin.service.X7GameBoosterMessengerService"
        ));
        boolean bingResult = bindService(intent, connection, Context.BIND_AUTO_CREATE);
        LogUtil.i("Bind result: " + bingResult);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isBound) {
            unbindService(connection);
            isBound = false;
        }
    }
}
