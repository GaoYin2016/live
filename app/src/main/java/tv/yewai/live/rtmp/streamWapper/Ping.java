 package tv.yewai.live.rtmp.streamWapper;
 
 import java.util.Map;
 import org.red5.server.net.rtmp.RTMPClient;
 
 class Ping
   implements Runnable
 {
   private RtmpClientManager rtmpClientmgr;
 
   Ping(RtmpClientManager rtmpC)
   {
     this.rtmpClientmgr = rtmpC;
   }
 
   public synchronized void run()
   {
     this.rtmpClientmgr.rtmpClient = new RTMPClient();
     Map defParams = this.rtmpClientmgr.rtmpClient.makeDefaultConnectionParams(
       this.rtmpClientmgr.mhost, this.rtmpClientmgr.mport, this.rtmpClientmgr.appName);
     this.rtmpClientmgr.rtmpClient.connect(this.rtmpClientmgr.mhost, this.rtmpClientmgr.mport, defParams, this.rtmpClientmgr, null);
     this.rtmpClientmgr.audioIsFirst = true;
     this.rtmpClientmgr.videoIsFirst = true;
   }
 }

