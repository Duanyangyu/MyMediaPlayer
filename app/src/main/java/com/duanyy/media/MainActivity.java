package com.duanyy.media;

import android.content.res.AssetManager;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.duanyy.media.decoder.VideoDecoder;
import com.duanyy.media.utils.FileUtils;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String filePath = Environment.getExternalStorageDirectory()+File.separator+"preview.mp4";
        File videoFile = new File(filePath);
        if (videoFile.exists() && videoFile.length() > 0){
            VideoDecoder decoder = new VideoDecoder();
            decoder.setDataSource(videoFile.getAbsolutePath());
        }else {
            copyToExternal();
        }
    }

    private void copyToExternal(){
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())){
            File externalFile = Environment.getExternalStorageDirectory();
            Log.e(TAG,externalFile.getAbsolutePath());
            FileUtils.putAssetsToSDCard(getApplicationContext(),"video/preview.mp4",externalFile.getAbsolutePath());
        }
    }
}
