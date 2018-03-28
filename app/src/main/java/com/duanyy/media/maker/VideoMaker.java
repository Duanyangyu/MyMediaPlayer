package com.duanyy.media.maker;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.util.Log;
import android.view.Surface;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by duanyy on 2018/3/26.
 */

public class VideoMaker implements IMaker{

    public static final String TAG = VideoMaker.class.getSimpleName();
    private static final boolean VERBOSE = true;

    private static final String MIME_TYPE = "video/avc";
    private static final int VIDEO_WIDTH = 320;
    private static final int VIDEO_HEIGHT = 480;
    private static final int BIT_RATE = 4000000;
    private static final int FRAMES_PER_SECOND = 4;
    private static final int IFRAME_INTERVAL = 5;
    private long mFakePts;

    private String mTargetFilePath;

    private MediaCodec mEncoder;
    private MediaCodec.BufferInfo mBufferInfo;
    private Surface mInputSurface;

    private MediaMuxer mMuxer;
    private boolean mMuxerStarted;
    private int mTrackIndex = -1;

    private static final long TIME_OUT = 10000;
    private static final int SUM_FRAME = 16;

    public void generateMoview(String targetPath){
        this.mTargetFilePath = targetPath;
        boolean encoderPrepared = prepareEncoder();
        boolean muxerPrepared = prepareMuxer();
        Log.d(TAG,"generateMoview encoderPrepared="+encoderPrepared+", muxerPrepared="+muxerPrepared);

        ExecutorService executorService = Executors.newScheduledThreadPool(1);
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < SUM_FRAME; i++) {
                    drainEncoder(false);
                    generateFrame(i);
                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                drainEncoder(true);
            }
        });
    }

    private boolean prepareEncoder(){
        try {
            mEncoder = MediaCodec.createEncoderByType(MIME_TYPE);
            mBufferInfo = new MediaCodec.BufferInfo();
            MediaFormat format = MediaFormat.createVideoFormat(MIME_TYPE,VIDEO_WIDTH,VIDEO_HEIGHT);
            format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
            format.setInteger(MediaFormat.KEY_BIT_RATE,BIT_RATE);//码率
            format.setInteger(MediaFormat.KEY_FRAME_RATE,FRAMES_PER_SECOND);//帧率
            format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL,IFRAME_INTERVAL);//关键帧
            if (VERBOSE) Log.d(TAG,"prepareEncoder format="+format);
            mEncoder.configure(format,null,null,MediaCodec.CONFIGURE_FLAG_ENCODE);
            mInputSurface = mEncoder.createInputSurface();
            mEncoder.start();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean prepareMuxer(){
        try {
            mMuxer = new MediaMuxer(mTargetFilePath,MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
            mTrackIndex = -1;
            mMuxerStarted = false;
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void drainEncoder(boolean endOfStream){
        if (endOfStream){
            if (VERBOSE) Log.d(TAG,"end of stream~");
            mEncoder.signalEndOfInputStream();
        }

        while (true){
            int outputBufferIndex = mEncoder.dequeueOutputBuffer(mBufferInfo,TIME_OUT);
            if (outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED){
                Log.d(TAG,"MediaCodec.INFO_OUTPUT_FORMAT_CHANGED~");
                if (mMuxerStarted){
                    throw new RuntimeException("format changed twice");
                }
                MediaFormat newFormat = mEncoder.getOutputFormat();
                mTrackIndex = mMuxer.addTrack(newFormat);
                mMuxer.start();
                mMuxerStarted = true;
            }else if (outputBufferIndex == MediaCodec.INFO_TRY_AGAIN_LATER){
                if (VERBOSE) Log.d(TAG,"MediaCodec.INFO_TRY_AGAIN_LATER~");
                if (!endOfStream) {
                    break;      // out of while
                } else {
                    if (VERBOSE) Log.d(TAG, "no output available, spinning to await EOS");
                }
            }else if (outputBufferIndex < 0){
                if (VERBOSE) Log.d(TAG,"outputBufferIndex illegal~");
            }else {
                ByteBuffer outputBuffer = mEncoder.getOutputBuffer(outputBufferIndex);
                int length = mBufferInfo.size;
                if (length > 0){
                    outputBuffer.position(mBufferInfo.offset);
                    outputBuffer.limit(mBufferInfo.offset+mBufferInfo.size);
                    mBufferInfo.presentationTimeUs = mFakePts;
                    mFakePts += 1000000L / FRAMES_PER_SECOND;
                    mMuxer.writeSampleData(mTrackIndex,outputBuffer,mBufferInfo);
                }
                mEncoder.releaseOutputBuffer(outputBufferIndex,false);
                if (VERBOSE) Log.d(TAG,"outputBufferIndex="+outputBufferIndex+", outputBuffer.size="+length+"， presentationTimeUs="+mBufferInfo.presentationTimeUs+"， mFakePts="+mFakePts);

                if ((mBufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0){
                    if (!endOfStream){
                        Log.e(TAG,"reached end of stream unexpectedly~");
                    }else {
                        if (VERBOSE) Log.d(TAG, "end of stream reached");
                    }
                    break;
                }
            }
        }
    }

    private void generateFrame(int frameNum){
        Canvas canvas = mInputSurface.lockCanvas(null);
        try {
            int width = canvas.getWidth();
            int height = canvas.getHeight();
            Paint paint = new Paint();
            for (int i = 0; i < SUM_FRAME; i++) {
                int color = 0xff000000;
                if ((i & color) != 0){
                    color |= 0x00ff0000;
                }
                if ((i & 0x02) != 0) {
                    color |= 0x0000ff00;
                }
                if ((i & 0x04) != 0) {
                    color |= 0x000000ff;
                }
                paint.setColor(color);

                float sliceWidth = width / 8;
                canvas.drawRect(sliceWidth * i, 0, sliceWidth * (i+1), height, paint);
            }
            paint.setColor(0x80808080);
            float sliceHeight = height / 8;
            int frameMod = frameNum % 8;
            canvas.drawRect(0, sliceHeight * frameMod, width, sliceHeight * (frameMod+1), paint);
        }finally {
            mInputSurface.unlockCanvasAndPost(canvas);
        }
    }

    public void release(){
        if (mEncoder != null) {
            mEncoder.stop();
            mEncoder.release();
            mEncoder = null;
        }
        if (mInputSurface != null) {
            mInputSurface.release();
            mInputSurface = null;
        }
        if (mMuxer != null) {
            mMuxer.stop();
            mMuxer.release();
            mMuxer = null;
        }
    }

}
