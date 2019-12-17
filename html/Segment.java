
import java.io.*;

public class Segment {

    public int sourcePortNum;
    public int destPortNum;
    public long sequenceNum;
    public long ackNum;
    public int flags;
    public int rcvWindow;
    public int urgDataPtr;
    public byte[] options;
    public byte[] data;
    public int dataLen;
    public static final int CWR = 1;
    public static final int ECE = 2;
    public static final int URG = 4;
    public static final int ACK = 8;
    public static final int PSH = 16;
    public static final int RST = 32;
    public static final int SYN = 64;
    public static final int FIN = 128;
    public static final int MAX_SEGMENT_LENGTH = 1024;

    public String flagStr() {
        String s = "";
        if ((flags & CWR) > 0) {
            if (!s.equals("")) {
                s += "|";
            }
            s += "CWR";
        }
        if ((flags & ECE) > 0) {
            if (!s.equals("")) {
                s += "|";
            }
            s += "ECE";
        }
        if ((flags & URG) > 0) {
            if (!s.equals("")) {
                s += "|";
            }
            s += "URG";
        }
        if ((flags & ACK) > 0) {
            if (!s.equals("")) {
                s += "|";
            }
            s += "ACK";
        }
        if ((flags & PSH) > 0) {
            if (!s.equals("")) {
                s += "|";
            }
            s += "PSH";
        }
        if ((flags & RST) > 0) {
            if (!s.equals("")) {
                s += "|";
            }
            s += "RST";
        }
        if ((flags & SYN) > 0) {
            if (!s.equals("")) {
                s += "|";
            }
            s += "SYN";
        }
        if ((flags & FIN) > 0) {
            if (!s.equals("")) {
                s += "|";
            }
            s += "FIN";
        }

        if (s.equals("")) {
            s = "<data packet>";
        }
        return s;
    }

    public Segment(int sPortNum, int dPortNum, long seqNum, long ackNumber,
            int flagVal, int rcvWin, int urgent, byte[] optionArea, byte[] dataArea, int dataLength) {
        sourcePortNum = sPortNum;
        destPortNum = dPortNum;
        sequenceNum = seqNum;
        ackNum = ackNumber;
        flags = flagVal;
        rcvWindow = rcvWin;
        urgDataPtr = urgent;
        options = optionArea;
        data = dataArea;
        dataLen = dataLength;
    }

    private static final byte[] intToByte(int value) {
        byte[] rtnVal = new byte[]{(byte) value};

        if ((value >>> 8) != 0) {
            System.out.println("Byte value too big.");
        }

        return rtnVal;
    }

    // The following conversions use big-endian format. This is what
    // is used for TCP multi-byte integer formats.
    public static final byte[] intToTwoBytes(int value) {
        byte[] rtnVal = new byte[]{
            (byte) (value >>> 8),
            (byte) value};

        if ((value >>> 16) != 0) {
            System.out.println("16-bit integer value too big to fit in two bytes.");
        }

        return rtnVal;
    }

    public static final byte[] longToFourBytes(long value) {
        byte[] rtnVal = new byte[]{
            (byte) (value >>> 24),
            (byte) (value >>> 16),
            (byte) (value >>> 8),
            (byte) value};

        if ((value >>> 32) != 0) {
            System.out.println("32-bit integer value too big to fit in four bytes.");
        }

        return rtnVal;
    }

    private static void copy(byte[] from, int fromStart, byte[] to, int toStart, int length) {
        int k;

        for (k = 0; k < length; k++) {
            to[k + toStart] = from[k + fromStart];
        }
    }

    // The following functions assume big-endian format which is used
    // for multi-byte integers within TCP.
    public static int extractInt(byte[] data, int start, int len) {
        int rtnVal = 0;
        int k;

        if (len > 4) {
            System.out.println("Length of data greater then 4 bytes. Cannot extract to integer.");
        }

        for (k = 0; k < len; k++) {
            rtnVal = (int) ((rtnVal << 8) | (data[k + start] & 0x000000ff));
        }

        return rtnVal;

    }

    public static long extractLong(byte[] data, int start, int len) {
        long rtnVal = 0;
        int k;

        if (len > 8) {
            System.out.println("Length of data greater then 8 bytes. Cannot extract to long.");
        }

        for (k = 0; k < len; k++) {
            rtnVal = (long) ((rtnVal << 8) | (data[k + start] & 0x00000000000000ff));
        }

        return rtnVal;
    }

    private static int checksum(byte[] data, int length) {
        int checksum = 0;

        int k;

        for (k = 0; k < length - 1; k = k + 2) {
            int x = extractInt(data,k,2);
            checksum = checksum + extractInt(data, k, 2);
            checksum = (checksum & 0xffff) + (checksum >>> 16);
            //System.out.println("x is "+x+" and checksum is "+checksum);
        }

        if (length % 2 != 0) {
            checksum = checksum + (0xff00 & (data[length - 1] << 8));
            checksum = (checksum & 0xffff) + (checksum >>> 16);
        }

        //System.out.println("checksum variable is " + checksum);
        //System.out.println("CheckSum is " + ((~checksum & 0xffff)));

        return (~checksum) & 0xffff;
    }

    public byte[] toBytes() {
        int optionsLen;
        int chksum;

        if (options == null) {
            optionsLen = 0;
        } else {
            optionsLen = options.length;
        }


        byte[] segment = new byte[20 + optionsLen + dataLen];

        int headerLength = (20 + optionsLen) / 4;

        copy(intToTwoBytes(sourcePortNum), 0, segment, 0, 2);
        copy(intToTwoBytes(destPortNum), 0, segment, 2, 2);
        copy(longToFourBytes(sequenceNum), 0, segment, 4, 4);
        copy(longToFourBytes(ackNum), 0, segment, 8, 4);
        copy(intToByte(headerLength), 0, segment, 12, 1);
        copy(intToByte(flags), 0, segment, 13, 1);
        copy(intToTwoBytes(rcvWindow), 0, segment, 14, 2);
        copy(intToTwoBytes(urgDataPtr), 0, segment, 18, 2);
        copy(options, 0, segment, 20, optionsLen);
        copy(data, 0, segment, 20 + optionsLen, dataLen);

        chksum = checksum(segment, segment.length);

        copy(intToTwoBytes(chksum), 0, segment, 16, 2);

        return segment;
    }

    public static Segment fromBytes(byte[] segment, int length) throws IOException {

        int computedChecksum = checksum(segment, length);

        if (computedChecksum != 0) {
            throw new IOException("Checksum Error.");
        }

        int sourcePortNum = extractInt(segment, 0, 2);
        int destPortNum = extractInt(segment, 2, 2);
        long sequenceNum = extractLong(segment, 4, 4);
        long ackNum = extractLong(segment, 8, 4);
        int headerLength = extractInt(segment, 12, 1);
        int flags = extractInt(segment, 13, 1);
        int rcvWindow = extractInt(segment, 14, 2);
        int checksum = extractInt(segment, 16, 2);
        int urgDataPtr = extractInt(segment, 18, 2);
        byte[] options = new byte[headerLength * 4 - 20];
        copy(segment, 20, options, 0, headerLength * 4 - 20);
        byte[] data = new byte[length - headerLength * 4];
        copy(segment, headerLength * 4, data, 0, data.length);

        return new Segment(sourcePortNum, destPortNum, sequenceNum, ackNum,
                flags, rcvWindow, urgDataPtr, options, data, data.length);
    }
}
