package tv.yewai.live.douyu.danmu;

import android.os.Handler;
import android.os.Message;
import org.apache.mina.core.service.IoHandler;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import java.util.Arrays;
import java.util.List;
import tv.yewai.live.douyu.utils.BigHexStringUtils;
import tv.yewai.live.douyu.utils.HexUtils;
import tv.yewai.live.douyu.utils.ServerUtil;
import tv.yewai.live.douyu.utils.SttDecoder;
import tv.yewai.live.douyu.vo.ContentServerVo;

public class LoginMinaHandler implements IoHandler {
    private String loginUser = "";
    private String loginPwd = "";
    private String gid = "";
    private List<ContentServerVo> contentServerList = null;
    private SttDecoder sttDecoder;
    private BigHexStringUtils bigHexStringUtils;
    private String roomid;
    private String loginUserUid;
    private Handler handler;
    private ContentMinaThread contentMina;
    private Thread contentMinaThead ;

    public LoginMinaHandler( Handler handler,String roomid,String loginUserUid,String loginPwd) {
        this.sttDecoder = new SttDecoder();
        this.bigHexStringUtils = new BigHexStringUtils();
        this.roomid = roomid;
        this.loginUserUid = loginUserUid;
        this.handler = handler;
        this.loginPwd = loginPwd;
    }

    public void exceptionCaught(IoSession session, Throwable arg1) throws Exception {
    }

    public void messageReceived(IoSession session, Object message) throws Exception {
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

                if (this.sttDecoder.GetItem("type").equals("loginres")) {
                    this.loginUser = this.sttDecoder.GetItem("username");
                }
                if (this.sttDecoder.GetItem("type").equals("msgrepeaterlist")) {
                    this.contentServerList = ServerUtil.QueryContentServerList(this.sttDecoder.GetItem("list"));
                }
                if (this.sttDecoder.GetItem("type").equals("setmsggroup")) {
                    Message msg = new Message();
                    this.gid = this.sttDecoder.GetItem("gid");
                    msg.obj = ("获取的登录用户名为: " + this.loginUser + "\n");
                    Thread.sleep(100L);
                    msg.obj = ((String) msg.obj) + "登陆的病床编号为: " + this.roomid + " 床\n";
                    Thread.sleep(100L);
                    msg.obj = ((String) msg.obj) + "加入的讨论群组为: " + this.gid + " 组\n";
                    Thread.sleep(100L);
                    contentMina = new ContentMinaThread(this.handler, this.contentServerList, this.roomid, this.loginUser, this.loginPwd,this.loginUserUid,this.gid, session);
                    contentMinaThead = new Thread(contentMina);
                    contentMinaThead.start();
                    msg.obj = ((String) msg.obj) + "小助手启动完毕!\n";
                    handler.sendMessage(msg);
                }

            } else if (this.bigHexStringUtils.getHexStr().replace(" ", "").endsWith("40532F00")) {
                this.bigHexStringUtils.clear();
            }
        }
    }

    public void messageSent(IoSession session, Object massage) throws Exception {

    }

    public void sessionClosed(IoSession session) throws Exception {
        Message msg = new Message();
        msg.obj = "CONNECT_login_session_close";
        handler.sendMessage(msg);
    }

    public void sessionCreated(IoSession session)
            throws Exception {
    }

    public void sessionIdle(IoSession session, IdleStatus arg1) throws Exception {
    }

    public void sessionOpened(IoSession session) throws Exception {
    }

    public void close(){
        if(null!=contentMina){
            contentMina.close();
            contentMina = null;
        }
        if(null!=contentMinaThead){
            contentMinaThead.isInterrupted();
            try {
                contentMinaThead.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            contentMinaThead = null;
        }
    }
}

