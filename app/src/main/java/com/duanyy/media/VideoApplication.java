package com.duanyy.media;

import android.app.Application;
import android.content.Context;

/**
 * Created by duanyy on 2018/3/20.
 */

public class VideoApplication extends Application {

    private static Context mContext;

    public static Context getContext(){
        return mContext;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mContext = getApplicationContext();
    }
}
