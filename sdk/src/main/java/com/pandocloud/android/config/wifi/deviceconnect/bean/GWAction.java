package com.pandocloud.android.config.wifi.deviceconnect.bean;

import com.pandocloud.android.config.wifi.deviceconnect.interfaces.StringFormat;

import org.json.JSONException;
import org.json.JSONStringer;


public class GWAction implements StringFormat {
	
	public String action;

	@Override
	public String toJsonString() {
		try {
			return new JSONStringer().object().key("action").value(action).endObject().toString();
		} catch (JSONException e) {
			e.printStackTrace();
			return "{\"action\":" + action + "}";
		}
	}
}
