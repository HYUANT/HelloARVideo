//================================================================================================================================
//
//  Copyright (c) 2015-2018 VisionStar Information Technology (Shanghai) Co., Ltd. All Rights Reserved.
//  EasyAR is the registered trademark or trademark of VisionStar Information Technology (Shanghai) Co., Ltd in China
//  and other countries for the augmented reality technology developed by VisionStar Information Technology (Shanghai) Co., Ltd.
//
//================================================================================================================================

package cn.easyar.samples.helloarvideo;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.Toast;
import android.widget.VideoView;

import java.io.File;
import java.io.FileDescriptor;
import java.util.HashMap;

import cn.easyar.Engine;


public class MainActivity extends AppCompatActivity
{
    /*
    * Steps to create the key for this sample:
    *  1. login www.easyar.com
    *  2. create app with
    *      Name: HelloARVideo
    *      Package Name: cn.easyar.samples.helloarvideo
    *  3. find the created item in the list and show key
    *  4. set key string bellow
    */
    private final static String TAG = "MainActivity";
    private static String key = "x0s8fBBGwb4krQGpU4zlLfeBAW8LQkVl2jWnhPn4WpCd1tDstrAw25KFid2Q41GbeMHvSGcxvJVAMsd6detcpIbrvaKrtJsdZKP76l43HSQ53P9MbjBnHj7HG6huGGxeQJ3J2zdZQXmXCP9k8qw91ANnTnx1apEwCSlNBfThnEODRTMp0ce7FiCE2DtoK0q6iolKNope";
    private GLView glView;

/*
    private SurfaceView videoSFView;
    private SurfaceHolder surfaceHolder;
    private MediaPlayer mediaPlayer;
*/

    private VideoView videoView;

    private FrameLayout frameLayout;

    public static Handler handler;
    public static String videoPath;


    private boolean hasActiveHolder = false;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        if (!Engine.initialize(this, key)) {
            Log.e("HelloAR", "Initialization Failed.");
        }

        videoView = (VideoView)findViewById(R.id.videoView);


/*
        videoSFView = (SurfaceView)findViewById(R.id.videoSFView);
        surfaceHolder = videoSFView.getHolder();
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        surfaceHolder.addCallback(callback);
        // 设置Surface不维护自己的缓冲区，而是等待屏幕的渲染引擎将内容推送到界面
        // videoSFView.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
*/

        frameLayout = (FrameLayout)findViewById(R.id.preview);
        glView = new GLView(this);

        requestCameraPermission(new PermissionCallback() {
            @Override
            public void onSuccess() {
                // ((ViewGroup) findViewById(R.id.preview)).addView(glView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                frameLayout.addView(glView);
            }

            @Override
            public void onFailure() {
            }
        });

        handler = new Handler(){
            @Override
            public void handleMessage(Message msg){
                super.handleMessage(msg);
                if (msg.what == 1) {
                    // 显示videoSFView, play video
                    frameLayout.setVisibility(View.INVISIBLE);
                    // videoSFView.setVisibility(View.VISIBLE);
                    videoView.setVisibility(View.VISIBLE);
                    Log.i(TAG, "play video");
                    playVideo(MainActivity.videoPath);
                } else if (msg.what == 0) {
                    // 显示glView, stop video
                    // videoSFView.setVisibility(View.INVISIBLE);
                    videoView.setVisibility(View.INVISIBLE);
                    frameLayout.setVisibility(View.VISIBLE);
                    Log.i(TAG, "stop video");
                    stopVideo();
                }
            }
        };

    }

    // 添加一个Callback对象监听SurfaceView的变化
    private SurfaceHolder.Callback callback = new SurfaceHolder.Callback() {
        // SurfaceHolder被修改的时候回调
        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            // mediaPlayer.stop();
            Log.i(TAG, "surfaceDestroyed");
        }

        //SurfaceView创建时触发
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            /*
            Log.i(TAG, "surfaceCreated");
            synchronized (this) {
                hasActiveHolder = true;
                this.notifyAll();
            }
            */
            Log.i(TAG, "surfaceCreated");
        }

        //SurfaceView改变时触发
        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width,
                                   int height) {
            /*
            Log.i(TAG, "surfaceChanged");
            synchronized (this) {
                hasActiveHolder = true;
                this.notifyAll();
            }
            */
            Log.i(TAG, "surfaceChanged");
        }
    };

    protected void stopVideo() {

/*        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }*/

        videoView.pause();

    }

    protected void playVideo(String videoPath) {

        try {
            Log.i(TAG, videoPath);
            AssetFileDescriptor fd = getAssets().openFd(videoPath.trim());

            Uri videoUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.video);
            // Uri videoUri = Uri.parse("file:///android_asset/" + videoPath);
            videoView.setVideoURI(videoUri);
            videoView.start();

/*            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setDataSource(fd.getFileDescriptor(), fd.getStartOffset(), fd.getLength());

            mediaPlayer.prepareAsync();

            //等待surfaceHolder初始化完成才能执行mPlayer.setDisplay(surfaceHolder)
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    // 把视频画面输出到SurfaceView
                    Log.i(TAG, "mediaPlayer.start()");
                    Log.i(TAG, "mp.getVideoHeight() " + mp.getVideoHeight());
                    mediaPlayer.setDisplay(surfaceHolder);
                    mediaPlayer.start();
                }
            });

            //视频播放完成后的操作
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    if(mediaPlayer!=null)
                        mediaPlayer.release();//重置mediaplayer等待下一次播放
                }
            });*/

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private interface PermissionCallback
    {
        void onSuccess();
        void onFailure();
    }
    private HashMap<Integer, PermissionCallback> permissionCallbacks = new HashMap<Integer, PermissionCallback>();
    private int permissionRequestCodeSerial = 0;
    @TargetApi(23)
    private void requestCameraPermission(PermissionCallback callback)
    {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                int requestCode = permissionRequestCodeSerial;
                permissionRequestCodeSerial += 1;
                permissionCallbacks.put(requestCode, callback);
                requestPermissions(new String[]{Manifest.permission.CAMERA}, requestCode);
            } else {
                callback.onSuccess();
            }
        } else {
            callback.onSuccess();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        if (permissionCallbacks.containsKey(requestCode)) {
            PermissionCallback callback = permissionCallbacks.get(requestCode);
            permissionCallbacks.remove(requestCode);
            boolean executed = false;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    executed = true;
                    callback.onFailure();
                }
            }
            if (!executed) {
                callback.onSuccess();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        if (glView != null) { glView.onResume(); }
    }

    @Override
    protected void onPause()
    {
        if (glView != null) { glView.onPause(); }
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
