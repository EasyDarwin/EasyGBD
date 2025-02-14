package com.easygbs.easygbd.fragment;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.easygbs.easygbd.R;
import com.easygbs.easygbd.common.Constant;
import com.easygbs.easygbd.databinding.FragmentBasicsettingsBinding;
import com.easygbs.easygbd.activity.MainActivity;
import com.easygbs.easygbd.logger.LogLevel;
import com.easygbs.easygbd.logger.Logger;
import com.easygbs.easygbd.util.PeUtil;
import com.easygbs.easygbd.push.MediaStream;
import com.easygbs.easygbd.util.SPHelper;

import org.easydarwin.util.SPUtil;

import com.easygbs.easygbd.viewmodel.fragment.BasicSettingsViewModel;
import com.easygbs.easygbd.service.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;


import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;

public class BasicSettingsFragment extends Fragment {
    private String TAG = BasicSettingsFragment.class.getSimpleName();
    private MainActivity mMainActivity;
    public FragmentBasicsettingsBinding mFragmentBasicsettingsBinding;
    private BasicSettingsViewModel mBasicSettingsViewModel;

    private int width = 1920;
    private int height = 1080;

    private BackgroundCameraService mService;
    private ServiceConnection conn;

    private printThread mprintThread = null;

    public static boolean running = true;

    public List<String> logList = new ArrayList<String>();

    public Intent intent;

    public Intent intent1;

    public Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constant.OPENCAMERA:
                    initCamera();
                    break;
                case Constant.LOG:
                    if (logList.size() >= 6) {
                        logList.remove(0);
                    }

                    logList.add(msg.obj.toString());

                    String str = "";
                    for (int i = 0; i < logList.size(); i++) {
                        str += logList.get(i) + "\n\n";
                    }

                    mFragmentBasicsettingsBinding.tvlog.setText(str);
                    break;
                case Constant.STARTPRINTLOG:
                    SPHelper mSPHelper = mMainActivity.getSPHelper();
                    int logstatus = (int) mSPHelper.get(Constant.LOGSTATUS, 1);
                    if (logstatus == 1) {
                        running = true;
                        String loglev = (String) mSPHelper.get(Constant.LOGLEV, "DEBUG");
                        start(loglev);
                    }
                    break;
                case Constant.STARTORSTOPSTREAM:
                    startOrStopStream();
                    break;
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
        mFragmentBasicsettingsBinding = FragmentBasicsettingsBinding.inflate(inflater);

        mBasicSettingsViewModel = new BasicSettingsViewModel(mMainActivity, BasicSettingsFragment.this);
        mFragmentBasicsettingsBinding.setViewModel(mBasicSettingsViewModel);

        init();

        View mView = mFragmentBasicsettingsBinding.getRoot();
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
        stop();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    public void init() {
        mFragmentBasicsettingsBinding.lllogall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SPHelper mSPHelper = mMainActivity.getSPHelper();
                int logstatus = (int) mSPHelper.get(Constant.LOGSTATUS, 1);
                if (logstatus == 0) {
                    mSPHelper.put(Constant.LOGSTATUS, 1);

                    mFragmentBasicsettingsBinding.lllog.setBackgroundResource(R.mipmap.ic_selected);

                    mFragmentBasicsettingsBinding.ivlog.setVisibility(View.VISIBLE);

                    Logger.getInstance().startLogging();


                } else {
                    mSPHelper.put(Constant.LOGSTATUS, 0);

                    mFragmentBasicsettingsBinding.lllog.setBackgroundResource(R.mipmap.ic_notselected);

                    Logger.getInstance().stopLogging();

                }
            }
        });

        boolean hasPermission1 = PeUtil.ishasPer(mMainActivity, Manifest.permission.CAMERA);
        if (hasPermission1) {
            boolean hasPermission2 = PeUtil.ishasPer(mMainActivity, Manifest.permission.RECORD_AUDIO);
            if (hasPermission2) {
                initCamera();
            }
        }


        SPHelper mSPHelper = mMainActivity.getSPHelper();

        int logstatus = (int) mSPHelper.get(Constant.LOGSTATUS, 1);

        if (logstatus == 1) {
            String loglev = (String) mSPHelper.get(Constant.LOGLEV, LogLevel.DEBUG);

            // 设置全局 Logger 的 logTextView 和 logScrollView
            Logger.getInstance().setLogTextView(mFragmentBasicsettingsBinding.tvlog, mFragmentBasicsettingsBinding.svLog);

            // 设置日志等级
            Logger.getInstance().setLogLevel(LogLevel.DEBUG);

            // 记录不同等级的日志
            Logger.getInstance().debug("-----begin-----");
        }

        //浮动按钮
        mFragmentBasicsettingsBinding.fab.setOnTouchListener(new View.OnTouchListener() {
            private float dX, dY;
            private long startTime = 0;
            private static final int CLICK_THRESHOLD = 20;  // 增大阈值
            private static final int CLICK_TIME_THRESHOLD = 300; // 最大点击时间

            @Override
            public boolean onTouch(View view, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        // 记录按下时的坐标和时间
                        dX = event.getRawX() - view.getX();
                        dY = event.getRawY() - view.getY();
                        startTime = System.currentTimeMillis(); // 记录按下时间
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        // 计算新的坐标
                        float newX = event.getRawX() - dX;
                        float newY = event.getRawY() - dY;
                        view.animate().x(newX).y(newY).setDuration(0).start();
                        return true;

                    case MotionEvent.ACTION_UP:
                        // 获取按下到松开时的坐标差距
                        float deltaX = Math.abs(event.getRawX() - (view.getX() + dX));
                        float deltaY = Math.abs(event.getRawY() - (view.getY() + dY));  // 使用View当前位置加上偏移量

                        // 获取按下到释放的时间差
                        long upTime = System.currentTimeMillis();
                        long time = upTime - startTime;

                        // 打印调试信息
//                        Log.d("TouchEvent", "deltaX: " + deltaX + ", deltaY: " + deltaY + ", time: " + time);

                        // 判断是否为点击事件
                        if (deltaX < CLICK_THRESHOLD && deltaY < CLICK_THRESHOLD && time < CLICK_TIME_THRESHOLD) {
                            // 点击事件
//                            Log.d("TouchEvent", "Detected click");
                            view.performClick();  // 手动触发点击事件
                        }
                        return true;

                    default:
                        return false;
                }
            }
        });
    }

    public void start(String lev) {
        if (mprintThread == null) {
            int pid = android.os.Process.myPid();
            mprintThread = new printThread(String.valueOf(pid), lev, new printThread.info() {
                @Override
                public void msg(String value) {
                    super.msg(value);
                    Message msg = new Message();
                    msg.what = Constant.LOG;
                    msg.obj = value;
                    mHandler.sendMessage(msg);
                }
            });
            mprintThread.start();
        }
    }

    public void stop() {
        mFragmentBasicsettingsBinding.tvlog.setText("");
        if (mprintThread != null) {
            mprintThread.stopt();
            mprintThread = null;
        }
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

                            if (mFragmentBasicsettingsBinding.tvcamera.isAvailable()) {
                                goonWithAvailableTexture(mFragmentBasicsettingsBinding.tvcamera.getSurfaceTexture());
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

    public void destroyService() {
        mMainActivity.stopService(intent);
        mMainActivity.stopService(intent1);
        mMainActivity.unbindService(conn);
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
            height = 960;
        } else if (videoresolution == 2) {
            width = 1280;
            height = 720;
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

    public void save() {
        try {
            String port = mFragmentBasicsettingsBinding.etport.getText().toString();
            SPUtil.setServerport(mMainActivity, Integer.parseInt(port));

            String serveraddr = mFragmentBasicsettingsBinding.etsipserveraddr.getText().toString();
            SPUtil.setServerip(mMainActivity, serveraddr);

            String serverid = mFragmentBasicsettingsBinding.etsipserverid.getText().toString();
            SPUtil.setServerid(mMainActivity, serverid);

            String serverdomain = mFragmentBasicsettingsBinding.etsipserverdomain.getText().toString();
            SPUtil.setServerdomain(mMainActivity, serverdomain);

            String sipname = mFragmentBasicsettingsBinding.tvsipname.getText().toString();
            SPUtil.setDeviceid(mMainActivity, sipname);

            String devicename = mFragmentBasicsettingsBinding.etdevicename.getText().toString();
            SPUtil.setDevicename(mMainActivity, devicename);

            String sippassword = mFragmentBasicsettingsBinding.etsippassword.getText().toString();
            SPUtil.setPassword(mMainActivity, sippassword);

            String registervalidtime = mFragmentBasicsettingsBinding.etregistervalidtime.getText().toString();
            SPUtil.setRegexpires(mMainActivity, Integer.parseInt(registervalidtime));

            String heartbeatcycle = mFragmentBasicsettingsBinding.tvheartbeatcycle.getText().toString();
            SPUtil.setHeartbeatinterval(mMainActivity, Integer.parseInt(heartbeatcycle));

            String heartbeatcount = mFragmentBasicsettingsBinding.etheartbeatcount.getText().toString();
            SPUtil.setHeartbeatcount(mMainActivity, Integer.parseInt(heartbeatcount));
        } catch (Exception e) {
            Log.e(TAG, "save  Exception  " + e.toString());
        }
    }

    public void startOrStopStream() {
        try {
            if (mMainActivity.getMMediaStream() != null) {
                if (!mMainActivity.getMMediaStream().isStreaming()) {
                    mMainActivity.getMMediaStream().startStream();
                } else {
                    mMainActivity.getMMediaStream().stopStream();
                    Logger.getInstance().debug("注销");
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "startOrStopStream  Exception  " + e.toString());
        }
    }

    static class printThread extends Thread {
        public String TAG = printThread.class.getSimpleName();
        private BufferedReader mBufferedReader = null;
        private Object lock = new Object();
        private String cmd = null;
        private Process process;
        private String mPID;
        private String lev;
        private info minfo;

        public printThread(String pid, String lev, info minfo) {
            mPID = pid;
            this.lev = lev;
            this.minfo = minfo;

            if (lev.equals("DEBUG")) {
                cmd = "logcat *:d | grep \"(" + mPID + ")\"";
            } else if (lev.equals("INFO")) {
                cmd = "logcat *:i | grep \"(" + mPID + ")\"";
            } else if (lev.equals("WARNING")) {
                cmd = "logcat *:w | grep \"(" + mPID + ")\"";
            } else if (lev.equals("ERROR")) {
                cmd = "logcat *:e | grep \"(" + mPID + ")\"";
            }
        }

        public void stopt() {
            synchronized (lock) {
                lock.notify();
                running = false;
            }
        }

        @Override
        public void run() {
            try {
                super.run();
                process = Runtime.getRuntime().exec(cmd);
                InputStream is = process.getInputStream();
                InputStreamReader isr = new InputStreamReader(is);
                mBufferedReader = new BufferedReader(isr, 1024);
                String line = null;

                synchronized (lock) {
                    while (running && ((line = mBufferedReader.readLine()) != null)) {
                        if (!running) {
                            break;
                        }

                        if (line.length() == 0) {
                            continue;
                        }

                        minfo.msg(line);

                        lock.wait(500);
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "run  Exception  " + e.toString());
            } finally {
                try {
                    if (process != null) {
                        process.destroy();
                        process = null;
                    }

                    if (mBufferedReader != null) {
                        mBufferedReader.close();
                        mBufferedReader = null;
                    }
                } catch (Exception e) {
                    Log.e(TAG, "run  finally  Exception  " + e.toString());
                }
            }
        }

        static class info {
            public void msg(String value) {

            }
        }
    }
}