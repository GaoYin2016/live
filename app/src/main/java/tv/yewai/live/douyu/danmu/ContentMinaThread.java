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
import tv.yewai.live.douyu.utils.HexCodecFactory;
import tv.yewai.live.douyu.utils.HexUtils;
import tv.yewai.live.douyu.utils.SttEncoder;
import tv.yewai.live.douyu.vo.ContentServerVo;

public class ContentMinaThread implements Runnable {
    private static String Server2_Host;
    private static int Server2_Port;
    private String loginUser;
    private String loginPwd;
    private String loginUserUid;
    private String gid;
    private IoSession loginSession;
    private SttEncoder sttEncoder;
    private String roomid;
    private Handler handler;
    private IoSession session = null;
    private KeepLive keeplive;
    private Thread keepliveThread;
    private ContentMinaHandler contentMinaHandler;

    public ContentMinaThread(Handler handler, List<ContentServerVo> contentServerList, String roomid, String loginUser, String loginPwd,String loginUserUid,String gid, IoSession loginSession) {
        ContentServerVo cntentServerVo = (ContentServerVo) contentServerList.get((int) (Math.random() * contentServerList.size()));
        Server2_Host = cntentServerVo.getIp();
        Server2_Port = Integer.parseInt(cntentServerVo.getPort());
        this.roomid = roomid;
        this.loginUser = loginUser;
        this.loginPwd = loginPwd;
        this.loginUserUid = loginUserUid;
        this.gid = gid;
        this.loginSession = loginSession;
        this.sttEncoder = new SttEncoder();
        this.handler = handler;
    }

    public void run() {
        IoConnector connector = new NioSocketConnector();
        DefaultIoFilterChainBuilder chain = connector.getFilterChain();
        chain.addLast("codec", new ProtocolCodecFilter(new HexCodecFactory()));
        contentMinaHandler = new ContentMinaHandler(this.handler ,this.loginUserUid, this.loginSession);
        connector.setHandler(contentMinaHandler);
        try {
            ConnectFuture future = connector.connect(new InetSocketAddress(Server2_Host, Server2_Port));
            future.awaitUninterruptibly();
            session = future.getSession();

            this.sttEncoder.Clear();
            this.sttEncoder.AddItem("type", "loginreq");
            this.sttEncoder.AddItem("username", this.loginUser); // 或者为空串 ""
            this.sttEncoder.AddItem("password", this.loginPwd);// 空串密码 1234567890123456
            this.sttEncoder.AddItem("roomid", roomid);
            session.write(HexUtils.setStringHeader("b1020000" + HexUtils.Bytes2HexStringLower(this.sttEncoder.GetResualt().getBytes("UTF-8")) + "00"));
            Thread.sleep(1000L);

            this.sttEncoder.Clear();
            this.sttEncoder.AddItem("type", "joingroup");
            this.sttEncoder.AddItem("rid", roomid);
            this.sttEncoder.AddItem("gid", this.gid);
            session.write(HexUtils.setStringHeader("b1020000" + HexUtils.Bytes2HexStringLower(this.sttEncoder.GetResualt().getBytes("UTF-8")) + "00"));

            keeplive = new KeepLive(session);
            keepliveThread = new Thread(keeplive);
            keepliveThread.start();
        } catch (Exception e) {
            Message msg = new Message();
            msg.obj =  "CONNECT_danmu";
            handler.sendMessage(msg);
        }
    }

    public void close(){
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

