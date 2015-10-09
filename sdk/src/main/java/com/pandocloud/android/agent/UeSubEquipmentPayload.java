package com.pandocloud.android.agent;

public class UeSubEquipmentPayload {

    class TLV {
        private byte[] type;
        private byte[] length;
        private byte[] value;
    }

    class Data {
        private byte[] subdeviceId;
        private byte[] propertyNum;
        private TLV[] params;
    }

    class Command {
        private byte[] subdeviceId;
        private byte[] commandNum;
        private byte[] priority;
        private TLV[] params;
    }

    class Event {
        private byte[] subdeviceId;
        private byte[] eventNum;
        private byte[] priority;
        private TLV[] params;
    }
}
