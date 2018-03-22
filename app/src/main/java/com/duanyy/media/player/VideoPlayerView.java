package com.duanyy.media.player;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.Surface;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;

import com.duanyy.media.decoder.VideoDecoder;
import com.duanyy.media.filter.VideoMosaicFilter;
import com.duanyy.media.filter.VideoOESRender;
import com.duanyy.media.utils.VideoSize;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by duanyy on 2018/3/19.
 */

public class VideoPlayerView extends GLSurfaceView implements IPlayer ,GLSurfaceView.Renderer{

    public static final String TAG = "VideoPlayerView";

    private String mVideoSource;
    private VideoDecoder mDecoder;

    private int mPreviewTextureId;
    private SurfaceTexture mPreviewSurfaceTexture;


    private VideoMosaicFilter mMosaicFilter;
    private VideoOESRender mVideoOESRender;

    public VideoPlayerView(Context context) {
        this(context,null);
    }

    public VideoPlayerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public void setDataSource(String dataSource){
        if (TextUtils.isEmpty(dataSource))
            return;
        if (dataSource.equals(mVideoSource))
            return;

        mVideoSource = dataSource;
        Log.e(TAG,"setDataSource:"+dataSource);
    }

    @Override
    public void play() {
        if (mDecoder != null) {
            mDecoder.play();
        }
    }

    @Override
    public void pause() {
        if (mDecoder != null) {
            mDecoder.pause();
        }
    }

    @Override
    public void resume() {
        if (mDecoder != null) {
            mDecoder.resume();
        }
    }

    @Override
    public void onDestroy() {
        if (mDecoder != null) {
            mDecoder.release();
        }
        if (mMosaicFilter != null) {
            mMosaicFilter.release();
        }
        if (mVideoOESRender != null) {
            mVideoOESRender.release();
        }
    }

    private void init(){
        setEGLContextClientVersion(2);
        setRenderer(this);
        setRenderMode(RENDERMODE_WHEN_DIRTY);

        reLayoutSelf();
    }

    private void reLayoutSelf(){
        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                getViewTreeObserver().removeOnGlobalLayoutListener(this);
                int width = getWidth();
                Log.e(TAG,"onGlobalLayout w="+width);
                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(width,width);
                params.gravity = Gravity.TOP;
                setLayoutParams(params);
            }
        });
    }

    private void initDecoder(Surface surface){
        if (!TextUtils.isEmpty(mVideoSource)){
            mDecoder = new VideoDecoder();
            mDecoder.setDataSource(mVideoSource);
            mDecoder.setSurface(surface);
            if (mDecoder.prepare()){
                Log.e(TAG,"initDecoder prepared!");
                VideoSize videoSize = mDecoder.getVideoSize();
                if (mMosaicFilter != null) {
                    mMosaicFilter.setContentSize(videoSize.getWidth(),videoSize.getHeight());
                }
            }
        }else {
            throw new RuntimeException("mVideoSource is null");
        }
    }

    private void initFilter(){
        mMosaicFilter = new VideoMosaicFilter();
        mMosaicFilter.init();

        mVideoOESRender = new VideoOESRender();
        mVideoOESRender.init();
    }

    private SurfaceTexture.OnFrameAvailableListener mFrameAvailableListener = new SurfaceTexture.OnFrameAvailableListener() {
        @Override
        public void onFrameAvailable(SurfaceTexture surfaceTexture) {
            Log.e(TAG,"onFrameAvailable");
            requestRender();
        }
    };

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        mPreviewTextureId = getPreviewTextureId();
        mPreviewSurfaceTexture = new SurfaceTexture(mPreviewTextureId);
        mPreviewSurfaceTexture.setOnFrameAvailableListener(mFrameAvailableListener);

        initFilter();

        Surface surface = new Surface(mPreviewSurfaceTexture);
        initDecoder(surface);
        Log.e(TAG,"onSurfaceCreated");
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        if (mMosaicFilter != null) {
            mMosaicFilter.onSurfaceSizeChanged(width,height);
        }
        if (mVideoOESRender != null) {
            mVideoOESRender.onSurfaceSizeChanged(width,height);
        }
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        mPreviewSurfaceTexture.updateTexImage();

        int textureUse = -1;
        if (mMosaicFilter != null) {
            mMosaicFilter.drawFrame(mPreviewTextureId);
            textureUse = mMosaicFilter.getTargetTexture();
        }

        if (mVideoOESRender != null) {
            mVideoOESRender.drawFrame(textureUse);
        }
    }

    private int getPreviewTextureId(){
        int[] texture = new int[1];

        GLES20.glGenTextures(1, texture, 0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, texture[0]);

        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_MIN_FILTER,GL10.GL_LINEAR);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);

        return texture[0];
    }

}
