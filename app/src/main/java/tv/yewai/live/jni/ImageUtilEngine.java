package tv.yewai.live.jni;

import android.content.Context;

public class ImageUtilEngine {

    static {
        System.loadLibrary("JniToolKit");
    }

    public static native int[] decodeYUV420SP(byte[] buf, int width, int heigth);

    //public static native String call(Context context ,String param,String param2);
}
