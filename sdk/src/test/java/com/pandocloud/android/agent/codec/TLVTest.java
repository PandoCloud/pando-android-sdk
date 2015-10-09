package com.pandocloud.android.agent.codec;

import org.junit.Before;
import org.junit.Test;
import org.junit.internal.runners.statements.Fail;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by ruizeng on 8/29/15.
 */
public class TLVTest {

    class testPair {
        public TLV tlv;
        public byte[] bytes;
    }

    private List<testPair> cases;

    @Before
    public void setUp() throws Exception {
        cases = new ArrayList<>();
        {
            // FLOAT32 test
            testPair float32Test = new testPair();
            float32Test.tlv = new TLV();
            float32Test.tlv.setTag(TLV.FLOAT32);
            float float32Var = (float)9.0;
            float32Test.tlv.setObj(float32Var);
            float32Test.bytes = new byte[]{0x0, 0x02, 0x41, 0x10, 0x0, 0x0};
            cases.add(float32Test);
        }
        {
            // INT8 test
            testPair int8Test = new testPair();
            int8Test.tlv = new TLV();
            int8Test.tlv.setTag(TLV.INT8);
            int8Test.tlv.setObj((byte) 9);
            int8Test.bytes = new byte[]{0x0, 0x3, 0x9};
            cases.add(int8Test);
        }
        {
            // INT16 test
            testPair int16Test = new testPair();
            int16Test.tlv = new TLV();
            int16Test.tlv.setTag(TLV.INT16);
            int16Test.tlv.setObj((short) 9);
            int16Test.bytes = new byte[]{0x0, 0x4, 0x00, 0x09};
            cases.add(int16Test);
        }
        {
            // INT32 test
            testPair int32Test = new testPair();
            int32Test.tlv = new TLV();
            int32Test.tlv.setTag(TLV.INT32);
            int32Test.tlv.setObj(9);
            int32Test.bytes = new byte[]{0x0, 0x5, 0x00, 0x00, 0x00, 0x09};
            cases.add(int32Test);
        }
        {
            // INT64 test
            testPair int64Test = new testPair();
            int64Test.tlv = new TLV();
            int64Test.tlv.setTag(TLV.INT64);
            int64Test.tlv.setObj((long)9);
            int64Test.bytes = new byte[]{0x0, 0x6, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x09};
            cases.add(int64Test);
        }
        {
            // BYTES test
            testPair bytesTest = new testPair();
            bytesTest.tlv = new TLV();
            bytesTest.tlv.setTag(TLV.BYTES);
            bytesTest.tlv.setObj(new byte[]{0x00, 0x01, 0x02, 0x03, 0x04});
            bytesTest.bytes = new byte[]{0x0, 0x0b, 0x00, 0x05, 0x00, 0x01, 0x02, 0x03, 0x04};
            cases.add(bytesTest);
        }
        {
            // BYTES test
            testPair uriTest = new testPair();
            uriTest.tlv = new TLV();
            uriTest.tlv.setTag(TLV.URI);
            uriTest.tlv.setObj("abc");
            uriTest.bytes = new byte[]{0x0, 0x0c, 0x00, 0x03, 0x61, 0x62, 0x63};
            cases.add(uriTest);
        }

    }

    @Test
    public void testEncodeToByteArray() throws Exception {

        for(int i = 0; i < cases.size(); i++)
        {
            testPair pair = cases.get(i);
            assertArrayEquals(pair.bytes, pair.tlv.encodeToByteArray());
        }
    }

    @Test
    public void testDecodeFromByteArray() throws Exception {
        for(int i = 0; i < cases.size(); i++)
        {
            testPair pair = cases.get(i);
            TLV result = TLV.decodeFromByteArray(pair.bytes);
            assertEquals(pair.tlv.getTag(), result.getTag());
        }
    }
}