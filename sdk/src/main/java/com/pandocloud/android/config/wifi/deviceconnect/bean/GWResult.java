package com.pandocloud.android.config.wifi.bean;

import com.pandocloud.android.config.wifi.deviceconnect.interfaces.StringFormat;

import org.json.JSONException;
import org.json.JSONStringer;


public class GWResult implements StringFormat {

	public int code;
	
	public String message;

	@Override
	public String toJsonString() {
		try {
			return new JSONStringer().object().key("code").value(code).key("message").value(message).object().toString();
		} catch (JSONException e) {
			e.printStackTrace();
			return "{\"code\":"+ code +",\"message:" + message + "}";
		}
	}
}
