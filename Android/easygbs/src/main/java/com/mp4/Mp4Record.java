package com.mp4;


import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class Mp4Record {
    private int size;           // 文件大小，单位：字节
    private String fileName;
    private int duration;       // 持续时间，单位：秒
    private String startTime;   // 开始时间，格式：yyyy-MM-ddTHH:mm:ss

    // 构造函数
    public Mp4Record(String fileName, int size, String startTime, int duration) {
        this.fileName = fileName;
        this.size = size;
        this.startTime = startTime;
        this.duration = duration;
    }

    // 无参构造函数
    public Mp4Record() {

    }

    // Getter 和 Setter 方法
    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    /**
     * 获取结束时间（根据开始时间和持续时间计算）
     */
    public String getEndTime() {
        try {
            // 解析开始时间
            LocalDateTime start = LocalDateTime.parse(this.startTime);
            LocalDateTime end = start.plusSeconds(this.duration);
            return end.toString();
        } catch (Exception e) {
            return startTime; // 解析失败时返回开始时间
        }
    }

    /**
     * 将对象转换为Map（用于兼容旧代码）
     */
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("fileName", fileName);
        map.put("size", size);
        map.put("startTime", startTime);
        map.put("duration", duration);
        map.put("endTime", getEndTime());
        return map;
    }

    @Override
    public String toString() {
        return "RecordMp4{" + "fileName='" + fileName + '\'' + ", size=" + size + ", startTime='" + startTime + '\'' + ", duration=" + duration + ", endTime='" + getEndTime() + '\'' + '}';
    }
}