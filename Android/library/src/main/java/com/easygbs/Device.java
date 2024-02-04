package com.easygbs;

import android.content.Context;
import android.preference.PreferenceManager;
import android.util.Log;

import org.easydarwin.push.Pusher;
import org.easydarwin.util.SIP;

public class Device implements Pusher {

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

    private boolean pushed = false;

    private int videoCodec;
    private int width;
    private int height;
    private int frameRate;

    private int audioCodec;
    private int sampleRate;
    private int channels;
    private int bitPerSamples;

    public Device(Context c) {
        context = c;
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
    public native int create(String serverIp, int serverPort, String serverId, String serverDomain,
                             String deviceId, int localSipPort, int channelNum, String password, int protocol,
                             int regExpires, int heartbeatInterval, int heartbeatCount);

    public native int addChannelInfo(int channelId, String indexCode, String name, String manufacturer, String model,
                                     String parentID, String owner, String civilCode, String address, double longitude, double latitude);

    public native static int setLotLat(int channelId, double longitude, double latitude);

    /**
     * pushVideo
     *
     * @param buffer
     * @param frameSize
     * @param keyframe  关键帧1 其他0
     */
    public native int pushVideo(int channelId, byte[] buffer, int frameSize, int keyframe);

    public native int pushAudio(int channelId, int format, byte[] buffer, int frameSize, int nbSamples);

    public native int release();

    @Override
    public void initPush(SIP sip) {
        if (sip == null) {
            return;
        }

        ip = sip.getServerIp();
        port = sip.getServerPort();

        int size = sip.getList().size();

        create(sip.getServerIp(),
                sip.getServerPort(),
                sip.getServerId(),
                sip.getServerDomain(),
                sip.getDeviceId(),
                sip.getLocalSipPort(),
                size,
                sip.getPassword(),
                sip.getProtocol(),
                sip.getRegExpires(),
                sip.getHeartbeatInterval(),
                sip.getHeartbeatCount());

        for (int i = 0; i < size; i++) {
            SIP.GB28181_CHANNEL_INFO_T item = sip.getList().get(i);

            addChannelInfo(i,
                    item.getIndexCode(),
                    item.getName(),
                    item.getManufacturer(),
                    item.getModel(),
                    item.getParentId(),
                    item.getOwner(),
                    item.getCivilCode(),
                    item.getAddress(),
                    item.getLongitude(),
                    item.getLatitude());

            setVideoFormat(i, videoCodec, width, height, frameRate);
            setAudioFormat(i, audioCodec, sampleRate, channels, bitPerSamples);
        }

        pushed = true;
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
    public void pushV(byte[] buffer, int length, int keyframe) {
        if (pushed) {
            // TODO channelId是按通道来的：0，1...7  现在写死0 只有一路通道
            int res = pushVideo(0, buffer, length, keyframe);
            Log.i("AAA", "Video：" + res + " >>> " + length);
        }
    }

    @Override
    public void pushA(boolean isAac, byte[] buffer, int length, int nbSamples) {
        if (pushed) {
            // TODO channelId是按通道来的：0，1...7  现在写死0 只有一路通道

            int res;
            if (isAac) {
                res = pushAudio(0, AUDIO_CODEC_AAC, buffer, length, length);
            } else {
                res = pushAudio(0, AUDIO_CODEC_PCM, buffer, length, length);
            }
            Log.i("AAA", "Audio：" + res + " >>> " + length);
        }
    }

    @Override
    public void stop() {
        if (pushed) {
            release();
        }

        pushed = false;
    }

    /**
     * 回调函数
     *
     * @param prt
     * @param channelId
     * @param eventType
     * @param param
     * @param paramLength
     */
    public static void OnGB28181DeviceCALLBACK(int prt, int channelId, int eventType, byte[] param, int paramLength) {
        if (callback != null) {
            callback.onCallback(eventType, OnInitPusherCallback.CODE.getName(eventType));
        }
    }

    public interface OnInitPusherCallback {
        void onCallback(int code, String name);

        class CODE {
            public static final int GB28181_DEVICE_EVENT_CONNECTING = 1;
            public static final int GB28181_DEVICE_EVENT_REGISTER_ING = 2;
            public static final int GB28181_DEVICE_EVENT_REGISTER_OK = 3;
            public static final int GB28181_DEVICE_EVENT_REGISTER_AUTH_FAIL = 4;
            public static final int GB28181_DEVICE_EVENT_START_VIDEO = 5;
            public static final int GB28181_DEVICE_EVENT_STOP_VIDEO = 6;
            public static final int GB28181_DEVICE_EVENT_TALK_AUDIO_DATA = 7;
            public static final int GB28181_DEVICE_EVENT_DISCONNECT = 8;

            public static String getName(int code) {
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
                    case GB28181_DEVICE_EVENT_REGISTER_AUTH_FAIL:
                        res = "注册鉴权失败：" + ip + ":" + port;
                        break;
                    case GB28181_DEVICE_EVENT_START_VIDEO:
                        if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean("key-aac-codec", false)) {
                            res = "开始视频：H264+AAC";
                        } else {
                            res = "开始视频：H264+G711U";
                        }
                        break;
                    case GB28181_DEVICE_EVENT_STOP_VIDEO:
                        res = "停止视频";
                        break;
                    case GB28181_DEVICE_EVENT_TALK_AUDIO_DATA:
                        res = "对讲发过来的音频数据";
                        break;
                    case GB28181_DEVICE_EVENT_DISCONNECT:
                        res = "已断线：" + ip + ":" + port;
                        break;
                    default:
                        res = "";
                        break;
                }
                return res;
            }
        }
    }

    private static OnInitPusherCallback callback;

    public static void setCallback(OnInitPusherCallback callback) {
        Device.callback = callback;
    }
}
