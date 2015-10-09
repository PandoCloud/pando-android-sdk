package com.pandocloud.android.config.wifi.deviceconnect.bean;

import com.pandocloud.android.config.wifi.deviceconnect.interfaces.StringFormat;

import org.json.JSONException;
import org.json.JSONStringer;


public class GWToken implements StringFormat {
	
	public String token;

	@Override
	public String toJsonString() {
		try {
			return new JSONStringer().object()
					.key("token").value(token)
					.endObject().toString();
		} catch (JSONException e) {
			e.printStackTrace();
			return "{\"token\":" + token + "}";
		}
	}
}
