package com.duanyy.media.filter;

import android.opengl.GLES20;
import android.opengl.Matrix;

import com.duanyy.media.glutil.BufferUtils;
import com.duanyy.media.glutil.FboHelper;
import com.duanyy.media.glutil.OpenGlUtils;

/**
 * Created by duanyy on 2018/3/21.
 */

public class VideoMosaicFilter extends BaseFilter{


    @Override
    public void initCoordinates() {

    }

    @Override
    public void initProgram() {
        mProgramId = OpenGlUtils.loadProgram(VERTEX_SHADER, SIMPLE_MOSAIC_FRAGMENT_SHADER);
        float[] vertex = { -1f,-1f,  -1f,1f,  1f,-1f,  -1f,1f,  1f,1f,  1f,-1f };
        float[] fragment = { 0f,0f,  0f,1f,  1f,0f,  0f,1f,  1f,1f,  1f,0f };
        mVertexBuffer = BufferUtils.float2Buffer(vertex);
        mFragmentBuffer = BufferUtils.float2Buffer(fragment);

        Matrix.setIdentityM(mMVPMatrix,0);
    }

    @Override
    public void initFbo(int width, int height) {
        mFbo = new FboHelper(width, height);
        mFbo.createFbo();
    }

    @Override
    protected void release() {
        if (mFbo != null) {
            mFbo.close();
            mFbo = null;
        }
    }

    @Override
    public void drawFrame(int textureId) {
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFbo.frameId());
        GLES20.glUseProgram(mProgramId);

        int a_position = GLES20.glGetAttribLocation(mProgramId,"a_position");
        int a_texture = GLES20.glGetAttribLocation(mProgramId,"a_texture");
        int u_mvpMatrix = GLES20.glGetUniformLocation(mProgramId,"u_MVPMatrix");
        GLES20.glEnableVertexAttribArray(a_position);
        GLES20.glEnableVertexAttribArray(a_texture);
        GLES20.glVertexAttribPointer(a_position, 2, GLES20.GL_FLOAT, false, 0, mVertexBuffer);
        GLES20.glVertexAttribPointer(a_texture, 2, GLES20.GL_FLOAT, false, 0, mFragmentBuffer);
        GLES20.glUniformMatrix4fv(u_mvpMatrix,1,false,mMVPMatrix,0);

        if (textureId != OpenGlUtils.NO_TEXTURE) {
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
            GLES20.glUniform1i(GLES20.glGetUniformLocation(mProgramId, "inputImageTexture"), 0);
        }

        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 6);
        GLES20.glDisableVertexAttribArray(a_position);
        GLES20.glDisableVertexAttribArray(a_texture);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);

        GLES20.glUseProgram(0);
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
