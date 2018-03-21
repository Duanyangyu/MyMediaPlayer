package com.duanyy.media.player;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.Surface;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;

import com.duanyy.media.decoder.VideoDecoder;
import com.duanyy.media.glutil.BufferUtils;
import com.duanyy.media.glutil.OpenGlUtils;
import com.duanyy.media.utils.VideoSize;

import java.nio.FloatBuffer;
import java.security.acl.LastOwnerException;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by duanyy on 2018/3/19.
 */

public class VideoPlayerView extends GLSurfaceView implements IPlayer ,GLSurfaceView.Renderer{

    public static final String TAG = "VideoPlayerView";


    private String mVideoSource;
    private VideoDecoder mDecoder;

    private FloatBuffer mVertexBuffer;
    private FloatBuffer mFragmentBuffer;
    private int mPreviewTextureId;
    private SurfaceTexture mPreviewSurfaceTexture;
    private int mProgramId;

    private float[] mMVPMatrix = new float[16];
    private float[] mVertexCoords;

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

    private void initProgram(){
        mProgramId = OpenGlUtils.loadProgram(VERTEX_SHADER,FRAGMENT_SHADER_OES);
        mVertexBuffer = BufferUtils.float2Buffer(mVertexCoords);
        mFragmentBuffer = BufferUtils.float2Buffer(mFragmentCoords);

        Matrix.setIdentityM(mMVPMatrix,0);
    }

    private void initDecoder(Surface surface){
        if (!TextUtils.isEmpty(mVideoSource)){
            mDecoder = new VideoDecoder();
            mDecoder.setDataSource(mVideoSource);
            mDecoder.setSurface(surface);
            if (mDecoder.prepare()){
                Log.e(TAG,"initDecoder prepared!");
                VideoSize videoSize = mDecoder.getVideoSize();
                calFragmentCoordsByVideoSize(videoSize);
                int videoRotation = mDecoder.getVideoRotation();
            }
        }else {
            throw new RuntimeException("mVideoSource is null");
        }
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

        Surface surface = new Surface(mPreviewSurfaceTexture);
        initDecoder(surface);

        initProgram();
        Log.e(TAG,"onSurfaceCreated");
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {

    }

    @Override
    public void onDrawFrame(GL10 gl) {

        mPreviewSurfaceTexture.updateTexImage();

        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);

        GLES20.glUseProgram(mProgramId);
        int a_positin = GLES20.glGetAttribLocation(mProgramId,"a_position");
        int a_texture = GLES20.glGetAttribLocation(mProgramId,"a_texture");
        int u_mvpMatrix = GLES20.glGetUniformLocation(mProgramId,"u_MVPMatrix");

        GLES20.glEnableVertexAttribArray(a_positin);
        GLES20.glEnableVertexAttribArray(a_texture);

        GLES20.glVertexAttribPointer(a_positin,2,GLES20.GL_FLOAT,false,0,mVertexBuffer);
        GLES20.glVertexAttribPointer(a_texture,2,GLES20.GL_FLOAT,false,0,mFragmentBuffer);
        GLES20.glUniformMatrix4fv(u_mvpMatrix,1,false,mMVPMatrix,0);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,mPreviewTextureId);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES,0,6);

        GLES20.glDisableVertexAttribArray(a_positin);
        GLES20.glDisableVertexAttribArray(a_texture);
        GLES20.glUseProgram(0);
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

    private void calFragmentCoordsByVideoSize(VideoSize size){
        if (size == null) {
            return;
        }
        float ratio = size.getWidth()/size.getHeight();

        float left = -1,top = 1,right = 1,bottom = -1;
        if (ratio > 1){
            bottom = -1/ratio;
            top = 1/ratio;
        }else {
            left = -ratio;
            right = ratio;
        }
        Log.e(TAG,"calFragmentCoordsByVideoSize ratio="+ratio+", left="+left+", top="+top+", right="+right+", bottom="+bottom);
        mVertexCoords = new float[]{left,bottom,left,top,right,bottom,left,top,right,top,right,bottom};
    }

    private static float mFragmentCoords[] = {
            0f,0f,  0f,1f,  1f,0f,  0f,1f,  1f,1f,  1f,0f
    };

    private static final String VERTEX_SHADER =
            "uniform mat4 u_MVPMatrix;" +
                    "attribute vec4 a_position;" +
                    "attribute vec2 a_texture;" +
                    "varying vec2 textureCoordinate;" +
                    "void main()" +
                    "{"+
                    "gl_Position = u_MVPMatrix * a_position;"+
                    "textureCoordinate = a_texture;" +
                    "}";

    private static final String FRAGMENT_SHADER_OES =
            "#extension GL_OES_EGL_image_external : require\n"+
                    "precision mediump float;" +
                    "varying vec2 textureCoordinate;\n" +
                    "uniform samplerExternalOES s_texture;\n" +
                    "void main() {" +
                    "  gl_FragColor = texture2D( s_texture, textureCoordinate );\n" +
                    "}";
}
