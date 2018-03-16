package com.duanyy.media.decoder;

import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.text.TextUtils;
import android.util.Log;

import java.nio.ByteBuffer;

/**
 * Created by Duanyy on 2017/7/22.
 */
public class VideoDecoder {

    public static final String TAG = "VideoDecoder";

    private String mVideoSource;
    private Object mSync;
    private boolean isMediaPlaying;

    private MediaCodec mVideoDecoder;
    private MediaExtractor mVideoExtractor;
    private MediaFormat mVideoFormat;

    private static final long OUT_TIME_US = 10000;


    public VideoDecoder(){
        mSync = new Object();
    }

    public void pause(){

    }

    public void resume(){

    }

    public void setDataSource(String dataSource){
        if (TextUtils.isEmpty(dataSource))
            return;
        if (dataSource.equals(mVideoSource))
            return;

        mVideoSource = dataSource;
        Log.e(TAG,"dataSource:"+dataSource);
        boolean prepare = prepare();
        Log.e(TAG,"prepare:"+prepare);
        if (prepare){

        }
    }

    private boolean prepare(){
        boolean success = true;

        mVideoExtractor = new MediaExtractor();
        try {
            mVideoExtractor.setDataSource(mVideoSource);
            selectTrack(mVideoExtractor);
            String mime = mVideoFormat.getString(MediaFormat.KEY_MIME);
            Log.e(TAG,"mime="+mime);
            mVideoDecoder = MediaCodec.createDecoderByType(mime);
            mVideoDecoder.configure(mVideoFormat,null,null,0);
            mVideoDecoder.start();
        } catch (Exception e) {
            e.printStackTrace();
            success = false;
        }

        return success;
    }

    private void selectTrack(MediaExtractor extractor){
        if (extractor == null) {
            return;
        }
        int count = extractor.getTrackCount();
        for (int i = 0; i < count; i++) {
            MediaFormat format = extractor.getTrackFormat(i);
            String string = format.getString(MediaFormat.KEY_MIME);
            if (TextUtils.isEmpty(string))
                return;
            if (string.startsWith("video")){
                extractor.selectTrack(i);
                mVideoFormat = format;
                break;
            }
        }
    }

    private void doExtract(){
        boolean outputDone = false;
        boolean inputDone = false;
        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
        while (!outputDone){
            if (!inputDone){
                int inputBufferId = mVideoDecoder.dequeueInputBuffer(OUT_TIME_US);
                ByteBuffer inputBuffer = mVideoDecoder.getInputBuffer(inputBufferId);
                int size = mVideoExtractor.readSampleData(inputBuffer, 0);
                if (size < 0){
                    inputDone = true;
                    mVideoDecoder.queueInputBuffer(inputBufferId,0,size,0,MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                    Log.e(TAG,"input eos!");
                }else {
                    long sampleTime = mVideoExtractor.getSampleTime();
                    mVideoDecoder.queueInputBuffer(inputBufferId,0,size,sampleTime,0);
                    mVideoExtractor.advance();
                    Log.e(TAG,"input data size="+size+", sampleTime="+sampleTime);
                }

            }
            if (!outputDone){
                int outputBufferId = mVideoDecoder.dequeueOutputBuffer(bufferInfo, OUT_TIME_US);
                ByteBuffer outputBuffer = mVideoDecoder.getOutputBuffer(outputBufferId);

            }
        }
    }

}
