package com.pandocloud.android.api.interfaces;

public interface RequestListener {

	public void onPrepare();
	
	public void onSuccess();
	
	public void onFail(Exception e);
	
	public void onFinish();
}
