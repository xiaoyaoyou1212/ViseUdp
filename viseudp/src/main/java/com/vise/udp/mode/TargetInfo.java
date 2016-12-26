package com.vise.udp.mode;

/**
 * @Description:
 * @author: <a href="http://www.xiaoyaoyou1212.com">DAWI</a>
 * @date: 2016-12-21 16:02
 */
public class TargetInfo {
    private String ip;
    private int port;

    public String getIp() {
        return ip;
    }

    public TargetInfo setIp(String ip) {
        this.ip = ip;
        return this;
    }

    public int getPort() {
        return port;
    }

    public TargetInfo setPort(int port) {
        this.port = port;
        return this;
    }

    @Override
    public String toString() {
        return "TargetInfo{" +
                "ip='" + ip + '\'' +
                ", port=" + port +
                '}';
    }
}
