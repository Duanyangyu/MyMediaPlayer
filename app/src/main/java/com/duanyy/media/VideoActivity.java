package com.duanyy.media;

import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.duanyy.media.player.VideoPlayerView;

import java.io.File;

public class VideoActivity extends AppCompatActivity {

    private VideoPlayerView mVideoPlaerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);

        mVideoPlaerView = (VideoPlayerView) findViewById(R.id.mVideoPlaerView);
        String videoPath = Environment.getExternalStorageDirectory()+ File.separator+"preview.mp4";
        mVideoPlaerView.setDataSource(videoPath);
    }

    public void btnPlay(View view){
        if (mVideoPlaerView != null) {
            mVideoPlaerView.play();
        }
    }
}
