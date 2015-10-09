package com.pandocloud.android.config.wifi.deviceconnect;

import android.text.TextUtils;
import android.util.Log;

import com.pandocloud.android.config.wifi.WifiConfigConsts;
import com.pandocloud.android.config.wifi.deviceconnect.bean.GWAction;
import com.pandocloud.android.config.wifi.deviceconnect.bean.WifiInfo;
import com.pandocloud.android.config.wifi.deviceconnect.packet.GWActionPacket;
import com.pandocloud.android.config.wifi.deviceconnect.packet.GWPacket;
import com.pandocloud.android.config.wifi.deviceconnect.packet.WifiInfoPacket;
import com.pandocloud.android.utils.LogUtils;

import org.json.JSONObject;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;

/**
 * Created by ruizeng on 7/7/15.
 */
public class DeviceRequest {
    public static final String ACTION_CONFIG = "config";

    public static final String ACTION_CHECK_CONFIG = "check_config";

    public static final String ACTION_EXIT_CONFIG = "exit_config";

    public static final String ACTION_TOKEN = "token";



    //网关包头设置
    public static final short PACKET_SATRT_DATA = 0x7064;

    public static final int PACKET_START_LEN = 2;

    public static final int PACKET_TYPE_LEN = 2;

    public static final int PACKET_LEN = 4;

    public static final int PACKET_TOTAL_LEN = PACKET_START_LEN + PACKET_TYPE_LEN + PACKET_LEN;

    private String host;
    private int port;
    private Socket socket = null;

    public DeviceRequest(String host, int port){
        this.host = host;
        this.port = port;
    }

    private void connect() throws Exception{
        socket = new Socket();
        socket.connect(new InetSocketAddress(host, port), 20 * 1000);
        socket.setKeepAlive(true);
        socket.setTcpNoDelay(true);
    }

    private int send(GWPacket packet)throws Exception {
        byte[] byteData = packet.pack();
        if (byteData != null) {
            OutputStream outStream = socket.getOutputStream();

            outStream.write(byteData);
            outStream.flush();

            Log.d("device request", "send success...");
        }
        return 0;
    }

    private JSONObject recv() throws Exception {
        InputStream inStream = socket.getInputStream();

        byte[] headPkg = new byte[WifiConfigConsts.PACKET_TOTAL_LEN];
        ByteBuffer byteBuffer = ByteBuffer.wrap(headPkg);
        int length = inStream.read(headPkg);
        if (length == WifiConfigConsts.PACKET_TOTAL_LEN) {
            short start = byteBuffer.getShort();
            LogUtils.e("pandocloud", "pkg start: " + start);
            if (start != WifiConfigConsts.PACKET_START_DATA) {
                return null;
            }
            int pkgLen = byteBuffer.getInt(WifiConfigConsts.PACKET_START_LEN + WifiConfigConsts.PACKET_TYPE_LEN);
            LogUtils.e("pandocloud", "pkgLen: " + pkgLen);
            if (pkgLen > 0) {
                byte[] data = new byte[pkgLen];
                length = inStream.read(data);
                if (length > 0) {
                    String jsonData = new String(data, "utf-8");
                    LogUtils.d("SocketConnect", "Rec # json: " + jsonData);
                    LogUtils.d(" socket receive jsonData: " + jsonData);
                    JSONObject jsonObject = new JSONObject(jsonData);
                    return jsonObject;
                }
            }
        }

        return null;
    }

    private void close() throws Exception {
        if (socket != null ){
            socket.close();
            socket = null;
        }
    }

    /**
     * send ssid and wifi to device ap
     * @param ssid ssid
     * @param password password
     * @return 0 if send succeed, -1 if failed.
     */
    public int sendWifiInfo(String ssid, String password) {
        try {
            this.connect();
            WifiInfo wifiInfo = new WifiInfo();
            wifiInfo.ssid = ssid;
            wifiInfo.password = password;
            wifiInfo.action = DeviceRequest.ACTION_CONFIG;
            WifiInfoPacket wifiInfoPacket = new WifiInfoPacket(wifiInfo);
            if(this.send(wifiInfoPacket) > 0){
                JSONObject jsonObject = this.recv();
                if (jsonObject.has("code")) {
                    int code = jsonObject.getInt("code");
                    LogUtils.d("code: " + code);
                    if (code == 0) {
                        close();
                        return 0;
                    }
                }
            }
            return -1;
        } catch(Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    /**
     *
     * @return 0 if wifi config succeed, -1 if failed.
     */
    public int checkWifiConfig() {
        try {
            this.connect();
            GWAction gwAction = new GWAction();
            gwAction.action = DeviceRequest.ACTION_CHECK_CONFIG;
            GWActionPacket packet = new GWActionPacket(gwAction);
            packet.setMsgType(GWPacket.MSG_CHECK_SSID);
            if(this.send(packet) > 0){
                JSONObject jsonObject = this.recv();
                if (jsonObject.has("code")) {
                    int code = jsonObject.getInt("code");
                    LogUtils.d("code: " + code);
                    if (code == 0) {
                        close();
                        return 0;
                    }
                }
            }
            return -1;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * exit the hotspot config mode.
     * @return 0 if succeed.
     */
    public int exitWifiConfig() {
        try {
            this.connect();
            GWAction gwAction = new GWAction();
            gwAction.action = DeviceRequest.ACTION_EXIT_CONFIG;
            GWActionPacket packet = new GWActionPacket(gwAction);
            packet.setMsgType(GWPacket.MSG_EXIT_CONFIG);
            if(this.send(packet) > 0){
                return 0;
            }
            return -1;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * get device key
     * @return the string format of key , "" if failed.
     */
    public String getDeviceKey() {
        try {
            this.connect();
            GWAction gwAction = new GWAction();
            gwAction.action = DeviceRequest.ACTION_TOKEN;

            GWActionPacket packet = new GWActionPacket(gwAction);
            packet.setMsgType(GWPacket.MSG_REQUEST_TOKEN);
            if(this.send(packet) > 0){
                JSONObject jsonObject = this.recv();
                if (jsonObject.has("code")) {
                    int code = jsonObject.getInt("code");
                    LogUtils.d("code: " + code);
                    if (code == 0) {
                        close();
                        String key = jsonObject.getString("token");
                        if (!TextUtils.isEmpty(key)){
                            return key;
                        }
                    }
                }
            }
            return "";
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }

    }


}
