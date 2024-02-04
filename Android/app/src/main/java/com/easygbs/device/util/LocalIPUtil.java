package com.easygbs.device.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;

public class LocalIPUtil {

    public static String getLocalIp(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();

        if (activeNetworkInfo == null) {
            return null;
        }

        String ip;
        if (activeNetworkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
            // wifi
            ip = getWifiIp(context);
        } else {
            // 不是wifi
            ip = get4gIp();
        }

        Log.i("LocalIPUtil", ip);
        return ip;
    }

    /*
    * 移动网络下获得本地IP地址
    * */
    private static String get4gIp() {
        try {
            String ipv4;
            ArrayList<NetworkInterface> nilist = Collections.list(NetworkInterface.getNetworkInterfaces());

            for (NetworkInterface ni: nilist) {
                ArrayList<InetAddress> ialist = Collections.list(ni.getInetAddresses());

                for (InetAddress address: ialist) {
                    if (!address.isLoopbackAddress() && !address.isLinkLocalAddress()) {
                        ipv4=address.getHostAddress();
                        return ipv4;
                    }
                }
            }
        } catch (SocketException ex) {
            Log.e("localip", ex.toString());
        }

        return null;
    }

    /*
    * wifi下获得本地IP地址
    * */
    private static String getWifiIp(Context context) {
        //获取wifi服务

        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

        //判断wifi是否开启
        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }

        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ipAddress = wifiInfo.getIpAddress();
        String ip = intToIp(ipAddress);

        return ip;
    }

    /* 获取Wifi ip 地址 */
    private static String intToIp(int i) {
        return (i & 0xFF) + "." +
                ((i >> 8) & 0xFF) + "." +
                ((i >> 16) & 0xFF) + "." +
                (i >> 24 & 0xFF);
    }
}
