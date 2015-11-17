package tv.yewai.live.douyu.danmu;

import org.apache.mina.core.session.IoSession;
import java.util.Random;
import tv.yewai.live.douyu.utils.HexUtils;
import tv.yewai.live.douyu.utils.SttEncoder;


public class KeepLive  implements Runnable {
    private IoSession session;

    public KeepLive(IoSession session) {
        this.session = session;
    }

    public  void run() {
            SttEncoder sttEncoder = new SttEncoder();
            while (null!=session && !session.isClosing()&&!Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(5000L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                sttEncoder.Clear();
                sttEncoder.AddItem("type", "keeplive");
                Random random = new Random();
                String randomNum = random.nextInt(99) + "";
                if (randomNum.length() == 1) randomNum = randomNum + randomNum;
                sttEncoder.AddItem("tick", randomNum);
                this.session.write(HexUtils.setStringHeader("b2020000" + HexUtils.Bytes2HexStringLower(sttEncoder.GetResualt().getBytes()) + "00"));
            }
    }
}

