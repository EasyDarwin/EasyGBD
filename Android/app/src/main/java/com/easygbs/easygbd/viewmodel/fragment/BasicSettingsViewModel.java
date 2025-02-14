package com.easygbs.easygbd.viewmodel.fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.Toast;

import androidx.databinding.BaseObservable;
import androidx.databinding.ObservableField;
import androidx.lifecycle.ViewModel;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.easygbs.easygbd.R;
import com.easygbs.easygbd.activity.MainActivity;
import com.easygbs.easygbd.adapter.LoglevelAdapter;
import com.easygbs.easygbd.adapter.TranproAdapter;
import com.easygbs.easygbd.adapter.VerAdapter;
import com.easygbs.easygbd.bean.LoglevelBean;
import com.easygbs.easygbd.bean.TranproBean;
import com.easygbs.easygbd.bean.VerBean;
import com.easygbs.easygbd.common.Constant;
import com.easygbs.easygbd.fragment.BasicSettingsFragment;
import com.easygbs.easygbd.logger.LogLevel;
import com.easygbs.easygbd.logger.Logger;
import com.easygbs.easygbd.push.MediaStream;
import com.easygbs.easygbd.util.SPHelper;
import com.easygbs.easygbd.viewadapter.MultiItemTypeAdapter;

import org.easydarwin.util.SPUtil;

import java.util.ArrayList;
import java.util.List;

public class BasicSettingsViewModel extends ViewModel {
    private static final String TAG = BasicSettingsViewModel.class.getSimpleName();
    public MainActivity mMainActivity = null;
    public BasicSettingsFragment mBasicSettingsFragment = null;

    public LayoutInflater mLayoutInflater = null;

    public View mViewVer = null;

    public RecyclerView rvver;

    public List<VerBean> VerBeanList = null;

    public VerAdapter mVerAdapter = null;

    public PopupWindow mPopupVer = null;

    public View mViewTranpro = null;

    public RecyclerView rvtranpro;

    public List<TranproBean> TranproBeanList = null;

    public TranproAdapter mTranproAdapter = null;

    public PopupWindow mPopupTranpro = null;

    public View mViewLoglevel = null;

    public RecyclerView rvloglevel;

    public List<LoglevelBean> LoglevelBeanList = null;

    public LoglevelAdapter mLoglevelAdapter = null;

    public PopupWindow mPopupLoglevel = null;

    public ObservableField<String> sipserveraddrObservableField = new ObservableField<>();

    public SPHelper mSPHelper;

    public BasicSettingsViewModel(MainActivity mMainActivity, BasicSettingsFragment mBasicSettingsFragment) {
        this.mMainActivity = mMainActivity;
        this.mBasicSettingsFragment = mBasicSettingsFragment;
        mLayoutInflater = LayoutInflater.from(mMainActivity);

        int getver = SPUtil.getVer(mMainActivity);
        mBasicSettingsFragment.mFragmentBasicsettingsBinding.tvver.setText("" + getver);

        mBasicSettingsFragment.mFragmentBasicsettingsBinding.etport.setText("" + SPUtil.getServerport(mMainActivity));

        mBasicSettingsFragment.mFragmentBasicsettingsBinding.etsipserveraddr.setText(SPUtil.getServerip(mMainActivity));
        mBasicSettingsFragment.mFragmentBasicsettingsBinding.etsipserverid.setText(SPUtil.getServerid(mMainActivity));

        mBasicSettingsFragment.mFragmentBasicsettingsBinding.etsipserverdomain.setText(SPUtil.getServerdomain(mMainActivity));

        String sipUserName = SPUtil.getDeviceid(mMainActivity);

        if (sipUserName.isEmpty()) {
            String pDeviceId = "34020000001320";
            int n = (int) (Math.random() * 999999) + 1;
            sipUserName = pDeviceId + String.format("%06d", n);
            SPUtil.setDeviceid(mMainActivity, sipUserName);
        }

        mBasicSettingsFragment.mFragmentBasicsettingsBinding.tvsipname.setText(sipUserName);

        String deviceName = SPUtil.getDevicename(mMainActivity);
        if (deviceName.isEmpty()) {
            deviceName = "EasyGBD-" + sipUserName.substring(sipUserName.length() - 6);
            SPUtil.setDevicename(mMainActivity, deviceName);
        }

        mBasicSettingsFragment.mFragmentBasicsettingsBinding.etdevicename.setText(deviceName);

        mBasicSettingsFragment.mFragmentBasicsettingsBinding.etsippassword.setText(SPUtil.getPassword(mMainActivity));
        mBasicSettingsFragment.mFragmentBasicsettingsBinding.etregistervalidtime.setText("" + SPUtil.getRegexpires(mMainActivity));
        mBasicSettingsFragment.mFragmentBasicsettingsBinding.tvheartbeatcycle.setText("" + SPUtil.getHeartbeatinterval(mMainActivity));
        mBasicSettingsFragment.mFragmentBasicsettingsBinding.etheartbeatcount.setText("" + SPUtil.getHeartbeatcount(mMainActivity));

        mSPHelper = mMainActivity.getSPHelper();
        int logstatus = (int) mSPHelper.get(Constant.LOGSTATUS, 1);
        if (logstatus == 0) {
            mBasicSettingsFragment.mFragmentBasicsettingsBinding.lllog.setBackgroundResource(R.mipmap.ic_notselected);

            mBasicSettingsFragment.mFragmentBasicsettingsBinding.ivlog.setVisibility(View.INVISIBLE);
        } else {
            mBasicSettingsFragment.mFragmentBasicsettingsBinding.lllog.setBackgroundResource(R.mipmap.ic_selected);

            mBasicSettingsFragment.mFragmentBasicsettingsBinding.ivlog.setVisibility(View.VISIBLE);
        }

        int pro = SPUtil.getProtocol(mMainActivity);
        if (pro == 0) {
            mBasicSettingsFragment.mFragmentBasicsettingsBinding.tvtranpro.setText("UDP");
        } else {
            mBasicSettingsFragment.mFragmentBasicsettingsBinding.tvtranpro.setText("TCP");
        }

        mSPHelper = mMainActivity.getSPHelper();
        String loglev = (String) mSPHelper.get(Constant.LOGLEV, "DEBUG");
        mBasicSettingsFragment.mFragmentBasicsettingsBinding.tvloglevel.setText(loglev);
        if (loglev.equals("DEBUG")) {
            Logger.getInstance().setLogLevel(LogLevel.DEBUG);
        } else if (loglev.equals("INFO")) {
            Logger.getInstance().setLogLevel(LogLevel.INFO);
        } else if (loglev.equals("WARNING")) {
            Logger.getInstance().setLogLevel(LogLevel.WARN);
        } else if (loglev.equals("ERROR")) {
            Logger.getInstance().setLogLevel(LogLevel.ERROR);
        }
        Logger.getInstance().clearLogText();
    }

    public void openQR(View view) {
        MediaStream mMediaStream = mMainActivity.getMMediaStream();
        if (mMediaStream != null) {
            if (mMediaStream.isStreaming()) {
                Toast.makeText(mMainActivity, "正在推流", Toast.LENGTH_SHORT).show();
                return;
            } else {
                mMediaStream.stopPreview();
                mMediaStream.destroyCamera();
                mBasicSettingsFragment.destroyService();
            }
        }

        Toast.makeText(mMainActivity, "扫一扫", Toast.LENGTH_SHORT).show();
        mMainActivity.toSc();
    }

    public void ver(View view) {
        view.post(new Runnable() {
            @Override
            public void run() {

                // 获取视图的宽度和高度
                int viewWidth = view.getWidth();
                if (mViewVer == null) {
                    mViewVer = mLayoutInflater.inflate(R.layout.po_ver, null);

                    rvver = mViewVer.findViewById(R.id.rvver);

                    rvver.setLayoutManager(new GridLayoutManager(mMainActivity, 1, RecyclerView.VERTICAL, false));

                    VerBeanList = new ArrayList<VerBean>();
                    mVerAdapter = new VerAdapter(mMainActivity, mMainActivity, R.layout.adapter_ver, VerBeanList);
                    rvver.setAdapter(mVerAdapter);

                    mVerAdapter.setOnItemClickListener(new MultiItemTypeAdapter.OnItemClickListener() {
                        @Override
                        public void onItemClick(View view, RecyclerView.ViewHolder holder, int position) {
                            String name = VerBeanList.get(position).getName();

                            SPUtil.setVer(mMainActivity, Integer.parseInt(name));

                            mBasicSettingsFragment.mFragmentBasicsettingsBinding.tvver.setText(name);

                            for (int i = 0; i < VerBeanList.size(); i++) {
                                VerBean mVerBean = VerBeanList.get(i);
                                mVerBean.setIsst(0);
                            }

                            for (int i = 0; i < VerBeanList.size(); i++) {
                                VerBean mVerBean = VerBeanList.get(i);
                                if (mVerBean.getName().equals(name)) {
                                    mVerBean.setIsst(1);
                                    break;
                                }
                            }

                            mVerAdapter.notifyDataSetChanged();


                            hidePopVer();
                        }

                        @Override
                        public boolean onItemLongClick(View view, RecyclerView.ViewHolder holder, int position) {
                            return false;
                        }
                    });


                    int getver = SPUtil.getVer(mMainActivity);

                    String[] vers = mMainActivity.getResources().getStringArray(R.array.verarr);
                    for (int i = 0; i < vers.length; i++) {
                        VerBean mVerBean = new VerBean();
                        mVerBean.setName(vers[i]);
                        if (getver == Integer.parseInt(vers[i])) {
                            mVerBean.setIsst(1);
                        } else {
                            mVerBean.setIsst(0);
                        }

                        VerBeanList.add(mVerBean);
                    }

                    mBasicSettingsFragment.mFragmentBasicsettingsBinding.tvver.setText("" + getver);

                    mVerAdapter.notifyDataSetChanged();
                }

                // 动态计算 PopupWindow 高度
                mViewVer.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
                int height = mViewVer.getMeasuredHeight(); // 获取测量后的高度

                mPopupVer = new PopupWindow(mViewVer, viewWidth, height);

                mPopupVer.setOutsideTouchable(true);

                mPopupVer.setOnDismissListener(new PopupWindow.OnDismissListener() {
                    @Override
                    public void onDismiss() {

                    }
                });

                int yoff = (int) mMainActivity.getResources().getDimension(R.dimen.dp_4);

                mPopupVer.showAsDropDown(mBasicSettingsFragment.mFragmentBasicsettingsBinding.tvver, 0, yoff);
            }
        });
    }

    public void hidePopVer() {
        if (mPopupVer != null) {
            boolean isShowing = mPopupVer.isShowing();
            if (isShowing) {
                mPopupVer.dismiss();
            }
        }
    }

    public void tranpro(View view) {
        view.post(new Runnable() {
            @Override
            public void run() {
                // 获取视图的宽度和高度
                int viewWidth = view.getWidth();
                if (mViewTranpro == null) {
                    mViewTranpro = mLayoutInflater.inflate(R.layout.po_ver, null);

                    rvtranpro = mViewTranpro.findViewById(R.id.rvver);

                    rvtranpro.setLayoutManager(new GridLayoutManager(mMainActivity, 1, RecyclerView.VERTICAL, false));

                    TranproBeanList = new ArrayList<TranproBean>();
                    mTranproAdapter = new TranproAdapter(mMainActivity, mMainActivity, R.layout.adapter_ver, TranproBeanList);
                    rvtranpro.setAdapter(mTranproAdapter);

                    mTranproAdapter.setOnItemClickListener(new MultiItemTypeAdapter.OnItemClickListener() {
                        @Override
                        public void onItemClick(View view, RecyclerView.ViewHolder holder, int position) {
                            String name = TranproBeanList.get(position).getName();

                            int pro;
                            if (name.equals("UDP")) {
                                pro = 0;
                            } else {
                                pro = 1;
                            }
                            SPUtil.setProtocol(mMainActivity, pro);

                            mBasicSettingsFragment.mFragmentBasicsettingsBinding.tvtranpro.setText(name);

                            for (int i = 0; i < TranproBeanList.size(); i++) {
                                TranproBean mTranproBean = TranproBeanList.get(i);
                                mTranproBean.setIsst(0);
                            }

                            for (int i = 0; i < TranproBeanList.size(); i++) {
                                TranproBean mTranproBean = TranproBeanList.get(i);
                                if (mTranproBean.getName().equals(name)) {
                                    mTranproBean.setIsst(1);
                                    break;
                                }
                            }

                            mTranproAdapter.notifyDataSetChanged();

                            hidePopTranpro();
                        }

                        @Override
                        public boolean onItemLongClick(View view, RecyclerView.ViewHolder holder, int position) {
                            return false;
                        }
                    });

                    int pro = SPUtil.getProtocol(mMainActivity);
                    String str = "";
                    if (pro == 0) {
                        str = "UDP";
                    } else {
                        str = "TCP";
                    }

                    mBasicSettingsFragment.mFragmentBasicsettingsBinding.tvtranpro.setText(str);

                    String[] tranpros = mMainActivity.getResources().getStringArray(R.array.tranproarr);
                    for (int i = 0; i < tranpros.length; i++) {
                        TranproBean mTranproBean = new TranproBean();
                        mTranproBean.setName(tranpros[i]);
                        if (str.equals(tranpros[i])) {
                            mTranproBean.setIsst(1);
                        } else {
                            mTranproBean.setIsst(0);
                        }

                        TranproBeanList.add(mTranproBean);
                    }

                    mTranproAdapter.notifyDataSetChanged();
                }

                // 动态计算 PopupWindow 高度
                mViewTranpro.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
                int height = mViewTranpro.getMeasuredHeight(); // 获取测量后的高度

                mPopupTranpro = new PopupWindow(mViewTranpro, viewWidth, height);

                mPopupTranpro.setOutsideTouchable(true);

                mPopupTranpro.setOnDismissListener(new PopupWindow.OnDismissListener() {
                    @Override
                    public void onDismiss() {

                    }
                });

                int y0ff = (int) mMainActivity.getResources().getDimension(R.dimen.dp_4);

                mPopupTranpro.showAsDropDown(mBasicSettingsFragment.mFragmentBasicsettingsBinding.tvtranpro, 0, y0ff);
            }
        });
    }

    public void hidePopTranpro() {
        if (mPopupTranpro != null) {
            boolean isShowing = mPopupTranpro.isShowing();
            if (isShowing) {
                mPopupTranpro.dismiss();
            }
        }
    }

    public void loglevel(View view) {
        view.post(new Runnable() {
            @Override
            public void run() {
                // 获取视图的宽度和高度
                int viewWidth = view.getWidth();

                if (mViewLoglevel == null) {

                    mViewLoglevel = mLayoutInflater.inflate(R.layout.po_ver, null);

                    rvloglevel = mViewLoglevel.findViewById(R.id.rvver);

                    rvloglevel.setLayoutManager(new GridLayoutManager(mMainActivity, 1, RecyclerView.VERTICAL, false));

                    LoglevelBeanList = new ArrayList<LoglevelBean>();
                    mLoglevelAdapter = new LoglevelAdapter(mMainActivity, mMainActivity, R.layout.adapter_ver, LoglevelBeanList);
                    rvloglevel.setAdapter(mLoglevelAdapter);

                    mLoglevelAdapter.setOnItemClickListener(new MultiItemTypeAdapter.OnItemClickListener() {
                        @Override
                        public void onItemClick(View view, RecyclerView.ViewHolder holder, int position) {
                            String name = LoglevelBeanList.get(position).getName();

                            String loglev = (String) mSPHelper.get(Constant.LOGLEV, "DEBUG");


                            if (loglev.equals(name)) {
                                Log.i(TAG, "一样  log");
                                hidePopLoglevel();
                                return;
                            }


                            mSPHelper.put(Constant.LOGLEV, name);

                            mBasicSettingsFragment.mFragmentBasicsettingsBinding.tvloglevel.setText(name);

                            for (int i = 0; i < LoglevelBeanList.size(); i++) {
                                LoglevelBean mLoglevelBean = LoglevelBeanList.get(i);
                                mLoglevelBean.setIsst(0);
                            }

                            for (int i = 0; i < LoglevelBeanList.size(); i++) {
                                LoglevelBean mLoglevelBean = LoglevelBeanList.get(i);
                                if (mLoglevelBean.getName().equals(name)) {
                                    mLoglevelBean.setIsst(1);
                                    break;
                                }
                            }
                            mLoglevelAdapter.notifyDataSetChanged();

                            Log.i(TAG, "一样  log" + name + "," + (name.equals("DEBUG")));
                            if (name.equals("DEBUG")) {
                                Logger.getInstance().setLogLevel(LogLevel.DEBUG);
                            } else if (name.equals("INFO")) {
                                Logger.getInstance().setLogLevel(LogLevel.INFO);
                            } else if (name.equals("WARNING")) {
                                Logger.getInstance().setLogLevel(LogLevel.WARN);
                            } else if (name.equals("ERROR")) {
                                Logger.getInstance().setLogLevel(LogLevel.ERROR);
                            }
                            Logger.getInstance().clearLogText();
                            hidePopLoglevel();
                        }

                        @Override
                        public boolean onItemLongClick(View view, RecyclerView.ViewHolder holder, int position) {
                            return false;
                        }
                    });

                    SPHelper mSPHelper = mMainActivity.getSPHelper();
                    String loglev = (String) mSPHelper.get(Constant.LOGLEV, "DEBUG");
                    mBasicSettingsFragment.mFragmentBasicsettingsBinding.tvloglevel.setText(loglev);

                    String[] loglevels = mMainActivity.getResources().getStringArray(R.array.loglevelarr);
                    for (int i = 0; i < loglevels.length; i++) {
                        LoglevelBean mLoglevelBean = new LoglevelBean();
                        mLoglevelBean.setName(loglevels[i]);
                        if (loglev.equals(loglevels[i])) {
                            mLoglevelBean.setIsst(1);
                        } else {
                            mLoglevelBean.setIsst(0);
                        }

                        LoglevelBeanList.add(mLoglevelBean);
                    }

                    mLoglevelAdapter.notifyDataSetChanged();
                }

                // 动态计算 PopupWindow 高度
                mViewLoglevel.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
                int height = mViewLoglevel.getMeasuredHeight(); // 获取测量后的高度

                mPopupLoglevel = new PopupWindow(mViewLoglevel, viewWidth, height);

                mPopupLoglevel.setOutsideTouchable(true);

                mPopupLoglevel.setOnDismissListener(new PopupWindow.OnDismissListener() {
                    @Override
                    public void onDismiss() {

                    }
                });

                int yoff = (int) mMainActivity.getResources().getDimension(R.dimen.dp_4);

                mPopupLoglevel.showAsDropDown(mBasicSettingsFragment.mFragmentBasicsettingsBinding.tvloglevel, 0, yoff);

            }
        });


    }

    public void hidePopLoglevel() {
        if (mPopupLoglevel != null) {
            boolean isShowing = mPopupLoglevel.isShowing();
            if (isShowing) {
                mPopupLoglevel.dismiss();
            }
        }
    }

    public void save(String text) {
        mBasicSettingsFragment.save();
    }

    // 创建一个 TextWatcher，用于监听 EditText 的文本变化
    public TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
            // 文本变化前的逻辑
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
            // 文本变化中的逻辑

        }

        @Override
        public void afterTextChanged(Editable editable) {
            save(editable.toString());  // 调用 save 方法
        }
    };

    public void startOrStopRecording(View view) {
        MediaStream mMediaStream = mMainActivity.getMMediaStream();
        boolean isRecording = mMediaStream.isRecording();
        Toast.makeText(mMainActivity, String.format("%s录像", !isRecording ? "开始" : "结束"), Toast.LENGTH_SHORT).show();
        ImageView imageView = (ImageView) view;
        imageView.setImageResource(!isRecording ? R.mipmap.recording : R.mipmap.record_default);  // 替换为新的图片资源

        if (mMediaStream != null) {
            if (isRecording) {
                mMediaStream.stopRecord();
            } else {
                mMediaStream.startRecord();
            }
        }
    }
}

