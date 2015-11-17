package tv.yewai.live.utils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.ThumbnailUtils;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;

import tv.yewai.live.R;

/**
 * 截屏工具
 * Created by Star on 2015/10/23.
 */
public class CaptureUtil {

    private static final String TAG = CaptureUtil.class.getSimpleName();
    private static CaptureUtil cu ;
    private static Context context;
    //全局间接平截屏
    private MediaProjectionManager mMediaProjectionManager;
    private LinearLayout mFloatLayout = null;
    private WindowManager.LayoutParams wmParams = null;
    private WindowManager mWindowManager = null;
    private LayoutInflater inflater = null;
    private ImageButton mFloatView = null;
    private SimpleDateFormat dateFormat = null;
    private String strDate = null;
    private String pathImage = null;
    private String nameImage = null;
    private MediaProjection mMediaProjection = null;
    private VirtualDisplay mVirtualDisplay = null;
    private int windowWidth = 0;
    private int windowHeight = 0;
    private ImageReader mImageReader = null;
    private DisplayMetrics metrics = null;
    private int mScreenDensity = 0;


    public static CaptureUtil build(Context context){
        synchronized (CaptureUtil.class){
            if(null == cu){
                cu = new CaptureUtil(context);
                return cu;
            }else{
                return cu;
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private CaptureUtil(Context context){
        this.context = context;
        createFloatView();
        createVirtualEnvironment();
        Intent catureintent =   mMediaProjectionManager.createScreenCaptureIntent();
        Activity activity =  (Activity)context;
        tv.yewai.live.Application app = (tv.yewai.live.Application) activity.getApplication();
        app.setmMediaProjectionManager(mMediaProjectionManager);
        activity.startActivityForResult(catureintent, 1);
    }

    public void createFloatView()
    {
        wmParams = new WindowManager.LayoutParams();
        mWindowManager = (WindowManager)context.getSystemService(context.WINDOW_SERVICE);
        wmParams.type =  WindowManager.LayoutParams.TYPE_PHONE;
        wmParams.format = PixelFormat.RGBA_8888;
        wmParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        wmParams.gravity = Gravity.LEFT | Gravity.TOP;
        wmParams.x = 0;
        wmParams.y = 0;
        wmParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        wmParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        inflater = LayoutInflater.from(context);
        mFloatLayout = (LinearLayout) inflater.inflate(R.layout.float_layout, null);
        mWindowManager.addView(mFloatLayout, wmParams);
        mFloatView = (ImageButton) mFloatLayout.findViewById(R.id.float_id);

        mFloatLayout.measure(View.MeasureSpec.makeMeasureSpec(0,
                View.MeasureSpec.UNSPECIFIED), View.MeasureSpec
                .makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));

        mFloatView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                wmParams.x = (int) event.getRawX() - mFloatView.getMeasuredWidth() / 2;
                wmParams.y = (int) event.getRawY() - mFloatView.getMeasuredHeight() / 2 - 25;
                mWindowManager.updateViewLayout(mFloatLayout, wmParams);
                return false;
            }
        });

        mFloatView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // hide the button
                mFloatView.setVisibility(View.INVISIBLE);

                Handler handler1 = new Handler();
                handler1.postDelayed(new Runnable() {
                    public void run() {
                        //start virtual
                        virtualDisplay();
                    }
                }, 500);

                Handler handler2 = new Handler();
                handler2.postDelayed(new Runnable() {
                    public void run() {
                        //capture the screen
                        startCapture();
                    }
                }, 1500);

                Handler handler3 = new Handler();
                handler3.postDelayed(new Runnable() {
                    public void run() {
                        mFloatView.setVisibility(View.VISIBLE);
                        //stopVirtual();
                    }
                }, 1000);
            }
        });

        Log.i(TAG, "created the float sphere view");
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public void createVirtualEnvironment(){
        dateFormat = new SimpleDateFormat("yyyy_MM_dd_hh_mm_ss");
        strDate = dateFormat.format(new java.util.Date());
        pathImage = SerialUtil.getSDPath("yewaitv");
        nameImage = pathImage+strDate+".png";
        mMediaProjectionManager = (MediaProjectionManager)context.getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        mWindowManager = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
        windowWidth = mWindowManager.getDefaultDisplay().getWidth();
        windowHeight = mWindowManager.getDefaultDisplay().getHeight();
        metrics = new DisplayMetrics();
        mWindowManager.getDefaultDisplay().getMetrics(metrics);
        mScreenDensity = metrics.densityDpi;
        mImageReader = ImageReader.newInstance(windowWidth, windowHeight, 0x1, 2); //ImageFormat.RGB_565
        Log.i(TAG, "prepared the virtual environment");
    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void virtualDisplay(){
        tv.yewai.live.Application app = (tv.yewai.live.Application)((Activity)context).getApplication();
        mMediaProjection = app.getmMediaProjection();
        mVirtualDisplay = mMediaProjection.createVirtualDisplay("截屏监控",
                windowWidth, windowHeight, mScreenDensity, DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                mImageReader.getSurface(), null, null);
        Log.i(TAG, "virtual displayed");
    }
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public Bitmap captureVideo(int w,int h){
        Image image = mImageReader.acquireLatestImage();
        if(null == image){
            virtualDisplay();
        }

        if(null == image){
            return null;
        }
        int width = image.getWidth();
        int height = image.getHeight();
        final Image.Plane[] planes = image.getPlanes();
        final ByteBuffer buffer = planes[0].getBuffer();
        int pixelStride = planes[0].getPixelStride();
        int rowStride = planes[0].getRowStride();
        int rowPadding = rowStride - pixelStride * width;

        Bitmap bitmap = Bitmap.createBitmap(width+rowPadding/pixelStride, height, Bitmap.Config.ARGB_8888);//8888
        bitmap.copyPixelsFromBuffer(buffer);

       //bitmap = Bitmap.createBitmap(bitmap, 0,0,width,height);
        bitmap = ThumbnailUtils.extractThumbnail(bitmap,w,h);
//        Bitmap wm = BitmapFactory.decodeResource(context.getResources(),R.drawable.douyuwatermark);
//        bitmap = Watermark.watermarkBitmap(bitmap, wm, "  YEWAI.TV  ");
        image.close();
        return bitmap;
    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void startCapture(){
        strDate = dateFormat.format(new java.util.Date());
        nameImage = pathImage+strDate+".png";
        Image image = mImageReader.acquireLatestImage();
        int width = image.getWidth();
        int height = image.getHeight();
        final Image.Plane[] planes = image.getPlanes();
        final ByteBuffer buffer = planes[0].getBuffer();
        int pixelStride = planes[0].getPixelStride();
        int rowStride = planes[0].getRowStride();
        int rowPadding = rowStride - pixelStride * width;
        Bitmap bitmap = Bitmap.createBitmap(width+rowPadding/pixelStride, height, Bitmap.Config.ARGB_8888);
        bitmap.copyPixelsFromBuffer(buffer);
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height);
        image.close();
        Log.i(TAG, "image data captured");

        if(bitmap != null) {
            try{
                File fileImage = new File(nameImage);
                if(!fileImage.exists()){
                    fileImage.createNewFile();
                    Log.i(TAG, "image file created");
                }
                FileOutputStream out = new FileOutputStream(fileImage);
                if(out != null){
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                    out.flush();
                    out.close();
                    Intent media = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                    Uri contentUri = Uri.fromFile(fileImage);
                    media.setData(contentUri);
                    context.sendBroadcast(media);

                    //直接打开截图位置
                    Intent intent = new Intent();
                    intent.setAction(android.content.Intent.ACTION_VIEW);
                    intent.setDataAndType(Uri.fromFile(fileImage), "image/*");
                    context.startActivity(intent);
                    Log.i(TAG, "screen image saved");
                }
            }catch(FileNotFoundException e) {
                e.printStackTrace();
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void tearDownMediaProjection() {
        if (mMediaProjection != null) {
            mMediaProjection.stop();
            mMediaProjection = null;
        }
        Log.i(TAG,"mMediaProjection undefined");
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void stopVirtual() {
        if (mVirtualDisplay == null) {
            return;
        }
        mVirtualDisplay.release();
        mVirtualDisplay = null;
        Log.i(TAG,"virtual display stopped");
    }


}
