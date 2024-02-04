/*
	Copyright (c) 2013-2016 EasyDarwin.ORG.  All rights reserved.
	Github: https://github.com/EasyDarwin
	WEChat: EasyDarwin
	Website: http://www.easydarwin.org
*/

package com.easygbs.device;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.easygbs.device.push.MediaStream;
import com.easygbs.device.push.PushCallback;
import com.easygbs.device.util.DataUtil;
import com.easygbs.device.util.SPUtil;
import com.squareup.otto.Subscribe;

import org.easydarwin.bus.StartRecord;
import org.easydarwin.bus.StopRecord;
import org.easydarwin.bus.StreamStat;
import org.easydarwin.bus.SupportResolution;

import org.easydarwin.util.BUSUtil;
import org.easydarwin.util.Util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;

/**
 * 预览+推流等主页
 */
public class StreamActivity extends AppCompatActivity implements View.OnClickListener, TextureView.SurfaceTextureListener {
    public static final int EXTERNAL_STORAGE_REQ_CODE = 10;
    public static final int REQUEST_CAMERA_PERMISSION = 1003;
    public static final int REQUEST_STORAGE_PERMISSION = 1004;

    private ImageView startPush;
    // 默认分辨率
    int width = 1280, height = 720;

    ImageView btnSwitchCemera;
    Spinner spnResolution;
    TextView txtStatus, streamStat;
    TextView textRecordTick;

    List<String> listResolution = new ArrayList<>();

    MediaStream mMediaStream;

    private BackgroundCameraService mService;
    private ServiceConnection conn;

    private boolean mNeedGrantedPermission;

    private static final String STATE = "state";
    private static final int MSG_STATE = 1;

    public static long mRecordingBegin;
    public static boolean mRecording;

    private long mExitTime;//声明一个long类型变量：用于存放上一点击“返回键”的时刻

    // 录像时的线程
    private Runnable mRecordTickRunnable = new Runnable() {
        @Override
        public void run() {
            long duration = System.currentTimeMillis() - mRecordingBegin;
            duration /= 1000;

            textRecordTick.setText(String.format("%02d:%02d", duration / 60, (duration) % 60));

            if (duration % 2 == 0) {
                textRecordTick.setCompoundDrawablesWithIntrinsicBounds(R.drawable.recording_marker_shape, 0, 0, 0);
            } else {
                textRecordTick.setCompoundDrawablesWithIntrinsicBounds(R.drawable.recording_marker_interval_shape, 0, 0, 0);
            }

            textRecordTick.removeCallbacks(this);
            textRecordTick.postDelayed(this, 1000);
        }
    };

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_STATE:
                    String state = msg.getData().getString("state");
                    txtStatus.setText(state);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 全屏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stream);
        startPush = findViewById(R.id.streaming_activity_push);

        BUSUtil.BUS.register(this);

        notifyAboutColorChange();

        // 动态获取camera和audio权限
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO}, REQUEST_CAMERA_PERMISSION);
            mNeedGrantedPermission = true;
            return;
        } else {
            // resume
        }

        int permission = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // 请求权限
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    EXTERNAL_STORAGE_REQ_CODE);
        }
    }

    @Override
    protected void onPause() {
        if (!mNeedGrantedPermission) {
            unbindService(conn);
            handler.removeCallbacksAndMessages(null);
        }

        boolean isStreaming = mMediaStream != null && mMediaStream.isStreaming();

        if (mMediaStream != null) {
            mMediaStream.stopPreview();

//            if (isStreaming && SPUtil.getEnableBackgroundCamera(this)) {
//                mService.activePreview();
//            } else {
                mMediaStream.stopStream();
                mMediaStream.release();
                mMediaStream = null;

                stopService(new Intent(this, BackgroundCameraService.class));
                stopService(new Intent(this, UVCCameraService.class));
//            }
        }

        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!mNeedGrantedPermission) {
            goonWithPermissionGranted();
        }
    }

    @Override
    protected void onDestroy() {
        BUSUtil.BUS.unregister(this);
        super.onDestroy();
    }

    /*
     * android6.0权限，onRequestPermissionsResult回调
     * */
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CAMERA_PERMISSION: {
                if (grantResults.length > 1
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    mNeedGrantedPermission = false;
                    goonWithPermissionGranted();
                } else {
                    finish();
                }

                break;
            }
        }
    }

    private void notifyAboutColorChange() {
        ImageView iv = findViewById(R.id.toolbar_about);

        if (EasyApplication.activeDays >= 9999) {
            iv.setImageResource(R.drawable.green);
        } else if (EasyApplication.activeDays > 0) {
            iv.setImageResource(R.drawable.yellow);
        } else {
            iv.setImageResource(R.drawable.red);
        }
    }

    private void goonWithPermissionGranted() {
        spnResolution = findViewById(R.id.spn_resolution);
        streamStat = findViewById(R.id.stream_stat);
        txtStatus = findViewById(R.id.txt_stream_status);
        btnSwitchCemera = findViewById(R.id.btn_switchCamera);
        textRecordTick = findViewById(R.id.tv_start_record);
        final TextureView surfaceView = findViewById(R.id.sv_surfaceview);

        streamStat.setText(null);
        btnSwitchCemera.setOnClickListener(this);
        surfaceView.setSurfaceTextureListener(this);
        surfaceView.setOnClickListener(this);
        startPush.setImageResource(R.drawable.start_push);

        // create background service for background use.
        Intent intent = new Intent(this, BackgroundCameraService.class);
        startService(intent);

        Intent intent1 = new Intent(this, UVCCameraService.class);
        startService(intent1);

        conn = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                mService = ((BackgroundCameraService.LocalBinder) iBinder).getService();

                if (surfaceView.isAvailable()) {
                    goonWithAvailableTexture(surfaceView.getSurfaceTexture());
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {

            }
        };

        bindService(new Intent(this, BackgroundCameraService.class), conn, 0);

        if (mRecording) {
            textRecordTick.setVisibility(View.VISIBLE);
            textRecordTick.removeCallbacks(mRecordTickRunnable);
            textRecordTick.post(mRecordTickRunnable);
        } else {
            textRecordTick.setVisibility(View.INVISIBLE);
            textRecordTick.removeCallbacks(mRecordTickRunnable);
        }
    }

    /*
     * 初始化MediaStream
     * */
    private void goonWithAvailableTexture(SurfaceTexture surface) {
        final File easyPusher = new File(DataUtil.recordPath());
        easyPusher.mkdir();

        MediaStream ms = mService.getMediaStream();

        if (ms != null) { // switch from background to front
            ms.stopPreview();
            mService.inActivePreview();
            ms.setSurfaceTexture(surface);
            ms.startPreview();

            mMediaStream = ms;

//            if (ms.isStreaming()) {
//                sendMessage("推流中");
//                startPush.setImageResource(R.drawable.start_push_pressed);
//            }

            if (ms.getDisplayRotationDegree() != getDisplayRotationDegree()) {
                int orientation = getRequestedOrientation();

                if (orientation == SCREEN_ORIENTATION_UNSPECIFIED || orientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                } else {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                }
            }
        } else {
            boolean enableVideo = SPUtil.getEnableVideo(this);

            ms = new MediaStream(getApplicationContext(), surface, enableVideo);
            ms.setRecordPath(easyPusher.getPath());
            mMediaStream = ms;

            startCamera();
            sendMessage("");

            mService.setMediaStream(ms);
        }
    }

    private void startCamera() {
        mMediaStream.updateResolution(width, height);
        mMediaStream.setDisplayRotationDegree(getDisplayRotationDegree());
        mMediaStream.createCamera();
        mMediaStream.startPreview();

//        if (mMediaStream.isStreaming()) {
//            sendMessage("推流中");
//        }
    }

    // 屏幕的角度
    private int getDisplayRotationDegree() {
        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;

        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break; // Natural orientation
            case Surface.ROTATION_90:
                degrees = 90;
                break; // Landscape left
            case Surface.ROTATION_180:
                degrees = 180;
                break;// Upside down
            case Surface.ROTATION_270:
                degrees = 270;
                break;// Landscape right
        }

        return degrees;
    }

    /*
     * 初始化下拉控件的列表（显示分辨率）
     * */
    private void initSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.spn_item, listResolution);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnResolution.setAdapter(adapter);

        int position = listResolution.indexOf(String.format("%dx%d", width, height));
        spnResolution.setSelection(position, false);

        spnResolution.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (mMediaStream != null && mMediaStream.isStreaming()) {
                    int pos = listResolution.indexOf(String.format("%dx%d", width, height));

                    if (pos == position)
                        return;

                    spnResolution.setSelection(pos, false);

                    Toast.makeText(StreamActivity.this, "正在推送中,无法切换分辨率", Toast.LENGTH_SHORT).show();
                    return;
                }

                String r = listResolution.get(position);
                String[] splitR = r.split("x");

                int wh = Integer.parseInt(splitR[0]);
                int ht = Integer.parseInt(splitR[1]);

                if (width != wh || height != ht) {
                    width = wh;
                    height = ht;

                    if (mMediaStream != null) {
                        mMediaStream.updateResolution(width, height);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    /*
     * 开始录像的通知
     * */
    @Subscribe
    public void onStartRecord(StartRecord sr) {
        // 开始录像的通知，记下当前时间
        mRecording = true;
        mRecordingBegin = System.currentTimeMillis();

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textRecordTick.setVisibility(View.VISIBLE);
                textRecordTick.removeCallbacks(mRecordTickRunnable);
                textRecordTick.post(mRecordTickRunnable);

                ImageView ib = findViewById(R.id.streaming_activity_record);
                ib.setImageResource(R.drawable.record_pressed);
            }
        });
    }

    /*
     * 得知停止录像
     * */
    @Subscribe
    public void onStopRecord(StopRecord sr) {
        // 停止录像的通知，更新状态
        mRecording = false;
        mRecordingBegin = 0;

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textRecordTick.setVisibility(View.INVISIBLE);
                textRecordTick.removeCallbacks(mRecordTickRunnable);

                ImageView ib = findViewById(R.id.streaming_activity_record);
                ib.setImageResource(R.drawable.record);
            }
        });
    }

    /*
     * 开始推流，获取fps、bps
     * */
    @Subscribe
    public void onStreamStat(final StreamStat stat) {
        streamStat.post(() ->
                streamStat.setText(getString(R.string.stream_stat,
                        stat.framePerSecond,
                        stat.bytesPerSecond * 8 / 1024))
        );
    }

    /*
     * 获取可以支持的分辨率
     * */
    @Subscribe
    public void onSupportResolution(SupportResolution res) {
        runOnUiThread(() -> {
            listResolution = Util.getSupportResolution(getApplicationContext());
            boolean supportdefault = listResolution.contains(String.format("%dx%d", width, height));

            if (!supportdefault) {
                String r = listResolution.get(0);
                String[] splitR = r.split("x");

                width = Integer.parseInt(splitR[0]);
                height = Integer.parseInt(splitR[1]);
            }

            initSpinner();
        });
    }

    /*
     * 显示推流的状态
     * */
    private void sendMessage(String message) {
        Message msg = Message.obtain();
        msg.what = MSG_STATE;
        Bundle bundle = new Bundle();
        bundle.putString(STATE, message);
        msg.setData(bundle);

        handler.sendMessage(msg);
    }

    /* ========================= 点击事件 ========================= */

    /**
     * Take care of popping the fragment back stack or finishing the activity
     * as appropriate.
     */
    @Override
    public void onBackPressed() {
//        boolean isStreaming = mMediaStream != null && mMediaStream.isStreaming();
//
//        if (isStreaming && SPUtil.getEnableBackgroundCamera(this)) {
//            new AlertDialog.Builder(this).setTitle("是否允许后台上传？")
//                    .setMessage("您设置了使能摄像头后台采集,是否继续在后台采集并上传视频？如果是，记得直播结束后,再回来这里关闭直播。")
//                    .setNeutralButton("后台采集", (dialogInterface, i) -> {
//                        StreamActivity.super.onBackPressed();
//                    })
//                    .setPositiveButton("退出程序", (dialogInterface, i) -> {
//                        mMediaStream.stopStream();
//                        StreamActivity.super.onBackPressed();
//                        Toast.makeText(StreamActivity.this, "程序已退出。", Toast.LENGTH_SHORT).show();
//                    })
//                    .setNegativeButton(android.R.string.cancel, null)
//                    .show();
//            return;
//        } else {
//            super.onBackPressed();
//        }
//
//        boolean isStreaming = mMediaStream != null && mMediaStream.isStreaming();
//
//        if (isStreaming && SPUtil.getEnableBackgroundCamera(this)) {
//            new AlertDialog.Builder(this).setTitle("是否允许后台上传？")
//                    .setMessage("您设置了使能摄像头后台采集,是否继续在后台采集并上传视频？如果是，记得直播结束后,再回来这里关闭直播。")
//                    .setNeutralButton("后台采集", (dialogInterface, i) -> {
//                        StreamActivity.super.onBackPressed();
//                    })
//                    .setPositiveButton("退出程序", (dialogInterface, i) -> {
//                        mMediaStream.stopStream();
//                        StreamActivity.super.onBackPressed();
//                        Toast.makeText(StreamActivity.this, "程序已退出。", Toast.LENGTH_SHORT).show();
//                    })
//                    .setNegativeButton(android.R.string.cancel, null)
//                    .show();
//            return;
//        }

        //与上次点击返回键时刻作差
        if ((System.currentTimeMillis() - mExitTime) > 2000) {
            //大于2000ms则认为是误操作，使用Toast进行提示
            Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
            //并记录下本次点击“返回键”的时刻，以便下次进行判断
            mExitTime = System.currentTimeMillis();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sv_surfaceview:
                try {
                    mMediaStream.getCamera().autoFocus(null);
                } catch (Exception e) {

                }
                break;
            case R.id.btn_switchCamera:
                if (!mMediaStream.isStreaming()) {
                    mMediaStream.switchCamera();
                }
                break;
        }
    }

    /*
     * 录像
     * */
    public void onRecord(View view) {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_STORAGE_PERMISSION);
            return;
        }

        ImageView ib = findViewById(R.id.streaming_activity_record);

        if (mMediaStream != null) {
            if (mMediaStream.isRecording()) {
                mMediaStream.stopRecord();
                ib.setImageResource(R.drawable.record_pressed);
            } else {
                mMediaStream.startRecord();
                ib.setImageResource(R.drawable.record);
            }
        }
    }

    /*
     * 切换分辨率
     * */
    public void onClickResolution(View view) {
        findViewById(R.id.spn_resolution).performClick();
    }

    /*
     * 切换屏幕方向
     * */
    public void onSwitchOrientation(View view) {
        if (mMediaStream != null) {
            if (mMediaStream.isStreaming()) {
                Toast.makeText(this, "正在推送中,无法更改屏幕方向", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        int orientation = getRequestedOrientation();

        if (orientation == SCREEN_ORIENTATION_UNSPECIFIED || orientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

//        if (mMediaStream != null)
//            mMediaStream.setDisplayRotationDegree(getDisplayRotationDegree());
    }

    /*
     * 推流or停止
     * */
    public void onStartOrStopPush(View view) {
        ImageView ib = findViewById(R.id.streaming_activity_push);

        if (mMediaStream != null && !mMediaStream.isStreaming()) {
            try {
                mMediaStream.startStream();

                ib.setImageResource(R.drawable.start_push_pressed);
            } catch (IOException e) {
                e.printStackTrace();
                sendMessage("激活失败，无效Key");
            }
        } else {
            mMediaStream.stopStream();
            ib.setImageResource(R.drawable.start_push);
            sendMessage("离线");
        }
    }

    /*
     * 关于我们
     * */
    public void onAbout(View view) {
        Intent intent = new Intent(this, AboutActivity.class);
        startActivityForResult(intent, 0);
        overridePendingTransition(R.anim.slide_right_in, R.anim.slide_left_out);
    }

    /*
     * 设置
     * */
    public void onSetting(View view) {
        Intent intent = new Intent(this, SettingActivity.class);
        startActivityForResult(intent, 0);
        overridePendingTransition(R.anim.slide_right_in, R.anim.slide_left_out);
    }

    /* ========================= TextureView.SurfaceTextureListener ========================= */

    @Override
    public void onSurfaceTextureAvailable(final SurfaceTexture surface, int width, int height) {
        if (mService != null) {
            goonWithAvailableTexture(surface);
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

    /**
     * 状态的回调
     * */
    @Subscribe
    public void onPushCallback(final PushCallback cb) {
        sendMessage(cb.getName());
    }
}
