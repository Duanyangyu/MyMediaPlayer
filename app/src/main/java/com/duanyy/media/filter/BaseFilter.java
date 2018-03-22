package com.duanyy.media.filter;

import com.duanyy.media.glutil.FboHelper;

import java.nio.FloatBuffer;

/**
 * Created by duanyy on 2018/3/21.
 */

public abstract class BaseFilter {

    protected static final String TAG = BaseFilter.class.getSimpleName();

    protected FloatBuffer mVertexBuffer;
    protected FloatBuffer mFragmentBuffer;
    protected float[] mMVPMatrix = new float[16];

    protected int mProgramId;
    protected FboHelper mFbo;

    public void init(){
        initCoordinates();
        initProgram();
    }


    public abstract void drawFrame(int textureId);

    protected abstract void initCoordinates();

    protected abstract void initProgram();

    protected abstract void initFbo(int width,int height);

    public abstract void onSurfaceSizeChanged(int width,int height);

    public int getTargetTexture(){
        if (mFbo != null) {
            return mFbo.textureId();
        }
        return 0;
    }

    public abstract void release();



}
