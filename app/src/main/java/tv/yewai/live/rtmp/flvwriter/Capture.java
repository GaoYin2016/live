package tv.yewai.live.rtmp.flvwriter;

import android.graphics.Bitmap;
import java.io.IOException;

/**
 *  An interface to render movies
 * @author ceraj
 *
 */
public interface Capture
{
	/**
	 * Writes Image to capture stream
	 * @param image Image to write
	 * @param timestamp Time stamp in milliseconds
	 * @throws IOException
	 */
	void writeFrame(Bitmap image, int timestamp) throws IOException;
}
