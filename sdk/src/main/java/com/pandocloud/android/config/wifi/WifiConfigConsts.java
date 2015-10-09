package com.pandocloud.android.config.wifi;

public class WifiConfigConsts {

	// 网关包头设置
	public static final short PACKET_START_DATA = 0x7064;

	public static final int PACKET_START_LEN = 2;

	public static final int PACKET_TYPE_LEN = 2;

	public static final int PACKET_LEN = 4;

	public static final int PACKET_TOTAL_LEN = PACKET_START_LEN + PACKET_TYPE_LEN + PACKET_LEN;
	
	/*
	// result
	public static final String MESSAGE_OK = "ok";
	
	public static final String MESSAGE_ERROR = "error";
	*/
	
	

	public static String DEVICE_HOST = "192.168.4.1";
	
	public static final int DEVICE_PORT = 8890;
	
	// 网关协议版本
	public static final String GATEWAY_VERSION = "0.1.0";
}
