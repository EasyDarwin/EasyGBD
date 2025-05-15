package com.easygbs.easygbd.service;

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
import android.view.Gravity;
import android.view.TextureView;
import android.view.WindowManager;

import com.easygbs.easygbd.application.App;

import androidx.core.app.NotificationCompat;

import com.easygbs.easygbd.R;
import com.easygbs.easygbd.push.*;
import com.easygbs.easygbd.activity.*;

public class BackgroundCameraService extends Service implements TextureView.SurfaceTextureListener {
    private static final int NOTIFICATION_ID = 1;

    /**
     * 表示后台是否正在渲染
     */
    private TextureView mOutComeVideoView;
    private WindowManager mWindowManager;
    private MediaStream mMediaStream;

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

    private void backGroundNotificate() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        Notification notification = new NotificationCompat.Builder(this, App.CHANNEL_CAMERA)
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