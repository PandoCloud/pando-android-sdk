package com.pandocloud.android.agent.codec;

/**
 * Created by ruizeng on 8/28/15.
 */

public class SubDevicePackage {

    public int messageType = 0;
    public Object message = null;

    public SubDevicePackage(Command cmd) {
        messageType = Payload.COMMAND;
        message = cmd;
    }

    public SubDevicePackage(Event event) {
        messageType = Payload.EVENT;
        message = event;
    }

    public SubDevicePackage(Data data) {
        messageType = Payload.DATA;
        message = data;
    }

}
