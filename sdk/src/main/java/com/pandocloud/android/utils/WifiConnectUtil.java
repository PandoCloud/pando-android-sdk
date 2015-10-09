package com.pandocloud.android.utils;


import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.TextUtils;
import android.util.Log;

public class WifiConnectUtil {

	WifiManager wifiManager;

	// 定义几种加密方式，一种是WEP，一种是WPA，还有没有密码的情况
	public enum WifiCipherType {
		WIFICIPHER_WEP, WIFICIPHER_WPA, WIFICIPHER_NOPASS, WIFICIPHER_INVALID
	}

	// 构造函数
	public WifiConnectUtil(WifiManager wifiManager) {
		this.wifiManager = wifiManager;
	}

	public static void enableAllAps(final Context ctx) {
		if (ctx == null) {
			return;
		}
		final WifiManager wifiMgr = (WifiManager)ctx.getSystemService(Context.WIFI_SERVICE);
		final List<WifiConfiguration> configurations = wifiMgr.getConfiguredNetworks();
		if(configurations != null) {
			for(final WifiConfiguration config:configurations) {
				wifiMgr.enableNetwork(config.networkId, false);
			}
		}
	}
	
	public String getCurrentWifiSSID(String tag) {
		WifiInfo wifiInfo = wifiManager.getConnectionInfo();
		Log.e(tag, wifiInfo.toString());
		String wifiSSID = wifiInfo.getBSSID();
		if (wifiSSID == null) {
			return "unknow";
		}
		wifiSSID = wifiInfo.getSSID();
		if (!TextUtils.isEmpty(wifiSSID)) {
			if (wifiSSID.startsWith("\"")) {
				wifiSSID = wifiSSID.replaceAll("\"", "");
			}
			return wifiSSID;
		} else {
			return "unknow";
		}
	}
	
	/**
	 * 开启wifi AP
	 * @param mSSID
	 * @param mPasswd
	 */
	public void stratWifiAp(String mSSID, String mPasswd) {
		Method method1 = null;
		try {
			method1 = wifiManager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, boolean.class);
			WifiConfiguration netConfig = new WifiConfiguration();
			netConfig.SSID = mSSID;
			netConfig.preSharedKey = mPasswd;
			netConfig.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
			netConfig.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
			netConfig.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
			netConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
			netConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
			netConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
			netConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
			netConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);

			method1.invoke(wifiManager, netConfig, true);

		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 关闭wifi AP共享
	 */
	public void closeWifiAp() {
		//if (wifiManager.isWifiEnabled())
		{
			try {
				Method method = wifiManager.getClass().getMethod("getWifiApConfiguration");
				method.setAccessible(true);
				WifiConfiguration config = (WifiConfiguration) method.invoke(wifiManager);
				Method method2 = wifiManager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class,
						boolean.class);
				method2.invoke(wifiManager, config, false);
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
		}
	}

	// 打开wifi功能
	public boolean OpenWifi() {
		boolean bRet = true;
		if (!wifiManager.isWifiEnabled()) {
			bRet = wifiManager.setWifiEnabled(true);
		}
		return bRet;
	}

	// 提供一个外部接口，传入要连接的无线网
	public boolean Connect(String SSID, String password, WifiCipherType Type) {
		if (!this.OpenWifi()) {
			return false;
		}
		// 开启wifi功能需要一段时间(我在手机上测试一般需要1-3秒左右)，所以要等到wifi
		// 状态变成WIFI_STATE_ENABLED的时候才能执行下面的语句
		while (wifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLING) {
			try {
				// 为了避免程序一直while循环，让它睡个100毫秒在检测……
				Thread.currentThread();
				Thread.sleep(100);
			} catch (InterruptedException ie) {
			}
		}

		WifiConfiguration wifiConfig = this.CreateWifiInfo(SSID, password, Type);
		//
		if (wifiConfig == null) {
			return false;
		}

		WifiConfiguration tempConfig = this.IsExsits(SSID);
		if (tempConfig != null) {
			wifiManager.removeNetwork(tempConfig.networkId);
		}
		
		int netID = wifiManager.addNetwork(wifiConfig);
		return wifiManager.enableNetwork(netID, true);
	}

	// 查看以前是否也配置过这个网络
	public WifiConfiguration IsExsits(String SSID) {
		List<WifiConfiguration> existingConfigs = wifiManager.getConfiguredNetworks();
		if (existingConfigs == null) {
			return null;
		}
		for (WifiConfiguration existingConfig : existingConfigs) {
			if (existingConfig.SSID.equals("\"" + SSID + "\"")) {
				return existingConfig;
			}
		}
		return null;
	}
	
	
	/**
	 * 配置连接
	 * @param SSID
	 * @param Password
	 * @param Type
	 * @return
	 */
	private WifiConfiguration CreateWifiInfo(String SSID, String Password, WifiCipherType Type) {
		WifiConfiguration config = new WifiConfiguration();
		config.allowedAuthAlgorithms.clear();
		config.allowedGroupCiphers.clear();
		config.allowedKeyManagement.clear();
		config.allowedPairwiseCiphers.clear();
		config.allowedProtocols.clear();
		config.SSID = "\"" + SSID + "\"";
		if (Type == WifiCipherType.WIFICIPHER_NOPASS) {
			config.hiddenSSID = true;
//			config.wepKeys[0] = "\"" + "\"";
			config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
//			config.wepTxKeyIndex = 0;
			   
		} else if (Type == WifiCipherType.WIFICIPHER_WEP) {
			//	config.preSharedKey = "\"" + Password + "\"";
			config.hiddenSSID = true;
			config.wepKeys[0] = "\"" + Password + "\"";
			config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
			config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
			config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
			config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
			config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
			config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
			config.wepTxKeyIndex = 0;
		} else if (Type == WifiCipherType.WIFICIPHER_WPA) {
			config.preSharedKey = "\"" + Password + "\"";
			config.status = WifiConfiguration.Status.ENABLED;
		} else {
			return null;
		}
		return config;
	}

}
