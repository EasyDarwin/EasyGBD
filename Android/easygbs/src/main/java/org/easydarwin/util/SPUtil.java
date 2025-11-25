package org.easydarwin.util;

import android.content.Context;
import android.preference.PreferenceManager;

public class SPUtil {
    //软编码
    private static final String KEY_SW_CODEC = "key-sw-codec";
    //true:H265   false:H264
    private static final String KEY_HEVC_CODEC = "key-hevc-codec";
    //音频编码  0:G711A    1:G711U    2:AAC
    private static final String KEY_AAC_CODEC = "key-aac-codec";
    //叠加水印
    private static final String KEY_ENABLE_VIDEO_OVERLAY = "key_enable_video_overlay";
    //视频码率
    private static final String KEY_BITRATE_ADDED_KBPS = "bitrate_added_kbps";
    //视频
    private static final String KEY_ENABLE_VIDEO = "key-enable-video";
    //音频
    private static final String KEY_ENABLE_AUDIO = "key-enable-audio";
    private static final String VER = "ver";
    private static final String GB_VER = "gb_ver";
    public static final String SERVERIP = "serverip";
    public static final String SERVERPORT = "serverport";
    public static final String LOCALSIPPORT = "localsipport";
    public static final String SERVERID = "serverid";
    public static final String SERVERDOMAIN = "serverdomain";
    public static final String DEVICEID = "deviceid";
    public static final String PASSWORD = "password";
    public static final String PROTOCOL = "protocol";
    public static final String REGEXPIRES = "regexpires";
    public static final String HEARTBEATINTERVAL = "heartbeatinterval";
    public static final String HEARTBEATCOUNT = "heartbeatcount";
    //public static final String INDEXCODE = "indexcode";
    public static final String DEVICENAME = "devicename";
    public static String ISENVIDEO = "isenvideo";
    public static String ISENAUDIO = "isenaudio";
    public static String ISENLOCREPORT = "isenlocreport";
    public static String CAMERAID = "cameraid";
    //0:1920*1080  1:1280*720  2:640*480
    public static String VIDEORESOLUTION = "videoresolution";
    public static String FRAMERATE = "framerate";
    public static String SAMPLINGRATE = "samplingrate";
    //0:单声道  1:立体声道
    public static String AUDIOCHANNEL = "audiochannel";
    public static String AUDIOCODERATE = "audiocoderate";
    public static String LOCATIONFREQ = "locationfreq";
    public static String LONGITUDE = "longitude";
    public static String LATITUDE = "latitude";

    public static boolean getswCodec(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(KEY_SW_CODEC, false);
    }

    public static void setswCodec(Context context, boolean isChecked) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(KEY_SW_CODEC, isChecked).apply();
    }

    public static boolean getHevcCodec(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(KEY_HEVC_CODEC, false);
    }

    public static void setHevcCodec(Context context, boolean isChecked) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(KEY_HEVC_CODEC, isChecked).apply();
    }

    public static int getAACCodec(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getInt(KEY_AAC_CODEC, 2);
    }

    public static void setAACCodec(Context context, int value) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putInt(KEY_AAC_CODEC, value).apply();
    }

    public static boolean getEnableVideoOverlay(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(KEY_ENABLE_VIDEO_OVERLAY, false);
    }

    public static void setEnableVideoOverlay(Context context, boolean isChecked) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(KEY_ENABLE_VIDEO_OVERLAY, isChecked).apply();
    }

    public static int getBitrateKbps(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getInt(KEY_BITRATE_ADDED_KBPS, 1024);
    }

    public static void setBitrateKbps(Context context, int value) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putInt(KEY_BITRATE_ADDED_KBPS, value).apply();
    }

    public static boolean getEnableVideo(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(KEY_ENABLE_VIDEO, true);
    }

    public static void setEnableVideo(Context context, boolean value) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(KEY_ENABLE_VIDEO, value).apply();
    }

    public static boolean getEnableAudio(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(KEY_ENABLE_AUDIO, true);
    }

    public static void setEnableAudio(Context context, boolean value) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(KEY_ENABLE_AUDIO, value).apply();
    }

    public static int getVer(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getInt(VER, 2016);
    }

    public static void setVer(Context context, int value) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putInt(VER, value).apply();
    }

    public static void setGBVer(Context context, String value) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString(GB_VER, value).apply();
    }

    public static String getGBVer(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(GB_VER, "GB28181");
    }

    public static int getLocalsipport(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getInt(LOCALSIPPORT, 15060);
    }

    public static int getServerport(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getInt(SERVERPORT, 15060);
    }

    public static void setServerport(Context context, int value) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putInt(SERVERPORT, value).apply();
    }

    public static String getServerip(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(SERVERIP, "demo.easygbs.com");
    }

    public static void setServerip(Context context, String value) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString(SERVERIP, value).apply();
    }

    public static String getServerid(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(SERVERID, "34020000002000000001");
    }

    public static void setServerid(Context context, String value) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString(SERVERID, value).apply();
    }

    public static String getServerdomain(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(SERVERDOMAIN, "3402000000");
    }

    public static void setServerdomain(Context context, String value) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString(SERVERDOMAIN, value).apply();
    }


    public static String getDeviceid(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(DEVICEID, "");
    }

    public static void setDeviceid(Context context, String value) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString(DEVICEID, value).apply();
    }

    public static String getDevicename(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(DEVICENAME, "");
    }

    public static void setDevicename(Context context, String value) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString(DEVICENAME, value).apply();
    }

    public static String getPassword(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(PASSWORD, "12345678");
    }

    public static void setPassword(Context context, String value) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString(PASSWORD, value).apply();
    }

    public static int getRegexpires(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getInt(REGEXPIRES, 3600);
    }

    public static void setRegexpires(Context context, int value) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putInt(REGEXPIRES, value).apply();
    }

    public static int getHeartbeatinterval(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getInt(HEARTBEATINTERVAL, 30);
    }

    public static void setHeartbeatinterval(Context context, int value) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putInt(HEARTBEATINTERVAL, value).apply();
    }

    public static int getHeartbeatcount(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getInt(HEARTBEATCOUNT, 3);
    }

    public static void setHeartbeatcount(Context context, int value) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putInt(HEARTBEATCOUNT, value).apply();
    }

    public static int getProtocol(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getInt(PROTOCOL, SIP.ProtocolEnum.UDP.getValue());
    }

    public static void setProtocol(Context context, int value) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putInt(PROTOCOL, value).apply();
    }

    /*
    public static String getIndexcode(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(INDEXCODE, "34020000001320000001");
    }

    public static void setIndexcode(Context context, String value) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString(INDEXCODE, value)
                .apply();
    }
    */

    public static int getIsenvideo(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getInt(ISENVIDEO, 1);
    }

    public static void setIsenvideo(Context context, int value) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putInt(ISENVIDEO, value).apply();
    }

    public static int getIsenaudio(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getInt(ISENAUDIO, 1);
    }

    public static void setIsenaudio(Context context, int value) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putInt(ISENAUDIO, value).apply();
    }

    public static int getIsenlocreport(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getInt(ISENLOCREPORT, 1);
    }

    public static void setIsenlocreport(Context context, int value) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putInt(ISENLOCREPORT, value).apply();
    }

    public static int getCameraid(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getInt(CAMERAID, 0);
    }

    public static void setCameraid(Context context, int value) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putInt(CAMERAID, value).apply();
    }

    public static int getVideoresolution(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getInt(VIDEORESOLUTION, 0);
    }

    public static void setVideoresolution(Context context, int value) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putInt(VIDEORESOLUTION, value).apply();
    }

    public static int getFramerate(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getInt(FRAMERATE, 25);
    }

    public static void setFramerate(Context context, int value) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putInt(FRAMERATE, value).apply();
    }

    public static int getSamplingrate(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getInt(SAMPLINGRATE, 8000);
    }

    public static void setSamplingrate(Context context, int value) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putInt(SAMPLINGRATE, value).apply();
    }

    public static int getAudiochannel(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getInt(AUDIOCHANNEL, 1);
    }

    public static void setAudiochannel(Context context, int value) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putInt(AUDIOCHANNEL, value).apply();
    }

    public static int getAudiocoderate(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getInt(AUDIOCODERATE, 16);
    }

    public static void setAudiocoderate(Context context, int value) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putInt(AUDIOCODERATE, value).apply();
    }

    public static int getLocationfreq(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getInt(LOCATIONFREQ, 10);
    }

    public static void setLocationfreq(Context context, int value) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putInt(LOCATIONFREQ, value).apply();
    }

    public static String getLongitude(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(LONGITUDE, "116.397128");
    }

    public static void setLongitude(Context context, String value) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString(LONGITUDE, value).apply();
    }

    public static String getLatitude(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(LATITUDE, "39.916527");
    }

    public static void setLatitude(Context context, String value) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString(LATITUDE, value).apply();
    }

    public static void setIsEnTransPush(Context context, int enable) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putInt("isEnTransPush", enable).apply();
    }

    public static int getIsEnTransPush(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getInt("isEnTransPush", 0);
    }

    public static int getTranPushResolution(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getInt("TranPushResolution", 1080);
    }

    public static void setTranPushResolution(Context context, int resolution) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putInt("TranPushResolution", resolution).apply();
    }

    public static void setLogPth(Context context, String path) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString("LogPath", path).apply();
    }

    public static String getLogPth(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString("LogPath", "");
    }


}
