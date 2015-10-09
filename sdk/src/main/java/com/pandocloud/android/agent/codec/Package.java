package com.pandocloud.android.agent.codec;

/**
 * Created by ruizeng on 8/28/15.
 */

public class Package {
    public  byte flag;
    public  long timestamp;
    public  byte[] token;
    public  SubDevicePackage subDevicePakage;

    public Package() {
        token = new byte[16];
    }
}
