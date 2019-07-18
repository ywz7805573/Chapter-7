package com.bytedance.videoplayer;

import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;

import java.io.IOException;

import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class MediaPlayerActivity extends AppCompatActivity {
    private SurfaceView surfaceView;
    private MediaPlayer player;
    private SurfaceHolder holder;
    private SeekBar seekBar;
    private Boolean changing = false;
    private Boolean playing = true;
    private Timer timer;

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {

            setVideoParams(player,true);
             }
             else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            setVideoParams(player,false);
                                     }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_media_player);
        surfaceView = findViewById(R.id.surfaceView);
        player = new MediaPlayer();
        seekBar = findViewById(R.id.seekBar);
        try{
            player.setDataSource(getResources().openRawResourceFd(R.raw.bytedance));
            holder = surfaceView.getHolder();
            holder.addCallback(new PlayerCallBack());
            player.prepare();
            player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    player.start();
                    player.setLooping(true);
                    timer = new Timer();
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            if(!changing){
                                seekBar.setProgress(player.getCurrentPosition());
                            }
                        }
                    },0,1000);
                }
            });
            setVideoParams(player,false);
            player.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
                @Override
                public void onBufferingUpdate(MediaPlayer mp, int percent) {
                    System.out.println(percent);
                }
            });
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        findViewById(R.id.buttonFunction).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(playing == true){
                    player.pause();
                    playing = false;
                }
                else {
                    player.start();
                    playing = true;
                }
            }
        });
        final int duration = player.getDuration();
        seekBar.setMax(duration);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                double time = progress/1000;
                int min = (int) (time/60);
                int seconds = (int)(time) - min *60;
                TextView tv = findViewById(R.id.textView2);
                tv.setText(min+":"+seconds);
                if(fromUser){
                    player.seekTo(progress,MediaPlayer.SEEK_CLOSEST);
                }
            }

            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                changing = true;
            }
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                changing = false;
                player.seekTo(seekBar.getProgress(),MediaPlayer.SEEK_CLOSEST);
            }
        });
    }

    /**

     * 设置SurfaceView的参数

     *

     * @param mediaPlayer

     * @param isLand

     */

    public void setVideoParams(MediaPlayer mediaPlayer, boolean isLand) {

        //获取surfaceView父布局的参数
        ViewGroup.LayoutParams rl_paramters = findViewById(R.id.rl).getLayoutParams();
        //获取SurfaceView的参数
        ViewGroup.LayoutParams sv_paramters = findViewById(R.id.surfaceView).getLayoutParams();
        //设置宽高比为16/9
        float screen_widthPixels = getResources().getDisplayMetrics().widthPixels;
        float screen_heightPixels = getResources().getDisplayMetrics().widthPixels * 9f / 16f;
        //取消全屏
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        if (isLand) {
            screen_heightPixels = getResources().getDisplayMetrics().heightPixels;
            //设置全屏
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
        rl_paramters.width = (int) screen_widthPixels;
        rl_paramters.height = (int) screen_heightPixels;

        //获取MediaPlayer的宽高
        int videoWidth = mediaPlayer.getVideoWidth();
        int videoHeight = mediaPlayer.getVideoHeight();

        float video_por = videoWidth / videoHeight;
        float screen_por = screen_widthPixels / screen_heightPixels;
        //16:9    16:12
        if (screen_por > video_por) {
            sv_paramters.height = (int) screen_heightPixels;
            sv_paramters.width = (int) (screen_heightPixels * screen_por);
        } else {
            //16:9  19:9
            sv_paramters.width = (int) screen_widthPixels;
            sv_paramters.height = (int) (screen_widthPixels / screen_por);
        }
        findViewById(R.id.rl).setLayoutParams(rl_paramters);
        findViewById(R.id.surfaceView).setLayoutParams(sv_paramters);
    }

//    public void changeVideoSize(MediaPlayer mediaPlayer) {
//        int surfaceWidth = surfaceView.getWidth();
//        int surfaceHeight = surfaceView.getHeight();
//
//
//
//        int videoWidth = mediaPlayer.getVideoWidth();
//        int videoHeight = mediaPlayer.getVideoHeight();
//
//        //根据视频尺寸去计算->视频可以在sufaceView中放大的最大倍数。
//        float max;
//        if (getResources().getConfiguration().orientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
//            //竖屏模式下按视频宽度计算放大倍数值
//            max = Math.max((float) videoWidth / (float) surfaceWidth, (float) videoHeight / (float) surfaceHeight);
//            videoWidth = (int) Math.ceil((float) videoWidth / max);
//            videoHeight = (int) Math.ceil((float) videoHeight / max);
//            surfaceView.setLayoutParams(new RelativeLayout.LayoutParams(videoWidth, videoHeight));
//        //   findViewById(R.id.rl).setLayoutParams();
//        } else {
//            //横屏模式下按视频高度计算放大倍数值
//            max = Math.min(((float) videoWidth / (float) surfaceHeight), (float) videoHeight / (float) surfaceWidth);
//            //max = Math.max((float) videoWidth / (float) surfaceWidth, (float) videoHeight / (float) surfaceHeight);
//            videoWidth = (int) Math.ceil((float) videoWidth / max);
//            videoHeight = (int) Math.ceil((float) videoHeight / max);
//            surfaceView.setLayoutParams(new RelativeLayout.LayoutParams((int)(videoWidth*1.5), (int)(videoHeight*1.8)));
//
//        }
//
//
//
//        //无法直接设置视频尺寸，将计算出的视频尺寸设置到surfaceView 让视频自动填充。
//
//    }

    private class PlayerCallBack implements SurfaceHolder.Callback {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            player.setDisplay(holder);
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {

        }
    }
}
