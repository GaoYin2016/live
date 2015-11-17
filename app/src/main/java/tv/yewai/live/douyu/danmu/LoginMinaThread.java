package tv.yewai.live.douyu.danmu;

import android.os.Handler;
import android.os.Message;

import org.apache.mina.core.filterchain.DefaultIoFilterChainBuilder;
import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.service.IoConnector;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.nio.NioSocketConnector;
import java.net.InetSocketAddress;
import java.util.List;
import tv.yewai.live.douyu.vo.DouyuUtil;
import tv.yewai.live.douyu.utils.HexCodecFactory;
import tv.yewai.live.douyu.utils.HexUtils;
import tv.yewai.live.douyu.utils.SttEncoder;
import tv.yewai.live.douyu.vo.DouyuApi;
import tv.yewai.live.douyu.vo.DouyuZhiboApi;
import tv.yewai.live.douyu.vo.Server;

public class LoginMinaThread implements Runnable {
    private String Server1_Host;
    private int Server1_Port;
    private SttEncoder sttEncoder;
    private String roomid;
    private String loginUser ;//ystar8 = "auto_pf2OS2PHD9" 或者为空串 ""
    private String loginPwd ; //2941a9f7f0197f032b235d75031da5e0  或者为空串 ""
    private String loginUserUid; //;= "14130425";
    private Handler handler;
    private  IoSession session = null;
    private  Thread keepliveThread;
    private  KeepLive keeplive;
    private LoginMinaHandler loginMinaHandler;

    public LoginMinaThread(Handler handler , String roomName,String username,String password) throws Exception {

        this.loginUser = "";
        this.loginUserUid = "";//之前发信息用来着
        this.loginPwd = "";
        this.handler = handler;
        this.sttEncoder = new SttEncoder();

        DouyuApi douyuApi = DouyuUtil.queryDouyuApi(roomName);

        if(null == douyuApi){
            throw new Exception("douyu api null.");
        }

        if(null == douyuApi.getData()){
            throw new Exception("douyu serviers  null.");
        }
        List<Server> serverips = douyuApi.getData().getServers();
        Server loginServerVo = (Server) serverips.get((int) (Math.random() * serverips.size()));
        this.Server1_Host = loginServerVo.getIp();
        this.Server1_Port = Integer.parseInt(loginServerVo.getPort());
        this.roomid = douyuApi.getData().getRoom_id();

        Message msg = new Message();
        msg.obj = "随机选择登陆服务器 " + this.Server1_Host + ":" + this.Server1_Port;
        handler.sendMessage(msg);
    }

    public void run() {
        if ("".equals(roomid)) {
            Message msg = new Message();
            msg.obj = "房间不存在,请重新输入房间名称.";
            handler.sendMessage(msg);
        } else {
            IoConnector connector = new NioSocketConnector();
            DefaultIoFilterChainBuilder chain = connector.getFilterChain();
            chain.addLast("codec", new ProtocolCodecFilter(new HexCodecFactory()));
            loginMinaHandler =  new LoginMinaHandler(this.handler,this.roomid,this.loginUserUid,this.loginPwd);
            connector.setHandler(loginMinaHandler);

            try {
                ConnectFuture future = connector.connect(new InetSocketAddress(this.Server1_Host, this.Server1_Port));
                future.awaitUninterruptibly();
                session = future.getSession();

                this.sttEncoder.Clear();
                this.sttEncoder.AddItem("type", "loginreq");
                this.sttEncoder.AddItem("username",this.loginUser);
                this.sttEncoder.AddItem("password", this.loginPwd);
                this.sttEncoder.AddItem("roomid", roomid);

                session.write(HexUtils.setStringHeader("b1020000" + HexUtils.Bytes2HexStringLower(this.sttEncoder.GetResualt().getBytes("UTF-8")) + "00"));
                Thread.sleep(3000L);

                this.sttEncoder.Clear();
                this.sttEncoder.AddItem("type", "roomrefresh");
                this.sttEncoder.AddItem("serialnum", "0");
                session.write(HexUtils.setStringHeader("b1020000" + HexUtils.Bytes2HexStringLower(this.sttEncoder.GetResualt().getBytes("UTF-8")) + "00"));
                Thread.sleep(1000L);

                keeplive = new KeepLive(session);
                keepliveThread = new Thread(keeplive);
                keepliveThread.start();
            } catch (Exception e) {
                Message msg = new Message();
                msg.obj = "CONNECT_login";
                handler.sendMessage(msg);
            }
        }
    }

    public void close(){
        if(null!=loginMinaHandler){
            loginMinaHandler.close();
            loginMinaHandler = null;
        }
        if(null!=session){
            session.close(true);
            session = null;
        }
        if(null!=keepliveThread){
            keepliveThread.interrupt();
            try {
                keepliveThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            keepliveThread = null;
        }
    }
}