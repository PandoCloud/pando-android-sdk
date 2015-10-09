package com.pandocloud.android.agent.codec;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ruizeng on 8/28/15.
 */

public class Event {
    public short subdeviceId;
    public short eventNum;
    public short priority;
    public List<TLV> params;

    public static Event decodeFromInputStream(ByteArrayInputStream bin) throws Exception {
        Event event = new Event();
        DataInputStream dis = new DataInputStream(bin);
        event.subdeviceId = dis.readShort();
        event.eventNum = dis.readShort();
        event.priority = dis.readShort();
        short cnt = dis.readShort();
        event.params = new ArrayList<>();
        for(short i=0; i<cnt; i++) {
            TLV tlv = TLV.decodeFromInputStream(bin);
            event.params.add(tlv);
        }
        return event;
    }

    public void encodeToOutputStream(ByteArrayOutputStream bout) throws Exception {
        DataOutputStream dos = new DataOutputStream(bout);
        dos.writeShort(subdeviceId);
        dos.writeShort(eventNum);
        dos.writeShort(priority);
        short cnt = (short)params.size();
        for(short i=0; i<cnt; i++) {
            params.get(i).encodeToOutputSteam(bout);
        }
    }
}
