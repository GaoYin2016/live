package tv.yewai.live;

import android.media.projection.MediaProjectionManager;

/**
 * 系统设置
 * Created by Star on 2015/10/23.
 */
public class Config {

    private boolean ttsflag;//tts 开关
    private boolean houtaiflag;//后台开关

    private String rtmpurl = "rtmp://127.0.0.1/appName";//直播服务器地址
    private String rtmpparam = "streamName";//直播流名称

    public String getRtmpurl() {
        return rtmpurl;
    }

    public void setRtmpurl(String rtmpurl) {
        this.rtmpurl = rtmpurl;
    }

    public String getRtmpparam() {
        return rtmpparam;
    }

    public void setRtmpparam(String rtmpparam) {
        this.rtmpparam = rtmpparam;
    }

    private MediaProjectionManager mMediaProjectionManager;

    public boolean isHoutaiflag() {
        return houtaiflag;
    }

    public void setHoutaiflag(boolean houtaiflag) {
        this.houtaiflag = houtaiflag;
    }

    public boolean isTtsflag() {
        return ttsflag;
    }

    public void setTtsflag(boolean ttsflag) {
        this.ttsflag = ttsflag;
    }

    public MediaProjectionManager getmMediaProjectionManager() {
        return mMediaProjectionManager;
    }

    public void setmMediaProjectionManager(MediaProjectionManager mMediaProjectionManager) {
        this.mMediaProjectionManager = mMediaProjectionManager;
    }
}
