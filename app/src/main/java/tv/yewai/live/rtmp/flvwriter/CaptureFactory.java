package tv.yewai.live.rtmp.flvwriter;

import java.io.IOException;
import java.io.OutputStream;

import tv.yewai.live.rtmp.flvwriter.impl.Capturer;

public class CaptureFactory
{
	/**
	 * Creates new FLV capture Stream
	 * @param os stream to write FLV 
	 * @return an instance of @Capture
	 * @throws IOException
	 */
	public static Capturer getCapturer( OutputStream os ,int captureSizeWidth,int captureSizeHeight ) throws IOException
	{
		return new Capturer( os , captureSizeWidth,captureSizeHeight ) ;
	}

	/**
	 * Creates new FLV capture Stream
	 * @param os stream to write FLV 
	 * @param timeBetweenKeyframes number of milliseconds between two keyframes
	 * @return an instance of @Capture
	 * @throws IOException
	 */
	public static Capturer getCapturer( OutputStream os  ,int captureSizeWidth,int captureSizeHeight  , int timeBetweenKeyframes ) throws IOException
	{
		return new Capturer( os , captureSizeWidth,captureSizeHeight  , timeBetweenKeyframes ) ;
	}
	
}
