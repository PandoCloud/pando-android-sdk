package com.pandocloud.android.agent.codec;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ruizeng on 8/28/15.
 */

public class Data {
    public List<DataProperty> properties;

    public static Data decodeFromInputSteam(ByteArrayInputStream bin) throws Exception {
        DataInputStream dis = new DataInputStream(bin);
        Data data = new Data();
        while( true ) {
            try {
                DataProperty property = DataProperty.decodeFromInputStream(bin);
                data.properties.add(property);
            } catch (EOFException e){
                break;
            }
        }
        return data;
    }

    public void encodeToOutputStream(ByteArrayOutputStream bout) throws Exception {
        DataOutputStream dos = new DataOutputStream(bout);
        short cnt = (short)properties.size();
        for(short i=0; i<cnt; i++) {
            properties.get(i).encodeToOutputStream(bout);
        }
    }
}
