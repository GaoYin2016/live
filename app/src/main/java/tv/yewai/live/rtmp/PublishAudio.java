package tv.yewai.live.rtmp;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import com.sinaapp.bashell.sayhi.LogHelper;
import com.sinaapp.bashell.sayhi.Speex;

public class PublishAudio implements Runnable {
	private final String subTAG = "PublishAudio";
	private boolean isPublish;
	private Consumer consumer;

	private Speex publishSpeex = new Speex();
	private int frameSize;
	private byte[] processedData;

	public PublishAudio(Consumer consumer){
		this.consumer = consumer;
	}

	public void stopPublish() {
		isPublish = false;
	}

	@Override
	public void run() {
		frameSize = publishSpeex.getFrameSize();
		processedData = new byte[frameSize];
		int bufferSize = AudioRecord.getMinBufferSize(8000, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
		short[] mAudioRecordBuffer = new short[bufferSize];
		AudioRecord mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, 8000, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize);
		mAudioRecord.startRecording();
		int bufferRead = 0;
		int len;
		isPublish = true;
		while (isPublish) {
			bufferRead = mAudioRecord.read(mAudioRecordBuffer, 0, frameSize);
			if (bufferRead > 0) {
				try {
					len = publishSpeex.encode(mAudioRecordBuffer, 0, processedData, frameSize);
					if (consumer != null)
						consumer.putData(ClientManager.DataType.AUDIO, System.currentTimeMillis(), processedData, len);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		mAudioRecord.stop();
		mAudioRecord.release();
		mAudioRecord = null;
		publishSpeex.close();
		LogHelper.d("Publish SpeexAudio Thread Release", subTAG);
	}
}
