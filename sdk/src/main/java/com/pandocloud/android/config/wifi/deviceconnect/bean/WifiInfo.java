package com.pandocloud.android.config.wifi.deviceconnect.bean;

import com.pandocloud.android.config.wifi.deviceconnect.interfaces.StringFormat;

import org.json.JSONException;
import org.json.JSONStringer;


public class WifiInfo implements StringFormat {
	
	public String ssid;
	
	public String password;
	
	public String action;
	
	@Override
	public String toJsonString() {
		try {
			return new JSONStringer().object()
						.key("ssid").value(ssid)
						.key("password").value(password)
						.key("action").value(action)
						.endObject().toString();
		} catch (JSONException e) {
			e.printStackTrace();
			return "";
		}
	}


	@Override
	public String toString() {
		return "WifiInfo [ssid=" + ssid + ", password=" + password
				+ ", action=" + action + "]";
	}
	
}
