package com.pandocloud.android.config.wifi;


import android.os.Handler;
import android.os.Message;

public class WifiConfigMessageHandler {

    private Handler mHandler;


    public WifiConfigMessageHandler(Handler handler) {
        this.mHandler = handler;
    }


    public void sendMessage(Message msg) {
        this.mHandler.sendMessage(msg);
    }

}