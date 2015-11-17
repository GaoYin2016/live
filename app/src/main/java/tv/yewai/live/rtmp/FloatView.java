package tv.yewai.live.rtmp;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.hardware.Camera;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import tv.yewai.live.douyu.utils.ServerUtil;
import tv.yewai.live.jni.ImageUtilEngine;
import tv.yewai.live.rtmp.flvwriter.Capture;
import tv.yewai.live.rtmp.flvwriter.CaptureFactory;
import tv.yewai.live.rtmp.flvwriter.impl.Capturer;
import tv.yewai.live.rtmp.streamWapper.RtmpStreamer;
import tv.yewai.live.utils.SerialUtil;

public class FloatView extends SurfaceView implements SurfaceHolder.Callback, Camera.PreviewCallback {

    private float mTouchX;
    private float mTouchY;
    private float x;
    private float y;
    private OnClickListener mClickListener;
    private boolean moveflag = true;
    private Context context;
    private SurfaceHolder surfaceHolder;
    private static int previewWidth = 480;
    private static int previewHeight = 320;
    private boolean isPreviewRunning = false;
    private static Camera mCamera;
    private static boolean flag;

    private static String rtmpurl;
    private static String rtmpparam;

    private static ClientManager c;
    private static PublishAudio pa;

    public FloatView(final String rtmpurl, final String rtmpparam, Context context) {
        super(context);

        this.rtmpurl = rtmpurl;
        this.rtmpparam = rtmpparam;
        this.context = context;
        super.setFocusable(true);
        super.setFocusableInTouchMode(true);
        InitSurfaceView();

        //管理初始化
        c = new ClientManager(rtmpurl, rtmpparam);
        c.setRunning(true);
        c.setRecording(true);
        c.setMode(ClientManager.NETONLY);
        new Thread(c).start();
        pa = new PublishAudio(c);
        new Thread(pa).start();
    }

    //换摄像头
    public void changeCam(boolean flag) {
        if (null != surfaceHolder) {
            try {
                isPreviewRunning = false;
                mCamera.setPreviewCallback(null); //m_camera.stopPreview();
                mCamera.release();
                mCamera = null;

                openCamera(surfaceHolder, flag);
                startPreview();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //获取到状态栏的高度
        Rect frame = new Rect();
        getWindowVisibleDisplayFrame(frame);
        int statusBarHeight = frame.top;
        System.out.println("statusBarHeight:" + statusBarHeight);
        // 获取相对屏幕的坐标，即以屏幕左上角为原点
        x = event.getRawX();
        y = event.getRawY() - statusBarHeight;//是系统状态栏的高度
        Log.i("tag", "X=" + x + ",Y=" + y);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: // 捕获手指触摸按下动作
                // 获取相对View的坐标，即以此View左上角为原点
                mTouchX = event.getX();
                mTouchY = event.getY();
                Log.i("tag", "startX=" + mTouchX + ",startY=" + mTouchY);
                moveflag = true;
                break;

            case MotionEvent.ACTION_MOVE: // 捕获手指触摸移动动作
                updateViewPosition();
                moveflag = false;
                break;

            case MotionEvent.ACTION_UP: // 捕获手指触摸离开动作
                updateViewPosition();
                mTouchX = mTouchY = 0;
                if (mClickListener != null && moveflag) {
                    mClickListener.onClick(this);
                }
                break;
        }
        return true;
    }

    @Override
    public void setOnClickListener(OnClickListener l) {
        this.mClickListener = l;
    }

    private void updateViewPosition() {
        // 更新浮动窗口位置参数
        WindowUtils.getParams().x = (int) (x - mTouchX);
        WindowUtils.getParams().y = (int) (y - mTouchY);
        WindowUtils.getWindowManager().updateViewLayout(this, WindowUtils.getParams()); // 刷新显示
    }


    private void InitSurfaceView() {
        surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        //实现自动对焦
        mCamera.autoFocus(new Camera.AutoFocusCallback() {
            @Override
            public void onAutoFocus(boolean success, Camera camera) {
                if (success) {
                    camera.cancelAutoFocus();//只有加上了这一句，才会自动对焦。
                }
            }
        });
    }

    public void surfaceCreated(SurfaceHolder holder) {
        try {
            openCamera(surfaceHolder, true);
            startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        stopPreview();
    }


    private void startPreview() {
        if (mCamera != null) {
            mCamera.startPreview();
        }
    }

    public void stopPreview() {
        isPreviewRunning = false;
        mCamera.setPreviewCallback(null); //m_camera.stopPreview();
        mCamera.release();
        mCamera = null;

        //停播
        if (null != pa)
            pa.stopPublish();
        if (null != c)
            c.setRunning(false);
    }

    //flag 前置后置 true 后置，false 前置
    private void openCamera(SurfaceHolder holder, boolean flag) throws IOException {
        this.flag = flag;
        if (flag) {
            int numberOfCameras = Camera.getNumberOfCameras();
            Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
            for (int i = 0; i < numberOfCameras; i++) {
                Camera.getCameraInfo(i, cameraInfo);
                if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                    mCamera = Camera.open(i);
                }
            }
        } else {
            int numberOfCameras = Camera.getNumberOfCameras();
            Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
            for (int i = 0; i < numberOfCameras; i++) {
                Camera.getCameraInfo(i, cameraInfo);
                if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                    mCamera = Camera.open(i);
                }
            }
        }
        if (mCamera == null) {
            throw new IOException();
        }
        mCamera.setPreviewDisplay(holder);
        changeOrientation();
    }

    public void changeOrientation(){
        Camera.Parameters parameters = mCamera.getParameters();
        Display display = ((WindowManager) context.getSystemService(Activity.WINDOW_SERVICE)).getDefaultDisplay();

        //产看相机蛇和的尺寸 // 选择合适的预览尺寸
        List<Camera.Size> sizeList = parameters.getSupportedPreviewSizes();
        // 如果sizeList只有一个我们也没有必要做什么了，因为就他一个别无选择
        if (sizeList.size() > 1) {
            Iterator<Camera.Size> itor = sizeList.iterator();
            while (itor.hasNext()) {
                Camera.Size cur = itor.next();
                System.err.println("size==" + cur.width + " " + cur.height);
            }
        }

        if (display.getRotation() == Surface.ROTATION_0) {
            parameters.setPreviewSize(previewWidth, previewHeight);
            mCamera.setDisplayOrientation(90);
        }

        if (display.getRotation() == Surface.ROTATION_90) {
            parameters.setPreviewSize(previewWidth, previewHeight);
            mCamera.setDisplayOrientation(180);
        }

        if (display.getRotation() == Surface.ROTATION_180) {
            parameters.setPreviewSize(previewWidth, previewHeight);
            mCamera.setDisplayOrientation(90);
        }

        if (display.getRotation() == Surface.ROTATION_270) {
            parameters.setPreviewSize(previewWidth, previewHeight);
            mCamera.setDisplayOrientation(180);
        }
        parameters.setPreviewFormat(ImageFormat.NV21);
        //parameters.setPreviewFormat(ImageFormat.YV12);
        //parameters.setPictureSize(surfaceView.getWidth(), surfaceView.getHeight());  // 部分定制手机，无法正常识别该方法。
        //parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
        if (flag) {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);//1连续对焦
        }
        mCamera.setParameters(parameters);
        mCamera.setPreviewCallback(this);
        startPreview();
        mCamera.cancelAutoFocus();// 2如果要实现连续的自动对焦，这一句必须加上
    }

    //返回当前显示的摄像头
    public boolean getFlag() {
        return this.flag;
    }

    byte[] previous = null;
    int[] rgb = null;
    Bitmap bitmap = null;
    private ScreenVideo screenVideo = new ScreenVideo();
    final int timeBetweenFrames = 100; // 1000 / frameRate
    int frameCounter = 0;

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        final long ctime = System.currentTimeMillis();
        rgb = ImageUtilEngine.decodeYUV420SP(data, previewWidth, previewHeight);
        bitmap = Bitmap.createBitmap(rgb, previewWidth, previewHeight, Bitmap.Config.RGB_565);
        if (null == bitmap) return;
        byte[] current = screenVideo.toBGR(bitmap);
        if (null == current) return;
        try {
            final byte[] encoded = screenVideo.encode(current, previous, previewWidth, previewHeight);
            if (previous == null) {
                c.putData(ClientManager.DataType.KEY_FRAME, System.currentTimeMillis(), encoded, encoded.length);
            } else {
                c.putData(ClientManager.DataType.INTER_FRAME, System.currentTimeMillis(), encoded, encoded.length);
            }
              previous = current;
            if (++frameCounter %  10 == 0) previous = null;
        } catch (Exception e) {
            e.printStackTrace();
        }

        final int spent = (int) (System.currentTimeMillis() - ctime);

        try {
            Thread.sleep(Math.max(0, timeBetweenFrames - spent));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (null!=bitmap &&!bitmap.isRecycled()) {
            bitmap.recycle();   //回收图片所占的内存
            System.gc();  //提醒系统及时回收
        }
        Log.d("上传时间:", "》》》》》》》》》》》》》》》》》》》》》》》》》》》》》》》》》》》》》》》》》》》》》》》==" + spent);
    }

}