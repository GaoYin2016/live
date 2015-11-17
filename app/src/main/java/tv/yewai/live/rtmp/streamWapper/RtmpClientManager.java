package tv.yewai.live.rtmp.streamWapper;

import android.util.Base64;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Timer;

import org.apache.mina.core.buffer.IoBuffer;
import org.red5.io.ITag;
import org.red5.io.flv.Tag;
import org.red5.io.utils.ObjectMap;
import org.red5.server.messaging.IMessage;
import org.red5.server.net.rtmp.INetStreamEventHandler;
import org.red5.server.net.rtmp.RTMPClient;
import org.red5.server.net.rtmp.event.AudioData;
import org.red5.server.net.rtmp.event.IRTMPEvent;
import org.red5.server.net.rtmp.event.Notify;
import org.red5.server.net.rtmp.event.VideoData;
import org.red5.server.service.IPendingServiceCall;
import org.red5.server.service.IPendingServiceCallback;
import org.red5.server.stream.message.RTMPMessage;

public class RtmpClientManager implements INetStreamEventHandler, IPendingServiceCallback {
    public RTMPClient rtmpClient;
    private int streamId;
    private String streamName = "live";
    public String appName = "live";
    public String mUser = "";
    public String mpassword = "";
    private List<IMessage> frameBuffer = new ArrayList();
    private int prevSize = 0;
    public static final byte[] PREFIX_AUDIO_FRAME = {-81,
            1};

    public static final byte[] PREFIX_VIDEO_KEYFRAME = {
            23, 1};

    public static final byte[] PREFIX_VIDEO_FRAME = {39,
            1};

    public static final byte[] AUDIO_CONFIG_FRAME_AAC_LC = {
            18, 16};

    public static final byte[] PREFIX_VIDEO_CONFIG_FRAME = {
            23};

    boolean audioIsFirst = false;
    boolean videoIsFirst = false;
    private byte[] audioDecoderBytes;
    private byte[] videoDecoderBytes;
    public String mhost;
    public int mport;
    public Ping ping;
    public volatile boolean isConnected = false;
    public Timer timer = null;
    public boolean isAuthenticationRequired;
    public String outputResult;
    private Object waiter;
    Thread pingThread = null;
    Thread pingoR = null;

    public RtmpClientManager() {
        this.ping = new Ping(this);
        this.waiter = new Object();
    }

    public String connectToServer(String host, int port, String stream, String AppName, String userName, String password) {
        this.mhost = host;
        this.mport = port;
        this.appName = AppName;
        this.mUser = userName;
        this.mpassword = password;
        this.streamName = stream;
        this.rtmpClient = new RTMPClient();
        Map defParams = this.rtmpClient.makeDefaultConnectionParams(host, port, this.appName);
        this.rtmpClient.connect(host, port, defParams, this, null);

        this.audioIsFirst = true;
        this.videoIsFirst = true;

        this.mhost.isEmpty();
        try {
            synchronized (this.waiter) {
                this.waiter.wait();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return this.outputResult;
    }

    public int disconnectFromServer() {
        this.rtmpClient.disconnect();
        return 0;
    }

    public void setAACDecoderSpecInfo(byte[] decSpecInfo) {
        this.audioDecoderBytes = decSpecInfo;
    }

    public void setH264DecoderSpecInfo(byte[] decSpecInfo) {
        this.videoDecoderBytes = decSpecInfo;
    }

    public synchronized void putAudioData(long timeStamp, byte[] buf, int size)
            throws Exception {
        if (this.isConnected) {
            ITag tag = null;
            IRTMPEvent msg = null;
            if (this.audioIsFirst) {
                IoBuffer body = null;

                body = IoBuffer.allocate(7);
                body.setAutoExpand(true);
                body.put(new byte[]{-81});
                if (this.audioDecoderBytes != null)
                    body.put(this.audioDecoderBytes);
                else {
                    body.put(AUDIO_CONFIG_FRAME_AAC_LC);
                }

                tag = new Tag((byte) 8, 0, body.position(), null, 0);
                body.flip();
                tag.setBody(body);

                this.audioIsFirst = false;
            } else {
                ByteBuffer data = ByteBuffer.allocate(size + 2);
                data.put(PREFIX_AUDIO_FRAME);
                System.arraycopy(buf, 0, data.array(), 2, size);
                IoBuffer payload = IoBuffer.wrap(data.array());

                tag = new Tag((byte) 8, (int) (timeStamp / 1000L),
                        payload.limit(), payload, this.prevSize);
                this.prevSize = tag.getBodySize();
            }

            int timestamp = tag.getTimestamp();
            msg = new AudioData(tag.getBody());
            msg.setTimestamp(timestamp);
            RTMPMessage rtmpMsg = new RTMPMessage();
            rtmpMsg.setBody(msg);
            synchronized (this) {
                try {
                    this.rtmpClient.publishStreamData(this.streamId, rtmpMsg);
                } catch (Throwable e) {
                    this.isConnected = false;
                    throw new Exception(e);
                }
            }
        }
    }

    public synchronized void putVideoData(long timeStamp, byte[] buf, int size, boolean keyframe)
            throws Exception {
        if (this.isConnected) {
            ITag tag = null;
            IRTMPEvent msg = null;
            if (this.videoIsFirst) {
                IoBuffer body = null;

                body = IoBuffer.allocate(41);
                body.setAutoExpand(true);
                body.put(PREFIX_VIDEO_CONFIG_FRAME);
                byte[] decd = {-64, 30, -1,-31};
                byte[] ext = {1, 66};
                body.put(ext);
                byte[] dd = new byte[decd.length + this.videoDecoderBytes.length];
                System.arraycopy(decd, 0, dd, 0, decd.length);
                System.arraycopy(this.videoDecoderBytes, 0, dd, decd.length, this.videoDecoderBytes.length);
                if (this.videoDecoderBytes != null) {
                    body.put(dd);
                }
                tag = new Tag((byte) 9, 0, body.position(), null, 0);
                body.flip();
                tag.setBody(body);
                int timestamp = tag.getTimestamp();
                msg = new VideoData(tag.getBody());
                msg.setTimestamp(timestamp);
                RTMPMessage rtmpMsg = new RTMPMessage();
                rtmpMsg.setBody(msg);
                synchronized (this) {
                    this.rtmpClient.publishStreamData(this.streamId, rtmpMsg);
                }
                this.videoIsFirst = false;
            }
            if ((buf[4] & 0x1F) == 1)
                keyframe = false;
            else
                keyframe = true;
            long datal = 0L;

            ByteBuffer data = ByteBuffer.allocate(size + 5);
            if (keyframe)
                data.put(PREFIX_VIDEO_KEYFRAME);
            else {
                data.put(PREFIX_VIDEO_FRAME);
            }

            data.put((byte) 0);
            data.put((byte) 0);
            data.put((byte) 0);
            System.arraycopy(buf, (int) datal, data.array(), 5, size);

            IoBuffer payload = IoBuffer.wrap(data.array());

            tag = new Tag((byte) 9, (int) (timeStamp / 1000L),
                    payload.limit(), payload, this.prevSize);

            this.prevSize = tag.getBodySize();

            int timestamp = tag.getTimestamp();
            msg = new VideoData(tag.getBody());
            msg.setTimestamp(timestamp);
            RTMPMessage rtmpMsg = new RTMPMessage();
            rtmpMsg.setBody(msg);
            synchronized (this) {
                try {
                    this.rtmpClient.publishStreamData(this.streamId, rtmpMsg);
                } catch (Throwable e) {
                    this.isConnected = false;
                    throw new Exception(e);
                }
            }
        }
    }

    public synchronized void resultReceived(IPendingServiceCall call) {
        Exception re = call.getException();

        if (re != null) {
            if (re.getCause().toString().contains("ConnectException")) {
                this.outputResult = "No connection to the server can be made. Check server status";
                synchronized (this.waiter) {
                    this.waiter.notifyAll();
                }

                return;
            }

            if (re.getCause().toString().contains("IOException")) {
                this.outputResult = "No connection to the server can be made. IOException";
                synchronized (this.waiter) {
                    this.waiter.notifyAll();
                }
                return;
            }
            if (re.getCause().toString().contains("WriteToClosedSessionException")) {
                this.outputResult = "No connection to the server can be made. Session was closed";
                synchronized (this.waiter) {
                    this.waiter.notifyAll();
                }
                return;
            }
            if (re.getCause().toString().contains("Network is unreachable")) {
                this.outputResult = "No connection to the server can be made. Check your network connection.";
                synchronized (this.waiter) {
                    this.waiter.notifyAll();
                }
                return;
            }
            if (re.getCause().toString().contains("NoRouteToHostException")) {
                this.outputResult = "No connection to the server can be made. Server not listening";
                synchronized (this.waiter) {
                    this.waiter.notifyAll();
                }
                return;
            }

            System.out.println(re.getMessage());
            System.out.flush();
            this.outputResult = ("No connection to the server can be made." + re.getMessage());
            this.isConnected = false;
            synchronized (this.waiter) {
                this.waiter.notifyAll();
            }
            return;
        }

        Object obj = call.getResult();
        if (obj != null) {
            String res = obj.toString();
            if (res.contains("Rejected")) {
                if (res.contains("code=403 need auth; authmod=adobe")) {
                    this.appName =
                            (this.appName +
                                    "?authmod=adobe&user=" + this.mUser);
                    this.isAuthenticationRequired = true;
                    stopThread(this.pingThread, 1000L);
                    this.pingThread = new Thread(new Ping(this));
                    this.pingThread.start();
                    return;
                }

                if (res.contains("reason=authfailed")) {
                    String[] appns = this.appName.split("&challenge=");
                    this.appName = appns[0];
                }

                String msg = res;
                String[] msgs = msg.split("&");
                if ((msgs[0].contains("reason=needauth")) &&
                        (msgs[0].contains("authmod=adobe"))) {
                    String[] userVal = msgs[1].split("user=");
                    String[] saltVal = msgs[2].split("salt=");
                    String[] challengeVal = msgs[3].split("challenge=");
                    String[] OpaqueTemp = msgs[4].split(",");
                    String[] OpaqueVal = OpaqueTemp[0].split("opaque=");


                    String User = userVal[1];
                    String Salt = saltVal[1];
                    String Opaque = OpaqueVal[1];
                    String salted1 = User + Salt + this.mpassword;
                    MessageDigest sha = null;
                    try {
                        sha = MessageDigest.getInstance("MD5");
                    } catch (NoSuchAlgorithmException localNoSuchAlgorithmException) {
                    }
                    try {
                        sha.update(salted1.getBytes("UTF8"));
                    } catch (UnsupportedEncodingException e1) {
                        e1.printStackTrace();
                    }
                    byte[] md5sum_val = sha.digest();
                    String salted2 = Base64.encodeToString(md5sum_val, Base64.DEFAULT);

                    Random randomGenerator = new Random();
                    long secureInitializer = randomGenerator.nextInt();
                    String challenge2_data = Long.toString(secureInitializer);
                    String challenge2 = null;
                    try {
                        challenge2 = Base64.encodeToString(challenge2_data.getBytes("UTF8"), Base64.DEFAULT);
                    } catch (UnsupportedEncodingException e1) {
                        e1.printStackTrace();
                    }

                    String str2 = salted2 + Opaque + challenge2;
                    MessageDigest sha1 = null;
                    try {
                        sha1 = MessageDigest.getInstance("MD5");
                    } catch (NoSuchAlgorithmException localNoSuchAlgorithmException1) {
                    }
                    try {
                        sha1.update(str2.getBytes("UTF8"));
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    md5sum_val = sha1.digest();
                    String response = Base64.encodeToString(md5sum_val, Base64.DEFAULT);

                    String pubToken = String.format(
                            "&challenge=%s&response=%s&opaque=%s", new Object[]{challenge2,
                                    response, Opaque});

                    this.appName += pubToken;
                }

                if (this.pingThread != null) {
                    while (this.pingThread.isAlive()) {
                        try {
                            this.pingThread.join(1000L);
                        } catch (InterruptedException localInterruptedException) {
                        }
                        if (this.pingThread.isAlive()) {
                            this.pingThread.interrupt();
                            try {
                                this.pingThread.join();
                            } catch (InterruptedException localInterruptedException1) {
                            }
                        }
                    }
                }
                this.pingThread = new Thread(new Ping(this));
                this.pingThread.start();

                return;
            }
        }
        if ("connect".equals(call.getServiceMethodName())) {
            this.rtmpClient.createStream(this);
        } else if ("createStream".equals(call.getServiceMethodName())) {
            Object result = call.getResult();
            if ((result instanceof Integer)) {
                Integer streamIdInt = (Integer) result;
                this.streamId = streamIdInt.intValue();
                String[] apps = this.appName.split("\\?");
                String app;
                if (apps.length >= 2)
                    app = apps[0];
                else
                    app = this.appName;
                this.rtmpClient.publish(streamIdInt.intValue(), this.streamName, app, this);
                this.isConnected = true;

                this.outputResult = "Connection Successed.";
                synchronized (this.waiter) {
                    this.waiter.notifyAll();
                }
            }
            this.rtmpClient.disconnect();

            this.outputResult = "Connection Disconnected.";
            synchronized (this.waiter) {
                this.waiter.notifyAll();
            }
        }
    }

    public synchronized void onStreamEvent(Notify notify) {
        ObjectMap map = (ObjectMap) notify.getCall().getArguments()[0];
        String code = (String) map.get("code");
        if ("NetStream.Publish.Start".equals(code))
            while (this.frameBuffer.size() > 0)
                this.rtmpClient.publishStreamData(this.streamId, (IMessage) this.frameBuffer.remove(0));
    }

    private void stopThread(Thread thread, long time) {
        if (thread != null) {
            while (thread.isAlive()) {
                try {
                    thread.join(time);
                } catch (InterruptedException localInterruptedException) {
                }
                if (thread.isAlive()) {
                    thread.interrupt();
                    try {
                        thread.join();
                    } catch (InterruptedException localInterruptedException1) {
                    }
                }
            }
        }
    }

    public void reConnect() {
        stopThread(this.pingThread, 1000L);
        this.pingThread = new Thread(new Ping(this));
        this.pingThread.start();
    }
}

