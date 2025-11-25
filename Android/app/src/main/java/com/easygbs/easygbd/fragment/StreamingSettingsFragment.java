package com.easygbs.easygbd.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.easygbs.easygbd.R;
import com.easygbs.easygbd.activity.MainActivity;
import com.easygbs.easygbd.databinding.FragmentStreamingsettingsBinding;

import org.easydarwin.util.SPUtil;

import com.easygbs.easygbd.viewmodel.fragment.StreamingSettingsViewModel;

public class StreamingSettingsFragment extends Fragment {
    public String TAG = StreamingSettingsFragment.class.getSimpleName();
    public MainActivity mMainActivity;
    public FragmentStreamingsettingsBinding mFragmentStreamingsettingsBinding;
    public StreamingSettingsViewModel mStreamingSettingsViewModel;
    public int isenvideo = 0;
    public int isenaudio = 0;
    public int isenlocreport = 0;
    public int isEnTransPush = 0;

    public Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {

            }
        }
    };

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
        mFragmentStreamingsettingsBinding = FragmentStreamingsettingsBinding.inflate(inflater);

        mStreamingSettingsViewModel = new StreamingSettingsViewModel(mMainActivity, StreamingSettingsFragment.this);
        mFragmentStreamingsettingsBinding.setViewModel(mStreamingSettingsViewModel);

        init();

        View mView = mFragmentStreamingsettingsBinding.getRoot();
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

    public void init() {
        isenvideo = SPUtil.getIsenvideo(mMainActivity);
        isEnTransPush = SPUtil.getIsEnTransPush(mMainActivity);



        mFragmentStreamingsettingsBinding.llaudiosetall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isenaudio == 0) {
                    isenaudio = 1;
                    mFragmentStreamingsettingsBinding.llaudioset.setBackgroundResource(R.mipmap.ic_selected);
                    mFragmentStreamingsettingsBinding.ivaudioset.setVisibility(View.VISIBLE);

                    mFragmentStreamingsettingsBinding.llaudiocode.setEnabled(true);
                    mFragmentStreamingsettingsBinding.llsamplingrate.setEnabled(true);
                    mFragmentStreamingsettingsBinding.llaudiochannel.setEnabled(true);
                    mFragmentStreamingsettingsBinding.llaudiocoderate.setEnabled(true);
                } else {
                    isenaudio = 0;
                    mFragmentStreamingsettingsBinding.llaudioset.setBackgroundResource(R.mipmap.ic_notselected);
                    mFragmentStreamingsettingsBinding.ivaudioset.setVisibility(View.INVISIBLE);

                    mFragmentStreamingsettingsBinding.llaudiocode.setEnabled(false);
                    mFragmentStreamingsettingsBinding.llsamplingrate.setEnabled(false);
                    mFragmentStreamingsettingsBinding.llaudiochannel.setEnabled(false);
                    mFragmentStreamingsettingsBinding.llaudiocoderate.setEnabled(false);
                }
            }
        });

        mFragmentStreamingsettingsBinding.lllocreportsetall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isenlocreport == 0) {
                    isenlocreport = 1;
                    mFragmentStreamingsettingsBinding.lllocreportset.setBackgroundResource(R.mipmap.ic_selected);
                    mFragmentStreamingsettingsBinding.ivlocreportset.setVisibility(View.VISIBLE);

                    mFragmentStreamingsettingsBinding.lllocationfrequency.setEnabled(true);
                } else {
                    isenlocreport = 0;
                    mFragmentStreamingsettingsBinding.lllocreportset.setBackgroundResource(R.mipmap.ic_notselected);
                    mFragmentStreamingsettingsBinding.ivlocreportset.setVisibility(View.INVISIBLE);

                    mFragmentStreamingsettingsBinding.lllocationfrequency.setEnabled(false);
                }
            }
        });

    }

}