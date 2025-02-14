package com.easygbs.easygbd.fragment;
import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.fragment.app.Fragment;
import com.easygbs.easygbd.R;
import com.easygbs.easygbd.activity.MainActivity;
import com.easygbs.easygbd.databinding.FragmentAboutBinding;
import com.easygbs.easygbd.util.ScrUtil;
import com.easygbs.easygbd.viewmodel.fragment.AboutViewModel;

public class AboutFragment extends Fragment {
	public String TAG= AboutFragment.class.getSimpleName();
	public MainActivity mMainActivity;
	public FragmentAboutBinding mFragmentAboutBinding;
	public AboutViewModel mAboutViewModel;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mMainActivity= (MainActivity) activity;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
		mFragmentAboutBinding = FragmentAboutBinding.inflate(inflater);

		mAboutViewModel = new AboutViewModel(mMainActivity,AboutFragment.this);
		mFragmentAboutBinding.setViewModel(mAboutViewModel);

		View mView=mFragmentAboutBinding.getRoot();
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

	public void show(){
		LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) mFragmentAboutBinding.llroot.getLayoutParams();
		layoutParams.topMargin = ScrUtil.getStatusBarHeight(mMainActivity);
		mFragmentAboutBinding.llroot.setLayoutParams(layoutParams);
	}
}