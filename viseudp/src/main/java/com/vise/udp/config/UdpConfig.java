package com.vise.udp.config;

import com.vise.udp.core.inter.IData;
import com.vise.udp.handler.ClientDiscoveryHandler;
import com.vise.udp.handler.ServerDiscoveryHandler;

/**
 * @Description:
 * @author: <a href="http://www.xiaoyaoyou1212.com">DAWI</a>
 * @date: 2016-12-21 15:58
 */
public class UdpConfig implements IUdpConfig {

    private static UdpConfig instance;
    private String ip;
    private int port;
    private long keepAliveMillis;
    private int bufferSize;
    private IData dataDispose;
    private ServerDiscoveryHandler serverDiscoveryHandler;
    private ClientDiscoveryHandler clientDiscoveryHandler;

    private UdpConfig() {

    }

    public static UdpConfig getInstance() {
        if (instance == null) {
            synchronized (UdpConfig.class) {
                if (instance == null) {
                    instance = new UdpConfig();
                }
            }
        }
        return instance;
    }

    @Override
    public IUdpConfig setIp(String ip) {
        this.ip = ip;
        return instance;
    }

    @Override
    public IUdpConfig setPort(int port) {
        this.port = port;
        return instance;
    }

    @Override
    public IUdpConfig setKeepAliveMillis(long time) {
        this.keepAliveMillis = time;
        return instance;
    }

    @Override
    public IUdpConfig setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
        return instance;
    }

    @Override
    public IUdpConfig setDataDispose(IData dataDispose) {
        this.dataDispose = dataDispose;
        return instance;
    }

    @Override
    public IUdpConfig setDiscoveryHandler(ServerDiscoveryHandler serverDiscoveryHandler) {
        this.serverDiscoveryHandler = serverDiscoveryHandler;
        return instance;
    }

    @Override
    public IUdpConfig setDiscoveryHandler(ClientDiscoveryHandler clientDiscoveryHandler) {
        this.clientDiscoveryHandler = clientDiscoveryHandler;
        return instance;
    }

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    public long getKeepAliveMillis() {
        return keepAliveMillis;
    }

    public int getBufferSize() {
        return bufferSize;
    }

    public IData getDataDispose() {
        return dataDispose;
    }

    public ServerDiscoveryHandler getServerDiscoveryHandler() {
        return serverDiscoveryHandler;
    }

    public ClientDiscoveryHandler getClientDiscoveryHandler() {
        return clientDiscoveryHandler;
    }
}
