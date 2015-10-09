package com.pandocloud.android.config.wifi.deviceconnect.packet;

import com.pandocloud.android.config.wifi.WifiConfigConsts;
import com.pandocloud.android.config.wifi.deviceconnect.bean.GWAction;

import java.nio.ByteBuffer;

import org.json.JSONException;
import org.json.JSONObject;



public class GWActionPacket extends GWPacket<GWAction>{

	public GWActionPacket(GWAction t) {
		super(t);
	}

	@Override
	public byte[] pack() {
		GWAction action = getTEntity();
		if (action == null) {
			throw new IllegalStateException("wifi info is null...");
		}
		String wifiJson = action.toJsonString();
		byte[] packetData = wifiJson.getBytes();
		int packetLen = WifiConfigConsts.PACKET_START_LEN + WifiConfigConsts.PACKET_TYPE_LEN
				+ WifiConfigConsts.PACKET_LEN + packetData.length;
		ByteBuffer byteBuffer = ByteBuffer.allocate(packetLen);
		byteBuffer.putShort(WifiConfigConsts.PACKET_START_DATA);
		byteBuffer.putShort((short) 0);
		byteBuffer.putInt(packetData.length);
		byteBuffer.put(packetData);
		byteBuffer.flip();
		byte[] bytes = new byte[byteBuffer.remaining()]; 
		byteBuffer.get(bytes);
		return bytes;
	}


	@Override
	public GWAction unPack(byte[] data) {
		ByteBuffer byteBuffer = ByteBuffer.wrap(data);
		int packetStart = byteBuffer.getShort();
		if (packetStart == WifiConfigConsts.PACKET_START_DATA) {
			int length = byteBuffer.getInt(WifiConfigConsts.PACKET_START_LEN + WifiConfigConsts.PACKET_TYPE_LEN);
			byte[] byteData = new byte[length];
			System.arraycopy(data, WifiConfigConsts.PACKET_START_LEN + WifiConfigConsts.PACKET_TYPE_LEN
					+ WifiConfigConsts.PACKET_LEN, byteData, 0, length);
			String jsonData = new String(byteData);
			try {
				JSONObject jsonObject = new JSONObject(jsonData);
				GWAction gwAction = new GWAction();
				gwAction.action = jsonObject.getString("action");
				return gwAction;
			} catch (JSONException e) {
				e.printStackTrace();
				return null;
			}
		}
		return null;
	}
	
}
