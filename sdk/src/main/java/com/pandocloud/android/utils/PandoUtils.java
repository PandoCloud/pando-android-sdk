package com.pandocloud.android.utils;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;

public class PandoUtils {
	
	private static String getLocalMacAddress(Context context) {
		WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		WifiInfo info = wifi.getConnectionInfo();
		if (info != null) {
			return info.getMacAddress();
		}
		return "";
	}
	
	public static final String getDeviceCode(Context context) {
		TelephonyManager telManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		if (telManager != null) {
			return telManager.getDeviceId();
		} 
		return getLocalMacAddress(context);
	}
}
