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

public class Command {

    public short subdeviceId;
    public short commandNum;
    public short priority;
    public List<TLV> params;

    public static Command decodeFromInputStream(ByteArrayInputStream bin) throws Exception {
        Command cmd = new Command();
        DataInputStream dis = new DataInputStream(bin);
        cmd.subdeviceId = dis.readShort();
        cmd.commandNum = dis.readShort();
        cmd.priority = dis.readShort();
        short cnt = dis.readShort();
        cmd.params = new ArrayList<>();
        for(short i=0; i<cnt; i++) {
            TLV tlv = TLV.decodeFromInputStream(bin);
            cmd.params.add(tlv);
        }
        return cmd;
    }

    public void encodeToOutputStream(ByteArrayOutputStream bout) throws Exception {
        DataOutputStream dos = new DataOutputStream(bout);
        dos.writeShort(subdeviceId);
        dos.writeShort(commandNum);
        dos.writeShort(priority);
        short cnt = (short)params.size();
        for(short i=0; i<cnt; i++) {
            params.get(i).encodeToOutputSteam(bout);
        }
    }
}
