package com.easygbs;

import android.content.Context;
import android.util.Log;


import com.mp4.Mp4Filter;
import com.mp4.Mp4Record;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GbFileHelper {

    private static String TAG = "GbFileHelper";

    private static Context mContext;

    private static final DateTimeFormatter FORMATTER_DIGIT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss").withLocale(Locale.US);
    // 输出格式：2025-11-30T13:39:56
    private static final DateTimeFormatter FORMATTER_ISO = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss").withLocale(Locale.US);

    // 任务ID计数器
    private static int taskId = 0;

    // 存储 params -> taskId 的映射
    private static final Map<String, Integer> paramsToTaskIdMap = new HashMap<>();

    // 存储 taskId -> RecordMp4 列表的映射
    private static final Map<Integer, List<Mp4Record>> taskIdToRecordFilesMap = new HashMap<>();


    public static void setContext(Context context) {
        mContext = context;
    }

    //    {StartTime:"2025-12-10T00:00:00",EndTime:"2025-12-10T23:59:59","cIndex":"0"}
    public static int getTaskID(byte[] param) {

        if (param == null) {
            Log.e(TAG, "getTaskID param is null");
            return -1;
        }
        String params = new String(param, StandardCharsets.UTF_8);
        // 如果参数已存在，返回已有的taskId
        Integer existingTaskId = paramsToTaskIdMap.get(params);
        if (existingTaskId != null) {
            return existingTaskId;
        }

        // 生成新的taskId
        int newTaskId = ++taskId;

        // 保存映射关系
        paramsToTaskIdMap.put(params, newTaskId);

        // 为新的taskId创建文件信息列表（根据params参数动态生成文件列表）
        List<Mp4Record> fileList = getMp4RecordFiles(params);
        taskIdToRecordFilesMap.put(newTaskId, fileList);

        return newTaskId;
    }

    public static int getSumNum(int taskId) {
        if (!taskIdToRecordFilesMap.containsKey(taskId)) {
            return 0; // 无效的taskId返回0
        }
        return taskIdToRecordFilesMap.get(taskId).size();
    }

    public static int getFileSize(int taskId, int index) {
        List<Mp4Record> fileList = taskIdToRecordFilesMap.get(taskId);
        if (fileList == null || index < 0 || index >= fileList.size()) {
            return -1;
        }
        return fileList.get(index).getSize();
    }

    public static String getName(int taskId, int index) {
        List<Mp4Record> fileList = taskIdToRecordFilesMap.get(taskId);
        if (fileList == null || index < 0 || index >= fileList.size()) {
            return null;
        }
        return fileList.get(index).getFileName();
    }

    public static String getStartTime(int taskId, int index) {
        List<Mp4Record> fileList = taskIdToRecordFilesMap.get(taskId);
        if (fileList == null || index < 0 || index >= fileList.size()) {
            return null;
        }
        return fileList.get(index).getStartTime();
    }

    public static String getEndTime(int taskId, int index) {
        List<Mp4Record> fileList = taskIdToRecordFilesMap.get(taskId);
        if (fileList == null || index < 0 || index >= fileList.size()) {
            return null;
        }
        return fileList.get(index).getEndTime();
    }

    public static void cleanTaskID(int taskId) {
        // 清理映射关系
        taskIdToRecordFilesMap.remove(taskId);

        // 从params映射中移除对应的条目
        paramsToTaskIdMap.entrySet().removeIf(entry -> entry.getValue() == taskId);
    }

    /**
     * 根据params参数生成文件列表
     * 这里可以根据params中的时间范围、通道等信息动态生成
     * {startTime:"2025-12-10T00:00:00",endTime:"2025-12-10T23:59:59",cIndex:"0"}
     */
    private static List<Mp4Record> getMp4RecordFiles(String params) {
        List<Mp4Record> fileList = new ArrayList<>();
        try {

            JSONObject jsonParams = new JSONObject(params);
            String startTime = jsonParams.getString("startTime");
            String endTime = jsonParams.getString("endTime");
            int cIndex = jsonParams.getInt("cIndex");

            String childPath = String.format("CH%d", cIndex + 1);
            String mp4PFolder = new File(mContext.getExternalFilesDir(null), childPath).getPath();


            // 解析params，这里假设params格式为JSON字符串
            // TODO: 根据params的实际内容动态生成文件列表
            List<File> result = Mp4Filter.filterByTime(mp4PFolder, startTime, endTime);
            if (!result.isEmpty()) {
                for (File item : result) {
                    String fileName = item.getName();
                    // 最简单的方式：直接提取和格式化
                    String startTimeISO = getStartTimeByFileName(fileName);
                    Mp4Record record = new Mp4Record(item.getName(), 0, startTimeISO, 60);  //大小 时长展示写死 如果要读取文件的真实大小和时长 可能需要消耗性能
                    fileList.add(record);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return fileList;
    }


    /**
     * 新增方法：获取taskId对应的所有文件信息
     */
    public static List<Mp4Record> getRecordFiles(int taskId) {
        return taskIdToRecordFilesMap.get(taskId);
    }

    /**
     * 新增方法：获取taskId对应的所有文件信息（Map格式，用于兼容旧代码）
     */
    public static List<Map<String, Object>> getRecordFilesAsMap(int taskId) {
        List<Mp4Record> recordList = taskIdToRecordFilesMap.get(taskId);
        if (recordList == null) {
            return Collections.emptyList();
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (Mp4Record record : recordList) {
            result.add(record.toMap());
        }
        return result;
    }

    /**
     * 新增方法：根据params获取taskId（不创建新的）
     */
    public static Integer getTaskIdByParams(String params) {
        return paramsToTaskIdMap.get(params);
    }

    /**
     * 新增方法：添加文件到指定任务
     */
    public static void addRecordFile(int taskId, Mp4Record record) {
        if (!taskIdToRecordFilesMap.containsKey(taskId)) {
            taskIdToRecordFilesMap.put(taskId, new ArrayList<>());
        }
        taskIdToRecordFilesMap.get(taskId).add(record);
    }

    /**
     * 新增方法：更新文件信息
     */
    public static boolean updateRecordFile(int taskId, int index, Mp4Record newRecord) {
        List<Mp4Record> fileList = taskIdToRecordFilesMap.get(taskId);
        if (fileList == null || index < 0 || index >= fileList.size()) {
            return false;
        }
        fileList.set(index, newRecord);
        return true;
    }

    /**
     * 新增方法：根据文件名查找文件
     */
    public static Mp4Record findRecordByFileName(int taskId, String fileName) {
        List<Mp4Record> fileList = taskIdToRecordFilesMap.get(taskId);
        if (fileList == null) {
            return null;
        }

        for (Mp4Record record : fileList) {
            if (fileName.equals(record.getFileName())) {
                return record;
            }
        }
        return null;
    }

    /**
     * 新增方法：删除指定文件
     */
    public static boolean deleteRecordFile(int taskId, int index) {
        List<Mp4Record> fileList = taskIdToRecordFilesMap.get(taskId);
        if (fileList == null || index < 0 || index >= fileList.size()) {
            return false;
        }
        fileList.remove(index);
        return true;
    }

    private static String getStartTimeByFileName(String fileName) {
        String dateStr = "";
        String startTime = "";
        Pattern pattern = Pattern.compile("CH\\d+_(\\d{14})");
        Matcher matcher = pattern.matcher(fileName);
        if (matcher.find()) dateStr = matcher.group(1);
        try {
            LocalDateTime dateTime = LocalDateTime.parse(dateStr, FORMATTER_DIGIT);
            startTime = dateTime.format(FORMATTER_ISO);
        } catch (DateTimeParseException e) {
            Log.e(TAG, "getStartTimeByFileName 解析失败: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            Log.e(TAG, "getStartTimeByFileName 其他错误: " + e.getMessage());
            e.printStackTrace();
        }
        return startTime;
    }


}