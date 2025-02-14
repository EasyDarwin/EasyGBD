package com.easygbs.easygbd.fragment;

import android.Manifest;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.OrientationHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.easygbs.easygbd.R;
import com.easygbs.easygbd.common.Constant;
import com.easygbs.easygbd.dao.DCon;
import com.easygbs.easygbd.dao.DOpe;
import com.easygbs.easygbd.dao.bean.Chan;
import com.easygbs.easygbd.dialog.ChanDialog;
import com.easygbs.easygbd.util.PeUtil;
import com.easygbs.easygbd.util.ScrUtil;
import com.easygbs.easygbd.viewadapter.MultiItemTypeAdapter;
import com.easygbs.easygbd.activity.MainActivity;
import com.easygbs.easygbd.adapter.ChannelAdapter;
import com.easygbs.easygbd.databinding.FragmentChannelsettingsBinding;
import com.easygbs.easygbd.viewmodel.fragment.ChannelSettingsViewModel;

import android.app.Dialog;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ChannelSettingsFragment extends Fragment {
    private String TAG = ChannelSettingsFragment.class.getSimpleName();
    private MainActivity mMainActivity;
    private FragmentChannelsettingsBinding mFragmentChannelsettingsBinding;
    private ChannelSettingsViewModel mChannelSettingsViewModel;

    public Chan modifyChan;
    private View operationView;

    public List<Chan> ChanList;
    public ChannelAdapter mChannelAdapter;

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
        mFragmentChannelsettingsBinding = FragmentChannelsettingsBinding.inflate(inflater);

        mChannelSettingsViewModel = new ChannelSettingsViewModel(mMainActivity, ChannelSettingsFragment.this);
        mFragmentChannelsettingsBinding.setViewModel(mChannelSettingsViewModel);

        init();

        View mView = mFragmentChannelsettingsBinding.getRoot();
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
        mFragmentChannelsettingsBinding.rvlist.setLayoutManager(
                new GridLayoutManager(mMainActivity, 1, RecyclerView.VERTICAL, false));


        mFragmentChannelsettingsBinding.llroot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearOperationView();
            }
        });

        ChanList = new ArrayList<Chan>();
        mChannelAdapter = new ChannelAdapter(mMainActivity, mMainActivity, R.layout.adapter_channel, ChanList);
        mFragmentChannelsettingsBinding.rvlist.setAdapter(mChannelAdapter);
        mFragmentChannelsettingsBinding.rvlist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        mChannelAdapter.setOnItemClickListener(new ChannelAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position, Chan chan, String type) {

                ChanDialog mChanDialog = new ChanDialog(mMainActivity, mMainActivity, ChannelSettingsFragment.this, ChanList.get(position));
                mChanDialog.show();
            }
        });

        mChannelAdapter.setOnItemClickListener(new MultiItemTypeAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, RecyclerView.ViewHolder holder, int position) {
                for (int i = 0; i < ChanList.size(); i++) {
                    Chan mChan = ChanList.get(i);
                    if (mChan.getUid() == ChanList.get(position).getUid()) {
                        mChan.setSelected(1);
                    } else {
                        mChan.setSelected(0);
                    }
                }


//                mChannelAdapter.notifyDataSetChanged();


                ChanDialog mChanDialog = new ChanDialog(mMainActivity, mMainActivity, ChannelSettingsFragment.this, ChanList.get(position));
                mChanDialog.show();
            }

            @Override
            public boolean onItemLongClick(View view, RecyclerView.ViewHolder holder, int position) {

                return false;
            }
        });

        int height = getScreenHeight(mMainActivity);
        int StatusBarHeight = ScrUtil.getStatusBarHeight(mMainActivity);
        int navigationheight = getNavBarHeight(mMainActivity);
        int diff = height - StatusBarHeight - mMainActivity.getHeight() - navigationheight;

        if (diff == 0) {
            int getheight = height - StatusBarHeight - (int) mMainActivity.getResources().getDimension(R.dimen.dp_220) - navigationheight;
            LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) mFragmentChannelsettingsBinding.lllist.getLayoutParams();
            lp.height = getheight;
            mFragmentChannelsettingsBinding.lllist.setLayoutParams(lp);
        } else if (diff < 0) {
            int getheight = height - StatusBarHeight - (int) mMainActivity.getResources().getDimension(R.dimen.dp_220) - navigationheight / 2;
            LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) mFragmentChannelsettingsBinding.lllist.getLayoutParams();
            lp.height = getheight;
            mFragmentChannelsettingsBinding.lllist.setLayoutParams(lp);
        }

        showChannels();
    }


    private void showMenuDialog(View anchorView) {
        // 加载菜单布局
        View popupView = LayoutInflater.from(mMainActivity).inflate(R.layout.modify_channel_dialog, null);

        // 创建 PopupWindow
        PopupWindow popupWindow = new PopupWindow(
                popupView,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true);

        // 设置 PopupWindow 的背景（必要，否则点击外部无法关闭）
        popupWindow.setBackgroundDrawable(mMainActivity.getResources().getDrawable(android.R.drawable.dialog_holo_light_frame));

        // 显示 PopupWindow

        popupWindow.showAsDropDown(anchorView, -mMainActivity.dpToPx(10), 0);


        popupView.findViewById(R.id.cut).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ClipboardManager clipboard = (ClipboardManager) mMainActivity.getSystemService(Context.CLIPBOARD_SERVICE);
                // 创建一个 ClipData 对象，包含你要复制的文本
                EditText editText = (EditText) operationView;
                ClipData clip = ClipData.newPlainText("label", editText.getText().toString());
                // 将文本复制到剪切板
                clipboard.setPrimaryClip(clip);
                editText.setText("");
                editText.requestFocus();
                // 获取输入法管理器
                InputMethodManager imm = (InputMethodManager) mMainActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
                // 显示键盘
                if (imm != null) {
                    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                }
                popupWindow.dismiss();
            }
        });

        popupView.findViewById(R.id.copy).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ClipboardManager clipboard = (ClipboardManager) mMainActivity.getSystemService(Context.CLIPBOARD_SERVICE);
                // 创建一个 ClipData 对象，包含你要复制的文本
                EditText editText = (EditText) operationView;
                ClipData clip = ClipData.newPlainText("label", editText.getText().toString());
                // 将文本复制到剪切板
                clipboard.setPrimaryClip(clip);
                editText.requestFocus();
                // 获取输入法管理器
                InputMethodManager imm = (InputMethodManager) mMainActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
                // 显示键盘
                if (imm != null) {
                    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                }
                popupWindow.dismiss();
            }
        });

        popupView.findViewById(R.id.paste).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ClipboardManager clipboard = (ClipboardManager) mMainActivity.getSystemService(Context.CLIPBOARD_SERVICE);
                // 创建一个 ClipData 对象，包含你要复制的文本
                if (clipboard != null && clipboard.hasPrimaryClip()) {
                    // 获取剪切板中的文本
                    CharSequence clipText = clipboard.getPrimaryClip().getItemAt(0).getText();
                    if (!TextUtils.isEmpty(clipText)) {
                        EditText editText = (EditText) operationView;
                        editText.setText(clipText); // 粘贴到 EditText 中
                    }
                }
                popupWindow.dismiss();
            }
        });

        popupView.findViewById(R.id.modify).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                EditText editText = (EditText) operationView;
                editText.requestFocus();

                mMainActivity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

                // 显示软键盘
                InputMethodManager imm = (InputMethodManager) mMainActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
                }
                // 移动光标到文本末尾
                editText.setSelection(editText.getText().length());
                popupWindow.dismiss();
            }
        });


        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {

            }
        });
    }

    private void clearOperationView() {
        if (operationView == null) return;
        operationView.setBackgroundColor(Color.parseColor("#FFFFFF"));
        operationView.setFocusable(false);
        operationView.setFocusableInTouchMode(false);

        operationView = null;
        modifyChan = null;


    }

    private void setAdapterListener() {
        mChannelAdapter.setOnItemClickListener(new ChannelAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position, Chan chan, String type) {
                mFragmentChannelsettingsBinding.llroot.clearFocus();
                EditText editText = (EditText) view;
                if (operationView != null) clearOperationView();

                view.setBackgroundColor(Color.parseColor("#D9F4EB"));
                view.setFocusable(true);
                view.setFocusableInTouchMode(true);
                editText.setSelection(0, editText.getText().length());
                modifyChan = chan;
                operationView = view;
                showMenuDialog(view);

                editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View view, boolean hasFocus) {
                        if (modifyChan == null) return;
                        // 当 EditText 失去焦点时执行
                        if (!hasFocus) {
                            if (Objects.equals(type, "channel_id")) {
                                modifyChan.setCid(((EditText) view).getText().toString().trim());
                            } else if (Objects.equals(type, "channel_name")) {
                                modifyChan.setNa(((EditText) view).getText().toString().trim());
                            }
                            modify(modifyChan);
                        }
                    }
                });
            }
        });
    }


    public static int getNavBarHeight(Context c) {
        Resources resources = c.getResources();
        int identifier = resources.getIdentifier("navigation_bar_height", "dimen", "android");
        return resources.getDimensionPixelOffset(identifier);
    }

    public static int getScreenHeight(Context context) {
        int dpi = 0;
        Display display = ((Activity) context).getWindowManager()
                .getDefaultDisplay();
        DisplayMetrics dm = new DisplayMetrics();
        @SuppressWarnings("rawtypes")
        Class c;
        try {
            c = Class.forName("android.view.Display");
            @SuppressWarnings("unchecked") Method method = c.getMethod("getRealMetrics", DisplayMetrics.class);
            method.invoke(display, dm);
            dpi = dm.heightPixels;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dpi;
    }

    public void showChannels() {
        ChanList.clear();

        boolean per = PeUtil.ishasPer(mMainActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (per) {
            if (mMainActivity.getMDOpe() == null) {
                DOpe mDOpe = DOpe.Instance(mMainActivity, mMainActivity.getExternalFilesDir(null).getAbsolutePath() + File.separator + Constant.DIR);
                List<Chan> ChanListLocal = (List<Chan>) mDOpe.OperaDatabase(DCon.Chanqueryeffect, "");
                if (ChanListLocal.size() >= 1) {
                    ChanList.addAll(ChanListLocal);
                }
            } else {
                List<Chan> ChanListLocal = (List<Chan>) mMainActivity.getMDOpe().OperaDatabase(DCon.Chanqueryeffect, "");
                if (ChanListLocal.size() >= 1) {
                    ChanList.addAll(ChanListLocal);
                }
            }
        }

        mChannelAdapter.notifyDataSetChanged();
    }

    public void cancel() {
        mChannelAdapter.notifyDataSetChanged();
    }

    public void delete() {
        if (operationView != null) {
            operationView.setBackgroundColor(Color.parseColor("#FFFFFF"));
            operationView = null;
        }

        if (modifyChan != null) {
            cancel();
            for (int i = 0; i < ChanList.size(); i++) {
                Chan mmChan = ChanList.get(i);
                if (mmChan.getUid() == modifyChan.getUid()) {
                    mmChan.setSta(0);
                    ChanList.remove(i);
                    mMainActivity.getMDOpe().OperaDatabase(DCon.Chanupdatedependuid, mmChan);
                    break;
                }
            }
            modifyChan = null;
            mChannelAdapter.notifyDataSetChanged();
        }

    }

    public void delete(Chan mChan) {

        if (ChanList.size() == 1) {
            Toast.makeText(mMainActivity, "通道数最少一路", Toast.LENGTH_SHORT).show();
            return;
        }

        cancel();

        for (int i = 0; i < ChanList.size(); i++) {
            Chan mmChan = ChanList.get(i);
            if (mmChan.getUid() == mChan.getUid()) {
                mmChan.setSta(0);
                ChanList.remove(i);

                mMainActivity.getMDOpe().OperaDatabase(DCon.Chanupdatedependuid, mmChan);
                break;
            }
        }

        mChannelAdapter.notifyDataSetChanged();
    }

    public void modify(Chan mChan) {
        cancel();

        for (int i = 0; i < ChanList.size(); i++) {
            Chan mmChan = ChanList.get(i);
            if (mmChan.getUid() == mChan.getUid()) {
                mmChan.setCid(mChan.getCid());
                mmChan.setNa(mChan.getNa());
                mMainActivity.getMDOpe().OperaDatabase(DCon.Chanupdatedependuid, mmChan);
                break;
            }
        }

        mChannelAdapter.notifyDataSetChanged();
    }
}