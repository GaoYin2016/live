package tv.yewai.live.douyu.vo;

import java.util.List;

/**
 *
 * {"error":0,"data":
 *      {"room_id":"333910",
 *       "room_src":"http:\/\/staticlive.douyutv.com\/upload\/web_pic\/0\/333910_1510171654_thumb.jpg",
 *       "cate_id":"133",
 *       "tags":"",
 *       "room_name":"\u5b9e\u529b\u5751\u7239\u554a",
 *       "vod_quality":"0",
 *       "show_status":"2",
 *       "subject":"",
 *       "show_time":"1445541957",
 *       "owner_uid":"21011525",
 *       "specific_catalog":"StarTv",
 *       "specific_status":"1",
 *       "online":0,
 *       "nickname":"ystar9",
 *       "show_details":"\u76f4\u64ad\u670b\u53cbQQ\u7fa4:8860620",
 *       "url":"\/StarTv",
 *       "game_url":"\/directory\/game\/yzzr",
 *       "game_name":"\u5fa1\u5b85\u804c\u4eba",
 *       "fans":"10",
 *       "rtmp_url":"",
 *       "rtmp_live":"",
 *       "rtmp_cdn":"",
 *       "rtmp_multi_bitrate":"",
 *       "owner_avatar":"http:\/\/uc.douyutv.com\/avatar.php?uid=21011525&size=big",
 *       "servers":[
 *              {"ip":"119.90.49.110","port":"8046"},
 *              {"ip":"119.90.49.105","port":"8021"},
 *              {"ip":"119.90.49.101","port":"8005"},
 *              {"ip":"119.90.49.106","port":"8026"},
 *              {"ip":"119.90.49.107","port":"8031"},
 *              {"ip":"119.90.49.109","port":"8044"},
 *              {"ip":"119.90.49.104","port":"8019"},
 *              {"ip":"119.90.49.108","port":"8036"},
 *              {"ip":"119.90.49.106","port":"8029"},
 *              {"ip":"119.90.49.102","port":"8009"}],
 *         "owner_weight":"100g",
 *         "use_p2p":"0"
 *       }
 *  }
 * Created by Star on 2015/10/23.
 */
public class DouyuApi {

    private String error;
    private Data data;

    public void setError(String error) {
        this.error = error;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public String getError() {
        return error;
    }

    public Data getData() {
        return data;
    }

    @Override
    public String toString() {
        return "DouyuApi{" +
                "error='" + error + '\'' +
                ", data=" + data +
                '}';
    }
}
