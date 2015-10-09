package com.pandocloud.android.config.wifi.deviceconnect.interfaces;


public interface OnPacketHandler<T> {

	public byte[] pack();
	
	public T unPack(byte[] data);
}
