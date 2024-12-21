package com.easygbs.easygbd.viewmodel.fragment;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.databinding.BaseObservable;

import com.easygbs.easygbd.R;
import com.easygbs.easygbd.activity.MainActivity;
import com.easygbs.easygbd.activity.RecordListActivity;
import com.easygbs.easygbd.fragment.RecordFragment;
import com.easygbs.easygbd.push.MediaStream;

public class RecordViewModel extends BaseObservable {
    private static final String TAG = RecordViewModel.class.getSimpleName();
    public MainActivity mMainActivity = null;
    public RecordFragment mRecordFragment;
    public Boolean isRecording = false;

    public RecordViewModel(MainActivity mMainActivity, RecordFragment fragment) {
        this.mMainActivity = mMainActivity;
        this.mRecordFragment = fragment;
    }

    public void back(View view) {
        if (mMainActivity.getMMediaStream() != null) {
            mMainActivity.getMMediaStream().stopPreview();
            mMainActivity.getMMediaStream().destroyCamera();
        }

        mRecordFragment.destroyService();

    }

    public void openRecordPath(View view) {
        mMainActivity.openRecordPath();
    }

    public void switchCamera(View view) {
        MediaStream mMediaStream = mMainActivity.getMMediaStream();
        if (mMediaStream != null) {
            boolean isRecording = mMediaStream.isRecording();
            if (isRecording) {
                Toast.makeText(mMainActivity, "正在录像", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        Toast.makeText(mMainActivity, "切换摄像头", Toast.LENGTH_SHORT).show();
        int id = mMainActivity.getMMediaStream().getCameraId();
        int swid = 0;
        if (id == 0) {
            swid = 1;
        } else {
            swid = 0;
        }

        mMainActivity.getMMediaStream().switchCamera(swid);
    }

    public void startOrStopRecording(View view) {
        isRecording = !isRecording;
        Toast.makeText(mMainActivity, String.format("%s录像", isRecording ? "开始" : "结束"), Toast.LENGTH_SHORT).show();
        ImageView imageView = (ImageView) view;
        imageView.setImageResource(isRecording ? R.mipmap.recording : R.mipmap.record_default);  // 替换为新的图片资源

        MediaStream mMediaStream = mMainActivity.getMMediaStream();
        if (mMediaStream != null) {
            boolean isRecording = mMediaStream.isRecording();
            if (isRecording) {
                mMediaStream.stopRecord();
            } else {
                mMediaStream.startRecord();
            }
        }
    }
}

