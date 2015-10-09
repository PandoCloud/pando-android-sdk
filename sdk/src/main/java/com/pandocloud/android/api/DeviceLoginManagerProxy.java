package com.pandocloud.android.api;

import android.content.Context;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.pandocloud.android.api.AbsOpenApi;
import com.pandocloud.android.api.DeviceLoginApi;
import com.pandocloud.android.api.DeviceState;
import com.pandocloud.android.api.interfaces.RequestListener;
import com.pandocloud.android.utils.LogUtils;

import org.apache.http.Header;
import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by ywen on 15/5/22.
 */
public class DeviceLoginManagerProxy {

    /**
     * 设备注册登录
     * @param context
     * @param vendorKey 厂商id及key
     * @param productKey 产品id及key
     * @param requestListener
     */
    public void registerDevice(final Context context, String vendorKey, String productKey,
                               final RequestListener requestListener){
        if (requestListener != null) {
            requestListener.onPrepare();
        }

        DeviceLoginApi.deviceRegister(context, vendorKey, productKey, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);

                if (statusCode == HttpStatus.SC_OK && response != null) {
                    if (AbsOpenApi.DEBUG) {
                        LogUtils.e(getClass().getSimpleName(), "deviceRegister: " + response.toString());
                    }
                    try {
                        int code = response.getInt("code");
                        JSONObject jsonData = response.getJSONObject("data");
                        if (jsonData != null && code == AbsOpenApi.CODE_SUCCESS) {
                            final int deviceId = jsonData.getInt("device_id");
                            String deviceSecret = jsonData.getString("device_secret");
//                            String deviceKey = jsonData.getString("device_key");
                            DeviceLoginApi.deviceAuthor(context, deviceId, deviceSecret, new JsonHttpResponseHandler() {

                                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                                    if (AbsOpenApi.DEBUG) {
                                        LogUtils.e(getClass().getSimpleName(), "deviceAuthor: " + response.toString());
                                    }
                                    try {
                                        int code = response.getInt("code");
                                        if (code == AbsOpenApi.CODE_SUCCESS) {
                                            JSONObject jsonObject = response.getJSONObject("data");
                                            if (jsonObject != null) {
                                                String accessToken = jsonObject.getString(DeviceState.ACCESS_TOKEN);
                                                String accessAddr = jsonObject.getString(DeviceState.ACCESS_ADDR);

                                                DeviceState.Builder builder = new DeviceState.Builder(context);
                                                builder.saveInt(DeviceState.DEVICE_ID, deviceId)
                                                        .saveString(DeviceState.ACCESS_TOKEN, accessToken)
                                                        .saveString(DeviceState.ACCESS_ADDR, accessAddr);

                                                if (jsonObject.has(DeviceState.EVENT_SEQUENCE)) {
                                                    builder.saveLong(DeviceState.EVENT_SEQUENCE, jsonObject.getLong(DeviceState.EVENT_SEQUENCE));
                                                }
                                                if (jsonObject.has(DeviceState.DATA_SEQUENCE)) {
                                                    builder.saveLong(DeviceState.DATA_SEQUENCE, jsonObject.getLong(DeviceState.DATA_SEQUENCE));
                                                }
                                                builder.commit();

                                                if (requestListener != null) {
                                                    requestListener.onSuccess();
                                                    requestListener.onFinish();
                                                }
                                            } else {
                                                LogUtils.e("deviceAuthor#jsonObject is null...");
                                            }
                                        } else {
                                            String message = response.getString("message");
                                            if (requestListener != null) {
                                                requestListener.onFail(new Exception(message));
                                                requestListener.onFinish();
                                            }
                                        }
                                    } catch (JSONException e1) {
                                        e1.printStackTrace();
                                    }
                                }

                                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                                    LogUtils.d("DeviceLoginManager", throwable.toString());
                                    if (requestListener != null) {
                                        requestListener.onFail(new Exception(throwable));
                                        requestListener.onFinish();
                                    }
                                }

                            });
                        } else {
                            if (requestListener != null) {
                                requestListener.onFail(new Exception(response.toString()));
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    if (requestListener != null) {
                        requestListener.onFail(new Exception("http status code: " + statusCode));
                    }
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                if (requestListener != null) {
                    requestListener.onFail(new Exception(throwable));
                    requestListener.onFinish();
                }
            }
        });
    }


    /**
     * 设备登录
     * @param context
     * @param deviceId
     * @param deviceSecret
     * @param requestListener
     */
    public void deviceAuthor(final Context context, final int deviceId, String deviceSecret, final RequestListener requestListener) {
        DeviceLoginApi.deviceAuthor(context, deviceId, deviceSecret, new JsonHttpResponseHandler() {

            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                if (AbsOpenApi.DEBUG) {
                    LogUtils.e(getClass().getSimpleName(), "deviceAuthor: " + response.toString());
                }
                try {
                    int code = response.getInt("code");
                    if (code == AbsOpenApi.CODE_SUCCESS) {
                        JSONObject jsonObject = response.getJSONObject("data");
                        if (jsonObject != null) {
                            String accessToken = jsonObject.getString(DeviceState.ACCESS_TOKEN);
                            String accessAddr = jsonObject.getString(DeviceState.ACCESS_ADDR);

                            DeviceState.Builder builder = new DeviceState.Builder(context);
                            builder.saveInt(DeviceState.DEVICE_ID, deviceId)
                                    .saveString(DeviceState.ACCESS_TOKEN, accessToken)
                                    .saveString(DeviceState.ACCESS_ADDR, accessAddr);

                            if (jsonObject.has(DeviceState.EVENT_SEQUENCE)) {
                                builder.saveLong(DeviceState.EVENT_SEQUENCE, jsonObject.getLong(DeviceState.EVENT_SEQUENCE));
                            }
                            if (jsonObject.has(DeviceState.DATA_SEQUENCE)) {
                                builder.saveLong(DeviceState.DATA_SEQUENCE, jsonObject.getLong(DeviceState.DATA_SEQUENCE));
                            }
                            builder.commit();

                            if (requestListener != null) {
                                requestListener.onSuccess();
                                requestListener.onFinish();
                            }
                        } else {
                            LogUtils.e("deviceAuthor#jsonObject is null...");
                        }
                    } else {
                        String message = response.getString("message");
                        if (requestListener != null) {
                            requestListener.onFail(new Exception(message));
                            requestListener.onFinish();
                        }
                    }
                } catch (JSONException e1) {
                    e1.printStackTrace();
                }
            }

            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                LogUtils.d("DeviceLoginManager", throwable.toString());
                if (requestListener != null) {
                    requestListener.onFail(new Exception(throwable));
                    requestListener.onFinish();
                }
            }

        });
    }
}
