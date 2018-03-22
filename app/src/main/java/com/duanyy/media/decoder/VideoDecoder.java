package com.duanyy.media.decoder;

import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.text.TextUtils;
import android.util.Log;
import android.view.Surface;

import com.duanyy.media.utils.VideoSize;

import java.nio.ByteBuffer;

import static android.media.MediaCodec.INFO_OUTPUT_FORMAT_CHANGED;
import static android.media.MediaCodec.INFO_TRY_AGAIN_LATER;

/**
 * Created by Duanyy on 2017/7/22.
 * 使用：
 *  decoder.setDataSource()
 *  decoder.setSurface()
 *  decoder.prepare()
 *  decoder.play()
 */
public class VideoDecoder implements IDecoder{

    public static final String TAG = "VideoDecoder";

    private String mVideoSource;
    private Object mSync;
    private boolean mPause;
    private boolean mPrepared;

    private MediaCodec mVideoDecoder;
    private MediaExtractor mVideoExtractor;
    private MediaFormat mVideoFormat;
    private Surface mOutputSurface;

    private static final long OUT_TIME_US = 10000;


    public VideoDecoder(){
        mSync = new Object();
    }

    @Override
    public void pause(){
        synchronized (mSync){
            mPause = true;
        }
    }

    @Override
    public void resume(){
        synchronized (mSync){
            mPause = false;
            mSync.notifyAll();
        }
    }

    @Override
    public void play() {
        if (mPrepared){
            decodeThread();
        }
    }

    @Override
    public void setDataSource(String dataSource){
        if (TextUtils.isEmpty(dataSource))
            return;
        if (dataSource.equals(mVideoSource))
            return;

        mVideoSource = dataSource;
        Log.e(TAG,"dataSource:"+dataSource);
    }

    @Override
    public void release() {
        //TODO release self.
    }

    public void setSurface(Surface surface){
        this.mOutputSurface = surface;
    }

    public boolean prepare(){
        boolean success = true;

        mVideoExtractor = new MediaExtractor();
        try {
            mVideoExtractor.setDataSource(mVideoSource);
            mVideoFormat = selectTrack(mVideoExtractor);
            String mime = mVideoFormat.getString(MediaFormat.KEY_MIME);
            Log.e(TAG,"mime="+mime);
            mVideoDecoder = MediaCodec.createDecoderByType(mime);
            mVideoDecoder.configure(mVideoFormat,mOutputSurface,null,0);
            mVideoDecoder.start();
        } catch (Exception e) {
            e.printStackTrace();
            success = false;
        }
        mPrepared = success;
        return success;
    }

    /* 这个方法应该在prepare返回true以后调用 */
    public VideoSize getVideoSize(){
        if (mVideoFormat != null) {
            int width = mVideoFormat.getInteger(MediaFormat.KEY_WIDTH);
            int height = mVideoFormat.getInteger(MediaFormat.KEY_HEIGHT);
            Log.e(TAG,"getVideoSize: width="+width+", height="+height);
            return new VideoSize((float) width,(float)height);
        }else {
            return null;
        }
    }

    public int getVideoRotation(){
        if (mVideoFormat != null) {
            if (mVideoFormat.containsKey(MediaFormat.KEY_ROTATION)){
                int rotation = mVideoFormat.getInteger(MediaFormat.KEY_ROTATION);
                Log.e(TAG,"getVideoRotation rotation="+rotation);
                return rotation;
            }
        }
//        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
//        retriever.setDataSource(mVideoSource);
//        int rotation = Integer.parseInt(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION));
//        Log.e(TAG,"getVideoRotation rotation="+rotation);
        return 0;
    }

    private MediaFormat selectTrack(MediaExtractor extractor){
        if (extractor == null) {
            return null;
        }
        int count = extractor.getTrackCount();
        for (int i = 0; i < count; i++) {
            MediaFormat format = extractor.getTrackFormat(i);
            String string = format.getString(MediaFormat.KEY_MIME);
            if (TextUtils.isEmpty(string))
                return null;
            if (string.startsWith("video")){
                extractor.selectTrack(i);
                return format;
            }
        }
        return null;
    }

    private void decodeThread(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                doDecode();
            }
        }).start();
    }

    private void doDecode(){
        boolean outputDone = false;
        boolean inputDone = false;
        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
        while (!outputDone){

            synchronized (mSync){
                if (mPause){
                    try {
                        mSync.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

            if (!inputDone){
                int inputBufferId = mVideoDecoder.dequeueInputBuffer(OUT_TIME_US);
                if (inputBufferId >= 0){
                    ByteBuffer inputBuffer = mVideoDecoder.getInputBuffer(inputBufferId);
                    int size = mVideoExtractor.readSampleData(inputBuffer, 0);
                    if (size <= 0){
                        inputDone = true;
                        mVideoDecoder.queueInputBuffer(inputBufferId,0,0,0,MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                        Log.e(TAG,"input eos!");
                    }else {
                        long sampleTime = mVideoExtractor.getSampleTime();
                        mVideoDecoder.queueInputBuffer(inputBufferId,0,size,sampleTime,0);
                        mVideoExtractor.advance();
                        Log.e(TAG,"input data size="+size+", sampleTime="+sampleTime);
                    }
                }
            }

            if (!outputDone){
                int outputBufferId = mVideoDecoder.dequeueOutputBuffer(bufferInfo, OUT_TIME_US);
                Log.e(TAG,"outputBufferId:"+outputBufferId);
                if(outputBufferId == INFO_TRY_AGAIN_LATER){

                }else if (outputBufferId == INFO_OUTPUT_FORMAT_CHANGED){

                }else {
                    mVideoDecoder.releaseOutputBuffer(outputBufferId,true);
                }
            }
        }
    }

}
