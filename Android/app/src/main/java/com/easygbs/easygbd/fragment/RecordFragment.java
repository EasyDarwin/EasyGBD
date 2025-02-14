package com.easygbs.easygbd.fragment;

import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.fragment.app.Fragment;

import com.easygbs.easygbd.activity.MainActivity;
import com.easygbs.easygbd.common.Constant;
import com.easygbs.easygbd.databinding.FragmentRecordBinding;
import com.easygbs.easygbd.push.MediaStream;
import com.easygbs.easygbd.service.BackgroundCameraService;
import com.easygbs.easygbd.service.UVCCameraService;
import com.easygbs.easygbd.util.PeUtil;

import org.easydarwin.util.SPUtil;

import com.easygbs.easygbd.util.ScrUtil;
import com.easygbs.easygbd.viewmodel.fragment.RecordViewModel;

import java.io.File;

public class RecordFragment extends Fragment {
    public String TAG = RecordFragment.class.getSimpleName();
    public MainActivity mMainActivity;
    public FragmentRecordBinding mFragmentRecordBinding;
    public RecordViewModel mRecordViewModel;

    private BackgroundCameraService mService;
    private ServiceConnection conn;

    public Intent intent;

    public Intent intent1;

    private int width = 1920;
    private int height = 1080;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mMainActivity = (MainActivity) activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mFragmentRecordBinding = FragmentRecordBinding.inflate(inflater);
        mRecordViewModel = new RecordViewModel(mMainActivity, RecordFragment.this);
        mFragmentRecordBinding.setViewModel(mRecordViewModel);

        View mView = mFragmentRecordBinding.getRoot();
        return mView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    public void show() {
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) mFragmentRecordBinding.llroot.getLayoutParams();
        // 设置宽度和高度为 MATCH_PARENT
        layoutParams.topMargin = ScrUtil.getStatusBarHeight(mMainActivity);
        mFragmentRecordBinding.llroot.setLayoutParams(layoutParams);
        init();
    }

    public void init() {
        initCamera();
    }

    public void initCamera() {
        try {
            boolean hasPermission1 = PeUtil.ishasPer(mMainActivity, Manifest.permission.CAMERA);
            if (hasPermission1) {
                boolean hasPermission2 = PeUtil.ishasPer(mMainActivity, Manifest.permission.RECORD_AUDIO);
                if (hasPermission2) {
                    intent = new Intent(mMainActivity, BackgroundCameraService.class);
                    mMainActivity.startService(intent);

                    intent1 = new Intent(mMainActivity, UVCCameraService.class);
                    mMainActivity.startService(intent1);

                    conn = new ServiceConnection() {
                        @Override
                        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                            mService = ((BackgroundCameraService.LocalBinder) iBinder).getService();

                            if (mFragmentRecordBinding.tvcamera.isAvailable()) {
                                goonWithAvailableTexture(mFragmentRecordBinding.tvcamera.getSurfaceTexture());
                            }
                        }

                        @Override
                        public void onServiceDisconnected(ComponentName componentName) {

                        }
                    };

                    mMainActivity.bindService(new Intent(mMainActivity, BackgroundCameraService.class), conn, 0);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "initCamera  Exception  " + e.toString());
        }
    }

    private void goonWithAvailableTexture(SurfaceTexture surface) {
        final File easyPusher = new File(recordPath());
        easyPusher.mkdirs();

        MediaStream ms = mService.getMediaStream();

        if (ms != null) {
            ms.stopPreview();
            mService.inActivePreview();
            ms.setSurfaceTexture(surface);
            ms.startPreview();

            mMainActivity.setMMediaStream(ms);

            if (ms.getDisplayRotationDegree() != getDisplayRotationDegree()) {
                int orientation = mMainActivity.getRequestedOrientation();

                if (orientation == SCREEN_ORIENTATION_UNSPECIFIED || orientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
                    mMainActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                } else {
                    mMainActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                }
            }
        } else {
            boolean enableVideo = SPUtil.getEnableVideo(mMainActivity);

            mMainActivity.setMMediaStream(new MediaStream(mMainActivity.getApplicationContext(), surface, enableVideo));
            String path = easyPusher.getPath();

            mMainActivity.getMMediaStream().setRecordPath(path);

            startCamera();

            mService.setMediaStream(mMainActivity.getMMediaStream());
        }
    }

    public void startCamera() {
        int cameraid = SPUtil.getCameraid(mMainActivity);
        mMainActivity.getMMediaStream().setCameraId(cameraid);
        int videoresolution = SPUtil.getVideoresolution(mMainActivity);
        if (videoresolution == 0) {
            width = 1920;
            height = 1080;
        } else if (videoresolution == 1) {
            width = 1280;
            height = 720;
        } else if (videoresolution == 2) {
            width = 640;
            height = 480;
        }
        mMainActivity.getMMediaStream().updateResolution(width, height);
        mMainActivity.getMMediaStream().setDisplayRotationDegree(getDisplayRotationDegree());
        mMainActivity.getMMediaStream().createCamera();
        mMainActivity.getMMediaStream().startPreview();
    }

    public String recordPath() {
        return mMainActivity.getExternalFilesDir(null).getAbsolutePath() + "/" + Constant.DIR;
    }

    private int getDisplayRotationDegree() {
        int rotation = mMainActivity.getWindowManager().getDefaultDisplay().getRotation();
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

    public void destroyService() {
        if (mMainActivity == null) return;
        if (intent != null) mMainActivity.stopService(intent);
        if (intent1 != null) mMainActivity.stopService(intent1);
        if (conn != null) mMainActivity.unbindService(conn);
    }
}