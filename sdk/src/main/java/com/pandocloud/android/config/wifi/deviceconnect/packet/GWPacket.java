package com.pandocloud.android.config.wifi.deviceconnect.packet;


import com.pandocloud.android.config.wifi.deviceconnect.interfaces.OnPacketHandler;

public abstract class GWPacket<T> implements OnPacketHandler<T> {
	
	private T objT;
	
	public GWPacket(T t) {
		this.objT = t;
	}
	
	public static final int MSG_SSID_PWD = 1;
	
	public static final int MSG_CHECK_SSID = 2;
	
	public static final int MSG_REQUEST_TOKEN = 3;
	
	public static final int MSG_EXIT_CONFIG = 4;
	
	private int msgType = MSG_SSID_PWD;
	
	public void setMsgType(int msgType) {
		this.msgType = msgType;
	}
	
	public int getMsgType() {
		return msgType;
	}

	public T getTEntity() {
		return objT;
	}
	public abstract byte[] pack();
	
	public abstract T unPack(byte[] data);
}
