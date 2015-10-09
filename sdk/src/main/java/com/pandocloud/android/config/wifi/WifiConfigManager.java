package com.pandocloud.android.config.wifi;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;


public final class WifiConfigManager {
	// 返回结果
	public static final int CONFIG_SUCCESS = 0;

	public static final int CONFIG_FAILED = -1;
	
	public static final int DEVICE_CONNECT_FAILED = -2;
	
	public static final int DEVICE_SEND_FAILED = -3;

	public static final int DEVICE_RECV_FAILED = -4;

	public static final int CONFIG_TIMEOUT = -5;

	// 配置模式
	public static final String CONFIG_MODE_HOTSPOT = "hotspot";

	public static final String CONFIG_MODE_SMARTLINK = "smartlink";


	public static Context getContext() {
		return context;
	}

	private static Context context = null;

	private static WifiConfigMessageHandler msgHandler = null;

	/**
	 * 设置配置结果的回调
	 * @param handler 配置结果回调处理器
	 */
	public static void setMsgHandler(WifiConfigMessageHandler handler) {
		msgHandler = handler;
	}

	public static WifiConfigMessageHandler getMsgHandler(){
		return msgHandler;
	}

	/**
	 * 启动配置模式
	 * @param context 传入当前activity的context
	 * @param mode 当前配置模式
	 * @param ssid 需要连接的wifi名
	 * @param pwd 对应的wifi密码
	 */
	public static void startConfig(Context context, String mode, String ssid, String pwd) {
		Log.d("pandocloud", "entering config mode "+mode+", ssid: "+ssid+", password: "+pwd);
		if(msgHandler == null){
			Log.e("pandocloud", "config message handler is not set!");
			return;
		}
		WifiConfigManager.context = context.getApplicationContext();
		Intent intent = new Intent(context, WifiConfigService.class);
		intent.putExtra("mode", mode);
		intent.putExtra("ssid", ssid);
		intent.putExtra("wifi_pwd", pwd);
		context.startService(intent);
	}

	/**
	 * 退出配置模式
	 */
    public static void stopConfig() {
    	if (context == null) {
			return;
		}
		Intent intent = new Intent(WifiConfigService.STOP_CONNECT_DEVICE_ACTION);
		LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
	}
}
