package com.pandocloud.android.agent.codec;

/**
 * Created by ruizeng on 8/28/15.
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.OutputStream;

public class TLV {
    public static final short  FLOAT64 = 1;
    public static final short  FLOAT32 = 2;
    public static final short  INT8    = 3;
    public static final short  INT16   = 4;
    public static final short  INT32   = 5;
    public static final short  INT64   = 6;
    public static final short  UINT8   = 7;
    public static final short  UINT16  = 8;
    public static final short  UINT32  = 9;
    public static final short  UINT64  = 10;
    public static final short  BYTES   = 11;
    public static final short  URI     = 12;
    public static final short  BOOL    = 13;

    public short getTag() {
        return tag;
    }

    public void setTag(short tag) {
        this.tag = tag;
    }

    private short tag = 0;

    public Object getObj() {
        return obj;
    }

    public void setObj(Object obj) {
        this.obj = obj;
    }

    private Object obj = null;

    public void encodeToOutputSteam(OutputStream bout) throws Exception{
        DataOutputStream dos = new DataOutputStream(bout);
        dos.writeShort(this.tag);
        switch (this.tag) {
            case FLOAT64:
                double doubleVar = (double)this.obj;
                dos.writeDouble(doubleVar);
                break;
            case FLOAT32:
                float floatVar = (float)this.obj;
                dos.writeFloat(floatVar);
                break;
            case INT8:
            case UINT8:
                byte byteVar = (byte)this.obj;
                dos.write(byteVar);
                break;
            case INT16:
            case UINT16:
                short shortVal = (short)this.obj;
                dos.writeShort(shortVal);
                break;
            case INT32:
            case UINT32:
                int intVal = (int)this.obj;
                dos.writeInt(intVal);
                break;
            case INT64:
            case UINT64:
                long longVal = (long)this.obj;
                dos.writeLong(longVal);
                break;
            case BOOL:
                boolean boolVal = (boolean)this.obj;
                dos.writeBoolean(boolVal);
                break;
            case BYTES:
                byte[] bytesVal = (byte[])this.obj;
                String stringToWrite = new String(bytesVal);
                dos.writeShort((short)stringToWrite.length());
                dos.writeBytes(stringToWrite);
                break;
            case URI:
                String uriVal = (String)this.obj;
                dos.writeShort((short)uriVal.length());
                dos.writeBytes(uriVal);
                break;
            default:
                System.out.println("TLV tag not support :" + Short.toString((this.tag)));
                break;
        }
    }

    public byte[] encodeToByteArray() throws Exception {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        encodeToOutputSteam(bout);
        return bout.toByteArray();
    }

    public static TLV decodeFromByteArray(byte[] inputBytes) throws Exception {
        ByteArrayInputStream bin = new ByteArrayInputStream(inputBytes);
        return decodeFromInputStream(bin);
    }

    public static TLV decodeFromInputStream(ByteArrayInputStream bin) throws Exception{
        TLV tlv = new TLV();
        DataInputStream dis = new DataInputStream(bin);
        tlv.tag = dis.readShort();
        switch(tlv.tag){
            case FLOAT64:
                tlv.obj = dis.readDouble();
                break;
            case FLOAT32:
                tlv.obj = dis.readFloat();
                break;
            case INT8:
            case UINT8:
                tlv.obj = dis.readByte();
                break;
            case INT16:
            case UINT16:
                tlv.obj = dis.readShort();
                break;
            case INT32:
            case UINT32:
                tlv.obj = dis.readInt();
                break;
            case INT64:
            case UINT64:
                tlv.obj = dis.readLong();
                break;
            case BOOL:
                tlv.obj = dis.readBoolean();
                break;
            case BYTES:
                short byteLength = dis.readShort();
                byte[] bytes = new byte[byteLength];
                dis.read(bytes);
                tlv.obj = bytes;
                break;
            case URI:
                short uriLength = dis.readShort();
                byte[] uriBytes = new byte[uriLength];
                dis.read(uriBytes);
                tlv.obj = new String(uriBytes);
                break;
            default:
                System.out.println("TLV tag not support :" + Short.toString((tlv.tag)));
                break;
        }
        return tlv;
    }

}
