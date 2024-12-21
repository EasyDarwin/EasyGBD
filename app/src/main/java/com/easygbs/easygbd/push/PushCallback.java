package com.easygbs.easygbd.push;

public class PushCallback {
    private int channelid;
    private int code;
    private String name;

    public PushCallback(int code,String name){
        this.code = code;
        this.name = name;
    }

    public PushCallback(int channelid,int code,String name){
        this.channelid = channelid;
        this.code = code;
        this.name = name;
    }

    public int getChannelid() {
        return channelid;
    }

    public void setChannelid(int channelid) {
        this.channelid = channelid;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
