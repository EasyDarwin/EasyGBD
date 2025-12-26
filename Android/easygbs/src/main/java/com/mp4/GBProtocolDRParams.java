package com.mp4;


import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;


public class GBProtocolDRParams {
    private String endTime;
    private String startTime;
    private float playSpeed;
    private float downloadSpeed;

    // 无参构造函数
    public GBProtocolDRParams() {

    }

    // 带参构造函数
    public GBProtocolDRParams(String startTime, String endTime, float scale, float downloadSpeed) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.playSpeed = scale;
        this.downloadSpeed = downloadSpeed;
    }

    // Getter和Setter方法
    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public float getPlaySpeed() {
        return playSpeed;
    }

    public void setPlaySpeed(float scale) {
        this.playSpeed = scale;
    }

    public float getDownloadSpeed() {
        return downloadSpeed;
    }

    public void setDownloadSpeed(float downloadSpeed) {
        this.downloadSpeed = downloadSpeed;
    }

    public static GBProtocolDRParams fromJson(String jsonString) {

        GBProtocolDRParams model = new GBProtocolDRParams();

        try {
            JSONObject json = new JSONObject(jsonString);

            // 解析并赋值
            if (json.has("startTime")) {
                String startTime = json.getString("startTime");
                model.setStartTime(startTime);
            }

            if (json.has("endTime")) {
                String endTime = json.getString("endTime");
                model.setEndTime(endTime);
            }

            if (json.has("scale")) {
                model.setPlaySpeed(json.getLong("scale"));
            }

            if (json.has("downloadSpeed")) {
                model.setDownloadSpeed(json.getLong("downloadSpeed"));
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        return model;
    }

    public static GBProtocolDRParams fromJson(byte[] param) {
        GBProtocolDRParams model = new GBProtocolDRParams();

        if (param == null || param.length == 0) return model;

        String jsonString = new String(param, StandardCharsets.UTF_8);

        try {
            JSONObject json = new JSONObject(jsonString);

            // 解析并赋值
            if (json.has("startTime")) {
                String startTime = json.getString("startTime");
                model.setStartTime(startTime);
            }

            if (json.has("endTime")) {
                String endTime = json.getString("endTime");
                model.setEndTime(endTime);
            }

            if (json.has("scale")) {
                model.setPlaySpeed(json.getLong("scale"));
            }

            if (json.has("downloadSpeed")) {
                model.setDownloadSpeed(json.getLong("downloadSpeed"));
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        return model;
    }

    // toString方法，方便调试
    @Override
    public String toString() {
        return "{ " + "startTime=" + startTime + ", endTime=" + endTime + ", playSpeed=" + playSpeed + ", downloadSpeed=" + downloadSpeed + " } ";
    }


}