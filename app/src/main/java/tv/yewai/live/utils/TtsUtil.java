package tv.yewai.live.utils;

import android.content.Context;
import android.os.Bundle;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SpeechUtility;
import com.iflytek.cloud.SynthesizerListener;

/**
 * Created by Star on 2015/10/22.
 */
public class TtsUtil {

    private static final String  APPID = "506f9a9a";
    private static Boolean isDone = true; //是否播放完成
    private static TtsUtil ttsutil;
    private Context context;

    private TtsUtil(Context context){
        //语音识别
        SpeechUtility.createUtility(context, SpeechConstant.APPID + "=" + APPID);
        this.context = context;
    }

    //语音合成
    public synchronized void tms(String txt){
        //1.创建 SpeechSynthesizer 对象, 第二个参数：本地合成时传 InitListener
        SpeechSynthesizer mTts = SpeechSynthesizer.createSynthesizer(context, null);
        // 设置播放合成音频打断音乐播放，默认为true
        mTts.setParameter(SpeechConstant.KEY_REQUEST_FOCUS, "false");
        //2.合成参数设置，详见《科大讯飞MSC API手册(Android)》SpeechSynthesizer 类 //设置发音人（更多在线发音人，用户可参见 附录12.2
        mTts.setParameter(SpeechConstant.VOICE_NAME, "xiaoyan");
        //设置发音人
        mTts.setParameter(SpeechConstant.SPEED, "60");
        //设置语速
        mTts.setParameter(SpeechConstant.VOLUME, "100");
        //设置音量，范围 0~100
        mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_LOCAL);
        //设置云端
        // 设置合成音频保存位置（可自定义保存位置），保存在“./sdcard/iflytek.pcm”
        // 保存在 SD 卡需要在 AndroidManifest.xml 添加写 SD 卡权限
        // 仅支持保存为 pcm 和 wav 格式，如果不需要保存合成音频，注释该行代码
        //mTts.setParameter(SpeechConstant.TTS_AUDIO_PATH, "./sdcard/iflytek.pcm");

        //合成监听器
        SynthesizerListener mSynListener = new SynthesizerListener() {
            //会话结束回调接口，没有错误时，error为null
            public void onCompleted(SpeechError error) {
                System.out.println("播放完成");
                if(null == error){
                    isDone = true;
                }
            }

            //缓冲进度回调
            // percent为缓冲进度0~100，beginPos为缓冲音频在文本中开始位置，endPos表示缓冲音频在 文本中结束位置，info为附加信息。
            public void onBufferProgress(int percent, int beginPos, int endPos, String info) {
                isDone = false;
            }

            //开始播放
            public void onSpeakBegin() {
                System.out.println("开始播放");
                isDone = false;
            }

            // 暂停播放
            public void onSpeakPaused() {
            }

            // 播放进度回调
            // percent为播放进度0~100,beginPos为播放音频在文本中开始位置，endPos表示播放音频在文 本中结束位置.
            public void onSpeakProgress(int percent, int beginPos, int endPos) {
                System.out.println("百分比  == " + percent + " === " + beginPos + "  == " + endPos);
            }

            //恢复播放回调接口
            public void onSpeakResumed() {
            }

            //会话事件回调接口
            public void onEvent(int arg0, int arg1, int arg2, Bundle arg3) {
            }
        };
        //3.开始合成
        mTts.startSpeaking(txt, mSynListener);
    }

    //判断是否完成合成
    public Boolean getIsDone(){
        return this.isDone;
    }

    //单例
    public static  TtsUtil build(Context context){
        synchronized (TtsUtil.class){
            if(null == ttsutil){
                ttsutil =  new TtsUtil(context);
                return  ttsutil;
            }else{
                return ttsutil;
            }
        }
    }

}
