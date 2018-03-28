package com.duanyy.media;

import android.content.Intent;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.duanyy.media.decoder.VideoDecoder;
import com.duanyy.media.maker.VideoMaker;
import com.duanyy.media.utils.FileUtils;

import java.io.File;
import java.util.concurrent.ExecutorService;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "MainActivity";
    private String filePath;
    private VideoMaker mVideoMaker;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        filePath = Environment.getExternalStorageDirectory()+ File.separator+"preview.mp4";
        handleVideoFile(filePath);


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mVideoMaker != null) {
            mVideoMaker.release();
        }
    }

    public void btnDecode(View view){
        Intent intent = new Intent(this,VideoActivity.class);
        startActivity(intent);
    }

    public void btnGenerate(View view){
        String path = Environment.getExternalStorageDirectory()+"/generateMovie.mp4";
        mVideoMaker = new VideoMaker();
        mVideoMaker.generateMoview(path);
    }

    private void handleVideoFile(String targetPath){
        File videoFile = new File(targetPath);
        if (videoFile.exists() && videoFile.length() > 0){

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
