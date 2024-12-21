package com.easygbs.easygbd.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.easygbs.easygbd.R;
import com.easygbs.easygbd.activity.MainActivity;
import com.easygbs.easygbd.dao.bean.Chan;
import com.easygbs.easygbd.fragment.ChannelSettingsFragment;

public class ChanDialog extends Dialog {
    public String TAG = ChanDialog.class.getSimpleName();
    public Context mContext;
    public MainActivity mMainActivity;
    public ChannelSettingsFragment mChannelSettingsFragment;
    public Chan mChan;
    public View mContextView;
    public LayoutInflater mInflater;

    public EditText etchanid;

    public EditText etchanna;

    public Button btnsu;

    public TextView btnca;

    public TextView btnde;

    public static final int DISMISS = 1;

    public Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case DISMISS:
                    ChanDialog.this.dismiss();
                    break;
            }
        }
    };

    public ChanDialog(Context mContext, MainActivity mMainActivity, ChannelSettingsFragment mChannelSettingsFragment, Chan mChan) {
        this(mContext, R.style.dialog_style);
        this.mContext = mContext;
        this.mMainActivity = mMainActivity;
        this.mChannelSettingsFragment = mChannelSettingsFragment;
        this.mChan = mChan;
    }

    public ChanDialog(Context context, int theme) {
        super(context, theme);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ChanDialog.this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        init(mContext);
        Window window = ChanDialog.this.getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();

        lp.width = (int) (mMainActivity.getWidth() - mMainActivity.getResources().getDimension(R.dimen.dp_40));
        lp.height = (int) (mMainActivity.getResources().getDimension(R.dimen.dp_220));
        lp.alpha = (float) 1.0;
        window.setAttributes(lp);
        window.setGravity(Gravity.CENTER);
    }

    private void init(Context context) {
        this.mContext = context;
        mInflater = LayoutInflater.from(mContext);
        mContextView = mInflater.inflate(R.layout.dialog_chan, null, false);

        setContentView(mContextView);

        etchanid = mContextView.findViewById(R.id.etchanid);

        etchanna = mContextView.findViewById(R.id.etchanna);

        btnsu = mContextView.findViewById(R.id.btnsu);
        btnsu.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {

                String id = etchanid.getText().toString().trim();

                if (id.length() != 20) {
                    Toast.makeText(context, "通道ID长度必须20位!!!", Toast.LENGTH_SHORT).show();
                    return;
                }
                String na = etchanna.getText().toString().trim();
                mChan.setCid(id);
                mChan.setNa(na);
                mChannelSettingsFragment.modify(mChan);

                mHandler.sendEmptyMessage(DISMISS);
            }
        });

        btnca = mContextView.findViewById(R.id.btnca);
        btnca.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                mChannelSettingsFragment.cancel();

                mHandler.sendEmptyMessage(DISMISS);
            }
        });

        btnde = mContextView.findViewById(R.id.btnde);
        btnde.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {

                mChannelSettingsFragment.delete(mChan);

                mHandler.sendEmptyMessage(DISMISS);
            }
        });

        etchanid.setText(mChan.getCid());

        etchanna.setText(mChan.getNa());

        ChanDialog.this.setCancelable(false);
    }
}