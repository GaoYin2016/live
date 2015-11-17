package tv.yewai.live.rtmp;

import android.content.Context;

/**
 * 截屏直播类
 * Created by Star on 2015/10/26.
 */
public class PublishHapper {

    private static  ClientManager c ;
    private static PublishAudio pa ;
    private static  PublishVideo pv;

    /**
     * "send3.douyutv.com","333910rKKbNTnGG1?wsSecret=56054b5fd256436ab6c36aea7cb5b593&wsTime=562e1487"
     * @param domain
     * @param rtmpParam
     * @param context
     */
    public static void start(String domain,String rtmpParam,Context context){
        c = new ClientManager(domain,rtmpParam);
        c.setRunning(true);
        c.setRecording(true);
        c.setMode(ClientManager.NETONLY);
        new Thread(c).start();
        pa = new PublishAudio(c);
        new Thread(pa).start();
        pv = new PublishVideo(c,context);
        new Thread(pv).start();
    }

    public static void stop(){
        if(null!=pa)
        pa.stopPublish();
        if(null!=pv)
        pv.setRunning(false);
        if(null!=c)
        c.setRunning(false);
    }
}
