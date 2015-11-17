package tv.yewai.live.rtmp;


public interface Consumer {
	
	public void putData(ClientManager.DataType dataType, long ts, byte[] buf, int size);

	public void setRecording(boolean isRecording);

	public boolean isRecording();

}
