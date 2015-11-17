package tv.yewai.live;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.projection.MediaProjection;
import android.os.Build;
import android.os.Bundle;
import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.method.ScrollingMovementMethod;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.SDKInitializer;

import org.apache.commons.lang.StringUtils;

import tv.yewai.live.baidu.OfflineDemo;
import tv.yewai.live.douyu.danmu.LoginMinaThread;
import tv.yewai.live.douyu.vo.DouyuUtil;
import tv.yewai.live.douyu.utils.ServerUtil;
import tv.yewai.live.douyu.vo.DouyuApi;
import tv.yewai.live.update.CheckVersionTask;
import tv.yewai.live.utils.CaptureUtil;
import tv.yewai.live.utils.H264EncoderNetUtil;
import tv.yewai.live.utils.SerialUtil;
import tv.yewai.live.utils.TtsUtil;
import tv.yewai.live.rtmp.WindowUtils;

public class DanmuActivity extends Activity implements View.OnClickListener {

    private boolean scrollflag = true;//是否滚动
    private boolean isConnectState ;//连接状态
    private LoginMinaThread loginMinaThread;
    private static String username = "";
    private static String password = "";
    //判断当前系统版本
    private final boolean android5 = Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP;
    private TextView textView;//弹幕区
    private EditText roomName;//房间名
    private Button connect;//连接按钮
    private TextView info;//房间信息
    private Button houtai;//后台运行
    private Button mapManger;//百度地图管理
    private Button clear;//清平
    private CheckBox tts;//TTS
    private Button kjfs;//快捷方式
    private Button black;//后台运行
    private CheckBox zhibo;//直播 h264 设置
    private CheckBox caplive;//截屏直播
    private Thread textthread;//弹幕线程

    private Handler handler;//弹幕handler
    private Handler infoHandler;//房间信息handler
    private ScrollView svResult;//弹幕内容滚动条
    private Button liveadd;//直播地址设置

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);//去掉标题栏
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);//全屏

        //在使用SDK各组件之前初始化context信息，传入ApplicationContext
        //注意该方法要再setContentView方法之前实现
        SDKInitializer.initialize(getApplicationContext());

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_danmu_portrait);

        /**
         * android.os.NetworkOnMainThreadException
         * 主线程操作网络
         */
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        if (android5) {
            //浮动截屏
            CaptureUtil.build(this);
        }

        houtai = (Button) findViewById(R.id.houtai);//后台通知Butt运行
        houtai.setOnClickListener(this);

        tts = (CheckBox) findViewById(R.id.tts);//TTS
        tts.setOnClickListener(this);

        kjfs = (Button) findViewById(R.id.kjfs);//快捷方式
        kjfs.setOnClickListener(this);

        black = (Button) findViewById(R.id.black);//后台运行
        black.setOnClickListener(this);

        clear = (Button) findViewById(R.id.clear);//清平
        clear.setOnClickListener(this);

        caplive = (CheckBox) findViewById(R.id.caplive);//TTS
        caplive.setOnClickListener(this);

        liveadd = (Button) findViewById(R.id.liveadd);//
        liveadd.setOnClickListener(this);

        mapManger = (Button) findViewById(R.id.mapManger);//
        mapManger.setOnClickListener(this);

        zhibo = (CheckBox) findViewById(R.id.zhibo);//直播 h264 设置
        zhibo.setOnClickListener(this);

        roomName = (EditText) findViewById(R.id.roomName);
        connect = (Button) findViewById(R.id.connect);

        //连接
        connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isConnectState){
                    //房间名称
                    final String roomname = roomName.getText().toString();

                    if (StringUtils.isBlank(roomname)) {
                        new AlertDialog.Builder(DanmuActivity.this).setMessage("房间名称不能为空.").setTitle("提示信息")
                                .setPositiveButton("确定", null).show();
                        return;
                    }

                    if (!SerialUtil.checkNetworkAvailable(DanmuActivity.this)) {
                        new AlertDialog.Builder(DanmuActivity.this).setMessage("对不起检测你网络连接失败\n无法上网,请连接后再试.").setTitle("提示信息")
                                .setPositiveButton("确定", null).show();
                        return;
                    }

                    roomName.setEnabled(false);
                    connect.setText("断开");

                    //弹幕显示
                    textView = (TextView) findViewById(R.id.danmu);
                    textView.setText("http://www.yewai.tv | tiny@gtv.so \n");
                    textView.setAutoLinkMask(Linkify.ALL);
                    textView.setMovementMethod(ScrollingMovementMethod.getInstance());
                    textView.setScrollbarFadingEnabled(false);
                    textView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (scrollflag) {
                                scrollflag = false;
                                Toast.makeText(DanmuActivity.this, "自动滚动已关闭,再次点击开启.", Toast.LENGTH_LONG).show();
                            } else {
                                scrollflag = true;
                                Toast.makeText(DanmuActivity.this, "自动滚动已开启,再次点击关闭.", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                    //弹幕消息处理
                    handler = new Handler() {
                        @Override
                        public void handleMessage(Message msg) {
                            if (msg.obj instanceof String) {
                                if (((String) msg.obj).startsWith("CONNECT")) { //重连 roomName.enable
                                    roomName.setEnabled(true);
                                    connect.setText(getResources().getString(R.string.connect));
                                    isConnectState = false;
                                    textView.append((String) msg.obj + ",服务器断开,请重新连接.\n");
                                } else {
                                    isConnectState = true;

                                    //信息更新线程
                                    info = ((TextView) findViewById(R.id.info));
                                    infoHandler = new Handler() {
                                        @Override
                                        public void handleMessage(Message msg) {
                                            super.handleMessage(msg);
                                            info.setText((String) msg.obj);
                                        }
                                    };
                                    Thread onlinethread = new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            while (isConnectState) {
                                                try {
                                                    Thread.sleep(3000);
                                                    DouyuApi douyuApi = DouyuUtil.queryDouyuApi(roomname);
                                                    String infoString = "在线人数:[" + douyuApi.getData().getOnline() + "]\n" +
                                                            "粉丝数:[" + douyuApi.getData().getFans() + "]\n" +
                                                            "鱼丸体重:[" + douyuApi.getData().getOwner_weight() + "]";
                                                    Message msg = new Message();
                                                    msg.obj = infoString;
                                                    infoHandler.sendMessage(msg);
                                                } catch (Exception e) {
                                                    continue;
                                                }
                                            }
                                        }
                                    });
                                    onlinethread.start();

                                    String txt = (String) msg.obj;
                                    SpannableStringBuilder style = new SpannableStringBuilder(txt + "\n");
                                    int color = SerialUtil.getRandColor();
                                    //制定颜色
                                    if (txt.indexOf("感谢") != -1) {
                                        color = Color.BLUE;
                                        //字体大小
                                        AbsoluteSizeSpan span = new AbsoluteSizeSpan(60);
                                        style.setSpan(span, 0, txt.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                                        // 颜色
                                        style.setSpan(new ForegroundColorSpan(color),
                                                0, txt.length(),
                                                Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
                                    } else if (txt.indexOf("提示") != -1) {
                                        color = Color.GREEN;
                                        //字体大小
                                        AbsoluteSizeSpan span = new AbsoluteSizeSpan(60);
                                        style.setSpan(span, 0, txt.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                                        // 颜色
                                        style.setSpan(new ForegroundColorSpan(color),
                                                0, txt.length(),
                                                Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
                                    } else if (txt.indexOf("欢迎超管大大") != -1) {
                                        color = Color.RED;
                                        //字体大小
                                        AbsoluteSizeSpan span = new AbsoluteSizeSpan(100);
                                        style.setSpan(span, 0, txt.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                                        new AlertDialog.Builder(DanmuActivity.this).setMessage(txt).setTitle("超管提示")
                                                .setPositiveButton("确定", null).show();
                                        // 颜色
                                        style.setSpan(new ForegroundColorSpan(color),
                                                0, txt.length(),
                                                Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
                                    } else {
                                        color = Color.GREEN;
                                        int end = txt.indexOf(":");
                                        if (-1 == end) end = 7;
                                        if (end >= 7 && style.length() >= 7) {
                                            //下滑先
                                            UnderlineSpan spann = new UnderlineSpan();
                                            style.setSpan(spann, 7, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                            // 颜色
                                            style.setSpan(new ForegroundColorSpan(color), 7, end, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
                                        } else {
                                            //下滑先
                                            UnderlineSpan spann = new UnderlineSpan();
                                            style.setSpan(spann, 0, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                            // 颜色
                                            style.setSpan(new ForegroundColorSpan(color), 0, end, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
                                        }
                                    }

                                    // 粗体
                                    style.setSpan(new StyleSpan(Typeface.BOLD),
                                            0, txt.length(),
                                            Spannable.SPAN_EXCLUSIVE_INCLUSIVE);

                                    CharSequence new_text = ServerUtil.replace(style, DanmuActivity.this);
                                    textView.append(new_text);
                                    tv.yewai.live.Application app = (tv.yewai.live.Application) getApplication();
                                    Config config = app.getConfig();

                                    //TTS
                                    if (config.isTtsflag()) {
                                        String ttstxt = StringUtils.substring(txt, txt.indexOf(" : "), txt.indexOf(": |"));
                                        TtsUtil ttsUtil = TtsUtil.build(DanmuActivity.this);
                                        if (ttsUtil.getIsDone()) {
                                            ttsUtil.tms(ttstxt);
                                        }
                                    }

                                    //通知栏
                                    if (config.isHoutaiflag()) {
                                        if (SerialUtil.isBackground(getApplicationContext())) {
                                            SerialUtil.postNotify(new_text, textView.getLineCount(), DanmuActivity.this);
                                        }
                                    }

                                    //滚动到底部;
                                    if (scrollflag) {
                                        TextPaint tp = textView.getPaint();
                                        tp.setFakeBoldText(true);

                                        svResult = ((ScrollView) findViewById(R.id.scrollView));
                                        svResult.post(new Runnable() {
                                            public void run() {
                                                svResult.fullScroll(ScrollView.FOCUS_DOWN);
                                            }
                                        });
                                    }

                                    //判断文本行数,超200 清屏
                                    int count = textView.getLineCount();
                                    if (count > 200) {
                                        textView.setText("");
                                    }
                                }
                            }
                        }
                    };

                    try {
                        loginMinaThread = new LoginMinaThread(handler, roomname, username, password);
                        textthread = new Thread(loginMinaThread);
                        textthread.start();
                    } catch (Exception e) {
                        roomName.setEnabled(true);
                        connect.setText(getResources().getString(R.string.connect));
                        isConnectState = false;
                        textView.append("Thread" + e.getMessage() + ",服务器断开,请重新连接.\n");
                    }
                }else{
                    isConnectState = false;
                    connect.setText(getResources().getString(R.string.connect));
                    if(null!=loginMinaThread){
                        loginMinaThread.close();
                        loginMinaThread = null;
                    }
                    if(null!=textthread){
                        textthread.interrupt();
                        try {
                            textthread.join();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        textthread = null;
                    }
                }
            }
        });


        //快捷入口
        if (getIntent() != null) {
            String rname = getIntent().getStringExtra("tName");
            if (StringUtils.isNotBlank(rname)) {
                roomName.setText(rname);
                connect.callOnClick();
            }
        }


        //检测新版本
        if(netcheck()){
            runOnUiThread(new CheckVersionTask(this));
        }
    }

    //旋转式会重新调用 oncreate 方法 我们Androidmanifest activaty 设置了   android:configChanges="orientation|keyboardHidden"
    // 属性会调用者个 onConfigurationChanged 方法，此方法可以重新设置布局用 setContentView(R.layout.xxx)
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            Toast.makeText(this, "横屏模式", Toast.LENGTH_SHORT).show();
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            Toast.makeText(this, "竖屏模式", Toast.LENGTH_SHORT).show();
            WindowUtils.reViewWindow();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        tv.yewai.live.Application app = (tv.yewai.live.Application) getApplication();
        final Config config = app.getConfig();
        config.setHoutaiflag(false);
    }

    @Override
    public void onClick(View v) {
        //系统设置
        tv.yewai.live.Application app = (tv.yewai.live.Application) getApplication();
        final Config config = app.getConfig();
        switch (v.getId()) {
            case R.id.liveadd: {
                final LayoutInflater inflater = getLayoutInflater();
                final View layout = inflater.inflate(R.layout.rtmp_config, (ViewGroup) findViewById(R.id.dialog));
                //如果点击了斗鱼那么复制上
                EditText url = (EditText)layout.findViewById(R.id.rtmpserver);
                url.setText(config.getRtmpurl());
                EditText param = (EditText)layout.findViewById(R.id.rtmpstreamname);
                param.setText(config.getRtmpparam());
                new AlertDialog.Builder(this).setTitle("直播地址设置").setView(layout)
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                EditText rtmpurl = (EditText) layout.findViewById(R.id.rtmpserver);
                                config.setRtmpurl(rtmpurl.getText().toString());
                                EditText rtmpparam = (EditText) layout.findViewById(R.id.rtmpstreamname);
                                config.setRtmpparam(rtmpparam.getText().toString());
                            }
                        })
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                EditText rtmpparam = (EditText) layout.findViewById(R.id.rtmpstreamname);
                                rtmpparam.setText("");
                            }
                        }).show();

                break;
            }
            case R.id.caplive: {
                CheckBox tmp = (CheckBox) v;
                if (tmp.isChecked()) {
                    if (StringUtils.isNotBlank(config.getRtmpurl()) && StringUtils.isNotBlank(config.getRtmpparam())) {
                        if (!netcheck()) {
                            return;
                        }
                        tv.yewai.live.rtmp.WindowUtils.showPopupWindow(config.getRtmpurl().replaceAll("rtmp://", "").replaceAll("/live", ""), config.getRtmpparam(), DanmuActivity.this);//rtmp://localhost/live
                    }else {
                        new AlertDialog.Builder(DanmuActivity.this).setMessage("没有设置RTMP服务器.").setTitle("提示信息")
                                .setPositiveButton("确定", null).show();
                        tmp.setChecked(false);
                    }
                } else {
                    tv.yewai.live.rtmp.WindowUtils.hidePopupWindow();
                }
                break;
            }
            case R.id.zhibo: {
                final CheckBox tmp = (CheckBox) v;
                if (!netcheck()) {
                    tmp.setChecked(false);
                    return;
                }
                try {
                    if (StringUtils.isNotBlank(config.getRtmpurl()) && StringUtils.isNotBlank(config.getRtmpparam())) {
                        H264EncoderNetUtil henu = H264EncoderNetUtil.build("192.168.100.200", config.getRtmpurl().replaceAll("rtmp://", "").replaceAll("/live", ""), "live", config.getRtmpparam());
                        if (tmp.isChecked()) {
                            henu.setHdmiRtmpUrl(true);//true 主流
                            new AlertDialog.Builder(DanmuActivity.this).setMessage("H.364直播设置成功..").setTitle("提示信息")
                                    .setPositiveButton("确定", null).show();
                            Thread.sleep(30000);
                        } else {
                            henu.setCloseStream(true);//true 主流
                            new AlertDialog.Builder(DanmuActivity.this).setMessage("停止H.264直播设置成功.").setTitle("提示信息")
                                    .setPositiveButton("确定", null).show();
                            tmp.setChecked(false);
                            Thread.sleep(30000);
                        }
                    } else {
                        new AlertDialog.Builder(DanmuActivity.this).setMessage("没有设置RTMP服务器.").setTitle("提示信息")
                                .setPositiveButton("确定", null).show();
                        tmp.setChecked(false);

                    }
                } catch (Exception e) {
                    new AlertDialog.Builder(DanmuActivity.this).setMessage("H.264设置失败.").setTitle("提示信息")
                            .setPositiveButton("确定", null).show();
                    tmp.setChecked(false);
                }
                break;
            }
            case R.id.tts: {
                CheckBox tmp = (CheckBox) v;
                if (tmp.isChecked()) {
                    config.setTtsflag(true);
                } else {
                    config.setTtsflag(false);
                }
                break;
            }
            case R.id.houtai: {
                //SerialUtil.moveTaskToFront(this);
                //打开路由器
                final Intent it = new Intent(this,RouteWeb.class);
                startActivity(it);
                break;
            }
            case R.id.mapManger: {
                final Intent it = new Intent(this,OfflineDemo.class);
                startActivity(it);
                break;
            }
            case R.id.clear: {
                textView.setText("");
                break;
            }
            case R.id.black: {
                config.setHoutaiflag(true);
                //后台运行
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_HOME);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                break;
            }
            case R.id.kjfs: {
                final String roomname = roomName.getText().toString();
                if(StringUtils.isBlank(roomname)){
                    new AlertDialog.Builder(DanmuActivity.this).setMessage("请输入房间号或房间名称.").setTitle("提示信息")
                            .setPositiveButton("确定", null).show();
                }else{
                    SerialUtil.addShortCut(roomname, DanmuActivity.this);
                    new AlertDialog.Builder(DanmuActivity.this).setMessage("房间" + roomname + "快捷方式建立完成。").setTitle("提示信息")
                            .setPositiveButton("确定", null).show();
                }
                break;
            }
        }
    }

    private boolean netcheck() {
        if (!SerialUtil.checkNetworkAvailable(DanmuActivity.this)) {
            new AlertDialog.Builder(DanmuActivity.this).setMessage("对不起检测你网络连接失败\n无法上网,请连接后再试.").setTitle("提示信息")
                    .setPositiveButton("确定", null).show();
            return false;
        } else
            return true;
    }

    /*****
     * 截图方法
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if (resultCode != Activity.RESULT_OK) {
                return;
            } else if (data != null && resultCode != 0) {
                tv.yewai.live.Application app = (tv.yewai.live.Application) getApplication();
                MediaProjection mMediaProjection = app.getmMediaProjectionManager().getMediaProjection(Activity.RESULT_OK, data);
                app.setmMediaProjection(mMediaProjection);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(null!=loginMinaThread ){
            loginMinaThread.close();
            loginMinaThread = null;
        }
        if(null!=textthread){
            textthread.interrupt();
            textthread = null;
        }
    }
}
