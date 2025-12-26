package com.easygbs;

import android.content.Context;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;


import com.mp4.GBProtocolDRParams;

import org.easydarwin.push.Pusher;
import org.easydarwin.util.SIP;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class Device implements Pusher {
    private static String TAG = "EasyGBD";
    private static String ip;
    private static int port;
    private static Context context;

    public static final int VIDEO_CODEC_NONE = 0;
    public static final int VIDEO_CODEC_H264 = 1;
    public static final int VIDEO_CODEC_MP4 = 2;
    public static final int VIDEO_CODEC_IPEG = 3;
    public static final int VIDEO_CODEC_H265 = 4;

    public static final int AUDIO_CODEC_NONE = 0;
    public static final int AUDIO_CODEC_G711A = 1;
    public static final int AUDIO_CODEC_G711U = 2;
    public static final int AUDIO_CODEC_G726 = 3;
    public static final int AUDIO_CODEC_AAC = 4;
    public static final int AUDIO_CODEC_G722 = 5;
    public static final int AUDIO_CODEC_OPUS = 6;
    public static final int AUDIO_CODEC_PCM = 7;

    private static boolean pushed = false;

    private int videoCodec;
    private int width;
    private int height;
    private int frameRate;

    private int audioCodec;
    private int sampleRate;
    private int channels;
    private int bitPerSamples;

    private static OnInitPusherCallback callback;

    // 单个转码器实例
    private long transContext;
    private int isEnTransPush;
    private int transPushResolution;

    public Device(Context c) {
        context = c;
        GbFileHelper.setContext(context);
    }

    static {
        System.loadLibrary("EasyGBSDevice");
    }

    public native int setVideoFormat(int channelId, int codec, int width, int height, int frameRate);

    public native int setAudioFormat(int channelId, int codec, int sampleRate, int channels, int bitPerSamples);

    /**
     * 创建链接
     *
     * @param serverIp          SIP服务器地址
     * @param serverPort        SIP服务器端口
     * @param serverId          SIP服务器ID
     * @param serverDomain      SIP服务器域
     * @param deviceId          SIP用户名
     * @param channelNum        xxx
     * @param password          SIP用户认证密码
     * @param protocol          0:udp，1:tcp
     * @param regExpires        注册有效期
     * @param heartbeatInterval 心跳周期
     * @param heartbeatCount    最大心跳超时次数
     */
    public native int create(int version, String serverIp, int serverPort, String serverId, String serverDomain, String deviceId, String deviceName, int localSipPort, int channelNum, String password, int protocol, int mediaProtocol, int regExpires, int heartbeatInterval, int heartbeatCount, int gbVer, String logPath);

    public native int addChannelInfo(int channelId, String indexCode, String name, String manufacturer, String model, String parentID, String owner, String civilCode, String address, double longitude, double latitude);

    public native static int setLotLat(int channelId, double longitude, double latitude);

    /**
     * pushVideo
     *
     * @param buffer
     * @param frameSize
     * @param keyframe  关键帧1 其他0
     */
    public static native int pushVideo(int channelId, byte[] buffer, int frameSize, int keyframe);

    public native int pushAudio(int channelId, int format, byte[] buffer, int frameSize, int nbSamples);

    public native int pushRecordVideo(long recordPtr, int channelId, byte[] buffer, int frameSize, int keyframe);

    public native int pushRecordAudio(long recordPtr, int channelId, byte[] buffer, int frameSize, int nbSamples);

    public native int endRecordData(long recordPtr); //录像结束

    public native int release();

    @Override
    public void initPush(SIP sip) {
        if (sip == null) {
            return;
        }

        ip = sip.getServerIp();
        port = sip.getServerPort();

        int size = sip.getList().size();
        create(sip.getVer(), sip.getServerIp(), sip.getServerPort(), sip.getServerId(), sip.getServerDomain(), sip.getDeviceId(), sip.getDeviceName(), sip.getLocalSipPort(), size, sip.getPassword(), sip.getProtocol(), 1, sip.getRegExpires(), sip.getHeartbeatInterval(), sip.getHeartbeatCount(), sip.getGbVer(), sip.getLogPath());

        Log.i(TAG, sip.toString());

        for (int i = 0; i < size; i++) {
            SIP.GB28181_CHANNEL_INFO_T item = sip.getList().get(i);
            addChannelInfo(i, item.getIndexCode(), item.getName(), item.getManufacturer(), item.getModel(), item.getParentId(), item.getOwner(), item.getCivilCode(), item.getAddress(), item.getLongitude(), item.getLatitude());
            Log.i(TAG, "执行完成" + item.toString());
        }


        for (int i = 0; i < size; i++) {
            int setVideoFormatRe = -1;
            int setAudioFormatRe = -1;
            setVideoFormatRe = setVideoFormat(i, videoCodec, width, height, frameRate);
            setAudioFormatRe = setAudioFormat(i, audioCodec, sampleRate, channels, bitPerSamples);
            while ((setVideoFormatRe != 0) || (setAudioFormatRe != 0)) {
                try {
                    Thread.sleep(500);
                    setVideoFormatRe = setVideoFormat(i, videoCodec, width, height, frameRate);
                    setAudioFormatRe = setAudioFormat(i, audioCodec, sampleRate, channels, bitPerSamples);

                } catch (Exception e) {
                    Log.i(TAG, "i  " + i + "  Exception    " + e.toString());
                }
            }
        }
    }

    @Override
    public void setVFormat(int codec, int width, int height, int frameRate) {
        this.videoCodec = codec;
        this.width = width;
        this.height = height;
        this.frameRate = frameRate;
    }

    @Override
    public void setAFormat(int codec, int sampleRate, int channels, int bitPerSamples) {
        this.audioCodec = codec;
        this.sampleRate = sampleRate;
        this.channels = channels;
        this.bitPerSamples = bitPerSamples;
    }

    @Override
    public void pushV(int channelId, byte[] buffer, int length, int keyframe) {
        if (pushed) {
            pushVideo(channelId, buffer, length, keyframe);
        }
    }

    @Override
    public void pushA(int channelId, boolean isAac, byte[] buffer, int length, int nbSamples) {
        if (pushed) {
            int res;
            if (isAac) {
                res = pushAudio(channelId, AUDIO_CODEC_AAC, buffer, length, length);
            } else {
                res = pushAudio(channelId, AUDIO_CODEC_PCM, buffer, length, length);
            }
        }
    }

    @Override
    public void setLoLa(int channel, double longitude, double latitude) {
        setLotLat(channel, longitude, latitude);
    }

    @Override
    public boolean getPushed() {
        return pushed;
    }

    @Override
    public void stop() {
        release();
        pushed = false;
    }


    /**
     * @param
     * @param recordPtr   录像回调 句柄  默认 0
     * @param channelId
     * @param eventType
     * @param param
     * @param paramLength
     */
    public static void OnGB28181DeviceCALLBACK(int ptr, long recordPtr, int channelId, int eventType, byte[] param, int paramLength) {

        Log.i(TAG, "OnGB28181DeviceCALLBACK  ptr  " + ptr + "recordPtr " + recordPtr + "  channelId  " + channelId + "  eventType  " + eventType);

        GBProtocolDRParams mGBProtocolDRParams = GBProtocolDRParams.fromJson(param);

        if (callback != null) {

            callback.onCallback(channelId, eventType, OnInitPusherCallback.CODE.getName(eventType, mGBProtocolDRParams));

            if (OnInitPusherCallback.CODE.GB28181_DEVICE_EVENT_TALK_AUDIO_DATA == eventType) {
                FrameInfo fi = new FrameInfo();
                fi.stamp = System.currentTimeMillis();
                fi.buffer = param;
                fi.length = paramLength;
                fi.audio = true;
                callback.onSourceCallBack(fi);
            } else if (OnInitPusherCallback.CODE.GB28181_DEVICE_EVENT_START_AUDIO_VIDEO == eventType) {
                pushed = true;
            } else if (OnInitPusherCallback.CODE.GB28181_DEVICE_EVENT_STOP_AUDIO_VIDEO == eventType) {
                pushed = false;
            }
        }
    }

    private static void saveToFile(byte[] buffer, String fileName) {
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

    public interface OnInitPusherCallback {
        void onCallback(int channel, int code, String name);

        void onSourceCallBack(FrameInfo frameInfo);

        class CODE {
            public static final int GB28181_DEVICE_EVENT_CONNECTING = 1;
            public static final int GB28181_DEVICE_EVENT_REGISTER_ING = 2;
            public static final int GB28181_DEVICE_EVENT_REGISTER_TIMEOUT = 3;
            public static final int GB28181_DEVICE_EVENT_REGISTER_OK = 4;
            public static final int GB28181_DEVICE_EVENT_REGISTER_AUTH_FAIL = 5;
            public static final int GB28181_DEVICE_EVENT_START_AUDIO_VIDEO = 6;
            public static final int GB28181_DEVICE_EVENT_STOP_AUDIO_VIDEO = 7;
            public static final int GB28181_DEVICE_EVENT_TALK_AUDIO_DATA = 8;
            public static final int GB28181_DEVICE_EVENT_DISCONNECT = 9;

            public static final int GB28181_DEVICE_EVENT_SUBSCRIBE_ALARM = 10;
            public static final int GB28181_DEVICE_EVENT_SUBSCRIBE_CATALOG = 11;
            public static final int GB28181_DEVICE_EVENT_SUBSCRIBE_MOBILEPOSITION = 12;

            public static final int GB28181_DEVICE_EVENT_PTZ_MOVE_LEFT = 13;
            public static final int GB28181_DEVICE_EVENT_PTZ_MOVE_UP = 14;
            public static final int GB28181_DEVICE_EVENT_PTZ_MOVE_RIGHT = 15;
            public static final int GB28181_DEVICE_EVENT_PTZ_MOVE_DOWN = 16;
            public static final int GB28181_DEVICE_EVENT_PTZ_MOVE_STOP = 17;
            public static final int GB28181_DEVICE_EVENT_PTZ_ZOOM_IN = 18;
            public static final int GB28181_DEVICE_EVENT_PTZ_ZOOM_OUT = 19;


            public static final int GB28181_DEVICE_EVENT_FIND_RECORD = 20;                //录像查询
            public static final int GB28181_DEVICE_EVENT_RECORD_START_AUDIO_VIDEO = 21;   //录像播放
            public static final int GB28181_DEVICE_EVENT_RECORD_STOP_AUDIO_VIDEO = 22;    //录像播放停止
            public static final int GB28181_DEVICE_EVENT_RECORD_SCALE_AUDIO_VIDEO = 23;   //录像倍速
            public static final int GB28181_DEVICE_EVENT_RECORD_PLAY_AUDIO_VIDEO = 24;    // 录像恢复播放
            public static final int GB28181_DEVICE_EVENT_RECORD_PAUSE_AUDIO_VIDEO = 25;   // 录像暂停播放
            public static final int GB28181_DEVICE_EVENT_RECORD_START_DOWNLOAD_AUDIO_VIDEO = 26;   // 开始录像下载
            public static final int GB28181_DEVICE_EVENT_RECORD_STOP_DOWNLOAD_AUDIO_VIDEO = 27;   // 停止录像下载


            public static String getName(int code, GBProtocolDRParams params) {
                String res;
                switch (code) {
                    case GB28181_DEVICE_EVENT_CONNECTING:
                        res = "连接中：" + ip + ":" + port;
                        break;
                    case GB28181_DEVICE_EVENT_REGISTER_ING:
                        res = "注册中：" + ip + ":" + port;
                        break;
                    case GB28181_DEVICE_EVENT_REGISTER_OK:
                        res = "注册成功：" + ip + ":" + port;
                        break;
                    case GB28181_DEVICE_EVENT_REGISTER_TIMEOUT:
                        res = "注册超时：" + ip + ":" + port;
                        break;
                    case GB28181_DEVICE_EVENT_REGISTER_AUTH_FAIL:
                        res = "注册鉴权失败：" + ip + ":" + port;
                        break;
                    case GB28181_DEVICE_EVENT_START_AUDIO_VIDEO:
                        //true:H265   false:H264
                        boolean codec = PreferenceManager.getDefaultSharedPreferences(context).getBoolean("key-hevc-codec", false);
                        int aac = PreferenceManager.getDefaultSharedPreferences(context).getInt("key-aac-codec", 2);
                        if (aac == 0) {
                            if (codec) {
                                res = "开始视频：H265+G711A";
                            } else {
                                res = "开始视频：H264+G711A";
                            }
                        } else if (aac == 1) {
                            if (codec) {
                                res = "开始视频：H265+G711U";
                            } else {
                                res = "开始视频：H264+G711U";
                            }
                        } else if (aac == 2) {
                            if (codec) {
                                res = "开始视频：H265+AAC";
                            } else {
                                res = "开始视频：H264+AAC";
                            }
                        } else {
                            if (codec) {
                                res = "开始视频：H265";
                            } else {
                                res = "开始视频：H264";
                            }
                        }
                        break;
                    case GB28181_DEVICE_EVENT_STOP_AUDIO_VIDEO:
                        res = "停止视频";
                        break;
                    case GB28181_DEVICE_EVENT_TALK_AUDIO_DATA:
                        res = "对讲发过来的音频数据";
                        break;
                    case GB28181_DEVICE_EVENT_DISCONNECT:
                        res = "已断线：" + ip + ":" + port;
                        break;

                    case GB28181_DEVICE_EVENT_FIND_RECORD:
                        res = "录像查询：" + params.toString();
                        break;
                    default:
                        res = "";
                        break;
                }
                return res;
            }
        }
    }

    public static void setCallback(OnInitPusherCallback callback) {
        Device.callback = callback;
    }


    public static final class FrameInfo {
        public long stamp;
        public int length;          /* 音视频帧大小 */
        public byte[] buffer;
        public boolean audio;
    }

    public static final class MediaInfo {
        int videoCodec;
        int fps;
        int audioCodec;
        public int sample;
        public int channel;
        public int bitPerSample;
        int spsLen;
        int ppsLen;
        byte[] sps;
        byte[] pps;

        public MediaInfo() {

        }

        /**
         * @param sample       采样率
         * @param channel      通道
         * @param bitPerSample 采样精度
         */
        public MediaInfo(int sample, int channel, int bitPerSample) {
            this.sample = sample;
            this.channel = channel;
            this.bitPerSample = bitPerSample;
        }

        @Override
        public String toString() {
            return "MediaInfo{" + "videoCodec=" + videoCodec + ", fps=" + fps + ", audioCodec=" + audioCodec + ", sample=" + sample + ", channel=" + channel + ", bitPerSample=" + bitPerSample + ", spsLen=" + spsLen + ", ppsLen=" + ppsLen + '}';
        }


    }


    public static void onTransH264CallBack(byte[] frameData, TransFrameInfo tfi, int user) {
        int res = pushVideo(user, frameData, frameData.length, tfi.flag);
    }

    public static final class TransFrameInfo {
        public int dataSize;
        public int pixFmt;
        public int width;
        public int height;
        public int streamIndex; // 0 - video; 1 - other
        public int flag;

        public TransFrameInfo() {
        }

        @Override
        public String toString() {
            return "TransFrameInfo{" + "dataSize=" + dataSize + ", pixFmt=" + pixFmt + ", width=" + width + ", height=" + height + ", streamIndex=" + streamIndex + ", flag=" + flag + '}';
        }
    }
}