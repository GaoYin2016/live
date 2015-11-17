package tv.yewai.live.douyu.utils;

import org.apache.mina.core.buffer.IoBuffer;

public class HexUtils {
    private static final byte[] hex = "0123456789ABCDEF".getBytes();

    public static IoBuffer hexString2IoBuffer(String hexString) {
        IoBuffer ioBuffer = IoBuffer.allocate(8);
        ioBuffer.setAutoExpand(true);
        ioBuffer.put(HexString2Bytes(hexString));
        ioBuffer.flip();
        return ioBuffer;
    }

    public static String ioBufferToString(Object message) throws Exception {
        if (!(message instanceof IoBuffer)) {
            return "";
        }
        IoBuffer ioBuffer = (IoBuffer) message;
        byte[] b = new byte[ioBuffer.limit()];
        ioBuffer.get(b);
        String bb = new String(b, "utf-8");

        return bb;
    }

    public static String ioBufferToHexString(Object message)
            throws Exception {
        if (!(message instanceof IoBuffer)) {
            return "";
        }
        IoBuffer ioBuffer = (IoBuffer) message;
        byte[] b = new byte[ioBuffer.limit()];
        ioBuffer.get(b);
        String bb = Bytes2HexString(b);
        return bb;
    }

    private static int parse(char c) {
        if (c >= 'a')
            return c - 'a' + 10 & 0xF;
        if (c >= 'A')
            return c - 'A' + 10 & 0xF;
        return c - '0' & 0xF;
    }

    public static String Bytes2HexString(byte[] b) {
        byte[] buff = new byte[3 * b.length];
        for (int i = 0; i < b.length; i++) {
            buff[(3 * i)] = hex[(b[i] >> 4 & 0xF)];
            buff[(3 * i + 1)] = hex[(b[i] & 0xF)];
            buff[(3 * i + 2)] = 45;
        }
        String re = new String(buff);
        return re.replace("-", " ");
    }

    public static String Bytes2HexStringLower(byte[] b) {
        byte[] buff = new byte[3 * b.length];
        for (int i = 0; i < b.length; i++) {
            buff[(3 * i)] = hex[(b[i] >> 4 & 0xF)];
            buff[(3 * i + 1)] = hex[(b[i] & 0xF)];
            buff[(3 * i + 2)] = 45;
        }
        String re = new String(buff);
        return re.replace("-", "").toLowerCase();
    }

    public static byte[] HexString2Bytes(String hexstr) {
        hexstr = hexstr.replace(" ", "");
        byte[] b = new byte[hexstr.length() / 2];
        int j = 0;
        for (int i = 0; i < b.length; i++) {
            char c0 = hexstr.charAt(j++);
            char c1 = hexstr.charAt(j++);
            b[i] = (byte) (parse(c0) << 4 | parse(c1));
        }
        return b;
    }

    public static String setStringHeader(String hexStr) {
        String length = Integer.toHexString((hexStr.length() + 8) / 2) + "000000";
        return length + length + hexStr;
    }

    public static int getHexStringLength(String hexStr) {
        hexStr = hexStr.replace(" ", "");
        if (hexStr.length() < 8) {
            return hexStr.length() + 1;
        }
        String headerStr = hexStr.substring(0, 8);
        String hexLength = "";
        for (int i = 6; i >= 0; i -= 2) {
            hexLength = hexLength + headerStr.substring(i, i + 2);
        }
        return Integer.parseInt(hexLength, 16) * 2 - 8;
    }
}
