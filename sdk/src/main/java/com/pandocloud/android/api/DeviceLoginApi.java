package com.pandocloud.android.api;

import android.content.Context;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.pandocloud.android.config.wifi.WifiConfigConsts;
import com.pandocloud.android.utils.LogUtils;
import com.pandocloud.android.utils.PandoUtils;

import org.json.JSONException;
import org.json.JSONStringer;

import java.io.UnsupportedEncodingException;

/**
 * Created by ywen on 15/5/4.
 */
class DeviceLoginApi extends AbsOpenApi {

    /**
     * 设备注册
     */
    private static final String DEVICES_REGISTER = "/v1/devices/registration";

    /**
     * 设备登录
     */
    private static final String DEVICES_LOGIN = "/v1/devices/authentication";

    /**
     *
     * @param context
     * @param vendorKey
     * @param productKey
     * @param responseHandler
     */
    public static void deviceRegister(Context context, String vendorKey, String productKey,
                                      AsyncHttpResponseHandler responseHandler) {

        JSONStringer jsonStringer = new JSONStringer();
        try {
            jsonStringer.object()
                    .key("vendor_key").value(vendorKey)
                    .key("product_key").value(productKey)
                    .key("device_code").value(PandoUtils.getDeviceCode(context))
                    .key("device_type").value(3)//3为安卓手机
                    .key("device_module").value(android.os.Build.MODEL)
                    .key("version").value(WifiConfigConsts.GATEWAY_VERSION)
                    .endObject();
            if (DEBUG) {
                LogUtils.d(jsonStringer.toString());
            }
            post(context, getApiServerUrl() + DEVICES_REGISTER, jsonStringer.toString(), responseHandler);
        } catch (JSONException e) {
            e.printStackTrace();
            responseHandler.onFailure(-200, null, null, e);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            responseHandler.onFailure(-200, null, null, e);
        }

    }

    /**
     *
     * @param context
     * @param deviceId
     * @param deviceSecret
     * @param responseHandler
     */
    public static void deviceAuthor(Context context, int deviceId, String deviceSecret, AsyncHttpResponseHandler responseHandler) {
        JSONStringer jsonStringer = new JSONStringer();
        try {
            jsonStringer.object()
                    .key("device_id").value(deviceId)
                    .key("device_secret").value(deviceSecret)
                    .endObject();
            if (DEBUG) {
                LogUtils.d(jsonStringer.toString());
            }
            post(context, getApiServerUrl() + DEVICES_LOGIN, jsonStringer.toString(), responseHandler);
        } catch (JSONException e) {
            e.printStackTrace();
            responseHandler.onFailure(-200, null, null, e);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            responseHandler.onFailure(-200, null, null, e);
        }
    }
}
