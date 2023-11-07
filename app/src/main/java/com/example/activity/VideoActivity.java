package com.example.activity;

import android.app.PictureInPictureParams;
import android.os.Bundle;
import android.os.Environment;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.R;

/**
 * 画中画demo界面
 *
 * @author laiweisheng
 * @date 2023/11/7
 */
public class VideoActivity extends AppCompatActivity {

    private VideoView videoView;
    private boolean starting = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);

        videoView = findViewById(R.id.videoView);
        TextView pipTv = findViewById(R.id.pip_tv);

        // 设置视频资源
        videoView.setVideoPath(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/Screenshots/SVID_20231107_105930_1.mp4");
        videoView.setOnClickListener(v -> {
            if (starting) {
                videoView.pause();
            } else {
                videoView.start();
            }
            starting = !starting;
        });

        // 设置画中画按钮点击事件
        pipTv.setOnClickListener(v -> {
            enterPiPMode();
        });
    }

    @Override
    protected void onResume() {
        videoView.start();
        starting = true;
        super.onResume();
    }

    /**
     * 进入画中画模式
     */
    private void enterPiPMode() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            PictureInPictureParams params = new PictureInPictureParams.Builder().build();
            enterPictureInPictureMode(params);
        }
    }
}

