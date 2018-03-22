package com.duanyy.media.filter;

import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.Matrix;

import com.duanyy.media.glutil.BufferUtils;
import com.duanyy.media.glutil.OpenGlUtils;


/**
 * Created by duanyy on 2018/3/21.
 */

public class VideoOESRender extends BaseFilter{

    @Override
    protected void initCoordinates() {
        float[] mVertexCoords = {-1f,-1f,  -1f,1f,  1f,-1f,  -1f,1f,  1f,1f,  1f,-1f};
        float[] mFragmentCoords = { 0f,0f,  0f,1f,  1f,0f,  0f,1f,  1f,1f,  1f,0f };
        mVertexBuffer = BufferUtils.float2Buffer(mVertexCoords);
        mFragmentBuffer = BufferUtils.float2Buffer(mFragmentCoords);
    }

    @Override
    protected void initProgram() {
        mProgramId = OpenGlUtils.loadProgram(VERTEX_SHADER,FRAGMENT_SHADER_OES);
        Matrix.setIdentityM(mMVPMatrix,0);
    }

    @Override
    protected void initFbo(int width, int height) {

    }

    @Override
    public void release() {
        if (mFbo != null) {
            mFbo.close();
            mFbo = null;
        }
        if (mProgramId >= 0){
            GLES20.glDeleteProgram(mProgramId);
            mProgramId = -1;
        }
    }

    @Override
    public void onSurfaceSizeChanged(int width,int height) {

    }

    @Override
    public void drawFrame(int textureId) {
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);


        //oes
        GLES20.glUseProgram(mProgramId);
        int a_position = GLES20.glGetAttribLocation(mProgramId,"a_position");
        int a_texture = GLES20.glGetAttribLocation(mProgramId,"a_texture");
        int u_mvpMatrix = GLES20.glGetUniformLocation(mProgramId,"u_MVPMatrix");
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER,0);
        GLES20.glEnableVertexAttribArray(a_position);
        GLES20.glEnableVertexAttribArray(a_texture);

        mVertexBuffer.position(0);
        mFragmentBuffer.position(0);
        GLES20.glVertexAttribPointer(a_position,2,GLES20.GL_FLOAT,false,0, mVertexBuffer);
        GLES20.glVertexAttribPointer(a_texture,2,GLES20.GL_FLOAT,false,0,mFragmentBuffer);
        GLES20.glUniformMatrix4fv(u_mvpMatrix,1,false,mMVPMatrix,0);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,textureId);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES,0,6);

        GLES20.glDisableVertexAttribArray(a_position);
        GLES20.glDisableVertexAttribArray(a_texture);
        GLES20.glUseProgram(0);
    }

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
