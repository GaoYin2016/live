package tv.yewai.live.rtmp.flvwriter.impl;

import android.graphics.Bitmap;
import java.io.IOException;
import java.io.OutputStream;

import tv.yewai.live.rtmp.flvwriter.Capture;

public class Capturer implements Capture
{
	FLVStream worker;
	int timeBetweenKeyframes = 0;
	int lastKeyFrameTimestamp = Integer.MIN_VALUE/4;
	int captureSizeWidth, captureSizeHeight;
	public Capturer(OutputStream os,int captureSizeWidth,int captureSizeHeight) throws IOException
	{
		if (captureSizeWidth % 16 != 0 || captureSizeHeight % 16 != 0)
		{
			throw new RuntimeException("size needs to be dividibe with 16");
		}
		if( captureSizeHeight > 4095 || captureSizeWidth > 4095 )
		{
			throw new RuntimeException("size needs to be less than 4096");
		}
		worker = new FLVStream(os, FLVStream.VIDEO);
		this.captureSizeWidth = captureSizeWidth;
		this.captureSizeHeight = captureSizeHeight;
	}
	
	public Capturer(OutputStream os, int captureSizeWidth,int captureSizeHeight, int timeBetweenKeyframes ) throws IOException
	{
		if (captureSizeWidth % 16 != 0 || captureSizeHeight % 16 != 0)
		{
			throw new RuntimeException("size needs to be dividibe with 16");
		}
		if( captureSizeHeight > 4095 || captureSizeWidth > 4095 )
		{
			throw new RuntimeException("size needs to be less than 4096");
		}
		this.timeBetweenKeyframes = timeBetweenKeyframes ;
		worker = new FLVStream(os, FLVStream.VIDEO);
		this.captureSizeWidth = captureSizeWidth;
		this.captureSizeHeight = captureSizeHeight;
	}

	public void writeFrame(Bitmap image, int timestamp) throws IOException
	{
		if( timestamp - lastKeyFrameTimestamp > timeBetweenKeyframes )
		{
			worker.writeImage(image, timestamp);
			lastKeyFrameTimestamp = timestamp ;
		}
		else
		{
			worker.writeInterframeImage(image, timestamp);
		}
	}
}
