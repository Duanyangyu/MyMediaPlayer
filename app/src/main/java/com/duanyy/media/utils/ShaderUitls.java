package com.duanyy.media.utils;

/**
 * Created by duanyy on 2018/3/22.
 */

public class ShaderUitls {

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

    public static final String FRAGMENT_SHADER = "" +
            "varying highp vec2 textureCoordinate;\n" +
            " \n" +
            "uniform sampler2D inputImageTexture;\n" +
            " \n" +
            "void main()\n" +
            "{\n" +
            "     gl_FragColor = texture2D(inputImageTexture, textureCoordinate);\n" +
            "}";

}
