package com.easygbs.easygbd.push;

import org.easydarwin.util.RecordStatusListener;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaFormat;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.Process;
import android.util.Log;

import com.easygbs.easygbd.common.Constant;
import com.easygbs.easygbd.util.SPHelper;

import org.easydarwin.util.SPUtil;

import com.easygbs.easygbd.util.SipUtil;
import com.easygbs.Device;
import com.serenegiant.usb.IFrameCallback;
import com.serenegiant.usb.UVCCamera;

import org.easydarwin.bus.StartRecord;
import org.easydarwin.bus.StopRecord;
import org.easydarwin.bus.SupportResolution;
import org.easydarwin.common.EasyGBDConstant;
import org.easydarwin.encode.AudioStream;
import org.easydarwin.encode.ClippableVideoConsumer;
import org.easydarwin.encode.HWConsumer;
import org.easydarwin.encode.SWConsumer;
import org.easydarwin.encode.VideoConsumer;
import org.easydarwin.muxer.EasyMuxer;
import org.easydarwin.muxer.RecordVideoConsumer;
import org.easydarwin.push.Pusher;
import org.easydarwin.sw.JNIUtil;
import org.easydarwin.util.Util;
import org.greenrobot.eventbus.EventBus;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.PriorityQueue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import static android.media.AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
import static android.media.MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420PackedPlanar;
import static android.media.MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar;
import static android.media.MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar;
import static android.media.MediaCodecInfo.CodecCapabilities.COLOR_TI_FormatYUV420PackedSemiPlanar;

import com.easygbs.easygbd.service.*;

public class MediaStream {
    private static final String TAG = MediaStream.class.getSimpleName();
    private static final int SWITCH_CAMERA = 11;

    private final boolean enableVideo;
    private boolean mSWCodec, mHevc;    // mSWCodec是否软编码, mHevc是否H265

    private String recordPath;          // 录像地址
    boolean isPushStream = false;       // 是否要推送数据
    private int displayRotationDegree;  // 旋转角度

    private Context context;
    WeakReference<SurfaceTexture> mSurfaceHolderRef;

    private VideoConsumer mVC, mRecordVC;
    private AudioStream audioStream;
    private EasyMuxer mEasyMuxer;
    private Pusher mEasyPusher;

    private final HandlerThread mCameraThread;
    private final Handler mCameraHandler;

    private TheRecordStatusListener mTheRecordStatusListener = null;

    private UVCCamera uvcCamera;

    BlockingQueue<byte[]> cache = new ArrayBlockingQueue<byte[]>(100);

    public SimpleDateFormat ymdhm = new SimpleDateFormat("yyyyMMddHHmmss", Locale.CHINA);

    /*
     0:Camera.CameraInfo.CAMERA_FACING_BACK
     1:Camera.CameraInfo.CAMERA_FACING_FRONT
     CAMERA_FACING_BACK_UVC
    */
    int mCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
    public static final int CAMERA_FACING_BACK_UVC = 2;
    public static final int CAMERA_FACING_BACK_LOOP = -1;

    private int frameWidth;
    private int frameHeight;

    int defaultWidth = 1920;
    int defaultHeight = 1080;

    private int mTargetCameraId;

    //通道ID
    private int channelid = 0;

    //录像时间
    private long durationMillis = 5 * 60 * 1000; //五分钟

    Camera mCamera;
    private Camera.CameraInfo camInfo;
    private Camera.Parameters parameters;
    private byte[] i420_buffer;

    public MediaStream(Context context, SurfaceTexture texture, boolean enableVideo) {
        this.context = context;
        audioStream = AudioStream.getInstance(context);
        mSurfaceHolderRef = new WeakReference(texture);

        mTheRecordStatusListener = new TheRecordStatusListener();

        mCameraThread = new HandlerThread("CAMERA") {
            public void run() {
                try {
                    super.run();
                } catch (Throwable e) {
                    Log.e(TAG, "HandlerThread  Throwable   " + e.toString());

                    Intent intent = new Intent(context, BackgroundCameraService.class);
                    context.stopService(intent);
                } finally {
                    Log.i(TAG, "HandlerThread  finally");
                    stopStream();
                    stopPreview();
                    destroyCamera();
                }
            }
        };

        mCameraThread.start();

        mCameraHandler = new Handler(mCameraThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);

                if (msg.what == SWITCH_CAMERA) {
                    switchCameraTask.run();
                }
            }
        };

        this.enableVideo = enableVideo;
    }

    public void createCamera() {
        if (Thread.currentThread() != mCameraThread) {
            mCameraHandler.post(() -> {
                Process.setThreadPriority(Process.THREAD_PRIORITY_DEFAULT);
                createCamera();
            });
            return;
        }

        mSWCodec = SPUtil.getswCodec(context);
        mHevc = SPUtil.getHevcCodec(context);

        mEasyPusher = new Device(context);
        ((Device) mEasyPusher).setCallback(new Device.OnInitPusherCallback() {

            @Override
            public void onCallback(int channel, int code, String name) {
                Log.i(TAG, "onCallback  channel  " + channel + "  code  " + code + "  name  " + name);
                EventBus.getDefault().post(new PushCallback(channelid, code, name));
            }

            @Override
            public void onSourceCallBack(Device.FrameInfo frameInfo) {
                try {
                    mQueue.put(frameInfo);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

//                Log.i(TAG, String.format("audio queue size :%d", mQueue.size()));
            }

        });

        if (!enableVideo) {
            return;
        }

        if (mCameraId == CAMERA_FACING_BACK_UVC) {
            createUvcCamera();
        } else {
            createNativeCamera();
        }
    }

    private void createNativeCamera() {
        try {
            mCamera = Camera.open(mCameraId);
            mCamera.setErrorCallback((i, camera) -> {
                throw new IllegalStateException("Camera Error:" + i);
            });

            parameters = mCamera.getParameters();

            if (Util.getSupportResolution(context).size() == 0) {
                StringBuilder stringBuilder = new StringBuilder();

                List<Camera.Size> supportedPreviewSizes = parameters.getSupportedPreviewSizes();

                for (Camera.Size str : supportedPreviewSizes) {
                    stringBuilder.append(str.width + "x" + str.height).append(";");
                }

                Util.saveSupportResolution(context, stringBuilder.toString());
            }

            EventBus.getDefault().post(new SupportResolution());

            camInfo = new Camera.CameraInfo();
            Camera.getCameraInfo(mCameraId, camInfo);
            int cameraRotationOffset = camInfo.orientation;

            if (mCameraId == Camera.CameraInfo.CAMERA_FACING_FRONT)
                cameraRotationOffset += 180;

            int rotate = (360 + cameraRotationOffset - displayRotationDegree) % 360;
            parameters.setRotation(rotate);
            ArrayList<CodecInfo> infos = listEncoders(mHevc ? MediaFormat.MIMETYPE_VIDEO_HEVC : MediaFormat.MIMETYPE_VIDEO_AVC);

            if (!infos.isEmpty()) {
                CodecInfo ci = infos.get(0);
                info.mName = ci.mName;
                info.mColorFormat = ci.mColorFormat;
            } else {
                mSWCodec = true;
            }

            int videoresolution = SPUtil.getVideoresolution(context);
            if (videoresolution == 0) {
                defaultWidth = 1920;
                defaultHeight = 1080;
            } else if (videoresolution == 1) {
                defaultWidth = 1280;
                defaultHeight = 960;
            } else if (videoresolution == 2) {
                defaultWidth = 1280;
                defaultHeight = 720;
            }

            boolean isfind = false;
            List<Camera.Size> sizes = parameters.getSupportedPreviewSizes();
            for (int i = 0; i < sizes.size(); i++) {
                Camera.Size mSize = sizes.get(i);
                if ((mSize.width == defaultWidth) && (mSize.height == defaultHeight)) {
                    isfind = true;
                    break;
                }
            }

            if (!isfind) {
                defaultWidth = sizes.get(0).width;
                defaultHeight = sizes.get(0).height;
            }

            parameters.setPreviewSize(defaultWidth, defaultHeight);

            int[] ints = determineMaximumSupportedFramerate(parameters);
            parameters.setPreviewFpsRange(ints[0], ints[1]);

            List<String> supportedFocusModes = parameters.getSupportedFocusModes();

            if (supportedFocusModes == null)
                supportedFocusModes = new ArrayList<>();

            if (supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            } else if (supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
            }

            mCamera.setParameters(parameters);

            int displayRotation;
            displayRotation = (cameraRotationOffset - displayRotationDegree + 360) % 360;
            mCamera.setDisplayOrientation(displayRotation);
        } catch (Exception e) {
            Log.e(TAG, "createNativeCamera  Exception  " + e.toString());

            destroyCamera();
        }
    }

    private void createUvcCamera() {
        frameWidth = defaultWidth;
        frameHeight = defaultHeight;

        uvcCamera = UVCCameraService.liveData.getValue();
        if (uvcCamera != null) {
            uvcCamera.setPreviewSize(frameWidth,
                    frameHeight,
                    1,
                    30,
                    UVCCamera.PIXEL_FORMAT_YUV420SP, 1.0f);
        }

        if (uvcCamera == null) {
            mCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
            createNativeCamera();
        }
    }

    public synchronized void destroyCamera() {
        if (Thread.currentThread() != mCameraThread) {
            mCameraHandler.post(() -> destroyCamera());
            return;
        }

        if (uvcCamera != null) {
            uvcCamera.destroy();
            uvcCamera = null;
        }

        if (mCamera != null) {
            mCamera.stopPreview();

            try {
                mCamera.release();
            } catch (Exception e) {
                e.printStackTrace();
            }

            mCamera = null;
        }

        if (mEasyMuxer != null) {
            mEasyMuxer.release();
            mEasyMuxer = null;
        }
    }

    //回收线程
    public void release() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            mCameraThread.quitSafely();
        } else {
            if (!mCameraHandler.post(() -> mCameraThread.quit())) {
                mCameraThread.quit();
            }
        }

        try {
            mCameraThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public synchronized void startPreview() {
        if (Thread.currentThread() != mCameraThread) {
            mCameraHandler.post(() -> startPreview());
            return;
        }

        if (uvcCamera != null) {
//            Log.i(TAG, "uvcCamera != null");
            startUvcPreview();
        } else if (mCamera != null) {
//            Log.i(TAG, "mCamera != null");
            startCameraPreview();
        }

        SPHelper mSPHelper = SPHelper.instance(context);
        Constant.Root = (String) mSPHelper.get(Constant.ROOT, "");
        if (Constant.Root.equals("")) {
            Constant.Root = context.getExternalFilesDir(null).getAbsolutePath() + File.separator + Constant.DIR;
            mSPHelper.put(Constant.ROOT, Constant.Root);

            File rootdir = new File(Constant.Root);
            if (!rootdir.exists()) {
                rootdir.mkdirs();
            }
        }
        mHevc = SPUtil.getHevcCodec(context);
        if (mSWCodec) {
            SWConsumer sw = new SWConsumer(context, mEasyPusher, SPUtil.getBitrateKbps(context), channelid, Constant.Root);
            mVC = new ClippableVideoConsumer(context, sw, frameWidth, frameHeight, SPUtil.getEnableVideoOverlay(context));
        } else {
            HWConsumer hw = new HWConsumer(context,
                    mHevc ? MediaFormat.MIMETYPE_VIDEO_HEVC : MediaFormat.MIMETYPE_VIDEO_AVC,
                    mEasyPusher,
                    SPUtil.getBitrateKbps(context),
                    info.mName,
                    info.mColorFormat,
                    channelid, Constant.Root);
            mVC = new ClippableVideoConsumer(context, hw, frameWidth, frameHeight, SPUtil.getEnableVideoOverlay(context));
        }

        if (uvcCamera != null || mCamera != null) {
            mVC.onVideoStart(frameWidth, frameHeight);
        }

        audioStream.setEnableAudio(SPUtil.getEnableAudio(context));
        audioStream.addPusher(mEasyPusher);
    }

    private void startUvcPreview() {
        SurfaceTexture holder = mSurfaceHolderRef.get();
        if (holder != null) {
            uvcCamera.setPreviewTexture(holder);
        }

        try {
            uvcCamera.setFrameCallback(uvcFrameCallback, UVCCamera.PIXEL_FORMAT_YUV420SP/*UVCCamera.PIXEL_FORMAT_NV21*/);
            uvcCamera.startPreview();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private void startCameraPreview() {
        int previewFormat = parameters.getPreviewFormat();

        Camera.Size previewSize = parameters.getPreviewSize();
        int size = previewSize.width * previewSize.height * ImageFormat.getBitsPerPixel(previewFormat) / 8;

        defaultWidth = previewSize.width;
        defaultHeight = previewSize.height;

        mCamera.addCallbackBuffer(new byte[size]);
        mCamera.addCallbackBuffer(new byte[size]);
        mCamera.setPreviewCallbackWithBuffer(previewCallback);


        try {
            // TextureView的
            SurfaceTexture holder = mSurfaceHolderRef.get();

            if (holder != null) {
                mCamera.setPreviewTexture(holder);
//                Log.i(TAG, "setPreviewTexture");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        mCamera.startPreview();

        boolean frameRotate;
        int result;

        if (camInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (camInfo.orientation + displayRotationDegree) % 360;
        } else {  // back-facing
            result = (camInfo.orientation - displayRotationDegree + 360) % 360;
        }

        frameRotate = result % 180 != 0;

        frameWidth = frameRotate ? defaultHeight : defaultWidth;
        frameHeight = frameRotate ? defaultWidth : defaultHeight;
    }

    public synchronized void stopPreview() {
        if (Thread.currentThread() != mCameraThread) {
            mCameraHandler.post(() -> stopPreview());
            return;
        }

        if (uvcCamera != null) {
            uvcCamera.stopPreview();
        }

        //关闭摄像头
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.setPreviewCallbackWithBuffer(null);
        }

        //关闭音频采集和音频编码器
        if (audioStream != null) {
            audioStream.removePusher(mEasyPusher);
            audioStream.setMuxer(null);
        }

        //关闭视频编码器
        if (mVC != null) {
            mVC.onVideoStop();
        }

        //关闭录像的编码器
        if (mRecordVC != null) {
            mRecordVC.onVideoStop();
        }

        //关闭音视频合成器
        if (mEasyMuxer != null) {
            mEasyMuxer.release();
            mEasyMuxer = null;
        }
    }

    //开始推流
    public void startStream() throws IOException {
        try {
            mEasyPusher.initPush(SipUtil.getInstance(context).getSIP());
            isPushStream = true;

            mQueue.clear();
            startAudio(); //启动对讲
        } catch (Exception ex) {
            Log.e(TAG, "startStream  Exception  " + ex.toString());
        }
    }

    //停止推流
    public void stopStream() {
        Log.i(TAG, "stopStream");
        if (mEasyPusher != null) {
            mEasyPusher.stop();
        }

        isPushStream = false;

        if (mAudioThread != null) {
            mAudioThread.interrupt();
            try {
                mAudioThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            mAudioThread = null;
        }
    }

    //开始录像
    public synchronized void startRecord() {
        if (Thread.currentThread() != mCameraThread) {
            mCameraHandler.post(() -> startRecord());
            return;
        }

        if (mCamera == null) {
            return;
        }

        String mFilePath = new File(recordPath, ymdhm.format(new Date())).toString();

        mEasyMuxer = new EasyMuxer(recordPath, mFilePath, durationMillis, mTheRecordStatusListener);

        mRecordVC = new RecordVideoConsumer(
                context, mHevc ? MediaFormat.MIMETYPE_VIDEO_HEVC : MediaFormat.MIMETYPE_VIDEO_AVC,
                mEasyMuxer,
                SPUtil.getEnableVideoOverlay(context),
                SPUtil.getBitrateKbps(context),
                info.mName, info.mColorFormat,
                channelid,
                Constant.Root);
        mRecordVC.onVideoStart(frameWidth, frameHeight);
        if (audioStream != null) {
            audioStream.setMuxer(mEasyMuxer);
        }
    }

    public synchronized void stopRecord() {
        if (Thread.currentThread() != mCameraThread) {
            mCameraHandler.post(() -> stopRecord());
            return;
        }

        if (mRecordVC == null || audioStream == null) {

        } else {
            audioStream.setMuxer(null);
            mRecordVC.onVideoStop();
            mRecordVC = null;
        }

        if (mEasyMuxer != null)
            mEasyMuxer.release();

        mEasyMuxer = null;
    }

    public void updateResolution(final int w, final int h) {
        if (mCamera == null)
            return;

        stopPreview();
        destroyCamera();

        mCameraHandler.post(() -> {
            defaultWidth = w;
            defaultHeight = h;
        });

        createCamera();
        startPreview();
    }

    public void upDateAllowAudio() {
        if (audioStream != null) audioStream.upDateAllowAudio();
    }

    /**
     * 切换前后摄像头
     * CAMERA_FACING_BACK_LOOP                 循环切换摄像头
     * Camera.CameraInfo.CAMERA_FACING_BACK    后置摄像头
     * Camera.CameraInfo.CAMERA_FACING_FRONT   前置摄像头
     * CAMERA_FACING_BACK_UVC                  UVC摄像头
     */
    public void switchCamera(int cameraId) {
        this.mTargetCameraId = cameraId;

        if (mCameraHandler.hasMessages(SWITCH_CAMERA)) {
            return;
        } else {
            mCameraHandler.sendEmptyMessage(SWITCH_CAMERA);
        }
    }

    public void switchCamera() {
        switchCamera(CAMERA_FACING_BACK_LOOP);
    }

    private Runnable switchCameraTask = new Runnable() {
        @Override
        public void run() {
            if (!enableVideo)
                return;

            try {
                if (mTargetCameraId != CAMERA_FACING_BACK_LOOP && mCameraId == mTargetCameraId) {
                    if (uvcCamera != null || mCamera != null) {
                        return;
                    }
                }

                if (mTargetCameraId == CAMERA_FACING_BACK_LOOP) {
                    if (mCameraId == Camera.CameraInfo.CAMERA_FACING_BACK) {
                        mCameraId = Camera.CameraInfo.CAMERA_FACING_FRONT;
                    } else if (mCameraId == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                        mCameraId = CAMERA_FACING_BACK_UVC;// 尝试切换到外置摄像头
                    } else {
                        mCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
                    }
                } else {
                    mCameraId = mTargetCameraId;
                }

                stopPreview();
                destroyCamera();
                createCamera();
                startPreview();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {

            }
        }
    };

    Camera.PreviewCallback previewCallback = (data, camera) -> {
        if (data == null)
            return;

        int result;
        if (camInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (camInfo.orientation + displayRotationDegree) % 360;
        } else {
            result = (camInfo.orientation - displayRotationDegree + 360) % 360;
        }

        if (i420_buffer == null || i420_buffer.length != data.length) {
            i420_buffer = new byte[data.length];
        }

        JNIUtil.ConvertToI420(data, i420_buffer, defaultWidth, defaultHeight, 0, 0, defaultWidth, defaultHeight, result % 360, 2);
        System.arraycopy(i420_buffer, 0, data, 0, data.length);

        if (mRecordVC != null) {
            mRecordVC.onVideo(i420_buffer, 0);
        }

        mVC.onVideo(data, 0);
        mCamera.addCallbackBuffer(data);
    };

    final IFrameCallback uvcFrameCallback = new IFrameCallback() {
        @Override
        public void onFrame(ByteBuffer frame) {
            if (uvcCamera == null)
                return;

            Thread.currentThread().setName("UVCCamera");
            frame.clear();

            byte[] data = cache.poll();
            if (data == null) {
                data = new byte[frame.capacity()];
            }

            frame.get(data);

//            bufferQueue.offer(data);
//            mCameraHandler.post(dequeueRunnable);

            onPreviewFrame2(data, uvcCamera);
        }
    };

    public void onPreviewFrame2(byte[] data, Object camera) {
        if (data == null)
            return;

        if (i420_buffer == null || i420_buffer.length != data.length) {
            i420_buffer = new byte[data.length];
        }

        JNIUtil.ConvertToI420(data, i420_buffer,
                defaultWidth, defaultHeight,
                0, 0,
                defaultWidth, defaultHeight,
                0, 2);
        System.arraycopy(i420_buffer, 0, data, 0, data.length);

        if (mRecordVC != null) {
            mRecordVC.onVideo(i420_buffer, 0);
        }

        mVC.onVideo(data, 0);
    }

    /* ============================== CodecInfo ============================== */

    public static CodecInfo info = new CodecInfo();

    public static class CodecInfo {
        public String mName;
        public int mColorFormat;
    }

    public static ArrayList<CodecInfo> listEncoders(String mime) {
        // 可能有多个编码库，都获取一下
        ArrayList<CodecInfo> codecInfoList = new ArrayList<>();
        int numCodecs = MediaCodecList.getCodecCount();

        // int colorFormat = 0;
        // String name = null;
        for (int i1 = 0; i1 < numCodecs; i1++) {
            MediaCodecInfo codecInfo = MediaCodecList.getCodecInfoAt(i1);

            if (!codecInfo.isEncoder()) {
                continue;
            }

            if (codecMatch(mime, codecInfo)) {
                String name = codecInfo.getName();
                int colorFormat = getColorFormat(codecInfo, mime);

                if (colorFormat != 0) {
                    CodecInfo ci = new CodecInfo();
                    ci.mName = name;
                    ci.mColorFormat = colorFormat;
                    codecInfoList.add(ci);
                }
            }
        }

        return codecInfoList;
    }

    /* ============================== private method ============================== */

    private static boolean codecMatch(String mimeType, MediaCodecInfo codecInfo) {
        String[] types = codecInfo.getSupportedTypes();

        for (String type : types) {
            if (type.equalsIgnoreCase(mimeType)) {
                return true;
            }
        }

        return false;
    }

    private static int getColorFormat(MediaCodecInfo codecInfo, String mimeType) {
        // 在ByteBuffer模式下，视频缓冲区根据其颜色格式进行布局。
        MediaCodecInfo.CodecCapabilities capabilities = codecInfo.getCapabilitiesForType(mimeType);
        int[] cf = new int[capabilities.colorFormats.length];
        System.arraycopy(capabilities.colorFormats, 0, cf, 0, cf.length);
        List<Integer> sets = new ArrayList<>();

        for (int i = 0; i < cf.length; i++) {
            sets.add(cf[i]);
        }

        if (sets.contains(COLOR_FormatYUV420SemiPlanar)) {
            return COLOR_FormatYUV420SemiPlanar;
        } else if (sets.contains(COLOR_FormatYUV420Planar)) {
            return COLOR_FormatYUV420Planar;
        } else if (sets.contains(COLOR_FormatYUV420PackedPlanar)) {
            return COLOR_FormatYUV420PackedPlanar;
        } else if (sets.contains(COLOR_TI_FormatYUV420PackedSemiPlanar)) {
            return COLOR_TI_FormatYUV420PackedSemiPlanar;
        }

        return 0;
    }

    private static int[] determineMaximumSupportedFramerate(Camera.Parameters parameters) {
        int[] maxFps = new int[]{0, 0};
        List<int[]> supportedFpsRanges = parameters.getSupportedPreviewFpsRange();

        for (Iterator<int[]> it = supportedFpsRanges.iterator(); it.hasNext(); ) {
            int[] interval = it.next();

            if (interval[1] > maxFps[1] || (interval[0] > maxFps[0] && interval[1] == maxFps[1])) {
                maxFps = interval;
            }
        }

        return maxFps;
    }

    public void setRecordPath(String recordPath) {
        this.recordPath = recordPath;
    }

    public boolean isRecording() {
        return mEasyMuxer != null;
    }

    public void setSurfaceTexture(SurfaceTexture texture) {
        mSurfaceHolderRef = new WeakReference<SurfaceTexture>(texture);
    }

    public boolean isStreaming() {
        return isPushStream;
    }

    public Camera getCamera() {
        return mCamera;
    }

    public int getDisplayRotationDegree() {
        return displayRotationDegree;
    }

    public void setDisplayRotationDegree(int degree) {
        displayRotationDegree = degree;
    }

    /**
     * 旋转YUV格式数据
     *
     * @param src    YUV数据
     * @param format 0，420P；1，420SP
     * @param width  宽度
     * @param height 高度
     * @param degree 旋转度数
     */
    private static void yuvRotate(byte[] src, int format, int width, int height, int degree) {
        int offset = 0;
        if (format == 0) {
            JNIUtil.rotateMatrix(src, offset, width, height, degree);
            offset += (width * height);
            JNIUtil.rotateMatrix(src, offset, width / 2, height / 2, degree);
            offset += width * height / 4;
            JNIUtil.rotateMatrix(src, offset, width / 2, height / 2, degree);
        } else if (format == 1) {
            JNIUtil.rotateMatrix(src, offset, width, height, degree);
            offset += width * height;
            JNIUtil.rotateShortMatrix(src, offset, width / 2, height / 2, degree);
        }
    }

    class TheRecordStatusListener extends RecordStatusListener {
        @Override
        public void msg(int action) {
            switch (action) {
                case EasyGBDConstant.STARTRECORD:
                    EventBus.getDefault().post(new StartRecord());
                    break;
                case EasyGBDConstant.STOPRECORD:
                    EventBus.getDefault().post(new StopRecord());
                    break;
            }
        }
    }

    public void setCameraId(int id) {
        mCameraId = id;
    }

    public int getCameraId() {
        return mCameraId;
    }

    public void setLoLa(int channel, double longitude, double latitude) {
        mEasyPusher.setLoLa(channel, longitude, latitude);
    }

    //    /* 音频编码 */
    public static final int EASY_SDK_AUDIO_CODEC_AAC = 0x15002;     /* AAC */
    public static final int EASY_SDK_AUDIO_CODEC_G711U = 0x10006;   /* G711 ulaw */
    public static final int EASY_SDK_AUDIO_CODEC_G711A = 0x10007;   /* G711 alaw */

    private FrameInfoQueue mQueue = new FrameInfoQueue();


    private static class FrameInfoQueue extends PriorityQueue<Device.FrameInfo> {
        public static final int CAPACITY = 500;
        public static final int INITIAL_CAPACITY = 300;

        public FrameInfoQueue() {
            super(INITIAL_CAPACITY, new Comparator<Device.FrameInfo>() {
                @Override
                public int compare(Device.FrameInfo frameInfo, Device.FrameInfo t1) {
                    return (int) (frameInfo.stamp - t1.stamp);
                }
            });
        }

        final ReentrantLock lock = new ReentrantLock();
        final Condition notFull = lock.newCondition();
        final Condition notVideo = lock.newCondition();
        final Condition notAudio = lock.newCondition();

        @Override
        public int size() {
            lock.lock();
            try {
                return super.size();
            } finally {
                lock.unlock();
            }
        }

        @Override
        public void clear() {
            lock.lock();
            try {
                int size = super.size();
                super.clear();
                int k = size;

                for (; k > 0 && lock.hasWaiters(notFull); k--) {
                    notFull.signal();
                }
            } finally {
                lock.unlock();
            }
        }

        public void put(Device.FrameInfo x) throws InterruptedException {
            lock.lockInterruptibly();

            try {
                int size;
                while ((size = super.size()) == CAPACITY) {
                    Log.i(TAG, "queue full:" + CAPACITY);
                    notFull.await();
                }

                offer(x);

                // 这里是乱序的。并非只有空的queue才丢到首位。因此不能做限制 if (size == 0)
                {
                    if (x.audio) {
                        notAudio.signal();
                    } else {
                        notVideo.signal();
                    }
                }
            } finally {
                lock.unlock();
            }
        }

        public Device.FrameInfo takeVideoFrame() throws InterruptedException {
            lock.lockInterruptibly();

            try {
                while (true) {
                    Device.FrameInfo x = peek();

                    if (x == null) {
                        notVideo.await();
                    } else {
                        if (!x.audio) {
                            remove();
                            notFull.signal();
                            notAudio.signal();
                            return x;
                        } else {
                            notVideo.await();
                        }
                    }
                }
            } finally {
                lock.unlock();
            }
        }

        public Device.FrameInfo takeVideoFrame(long ms) throws InterruptedException {
            lock.lockInterruptibly();

            try {
                while (true) {
                    Device.FrameInfo x = peek();

                    if (x == null) {
                        if (!notVideo.await(ms, TimeUnit.MILLISECONDS)) return null;
                    } else {
                        if (!x.audio) {
                            remove();
                            notFull.signal();
                            notAudio.signal();
                            return x;
                        } else {
                            notVideo.await();
                        }
                    }
                }
            } finally {
                lock.unlock();
            }
        }

        public Device.FrameInfo takeAudioFrame() throws InterruptedException {
            lock.lockInterruptibly();

            try {
                while (true) {
                    Device.FrameInfo x = peek();
                    if (x == null) {
                        notAudio.await();
                    } else {
                        if (x.audio) {
                            remove();
                            notFull.signal();
                            notVideo.signal();
                            return x;
                        } else {
                            notAudio.await();
                        }
                    }
                }
            } finally {
                lock.unlock();
            }
        }
    }


    private volatile Thread mAudioThread;
    private AudioTrack mAudioTrack;

    private Device.MediaInfo mMediaInfo = new Device.MediaInfo(8000, 1, 16);


    private void startAudio() {
        mAudioThread = new Thread("AUDIO_CONSUMER") {
            @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void run() {
                Process.setThreadPriority(Process.THREAD_PRIORITY_AUDIO);
                Device.FrameInfo frameInfo = null;

                final AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

                AudioManager.OnAudioFocusChangeListener l = new AudioManager.OnAudioFocusChangeListener() {
                    @Override
                    public void onAudioFocusChange(int focusChange) {
                        if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
                            AudioTrack audioTrack = mAudioTrack;
                            if (audioTrack != null) {
                                audioTrack.setStereoVolume(1.0f, 1.0f);
                                if (audioTrack.getPlayState() == AudioTrack.PLAYSTATE_PAUSED) {
                                    audioTrack.flush();
                                    audioTrack.play();
                                }
                            }
                        } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
                            AudioTrack audioTrack = mAudioTrack;
                            if (audioTrack != null) {
                                if (audioTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING) {
                                    audioTrack.pause();
                                }
                            }
                        } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) {
                            AudioTrack audioTrack = mAudioTrack;
                            if (audioTrack != null) {
                                audioTrack.setStereoVolume(0.5f, 0.5f);
                            }
                        }
                    }
                };

                try {
                    int requestCode = am.requestAudioFocus(l, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
                    if (requestCode != AUDIOFOCUS_REQUEST_GRANTED) {
                        return;
                    }

//                    do {
//                        frameInfo = mQueue.takeAudioFrame();
//
//                        if (mMediaInfo != null)
//                            break;
//                    } while (true);

//                    final Thread t = Thread.currentThread();

                    if (mAudioTrack == null) {
//                        int sampleRateInHz = (int) (mMediaInfo.sample * 1.001);
                        int sampleRateInHz = mMediaInfo.sample;
                        int channelConfig = mMediaInfo.channel == 1 ? AudioFormat.CHANNEL_OUT_MONO : AudioFormat.CHANNEL_OUT_STEREO;
                        int audioFormat = AudioFormat.ENCODING_PCM_16BIT;

//                        int bfSize = AudioTrack.getMinBufferSize(mMediaInfo.sample, channelConfig, audioFormat) * 8;
                        int bfSize = AudioTrack.getMinBufferSize(mMediaInfo.sample, channelConfig, audioFormat);

                        Log.d("Magic", String.format("Magic- bfSize: %d", bfSize));


                        mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRateInHz, channelConfig, audioFormat, bfSize, AudioTrack.MODE_STREAM);
                    }

                    mAudioTrack.play();

                    Log.i(TAG, String.format("POST VIDEO_DISPLAYED IN AUDIO THREAD!!!"));

                    // 半秒钟的数据缓存
//                    byte[] mBufferReuse = new byte[16000];

                    int[] outLen = new int[1];
                    while (mAudioThread != null) {

                        if (frameInfo == null) {
                            frameInfo = mQueue.takeAudioFrame();
                        }

                        short[] mBufferReuse = new short[frameInfo.buffer.length];

                        outLen[0] = mBufferReuse.length;


                        /////////////////// 第一种解码方式：///////////////////
//                        int nRet = AudioCodec.decode(handle, frameInfo.buffer,
//                                0, frameInfo.length, mBufferReuse, outLen);
//                        if (nRet == 0) {
//                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                                mAudioTrack.write(mBufferReuse, 0, outLen[0], AudioTrack.WRITE_NON_BLOCKING);
//                            } else {
//                                mAudioTrack.write(mBufferReuse, 0, outLen[0]);
//                            }
//                        }

                        /////////////////// 第二种解码方式：///////////////////


                        G711Code.G711aDecoder(mBufferReuse, frameInfo.buffer, frameInfo.length);


                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                        for (short s : mBufferReuse) {
                            byteArrayOutputStream.write(s & 0xFF);       // Write the lower byte (8 bits)
                            byteArrayOutputStream.write((s >> 8) & 0xFF); // Write the upper byte (8 bits)
                        }
                        byte[] byteBuffer = byteArrayOutputStream.toByteArray();

                        mAudioTrack.write(byteBuffer, 0, byteBuffer.length);

//                        saveToFile(byteBuffer, "talk.pcm");


                        frameInfo = null;
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                } finally {
                    am.abandonAudioFocus(l);

                    AudioTrack track = mAudioTrack;
                    if (track != null) {
                        synchronized (track) {
                            mAudioTrack = null;
                            track.release();
                        }
                    }
                }
            }
        };

        mAudioThread.start();
    }


    private void saveToFile(byte[] buffer, String fileName) {
        try {
            File file = new File(context.getExternalFilesDir(null), fileName);

            if (!file.exists()) {
                try {
                    boolean created = file.createNewFile();
                    if (!created) {
                        Log.e("FileOutput", "Failed to create file.");
                        return;
                    }
                } catch (IOException e) {
                    Log.e("FileOutput", "Error creating file: " + e.getMessage());
                    return;
                }
            }
            FileOutputStream fos = new FileOutputStream(file, true);
            fos.write(buffer, 0, buffer.length);
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}