package org.easydarwin.util;
import java.util.ArrayList;
import java.util.List;

public class SIP {
    private int ver;
    /**
     * SIP服务器地址
     */
    private String serverIp;
    /**
     * SIP服务器端口
     */
    private int serverPort;
    private int localSipPort;
    /**
     * SIP服务器ID
     */
    private String serverId;
    /**
     * SIP服务器域
     */
    private String serverDomain;
    /**
     * SIP用户名
     */
    private String deviceId;
    private String deviceName;
    /**
     * SIP用户认证密码
     */
    private String password;
    /**
     * 0:udp，1:tcp
     */
    private int protocol;
    /**
     * 注册有效期
     */
    private int regExpires;
    /**
     * 心跳周期
     */
    private int heartbeatInterval;
    /**
     * 最大心跳超时次数
     */
    private int heartbeatCount;

    public int getVer() {
        return ver;
    }

    public void setVer(int ver) {
        this.ver = ver;
    }

    public int getLocalSipPort() {
        return localSipPort;
    }

    public void setLocalSipPort(int localSipPort) {
        this.localSipPort = localSipPort;
    }

    private List<GB28181_CHANNEL_INFO_T> list;

    public String getServerIp() {
        return serverIp;
    }

    public void setServerIp(String serverIp) {
        this.serverIp = serverIp;
    }

    public int getServerPort() {
        return serverPort;
    }

    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }

    public String getServerId() {
        return serverId;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    public String getServerDomain() {
        return serverDomain;
    }

    public void setServerDomain(String serverDomain) {
        this.serverDomain = serverDomain;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getProtocol() {
        return protocol;
    }

    public void setProtocol(int protocol) {
        this.protocol = protocol;
    }

    public int getRegExpires() {
        return regExpires;
    }

    public void setRegExpires(int regExpires) {
        this.regExpires = regExpires;
    }

    public int getHeartbeatInterval() {
        return heartbeatInterval;
    }

    public void setHeartbeatInterval(int heartbeatInterval) {
        this.heartbeatInterval = heartbeatInterval;
    }

    public int getHeartbeatCount() {
        return heartbeatCount;
    }

    public void setHeartbeatCount(int heartbeatCount) {
        this.heartbeatCount = heartbeatCount;
    }

    public enum ProtocolEnum {
        UDP(0),
        TCP(1);

        private int value;

        ProtocolEnum(int v) {
            this.value = v;
        }

        public int getValue() {
            return value;
        }
    }

    public List<GB28181_CHANNEL_INFO_T> getList() {
        if (list == null) {
            list = new ArrayList<>();
        }

        return list;
    }

    public void setList(List<GB28181_CHANNEL_INFO_T> list) {
        this.list = list;
    }

    public static class GB28181_CHANNEL_INFO_T {
        private String name;
        private String manufacturer;
        private String model;
        private String parentId;
        private String owner;
        private String civilCode;
        private String address;
        private double longitude;
        private double latitude;
        /**
         * 国标的通道ID
         */
        private String indexCode;

        public String getIndexCode() {
            return indexCode;
        }

        public void setIndexCode(String indexCode) {
            this.indexCode = indexCode;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getManufacturer() {
            return manufacturer;
        }

        public void setManufacturer(String manufacturer) {
            this.manufacturer = manufacturer;
        }

        public String getModel() {
            return model;
        }

        public void setModel(String model) {
            this.model = model;
        }

        public String getParentId() {
            return parentId;
        }

        public void setParentId(String parentId) {
            this.parentId = parentId;
        }

        public String getOwner() {
            return owner;
        }

        public void setOwner(String owner) {
            this.owner = owner;
        }

        public String getCivilCode() {
            return civilCode;
        }

        public void setCivilCode(String civilCode) {
            this.civilCode = civilCode;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public double getLongitude() {
            return longitude;
        }

        public void setLongitude(double longitude) {
            this.longitude = longitude;
        }

        public double getLatitude() {
            return latitude;
        }

        public void setLatitude(double latitude) {
            this.latitude = latitude;
        }
    }
}
