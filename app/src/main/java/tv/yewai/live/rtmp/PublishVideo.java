package tv.yewai.live.rtmp;

import android.content.Context;
import android.graphics.Bitmap;
import tv.yewai.live.utils.CaptureUtil;
import tv.yewai.live.utils.SerialUtil;

public class PublishVideo implements Runnable {

    private Consumer consumer;
    private Context context;
    private ScreenVideo screenVideo;
    private boolean running;
    final int timeBetweenFrames = 100; // 1000 / frameRate
    int frameCounter = 0;
    private int width = 480;
    private int height = 270;

    public PublishVideo(Consumer consumer, Context context) {
        this.consumer = consumer;
        this.context = context;
        screenVideo = new ScreenVideo();
    }

    public void run() {
        running = true;
        byte[] previous = null;

        while (running) {
            final long ctime = System.currentTimeMillis();
            if (SerialUtil.getScreenOrientation(context) == 1) {
                Bitmap bitmap = CaptureUtil.build(context).captureVideo(height, width);
                if (null == bitmap) continue;
                byte[] current = screenVideo.toBGR(bitmap);
                try {
                    final byte[] encoded = screenVideo.encode(current, previous, height, width);
                    if (previous == null) {
                        consumer.putData(ClientManager.DataType.KEY_FRAME, System.currentTimeMillis(), encoded, encoded.length);
                    } else {
                        consumer.putData(ClientManager.DataType.INTER_FRAME, System.currentTimeMillis(), encoded, encoded.length);
                    }

                    previous = current;

                    if (++frameCounter % 10 == 0) previous = null;
                } catch (Exception e) {
                    e.printStackTrace();
                }

                final int spent = (int) (System.currentTimeMillis() - ctime);

                try {
                    Thread.sleep(Math.max(0, timeBetweenFrames - spent));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if (!bitmap.isRecycled()) {
                    bitmap.recycle();   //回收图片所占的内存
                    System.gc();  //提醒系统及时回收
                }
            } else {
                Bitmap bitmap = CaptureUtil.build(context).captureVideo(width, height);
                if (null == bitmap) continue;
                byte[] current = screenVideo.toBGR(bitmap);
                try {
                    final byte[] encoded = screenVideo.encode(current, previous, width, height);
                    if (previous == null) {
                        consumer.putData(ClientManager.DataType.KEY_FRAME, System.currentTimeMillis(), encoded, encoded.length);
                    } else {
                        consumer.putData(ClientManager.DataType.INTER_FRAME, System.currentTimeMillis(), encoded, encoded.length);
                    }

                    previous = current;

                    if (++frameCounter % 10 == 0) previous = null;
                } catch (Exception e) {
                    e.printStackTrace();
                }

                final int spent = (int) (System.currentTimeMillis() - ctime);

                try {
                    Thread.sleep(Math.max(0, timeBetweenFrames - spent));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if (!bitmap.isRecycled()) {
                    bitmap.recycle();   //回收图片所占的内存
                    System.gc();  //提醒系统及时回收
                }
            }
        }
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

}
