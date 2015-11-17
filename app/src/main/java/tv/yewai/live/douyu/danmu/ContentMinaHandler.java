package tv.yewai.live.douyu.danmu;

import android.os.Handler;
import android.os.Message;
import org.apache.commons.lang.StringUtils;
import org.apache.mina.core.service.IoHandler;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

import tv.yewai.live.douyu.utils.BigHexStringUtils;
import tv.yewai.live.douyu.utils.HexUtils;
import tv.yewai.live.douyu.utils.ServerUtil;
import tv.yewai.live.douyu.utils.SttDecoder;
import tv.yewai.live.douyu.utils.SttEncoder;


public class ContentMinaHandler  implements IoHandler {
    private String loginUserUid;
    private Handler handler;
    private IoSession loginSession;
    private SttDecoder sttDecoder;
    private BigHexStringUtils bigHexStringUtils;
    private static final int[] lev_vals = new int[]{
            0,//菜鸟
            100,//黄铜5
            1000,
            5000,
            10000,
            20000,
            30000,
            40000,
            50000,
            60000,
            80000,
            100000,
            150000,
            200000,
            250000,
            300000,
            400000,
            500000,
            600000,
            700000,
            800000,
            1000000,
            1200000,
            1500000,
            2000000,
            3000000,
            5000000,
            8000000,
            11000000,
            14000000,
            17000000
    };

    public ContentMinaHandler(Handler handler, String loginUserUid, final IoSession loginSession) {
        this.handler = handler;
        this.loginSession = loginSession;
        this.sttDecoder = new SttDecoder();
        this.bigHexStringUtils = new BigHexStringUtils();
        this.loginUserUid = loginUserUid;
    }

    public void exceptionCaught(IoSession session, Throwable arg1) throws Exception {
    }

    public void messageReceived(IoSession session, Object message)  throws Exception {

        String hexMessage = message.toString().replace(" ", "");

        while (hexMessage.length() > 0) {

            int msgLength = Math.abs(HexUtils.getHexStringLength(hexMessage));

            if (msgLength + 16 > hexMessage.length()) {
                if (this.bigHexStringUtils.getHexStr().equals("")) {
                    this.bigHexStringUtils.addHexStr(hexMessage);
                    hexMessage = hexMessage.substring(hexMessage.length());
                } else {
                    int pLength = HexUtils.getHexStringLength(this.bigHexStringUtils.getHexStr()) + 16 - this.bigHexStringUtils.getHexStr().length();
                    this.bigHexStringUtils.addHexStr(hexMessage.substring(0, pLength));
                    hexMessage = hexMessage.substring(pLength);
                }
            } else {
                String hexMsg = hexMessage.substring(0, msgLength + 16);
                this.bigHexStringUtils.addHexStr(hexMsg);
                hexMessage = hexMessage.substring(msgLength + 16);
            }

            if (this.bigHexStringUtils.isFullHexStr()) {
                byte[] msgBytes = HexUtils.HexString2Bytes(this.bigHexStringUtils.getHexStr());
                String msgStr = new String(Arrays.copyOfRange(msgBytes, 12, msgBytes.length - 1), "UTF-8");
                this.bigHexStringUtils.clear();

                this.sttDecoder.Parse(msgStr);
                if (this.sttDecoder.GetItem("type").equals("chatmessage")) {
                        String content = this.sttDecoder.GetItem("content");

                        String snick = this.sttDecoder.GetItem("snick");
                        Message msg = new Message();
                        String tt = "{" + ServerUtil.toDate() + "}" + snick + " : " + content + " : |";
                        String client = this.sttDecoder.GetItem("ct");
                        if ("1".equals(client) || "2".equals(client)) {//移动端
                            tt += "[mobile]";
                        }
                        String sui = this.sttDecoder.GetItem("sui");
                        this.sttDecoder.Clear();
                        this.sttDecoder.Parse(sui);
                        String sender_rg = this.sttDecoder.GetItem("rg");
                        String sender_pg = this.sttDecoder.GetItem("pg");
                        if ("5".equals(sender_pg) || "2".equals(sender_pg)) {
                            tt += " ,欢迎超管大大 [sa]";
                        } else {
                            if ("4".equals(sender_rg)) {
                                tt += "[fg]";
                            } else if ("5".equals(sender_rg)) {
                                tt += "[zb]";
                            }
                        }
                        //酬勤等级
                        msg.obj = getCqLev(tt);
                        handler.sendMessage(msg);
                }
                if (this.sttDecoder.GetItem("type").equals("donateres")) {
                    String sui = this.sttDecoder.GetItem("sui");
                    this.sttDecoder.Clear();
                    this.sttDecoder.Parse(sui);
                    String nnn = this.sttDecoder.GetItem("nick");
                    String ul = getUserLev();
                    Message msg = new Message();
                    msg.obj = " : 感谢" + nnn + "赠送给主播一百鱼丸 : |[yw_x]" + ul;
                    handler.sendMessage(msg);
                    this.sttDecoder.Clear();
                }

                if (this.sttDecoder.GetItem("type").equals("onlinegift")) {//在线获得礼物
                    String snick = this.sttDecoder.GetItem("nn");
                    Message msg = new Message();
                    msg.obj = " : " + snick + "在第" + this.sttDecoder.GetItem("if") + "次在线领鱼丸时获得了" + this.sttDecoder.GetItem("sil") + "个鱼丸 : |[tz]";
                    handler.sendMessage(msg);
                    this.sttDecoder.Clear();

                }
                if (this.sttDecoder.GetItem("type").equals("blackres")) {//禁言
                    String dnick = this.sttDecoder.GetItem("dnick");
                    String snick = this.sttDecoder.GetItem("snick");
                    Message msg = new Message();
                    msg.obj = " : " + dnick + "被管理员" + snick + "禁言. : |[tz]";
                    handler.sendMessage(msg);
                    this.sttDecoder.Clear();
                }
                if (this.sttDecoder.GetItem("type").equals("ranklist")) {//酬勤榜变更
                    Message msg = new Message();
                    msg.obj = " : 酬勤榜变更 : |[tz]";
                    handler.sendMessage(msg);
                    this.sttDecoder.Clear();
                }
                if (this.sttDecoder.GetItem("type").equals("userenter")) {//来到直播间
                    String userinfo = this.sttDecoder.GetItem("userinfo");
                    this.sttDecoder.Clear();
                    this.sttDecoder.Parse(userinfo);
                    String snick = this.sttDecoder.GetItem("nick");
                    Message msg = new Message();
                    //酬勤等级
                    String tt = " : 欢迎 " + snick + " 来到直播间 : |[tz]";
                    msg.obj = getCqLev(tt);
                    handler.sendMessage(msg);
                    this.sttDecoder.Clear();
                }
                if ("dgn".equals(this.sttDecoder.GetItem("type"))) {//连击鱼丸或者赞或者520鱼丸或者666鱼翅或飞机或火箭
                    String snick = this.sttDecoder.GetItem("src_ncnm");
                    String hits = this.sttDecoder.GetItem("hits");
                    String gfid = this.sttDecoder.GetItem("gfid");
                    String ul = getUserLev();
                    Message msg = new Message();
                    if ("51".equals(gfid) || "57".equals(gfid)) {
                        msg.obj = gfid + " : 感谢" + snick + "赠送给主播一个赞" + hits + "连击.谢谢. : |[zan]" + ul;
                    } else if ("50".equals(gfid)) {
                        msg.obj = gfid + " : 感谢" + snick + "赠送给主播一百鱼丸" + hits + "连击.谢谢. : |[yw_d]" + ul;
                    } else if ("53".equals(gfid)) {
                        msg.obj = gfid + " : 感谢" + snick + "赠送给主播五二零鱼丸" + hits + "连击.谢谢. : |[520]" + ul;
                    } else if ("52".equals(gfid)) {
                        msg.obj = gfid + " : 感谢" + snick + "赠送给主播六鱼翅" + hits + "连击.谢谢. : |[66]" + ul;
                    } else if ("54".equals(gfid)) {
                        msg.obj = gfid + " : 感谢" + snick + "赠送给主播" + hits + "个大飞机.谢谢. : |[fj]" + ul;
                    } else if ("55".equals(gfid)) {
                        msg.obj = gfid + " : 感谢" + snick + "赠送给主播" + hits + "个火箭.谢谢. : |[hj]" + ul;
                    } else if ("56".equals(gfid)) {
                        msg.obj = gfid + " : 感谢" + snick + "赠送给主播" + hits + "个红灯笼.谢谢. : |[dl]" + ul;
                    } else {
                        msg.obj = gfid + " : 感谢送的礼物. : |";
                    }

                    handler.sendMessage(msg);
                    this.sttDecoder.Clear();
                }
                if (this.sttDecoder.GetItem("type").equals("bc_buy_deserve")) {//酬勤

                    String lev = this.sttDecoder.GetItem("lev");
                    String ul = getUserLev();

                    if ("4".equals(lev)) {
                        String sui = this.sttDecoder.GetItem("sui");
                        this.sttDecoder.Clear();
                        this.sttDecoder.Parse(sui);
                        String snick = this.sttDecoder.GetItem("nick");
                        Message msg = new Message();
                        msg.obj = " : 感谢" + snick + "的超级酬勤 : |[cq_4]" + ul;
                        handler.sendMessage(msg);
                    } else if ("3".equals(lev)) {
                        String sui = this.sttDecoder.GetItem("sui");
                        this.sttDecoder.Clear();
                        this.sttDecoder.Parse(sui);
                        String snick = this.sttDecoder.GetItem("nick");
                        Message msg = new Message();
                        msg.obj = " : 感谢" + snick + "的高级酬勤 : |[cq_3]" + ul;
                        handler.sendMessage(msg);
                    } else if ("2".equals(lev)) {
                        String sui = this.sttDecoder.GetItem("sui");
                        this.sttDecoder.Clear();
                        this.sttDecoder.Parse(sui);
                        String snick = this.sttDecoder.GetItem("nick");
                        Message msg = new Message();
                        msg.obj = " : 感谢" + snick + "的中级酬勤 : |[cq_2]" + ul;
                        handler.sendMessage(msg);
                    } else if ("1".equals(lev)) {
                        String sui = this.sttDecoder.GetItem("sui");
                        this.sttDecoder.Clear();
                        this.sttDecoder.Parse(sui);
                        String snick = this.sttDecoder.GetItem("nick");
                        Message msg = new Message();
                        msg.obj = " : 感谢" + snick + "的初级酬勤 : |[cq_1]" + ul;
                        handler.sendMessage(msg);
                    }

                }
            } else if (this.bigHexStringUtils.getHexStr().replace(" ", "").endsWith("40532F00")) {
                this.bigHexStringUtils.clear();
            }
        }
    }

    public void messageSent(IoSession session, Object massage)  throws Exception {
    }

    //取用户等级
    public String getUserLev() {
        //用户等级 经验值数组，对应等级 strength 就是经验值
        String sth = this.sttDecoder.GetItem("sth");
        String strength = this.sttDecoder.GetItem("strength");
        if (StringUtils.isNotBlank(sth)) {
            int t = 0;//等级索引
            int v = Integer.valueOf(sth);
            for (int i = 0; i < lev_vals.length; i++) {
                if (v < lev_vals[i]) {
                    break;
                } else {
                    t = i;
                }
            }
            return "[user" + (t + 1) + "]";
        } else if (StringUtils.isNotBlank(strength)) {
            int t = 0;//等级索引
            int v = Integer.valueOf(strength);
            for (int i = 0; i < lev_vals.length; i++) {
                if (v < lev_vals[i]) {
                    break;
                } else {
                    t = i;
                }
            }
            return "[user" + (t + 1) + "]";
        } else {
            return "";
        }
    }

    //酬勤等级
    public String getCqLev(String tt) {
        String lev = this.sttDecoder.GetItem("m_deserve_lev");
        String cq_cnt = this.sttDecoder.GetItem("cq_cnt");
        String best_dlev = this.sttDecoder.GetItem("best_dlev");
        if (StringUtils.isNotBlank(lev) && StringUtils.isNotBlank(cq_cnt) && StringUtils.isNotBlank(best_dlev)) {
            if (Integer.valueOf(lev) > 0 && Integer.valueOf(cq_cnt) > 0) {
                if ("1".equals(lev)) {
                    tt += "[cqlev1] x " + cq_cnt;
                } else if ("2".equals(lev)) {
                    tt += "[cqlev2] x " + cq_cnt;
                } else if ("3".equals(lev)) {
                    tt += "[cqlev3] x " + cq_cnt;
                } else if ("4".equals(lev)) {
                    tt += "[cqlev4] x " + cq_cnt;
                }
            } else {
                if (Integer.valueOf(best_dlev) > 0) {
                    tt += "[cqother]";
                }
            }
        }
        return tt;
    }

    @SuppressWarnings("deprecation")
    public void sessionClosed(IoSession session) throws Exception {
        Message msg = new Message();
        msg.obj = "CONNECT_danmu_sessoin_close";
        handler.sendMessage(msg);
        this.loginSession.close();
    }

    public void sessionCreated(IoSession session) throws Exception { }

    public void sessionIdle(IoSession session, IdleStatus arg1) throws Exception { }

    public void sessionOpened(IoSession session) throws Exception { }

}

