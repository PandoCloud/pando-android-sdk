package com.pandocloud.android.config.wifi.deviceconnect;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.LinkedBlockingQueue;

import org.json.JSONObject;

import android.content.Context;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;


import com.pandocloud.android.config.wifi.WifiConfigConsts;
import com.pandocloud.android.config.wifi.WifiConfigManager;
import com.pandocloud.android.config.wifi.WifiConfigMessageHandler;
import com.pandocloud.android.config.wifi.deviceconnect.bean.GWAction;
import com.pandocloud.android.config.wifi.deviceconnect.bean.WifiInfo;
import com.pandocloud.android.config.wifi.deviceconnect.packet.GWActionPacket;
import com.pandocloud.android.config.wifi.deviceconnect.packet.GWPacket;
import com.pandocloud.android.config.wifi.deviceconnect.packet.WifiInfoPacket;
import com.pandocloud.android.utils.LogUtils;
import com.pandocloud.android.utils.NetworkUtil;


public class DeviceConnect {
	
	private static final String TAG = "DeviceConnect";

	public static final String ACTION_CONFIG = "config";

	public static final String ACTION_CHECK_CONFIG = "check_config";

	public static final String ACTION_EXIT_CONFIG = "exit_config";

	public static final String ACTION_TOKEN = "token";

	private final int SOCK_STATE_OPEN = 1;// socket打开
	private final int SOCK_STATE_CLOSE = 1 << 1;// socket关闭
	private final int SOCK_STATE_CONNECT_START = 1 << 2;// 开始连接server
	private final int SOCK_STATE_CONNECT_SUCCESS = 1 << 3;// 连接成功
	private final int SOCK_STATE_CONNECT_FAILED = 1 << 4;// 连接失败
	private final int SOCK_STATE_CONNECT_WAIT = 1 << 5;// 等待连接

	private enum ConfigState{START, CHECK_SSID, SSID_PWD_SENT, SUCCESS};
	
	private int SockState = SOCK_STATE_CONNECT_START;

	private int TimeoutSeconds = 30; // 30s 超时
	
	private WifiConfigMessageHandler msgHandler;
	
	private Context context;
	
	private WifiInfo wifiInfo;
	
	private Socket socket;
	
	private OutputStream outStream = null;
	private InputStream inStream = null;

	private Thread conn = null;
	private Thread send = null;
	private Thread rec = null;
	
	private final Object lock = new Object();
	
	private boolean complete = false;
	
	private ConfigState configState = ConfigState.START;
	
	private LinkedBlockingQueue<GWPacket> requestQueen = new LinkedBlockingQueue<GWPacket>();

	private Timer timer;
	
	private String tokenValue;
	
	private long lastReceiveMilliscond = 0;

	
	public DeviceConnect(Context context, String mode) {

		this.context = context;
		if (mode.equals(WifiConfigManager.CONFIG_MODE_SMARTLINK)){
			// ssid and pwd is aleady got in smart config
			this.configState = ConfigState.SSID_PWD_SENT;
			this.complete = false;
		}
	}

	/*
	
	private static DeviceConnect sInstances;
	
	public static DeviceConnect getInstances(Context context, String mode) {
		if (sInstances == null) {
			synchronized (DeviceConnect.class) {
				if (sInstances == null) {
					sInstances = new DeviceConnect(context, mode);
				}
			}
		}
		return sInstances;
	}
	*/

	public void setWifiInfo(String ssid, String password) {
		WifiInfo wifiInfo = new WifiInfo();
		wifiInfo.ssid = ssid;
		wifiInfo.password = password;
		this.wifiInfo = wifiInfo;
		this.configState = ConfigState.START;
		this.complete = false;
		if (timer != null) {
			timer.cancel();
			timer = null;
		}
		timer = new Timer();
		timer.schedule(new CheckTask(), 5000, 2000);
	}
	
	class CheckTask extends TimerTask {

		@Override
		public void run() {
//			if (socket == null || state == STATE_CLOSE || !socket.isConnected()) {
//			if (!isSocketConnected()) {
				conn();
//			}
				
//			}
		}
	}
	
	/**
	 * 处理消息Handler
	 * @param handler
	 */
	public void setMsgHandler(WifiConfigMessageHandler handler) {
		this.msgHandler = handler;
	}
	
	public synchronized boolean isSocketConnected() {
		return (SockState == SOCK_STATE_CONNECT_SUCCESS)
				&& (null != send && send.isAlive())
				&& (null != rec && rec.isAlive());
	}
	
	public int send(GWPacket packet) {
		if (isSocketConnected()) {
			requestQueen.add(packet);
//			synchronized (lock) {
//				lock.notifyAll();
//			}
			return 1;
		} else {
			conn();
		}
		return 0;
	}
	
	
	private long lastConnTime = 0;
	public synchronized void conn() {
		if (System.currentTimeMillis() - lastConnTime < 2000) {
			return;
		}
		if (SockState == SOCK_STATE_OPEN || complete) {
			return;
		}
		lastConnTime = System.currentTimeMillis();

		close();
		SockState = SOCK_STATE_OPEN;
		
		LogUtils.d("begin socket connection...");
		conn = new Thread(new Conn());
		conn.start();
	}
	
	
	private class Conn implements Runnable {
		
		@Override
		public void run() {
			LogUtils.e(TAG, "Conn :Start");
			try {
				long startTimeSecond = System.currentTimeMillis() / 1000;
				long currentTimeSecond;
				while (SockState != SOCK_STATE_CLOSE) {
					currentTimeSecond = System.currentTimeMillis() / 1000;
					if ((currentTimeSecond - startTimeSecond) > TimeoutSeconds) {
						Message msg = Message.obtain();
						msg.what = WifiConfigManager.CONFIG_TIMEOUT;
						msgHandler.sendMessage(msg);
						break;
					}
					try {
						SockState = SOCK_STATE_CONNECT_START;
						socket = new Socket();
						socket.connect(new InetSocketAddress(WifiConfigConsts.DEVICE_HOST, WifiConfigConsts.DEVICE_PORT), 20 * 1000);
						socket.setKeepAlive(true);
						socket.setTcpNoDelay(true);
						SockState = SOCK_STATE_CONNECT_SUCCESS;
					} catch (Exception e) {
						e.printStackTrace();
						SockState = SOCK_STATE_CONNECT_FAILED;
						LogUtils.d("socket connect exception: ", e);
					}
					
					if (SockState == SOCK_STATE_CONNECT_SUCCESS) {
						
						try {
							outStream = socket.getOutputStream();
							inStream = socket.getInputStream();
							
							send = new Thread(new Send());
							rec = new Thread(new Rec());

							lastReceiveMilliscond = 0;
							requestQueen.clear();
							send.start();
							rec.start();
						} catch (IOException e) {
							e.printStackTrace();
							SockState = SOCK_STATE_CONNECT_FAILED;
                            LogUtils.d("socket connect IOException: ", e);
						} finally {
							if (SockState == SOCK_STATE_CONNECT_SUCCESS && socket.isConnected()) {
								LogUtils.e("blocker", "ap socket connect success...");
                                LogUtils.d("socket connect success...");
								if (configState == ConfigState.START) {
									wifiInfo.action = DeviceConnect.ACTION_CONFIG;
									WifiInfoPacket wifiInfoPacket = new WifiInfoPacket(wifiInfo);
									int length = send(wifiInfoPacket);
									if (length > 0) {
										configState = ConfigState.CHECK_SSID;
                                        LogUtils.d("ap send wifiInfo packet success...");
									} else {
                                        LogUtils.d("ap socket is't connected, send wifiInfo packet failed...");
									}
								} else if(configState == ConfigState.CHECK_SSID) {
									sendSSIDCheckConfig();
									
								} else if (configState == ConfigState.SSID_PWD_SENT) {
									sendGateWayToken();
								} else if (configState == ConfigState.SUCCESS) {
									sendExitConfig();
								}
							}
						}
						break;
					} else {
						SockState = SOCK_STATE_CONNECT_WAIT;
						// 如果有网络没有连接上，则定时取连接，没有网络则直接退出
						if (NetworkUtil.isNetworkAvailable(context)) {
							try {
								Thread.sleep(5 * 1000);
							} catch (InterruptedException e) {
								e.printStackTrace();
								break;
							}
						} else {
                            LogUtils.d("ap socket connect failed, no network...");
							SockState = SOCK_STATE_CONNECT_FAILED;
							if (msgHandler != null) {
								Message msg = Message.obtain();
								msg.what = WifiConfigManager.DEVICE_CONNECT_FAILED;
								msgHandler.sendMessage(msg);
							}
							break;
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			LogUtils.e(TAG, "Conn :End");
		
		}
	}
	
	
	private class Send implements Runnable {

		@Override
		public void run() {
			LogUtils.e(TAG, "Send :Start");
			try {
				if (socket != null) {
					Log.e("blocker", socket.getLocalAddress() + " /" + socket.getLocalPort());
				}
				while (SockState != SOCK_STATE_CLOSE && SockState == SOCK_STATE_CONNECT_SUCCESS
						&& null != outStream) {
					byte[] byteData;
					
					@SuppressWarnings("rawtypes")
					GWPacket packet;
					while (null != (packet = requestQueen.poll())) {
						byteData = packet.pack();
						if (byteData != null) {
							LogUtils.e(TAG, byteData);
							outStream.write(byteData);
							outStream.flush();
							byteData = null;
							if (packet.getMsgType() == GWPacket.MSG_SSID_PWD) {
								close();
							} else if (packet.getMsgType() == GWPacket.MSG_EXIT_CONFIG) {
								complete = true;
								Thread.sleep(200);
								if (msgHandler != null) {
									Message msg = Message.obtain();
									msg.what = WifiConfigManager.CONFIG_SUCCESS;
									msg.obj = tokenValue;
									msgHandler.sendMessage(msg);
									tokenValue = null;
								}
								close();
								WifiConfigManager.stopConfig();
							}
							Log.e("blocker#send", "send success...");
						}
					}

//					synchronized (lock) {
//						lock.wait();
//					}
				}
			} catch (SocketException e1) {
				e1.printStackTrace();// 发送的时候出现异常，说明socket被关闭了(服务器关闭)java.net.SocketException:
										// sendto failed: EPIPE (Broken pipe)
				LogUtils.e(TAG, "Send ::SocketException");
                LogUtils.d("ap socket send occur exception, begin to reconnection");
				conn();
			} catch (Exception e) {
				LogUtils.e(TAG, "Send ::Exception");
				e.printStackTrace();

				if (msgHandler != null) {
					Message msg = Message.obtain();
					msg.what = WifiConfigManager.DEVICE_SEND_FAILED;
					msgHandler.sendMessage(msg);
				}
			}

			LogUtils.e(TAG, "Send ::End");
		}
		
	}
	
	
	private class Rec implements Runnable {
		public void run() {
			LogUtils.e(TAG, "Rec :Start");

			try {
				while (SockState != SOCK_STATE_CLOSE && SockState == SOCK_STATE_CONNECT_SUCCESS
						&& null != inStream && socket.isConnected()) {
//					LogUtils.v(TAG, "Rec :---------");
					byte[] headPkg = new byte[WifiConfigConsts.PACKET_TOTAL_LEN];
					ByteBuffer byteBuffer = ByteBuffer.wrap(headPkg);
					int lenght = inStream.read(headPkg);
					if (lenght == WifiConfigConsts.PACKET_TOTAL_LEN) {
						short start = byteBuffer.getShort();
						LogUtils.e(TAG, "pkg start: " + start);
						if (start != WifiConfigConsts.PACKET_START_DATA) {
							return;
						}
						int pkgLen = byteBuffer.getInt(WifiConfigConsts.PACKET_START_LEN + WifiConfigConsts.PACKET_TYPE_LEN);
						LogUtils.e(TAG, "pkgLen: " + pkgLen);
						if (pkgLen > 0) {
							byte[] data = new byte[pkgLen];
							lenght = inStream.read(data);
							if (lenght > 0) {
								String jsonData = new String(data, "utf-8");
								LogUtils.e("SocketConnect", "Rec # json: " + jsonData);
                                LogUtils.d("ap socket receive jsonData: " + jsonData);
								JSONObject jsonObject = new JSONObject(jsonData);
								if (jsonObject.has("message")) {
									int code = jsonObject.getInt("code");
									LogUtils.e("code: " + code);
									if (code == -1) {
										if (timer != null) {
											timer.cancel();
											timer = null;
										}
										configState = ConfigState.START;
										if (msgHandler != null) {
											Message msg = Message.obtain();
											msg.what = WifiConfigManager.CONFIG_FAILED;
											msgHandler.sendMessage(msg);
										}
									} else {
										configState = ConfigState.SSID_PWD_SENT;
									}
									close();
								} else if (jsonObject.has("token")) {
									tokenValue = jsonObject.getString("token");
									if (!TextUtils.isEmpty(tokenValue)) {
										LogUtils.e("token: " + tokenValue);

                                        LogUtils.d("ap config success with tokenValue: " + tokenValue);
										
										configState = ConfigState.SUCCESS;


										if (msgHandler != null) {
											Message msg = Message.obtain();
											msg.what = WifiConfigManager.CONFIG_SUCCESS;
											msg.obj = tokenValue;
											msgHandler.sendMessage(msg);
										}
										
										sendExitConfig();
										
									} else {
										close();
									}
								} else {
									close();
								}
							}
						}else if(pkgLen == 0) {
							if(configState == ConfigState.SSID_PWD_SENT) {
								Thread.sleep(2 * 1000);
								sendGateWayToken();
							}
						}
					}
				}
			} catch (SocketException e1) {
				e1.printStackTrace();// 客户端主动socket.close()会调用这里
										// java.net.SocketException: Socket
										// closed
				SockState = SOCK_STATE_CLOSE;
				if (!complete) {
					conn();
				}
				LogUtils.e(TAG, "Rec :SocketException");
			} catch (Exception e) {
				LogUtils.e(TAG, "Rec :Exception");
				e.printStackTrace();


				if (msgHandler != null) {
					Message msg = Message.obtain();
					msg.what = WifiConfigManager.DEVICE_RECV_FAILED;
					msgHandler.sendMessage(msg);
				}
				
				//服务器长连接断开异常处理
				SockState = SOCK_STATE_CLOSE;
				if (!complete) {
					lastConnTime = 0;
					conn();
				}
			} 
			LogUtils.e(TAG, "Rec :End");
		}
	}
	
	private synchronized void sendSSIDCheckConfig(){
		GWAction gwAction = new GWAction();
		gwAction.action = DeviceConnect.ACTION_CHECK_CONFIG;
		GWActionPacket packet = new GWActionPacket(gwAction);
		packet.setMsgType(GWPacket.MSG_CHECK_SSID);
		int len = send(packet);
		if (len > 0) {
            LogUtils.d("ap send check ssid action message success...");
		} else {
            LogUtils.d("ap send check ssid action message failed...");
		}
	}
	
	private synchronized void sendExitConfig() {
		GWAction gwAction = new GWAction();
		gwAction.action = DeviceConnect.ACTION_EXIT_CONFIG;
		GWActionPacket packet = new GWActionPacket(gwAction);
		packet.setMsgType(GWPacket.MSG_EXIT_CONFIG);
		int len = send(packet);
		if (len > 0) {
            LogUtils.d("ap send exit config action message success...");
		} else {
            LogUtils.d("ap send exit config action message failed...");
		}
	}
	
	private synchronized void sendGateWayToken(){

		GWAction gwAction = new GWAction();
		gwAction.action = DeviceConnect.ACTION_TOKEN;

		GWActionPacket gwActionPacket = new GWActionPacket(gwAction);
		gwActionPacket.setMsgType(GWPacket.MSG_REQUEST_TOKEN);
		int actionLen = send(gwActionPacket);
		if (actionLen > 0) {
            LogUtils.d("ap send token action message success...");
		} else {
            LogUtils.d("ap send token action message failed...");
		}
	}
	
	private synchronized void close() {
		try {
			if (SockState != SOCK_STATE_CLOSE) {
				try {
					if (null != socket) {
						socket.close();
					}
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					socket = null;
				}

				try {
					if (null != outStream) {
						outStream.close();
					}
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					outStream = null;
				}

				try {
					if (null != inStream) {
						inStream.close();
					}
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					inStream = null;
				}

				try {
					if (null != conn && conn.isAlive()) {
						conn.interrupt();
					}
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					conn = null;
				}

				try {
					if (null != send && send.isAlive()) {
						send.interrupt();
					}
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					send = null;
				}

				try {
					if (null != rec && rec.isAlive()) {
						rec.interrupt();
					}
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					rec = null;
				}

				SockState = SOCK_STATE_CLOSE;
			}
			requestQueen.clear();
			// mSocketResponseListener.onSocketResponse(
			// ISocketResponse.STATUS_SOCKET_CLOSE, null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
