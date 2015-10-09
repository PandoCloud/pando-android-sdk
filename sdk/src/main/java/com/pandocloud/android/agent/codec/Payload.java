package com.pandocloud.android.agent.codec;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;

/**
 * Created by ruizeng on 8/28/15.
 */

public final class Payload {
    public static final int COMMAND = 1;
    public static final int EVENT = 2;
    public static final int DATA = 3;

    public static byte[] encode(Package pkg, int type) throws Exception{
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bout);
        dos.writeByte(pkg.flag);
        dos.writeLong(pkg.timestamp);
        dos.write(pkg.token);
        SubDevicePackage subPkg = pkg.subDevicePakage;
        switch (type){
            case COMMAND: {
                Command cmd = (Command)subPkg.message;
                cmd.encodeToOutputStream(bout);
            }
            break;
            case EVENT: {
                Event event = (Event)subPkg.message;
                event.encodeToOutputStream(bout);
            }
            break;
            case DATA: {
                Data data = (Data)subPkg.message;
                data.encodeToOutputStream(bout);
            }
            break;
            default:
                break;
        }
        return bout.toByteArray();
    }
    public static Package decode(byte[] bytes, int type) throws Exception{
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        DataInputStream dis = new DataInputStream(bis);
        Package pkg = new Package();
        pkg.flag = dis.readByte();
        pkg.timestamp = dis.readLong();
        dis.read(pkg.token);
        SubDevicePackage subPkg;
        switch (type){
            case COMMAND: {
                Command cmd = Command.decodeFromInputStream(bis);
                subPkg = new SubDevicePackage(cmd);
                pkg.subDevicePakage = subPkg;
            }
                break;
            case EVENT: {
                Event event = Event.decodeFromInputStream(bis);
                subPkg = new SubDevicePackage(event);
                pkg.subDevicePakage = subPkg;
            }
                break;
            case DATA: {
                Data data = Data.decodeFromInputSteam(bis);
                subPkg = new SubDevicePackage(data);
                pkg.subDevicePakage = subPkg;
            }
                break;
            default:
                break;
        }
        return pkg;
    }
}
