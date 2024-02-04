package com.easygbs.device.util;

import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;

import com.easygbs.device.EasyApplication;

import org.easydarwin.util.SIP;

import java.util.ArrayList;
import java.util.List;

public class DataUtil {

    public static void setSIP(SIP sip) {
        PreferenceManager.getDefaultSharedPreferences(EasyApplication.getEasyApplication())
                .edit()
                .putString("serverIp", sip.getServerIp())
                .putInt("serverPort", sip.getServerPort())
                .putInt("localSipPort", sip.getLocalSipPort())
                .putString("serverId", sip.getServerId())
                .putString("serverDomain", sip.getServerDomain())
                .putString("deviceId", sip.getDeviceId())
                .putString("password", sip.getPassword())
                .putInt("protocol", sip.getProtocol())
                .putInt("regExpires", sip.getRegExpires())
                .putInt("heartbeatInterval", sip.getHeartbeatInterval())
                .putInt("heartbeatCount", sip.getHeartbeatCount())
                .apply();
    }

    public static SIP getSIP() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(EasyApplication.getEasyApplication());

        SIP sip = new SIP();
        sip.setServerIp(sp.getString("serverIp", "demo.easygbs.com"));
        sip.setServerPort(sp.getInt("serverPort", 15060));
        sip.setLocalSipPort(sp.getInt("localSipPort", 15060));
        sip.setServerId(sp.getString("serverId", "34020000002000000001"));
        sip.setServerDomain(sp.getString("serverDomain", "3402000000"));
        sip.setDeviceId(sp.getString("deviceId", "34020000001110005555"));
        sip.setPassword(sp.getString("password", "12345678"));
        sip.setProtocol(sp.getInt("protocol", SIP.ProtocolEnum.UDP.getValue()));
        sip.setRegExpires(sp.getInt("regExpires", 3600));
        sip.setHeartbeatInterval(sp.getInt("heartbeatInterval", 30));
        sip.setHeartbeatCount(sp.getInt("heartbeatCount", 3));

        // TODO
        List<SIP.GB28181_CHANNEL_INFO_T> list = new ArrayList<>();
        for (int i = 0; i < 1; i++) {
            SIP.GB28181_CHANNEL_INFO_T item = new SIP.GB28181_CHANNEL_INFO_T();
            item.setName("EasyIPC");
            item.setManufacturer("TSINGSEE");
            item.setModel("EasyGBD");
            item.setParentId(sp.getString("deviceId", "34020000001110005555"));
            item.setIndexCode(sp.getString("indexCode", "34020000001310005554"));
            item.setOwner("Owner");
            item.setCivilCode("CivilCode");
            item.setAddress("Address");
            // TODO 经纬度自己填写
            item.setLongitude(116.397128);
            item.setLatitude(39.916527);

            list.add(item);
        }
        sip.setList(list);

        return sip;
    }

    public static String recordPath() {
        return Environment.getExternalStorageDirectory() +"/EasyGBS";
    }
}
