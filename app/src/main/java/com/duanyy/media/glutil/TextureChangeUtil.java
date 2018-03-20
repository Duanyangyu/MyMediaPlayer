/*
 * Copyright (C) 2012 CyberAgent
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.duanyy.media.glutil;

public class TextureChangeUtil {

	public static final float VERTEX_NO_ROTATION[] = {
			-1.0f, -1.0f,
			1.0f, -1.0f,
			-1.0f,  1.0f,
			1.0f,  1.0f,
	};

	public static final float VERTEX_ROTATION_180[] = {
			-1.0f, 1.0f,
			1.0f, 1.0f,
			-1.0f,  -1.0f,
			1.0f,  -1.0f,
	};

	public static final float VERTEX_ROTATION_180_Mirror[] = {
			1.0f, 1.0f,
			-1.0f, 1.0f,
			1.0f,  -1.0f,
			-1.0f,  -1.0f,
	};
    public static final float TEXTURE_NO_ROTATION[] = {
            0.0f, 1.0f,
            1.0f, 1.0f,
            0.0f, 0.0f,
            1.0f, 0.0f,
    };

    public static final float TEXTURE_ROTATED_90[] = {
            1.0f, 1.0f,
            1.0f, 0.0f,
            0.0f, 1.0f,
            0.0f, 0.0f,
    };
	public static final float TEXTURE_ROTATED_90_MIRROR[] = {
			1.0f, 0.0f,
			1.0f, 1.0f,
			0.0f, 0.0f,
			0.0f, 1.0f,
	};

    public static final float TEXTURE_ROTATED_180[] = {
            1.0f, 0.0f,
            0.0f, 0.0f,
            1.0f, 1.0f,
            0.0f, 1.0f,
    };
    
    public static final float TEXTURE_ROTATED_180_MIRROR[] = {
        0.0f, 0.0f,
        1.0f, 0.0f,
        0.0f, 1.0f,
        1.0f, 1.0f,
    };
    
    public static final float TEXTURE_ROTATED_270[] = {
            0.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 0.0f,
            1.0f, 1.0f,
    };

    public static final float TEXTURE_ROTATED_270_MIRROR[] = {
            0.0f, 1.0f,
            0.0f, 0.0f,
            1.0f, 1.0f,
            1.0f, 0.0f,
    };

    private TextureChangeUtil() {
    }

    public static float[] getRotation(final Rotation rotation, final boolean flipHorizontal,
                                                         final boolean flipVertical) {
        float[] rotatedTex;
        switch (rotation) {
            case ROTATION_90:
                rotatedTex = TEXTURE_ROTATED_90;
                break;
            case ROTATION_180:
                rotatedTex = TEXTURE_ROTATED_180;
                break;
            case ROTATION_270:
                rotatedTex = TEXTURE_ROTATED_270;
                break;
            case NORMAL:
            default:
                rotatedTex = TEXTURE_NO_ROTATION;
                break;
        }
        if (flipHorizontal) {
            rotatedTex = new float[]{
                    flip(rotatedTex[0]), rotatedTex[1],
                    flip(rotatedTex[2]), rotatedTex[3],
                    flip(rotatedTex[4]), rotatedTex[5],
                    flip(rotatedTex[6]), rotatedTex[7],
            };
        }
        if (flipVertical) {
            rotatedTex = new float[]{
                    rotatedTex[0], flip(rotatedTex[1]),
                    rotatedTex[2], flip(rotatedTex[3]),
                    rotatedTex[4], flip(rotatedTex[5]),
                    rotatedTex[6], flip(rotatedTex[7]),
            };
        }
        return rotatedTex;
    }

	public static float[] getRotation(final boolean flipHorizontal, final boolean flipVertical) {
		float[] rotatedTex = TEXTURE_NO_ROTATION;
		if (flipHorizontal) {
			rotatedTex = new float[]{
					flip(rotatedTex[0]), rotatedTex[1],
					flip(rotatedTex[2]), rotatedTex[3],
					flip(rotatedTex[4]), rotatedTex[5],
					flip(rotatedTex[6]), rotatedTex[7],
			};
		}
		if (flipVertical) {
			rotatedTex = new float[]{
					rotatedTex[0], flip(rotatedTex[1]),
					rotatedTex[2], flip(rotatedTex[3]),
					rotatedTex[4], flip(rotatedTex[5]),
					rotatedTex[6], flip(rotatedTex[7]),
			};
		}
		return rotatedTex;
	}
    /**
     * 对origBuffer进行旋转
     * @param origBuffer
     * @param rotation
     * @return
     */
    public static float[] getRotationBuffer(float[] origBuffer, final Rotation rotation) {
    	float[] rotatedTex = origBuffer;
    	switch (rotation) {
		case ROTATION_90:
			rotatedTex = new float[]{
				origBuffer[2], origBuffer[3],
				origBuffer[6], origBuffer[7],
				origBuffer[0], origBuffer[1],
				origBuffer[4], origBuffer[5],
			};
			break;
		case ROTATION_180:
			rotatedTex = new float[]{
					origBuffer[6], origBuffer[7],
					origBuffer[4], origBuffer[5],
					origBuffer[2], origBuffer[3],
					origBuffer[0], origBuffer[1],
				};
			break;
		case ROTATION_270:
			rotatedTex = new float[]{
					origBuffer[4], origBuffer[5],
					origBuffer[0], origBuffer[1],
					origBuffer[6], origBuffer[7],
					origBuffer[2], origBuffer[3],
				};
			break;
		case NORMAL:
		default:
			break;
		}
    	return rotatedTex;
    }
    
    /**
     * 
     * @param origBuffer
     * @param rotation 当心了，origBuffer可能是被旋转过的
     * @param cutHorizontal
     * @param cutVertical
     * @param cutRatio
     * @return
     */
    public static float[] getCutBuffer(float[] origBuffer, final Rotation rotation, final boolean cutHorizontal, final boolean cutVertical, float cutRatio) {
    	if (origBuffer == null) {
			return null;
		}
    	float[] cutTex = origBuffer;
    	boolean localCutHorizontal = cutHorizontal;
    	boolean localCutVertical = cutVertical;
    	if (rotation == Rotation.ROTATION_90 || rotation == Rotation.ROTATION_270) {
			localCutHorizontal = cutVertical;
			localCutVertical = cutHorizontal;
		}
    	if (localCutHorizontal) {
			cutTex = new float[]{
					cut(origBuffer[0], cutRatio), origBuffer[1],
					cut(origBuffer[2], cutRatio), origBuffer[3],
					cut(origBuffer[4], cutRatio), origBuffer[5],
					cut(origBuffer[6], cutRatio), origBuffer[7],
			};
		}
    	if (localCutVertical) {
			cutTex = new float[]{
					origBuffer[0], cut(origBuffer[1], cutRatio), 
					origBuffer[2], cut(origBuffer[3], cutRatio), 
					origBuffer[4], cut(origBuffer[5], cutRatio), 
					origBuffer[6], cut(origBuffer[7], cutRatio), 
			};
		}
    	return cutTex;
    }
    
    
    private static float flip(final float i) {
        if (i == 0.0f) {
            return 1.0f;
        }
        return 0.0f;
    }
    
    private static float cut(final float i, final float cutRatio) {
    	if (i == 0.0f) {
			return cutRatio;
		}
    	return 1.0f-cutRatio;
    }
}
