package tv.yewai.live.rtmp;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.PixelFormat;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;

/**
 * 弹窗辅助类
 *
 * @ClassName WindowUtils
 *
 *
 */
public class WindowUtils {

    private static FloatView mView = null;
    private static WindowManager mWindowManager = null;
    private final static LayoutParams params = new LayoutParams();

    /**
     * 显示弹出框
     *
     * @param context
     * @param
     */
    public static void showPopupWindow(final String rtmpurl,final String rtmpparam ,final Activity context) {
        // 获取WindowManager
        mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        mView = new FloatView(rtmpurl,rtmpparam,context);
        mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mView.getFlag()) {
                    mView.changeCam(true);
                }else{
                    mView.changeCam(false);
                }
            }
        });
        // 类型
        params.type = LayoutParams.TYPE_SYSTEM_ALERT  ;

        // WindowManager.LayoutParams.TYPE_SYSTEM_ALERT

        // 设置flag

        int flags = LayoutParams.FLAG_ALT_FOCUSABLE_IM | LayoutParams.FLAG_NOT_TOUCH_MODAL| LayoutParams.FLAG_NOT_FOCUSABLE;
        // | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        // 如果设置了WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE，弹出的View收不到Back键的事件
        params.flags = flags;
        // 不设置这个弹出框的透明遮罩显示为黑色
        params.format = PixelFormat.TRANSLUCENT;
        // FLAG_NOT_TOUCH_MODAL不阻塞事件传递到后面的窗口
        // 设置 FLAG_NOT_FOCUSABLE 悬浮窗口较小时，后面的应用图标由不可长按变为可长按
        // 不设置这个flag的话，home页的划屏会有问题

        params.width = 960;
        params.height = 720;

        params.gravity = Gravity.TOP|Gravity.LEFT;
        params.setTitle("摄像头");



        mWindowManager.addView(mView, params);
    }

    /**
     * 隐藏弹出框
     */
    public static void hidePopupWindow() {
        if (null != mView) {
            mWindowManager.removeView(mView);
        }
    }

    public static void reViewWindow(){
        if(null!=mView){
            mView.changeOrientation();
        }

    }

    public static LayoutParams getParams(){
        return params;
    }

    public static WindowManager getWindowManager(){
        return mWindowManager;
    }
}
