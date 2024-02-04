package com.easygbs.device.util;

import android.content.Context;
import android.preference.PreferenceManager;

/**
 * SharedPreferences存储工具
 * */
public class SPUtil {

    /* ============================ 使用软编码 ============================ */
    private static final String KEY_SW_CODEC = "key-sw-codec";

    public static boolean getswCodec(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(KEY_SW_CODEC, false);
    }

    public static void setswCodec(Context context, boolean isChecked) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(KEY_SW_CODEC, isChecked)
                .apply();
    }

    /* ============================ 使能H.265编码 ============================ */
    private static final String KEY_HEVC_CODEC = "key-hevc-codec";

    public static boolean getHevcCodec(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(KEY_HEVC_CODEC, false);
    }

    public static void setHevcCodec(Context context, boolean isChecked) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(KEY_HEVC_CODEC, isChecked)
                .apply();
    }

    /* ============================ 使能AAC编码 ============================ */
    private static final String KEY_AAC_CODEC = "key-aac-codec";

    public static boolean getAACCodec(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(KEY_AAC_CODEC, false);
    }

    public static void setAACCodec(Context context, boolean isChecked) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(KEY_AAC_CODEC, isChecked)
                .apply();
    }

    /* ============================ 叠加水印 ============================ */
    private static final String KEY_ENABLE_VIDEO_OVERLAY = "key_enable_video_overlay";

    public static boolean getEnableVideoOverlay(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(KEY_ENABLE_VIDEO_OVERLAY, false);
    }

    public static void setEnableVideoOverlay(Context context, boolean isChecked) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(KEY_ENABLE_VIDEO_OVERLAY, isChecked)
                .apply();
    }

    /* ============================ 码率 ============================ */
    private static final String KEY_BITRATE_ADDED_KBPS = "bitrate_added_kbps";

    public static int getBitrateKbps(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getInt(KEY_BITRATE_ADDED_KBPS, 300000);
    }

    public static void setBitrateKbps(Context context, int value) {
         PreferenceManager.getDefaultSharedPreferences(context)
                 .edit()
                 .putInt(KEY_BITRATE_ADDED_KBPS, value)
                 .apply();
    }

//    /* ============================ 使能摄像头后台采集 ============================ */
//    private static final String KEY_ENABLE_BACKGROUND_CAMERA = "key_enable_background_camera";
//
//    public static boolean getEnableBackgroundCamera(Context context) {
//        return PreferenceManager.getDefaultSharedPreferences(context)
//                .getBoolean(KEY_ENABLE_BACKGROUND_CAMERA, false);
//    }
//
//    public static void setEnableBackgroundCamera(Context context, boolean value) {
//        PreferenceManager.getDefaultSharedPreferences(context)
//                .edit()
//                .putBoolean(KEY_ENABLE_BACKGROUND_CAMERA, value)
//                .apply();
//    }

    /* ============================ 推送视频 ============================ */
    private static final String KEY_ENABLE_VIDEO = "key-enable-video";

    public static boolean getEnableVideo(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(KEY_ENABLE_VIDEO, true);
    }

    public static void setEnableVideo(Context context, boolean value) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(KEY_ENABLE_VIDEO, value)
                .apply();
    }

    /* ============================ 推送音频 ============================ */

    private static final String KEY_ENABLE_AUDIO = "key-enable-audio";

    public static boolean getEnableAudio(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(KEY_ENABLE_AUDIO, true);
    }

    public static void setEnableAudio(Context context, boolean value) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(KEY_ENABLE_AUDIO, value)
                .apply();
    }
}
