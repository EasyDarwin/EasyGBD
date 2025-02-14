package com.easygbs.easygbd.viewmodel.fragment;

import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.databinding.BaseObservable;
import androidx.databinding.ObservableField;

import com.easygbs.easygbd.activity.MainActivity;
import com.easygbs.easygbd.dao.DCon;
import com.easygbs.easygbd.dao.bean.Chan;
import com.easygbs.easygbd.fragment.ChannelSettingsFragment;
import com.easygbs.easygbd.push.MediaStream;
import org.easydarwin.util.SPUtil;

import java.util.Random;

public class ChannelSettingsViewModel extends BaseObservable {
    private static final String TAG = ChannelSettingsViewModel.class.getSimpleName();
    public MainActivity mMainActivity = null;
    public ChannelSettingsFragment mChannelSettingsFragment = null;
    private static final String pCid = "34020000001320";

    public ChannelSettingsViewModel(MainActivity mMainActivity, ChannelSettingsFragment mChannelSettingsFragment) {
        this.mMainActivity = mMainActivity;
        this.mChannelSettingsFragment = mChannelSettingsFragment;
    }

    public void add(View view) {
        try {
            Chan mChan = (Chan) mMainActivity.getMDOpe().OperaDatabase(DCon.Chanquerymaxuid);

            if (mChan == null) {
                Chan mmChan = new Chan();
                mmChan.setUid(1);
                mmChan.setCid("34020000001320000001");
                mmChan.setNa("channel1");
                mmChan.setSta(1);
                mMainActivity.ChanIn(mmChan);
            } else {
                long size = (long) mMainActivity.getMDOpe().OperaDatabase(DCon.ChanCount);
                if (size >= 8) {
                    Toast.makeText(mMainActivity, "最大通道数仅支持 8 通道", Toast.LENGTH_SHORT).show();
                    return;
                }
                Chan mmChan = new Chan();
                int uid = mChan.getEc() + 1;
                mmChan.setUid(uid);
                mmChan.setCid(pCid + String.format("%06d", uid));
//                int n = (int) (Math.random() * 999999) + 1;
//                mmChan.setCid(pCid + String.format("%06d", n));
                mmChan.setNa("channel" + uid);
                mmChan.setSta(1);
                mMainActivity.ChanIn(mmChan);
            }

            mChannelSettingsFragment.showChannels();
        } catch (Exception e) {
            Log.e(TAG, "add  Exception  " + e.toString());
        }
    }

    public void save(View view) {
        mMainActivity.goTopBaseSetting();
    }

    public void deleteChan(View view) {
        mChannelSettingsFragment.delete();
    }
}





