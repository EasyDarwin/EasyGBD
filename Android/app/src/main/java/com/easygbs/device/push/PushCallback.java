package com.easygbs.device.push;

/**
 * 推流状态的回调
 */
public class PushCallback {
    private int code;
    private String name;

    public PushCallback(int code, String name) {
        this.code = code;
        this.name = name;
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
