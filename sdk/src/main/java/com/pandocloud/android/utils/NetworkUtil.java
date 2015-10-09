package com.pandocloud.android.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NetworkUtil {
	
	/** 检查是否有网络 */
	public static boolean isNetworkAvailable(Context context) {
		NetworkInfo info = getNetworkInfo(context);
		if (info != null) {
			return info.isAvailable();
		}
		return false;
	}

	/** 检查是否是WIFI */
	public static boolean isWifi(Context context) {
		NetworkInfo info = getNetworkInfo(context);
		if (info != null) {
			if (info.getType() == ConnectivityManager.TYPE_WIFI)
				return true;
		}
		return false;
	}

	/** 检查是否是移动网络 */
	public static boolean isMobile(Context context) {
		NetworkInfo info = getNetworkInfo(context);
		if (info != null) {
			if (info.getType() == ConnectivityManager.TYPE_MOBILE)
				return true;
		}
		return false;
	}

	private static NetworkInfo getNetworkInfo(Context context) {
		ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
		return cm.getActiveNetworkInfo();
	}
	
	public static byte[] toIPByteArray(int addr){
        return new byte[]{(byte)addr,(byte)(addr>>>8),(byte)(addr>>>16),(byte)(addr>>>24)};
    }
}
