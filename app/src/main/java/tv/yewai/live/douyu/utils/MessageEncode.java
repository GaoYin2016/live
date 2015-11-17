package tv.yewai.live.douyu.utils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class MessageEncode {
    private String a = "";

    private String a(String paramString) {
        return paramString.replaceAll("/", "@S").replaceAll("@", "@A");
    }

    public static MessageEncode a() {
        return new MessageEncode();
    }

    public MessageEncode a(String paramString1, String paramString2) {
        this.a = (this.a + a(paramString1) + "@=" + a(paramString2) + "/");
        return this;
    }

    public String b() {
        return this.a;
    }

    public ByteBuffer c() {
        ByteBuffer localByteBuffer1 = null;
        try {
            localByteBuffer1 = ByteBuffer.allocate(13 + this.a.getBytes("UTF-8").length);
            localByteBuffer1.order(ByteOrder.LITTLE_ENDIAN);
            localByteBuffer1.putInt(9 + this.a.getBytes("UTF-8").length);
            localByteBuffer1.putInt(9 + this.a.getBytes("UTF-8").length);
            localByteBuffer1.putShort((short) 689);
            localByteBuffer1.put(new byte[2]);
            localByteBuffer1.put(this.a.getBytes("UTF-8"));
            localByteBuffer1.put(new byte[1]);
            ByteBuffer localByteBuffer2 = localByteBuffer1;
            if (localByteBuffer2 != null)
                return localByteBuffer2;
        } catch (Throwable localThrowable) {
            while (true) {
                localThrowable.printStackTrace();
                ByteBuffer localByteBuffer2 = localByteBuffer1;
            }
        }
        return ByteBuffer.allocate(0);
    }
}