package tv.yewai.live;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.HttpAuthHandler;
import android.webkit.JavascriptInterface;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.Poi;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tv.yewai.live.baidu.OfflineDemo;
import tv.yewai.live.utils.SerialUtil;

public class RouteWeb extends Activity {

    private Spinner spinner;
    private ArrayAdapter<String> adapter;
    private WebView main_view;
    private static final String[] menu = {"请选择","查看路由流量", "H.264解码器流", "斗鱼直播地址", "斗鱼房间改名","QQ群批量改签名"};


    //百度地图定位
    private MapView mMapView = null;
    private BaiduMap mBaiduMap = null;
    private BitmapDescriptor mCurrentMarker;
    public LocationClient mLocationClient = null;
    public BDLocationListener myListener = new MyLocationListener();

    class MyLocationListener implements BDLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location) {
            // 开启定位图层
            mBaiduMap.setMyLocationEnabled(true);
            // 构造定位数据
            MyLocationData locData = new MyLocationData.Builder().accuracy(location.getRadius())
                    // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(location.getDirection()).latitude(location.getLatitude())
                    .longitude(location.getLongitude()).build();
            // 设置定位数据
            mBaiduMap.setMyLocationData(locData);
            MyLocationConfiguration config = mMapView.getMap().getLocationConfigeration();
            mBaiduMap.setMyLocationConfigeration(config);
            mBaiduMap.setMaxAndMinZoomLevel(20f, 10f);

            // 当不需要定位图层时关闭定位图层
            //mBaiduMap.setMyLocationEnabled(false);
            //Receive Location
            StringBuffer sb = new StringBuffer(256);
            sb.append("time : ");
            sb.append(location.getTime());
            sb.append("\nerror code : ");
            sb.append(location.getLocType());
            sb.append("\nlatitude : ");
            sb.append(location.getLatitude());
            sb.append("\nlontitude : ");
            sb.append(location.getLongitude());
            sb.append("\nradius : ");
            sb.append(location.getRadius());
            if (location.getLocType() == BDLocation.TypeGpsLocation) {// GPS定位结果
                sb.append("\nspeed : ");
                sb.append(location.getSpeed());// 单位：公里每小时
                sb.append("\nsatellite : ");
                sb.append(location.getSatelliteNumber());
                sb.append("\nheight : ");
                sb.append(location.getAltitude());// 单位：米
                sb.append("\ndirection : ");
                sb.append(location.getDirection());// 单位度
                sb.append("\naddr : ");
                sb.append(location.getAddrStr());
                sb.append("\ndescribe : ");
                sb.append("gps定位成功");

            } else if (location.getLocType() == BDLocation.TypeNetWorkLocation) {// 网络定位结果
                sb.append("\naddr : ");
                sb.append(location.getAddrStr());
                //运营商信息
                sb.append("\noperationers : ");
                sb.append(location.getOperators());
                sb.append("\ndescribe : ");
                sb.append("网络定位成功");
            } else if (location.getLocType() == BDLocation.TypeOffLineLocation) {// 离线定位结果
                sb.append("\ndescribe : ");
                sb.append("离线定位成功，离线定位结果也是有效的");
            } else if (location.getLocType() == BDLocation.TypeServerError) {
                sb.append("\ndescribe : ");
                sb.append("服务端网络定位失败，可以反馈IMEI号和大体定位时间到loc-bugs@baidu.com，会有人追查原因");
            } else if (location.getLocType() == BDLocation.TypeNetWorkException) {
                sb.append("\ndescribe : ");
                sb.append("网络不同导致定位失败，请检查网络是否通畅");
            } else if (location.getLocType() == BDLocation.TypeCriteriaException) {
                sb.append("\ndescribe : ");
                sb.append("无法获取有效定位依据导致定位失败，一般是由于手机的原因，处于飞行模式下一般会造成这种结果，可以试着重启手机");
            }
            sb.append("\nlocationdescribe : ");
            sb.append(location.getLocationDescribe());// 位置语义化信息
            List<Poi> list = location.getPoiList();// POI数据
            String zhoubian = "";
            if (list != null) {
                sb.append("\npoilist size = : ");
                sb.append(list.size());
                for (Poi p : list) {
                    sb.append("\npoi= : ");
                    sb.append(p.getId() + " " + p.getName() + " " + p.getRank());
                    zhoubian += ("|" + p.getName());
                }
            }
           Log.d("RouteWeb",sb.toString());
        }
    }

    ;

    private void initLocation() {
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);//可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        option.setCoorType("bd09ll");//可选，默认gcj02，设置返回的定位结果坐标系
        int span = 1000;
        option.setScanSpan(span);//可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
        option.setIsNeedAddress(true);//可选，设置是否需要地址信息，默认不需要
        option.setOpenGps(true);//可选，默认false,设置是否使用gps
        option.setLocationNotify(true);//可选，默认false，设置是否当gps有效时按照1S1次频率输出GPS结果
        option.setIsNeedLocationDescribe(true);//可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”
        option.setIsNeedLocationPoiList(true);//可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
        option.setIgnoreKillProcess(false);//可选，默认false，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认杀死
        option.SetIgnoreCacheException(false);//可选，默认false，设置是否收集CRASH信息，默认收集
        option.setEnableSimulateGps(false);//可选，默认false，设置是否需要过滤gps仿真结果，默认需要
        mLocationClient.setLocOption(option);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);//去掉标题栏
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);//全屏

        //在使用SDK各组件之前初始化context信息，传入ApplicationContext
        //注意该方法要再setContentView方法之前实现
        SDKInitializer.initialize(getApplicationContext());

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route_web);

        //百度地图sdk
        //获取地图控件引用
        mMapView = (MapView) findViewById(R.id.bmapView);
        //设置是否显示比例尺控件
        mMapView.showScaleControl(true);
        //设置是否显示缩放控件
        mMapView.showZoomControls(true);
        mBaiduMap = mMapView.getMap();
        mCurrentMarker = BitmapDescriptorFactory.fromResource(R.drawable.jt);
        mBaiduMap.setMyLocationConfigeration(new MyLocationConfiguration(MyLocationConfiguration.LocationMode.COMPASS, true, mCurrentMarker));
        //普通地图
        mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
        //定位
        mLocationClient = new LocationClient(getApplicationContext());     //声明LocationClient类
        mLocationClient.registerLocationListener(myListener);    //注册监听函数
        initLocation();//定位参数
        mLocationClient.start();//开始定位


        //判断横竖屏显示地图
        // Checks the orientation of the screen
        if (SerialUtil.getScreenOrientation(this)==0) {
            Toast.makeText(this, "横屏模式,地图隐藏.", Toast.LENGTH_SHORT).show();
            mMapView.setVisibility(View.GONE);
        } else {
            Toast.makeText(this, "竖屏模式,地图显示.", Toast.LENGTH_SHORT).show();
            mMapView.setVisibility(View.VISIBLE);
        }


        //显示路由器流量信息
        main_view = (WebView) findViewById(R.id.webroute);
        main_view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        break;
                    case MotionEvent.ACTION_MOVE:
                        return false;// true 禁止触摸
                    case MotionEvent.ACTION_UP:
                        break;
                }
                return false;
            }
        });
        main_view.loadUrl("http://192.168.100.1/login.asp");
        main_view.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);

        main_view.setScrollbarFadingEnabled(true);
        main_view.addJavascriptInterface(new LoginJavaScriptImpl(), "loginImpl");
        WebSettings webSettings = main_view.getSettings();
        webSettings.setJavaScriptEnabled(true);
        //不保存密码
        webSettings.setSavePassword(true);
        //不保存表单数据
        webSettings.setSaveFormData(true);
        // 设置可以访问文件
        webSettings.setAllowFileAccess(true);
        // 设置支持缩放
        webSettings.setBuiltInZoomControls(true);
        webSettings.setSupportZoom(true); // 支持缩放
        webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NARROW_COLUMNS);
        webSettings.setUseWideViewPort(true);
        String use_agent = webSettings.getUserAgentString();
        Log.v("TAG", use_agent);
        // Mozilla/5.0 (Linux; Android 5.0.2; HTC 8088 Build/LRX22G) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/37.0.0.0 Mobile Safari/537.36 我手机的
        //Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.93 Safari/537.36 电脑的
        webSettings.setUserAgentString(use_agent.replaceAll("Android", "").replaceAll("Mobile", ""));//电脑视图
        main_view.setWebViewClient(new WebViewClient() {

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error){
                handler.proceed();//ssl 错误处理
            }

            @Override
            public void onReceivedHttpAuthRequest(WebView view, HttpAuthHandler handler, String host, String realm) {
               if("192.168.100.200".equals(host)){
                   handler.proceed("admin","admin");
               }
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);

            }

            @Override
            public void onLoadResource(WebView view, String url) {
                // history();
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if (url.indexOf("login.asp") != -1) {
                    main_view.setInitialScale(320);//为25%，最小缩放等级
                    main_view.loadUrl("javascript:window.scrollTo(440,130);document.getElementById('username').value='admin';document.getElementById('password').value='218#!*nn';document.getElementById('btn_login').click();");
                } else if (url.indexOf("home.asp") != -1) {
                    main_view.setInitialScale(425);//为25%，最小缩放等级
                    main_view.loadUrl("javascript:window.scrollTo(8,270);");
                } else if (url.indexOf("http://www.douyutv.com/333910") != -1) {
                    //main_view.loadUrl("javascript:window.loginImpl.login(document.getElementById('yp_identify_area').value);");
                    main_view.loadUrl("javascript:$('#js_switch_live').click();live_code_show();setTimeout(\"window.loginImpl.login($('#rtmp_url').val(),$('#rtmp_val').val())\",1000)");
                    main_view.setInitialScale(25);//为25%，最小缩放等级
                } else if(url.indexOf("http://www.douyutv.com/room/my") != -1){
                    // main_view.loadUrl("javascript:alert(confirm(prompt('请输入哈哈')));"); //测试对话框
                    main_view.loadUrl("javascript:$('#new_name').val(window.loginImpl.val(prompt('请输入房间名称')));$('#new_name').blur();");
                    main_view.setInitialScale(25);//为25%，最小缩放等级
                }
                else if(url.indexOf("http://ui.ptlogin2.qq.com/") != -1){
                    String execjs = "$(document.getElementById('u')).value='81621594';$(document.getElementById('p')).value='******321321';$(document.getElementById('login_button')).click();";

                    main_view.loadUrl("javascript:"+execjs);
                    main_view.setInitialScale(25);//为25%，最小缩放等级
                }
                else if(url.indexOf("http://qun.qq.com") != -1){//如果滚动条没有到底一直拉到底在批量签名
                    String execjs =  "var timer = setInterval(function(){ if ($(document).scrollTop() <  ( $(document).height() -  $(window).height() - 1000 ) ) { " +
                            "$(document).scrollTop($(document).scrollTop()+1000);" +
                            "} else {clearInterval(timer);" +
                            "$.each($('.list > tr '),function(i,n){ var _o = $(n).find('.td-card > span > .member-card');var _l = $(n).find('.td-user-nick > span').text().trim();var _q = _o.attr('data-id');var _v = $('#member-card'+_q);_v.val('户外 | '+_l);_v.blur();});" +
                            " } },1000);$('.my-group-list > li[data-id=8860620]').click();";
                    main_view.loadUrl("javascript:"+execjs);
                    main_view.setInitialScale(25);//为25%，最小缩放等级
                }
                else if(url.equals("http://192.168.100.200/")){
                    main_view.loadData("<html><head></head><body><video src='http://192.168.100.200/hdmi' controls='controls' width='100%' height='100%'/><body></html>", "text/html","UTF-8");
                    main_view.setInitialScale(25);//为25%，最小缩放等级
                }
                else {
                    main_view.setInitialScale(25);//为25%，最小缩放等级
                }
            }
        });
        main_view.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onCloseWindow(WebView window) {
                super.onCloseWindow(window);
            }

            @Override
            public boolean onCreateWindow(WebView view, boolean dialog, boolean userGesture, Message resultMsg) {
                return super.onCreateWindow(view, dialog, userGesture, resultMsg);
            }

            /**
             * 覆盖默认的window.alert展示界面，避免title里显示为“：来自file:////”
             */
            public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());

                builder.setTitle("对话框")
                        .setMessage(message)
                        .setPositiveButton("确定", null);

                // 不需要绑定按键事件
                // 屏蔽keycode等于84之类的按键
                builder.setOnKeyListener(new DialogInterface.OnKeyListener() {
                    public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                        Log.v("onJsAlert", "keyCode==" + keyCode + "event=" + event);
                        return true;
                    }
                });
                // 禁止响应按back键的事件
                builder.setCancelable(false);
                AlertDialog dialog = builder.create();
                dialog.show();
                result.confirm();// 因为没有绑定事件，需要强行confirm,否则页面会变黑显示不了内容。
                return true;
                // return super.onJsAlert(view, url, message, result);
            }

            public boolean onJsBeforeUnload(WebView view, String url,  String message, JsResult result) {
                return super.onJsBeforeUnload(view, url, message, result);
            }

            /**
             * 覆盖默认的window.confirm展示界面，避免title里显示为“：来自file:////”
             */
            public boolean onJsConfirm(WebView view, String url, String message, final JsResult result) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                builder.setTitle("对话框")
                        .setMessage(message)
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                result.confirm();
                            }
                        })
                        .setNeutralButton("取消", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                result.cancel();
                            }
                        });
                builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        result.cancel();
                    }
                });

                // 屏蔽keycode等于84之类的按键，避免按键后导致对话框消息而页面无法再弹出对话框的问题
                builder.setOnKeyListener(new DialogInterface.OnKeyListener() {
                    @Override
                    public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                        Log.v("onJsConfirm", "keyCode==" + keyCode + "event=" + event);
                        return true;
                    }
                });
                // 禁止响应按back键的事件
                // builder.setCancelable(false);
                AlertDialog dialog = builder.create();
                dialog.show();
                return true;
                // return super.onJsConfirm(view, url, message, result);
            }

            /**
             * 覆盖默认的window.prompt展示界面，避免title里显示为“：来自file:////”
             * window.prompt('请输入您的域名地址', '618119.com');
             */
            public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, final JsPromptResult result) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());

                builder.setTitle("对话框").setMessage(message);

                final EditText et = new EditText(view.getContext());
                et.setSingleLine();
                et.setText(defaultValue);
                builder.setView(et)
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                result.confirm(et.getText().toString());
                            }

                        })
                        .setNeutralButton("取消", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                result.cancel();
                            }
                        });

                // 屏蔽keycode等于84之类的按键，避免按键后导致对话框消息而页面无法再弹出对话框的问题
                builder.setOnKeyListener(new DialogInterface.OnKeyListener() {
                    public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                        Log.v("onJsPrompt", "keyCode==" + keyCode + "event=" + event);
                        return true;
                    }
                });

                // 禁止响应按back键的事件
                builder.setCancelable(true);
                AlertDialog dialog = builder.create();
                dialog.show();
                return true;
                // return super.onJsPrompt(view, url, message, defaultValue,
                // result);
            }

            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
            }

            @Override
            public void onReceivedIcon(WebView view, Bitmap icon) {
                super.onReceivedIcon(view, icon);
            }

            @Override
            public void onReceivedTitle(WebView view, String title) {
                super.onReceivedTitle(view, title);
            }

            @Override
            public void onRequestFocus(WebView view) {
                super.onRequestFocus(view);
            }
        });


        /////////////////////////////////////////////////////////////下拉立标/////////////////////////////////////
        spinner = (Spinner) findViewById(R.id.spinner);
        //将可选内容与ArrayAdapter连接起来
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, menu);
        //设置下拉列表的风格
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //将adapter 添加到spinner中
        spinner.setAdapter(adapter);
        //添加事件Spinner事件监听
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                switch (i) {
                    case 1:{
                        main_view.loadUrl("http://192.168.100.1/login.asp");
                        setSpinnerItemSelectedByValue(spinner, 0);
                        break;
                    }
                    case 2: {
                        //264页面
                        Map<String, String> header = new HashMap<String, String>();
                        header.put("Authorization", "Basic YWRtaW46YWRtaW4=");
                        main_view.loadUrl("http://192.168.100.200", header);
                        setSpinnerItemSelectedByValue(spinner, 0);
                        break;
                    }
                    case 3: {
                        //斗鱼TV页面
                        main_view.loadUrl("http://www.douyutv.com/333910");
                        setSpinnerItemSelectedByValue(spinner, 0);
                        break;
                    }
                    case 4: {
                        //设置房名
                        main_view.loadUrl("http://www.douyutv.com/room/my");
                        setSpinnerItemSelectedByValue(spinner, 0);
                        break;
                    }
                    case 5: {
                        //qq 群
                        main_view.loadUrl("http://ui.ptlogin2.qq.com/cgi-bin/login?appid=715030901&daid=73&hide_close_icon=1&pt_no_auth=1&s_url=http%3A%2F%2Fqun.qq.com%2Fmember.html%3Fstyle%3D20%23");
                        //main_view.loadUrl("http://qun.qq.com/member.html?style=20#gid=8860620");
                        setSpinnerItemSelectedByValue(spinner, 0);
                        break;
                    }

                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
        //设置默认值
        spinner.setVisibility(View.VISIBLE);

    }


    /**
     * 根据值, 设置spinner默认选中:
     *
     * @param spinner
     * @param value
     */
    public static void setSpinnerItemSelectedByValue(Spinner spinner, int value) {
        SpinnerAdapter apsAdapter = spinner.getAdapter(); //得到SpinnerAdapter对象
        spinner.setSelection(value, true);// 默认选中项
    }

    //js 注入
    final class LoginJavaScriptImpl {
        @JavascriptInterface
        public void login(final String rtmpurl, final String rtmpParam) {
            Log.v("TAG", "URL=" + rtmpurl + "param=" + rtmpParam);
            tv.yewai.live.Application app = (tv.yewai.live.Application) getApplication();
            Config config = app.getConfig();
            config.setRtmpurl(rtmpurl);
            config.setRtmpparam(rtmpParam);
        }

        //设置斗鱼房间名称穿入后台
        @JavascriptInterface
        public String val(final String title) {
            Log.i("TAG", "传入参数="+title);
            return title;
        }
    }

    /**
     * 同步一下cookie
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static void synCookies(Context context, String url, String cookie) {
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        cookieManager.removeAllCookies(new ValueCallback<Boolean>() {
            @Override
            public void onReceiveValue(Boolean value) {

            }
        });//移除
        cookieManager.setCookie(url, cookie);//指定要修改的cookie
    }


    //旋转式会重新调用 oncreate 方法 我们Androidmanifest activaty 设置了   android:configChanges="orientation|keyboardHidden"
    // 属性会调用者个 onConfigurationChanged 方法，此方法可以重新设置布局用 setContentView(R.layout.xxx)
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            Toast.makeText(this, "横屏模式,地图隐藏.", Toast.LENGTH_SHORT).show();
            mMapView.setVisibility(View.GONE);
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            Toast.makeText(this, "竖屏模式,地图显示.", Toast.LENGTH_SHORT).show();
            mMapView.setVisibility(View.VISIBLE);
        }
    }

}
