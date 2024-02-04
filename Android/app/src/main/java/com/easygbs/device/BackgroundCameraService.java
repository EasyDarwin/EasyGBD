package com.easygbs.device;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.SurfaceTexture;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.view.Gravity;
import android.view.TextureView;
import android.view.WindowManager;

import com.easygbs.device.push.MediaStream;

public class BackgroundCameraService extends Service implements TextureView.SurfaceTextureListener {
    private static final int NOTIFICATION_ID = 1;

    /**
     * 表示后台是否正在渲染
     */
    private TextureView mOutComeVideoView;
    private WindowManager mWindowManager;
    private MediaStream mMediaStream;

    // Binder given to clients
    private final IBinder mBinder = new LocalBinder();
    private SurfaceTexture mTexture;
    private boolean mPenddingStartPreview;

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        mTexture = surface;

        if (mPenddingStartPreview) {
            mMediaStream.setSurfaceTexture(mTexture);
            mMediaStream.startPreview();
            backGroundNotificate();
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        mTexture = null;
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

    public void activePreview() {
        mMediaStream.stopPreview();
        mPenddingStartPreview = true;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Settings.canDrawOverlays(this)) {
                WindowManager.LayoutParams param = new WindowManager.LayoutParams();
                param.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    param.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
                }

                param.format = PixelFormat.TRANSLUCENT;
                param.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
                param.alpha = 1.0f;
                param.gravity = Gravity.LEFT | Gravity.TOP;
                param.width = 1;
                param.height = 1;

                mWindowManager.addView(mOutComeVideoView, param);
            }
        } else {
            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(1, 1,
                    WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
                    WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                    PixelFormat.TRANSLUCENT);
            layoutParams.gravity = Gravity.LEFT | Gravity.TOP;
            mWindowManager.addView(mOutComeVideoView, layoutParams);
        }
    }

    private void backGroundNotificate() {
        Intent notificationIntent = new Intent(this, StreamActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        Notification notification = new NotificationCompat.Builder(this, EasyApplication.CHANNEL_CAMERA)
                        .setContentTitle(getString(R.string.app_name))
                        .setContentText(getString(R.string.video_uploading_in_background))
                        .setSmallIcon(R.drawable.ic_stat_camera)
                        .setContentIntent(pendingIntent)
                        .build();

        startForeground(NOTIFICATION_ID, notification);
    }

    public void inActivePreview() {
        if (mOutComeVideoView != null) {
            if (mOutComeVideoView.getParent() != null) {
                mWindowManager.removeView(mOutComeVideoView);
            }
        }

        stopForeground(true);
    }

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        public BackgroundCameraService getService() {
            return BackgroundCameraService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // Create new SurfaceView, set its size to 1x1, move it to the top left
        // corner and set this service as a callback
        mWindowManager = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);

        mOutComeVideoView = new TextureView(this);
        mOutComeVideoView.setSurfaceTextureListener(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            return START_NOT_STICKY;
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        stopForeground(true);

        if (mOutComeVideoView != null) {
            if (mOutComeVideoView.getParent() != null) {
                mWindowManager.removeView(mOutComeVideoView);
            }
        }

        super.onDestroy();
    }

    public MediaStream getMediaStream() {
        return mMediaStream;
    }

    public void setMediaStream(MediaStream ms) {
        mMediaStream = ms;
    }
}
