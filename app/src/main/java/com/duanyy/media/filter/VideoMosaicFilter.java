package com.duanyy.media.filter;

import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

import com.duanyy.media.glutil.BufferUtils;
import com.duanyy.media.glutil.FboHelper;
import com.duanyy.media.glutil.OpenGlUtils;

import java.nio.FloatBuffer;

/**
 * Created by duanyy on 2018/3/21.
 */

public class VideoMosaicFilter extends BaseFilter{

    private int mProgramMosaicId;
    private FloatBuffer mVertexBufferMosaicBg;

    @Override
    protected void initCoordinates() {
        float[] vertexMosaicBg = { -1f,-1f,  -1f,1f,  1f,-1f,  -1f,1f,  1f,1f,  1f,-1f };
        float[] fragment = { 0f,0f,  0f,1f,  1f,0f,  0f,1f,  1f,1f,  1f,0f };
        mVertexBufferMosaicBg = BufferUtils.float2Buffer(vertexMosaicBg);
        mFragmentBuffer = BufferUtils.float2Buffer(fragment);
    }

    @Override
    protected void initProgram() {
        mProgramId = OpenGlUtils.loadProgram(VERTEX_SHADER, FRAGMENT_SHADER);
        mProgramMosaicId = OpenGlUtils.loadProgram(VERTEX_SHADER, SIMPLE_MOSAIC_FRAGMENT_SHADER);
    }

    @Override
    protected void initFbo(int width, int height) {
        mFbo = new FboHelper(width, height);
        mFbo.createFbo();
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
        if (mProgramMosaicId >= 0){
            GLES20.glDeleteProgram(mProgramMosaicId);
            mProgramMosaicId = -1;
        }
    }

    @Override
    public void onSurfaceSizeChanged(int width,int height) {
        this.initFbo(width,height);
    }

    public void setContentSize(float width,float height){
        float[] array = calVertexCoordsByVideoSize(width,height);
        mVertexBuffer = BufferUtils.float2Buffer(array);
    }

    @Override
    public void drawFrame(int textureId) {
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFbo.frameId());
        GLES20.glUseProgram(mProgramMosaicId);

        int a_position = GLES20.glGetAttribLocation(mProgramMosaicId,"a_position");
        int a_texture = GLES20.glGetAttribLocation(mProgramMosaicId,"a_texture");

        //draw Mosaic background to FrameBuffer.
        GLES20.glEnableVertexAttribArray(a_position);
        GLES20.glEnableVertexAttribArray(a_texture);
        mVertexBufferMosaicBg.position(0);
        mFragmentBuffer.position(0);
        GLES20.glVertexAttribPointer(a_position, 2, GLES20.GL_FLOAT, false, 0, mVertexBufferMosaicBg);
        GLES20.glVertexAttribPointer(a_texture, 2, GLES20.GL_FLOAT, false, 0, mFragmentBuffer);

        if (textureId != OpenGlUtils.NO_TEXTURE) {
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
            GLES20.glUniform1i(GLES20.glGetUniformLocation(mProgramMosaicId, "inputImageTexture"), 0);
        }
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 6);
        GLES20.glDisableVertexAttribArray(a_position);
        GLES20.glDisableVertexAttribArray(a_texture);

        //draw Video to FrameBuffer.
        GLES20.glUseProgram(mProgramId);
        a_position = GLES20.glGetAttribLocation(mProgramId,"a_position");
        a_texture = GLES20.glGetAttribLocation(mProgramId,"a_texture");
        mVertexBuffer.position(0);
        mFragmentBuffer.position(0);
        GLES20.glEnableVertexAttribArray(a_position);
        GLES20.glEnableVertexAttribArray(a_texture);
        GLES20.glVertexAttribPointer(a_position, 2, GLES20.GL_FLOAT, false, 0, mVertexBuffer);
        GLES20.glVertexAttribPointer(a_texture, 2, GLES20.GL_FLOAT, false, 0, mFragmentBuffer);
        if (textureId != OpenGlUtils.NO_TEXTURE) {
            GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
            GLES20.glUniform1i(GLES20.glGetUniformLocation(mProgramId, "inputImageTexture"), 1);
        }
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 6);

        GLES20.glDisableVertexAttribArray(a_position);
        GLES20.glDisableVertexAttribArray(a_texture);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);

        GLES20.glUseProgram(0);
    }

    private float[] calVertexCoordsByVideoSize(float width,float height){

        float ratio = width/height;

        float left = -1,top = 1,right = 1,bottom = -1;

        if (ratio > 1){
            bottom = -1/ratio;
            top = 1/ratio;
        }else {
            left = -ratio;
            right = ratio;
        }

        Log.e(TAG,"calVertexCoordsByVideoSize ratio="+ratio+", left="+left+", top="+top+", right="+right+", bottom="+bottom);
        return new float[] { left,bottom,left,top,right,bottom,left,top,right,top,right,bottom } ;
    }

    public static final String VERTEX_SHADER = "" +
            "attribute vec4 a_position;\n" +
            "attribute vec2 a_texture;\n" +
            "varying highp vec2 textureCoordinate;\n" +

            "void main()\n" +
            "{\n" +
                "gl_Position = a_position;\n" +
                "textureCoordinate = a_texture;\n" +
            "}\n";

    public static final String FRAGMENT_SHADER = "" +
            "varying highp vec2 textureCoordinate;\n" +
            " \n" +
            "uniform sampler2D inputImageTexture;\n" +
            " \n" +
            "void main()\n" +
            "{\n" +
            "     gl_FragColor = texture2D(inputImageTexture, textureCoordinate);\n" +
            "}";

    public static final String SIMPLE_MOSAIC_FRAGMENT_SHADER = "" +
            "precision mediump float;\n" +
            "\n" +
            "varying vec2 textureCoordinate;\n" +
            "uniform sampler2D inputImageTexture;\n" +
            "const vec2 TexSize = vec2(200.0, 200.0);\n" +
            "const vec2 mosaicSize = vec2(5.0, 5.0);\n" +
            "\n" +
            "void main()\n" +
            "{\n" +
            "    vec2 intXY = vec2(textureCoordinate.x*TexSize.x, textureCoordinate.y*TexSize.y);\n" +
            "    vec2 XYMosaic = vec2(floor(intXY.x/mosaicSize.x)*mosaicSize.x, floor(intXY.y/mosaicSize.y)*mosaicSize.y);\n" +
            "    vec2 UVMosaic = vec2(XYMosaic.x/TexSize.x, XYMosaic.y/TexSize.y);\n" +
            "    vec4 color = texture2D(inputImageTexture, UVMosaic);\n" +
            "    gl_FragColor = color;\n" +
            "}";


}
