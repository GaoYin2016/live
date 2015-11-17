package tv.yewai.live.utils;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.Map;

/**
 * 设备通知工具 管理员用户名，密码必须为admin
 * Created by Star on 2015/10/23.
 */
public class H264EncoderNetUtil {

    private static H264EncoderNetUtil h;
    private static String ip ;      //IP
    private static String rtmpurl;  //URL ID
    private static String directory;//目录
    private static String param;    //参数

    //私有构造
    private H264EncoderNetUtil(String ip, String rtmpurl, String directory, String param){
        this.ip         = ip;
        this.rtmpurl    = rtmpurl;
        this.directory  = directory;
        this.param      = param;
    }

    /**
     * 建立工具
     * @return
     */
    public static H264EncoderNetUtil build(String ip,String rtmpurl,String directory,String param){
        synchronized (H264EncoderNetUtil.class){
            if(null == h){
                h = new H264EncoderNetUtil(ip,rtmpurl,directory,param);
                return h;
            }else{
                return h;
            }
        }
    }

    /**
     * 设置直播参数
     * flag true :主流 false 副流
     */
    public  void setHdmiRtmpUrl(boolean flag){
        try {
            String set_codec="";
            if(flag){
                 set_codec="http://"+this.ip+"/set_codec?type=hdmi&used=cbr&cbr_BitRate=3000&vbr_MinQp=5&vbr_MaxQp=32&vbr_MaxBitRate=3200&fixqp_IQp=5&fixqp_PQp=32&" +
                        "http_src=/hdmi&http_open=1&rtsp_src=/hdmi&rtsp_open=0&cast_ip=238.0.0.1&cast_port=1234&cast_open=0&input_method=main%20profile&input_fps=25" +
                        "&http_port=80&rtsp_port=554&des_width=960&des_height=540&a0_aac_bitrate=48000&venc_gop=30&a0_leftright=0" +
                        "&rtmp_server_ip=" + this.rtmpurl+"&rtmp_server_port=1935" +
                        "&rtmp_stream_name="+ URLEncoder.encode(this.param, "UTF-8")+"&rtmp_enable=1&a0_out_resample=44100&audio_type=1" +
                        "&rtmp_app_name="+this.directory+"&use_vlc_muxer=1&rtsp_g711=0&rtsp_tcp=1&onvif_enable=0&way2_mode=0&rtmp_user=" +
                        "&rtmp_pass=&" +
                        "rtmp_user_empty=1&rtmp_pass_empty=1&rtsp_only=0&_=1440398641073";
            }else{
                 set_codec="http://"+this.ip+"/set_codec?type=hdmi_ext&used=cbr&cbr_BitRate=3000&vbr_MinQp=5&vbr_MaxQp=32&vbr_MaxBitRate=3200&fixqp_IQp=5&fixqp_PQp=32&" +
                        "http_src=/hdmi_ext&http_open=1&rtsp_src=/hdmi_ext&rtsp_open=0&cast_ip=238.0.0.1&cast_port=1236&cast_open=0&input_method=main%20profile&input_fps=25" +
                        "&http_port=80&rtsp_port=554&attr_LumaVal=undefined&attr_ContrVal=undefined&attr_HueVal=undefined&attr_SatuVal=undefined&des_width=960&des_height=540" +
                        "&rtmp_stream_name_ext="+ URLEncoder.encode(this.param, "UTF-8")+"&rtmp_second_enable=1&mpegts_change_id=0&mpegts0_transport_stream_id=300&mpegts0_pmt_start_pid=480&mpegts0_start_pid=481&_=1442940443342";
            }

            //配置模拟浏览器
            Connection.Response res =  Jsoup.connect(set_codec).timeout(5000)
                    .header("Authorization","Basic YWRtaW46YWRtaW4=")
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:29.0) Gecko/20100101 Firefox/29.0")
                    .method(Connection.Method.GET).execute();

            String reset = "http://"+this.ip+"/reboot?_=1440399007908";
            Connection conn =  Jsoup.connect(reset).cookies(res.cookies()).timeout(5000);
            for(Map.Entry<String,String> entry : res.headers().entrySet()){
                conn.header(entry.getKey(),entry.getValue());
            }
            conn .execute();

            String qqq = "http://"+this.ip+"/set_codec?type=hdmi&osd0_x=820&osd0_y=62&osd0_size=8&osd0_alpha=128&osd0_txt=%20&_=1440401943329";
            Connection conn1 =  Jsoup.connect(qqq).cookies(res.cookies()).timeout(5000);
            for(Map.Entry<String,String> entry : res.headers().entrySet()){
                conn1.header(entry.getKey(),entry.getValue());
            }
            conn1 .execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     *   关直播室本地流
     *   flag true :主流 false  副流
     */
    public void setCloseStream(boolean flag){
        try{
            String set_codec= "";
            if(flag){
                set_codec = "http://"+this.ip+"/set_codec?type=hdmi&rtmp_enable=0";
            }else{
                set_codec = "http://"+this.ip+"/set_codec?type=hdmi_ext&rtmp_second_enable=0";
            }
            //配置模拟浏览器
            Connection.Response res =  Jsoup.connect(set_codec).timeout(5000)
                    .header("Authorization","Basic YWRtaW46YWRtaW4=")
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:29.0) Gecko/20100101 Firefox/29.0")
                    .method(Connection.Method.GET).execute();

            String reset = "http://"+this.ip+"/reboot?_=1440399007908";
            Connection conn =  Jsoup.connect(reset).cookies(res.cookies()).timeout(5000);
            for(Map.Entry<String,String> entry : res.headers().entrySet()){
                conn.header(entry.getKey(),entry.getValue());
            }
            conn .execute();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    /**
     *   解码器重启
     */
    public void reset(){
        try{
            String reset = "http://"+this.ip+"/reboot?_=1440399007908";
            Jsoup.connect(reset)  .header("Authorization", "Basic YWRtaW46YWRtaW4=")
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:29.0) Gecko/20100101 Firefox/29.0")
                    .method(Connection.Method.GET).timeout(5000).execute();
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
