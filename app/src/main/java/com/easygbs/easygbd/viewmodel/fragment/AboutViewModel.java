package com.easygbs.easygbd.viewmodel.fragment;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.databinding.BaseObservable;

import com.easygbs.easygbd.R;
import com.easygbs.easygbd.activity.MainActivity;
import com.easygbs.easygbd.dao.DCon;
import com.easygbs.easygbd.dao.bean.Chan;
import com.easygbs.easygbd.fragment.AboutFragment;
import com.easygbs.easygbd.fragment.ChannelSettingsFragment;

public class AboutViewModel extends BaseObservable {
    private static final String TAG = AboutViewModel.class.getSimpleName();
    public MainActivity mMainActivity = null;
    public AboutFragment mAboutFragment;

    public AboutViewModel(MainActivity mMainActivity, AboutFragment mAboutFragment) {
        this.mMainActivity = mMainActivity;
        this.mAboutFragment=mAboutFragment;

        mAboutFragment.mFragmentAboutBinding.tvver.setText("Version  "+getversionName());
    }

    public void back(View view) {
        mMainActivity.about();
    }

    public String getversionName(){
        String ve="";
        try{
            PackageManager mPackageManager = null;
            PackageInfo mPackageInfo = null;
            mPackageManager=mMainActivity.getPackageManager();
            mPackageInfo = mPackageManager.getPackageInfo(mMainActivity.getPackageName(), PackageManager.GET_CONFIGURATIONS);
            ve = mPackageInfo.versionName;
        }catch(Exception e){
            Log.e(TAG,"getversionName  Exception  "+ e);
        }
        return ve;
    }
}

