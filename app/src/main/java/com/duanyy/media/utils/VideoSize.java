package com.duanyy.media.utils;

/**
 * Created by duanyy on 2018/3/21.
 */

public class VideoSize {

    private float mWidth;
    private float mHeight;

    public VideoSize() {
    }

    public VideoSize(float width, float height) {
        this.mWidth = width;
        this.mHeight = height;
    }

    public float getWidth() {
        return mWidth;
    }

    public void setWidth(float mWidth) {
        this.mWidth = mWidth;
    }

    public float getHeight() {
        return mHeight;
    }

    public void setHeight(float mHeight) {
        this.mHeight = mHeight;
    }
}
