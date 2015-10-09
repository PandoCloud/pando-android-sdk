package com.pandocloud.android.agent.codec;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ruizeng on 8/29/15.
 */
public class DataProperty {
    public short subdeviceId;
    public short propertyNum;
    public List<TLV> params;



    public static DataProperty decodeFromInputStream(ByteArrayInputStream bin) throws Exception {
        DataInputStream dis = new DataInputStream(bin);
        DataProperty property = new DataProperty();
        property.subdeviceId = dis.readShort();
        property.propertyNum = dis.readShort();
        short cnt = dis.readShort();
        property.params = new ArrayList<>();
        for(short i=0; i<cnt; i++) {
            TLV tlv = TLV.decodeFromInputStream(bin);
            property.params.add(tlv);
        }
        return property;
    }

    public void encodeToOutputStream(ByteArrayOutputStream bout) throws Exception {
        DataOutputStream dos = new DataOutputStream(bout);
        dos.writeShort(subdeviceId);
        dos.writeShort(propertyNum);
        short cnt = (short)params.size();
        for(short i=0; i<cnt; i++) {
            params.get(i).encodeToOutputSteam(bout);
        }
    }
}
