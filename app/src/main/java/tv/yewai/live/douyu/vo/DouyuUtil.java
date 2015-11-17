package tv.yewai.live.douyu.vo;

import android.util.Log;
import com.alibaba.fastjson.JSON;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 斗鱼开播工具
 * Created by Star on 2015/10/23.
 */
public class DouyuUtil {

    private static final String TAG = DouyuUtil.class.getSimpleName();

    /**
     * 取得时间戳 key
     * @return
     * @throws IOException
     */
    public static String getTimestamp()throws IOException{
        Connection.Response res1 =  Jsoup.connect("http://www.douyutv.com/api/v1/timestamp").timeout(5000).ignoreContentType(true).execute();
       return JSON.parseObject(res1.body()).getString("data");
    }

    //斗鱼登陆
    private static  Connection.Response  getLoginToken(String username,String password) throws IOException{
        Connection.Response res1 = Jsoup.connect("http://www.douyutv.com/api/v1/login?username=" + username + "&password=" + password + "&type=md5&client_sys=android").timeout(5000).ignoreContentType(true).execute();
        return  res1;
    }

    //斗鱼开播设置地址 0 url, 1 param
    public static String[] getRtmpAddress(String username,String password){
        try {
           String md5password = md5(password);
            String urlnumber = "22";//取rtmp服务器地址编号
            //登陆取得token
            Connection.Response res1 = getLoginToken(username,md5password);
            String res1body = res1 .body();
            com.alibaba.fastjson. JSONObject data = JSON.parseObject(res1body).getJSONObject("data");
            String token =data .getString("token");
            Log.d(TAG,"斗鱼直播登录成功.TOKEN=" + token);
            sleep(2000);
            //看看房间状态
            Connection.Response res5 = Jsoup.connect("http://www.douyutv.com/api/v1/my_room?aid=dytool&client_sys=android&time=&token="+token).timeout(5000).cookies(res1.cookies()).ignoreContentType(true).execute();
            String res5body = res5.body();
            com.alibaba.fastjson.JSONObject data10 = JSON.parseObject(res5body).getJSONObject("data");
            String status = data10.getString("show_status");
            String error10 = JSON.parseObject(res5body).getString("error");
            if("0".equals(error10)&&"2".equals(status)){//看看房间是否关闭着
                Log.d(TAG,"斗鱼房间已经关闭可以开始值播....");
                sleep(2000);
                Connection.Response res2 = Jsoup.connect("http://www.douyutv.com/api/v1/get_rtmplist?aid=dytool&client_sys=android&time=&token=" + token).cookies(res5.cookies()).timeout(5000).ignoreContentType(true).execute();//取出22 rtmp连接
                String res2body = res2.body();
                com.alibaba.fastjson.JSONObject rtmpobj =  JSON.parseObject(res2body).getJSONObject("data");
                com.alibaba.fastjson.JSONObject rtmpurls = rtmpobj.getJSONObject("list");
                String rtmp = rtmpurls.getString(urlnumber);//这里取22地址
                String fms_val = rtmpobj.getString("fms_val");
                //选地址后开播
                sleep(2000);
                Connection.Response res3 = Jsoup.connect("http://www.douyutv.com/api/v1/start_liveroom?aid=dytool&client_sys=android&time=&fms_val="+fms_val+"&rtmp_id="+urlnumber+"&token="+token).cookies(res2.cookies()).ignoreContentType(true).timeout(5000).execute();
                String res3body = res3.body();
                String error = JSON.parseObject(res3body).getString("error");
                if("0".equals(error)){//开始直播
                    Log.d(TAG,"取得斗鱼"+urlnumber+"地址,并开始直播.");
                    //需要重新取得房间地址因为开播已经变化了
                    sleep(2000);
                    Connection.Response res6 = Jsoup.connect("http://www.douyutv.com/api/v1/my_room?aid=dytool&client_sys=android&time=&token="+token).timeout(5000).cookies(res3.cookies()).ignoreContentType(true).execute();
                    String res6body = res6.body();
                    com.alibaba.fastjson.JSONObject data11 = JSON.parseObject(res6body).getJSONObject("data");
                    String error11 = JSON.parseObject(res6body).getString("error");
                    if("0".equals(error11)){
                        Log.d(TAG,"斗鱼直播开启获取地址成功开始直播.");
                        return new String[]{data11.getString("rtmp_send_url"),data11.getString("rtmp_send_live"),data11.getString("room_id")};
                    }
                }
            }
        }catch(Exception o){/** **/ }
        while(!closeDouyutv(username,password)){sleep(5000);}//循环关闭直播
        return getRtmpAddress(username, password);//重新设置地址
    }
    //关闭斗鱼
    public static Boolean closeDouyutv(String username,String password){
        try{
           String  md5password = md5(password);
            //登陆取得token
            Connection.Response res1 = getLoginToken(username, md5password);
            String res1body = res1 .body();
            com.alibaba.fastjson. JSONObject data = JSON.parseObject(res1body).getJSONObject("data");
            String token =data .getString("token");
            sleep(2000);
            Connection.Response res5 = Jsoup.connect("http://www.douyutv.com/api/v1/my_room?aid=dytool&client_sys=android&time=&token="+token).cookies(res1.cookies()).timeout(5000).ignoreContentType(true).execute();
            String res5body = res5.body();
            com.alibaba.fastjson.JSONObject data10 = JSON.parseObject(res5body).getJSONObject("data");
            String status = data10.getString("show_status");
            String error10 = JSON.parseObject(res5body).getString("error");
            if("0".equals(error10)&&"1".equals(status)) {//看看房间是否正在直播着
                sleep(2000);
                Connection.Response res4 = Jsoup.connect("http://www.douyutv.com/api/v1/zhubo_closeroom?aid=dytool&client_sys=android&time=&token="+token).cookies(res5.cookies()).ignoreContentType(true).timeout(5000).execute();
                String res4body = res4.body();
                String error1 = JSON.parseObject(res4body).getString("error");
                //612已经关闭直播
                if("0".equals(error1) || "612".equals(error1)){
                    return true;
                }
            }
            else if("0".equals(error10)&&"2".equals(status)){//2 已经关闭
                return true;
            }
        }catch(Exception o){
           //
        }
        return false;
    }

    /**
     * md5 密码加密
     * @param string
     * @return
     */
    public static String md5(String string) {
        byte[] hash;
        try {
            hash = MessageDigest.getInstance("MD5").digest(string.getBytes("UTF-8"));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Huh, MD5 should be supported?", e);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Huh, UTF-8 should be supported?", e);
        }
        StringBuilder hex = new StringBuilder(hash.length * 2);
        for (byte b : hash) {
            if ((b & 0xFF) < 0x10) hex.append("0");
            hex.append(Integer.toHexString(b & 0xFF));
        }
        return hex.toString();
    }

    private static void sleep(int timeout){
        try { Thread.sleep(timeout); } catch (InterruptedException e) { /** **/ }
    }


    /**
     * 衩裙斗鱼api
     * @return
     */
    public static DouyuApi queryDouyuApi(String roomName)throws  Exception {
        Connection.Response res =  Jsoup.connect("http://www.douyutv.com/api/client/room/" + roomName + "?cdn=ws").method(Connection.Method.GET).execute();
        String body = res.body();
        DouyuApi douyuApi = JSON.parseObject(body, DouyuApi.class);
        return douyuApi;
    }

    /**
     * 返回直播对象
     * @param username
     * @param password
     * @return
     * @throws IOException
     */
    public static DouyuZhiboApi getRoomInfo(String username,String password) throws IOException {
        String md5password = md5(password);
        Connection.Response res1 = getLoginToken(username, md5password);
        String res1body = res1 .body();
        com.alibaba.fastjson. JSONObject data = JSON.parseObject(res1body).getJSONObject("data");
        DouyuZhiboApi dza = JSON.parseObject(data.toJSONString(), DouyuZhiboApi.class);
        return dza ;
    }
}
