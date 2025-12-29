package com.mp4;

import android.media.MediaMetadataRetriever;
import android.util.LruCache;

import java.io.File;

public class Mp4Utils {
    // 缓存时长（秒），LRU缓存，最多缓存50个文件
    private static final LruCache<String, Integer> durationCache = new LruCache<>(50);

    /**
     * 获取MP4时长（秒），带缓存
     */
    public static int getMp4DurationSeconds(String filePath) {
        // 检查缓存
        Integer cached = durationCache.get(filePath);
        if (cached != null) return cached;

        int duration = getDurationFromFile(filePath);

        // 缓存结果（只缓存有效时长）
        if (duration > 0) durationCache.put(filePath, duration);

        return duration;
    }

    /**
     * 从文件获取时长
     */
    private static int getDurationFromFile(String filePath) {
        // 检查文件是否存在
        File file = new File(filePath);
        if (!file.exists() || file.length() == 0) return 60;

        MediaMetadataRetriever retriever = null;
        try {
            retriever = new MediaMetadataRetriever();
            retriever.setDataSource(filePath);

            String durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);

            if (durationStr != null) {
                long durationMs = Long.parseLong(durationStr);
                // 转换为秒，四舍五入
                return (int) Math.round(durationMs / 1000.0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (retriever != null) {
                retriever.release();
            }
        }
        return 60; // 默认60秒
    }

    /**
     * 清除缓存
     */
    public static void clearCache() {
        durationCache.evictAll();
    }
}