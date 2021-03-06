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
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.WindowManager;
import android.util.Log;
import android.widget.FrameLayout;

import java.util.HashMap;

import cn.easyar.Engine;


public class MainActivity extends AppCompatActivity {
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

    private TextureView videoTTView;
    private MediaPlayer mediaPlayer;
    private Surface surf = null;

    private FrameLayout frameLayout;
    private SurfaceTexture mSurfaceTexture;

    public static Handler handler;
    public static String videoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        if (!Engine.initialize(this, key)) {
            Log.e("HelloAR", "Initialization Failed.");
        }

        videoTTView = (TextureView) findViewById(R.id.videoTTView);
        videoTTView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {
                Log.i(TAG, "onSurfaceTextureAvailable");
                mSurfaceTexture = surfaceTexture;
                playVideo(MainActivity.videoPath, surfaceTexture);
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {
            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
                Log.i(TAG, "onSurfaceTextureDestroyed");
                mSurfaceTexture = null;
                stopVideo();
                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
            }
        });

        frameLayout = (FrameLayout) findViewById(R.id.preview);
        glView = new GLView(this);

        requestCameraPermission(new PermissionCallback() {
            @Override
            public void onSuccess() {
                frameLayout.addView(glView);
            }

            @Override
            public void onFailure() {
            }
        });

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.what == 1) {
                    frameLayout.setVisibility(View.INVISIBLE);
                    videoTTView.setVisibility(View.VISIBLE);
                    Log.i(TAG, "play video");
                    if (mSurfaceTexture != null)
                        playVideo(MainActivity.videoPath, mSurfaceTexture);
                } else if (msg.what == 0) {
                    videoTTView.setVisibility(View.INVISIBLE);
                    frameLayout.setVisibility(View.VISIBLE);
                    Log.i(TAG, "stop video");
                    stopVideo();
                }
            }
        };

    }

    private void adjustAspectRatio(int videoWidth, int videoHeight) {
        int viewWidth = videoTTView.getWidth();
        int viewHeight = videoTTView.getHeight();

        Log.i(TAG, "viewHeight=" + viewHeight + " viewWidth=" + viewWidth + " videoHeight=" + videoHeight + " videoWidth=" + videoWidth);

        if (videoHeight < videoWidth) {
            Log.i(TAG, "videoHeight < videoWidth");
            // 视频高度 < 宽度，而全屏情况下videoTTView高度 > 宽度，因此需要交换视频高度、宽度并旋转TextureView
            videoHeight = viewHeight + videoWidth;
            videoWidth = videoHeight - videoWidth;
            videoHeight = videoHeight - videoWidth;
            videoTTView.setRotation(90);

            double aspectRatio = (double) videoHeight / videoWidth;
            int newWidth, newHeight;
            if (viewHeight > (int) (viewWidth * aspectRatio)) {
                // limited by narrow width; restrict height
                newWidth = viewWidth;
                newHeight = (int) (viewWidth * aspectRatio);
            } else {
                // limited by short height; restrict width
                newWidth = (int) (viewHeight / aspectRatio);
                newHeight = viewHeight;
            }
            int xOff = (viewWidth - newWidth) / 2;
            int yOff = (viewHeight - newHeight) / 2;

            Log.v(TAG, "video=" + videoWidth + "x" + videoHeight + " view=" + viewWidth + "x" + viewHeight
                    + " newView=" + newWidth + "x" + newHeight + " off=" + xOff + "," + yOff);

            Matrix txForm = new Matrix();
            videoTTView.getTransform(txForm);
            txForm.setScale((float) newWidth / viewWidth, (float) newHeight / viewHeight);
            txForm.postTranslate(xOff, yOff);
            videoTTView.setTransform(txForm);

            // 放大videoTTView至全屏
            videoTTView.setScaleX((float) viewHeight / newWidth);
            videoTTView.setScaleY((float) viewWidth / newHeight);
        } else {
            Log.i(TAG, "videoHeight >= videoWidth");
            videoTTView.setRotation(0);
            videoTTView.setScaleX(1f);
            videoTTView.setScaleY(1f);
            videoTTView.setTransform(null);
        }


    }

    protected void stopVideo() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying())
                mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    protected void playVideo(String videoPath, SurfaceTexture surfaceTexture) {
        try {
            AssetFileDescriptor fd = getAssets().openFd(videoPath);
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setDataSource(fd.getFileDescriptor(), fd.getStartOffset(), fd.getLength());
            mediaPlayer.setLooping(true);
            if (surf == null) {
                surf = new Surface(surfaceTexture);
            }
            mediaPlayer.setSurface(surf);
            mediaPlayer.prepareAsync();

            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    Log.i(TAG, "setOnPreparedListener");
                    adjustAspectRatio(mediaPlayer.getVideoWidth(), mediaPlayer.getVideoHeight());
                    mediaPlayer.start();
                }
            });

            setVolumeControlStream(AudioManager.STREAM_MUSIC);
            fd.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private interface PermissionCallback {
        void onSuccess();

        void onFailure();
    }

    private HashMap<Integer, PermissionCallback> permissionCallbacks = new HashMap<Integer, PermissionCallback>();
    private int permissionRequestCodeSerial = 0;

    @TargetApi(23)
    private void requestCameraPermission(PermissionCallback callback) {
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
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
    protected void onResume() {
        super.onResume();
        if (glView != null) {
            glView.onResume();
        }
    }

    @Override
    protected void onPause() {
        if (glView != null) {
            glView.onPause();
        }
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
