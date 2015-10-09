package com.pandocloud.android.agent;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.android.service.MqttService;
import org.eclipse.paho.client.mqttv3.MqttClientPersistence;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

public class PandoService extends MqttService{

    private String TAG = "PandoService";

    private String serverUri = "tcp://120.24.241.177:1883";
    private String cliendId = "11";
    private String contextId = "ctxId";
    private String clientHandle = serverUri+":"+cliendId+":"+contextId;
    private String activityToken = "PandoService";
    private MqttConnectOptions conOpt = new MqttConnectOptions();

    private CallbackBroadcastReceiver callback = new CallbackBroadcastReceiver();

    public PandoService() {

    }

    public void setMessageHandler(Handler h) {

    }

    public String getAndroidDeviceKey() {
        String s = new String();
        return s;
    }

    public void connect() {
        Log.d(TAG, "do connect === ");
        getClient(serverUri, cliendId, contextId, (MqttClientPersistence)null);
        try {
            super.connect(clientHandle, conOpt, this.toString(), activityToken);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public class CallbackBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(TAG, action);
            if("MqttService.callbackToActivity.v0".equals(action)) {
                String ch = intent.getStringExtra("MqttService.clientHandle");
                Status sta = intent.getParcelableExtra("MqttService.callbackStatus");

                Bundle b = intent.getExtras();
                String at = b.getString("MqttService.activityToken");
                String ic = b.getString("MqttService.invocationContext");
                String act = b.getString("MqttService.callbackAction");

                Log.d(TAG, "clientHandle="+ch+", status="+sta+", activityToken="+at+", invocationContext="+ic+", callbackAction="+act);
            }
        }
    }

    public void onCreate() {
        Log.d(TAG, "PandoService created ===");
        super.onCreate();

        int timeout = Constants.defaultTimeOut;
        int keepAlive = Constants.defaultKeepAlive;
        conOpt.setConnectionTimeout(timeout);
        conOpt.setKeepAliveInterval(keepAlive);

        IntentFilter filter = new IntentFilter();
        filter.addAction("MqttService.callbackToActivity.v0");
        registerReceiver(callback, filter);

        connect();
    }

}
