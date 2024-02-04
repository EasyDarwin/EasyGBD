/*
	Copyright (c) 2013-2016 EasyDarwin.ORG.  All rights reserved.
	Github: https://github.com/EasyDarwin
	WEChat: EasyDarwin
	Website: http://www.easydarwin.org
*/

package com.easygbs.device;

import android.content.DialogInterface;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import com.easygbs.device.databinding.ActivitySettingBinding;
import com.easygbs.device.util.DataUtil;
import com.easygbs.device.util.SPUtil;

import org.easydarwin.util.SIP;

/**
 * 设置页
 */
public class SettingActivity extends AppCompatActivity implements Toolbar.OnMenuItemClickListener {

    public static final int REQUEST_OVERLAY_PERMISSION = 1004;  // 悬浮框

    private ActivitySettingBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_setting);

        setSupportActionBar(binding.mainToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        binding.mainToolbar.setOnMenuItemClickListener(this);
        // 左边的小箭头（注意需要在setSupportActionBar(toolbar)之后才有效果）
        binding.mainToolbar.setNavigationIcon(R.drawable.com_back);

        SIP sip = DataUtil.getSIP();
        binding.sipServerIp.setText(sip.getServerIp());
        binding.sipServerPort.setText(String.valueOf(sip.getServerPort()));
        binding.localSipPort.setText(String.valueOf(sip.getLocalSipPort()));
        binding.sipServerId.setText(sip.getServerId());
        binding.sipServerDomain.setText(sip.getServerDomain());
        binding.sipDeviceId.setText(sip.getDeviceId());
        binding.sipPassword.setText(sip.getPassword());
        binding.sipRegExpires.setText(String.valueOf(sip.getRegExpires()));
        binding.sipHeartbeatInterval.setText(String.valueOf(sip.getHeartbeatInterval()));
        binding.sipHeartbeatCount.setText(String.valueOf(sip.getHeartbeatCount()));

        if (sip.getProtocol() == SIP.ProtocolEnum.UDP.getValue()) {
            binding.sipProtocolUdp.setChecked(true);
        } else {
            binding.sipProtocolTcp.setChecked(true);
        }

//        // 使能摄像头后台采集
//        CheckBox backgroundPushing = (CheckBox) findViewById(R.id.enable_background_camera_pushing);
//        backgroundPushing.setChecked(SPUtil.getEnableBackgroundCamera(this));
//        backgroundPushing.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(final CompoundButton buttonView, boolean isChecked) {
//                if (isChecked) {
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                        if (Settings.canDrawOverlays(SettingActivity.this)) {
//                            SPUtil.setEnableBackgroundCamera(SettingActivity.this, true);
//                        } else {
//                            new AlertDialog
//                                    .Builder(SettingActivity.this)
//                                    .setTitle("后台上传视频")
//                                    .setMessage("后台上传视频需要APP出现在顶部.是否确定?")
//                                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
//                                        @Override
//                                        public void onClick(DialogInterface dialogInterface, int i) {
//                                            // 在Android 6.0后，Android需要动态获取权限，若没有权限，提示获取.
//                                            final Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + BuildConfig.APPLICATION_ID));
//                                            startActivityForResult(intent, REQUEST_OVERLAY_PERMISSION);
//                                        }
//                                    }).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
//                                        @Override
//                                        public void onClick(DialogInterface dialogInterface, int i) {
//                                            SPUtil.setEnableBackgroundCamera(SettingActivity.this, false);
//                                            buttonView.toggle();
//                                        }
//                                    })
//                                    .setCancelable(false)
//                                    .show();
//                        }
//                    } else {
//                        SPUtil.setEnableBackgroundCamera(SettingActivity.this, true);
//                    }
//                } else {
//                    SPUtil.setEnableBackgroundCamera(SettingActivity.this, false);
//                }
//            }
//        });

        // 是否使用软编码
        CheckBox x264enc = findViewById(R.id.use_x264_encode);
        x264enc.setChecked(SPUtil.getswCodec(this));
        x264enc.setOnCheckedChangeListener(
                (buttonView, isChecked) -> SPUtil.setswCodec(this, isChecked)
        );

//        // 使能H.265编码
//        CheckBox enable_hevc_cb = findViewById(R.id.enable_hevc);
//        enable_hevc_cb.setChecked(SPUtil.getHevcCodec(this));
//        enable_hevc_cb.setOnCheckedChangeListener(
//                (buttonView, isChecked) -> SPUtil.setHevcCodec(this, isChecked)
//        );

        // 使能H.265编码
        CheckBox enable_aac_cb = findViewById(R.id.enable_aac);
        enable_aac_cb.setChecked(SPUtil.getAACCodec(this));
        enable_aac_cb.setOnCheckedChangeListener(
                (buttonView, isChecked) -> SPUtil.setAACCodec(this, isChecked)
        );

        // 叠加水印
        CheckBox enable_video_overlay = findViewById(R.id.enable_video_overlay);
        enable_video_overlay.setChecked(SPUtil.getEnableVideoOverlay(this));
        enable_video_overlay.setOnCheckedChangeListener(
                (buttonView, isChecked) -> SPUtil.setEnableVideoOverlay(this, isChecked)
        );

        // 推送内容
        RadioGroup push_content = findViewById(R.id.push_content);

        boolean videoEnable = SPUtil.getEnableVideo(this);
        if (videoEnable) {
            boolean audioEnable = SPUtil.getEnableAudio(this);

            if (audioEnable) {
                RadioButton push_av = findViewById(R.id.push_av);
                push_av.setChecked(true);
            } else {
                RadioButton push_v = findViewById(R.id.push_v);
                push_v.setChecked(true);
            }
        } else {
            RadioButton push_a = findViewById(R.id.push_a);
            push_a.setChecked(true);
        }

        push_content.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.push_av) {
                    SPUtil.setEnableVideo(SettingActivity.this, true);
                    SPUtil.setEnableAudio(SettingActivity.this, true);
                } else if (checkedId == R.id.push_a) {
                    SPUtil.setEnableVideo(SettingActivity.this, false);
                    SPUtil.setEnableAudio(SettingActivity.this, true);
                } else if (checkedId == R.id.push_v) {
                    SPUtil.setEnableVideo(SettingActivity.this, true);
                    SPUtil.setEnableAudio(SettingActivity.this, false);
                }
            }
        });

        SeekBar sb = findViewById(R.id.bitrate_seekbar);
        final TextView bitrateValue = findViewById(R.id.bitrate_value);

        int bitrate_added_kbps = SPUtil.getBitrateKbps(this);
        int kbps = 72000 + bitrate_added_kbps;
        bitrateValue.setText(kbps / 1000 + "kbps");

        sb.setMax(5000000);
        sb.setProgress(bitrate_added_kbps);
        sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int kbps = 72000 + progress;
                bitrateValue.setText(kbps / 1000 + "kbps");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                SPUtil.setBitrateKbps(SettingActivity.this, seekBar.getProgress());
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();

        SIP sip = new SIP();
        sip.setServerIp(binding.sipServerIp.getText().toString());
        sip.setServerId(binding.sipServerId.getText().toString());
        sip.setServerDomain(binding.sipServerDomain.getText().toString());
        sip.setDeviceId(binding.sipDeviceId.getText().toString());
        sip.setPassword(binding.sipPassword.getText().toString());
        sip.setServerPort(Integer.parseInt(binding.sipServerPort.getText().toString()));
        sip.setLocalSipPort(Integer.parseInt(binding.localSipPort.getText().toString()));
        sip.setRegExpires(Integer.parseInt(binding.sipRegExpires.getText().toString()));
        sip.setHeartbeatInterval(Integer.parseInt(binding.sipHeartbeatInterval.getText().toString()));
        sip.setHeartbeatCount(Integer.parseInt(binding.sipHeartbeatCount.getText().toString()));

        if (binding.sipProtocolUdp.isChecked()) {
            sip.setProtocol(SIP.ProtocolEnum.UDP.getValue());
        } else {
            sip.setProtocol(SIP.ProtocolEnum.TCP.getValue());
        }

        DataUtil.setSIP(sip);
    }

    /**
     * 本地录像
     */
    public void onOpenLocalRecord(View view) {
        Intent intent = new Intent(this, MediaFilesActivity.class);
        startActivityForResult(intent, 0);
        overridePendingTransition(R.anim.slide_right_in, R.anim.slide_left_out);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

//        if (requestCode == REQUEST_OVERLAY_PERMISSION) {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                boolean canDraw = Settings.canDrawOverlays(this);
//                SPUtil.setEnableBackgroundCamera(SettingActivity.this, canDraw);
//
//                if (!canDraw) {
//                    CheckBox backgroundPushing = (CheckBox) findViewById(R.id.enable_background_camera_pushing);
//                    backgroundPushing.setChecked(false);
//                }
//            }
//        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onMenuItemClick(MenuItem menuItem) {
        return false;
    }

    // 返回的功能
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
