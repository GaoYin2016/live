package tv.yewai.live.utils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Random;

import tv.yewai.live.R;
import tv.yewai.live.DanmuActivity;

/**
 * Created by Star on 2015/10/15.
 */
public class SerialUtil {

    //返回设备的唯一码
    public static String getSerial(Activity context){
        TelephonyManager tm = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
        //DEVICE_ID
        String DEVICE_ID = tm.getDeviceId();
        //装有SIM卡的设备，可以通过下面的方法获取到Sim Serial Number：
        //String SimSerialNumber = tm.getSimSerialNumber();
        return DEVICE_ID;
    }

    //系统检查升级
    public static void update(Activity context){
        // 新的APK的文件名
        String str = "newUpdate.apk";
        // 新APK在存储卡上的位置
        String fileName = Environment.getExternalStorageDirectory() + str;
        // 通过启动一个Intent让系统来帮你安装新的APK
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(new File(fileName)), "application/vnd.android.package-archive");
        context.startActivity(intent);
    }

    /**
     * 获取版本号
     * @return 当前应用的版本号
     */
    public String getVersion(Activity context) {
        try {
            PackageManager manager = context.getPackageManager();
            PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
            String version = info.versionName;
            return  version;
        } catch (Exception e) {
            e.printStackTrace();
            return "0.0.0";
        }
    }

    /**
     * 存储目录
     * @return
     */
    public static String getSDPath(String path) {
        File sdDir = null;
        boolean sdCardExist = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);   //判断sd卡是否存在
        if (sdCardExist) {
            sdDir = Environment.getExternalStorageDirectory();//获取跟目录
        }
        path =  sdDir.toString()+"/"+path+"/";

        File dir = new File(path);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        return path;
    }

    /**
     * 随机颜色
     * @return
     */
    public static int getRandColor() {
        Random random = new Random();
        int color[] = new int[10];
        color[0] = Color.RED;
        color[1] = Color.BLUE;
        color[2] = Color.GREEN;
        color[3] = Color.BLACK;
        color[4] = Color.DKGRAY;
        color[5] = Color.MAGENTA;
        color[6] = Color.GRAY;
        return color[random.nextInt(7)];
    }

    /**
     *  检测网络
     */
    public static boolean checkNetworkAvailable(Context context) {
        ConnectivityManager connectivity = (ConnectivityManager) context .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity == null) {
            return false;
        } else {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null) {
                for (int i = 0; i < info.length; i++) {
                    if (info[i].getState() == NetworkInfo.State.CONNECTED && info[i].isAvailable()) {
                        NetworkInfo netWorkInfo = info[i];
                        if (netWorkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                            return true;
                        } else if (netWorkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
                            return true;
                        }else if (netWorkInfo.getType() == 51) {//usbnet
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }


    //消息通知栏
    //contentText = "我的通知栏展开详细内容";
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static void postNotify(CharSequence contentText,int lineCount,Context context){
        //定义NotificationManager
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService( Context.NOTIFICATION_SERVICE);
        //定义通知栏展现的内容信息
        Notification.Builder builder = new Notification.Builder(context);
        InputStream is = context.getResources().openRawResource(R.raw.large);
        Bitmap largeIcon = BitmapFactory.decodeStream(is);
        Notification.Builder builer = builder.setTicker(contentText).
                setWhen(System.currentTimeMillis()).
                setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(largeIcon).setOngoing(true).setPriority(Notification.PRIORITY_MAX);
        builer.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setVibrate(new long[]{100, 400, 100, 400, -1});//震动 // 停止 开启 停止 开启    重复两次上面的long数组 如果只想震动一次，index设-1

        Intent ii = moveTaskToFront(context);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, ii, PendingIntent.FLAG_CANCEL_CURRENT);//替换迁移个

        Notification notification =  builder.setAutoCancel(true)
                .setNumber(lineCount).setContentText("助手通知")
                .setContentTitle(contentText).setColor(getRandColor())
                .setContentIntent(contentIntent).build();
        //用mNotificationManager的notify方法通知用户生成标题栏消息通知
        mNotificationManager.notify(1, notification);
    }

    //判断程序是否后台运行
    public static boolean isBackground(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager .getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.processName.equals(context.getPackageName())) {
                /*
                BACKGROUND=400 EMPTY=500 FOREGROUND=100
                GONE=1000 PERCEPTIBLE=130 SERVICE=300 ISIBLE=200
                 */
                Log.i(context.getPackageName(), "此appimportace =" + appProcess.importance + ",context.getClass().getName()=" + context.getClass().getName());
                if (appProcess.importance != ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    return true;
                } else {
                    return false;
                }
            }
        }
        return false;
    }

    //移动应用到前台
    public synchronized static Intent moveTaskToFront(Context context){
        final PackageManager pm = context.getPackageManager();
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        //拿到最近使用的应用的信息列表
        final List<ActivityManager.RecentTaskInfo> recentTasks =  activityManager.getRecentTasks(Integer.MAX_VALUE, ActivityManager.RECENT_IGNORE_UNAVAILABLE);
        //自制一个home activity info，用来区分
        ActivityInfo homeInfo =  new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME) .resolveActivityInfo(pm, 0);
        int numTasks = recentTasks.size();
        //开始初始化每个任务的信息
        for (int i = 0; i < numTasks; ++i) {
            final ActivityManager.RecentTaskInfo info = recentTasks.get(i);
            if(null!=info.baseIntent){
                if(info.id >0 && info.baseIntent.getComponent().getPackageName().equals(context.getPackageName())){
                    activityManager.moveTaskToFront(info.id, ActivityManager.MOVE_TASK_WITH_HOME);
                    return info.baseIntent;
                }
            }
        }
        //一般不可能返回这个
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return intent;
    }

    //添加快捷方式
    public static void addShortCut(String tName,Context context) {
        // 安装的Intent
        Intent shortcut = new Intent("com.android.launcher.action.INSTALL_SHORTCUT");

        // 快捷名称
        shortcut.putExtra(Intent.EXTRA_SHORTCUT_NAME, tName);
        // 快捷图标是允许重复
        shortcut.putExtra("duplicate", false);
        Intent shortcutIntent = new Intent(Intent.ACTION_MAIN);
        shortcutIntent.putExtra("tName", tName);
        shortcutIntent.setClassName(DanmuActivity.class.getPackage().getName(), DanmuActivity.class.getName());
        shortcutIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        shortcut.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);

        // 快捷图标
        Intent.ShortcutIconResource iconRes = Intent.ShortcutIconResource.fromContext(context, R.mipmap.ic_launcher);
        shortcut.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, iconRes);

        // 发送广播
        context.sendBroadcast(shortcut);
    }

    //判断横屏还是书评
    public static int getScreenOrientation(Context context){
        //判断当前为横屏还是竖屏
        if(context.getResources().getConfiguration().orientation ==  ActivityInfo.SCREEN_ORIENTATION_PORTRAIT){
           return ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
        }else if(context.getResources().getConfiguration().orientation ==  ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE){
            return ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
        }else{
            return 0;
        }
    }

    //写测试数据
    public static FileOutputStream wirteTestData(byte [] data,String filename){
        FileOutputStream out= null;
        try {
            out = new FileOutputStream(new File(getSDPath("yewaitv")+filename),true);
            out.write(data);
            out.flush();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return out;
    }
}
