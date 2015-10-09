package com.pandocloud.android.api;

import android.content.Context;

import com.pandocloud.android.api.interfaces.RequestListener;

public final class DeviceLoginManager {
	
	private static DeviceLoginManager sInstances;
	private DeviceLoginManagerProxy mDeviceLoginManagerProxy;
	
	private DeviceLoginManager() {
		mDeviceLoginManagerProxy = new DeviceLoginManagerProxy();
	}
	
	public static DeviceLoginManager getInstances() {
		if (sInstances == null) {
			synchronized (DeviceLoginManager.class) {
				if (sInstances == null) {
					sInstances = new DeviceLoginManager();
				}
			}
		}
		return sInstances;
	}

    public void registerDevice(final Context context, String vendorKey, String productKey,
                               final RequestListener requestListener){
        mDeviceLoginManagerProxy.registerDevice(context, vendorKey, productKey, requestListener);
    }
}
