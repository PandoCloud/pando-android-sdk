package com.pandocloud.android.api;


import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

public class DeviceState {
	
	public static final String DEVICE_PREFS_NAME = "pando_device_prefs";

	public static final String ACCESS_TOKEN = "access_token";
	
	public static final String ACCESS_ADDR = "access_addr";
	
	public static final String DEVICE_ID = "device_id";
	
	//数据消息序列号
	public static final String DATA_SEQUENCE = "data_sequence";
	
	//事件消息序列号
	public static final String EVENT_SEQUENCE = "event_sequence";
	
	//与平台时间同步
	public static final String CLOCK_TIME = "clock_time";
	
	public static final String ELAPSED_REAL_TIME = "elapsedRealtime";
	
	private static DeviceState sInstances;
	
	private SharedPreferences prefs;
	
	private int deviceId;
	
	private String token;
	
	private long elaspsedRelaTime;
	
	/**
	 * 事件命令
	 */
	private long eventSequence;
	/**
	 * 数据
	 */
	private long dataSequence;
	
	public void init(Context context) {
		if (prefs == null) {
			prefs = context.getApplicationContext()
					.getSharedPreferences(DEVICE_PREFS_NAME, Context.MODE_PRIVATE);
		}
	}
	
	private DeviceState(Context context) {
		init(context);
	}
	
	public static DeviceState getInstances(Context context) {
		if (sInstances == null) {
			synchronized (DeviceState.class) {
				if (sInstances == null) {
					sInstances = new DeviceState(context);
				}
			}
		}
		return sInstances;
	}
	
	
	private SharedPreferences getSharedPreferences() {
		return prefs;
	}
	
	public String getAccessToken(String defValue) {
		if (TextUtils.isEmpty(token)) {
			token = prefs.getString(DeviceState.ACCESS_TOKEN, defValue);
		}
		return token;
	}
	
	public int getIntValue(String key, int defValue) {
		return prefs.getInt(key, defValue);
	}
	
	public void saveIntValue(String key, int value) {
		prefs.edit().putInt(key, value).commit();
	}
	
	public void saveLongValue(String key, long value) {
		prefs.edit().putLong(key, value).commit();
	}
	
	public long getLongValue(String key, long defValue) {
		if (ELAPSED_REAL_TIME.equals(key)) {
			if (elaspsedRelaTime == 0) {
				elaspsedRelaTime = prefs.getLong(key, defValue);
			}
			return elaspsedRelaTime;
		}
		return prefs.getLong(key, defValue);
	}
	
	/**
	 * <p>if has access token return true</p>
	 * <p>else return false</P>
	 * @return
	 */
	public boolean hasAccessToken() {
		if (TextUtils.isEmpty(token)) {
			token = prefs.getString(DeviceState.ACCESS_TOKEN, "");
			if (TextUtils.isEmpty(token)) {
				return false;
			}
		}
		return true;
	}
	
	public int getDeviceId() {
		if (deviceId <= 0) {
			deviceId = prefs.getInt(DeviceState.DEVICE_ID, 0);
		}
		return deviceId;
	}
	
	
	public long getEventSequence() {
		if (eventSequence == 0) {
			eventSequence = prefs.getLong(EVENT_SEQUENCE, 1);
		} else {
			eventSequence ++;
			prefs.edit().putLong(EVENT_SEQUENCE, eventSequence);
		}
		return eventSequence;
	}
	
	
	public long getDataSequence() {
		if (dataSequence == 0) {
			dataSequence = prefs.getLong(DATA_SEQUENCE, 1);
		} else {
			dataSequence ++;
			prefs.edit().putLong(EVENT_SEQUENCE, dataSequence);
		}
		return dataSequence;
	}
	
	
	/**
	 * 判断设备配置信息是否含有key值
	 * @param key
	 * @return
	 */
	public boolean containsKey(String key) {
		return prefs.contains(key);
	}
	
	/**
	 * 清楚所有设备配置信息
	 */
	public void clear(){
		deviceId = -1;
		token = null;
		prefs.edit().clear().commit();
	}
	
	public static class Builder {
		
		private SharedPreferences.Editor mEditor;
		
		public Builder(Context context) {
			if (mEditor == null) {
				mEditor = DeviceState.getInstances(context).getSharedPreferences().edit();
			}
		}
		
		public Builder saveString(String key, String value) {
			mEditor.putString(key, value);
			return this;
		}
		
		public Builder saveLong(String key, long value) {
			mEditor.putLong(key, value);
			return this;
		}
		
		public Builder saveInt(String key, int value) {
			mEditor.putInt(key, value);
			return this;
		}
		
		public Builder saveBoolean(String key, boolean value) {
			mEditor.putBoolean(key, value);
			return this;
		}
		
		public Builder saveFloat(String key, float value) {
			mEditor.putFloat(key, value);
			return this;
		}
		
		public void commit(){
			mEditor.commit();
		}
	}
}
