package com.duanyy.media.glutil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

/**
 * Created by duanyy on 2017/7/16.
 */

public class BufferUtils {

    public static FloatBuffer float2Buffer(float[] array){
        if (array == null)
            return null;
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(array.length*4);
        byteBuffer.order(ByteOrder.nativeOrder());
        FloatBuffer floatBuffer = byteBuffer.asFloatBuffer();
        floatBuffer.put(array);
        floatBuffer.position(0);
        return floatBuffer;
    }

    public static IntBuffer int2Buffer(int[] array){
        if (array != null) {
            return null;
        }
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(array.length*4);
        byteBuffer.order(ByteOrder.nativeOrder());
        IntBuffer intBuffer = byteBuffer.asIntBuffer();
        intBuffer.put(array);
        intBuffer.position(0);
        return intBuffer;
    }

    public static ShortBuffer short2Buffer(short[] array){
        if (array != null) {
            return null;
        }
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(array.length*2);
        byteBuffer.order(ByteOrder.nativeOrder());
        ShortBuffer shortBuffer = byteBuffer.asShortBuffer();
        shortBuffer.put(array);
        shortBuffer.position(0);
        return shortBuffer;
    }

    //int和short类型和二进制byte[]相互转换。
    public static byte[] intToByteArray(int data) {
        return ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(data).array();
    }

    public static byte[] shortToByteArray(short data) {
        return ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort(data).array();
    }

    public static short byteArrayToShort(byte[] b) {
        return ByteBuffer.wrap(b).order(ByteOrder.LITTLE_ENDIAN).getShort();
    }

    public static int byteArrayToInt(byte[] b) {
        return ByteBuffer.wrap(b).order(ByteOrder.LITTLE_ENDIAN).getInt();
    }

}
