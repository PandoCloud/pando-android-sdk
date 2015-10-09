package com.pandocloud.android.config.wifi.smartconfig;

import android.os.AsyncTask;
import android.os.Message;
import android.util.Log;

import com.espressif.iot.esptouch.EsptouchTask;
import com.espressif.iot.esptouch.IEsptouchResult;
import com.espressif.iot.esptouch.IEsptouchTask;
import com.pandocloud.android.config.wifi.WifiConfigConsts;
import com.pandocloud.android.config.wifi.WifiConfigManager;
import com.pandocloud.android.config.wifi.WifiConfigMessageHandler;
import com.pandocloud.android.config.wifi.deviceconnect.DeviceConnect;
import com.pandocloud.android.config.wifi.deviceconnect.DeviceRequest;

import java.util.List;

/**
 * Created by ruizeng on 7/8/15.
 */
public class SmartConfig {

    private WifiConfigMessageHandler msgHandler;

    private String ssid;

    private String password;

    private WifiAdmin wifiAdmin;

    private void sendMessage(int what, Object obj){
        if (msgHandler != null) {
            Message msg = Message.obtain();
            msg.what = what;
            msg.obj = obj;
            msgHandler.sendMessage(msg);
        }
    }

    /**
     * 处理消息Handler
     * @param handler
     */
    public void setMsgHandler(WifiConfigMessageHandler handler) {
        this.msgHandler = handler;
    }

    public SmartConfig(String ssid, String password){
        this.ssid = ssid;
        this.password = password;
        wifiAdmin = new WifiAdmin(WifiConfigManager.getContext());

        Log.d("pandocloud", "smart config init..");
    }

    public void start(){
        if (wifiAdmin.getWifiConnectedSsid().equals(ssid)) {
            String apSsid = ssid;
            String apPassword = password;
            String apBssid = wifiAdmin.getWifiConnectedBssid();
            Log.d("pandocloud", "Bssid = " + apBssid);
            Boolean isSsidHidden = wifiAdmin.isWifiConnectedHidden();
            String isSsidHiddenStr = "NO";
            if (isSsidHidden) {
                isSsidHiddenStr = "YES";
            }

            Log.d("pandocloud", "mEdtApSsid = " + apSsid
                        + ", " + " mEdtApPassword = " + apPassword);
            new EsptouchAsyncTask().execute(apSsid, apBssid, apPassword,
                    isSsidHiddenStr, "1");
        }
    }

    private class EsptouchAsyncTask extends AsyncTask<String, Void, List<IEsptouchResult>> {

        private IEsptouchTask mEsptouchTask;
        // without the lock, if the user tap confirm and cancel quickly enough,
        // the bug will arise. the reason is follows:
        // 0. task is starting created, but not finished
        // 1. the task is cancel for the task hasn't been created, it do nothing
        // 2. task is created
        // 3. Oops, the task should be cancelled, but it is running
        private final Object mLock = new Object();

        public void cancelTask(){
            if (mEsptouchTask != null) {
                mEsptouchTask.interrupt();
            }
        }


        @Override
        protected List<IEsptouchResult> doInBackground(String... params) {
            int taskResultCount = -1;
            synchronized (mLock) {
                String apSsid = params[0];
                String apBssid = params[1];
                String apPassword = params[2];
                String isSsidHiddenStr = params[3];
                String taskResultCountStr = params[4];
                boolean isSsidHidden = false;
                if (isSsidHiddenStr.equals("YES")) {
                    isSsidHidden = true;
                }
                taskResultCount = Integer.parseInt(taskResultCountStr);
                mEsptouchTask = new EsptouchTask(apSsid, apBssid, apPassword,
                        isSsidHidden, WifiConfigManager.getContext());
            }
            List<IEsptouchResult> resultList = mEsptouchTask.executeForResults(taskResultCount);
            return resultList;
        }

        @Override
        protected void onPostExecute(List<IEsptouchResult> result) {
            IEsptouchResult firstResult = result.get(0);
            // check whether the task is cancelled and no results received
            if (!firstResult.isCancelled()) {
                int count = 0;
                // max results to be displayed, if it is more than maxDisplayCount,
                // just show the count of redundant ones
                final int maxDisplayCount = 5;
                // the task received some results including cancelled while
                // executing before receiving enough results
                if (firstResult.isSuc()) {
                    StringBuilder sb = new StringBuilder();
                    for (IEsptouchResult resultInList : result) {
                        sb.append("Smart Config success, bssid = "
                                + resultInList.getBssid()
                                + ",InetAddress = "
                                + resultInList.getInetAddress()
                                .getHostAddress() + "\n");

                        Log.i("pandocloud", sb.toString());

                        WifiConfigConsts.DEVICE_HOST = resultInList.getInetAddress().getHostAddress();
                        count++;
                        if (count >= maxDisplayCount) {
                            break;
                        }
                    }
                    if (count < result.size()) {
                        sb.append("\nthere's " + (result.size() - count)
                                + " more result(s) without showing\n");
                        Log.d("pandocloud", sb.toString());
                    }
                    /*
                    DeviceRequest req = new DeviceRequest(WifiConfigConsts.DEVICE_HOST, WifiConfigConsts.DEVICE_PORT);
                    String key = req.getDeviceKey();
                    if (!key.equals("")) {
                        sendMessage(WifiConfigManager.CONFIG_SUCCESS, key);
                    }
                    */

                    DeviceConnect dc = new DeviceConnect(WifiConfigManager.getContext(), WifiConfigManager.CONFIG_MODE_SMARTLINK);
                    dc.setMsgHandler(WifiConfigManager.getMsgHandler());
                    dc.conn();
                } else {
                    sendMessage(WifiConfigManager.CONFIG_FAILED, "error");
                }
            }
        }
    }
}
