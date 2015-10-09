package com.pandocloud.android.config.wifi;


import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;


import com.pandocloud.android.config.wifi.deviceconnect.DeviceConnect;
import com.pandocloud.android.config.wifi.smartconfig.SmartConfig;


public class WifiConfigService extends Service {

	public static final String STOP_CONNECT_DEVICE_ACTION = "com.pandocloud.android.STOP_WIFI_CONFIG_ACTION";

	private String ssid;
	private String password;
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	
	@Override
	public void onCreate() {
		super.onCreate();
		LocalBroadcastManager.getInstance(this)
			.registerReceiver(stopConnectReceiver, new IntentFilter(STOP_CONNECT_DEVICE_ACTION));
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.i("pandocloud", "wifi config service started...");
		handleServiceStart(intent);
		
		return Service.START_STICKY;
	}
	
	
	public void handleServiceStart(Intent intent) {
		if (intent == null) {
			return;
		}
		Bundle bundle = intent.getExtras();
		if (bundle != null) {
			String mode = bundle.getString("mode");
			ssid = bundle.getString("ssid");
			password = bundle.getString("wifi_pwd");
			switch (mode){
				case WifiConfigManager.CONFIG_MODE_SMARTLINK:
					smartConfig();
					break;
				case WifiConfigManager.CONFIG_MODE_HOTSPOT:
					connectAndConfigDeviceHotspotMode();
					break;
				default:
					Log.e("pandocloud", "unknown config mode: " + mode);
					break;
			}

		}
		
	}
	
	BroadcastReceiver stopConnectReceiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.i("pandocloud", "wifi config service stopped.");
			WifiConfigService.this.stopSelf();
		}
	};

	private void connectAndConfigDeviceHotspotMode() {
		DeviceConnect dc = new DeviceConnect(this, WifiConfigManager.CONFIG_MODE_HOTSPOT);
		dc.setWifiInfo(ssid, password);
		dc.setMsgHandler(WifiConfigManager.getMsgHandler());
		dc.conn();
	}

	private void smartConfig() {
		SmartConfig sc = new SmartConfig(ssid, password);
		sc.setMsgHandler(WifiConfigManager.getMsgHandler());
		sc.start();
	}
	
	public void onDestroy() {
		super.onDestroy();
		
		LocalBroadcastManager.getInstance(this)
			.unregisterReceiver(stopConnectReceiver);
	}
}
