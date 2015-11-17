package tv.yewai.live.douyu.vo;

import java.util.List;

/**
 * Created by Star on 2015/10/23.
 */
public class Data {

    private  String room_id ; //房间号
    private  String room_src ;
    private  String cate_id ;
    private  String tags ;
    private  String room_name ; //房间名
    private  String vod_quality ;
    private  String show_status ;
    private  String subject ;
    private  String show_time ;
    private  String owner_uid ;
    private  String specific_catalog ;
    private  String specific_status ;
    private  String online ;//是否在线
    private  String nickname ;
    private  String show_details ;
    private  String url ;
    private  String game_url ;
    private  String game_name ;
    private  String fans ;//粉丝
    private  String rtmp_url ;
    private  String rtmp_live ;
    private  String rtmp_cdn ;
    private  String rtmp_multi_bitrate ;
    private  String owner_avatar ;
    private  List<Server> servers;//登录服务器列表
    private  String owner_weight ;//鱼丸体重
    private  String use_p2p ;

    public void setRoom_id(String room_id) {
        this.room_id = room_id;
    }

    public void setRoom_src(String room_src) {
        this.room_src = room_src;
    }

    public void setCate_id(String cate_id) {
        this.cate_id = cate_id;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public void setRoom_name(String room_name) {
        this.room_name = room_name;
    }

    public void setVod_quality(String vod_quality) {
        this.vod_quality = vod_quality;
    }

    public void setShow_status(String show_status) {
        this.show_status = show_status;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public void setShow_time(String show_time) {
        this.show_time = show_time;
    }

    public void setOwner_uid(String owner_uid) {
        this.owner_uid = owner_uid;
    }

    public void setSpecific_catalog(String specific_catalog) {
        this.specific_catalog = specific_catalog;
    }

    public void setSpecific_status(String specific_status) {
        this.specific_status = specific_status;
    }

    public void setOnline(String online) {
        this.online = online;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public void setShow_details(String show_details) {
        this.show_details = show_details;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setGame_url(String game_url) {
        this.game_url = game_url;
    }

    public void setGame_name(String game_name) {
        this.game_name = game_name;
    }

    public void setFans(String fans) {
        this.fans = fans;
    }

    public void setRtmp_url(String rtmp_url) {
        this.rtmp_url = rtmp_url;
    }

    public void setRtmp_live(String rtmp_live) {
        this.rtmp_live = rtmp_live;
    }

    public void setRtmp_cdn(String rtmp_cdn) {
        this.rtmp_cdn = rtmp_cdn;
    }

    public void setRtmp_multi_bitrate(String rtmp_multi_bitrate) {
        this.rtmp_multi_bitrate = rtmp_multi_bitrate;
    }

    public void setOwner_avatar(String owner_avatar) {
        this.owner_avatar = owner_avatar;
    }

    public void setServers(List<Server> servers) {
        this.servers = servers;
    }

    public void setOwner_weight(String owner_weight) {
        this.owner_weight = owner_weight;
    }

    public void setUse_p2p(String use_p2p) {
        this.use_p2p = use_p2p;
    }

    public String getRoom_id() {
        return room_id;
    }

    public String getRoom_src() {
        return room_src;
    }

    public String getCate_id() {
        return cate_id;
    }

    public String getTags() {
        return tags;
    }

    public String getRoom_name() {
        return room_name;
    }

    public String getVod_quality() {
        return vod_quality;
    }

    public String getShow_status() {
        return show_status;
    }

    public String getSubject() {
        return subject;
    }

    public String getShow_time() {
        return show_time;
    }

    public String getOwner_uid() {
        return owner_uid;
    }

    public String getSpecific_catalog() {
        return specific_catalog;
    }

    public String getSpecific_status() {
        return specific_status;
    }

    public String getOnline() {
        return online;
    }

    public String getNickname() {
        return nickname;
    }

    public String getShow_details() {
        return show_details;
    }

    public String getUrl() {
        return url;
    }

    public String getGame_url() {
        return game_url;
    }

    public String getGame_name() {
        return game_name;
    }

    public String getFans() {
        return fans;
    }

    public String getRtmp_url() {
        return rtmp_url;
    }

    public String getRtmp_live() {
        return rtmp_live;
    }

    public String getRtmp_cdn() {
        return rtmp_cdn;
    }

    public String getRtmp_multi_bitrate() {
        return rtmp_multi_bitrate;
    }

    public String getOwner_avatar() {
        return owner_avatar;
    }

    public List<Server> getServers() {
        return servers;
    }

    public String getOwner_weight() {
        return owner_weight;
    }

    public String getUse_p2p() {
        return use_p2p;
    }

    @Override
    public String toString() {
        return "Data{" +
                "room_id='" + room_id + '\'' +
                ", room_src='" + room_src + '\'' +
                ", cate_id='" + cate_id + '\'' +
                ", tags='" + tags + '\'' +
                ", room_name='" + room_name + '\'' +
                ", vod_quality='" + vod_quality + '\'' +
                ", show_status='" + show_status + '\'' +
                ", subject='" + subject + '\'' +
                ", show_time='" + show_time + '\'' +
                ", owner_uid='" + owner_uid + '\'' +
                ", specific_catalog='" + specific_catalog + '\'' +
                ", specific_status='" + specific_status + '\'' +
                ", online='" + online + '\'' +
                ", nickname='" + nickname + '\'' +
                ", show_details='" + show_details + '\'' +
                ", url='" + url + '\'' +
                ", game_url='" + game_url + '\'' +
                ", game_name='" + game_name + '\'' +
                ", fans='" + fans + '\'' +
                ", rtmp_url='" + rtmp_url + '\'' +
                ", rtmp_live='" + rtmp_live + '\'' +
                ", rtmp_cdn='" + rtmp_cdn + '\'' +
                ", rtmp_multi_bitrate='" + rtmp_multi_bitrate + '\'' +
                ", owner_avatar='" + owner_avatar + '\'' +
                ", servers=" + servers+
                ", owner_weight='" + owner_weight + '\'' +
                ", use_p2p='" + use_p2p + '\'' +
                '}';
    }
}
