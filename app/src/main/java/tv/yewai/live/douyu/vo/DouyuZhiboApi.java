package tv.yewai.live.douyu.vo;

/**
 * {"error":0,
 *      "data":{"
 *       uid":"21011525",
 *      "username":"auto_99NQojMWDE",
 *      "nickname":"ystar9",
 *      "email":"52480****@qq.com",
 *      "qq":"",
 *      "mobile_phone":"189****9889",
 *      "phone_status":"1",
 *      "email_status":"1",
 *      "lastlogin":"1445596261",
 *      "avatar":
 *          {
 *              "small":"http:\/\/uc.douyutv.com\/avatar.php?uid=21011525&size=small",
 *              "middle":"http:\/\/uc.douyutv.com\/avatar.php?uid=21011525&size=middle",
 *              "big":"http:\/\/uc.douyutv.com\/avatar.php?uid=21011525&size=big"
 *          },
 *       "has_room":"1",
 *       "gold1":"90",
 *       "score":"1600",
 *       "level":{
 *              "current":
 *                  {
 *                      "lv":3,
 *                      "pic":"user3.gif",
 *                      "mpic":"brass04.png",
 *                      "name":"\u9ec4\u94dc4",
 *                      "pic_url":"http:\/\/staticlive.douyutv.com\/common\/douyu\/images\/classimg\/user3.gif?v=t12729",
 *                      "score":1000
 *                   },
 *                   "next":{
 *                          "lv":4,
 *                          "pic":"user4.gif",
 *                          "mpic":"brass03.png",
 *                          "name":"\u9ec4\u94dc3",
 *                          "pic_url":"http:\/\/staticlive.douyutv.com\/common\/douyu\/images\/classimg\/user4.gif?v=t12729",
 *                          "score":5000
 *                    }
 *        },
 *        "follow":"4",
 *        "ios_gold_switch":1,
 *        "gold":0,
 *        "token":"8c3cd93d795df864",
 *        "token_exp":1446471176
 *     }
 * }
 * Created by Star on 2015/10/23.
 */
public class DouyuZhiboApi {

    private String uid;
    private String username;
    private String nickname;

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
