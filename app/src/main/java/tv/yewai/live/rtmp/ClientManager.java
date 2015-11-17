package tv.yewai.live.rtmp;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.apache.log4j.Logger;
import org.apache.mina.core.buffer.IoBuffer;
import org.red5.io.ITag;
import org.red5.io.IoConstants;
import org.red5.io.flv.Tag;


public class ClientManager implements Runnable, Consumer {

	public enum DataType{
		AUDIO,KEY_FRAME,INTER_FRAME,DISPOSABLE_INTER_FRAME,HEADER
	}

	private static Logger log = Logger.getLogger(ClientManager.class);
	
	private final Object mutex = new Object();
	public static final int NETONLY = 1;
	public static final int FILEONLY = 2;
	public static final int NETANDFILE = 3;
	private int mode = NETONLY;
	private volatile boolean isRecording;
	private volatile boolean isRunning;
	private List<ITag> dataQueue;
	private String streamName;
	private RtmpHandler rtmpClient = new RtmpHandler();

	private long timeBase;
	private int prevSize = 0;

	public ClientManager(String rtmpDomain,String streamName) {
		super();
		dataQueue = Collections.synchronizedList(new LinkedList<ITag>());
		this.streamName = streamName;
		rtmpClientInit(rtmpDomain);
	}

	private void rtmpClientInit(String doman) {
		rtmpClient.setHost(doman.substring(0, doman.lastIndexOf("/")));
		rtmpClient.setPort(1935);
		String app = doman.substring(doman.lastIndexOf("/")+1);
		rtmpClient.setApp(app);
	}

	public void setMode(int mode) {
		this.mode = mode;
	}

	public void run() {
		log.debug("publish thread runing");
		while (this.isRunning()) {
			synchronized (mutex) {
				while (!this.isRecording) {
					try {
						mutex.wait();
					} catch (InterruptedException e) {
						throw new IllegalStateException("Wait() interrupted!",
								e);
					}
				}
			}

			startClient();
			
			while (this.isRecording()) {
				if (dataQueue.size() > 0) {
					writeTag();
					log.debug("list size = "+ dataQueue.size());
				} else {
					try {
						Thread.sleep(20);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
			
			while(dataQueue.size() > 0){
				writeTag();
				log.debug("list size = "+ dataQueue.size());
			}
			stop();
		}
	}
	
	private void startClient() {
		switch (this.mode) {
		case NETONLY:
			rtmpClient.start(streamName, "publish"/*"record"*/, null);
			break;
		default:
			rtmpClient.start(streamName, "record", null);
		}
	}
	
	private void writeTag() {
		rtmpClient.writeTag(dataQueue.remove(0));
	}

	public synchronized void putData(ITag tag) {
		dataQueue.add(tag);
	}

	public synchronized void putData(DataType dataType, long ts, byte[] buf, int size) {
		
		if (timeBase == 0) {
			timeBase = ts;
		}
		
		int currentTime = (int) (ts - timeBase);		
		ITag tag = null;
		byte tagType = 0;
		switch (dataType) {
		case AUDIO:
			tag = new Tag(IoConstants.TYPE_AUDIO, currentTime, size + 1, null, prevSize);
			prevSize  = size + 1;
			tagType = (byte)0xB2;
			break;
		case KEY_FRAME:
			tag = new Tag(IoConstants.TYPE_VIDEO, currentTime, size + 1, null, prevSize);
			prevSize  = size + 1;
			tagType = 19;
			break;
		case INTER_FRAME:
			tag = new Tag(IoConstants.TYPE_VIDEO, currentTime, size + 1, null, prevSize);
			prevSize  = size + 1;
			tagType = 35;
			break;
		case DISPOSABLE_INTER_FRAME:
			break;
		case HEADER:
			tag = new Tag((byte)0x12, currentTime, size + 1, null, prevSize);
			prevSize  = size + 1;
			tagType = 0;
			break;
		default:
			//
		}

		IoBuffer body = IoBuffer.allocate(tag.getBodySize());
		body.setAutoExpand(true);
		body.put(tagType);
		body.put((byte[]) resizeArray(buf, size));
		body.flip();
		body.limit(tag.getBodySize());
		tag.setBody(body);
		dataQueue.add(tag);	
	}


	private static Object resizeArray (Object oldArray, int newSize) {
		   int oldSize = java.lang.reflect.Array.getLength(oldArray);
		   Class<?> elementType = oldArray.getClass().getComponentType();
		   Object newArray = java.lang.reflect.Array.newInstance(
		         elementType,newSize);
		   int preserveLength = Math.min(oldSize,newSize);
		   if (preserveLength > 0)
		      System.arraycopy (oldArray,0,newArray,0,preserveLength);
		   return newArray; 
	}

	private void stop() {
		rtmpClient.stop();
	}

	public boolean isRunning() {
		synchronized (mutex) {
			return isRunning;
		}
	}

	public void setRunning(boolean isRunning) {
		synchronized (mutex) {
			this.isRunning = isRunning;
			if (this.isRunning) {
				mutex.notify();
			}
		}
	}

	public void setRecording(boolean isRecording) {
		synchronized (mutex) {
			this.isRecording = isRecording;
			if (this.isRecording) {
				mutex.notify();
			}
		}
	}

	public boolean isRecording() {
		synchronized (mutex) {
			return isRecording;
		}
	}

}
