package com.mp4;

import android.util.Log;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Mp4Filter {

    private static final String TAG = "Mp4Filter";

    // ISO 格式解析器
    private static final SimpleDateFormat isoSdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());

    // 文件名格式解析器 yyyyMMddHHmmssSSS
    private static final SimpleDateFormat fileNameSdf = new SimpleDateFormat("yyyyMMddHHmmssSSS", Locale.getDefault());

    /**
     * 将时间字符串或数字解析为毫秒
     * 支持三种格式：
     * 1. 文件名 17 位数字：20251130133956100
     * 2. ISO 日期字符串：2025-05-05T02:03:04
     * 3. 毫秒时间戳字符串
     */
    public static long parseTime(String time) {
        if (time == null || time.isEmpty()) return -1;

        try {
            // 17位文件名格式
            if (time.matches("\\d{17}")) {
                Date d = fileNameSdf.parse(time);
                if (d != null) return d.getTime();
            }

            // ISO 日期格式
            if (time.matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}")) {
                Date d = isoSdf.parse(time);
                if (d != null) return d.getTime();
            }

            // 毫秒时间戳字符串
            if (time.matches("\\d+")) {
                return Long.parseLong(time);
            }

        } catch (ParseException | NumberFormatException e) {
            Log.e(TAG, "parseTime error: " + e.getMessage());
        }

        Log.e(TAG, "parseTime无法解析: " + time);
        return -1;
    }

    /**
     * 从文件名解析开始时间（返回毫秒）
     * 支持格式：xxx_yyyyMMddHHmmssSSS.mp4
     */
    public static long parseFileStart(File f) {
        try {
            String name = f.getName();
            int start = name.indexOf("_") + 1;
            int end = name.lastIndexOf(".");
            if (start <= 0 || end <= start) return -1;

            String timeStr = name.substring(start, end);
            Date d = fileNameSdf.parse(timeStr);
            if (d != null) return d.getTime();
        } catch (Exception e) {
            Log.e(TAG, "parseFileStart error: " + e.getMessage());
        }
        return -1;
    }

    /**
     * 获取 MP4 时长（毫秒）
     * 统一返回默认 60 秒
     */
    public static long getMp4Duration(File file) {
        return 60_000; // 默认 60 秒
    }

    /**
     * 扫描父目录下所有 MP4 文件
     */
    public static List<File> scanMp4Files(String parentDirPath) {
        List<File> files = new ArrayList<>();
        File parentDir = new File(parentDirPath);
        if (!parentDir.exists() || !parentDir.isDirectory()) return files;

        File[] allFiles = parentDir.listFiles();
        if (allFiles == null) return files;

        for (File f : allFiles) {
            if (f.isFile() && f.getName().toLowerCase().endsWith(".mp4")) {
                files.add(f);
            }
        }

        return files;
    }

    /**
     * 根据父目录 + start/end 时间筛选 MP4 文件
     * start/end 支持三种格式（文件名17位、ISO字符串、毫秒数字）
     * 修正逻辑：自动扩展查询 end，保证边界文件也能被选中
     */
    public static List<File> filterByTime(String parentDirPath, String start, String end) {
        long queryStart = parseTime(start);
        long queryEnd = parseTime(end);

        // 新增：如果查询结束时间大于当前系统时间，修正为系统时间
        long currentTime = System.currentTimeMillis();
        if (queryEnd > currentTime) {
            queryEnd = currentTime;
            Log.d(TAG, "filterByTime: 结束时间已修正为当前系统时间");
        }

        if (queryStart == -1 || queryEnd == -1 || queryEnd < queryStart) {
            Log.e(TAG, "filterByTime: start/end 无效");
            return new ArrayList<>();
        }

        List<File> allFiles = scanMp4Files(parentDirPath);
        List<File> result = new ArrayList<>();

        for (File f : allFiles) {
            long fileStart = parseFileStart(f);
            if (fileStart == -1) continue;

            long fileEnd = fileStart + getMp4Duration(f); // 默认 60 秒

            // 文件区间与查询区间有交集即可
            if (fileEnd > queryStart && fileStart < queryEnd) {
                result.add(f);
            }
        }

        // 按开始时间排序
        result.sort(Comparator.comparingLong(Mp4Filter::parseFileStart));

        return result;
    }
}
